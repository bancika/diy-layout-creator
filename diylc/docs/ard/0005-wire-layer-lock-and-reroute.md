# 0005 — Wires are made inert by locking the `WIRING` layer; they re-route on symbol move

**Status:** accepted

## Context

On the schematic the user may move symbols but must **not** be able to touch wires — no
selecting, no dragging, and above all no pulling a wire endpoint off a component. Symbols stay
fully movable. Because wires are real components ([0003](0003-wires-as-components-manhattan-routing.md)),
a `Presenter` will happily select and drag them.

There is no `IDIYComponent.isSelectable()` hook. The available levers are the project's
`lockedComponents` set and `lockedLayers` set. `Presenter.isComponentLocked()` already gates
click-select, rubber-band select, drag, and control-point grabbing off both.

## Decision

The wrapper `Project` used by the schematic tab locks the whole `WIRING` layer:

```java
wrapper.getLockedLayers().add(IDIYComponent.WIRING);
```

`SchematicWire` is the only thing on that layer (symbols are on `COMPONENT`), so every wire
becomes inert while symbols stay editable.

Locked components render at 0.5 alpha (`DrawOption.LOCKED_ALPHA`, on by default), which would
fade the wires. `SchematicWire.draw()` therefore forces `AlphaComposite.SrcOver` for its own
drawing and restores the previous composite afterwards.

**Re-routing on move:** `SchematicTabPlugin` installs a tiny `IPlugIn` on the schematic
presenter that listens for `EventType.PROJECT_MODIFIED` (dispatched once by `Presenter` when
a drag-move ends). On that event it calls `SchematicBuilder.rerouteWires(components)`, which
looks every wire's source/target symbols up by id, reads their current pin positions, and
re-runs `ManhattanRouter`. A `sameRoute` check skips the repaint when nothing changed; a
`rerouting` guard prevents re-entrancy.

## Consequences

- **+** One line makes wires completely non-interactive; symbols are untouched.
- **+** Uses the intended DIYLC mechanism (locked layers) rather than a new concept.
- **+** Wires follow symbols the user drags around, live.
- **−** `rerouteWires` recomputes *all* wires on every move, not just the affected ones. Fine
  for typical net counts; not optimized.
- **−** The opaque-composite workaround in `SchematicWire.draw()` is a wart that exists only
  because the wire is drawn on a locked layer.
- **−** If a future change puts something other than wires on the `WIRING` layer in the
  schematic, it would be locked too.
