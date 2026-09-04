# Composite Building Blocks

Instantiating a building block as a single, self-contained component.

## 1. Goal

Today a building block is a named list of components stored in local config
(`buildingBlocks` key). Placing one clones the components, gives each a fresh
name and UUID, and drops them on the canvas as an auto-group. They remain
individually selectable, editable and stretchable.

This document plans a second instantiation mode. In **composite mode** a block
becomes one component on the canvas: it has its own name, it aggregates the
control points of all its children, it moves and rotates as a rigid unit, and it
appears as a single entry in the BOM, the netlist and the AI project
description. This makes it possible to build something like an Arduino board out
of an existing board component plus pin headers, save it as a block, and from
then on treat it as a first-class part that other components can connect to.

Group mode is kept unchanged. Composite mode becomes the default for a plain
click; group mode costs one extra click.

## 2. Design decisions

| # | Decision | Choice | Rationale |
|---|---|---|---|
| D1 | Where child definitions live | Embedded in the instance, serialized into the `.diy` file | Projects stay portable; a file opens correctly on a machine that does not have the block in its local config. Blocks in config remain the *template*, not a live dependency. |
| D2 | Which control points are exposed | All of them, delegated straight through to children | No curation UI needed, and `isControlPointSticky` / `getControlPointVisibilityPolicy` come along for free with correct per-child semantics. |
| D3 | Terminal naming | `getSectionNames(i)` returns the child's name; `getControlPointNodeName(i)` returns the child's node name | `Node.toString()` already renders `component.getName() + "." + section + "." + nodeName`, so a netlist node reads `ARD1.PH1.3` with no changes to the netlist code. |
| D4 | Geometry | Rigid — `canPointMoveFreely(i)` returns `false` for every index | This is already a first-class concept used by ~60 components (`DIL_IC`, `Breadboard`, `TubeSocket`…). No new plumbing. |
| D5 | Type identity | One shared `ComponentType`; the instance carries a `blockName` field, and the handful of type-name consumers prefer it | `ComponentProcessor` caches `ComponentType` per Java class, so per-block types would mean changing the cache key of a widely used singleton. The field approach has a much smaller blast radius. |
| D6 | Electrical modelling | The composite is **not** `IContinuity` and **not** `ISwitch` | A composite is a footprint that exposes terminals, not a circuit model. Internal point-to-point continuity and internal switching are explicitly out of scope. |
| D7 | Internal copper | Left alone — child copper still registers continuity areas | Children draw through the same `G2DWrapper`, so `startTrackingContinuityArea` calls land on the composite naturally. A block built from a board with copper pours still conducts, which matches physical reality. Zero code. |
| D8 | Child properties | Opaque — the property editor shows only the composite's own properties | Matches the "single component" mental model. Children are frozen as saved. Because children are serialized anyway (D1), flattened child-property editing can be layered on later with no file-format change. |
| D9 | Rotation / mirroring | Supported, by delegating to each child's own `IComponentTransformer` around a shared centre | Reuses existing per-component transformers. If any child's type has no transformer, the composite reports that it cannot rotate. |
| D10 | Mode selection | Composite is the global default on click; group mode via the existing drop-down / right-click popup | Most uses want the new behaviour, and it costs no extra click. |
| D11 | BOM | One row per composite, keyed by block name | Consistent with D5 and D8. |
| D12 | Explode / collapse | Not in this change | The only way to get a composite is to place a block in composite mode; the only way to get loose components is group mode. Keeps the surface area small. |

### Explicitly out of scope

- Converting a placed composite back into loose components, or collapsing an
  arbitrary selection into a composite (D12).
- Editing child properties after placement (D8).
- Internal switches. A block containing an `ISwitch` will place fine, but the
  switch is inert: it contributes terminals and drawing, no switching
  behaviour (D6). See §7.
- Pushing block-library edits into already-placed instances (D1).

## 3. Why this fits the existing architecture

Three mechanisms already exist and carry most of the weight.

**Rigidity.** `Presenter` reads `canPointMoveFreely` in exactly three places:
`Presenter.java:868` excludes the point from individual dragging,
`Presenter.java:1243` forces *all* control points into the drag set when the
component is stuck to something, and `Presenter.java:1418` marks the move as
rigid in `moveComponents`. Returning `false` everywhere yields "moves as a unit,
never stretches" with no new code.

**Placement.** `Presenter.loadBlock` (`Presenter.java:2847`) already routes
through `pasteComponents(new ComponentTransferable(components, blockName), true,
true)`. Composite mode simply pastes a one-element list. Note that
`InstantiationManager.pasteComponents` and `updateSingleClick` position
components by translating *every* control point by the same delta via
`setControlPoint(p, i)` — so `setControlPoint` must delegate per-point to the
child. Rigidity is enforced by `canPointMoveFreely`, never by `setControlPoint`
refusing a write.

**Node naming.** `Node.getDisplayName()` prefixes the node name with
`getSectionNames(pointIndex)[0]` when there is exactly one section, and
`Node.toString()` prefixes that with the component name. Returning the child's
name as the section gives hierarchical netlist labels for free.

Three things do *not* come for free and are the substance of the work: type
identity (D5), palette visibility (§4.2), and a correct deep `clone()` (§7).

## 4. Implementation

### 4.1 New: `CompositeComponent`

`diylc-core/src/main/java/org/diylc/components/composite/CompositeComponent.java`

The package must sit under `org.diylc.components` — `ComponentProcessor`
restricts its Reflections scan with
`filterInputsBy(new FilterBuilder().includePackage("org.diylc.components"))`, and
`ProjectFileManager.configure` declares
`xStream.aliasPackage("diylc", "org.diylc.components")`, so the class serializes
as `diylc.composite.CompositeComponent` with no extra XStream configuration.

```java
@ComponentDescriptor(
    name = "Building Block",
    description = "A group of components instantiated as a single rigid component",
    category = "Building Blocks",
    instanceNamePrefix = "BLK",
    author = "Branislav Stojkovic",
    zOrder = IDIYComponent.COMPONENT,
    flexibleZOrder = true,
    bomPolicy = BomPolicy.SHOW_ALL_NAMES,
    autoEdit = false,
    enableCache = false,
    transformer = CompositeComponentTransformer.class,
    hiddenInPalette = true)
public class CompositeComponent extends AbstractComponent<Void> {

  private List<IDIYComponent<?>> components;   // embedded children (D1)
  private String blockName;                    // type identity (D5)

  private transient int[] childOf;             // flat index -> child ordinal
  private transient int[] pointOf;             // flat index -> child point index
}
```

The `@ComponentDescriptor` annotation is mandatory, not cosmetic:
`extractComponentTypeFrom` returns `null` without it, and `NetlistBuilder:69`
and `:401` dereference the result.

**Point index mapping.** A flat index is built lazily from the children and
cached in the two transient arrays. Children never change after construction
(D8), so the cache never needs invalidating; it only needs rebuilding after
deserialization, which the lazy getter handles.

**Delegating members**

| Member | Behaviour |
|---|---|
| `getControlPointCount()` | Sum over children |
| `getControlPoint(i)` | Delegate |
| `setControlPoint(p, i)` | Delegate (required for placement — see §3) |
| `isControlPointSticky(i)` | Delegate |
| `canControlPointOverlap(i)` | Delegate |
| `getControlPointVisibilityPolicy(i)` | Delegate |
| `getControlPointNodeName(i)` | Child's node name, prefixed with the child's own section name when it has exactly one |
| `getSectionNames(i)` | `new String[] { child.getName() }` |
| `canPointMoveFreely(i)` | `false` (D4) |
| `createdIn(project)` | Forward to every child |

**Own members**

- `draw(...)` — forwards to each child in order with the same `Graphics2D` and
  `IDrawingObserver`, passing `ComponentState.NORMAL` so children do not each
  paint their own red highlight. When `state != NORMAL` the composite then draws
  a single dashed outline around the aggregate bounds. (DIYLC has no central
  selection rendering; components draw their own, so the composite must draw
  its own.)
- `drawIcon(...)` — a generic block glyph. `extractComponentTypeFrom` calls
  `drawIcon` on a default-constructed instance with no children, so this must
  not touch `components`.
- `getValue()` / `setValue()` — `null` / no-op. `getValueForDisplay()` inherits
  the `AbstractComponent` behaviour of returning `""` for a null value, which
  keeps the BOM row (`BomMaker` skips rows with a null value).
- `getBlockName()` — `@EditableProperty(name = "Block")`, read-only in practice.
- `clone()` — **must be overridden.** `AbstractComponent.clone()` copies fields
  reflectively and only deep-copies `Point2D` and `Point2D[]`. The `components`
  list would be shared by reference between clones, so moving one instance would
  move every copy. The override deep-clones each child (via each child's own
  `clone()`, which preserves its id, matching `AbstractComponent.clone()`'s
  contract) and leaves the transient index arrays to rebuild lazily. `Building
  BlockManager.loadBlock` is a separate path and explicitly assigns fresh ids
  when it clones source components to place a brand new instance. See §7.

### 4.2 Hiding the type from the palette

`ComponentProcessor.getComponentTypes()` groups every annotated class by
category and feeds the toolbox tabs and the component tree. A composite is never
created from the palette, so it should not appear there — but it still needs a
resolvable `ComponentType` for the netlist, BOM and name generation.

Add `boolean hiddenInPalette() default false` to `ComponentDescriptor`, carry it
on `ComponentType`, and skip such types inside `getComponentTypes()`.
`extractComponentTypeFrom` is unaffected, so every existing consumer keeps
working. This is one new annotation attribute and one `continue`.

### 4.3 New: `CompositeComponentTransformer`

`diylc-core/src/main/java/org/diylc/components/composite/CompositeComponentTransformer.java`

```java
canRotate(c)              -> every child's ComponentType has a transformer that canRotate(child)
canMirror(c)              -> same, for canMirror
mirroringChangesCircuit() -> true if any child's transformer says so
rotate(c, center, dir)    -> for each child: childType.getTransformer().rotate(child, center, dir)
mirror(c, center, dir)    -> likewise
```

Passing the same `center` to every child keeps the assembly geometrically
consistent. A child whose type declares no transformer makes the whole composite
non-rotatable, which is honest — silently leaving one child un-rotated would be
worse.

### 4.4 Instantiation mode

New enum `org.diylc.common.BlockInstantiationMode { GROUP, COMPOSITE }`.

`IBlockProcessor` gains `loadBlock(String blockName, BlockInstantiationMode mode)`.
The existing single-argument `loadBlock` is kept and delegates with
`COMPOSITE`, so the default is the new behaviour (D10) and no call site is
left behind.

`BuildingBlockManager.loadBlock(blockName, existingComponents, mode)`:

- `GROUP` — exactly today's behaviour.
- `COMPOSITE` — clone the stored components with fresh UUIDs but **keep their
  saved names unchanged**. Child names are namespaced under the composite's name
  (D3), so running them through `createUniqueName` against the project would
  produce meaningless drift (`R1` becoming `R37`) and break netlist labels
  between two instances of the same block. Then wrap them in a
  `CompositeComponent`, set `blockName`, and give the composite itself a
  project-unique name via `createUniqueName`.

`Presenter.loadBlock` passes the mode through. For composite mode, call
`pasteComponents(..., autoGroup = false, ...)` — there is nothing to group.

### 4.5 Type-name consumers (D5)

Add a helper so the special case lives in one place:

```java
// ComponentProcessor
public static String getDisplayTypeName(IDIYComponent<?> c, ComponentType type) {
  if (c instanceof CompositeComponent cc && cc.getBlockName() != null)
    return cc.getBlockName();
  return type.getName();
}
```

Call sites to update:

| File | Line | Effect |
|---|---|---|
| `AiProjectBuilder.mapComponent` | `:178` | The AI sees `"Arduino Uno"` instead of `"Building Block"` — the main reason for D5 |
| `BomMaker.getBom` | `:80`, `:100-101` | Grouping key and BOM row label become the block name (D11) |
| `Presenter.selectMatching` | `:1064` | "select matching" finds blocks by their block name |

`SpiceSumarizer:94,107` should be audited but likely needs no change — a
composite is not a recognised SPICE element and will fall through as it does for
any other unmodelled component.

### 4.6 UI wiring

Four call sites reach `loadBlock`, all through `IPlugInPort`:

| File | Line | Change |
|---|---|---|
| `ComponentButtonFactory.createBuildingBlockButton` | `:319` | Default action → `COMPOSITE` |
| `CustomTreeModel` (favorites) | `:145` | Click → `COMPOSITE` |
| `CustomTreeModel` (Building Blocks category) | `:185` | Click → `COMPOSITE` |
| `CanvasPanel.functionKeyPressed` | `:255` | F-key shortcut → `COMPOSITE` |

Group mode is reached from the drop-down popup the toolbox button already
carries. `ComponentTabbedPane.createVariantPopup(null, blockName)` builds it for
blocks today (currently only the "Assign Shortcut" submenu); add an **"Insert as
Separate Components"** item at the top. Mirror it in the tree's right-click menu
for block nodes.

## 5. Serialization and compatibility

- **Existing `.diy` files** are unaffected — they contain no composites.
- **Existing blocks in config** are unaffected. The mode is chosen at placement,
  not stored on the block, so `buildingBlocks` keeps its
  `Map<String, List<IDIYComponent<?>>>` shape and needs no migration. Default
  and imported blocks work in both modes immediately.
- **New `.diy` files containing composites** cannot be opened by older DIYLC
  versions. Expected and unavoidable given D1.
- Children serialize as ordinary nested `diylc.*` elements inside the
  composite's `components` list, so every existing component converter applies
  unchanged.

## 6. Suggested order of work

1. `CompositeComponent` with the delegating members, the point index map, a
   correct `clone()`, and `draw`. No UI yet — exercise it from a unit test that
   builds a composite directly and asserts point counts, node names and rigidity.
2. `hiddenInPalette` on `ComponentDescriptor` / `ComponentType` /
   `getComponentTypes()`.
3. `BlockInstantiationMode`, the `IBlockProcessor` overload,
   `BuildingBlockManager.loadBlock`, `Presenter.loadBlock`.
4. UI wiring: four default call sites plus the "Insert as Separate Components"
   popup items.
5. `getDisplayTypeName` and its three consumers.
6. `CompositeComponentTransformer`, and rotation/mirroring enablement.
7. Round-trip and regression tests (§8).

Steps 1–4 are the minimum for a usable feature; 5–6 complete it.

## 7. Risks

**`clone()` is the sharp edge.** `AbstractComponent.clone()` is reflective and
only deep-copies points. Without an override, two placed copies of the same
composite share one child list, and moving one moves both. This will not fail
loudly — it will look like a bizarre rendering bug. It needs a dedicated test.

**Z-order.** A composite occupies a single slot in `project.getComponents()`, so
children spanning the board, trace and component layers all render at that one
slot. For a board-like block this is correct. It does mean an external trace
cannot be sandwiched between a block's board and the parts mounted on it. The
descriptor uses `zOrder = COMPONENT` with `flexibleZOrder = true`; deriving the
z-order from the lowest child at save time is a possible refinement if this
proves annoying in practice.

**Inert internal switches.** Per D6 the composite is not `ISwitch`, so a block
containing a switch places and draws correctly but contributes no switching to
the netlist. This is a silent difference from group mode. Worth a log warning
when a composite is created from a selection containing an `ISwitch`, and worth
mentioning in the release notes.

**Variants.** `Presenter.getVariantsFor` keys on `ComponentType`, so all
composites share one variant pool regardless of block. Applying a variant to a
composite would be meaningless. Suppress the variant UI for `CompositeComponent`
selections.

**Naming collisions.** Two instances of the same block deliberately carry
identically named children (§4.4). Netlist labels stay unique because they are
prefixed with the composite's own name (`ARD1.PH1.3` vs `ARD2.PH1.3`). Any code
that assumes component names are globally unique across the project would be
affected — none is known, but worth watching.

## 8. Testing

- **Unit** — point count equals the sum over children; `getControlPoint(i)`
  tracks the child after the child moves; `canPointMoveFreely` is false for
  every index; `getSectionNames`/`getControlPointNodeName` produce
  `ARD1.PH1.3` through `Node.toString()`.
- **Clone isolation** — clone a composite, move the clone, assert the original's
  points did not move.
- **Round trip** — save a project containing a composite, reload it, assert the
  point count, names and geometry survive, and that the transient index arrays
  rebuild lazily.
- **Both modes** — place the same block in group and composite mode; assert the
  group yields N components and the composite yields one with the same total
  point count and the same terminal coordinates.
- **Connectivity** — place a composite built from a board with copper, run the
  netlist, confirm internal copper still forms continuity areas (D7) while no
  `IContinuity` point-to-point connections are reported (D6).
- **BOM** — two instances of one block collapse to a single row with quantity 2,
  labelled by block name.
- **Regression** — run the existing `diylc-regression-data` suite to confirm
  nothing in the ordinary drawing/netlist path shifted.
