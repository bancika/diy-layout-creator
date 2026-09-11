# Orthogonal Connector

A wire that runs only horizontally and vertically, turning at rounded 90-degree
corners.

## 1. Goal

`HookupWire` draws a free Bezier curve between two sticky endpoints;
`Line` draws a single straight segment. Neither can express the routing style
used in schematics and block diagrams, where a connection leaves a terminal on
an axis, turns at right angles, and arrives on an axis. This document designs
that third kind of connector.

The shapes that must be reachable are, at minimum, horizontal-vertical-horizontal
and vertical-horizontal-vertical. Simple L bends and longer staircases should
fall out of the same mechanism rather than needing a separate component.

## 2. How many handles does the route actually need

An orthogonal route is a polyline whose segments alternate between horizontal
and vertical. Both endpoints are pinned, so counting the free numbers is easy:
a route of `s` segments has `s - 1` corners and `s - 2` continuous degrees of
freedom, plus **one discrete bit** saying whether the first segment leaves the
start point horizontally or vertically.

| Segments | Shape | Corners | Continuous DOF | Bit |
| --- | --- | --- | --- | --- |
| 2 | `HV` / `VH` (an L) | 1 | 0 | yes |
| 3 | `HVH` / `VHV` | 2 | 1 | yes |
| 4 | `HVHV` / `VHVH` | 3 | 2 | yes |
| 6 | `HVHVHV` / … | 5 | 4 | yes |

This is the crux of the problem stated in the request. A single interior control
point carries two numbers, but the `HVH` / `VHV` case needs *one* number and
*one bit*. One point is simultaneously one coordinate too many and one bit too
few, so no scheme that reads only that point's position can resolve the
arrangement without a heuristic that flips as the user drags across a diagonal.

The line in the table that is exactly parameterised is the four-segment one: three
corners, two degrees of freedom, and a middle corner that is a genuine free point
in the plane. That observation drives the whole design.

## 3. The chosen parameterisation

**Each interior control point is a real bend vertex, and the route between
consecutive vertices is a fixed `H` then `V` pair.** For one interior point `h`
the route is

```
    A ────────────┐                    A = start          (sticky)
                  │                    c1 = (h.x, A.y)    (derived)
                  h ─────────┐         h  = the handle    (draggable)
                             │         c2 = (B.x, h.y)    (derived)
                             B         B = end            (sticky)
```

that is `A → (h.x, A.y) → h → (B.x, h.y) → B`: four segments, alternating,
starting horizontal and ending vertical. Both of the handle's coordinates are
used, nothing is discarded, and there is no hidden state.

The requested arrangements are reached by letting segments collapse to zero
length, which is what snap-to-grid makes trivial:

| Handle placed at | Route degenerates to |
| --- | --- |
| anywhere | `HVHV`, three visible bends |
| `h.y == B.y` | `HVH`, two bends, vertical leg at `h.x` |
| `h.x == A.x` | `VHV`, two bends, horizontal leg at `h.y` |
| `h == (A.x, B.y)` | `VH`, one bend |
| `h == (B.x, A.y)` | `HV`, one bend |

The handle may sit outside the box spanned by the endpoints, which gives routes
that detour around obstacles. Nothing special is needed for that case.

### Generalising

`k` interior points chain the same template:

```
A → (h1.x, A.y) → h1 → (h2.x, h1.y) → h2 → … → (B.x, hk.y) → B
```

This is `2k + 2` segments and `2k` degrees of freedom, matching the `s - 2` rule
exactly. Every additional handle buys two more bends, every stored number is
meaningful, and the normalisation code is the same loop regardless of `k`. The
number of handles is an enum property, mirroring `AbstractCurvedComponent`'s
`Point Count`.

### The bit does not need a property

The template above always leaves the start horizontally, so in principle there is
a bit to expose. In practice there is not, because the collapses in the table
already cover every way a route can meet its endpoints:

| Leaves the start | Arrives at the end | Reached by |
| --- | --- | --- |
| horizontally | vertically | bend point anywhere |
| horizontally | horizontally | bend point on the end row |
| vertically | vertically | bend point on the start column |
| vertically | horizontally | bend point on the opposite corner of the box |

A `Start Direction` property would therefore be a second, less discoverable way
of doing what dragging already does, so it is not offered. The single shape it
would add is the mirror-image staircase through a given bend point, which one
more bend point reaches anyway.

The bit does survive as a plain field, because a quarter-turn rotation swaps the
two axes and an `H V H V` route becomes a `V H V H` one, which the fixed template
cannot express. `OrthogonalComponentTransformer` records the swap there so that
rotating a component yields exactly the rotated route. Nothing else reads or
writes it, and it never appears in the property editor.

### Why not the alternatives

| Scheme | Verdict |
| --- | --- |
| One handle, only one coordinate honoured, arrangement from an enum | Half of the handle's motion is visibly inert with no explanation, and it does not generalise past two bends without a second property. |
| One handle, arrangement inferred from which side of the diagonal it is on | The route snaps between `HVH` and `VHV` mid-drag. Predictable to implement, unpleasant to use. |
| Store corners raw and project them onto a legal path on every read | Over-parameterised: `2k` stored numbers for `k - 1` degrees of freedom, so the projection needs to know which point moved last, and a whole-component drag rewrites the raw values and loses the arrangement. |

## 4. Class layout

| Class | Module | Role |
| --- | --- | --- |
| `AbstractOrthogonalComponent<T>` | `diylc-core`, `org.diylc.components` | Sibling of `AbstractCurvedComponent`. Owns the control points, the point-count and start-direction properties, the corner radius, path construction, length and caching bounds. Declares `protected abstract void drawCurve(Path2D, Graphics2D, ComponentState, IDrawingObserver)`, the same hook `AbstractCurvedComponent` uses, so concrete subclasses stay thin. |
| `OrthogonalComponentTransformer` | `diylc-core`, `org.diylc.components.transform` | Delegates point rotation to `SimpleComponentTransformer`, then flips `Start Direction`. Mirroring needs no flip and passes straight through. |
| `OrthogonalWire` | `diylc-library`, `org.diylc.components.connectivity` | The layout part, shown as `Right Angle Wire` under `Connectivity`. `AWG` value, stripe support, `IContinuity`. Its body is close to a copy of `HookupWire`'s, which is the point of reusing the `drawCurve` hook. |
| `OrthogonalLine` | `diylc-library`, `org.diylc.components.connectivity` | The schematic part, shown as `Right Angle Line` under `Schematic Symbols`. Draws the same route as a plain stroke of a given thickness, one pixel by default, with the optional arrow heads `Line` offers. |

The two concrete parts differ only in `drawCurve` and in the properties each adds
on top of the base. `Right Angle Line` mirrors `Line`'s property set exactly:
thickness, colour, style, arrow size and the two arrow toggles. It also carries
`IContinuity`, which `Line` does not, because it sits in `Schematic Symbols`
rather than `Shapes` and a wire drawn between two symbols is expected to show up
in the netlist.

Its arrow heads take their direction from the first and the last route segment
rather than from the straight line between the endpoints, and they are not
trimmed out of the stroke the way `Line` trims its own. The head is filled in the
line colour with its tip on the endpoint, so whatever runs underneath it is
already hidden.

## 5. Geometry

`buildVertices()` returns the full vertex list including any zero-length
collapses, then consecutive duplicates are dropped. `buildPath()` walks the
deduplicated list and rounds each corner:

```
r_i = min(cornerRadius, len(v[i-1], v[i]) / 2, len(v[i], v[i+1]) / 2)
```

The corner is drawn as a quadratic Bezier whose control point is the corner
itself, from the point `r_i` before it to the point `r_i` after it. Clamping to
half of each adjacent segment means short segments and collapsed segments
degrade to sharp corners instead of overshooting; an arc would look marginally
better at large radii but the difference is invisible at the radii this
component is for.

`Corner Radius` is a `Size` property, default around `0.05in`, `0` giving hard
corners.

## 6. Component contract

| Member | Behaviour |
| --- | --- |
| `creationMethod` | `SINGLE_CLICK`, as `HookupWire`. `POINT_BY_POINT` only ever assigns control points `0` and `1`, so it cannot place a component whose second endpoint is not index 1. |
| `isControlPointSticky` | `true` only for the first and last index. |
| `canControlPointOverlap` | `true` for interior indices. |
| `getControlPointVisibilityPolicy` | `WHEN_SELECTED` for every index. |
| `getControlPointNodeName` | `null` for interior indices. |
| `arePointsConnected` | `true` only for the pair `{0, count - 1}`, exactly as `HookupWire`. |
| Selection guidelines | Draw the dashed guideline polyline through the derived vertices with tracking off, as `AbstractCurvedComponent.draw` does. |
| Hit area | Falls out of filling the stroked path while the drawing observer is tracking. |
| `getLength` | Exact sum of segment lengths, so `IHaveLength` is honest here in a way the Bezier approximation is not. |
| `enableCache` | `false`, matching `HookupWire`. |

## 7. Defaults

Dropped on an empty canvas the component spans `DEFAULT_SIZE` diagonally, with
the single handle at `((A.x + B.x) / 2, B.y)`, which renders as a plain `HVH`
route. That is the shape most users expect from a right-angle connector, and one
drag of the handle reaches everything else.

## 8. What was built

| File | Module |
| --- | --- |
| `org/diylc/components/AbstractOrthogonalComponent.java` | `diylc-core` |
| `org/diylc/components/transform/OrthogonalComponentTransformer.java` | `diylc-core` |
| `org/diylc/components/connectivity/OrthogonalWire.java` | `diylc-library` |
| `org/diylc/components/connectivity/OrthogonalLine.java` | `diylc-library` |
| `org/diylc/components/AbstractOrthogonalComponentTest.java` | `diylc-core`, tests |

The three open questions were settled as follows.

1. The component is called `Right Angle Wire` and sits in `Connectivity`, next to
   `Hookup Wire`, whose drawing code it shares through the `drawCurve` hook. Its
   property list differs from `Hookup Wire`'s only in carrying `Corner Radius`
   where the curved wire carries `Smooth`.
2. The handle count ships configurable from the start, as `Point Count` with
   values `Three`, `Four` and `Five`, matching `AbstractCurvedComponent`'s
   property of the same name. Changing it spreads the bend points into an even
   staircase between the two ends.
3. No snapping was added. The clean shapes are already one grid step away, and a
   snap that pulls a handle onto the start column or end row would fight
   snap-to-grid rather than complement it. This is the decision most worth
   revisiting, since with no `Start Direction` property those two alignments are
   now the only route to the two-bend shapes.

The test covers the routing template in both start directions, the degenerate
shapes, the invariant that every produced segment is axis aligned, point-count
changes, and the transformer contract that rotating the component yields exactly
the rotated route.
