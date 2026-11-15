# Split Trace Layer

Split the single **Trace** entry of the Layers menu into **Bottom Traces** and **Top Traces**, so
that copper on one side of the board can be hidden, locked or selected without touching the other.
Two-sided boards, the Underside Display editing and the Via made one trace layer too coarse: a user
routing the bottom in the Underside Display wants to lock the top, and the other way round.

Status: done. Core, library and swing tests pass; the regression suite has not been run, and no
change is expected there (section 5).

## 1. Background

- A layer is the component's z-order from `@ComponentDescriptor`, rounded to an integer:
  `CHASSIS` 1, `BOARD` 2, `TRACE` 3, `COMPONENT` 4, `WIRING` 5, `TEXT` 6. A composite takes the
  smallest z-order of its children (`ComponentProcessor.getZOrder`). Every component of a class is
  therefore on the same layer.
- The Trace layer holds `CopperTrace`, `CurvedTrace`, `GroundFill`, `SolderPad`, `PCBText`, `Via`,
  `Jumper`, `Eyelet` and `Turret`. The first five carry a `PCBLayer` through `IPCBLayerComponent`;
  a `SolderPad` with a plated through hole is on both sides and reports layer id `0`. `Via`,
  `Jumper`, `Eyelet` and `Turret` have no side.
- `Project` saves `lockedLayers` and `hiddenLayers` as `Set<Integer>` of those z-orders, e.g.
  `<hiddenLayers><int>3</int></hiddenLayers>`.
- Layer membership is worked out in four places, each with its own z-order lookup:
  `Presenter.isComponentVisible` (which also filters drawing), `Presenter.isComponentLayerLocked`,
  `GroupAndLockIndex.isLocked` and `Presenter.selectAll(int)`. `selectAll` truncates the z-order
  where the others round it.
- `LayersMenuPlugin` builds one submenu per value of its `Layer` enum, with Visible, Lock and
  Select All, and derives the Visible shortcut from the z-order (`VK_0 + zOrder`, so Ctrl+3 toggles
  the traces).
- None of the regression projects hides or locks the Trace layer; they lock only Chassis and Board.

## 2. Decisions

| Decision | Rationale |
| --- | --- |
| Layers become a property of the component, not of its class, decided in one helper | A trace's side is per instance. One helper replaces the four z-order lookups and their rounding mismatch. |
| Only the Trace layer is split; Component stays one layer | Top/Bottom Components is not planned. The helper keeps per-component membership in one place, so it could follow later without another refactoring. |
| The Visible shortcuts are renumbered to follow the menu: Ctrl+1 Chassis, Ctrl+2 Board, Ctrl+3 Bottom Traces, Ctrl+4 Top Traces, Ctrl+5 Component, Ctrl+6 Wiring, Ctrl+7 Text | Shortcuts that follow the menu order are easier to learn than a new one added out of order. Only the shortcuts move; the saved layer ids do not, so files are unaffected. |
| New ids: Bottom Traces `31`, Top Traces `32` (`TRACE * 10 + PCBLayer.getId()`) | They cannot collide with a z-order. Reusing `3` for one side would make an older DIYLC treat a file with only one side hidden as all traces hidden. |
| `3` stays as the legacy id meaning both trace layers, and the saved sets keep the invariant "`3` is present exactly when `31` and `32` both are" | A file saved with both sides hidden or locked still hides or locks all traces in an older DIYLC, and an older DIYLC that unhides or unlocks Trace is understood by the new one (section 4). |
| The ids and the set rules live in `org.diylc.core.LayerIds`; which layer a component is on lives in `org.diylc.presenter.ComponentLayers` | `Project` applies the rules on load and must not depend on the presenter package; layer membership needs the z-order from `ComponentProcessor`. |
| Two set rules, not one: `migrateLoaded` after a load, `syncLegacy` after a change | "Both sides set, no `3`" means opposite things in the two cases: written by an older version that cleared Trace, or just set by the user. |
| Copper with a side is on the layer of that side; the deprecated inner layers count as Bottom | `IPCBLayerComponent.getBoardSide()` already maps them that way. |
| A Trace-band component that reports layer id `0` is on both trace layers: a PTH `SolderPad`, and `Via`, `Eyelet` and `Turret`, which implement `ILayeredComponent` to say so. A composite whose z-order falls in the Trace band is on both as well | Layer id `0` already means "every layer" to continuity, and the three go through the board. A composite may hold copper of both sides. Core names no concrete component. |
| Every other Trace-band component is on the layer of `IBoardSideComponent.sideOf`, so a component without a side, such as `Jumper`, is on Top Traces | `sideOf` already treats a component without a side as top, the same rule the rendering uses. A jumper sits on top of the board: locking Bottom Traces to work on the top must leave it movable. |
| A component on both layers is hidden only when both are hidden, and locked when either is locked | Hiding one side must not hide a via that the other side's routing still needs to show. Moving a via changes the routing on both sides, so a locked side has to protect it. |
| Select All of a trace layer selects everything on it, including components on both layers | Matches what is shown on that layer. |
| The plug-in API keeps its `int` parameters, renamed from `layerZOrder` to `layerId` | `setLayerLocked`, `setLayerVisibility` and `selectAll` keep their signatures; only the meaning of the number widens. |

## 3. Design

### diylc-core

- `org.diylc.core.LayerIds` (new): `LEGACY_TRACES = IDIYComponent.TRACE`, `BOTTOM_TRACES = 31`,
  `TOP_TRACES = 32`, `migrateLoaded(Set<Integer>)` for a set read from a file and
  `syncLegacy(Set<Integer>)` for a set just changed (section 4).
- `org.diylc.presenter.ComponentLayers` (new, not in `Presenter`, which stays lean): the layer of a
  component is the rounded z-order outside the Trace band; inside it, both trace layers for layer
  id `0` and for a composite, otherwise the layer of `IBoardSideComponent.sideOf`. It answers
  `isHidden(hiddenLayers, component)` (all its layers hidden), `isLocked(lockedLayers, component)`
  (any of its layers locked) and `isOnLayer(component, layerId)` for Select All, without
  allocating, since drawing asks for every component on every repaint.
- `Presenter.isComponentVisible`, `isComponentLayerLocked`, `selectAll(int)` and
  `GroupAndLockIndex.isLocked` call the helper. Internal types without a descriptor stay visible
  and unlocked, as today.
- `Presenter.setLayerLocked` and `setLayerVisibility` call `syncLegacy` on the changed set; given
  the legacy id `3`, they change both trace layers.
- `Project.readResolve()` (new) runs `migrateLoaded` on `lockedLayers` and `hiddenLayers`
  whenever XStream loads a project, so the migration does not depend on which code path opened
  the file. XStream calls `readResolve` on any class that declares it.
- `Via`, `Eyelet` and `Turret` (diylc-library) implement `ILayeredComponent` with `getLayerId()`
  returning `0`. Continuity does not change: a component that is not layered already counts as
  layer `0`.
- `GroupAndLockIndex` is already invalidated in `notifyProjectModifiedIfNeeded`, which property
  edits go through, so a trace moved onto a locked side is locked from then on. A test pins this.
- `IPlugInPort` and `ISelectionProcessor` javadoc: the parameter is a layer id, a z-order or one of
  the trace ids.

### diylc-swing

- `LayersMenuPlugin.Layer` gets `BOTTOM_TRACES` ("Bottom Traces", `31`) and `TOP_TRACES`
  ("Top Traces", `32`) in place of `TRACE`, in the order Chassis, Board, Bottom Traces, Top Traces,
  Component, Wiring, Text.
- The Visible shortcut comes from the enum value's position in the menu, Ctrl+1 to Ctrl+7, instead
  of `VK_0 + zOrder`. Component, Wiring and Text move from Ctrl+4 to Ctrl+6 to Ctrl+5 to Ctrl+7.
- The language keys **Bottom Traces** and **Top Traces** go into every file in `lang/`; the
  submenu names are translated by `MainFrame`.

## 4. Backward compatibility

Nothing about components changes, and no field is added, renamed or retyped. The only saved data
involved are the two `Set<Integer>` fields of `Project`, whose type stays the same.

**Invariant.** In a saved set, `3` is present exactly when `31` and `32` both are. `migrateLoaded`
applies the first three rows after a load, `syncLegacy` the last one after a change:

| Set as loaded or changed | Normalized | Why |
| --- | --- | --- |
| contains `3` | adds `31` and `32` | An old file, or an older DIYLC that hid or locked Trace: all traces. |
| contains `31` and `32`, no `3` | removes `31` and `32` | Only an older DIYLC produces this, by unhiding or unlocking Trace in a file saved hidden or locked on both sides. |
| contains one of `31` and `32` | unchanged | One side set by this version. |
| both sides set by the user | adds `3` | So that an older DIYLC hides or locks all traces too. |

**Opening old files in the new version.** A project with the Trace layer hidden or locked opens
with both trace layers hidden or locked, looking and behaving exactly as before. Projects that
never touched the Trace layer, which includes every regression project, are unaffected.

**Opening new files in an older version.** The older version reads `31` and `32` into its sets as
plain integers and ignores them, because none of its components is on those layers.

- Both sides hidden or locked: `3` is saved too, so the older version hides or locks all traces.
- One side hidden or locked: the older version shows and unlocks all traces. Nothing is hidden
  that its Trace menu cannot reveal; only the one-sided lock is not honoured there.
- The older version keeps `31` and `32` when it saves, so the one-sided setting comes back when
  the file is opened in the new version again.
- If the older version unhides or unlocks Trace, it removes `3` and leaves `31` and `32`, which the
  second row of the table reads as unhidden or unlocked, matching what the user did.

**Undo and clipboard.** Undo snapshots are `Project` clones and copy the sets as they are; the
clipboard carries components, not layer sets.

## 5. Verification

- `ComponentLayersTest` (library): a top and a bottom trace, an inner-layer trace (Bottom), PTH and
  NPTH pads, a `Via`, an `Eyelet`, a `Jumper` (Top Traces) and a Trace-band composite each land on
  the expected layers; a Component-band part keeps its z-order layer.
- Hidden and locked rules: a via is hidden only with both sides hidden and locked with either side
  locked; a jumper stays unlocked when only Bottom Traces is locked.
- Continuity: the netlist of a project with vias, eyelets and turrets is unchanged.
- `LayerIdsTest` (core): each row of the table in section 4, and a project saved and loaded through
  XStream, so that `readResolve` runs.
- `TraceLayerTests` (swing): Select All per trace layer; locking Bottom Traces locks the via but
  not the top trace or the jumper; both sides hidden keeps `3`; a top trace moved to the locked
  bottom through its Layer property is locked.
- Regression suite: no image or netlist change expected, since no regression project hides or
  locks the Trace layer.

## 6. Next steps

1. Check the menu, the shortcuts and the locking in the running app, and open a project saved with
   the old Trace layer hidden or locked.
