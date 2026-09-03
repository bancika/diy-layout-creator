# 0003 — `SchematicWire` is a real component; connections are drawn, not implied

**Status:** accepted

## Context

Nets need to be drawn as schematic wires with 90° bends. Two options:

1. **Wires as components:** a `SchematicWire` `IDIYComponent` stored in the `SchematicView`,
   drawn by the normal pipeline like any symbol.
2. **Wires rendered on the fly:** compute routes from the netlist at paint time and draw them
   in a custom overlay; nothing is stored.

Option 2 is tempting because "there is nothing to select or drag, so it cannot be edited".

## Decision

Wires are components (`org.diylc.components.schematic.SchematicWire`).

- Implements `IContinuity` (so the connection is real to any downstream analysis).
- Stores `sourceComponentId` + `sourcePinIndex`, `targetComponentId` + `targetPinIndex`, and
  the calculated `routePoints`. Control points are the route way-points; only the two endpoints
  are sticky; visibility is `NEVER`; `canPointMoveFreely` is `false`.
- Z-order is `IDIYComponent.WIRING`. Symbols are `IDIYComponent.COMPONENT`.
- `clone()` is overridden to deep-copy `routePoints` (the base clone only deep-copies
  `Point2D` / `Point2D[]`, not a `List`).

Routing is a separate pure class, `ManhattanRouter`:

- Straight segment when endpoints are axis-aligned; otherwise candidate L / HVH / VHV routes.
- Each candidate scored on length, bend count, crossings with existing segments, collinear
  overlap length, and whether it leaves each pin in the expected direction
  (`SchematicBuilder.exitDirection` — away from the symbol centroid along the dominant axis).
- Lowest score wins; result is de-duplicated and collinear middle points removed.

## Consequences

- **+** Wires persist in the `.diy` file, are picked up by the existing image/PDF export
  paths, and can gain user-editable bend points later without a redesign.
- **+** Routing runs once per generation / move, not on every repaint.
- **+** Reuses the rendering pipeline — no custom paint overlay, no second code path.
- **−** Wires are selectable/draggable by default in a `Presenter`. Making them inert needs an
  explicit mechanism — see [0005](0005-wire-layer-lock-and-reroute.md).
- **−** The router is a heuristic scorer over a fixed candidate set, not a real grid/A\*
  router. It avoids overlap pairwise but is not globally crossing-aware.
