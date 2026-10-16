# Project Explorer Refresh Cost

`ExplorerPane` (`diylc-swing/.../plugins/explorer`) rebuilds its whole list on every
`PROJECT_MODIFIED`, `PROJECT_LOADED` and `SELECTION_CHANGED`, even when the panel is collapsed
and even when the change cannot affect what it shows. This plan removes both costs without
touching `diylc-core` or the event contract.

## 1. How the explorer is driven today

- `Presenter` uses `MessageDispatcher<EventType>(true)` — **synchronous**. Every
  `processMessage` runs on the EDT inside the call that changed the project, so explorer work
  adds directly to the latency of the edit that triggered it.
- The dispatcher reads `getSubscribedEventTypes()` **once, at registration**, and there is no
  unregister path on `IPlugInPort`. "Disconnecting" therefore means returning early from
  `processMessage`, which costs one method call per event and is equivalent in practice.
- `ExplorerPlugin.processMessage`:
  - `PROJECT_MODIFIED` / `PROJECT_LOADED` → `ExplorerPane.setComponents(all, selection)`.
  - `SELECTION_CHANGED` → `setSelection(...)`. (It also falls through to `default`; harmless.)
  - `FILE_STATUS_CHANGED` is subscribed but unused.
- What `setComponents` costs, for *n* components:
  1. sort/filter/reverse copy of the component list;
  2. `buildComponentListWithGroups` — a UUID→component map over the whole project plus a
     set per group;
  3. `fireContentsChanged(0, n)` — with no fixed cell height, `BasicListUI` re-measures
     **every row** through the renderer, and most rows are `<html>` labels, so each row is
     an HTML parse. This is the dominant cost;
  4. `setSelection` rebuilds the group map again and does a linear scan per selected item.

### Which interactions reach the explorer

Read from `Presenter`; to be confirmed by measurement (step 0).

| Interaction | Events reaching the explorer | Changes the list? |
| --- | --- | --- |
| Mouse drag of components / control points, per mouse move | none — `dragOver` fires only `REPAINT` | — |
| Drag start (`dragStarted`) | `SELECTION_CHANGED` | selection only |
| Drag end (`dragEnded`) | `SELECTION_CHANGED` + `PROJECT_MODIFIED("Drag")` | no |
| Arrow-key nudge (`nudgeSelection`), per key repeat | `PROJECT_MODIFIED("Move Selection")` | no |
| Rotate / mirror / align / distribute | `PROJECT_MODIFIED` | no |
| Click on canvas (even on the already-selected part) | `SELECTION_CHANGED` | selection only |
| Edit properties (name, value) | `PROJECT_MODIFIED("Edit Selection")` | yes |
| Add / delete / duplicate / paste, z-order, group / ungroup, undo / redo | `PROJECT_MODIFIED` or `PROJECT_LOADED` | yes |

So the explorer does not rebuild on every mouse move; it rebuilds once per *gesture* and once
per *key repeat*, and each rebuild is O(n) HTML layouts. On a large project that is the stall
felt at the start and end of each drag and the lag when holding an arrow key. If profiling in
step 0 shows explorer work *during* a drag, something else is dispatching and needs finding
before this plan is enough.

## 2. Decisions

**Visibility is checked with `isShowing()` and a `HierarchyListener`, not the config key.**
`CollapsiblePanel` hides the pane with `setVisible(false)`; `isShowing()` also covers an
ancestor being hidden and the frame not yet being displayed. `HierarchyEvent.SHOWING_CHANGED`
fires for all of those, whereas listening to `ConfigPlugin.PROJECT_EXPLORER` would miss
everything except the menu toggle.

**A hidden pane keeps no state in sync; it just remembers that it is stale.** On becoming
visible it pulls the current project and selection from `IPlugInPort` rather than replaying
events. On `PROJECT_LOADED` while hidden it also drops its model so it does not pin the
previous project's object graph.

**"Does this change affect the list" is decided by comparing a snapshot of what the list
shows, not by the action string.** `PROJECT_MODIFIED` carries a description (`"Drag"`,
`"Move Selection"`, …) that could be whitelisted, but those strings are scattered through
`Presenter`, are not a contract, and a missed one shows stale data. A snapshot is robust:
the pane records exactly the inputs its model and renderer read, and skips the rebuild when
they are unchanged. Capturing it is O(n) plain field reads, far cheaper than O(n) HTML
layouts.

The snapshot is a small private record (per `typed-records-over-string-keyed-maps`), holding
copies, not live references, because `Presenter` mutates components and groups in place:

- the component list **by identity, in z-order**;
- per component: `getName()`, `toString()` (used by the NAME/TYPE sorts) and
  `getValueForDisplay()`;
- per group: name and set of member UUIDs (`ComponentGroup` has no `equals`, and a rename
  mutates the existing instance).

The layer badge comes from `ComponentType.getZOrder()`, which is static per class, so it is
covered by identity.

**Selection sync is O(selected), not O(n).** `refresh()` builds a
`Map<IDIYComponent<?>, Integer>` from component to row index alongside the row list.
`setSelection` uses it instead of rebuilding the group map and scanning rows. The existing
"selection already matches" early-out stays.

**Rows get a fixed height.** `setFixedCellHeight` from the font metrics stops `BasicListUI`
from measuring every row through the HTML renderer whenever contents change. Rows are a
single line, so nothing is lost. This also speeds up the rebuilds that remain necessary.

**No debounce timer for now.** With the snapshot check, a nudge burst costs one O(n)
comparison per repeat and no layout. A `javax.swing.Timer` coalescer would add latency to
genuine edits for little gain; revisit only if step 0 shows the comparison itself matters.

## 3. Changes

All in `diylc-swing/src/main/java/org/diylc/swing/plugins/explorer/`. No core, library or
file-format changes; no regression-suite impact (the explorer is not rendered in exports).

1. **`ExplorerPane`**
   - Field `boolean stale`; method `refreshIfStale()` that, when showing, calls
     `setComponents(plugInPort.getCurrentProject().getComponents(),
     plugInPort.getSelectedComponents())`.
   - `HierarchyListener` in the constructor: on `SHOWING_CHANGED` with `isShowing()`, call
     `refreshIfStale()`.
   - `setComponents`: build the snapshot; if equal to the previous one, skip
     `ComponentListModel.setComponents` and go straight to `setSelection`.
   - `ComponentListModel.refresh()`: also build the component→row map.
   - `setSelection`: use the row map.
   - `getComponentList()`: `setFixedCellHeight(...)`.
   - Snapshot record declared at the bottom of the file with the other nested types.
2. **`ExplorerPlugin.processMessage`**
   - First line: if `!explorerPane.isShowing()`, mark the pane stale (and on `PROJECT_LOADED`
     clear its model), then return.
   - Add the missing `break` after `SELECTION_CHANGED`.
   - Drop `FILE_STATUS_CHANGED` from `getSubscribedEventTypes()`.

Search and sort changes call `refresh()` directly and bypass the snapshot
check; the snapshot only gates the event-driven path.

## 4. Status and next steps

Everything in §3 is implemented in the working tree and `diylc-swing` compiles. Not yet done:

1. **Measure.** On a large project (several hundred components), time `setComponents` and
   `setSelection`, or run a profiler, while dragging, holding an arrow key and editing a
   value. Compare against `master` to confirm the table in §1 and the gain.
2. **Manual check in the running app.** Collapse the explorer, edit, then expand: the list and
   selection should be correct. Repeat with undo/redo and file open while it is collapsed.
   Also check: rename and value edits in the property editor; group, rename the group, then
   ungroup; z-order drag inside the explorer; search and sort while visible; row height
   looks right under each look-and-feel/theme.
3. Add a debounce only if step 1 shows the snapshot comparison itself is significant.
