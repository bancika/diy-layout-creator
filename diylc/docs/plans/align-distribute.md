# Align & Distribute

Align or distribute the selected components by their drawn outline. The commands sit in an
**Align & Distribute** submenu in both the Edit menu and the canvas popup menu.

## 1. Behaviour

Every operation works on the outline `Area` that `DrawingManager` records for each component
(`ComponentArea.getOutlineArea()`). Because that area covers everything the component draws,
including strokes and labels, alignment means visual alignment, not control-point alignment.

**Units.** The selection is split into *units*. A unit is one component group (the union of its
members' outline areas) or one ungrouped component. A unit always moves rigidly, so groups are
never pulled apart. A selected component with no recorded area, for example because it was not
drawn, is left out of the operation.

**Reference.** Everything is measured against the bounding box of all units, so align and
distribute never move anything outside the space the selection already covers.

| Command | Effect on each unit |
| --- | --- |
| Align Left / Right | Moves its left (right) edge to the bbox's left (right) edge |
| Align Top / Bottom | Moves its top (bottom) edge to the bbox's top (bottom) edge |
| Align Horizontal Centers | Moves its center X to the bbox's center X (units end up in a column) |
| Align Vertical Centers | Moves its center Y to the bbox's center Y (units end up in a row) |
| Distribute Centers Horizontally / Vertically | Sorts units by center, keeps the first and last in place and spaces the centers evenly between them |
| Distribute Spacing Horizontally / Vertically | Sorts units by center, keeps the first unit's leading edge and the last unit's trailing edge in place, and makes every gap between neighbours equal. The gap can come out negative (overlap) when the units are wider than the span; that is allowed. |

Sorting breaks ties on the leading edge, then on project (z-)order, so the result is deterministic.

**Snapping.** When the snap-to-grid setting is on, each unit's delta is rounded to the project
grid spacing (`CalcUtils.roundToGrid`). Control points therefore stay on the grid, at the cost of
alignment that is only approximate to within half a grid step. With snapping off, alignment is
exact to the pixel.

**Stuck components.** Each unit's move goes through `includeStuckComponents`, as a mouse drag
does, so wires and leads attached to a moved part stretch to stay connected. A component that is
itself a unit of the operation is never also dragged as another unit's stuck component, otherwise
it would move twice.

**Undo.** A whole operation is one undo step, labelled with the command name.

**Enablement.** The align commands need at least 2 units and the distribute commands at least 3,
since distributing 2 units leaves both in place. Units are counted, not components, so one
selected group counts as 1.

## 2. Architecture

```
Swing actions ──► IPlugInPort (ISelectionProcessor) ──► Presenter ──► AlignmentManager
                                                          │               (pure geometry)
                                                          ├─ DrawingManager.getComponentArea
                                                          └─ includeStuckComponents + moveComponents
```

### diylc-core

**`org.diylc.common.AlignmentMode`** (enum): `LEFT`, `RIGHT`, `TOP`, `BOTTOM`,
`HORIZONTAL_CENTER`, `VERTICAL_CENTER`.

**`org.diylc.common.DistributionMode`** (enum): `HORIZONTAL_CENTERS`, `HORIZONTAL_SPACING`,
`VERTICAL_CENTERS`, `VERTICAL_SPACING`.

Both enums carry a `toString()` display label, used for the menu text and the undo label, the
same way other `org.diylc.common` enums do.

**`org.diylc.presenter.AlignmentManager`**: a stateless service, named like its siblings
`VariantManager` and `BuildingBlockManager`, and created in the `Presenter` constructor. It does
geometry only. It knows nothing about `Project`, `DrawingManager` or how a move is applied, which
keeps it unit-testable with plain rectangles.

```java
public List<Point2D> align(List<Rectangle2D> unitBounds, AlignmentMode mode, Double gridStep);
public List<Point2D> distribute(List<Rectangle2D> unitBounds, DistributionMode mode, Double gridStep);
```

Each call returns one delta per input rectangle, in input order. `gridStep == null` means no
snapping. Fewer than 2 rectangles (or fewer than 3 for `distribute`) returns all-zero deltas.

**`ISelectionProcessor`** gains three methods, each with Javadoc as the rest of that interface has:

```java
void alignSelection(AlignmentMode mode);
void distributeSelection(DistributionMode mode);
int getSelectionUnitCount();   // drives enablement in the Swing layer
```

**`Presenter`** implements them as follows:

1. Build the units from `selectedComponents` using the existing group lookup
   (`findAllGroupedComponents`), walking the project in z-order. For each unit, combine the
   bounds of the outline areas from `drawingManager.getComponentArea(...)`. Skip any unit with a
   missing area.
2. Ask `AlignmentManager` for deltas, passing the grid spacing in pixels only when snapping to the
   grid is on.
3. Take `oldProject = currentProject.clone()` once. Then, for each unit whose rounded delta is
   non-zero: build a control-point map containing every point of the unit's components, run
   `includeStuckComponents` (an overload takes the set to exclude) with the other units'
   components excluded, and call
   `moveComponents(map, dx, dy, false, false)`. Snapping already happened in step 2, so the move
   itself must not snap.
4. Call `notifyProjectModifiedIfNeeded(oldProject, mode.toString(), true, true)`.

All areas are read in step 1, before anything moves, because a move invalidates the moved
components' areas in `DrawingManager`.

`moveComponents` refuses a move that would push a control point off the canvas or make points
collide. That cannot happen to the units themselves, since they stay inside the selection bbox.
It can in principle happen to a dragged stuck component at the canvas edge, and in that case only
that unit's move is skipped. This is the same behaviour as a drag.

### diylc-swing

- **`actions/edit/AlignSelectionAction`** and **`actions/edit/DistributeSelectionAction`** follow the
  shape of `RotateSelectionAction`: they hold the mode, take the name from it, log one
  `ActionFactory.LOG.info(...)` line and delegate to `plugInPort`. Each gets a `create...` method in
  `ActionFactory`.
- **`EditMenuPlugin`**: add `ALIGN_TITLE = "Align & Distribute"`, injected with `injectSubmenu`
  directly after the Transform Selection submenu. It holds the six align actions, a separator, then
  the four distribute actions, with lazy getters like the existing ones. `refreshActions()` sets
  enablement from `getSelectionUnitCount()`.
- **`ComponentPopupMenu`**: add a `getAlignMenu()` `TranslatedMenu` after `getTransformMenu()`, with
  the same contents, and set enablement in `prepareAndShowAt`.

`refreshActions()` runs on `SELECTION_CHANGED`. Grouping or ungrouping without a selection change
can leave the enablement stale until the next refresh. That is harmless, because the Presenter
treats a too-small unit count as a no-op.

### Tests

`diylc-core/src/test/java/org/diylc/presenter/AlignmentManagerTest` covers each align mode, both
distribute variants on both axes, ties in the sort order, negative spacing, grid snapping,
already-aligned input (all-zero deltas) and the below-minimum unit counts. Presenter wiring and
menus get no unit tests, as is usual for this codebase. Rendering, serialization and netlist output
do not change, so no regression run is needed.

## 3. Open items

Everything in section 2 is implemented. The remaining items:

- **Icons** are in `diylc-swing-images/` (`align_*.png`, `distribute_*.png`) and registered in
  `IconLoader`. Each action picks its icon from its mode; the submenu uses `align_distribute.png`.
- **Shortcuts.** None yet. Rotate already uses Alt+arrows, and nudge has its own shortcut.
- **Action bar.** The commands are not on the context toolbar in `ActionBarPlugin`.
- **Manual check** in the running app: groups, stuck wires, snap on and off, and undo.
