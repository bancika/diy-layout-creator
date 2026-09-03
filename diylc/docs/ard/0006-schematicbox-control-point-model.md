# 0006 — `SchematicBox` is a rigid multi-pin component; it must not recompute in `setControlPoint`

**Status:** accepted

## Context

`SchematicBox` is a rectangle with N terminals on its four sides. Its pin positions are a
function of an anchor (control point 0) and the per-side node counts. The natural
implementation — "when the anchor moves, recompute all the pins" — was wired into
`setControlPoint`:

```java
// original, buggy
public void setControlPoint(Point2D point, int index) {
  controlPoints[index].setLocation(point);
  if (index == 0) updateControlPoints();   // rebuilds the whole control-point array
}
```

This breaks against how `Presenter` moves a rigid component. For a component whose
`canPointMoveFreely(i)` is `false`, the drag code adds **every** control-point index to the
move set and then applies the delta one index at a time:

```java
for (Integer index : indices) {                 // HashSet — order not guaranteed
  Point2D old = c.getControlPoint(index);
  c.setControlPoint(new Point2D.Double(old.getX() + dx, old.getY() + dy), index);
}
```

With the recompute in place, setting index 0 rebuilds pins 1..N relative to the new anchor;
the loop then reads those *already moved* pins and adds the delta again. Pins ended up moved
by 2× the drag, by different amounts depending on the `HashSet` iteration order — "control
points randomly move off the edge".

The same shape of bug hit `SchematicBuilder.moveAnchorTo` during initial placement: it read
`getControlPoint(i)` inside its loop after setting index 0, so box pins were double-offset,
which is why the first generation put every wire on one node until the user nudged the symbol.

`ICSymbol` — the component `SchematicBox` was modelled on — does **not** have this problem: its
`setControlPoint` only sets the one point, and it relies on `canPointMoveFreely == false` for
the presenter to translate all points together.

## Decision

Follow the `ICSymbol` model exactly:

- `SchematicBox.setControlPoint(point, index)` moves **only** that point. No recompute.
- `updateControlPoints()` runs only from the constructor and the node-list setters
  (`setLeftNodes` etc.). When the pin count is unchanged it mutates the existing `Point2D`
  objects in place (so references held by drawing/selection code stay valid); it allocates a
  new array only when the count changes.
- `SchematicBuilder.moveAnchorTo` snapshots every target position *before* mutating any
  control point, so a component that recomputes on set cannot be double-offset.

During a drag the presenter translates every pin by the same delta, which turns a valid box
configuration into another valid one — the pins stay on the (translated) edge without any
recompute.

## Consequences

- **+** Boxes drag cleanly; initial generation and re-sync produce identical routing.
- **+** `moveAnchorTo` is now robust for any current or future component that recomputes on
  `setControlPoint`.
- **−** If someone moves *only* control point 0 of a `SchematicBox` programmatically (not via
  the presenter's rigid-drag path), the body will move but the pins will not. Nothing in the
  codebase does this, but it is a latent sharp edge.
- **Rule of thumb:** a rigid multi-control-point component must never mutate points other than
  the one passed to `setControlPoint`. Geometry recompute belongs in explicit
  structure-changing setters, not in `setControlPoint`.
