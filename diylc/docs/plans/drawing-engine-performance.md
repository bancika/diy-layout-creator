# Drawing Engine Performance

The remaining repaint costs in the canvas drawing pipeline, ranked by expected payoff, and what
each fix needs. The image cache and off-screen culling that already exist are described in
[`../adr/canvas-rendering-cache-and-culling.md`](../adr/canvas-rendering-cache-and-culling.md),
which also lists their own open issues. This plan covers everything else.

How a repaint flows: `CanvasPanel.paint` → `Presenter.draw` → `DrawingManager.drawProject`. That
calls `DrawingCache.bulkPrepare` (parallel), then runs the per-component loop through
`DrawingCache.draw` / `G2DWrapper` → `component.draw()`.

## 0. Measurements

**How to measure.**

- **Recording.** Add `-XX:StartFlightRecording=duration=60s,filename=drag.jfr` to the run
  configuration, then drag, scroll, zoom and edit a complex layout.
- **Reading it.** `jfr view hot-methods drag.jfr`, `jfr view gc drag.jfr` and
  `jfr view allocation-by-site drag.jfr` give the overview. For DIYLC call sites, export the samples
  with `jfr print --json --events jdk.ExecutionSample --stack-depth 96` (or
  `jdk.ObjectAllocationSample`) and group them by the innermost `org.diylc` frame.
- **Before and after a change,** compare the `DrawingManager` "Page stats" average at the same zoom
  and viewport, plus the `DrawingCache` lines (see the ADR, §5). Compare total time, not per-type
  counts, because culled components are not counted.

**Profile of 2026-10-14.** Complex layout, wires cached, culling and invalidation-based caching in
place. 750 CPU samples over 60 s, so treat the shares as approximate.

| Share | Where | Item |
| --- | --- | --- |
| ~52% of CPU samples | `TwistedWire.recalculate` and the `Area` operations under it, mostly on the `bulkPrepare` workers | §8 |
| ~7% of CPU samples | `G2DWrapper.appendShape` recording `TwistedWire`'s outline (`currentArea.add`) | §2a |
| 16% of CPU samples | Marlin rasterisation (`Renderer._endRendering`): the drawing itself | — |
| ~19% of EDT samples | `Presenter.findAllGroupedComponents`, from `Presenter.draw` and `mouseMoved` | §6 |
| 958 MB / 60 s | pixel buffers for cache renders | — |
| 426 MB / 60 s | grid image rebuilt on zoom changes | §3 |
| 386 MB / 60 s | canvas back buffer re-created on every scroll step (fixed, see §5) | §5 |

GC pauses totalled 348 ms. Most were under 40 ms, with one young-generation pause of 115 ms during
heavy allocation.

**Baseline before culling,** from the same layout: 327 repaints averaging 26 ms; `HookupWire`
(then uncached) about 9 ms per repaint; `TwistedWire` about 96 ms per render; `TubeSocket` 572 ms
in `DrawingManager` against 25 ms of image pasting.

## 1. Reflection on every edit

**Done:** the per-repaint reflective comparison is gone. `DrawingCache` now relies on `Presenter`
invalidating changed components; see the ADR, §2.

**What is left.** Reflection now runs once per edit instead of on every repaint. Each edit:

- clones the whole project (`AbstractComponent.clone()` walks the fields with reflection);
- runs `Project.equals`;
- runs `Presenter.invalidateChangedComponents`, which compares every component with its snapshot.

All three use the same `getDeclaredFields()` / `setAccessible` / `Field.get` walk. It is felt
mostly on repeated edits, such as holding an arrow key to nudge a large project.

**Fix.** Cache each class's comparable fields once, with `setAccessible` already applied, in a
`ClassValue`, and use it in both `equalsTo` and `clone`. Behaviour stays identical. Unit-test the
new `equalsTo` against the current one on a few real components from `diylc-library`.

**Watch.** Missed invalidation paths are now a correctness risk. Run with
`-Dorg.diylc.verifyDrawingCache=true` while testing new edit operations or plugins that change
components.

## 2. Dragging redraws everything and re-records areas every frame

**Problem.** Each drag step invalidates the moved components (`Presenter`, after updating the
control points). The next frame:

- redraws the whole visible project;
- re-records the moved components' areas. That does `currentArea.add(...)` for every tracked shape
  (`G2DWrapper.appendShape`), and continuity areas do the same per marker. Adding shapes one at a
  time costs more as the area grows, so a board or a part with many pads pays heavily on every
  frame.

**Done:** a union with an empty area still sweeps every edge of the other shape, so the first
tracked shape now becomes the outline area directly. `new Area(shape)` is not a cost: for an `Area`
argument the JDK shares the curve list rather than copying it.

**2a. Compute areas lazily, or merge them in one batch.**

- Collect the tracked shapes in a list during drawing and merge them in
  `finishedDrawingComponent` by pairwise merging (merge neighbours, repeat), not one at a time.
  `AreaUtils` already has helpers.
- Better: store the shapes and merge only when something asks for the area. The consumers are:
  - `findComponentsAt` (click);
  - `Presenter.getComponentBounds` (selection bounds, rulers);
  - `DrawingManager.getContinuityAreas` / `getContinuityGraph` (netlist, highlight connected
    areas, compare, chatbot).

  A drag would then never pay for merging. `ComponentArea` would hold the shapes and compute
  `getOutlineArea()` the first time it is asked.
- **Watch:** `G2DWrapper.debugAreaPerformance` compares areas before and after each add, and needs
  rethinking under batching. `Snapshot` (the test recorder) stores `ComponentArea`s.

**2b. Draw the drag over a frozen background.**

- At drag start, render everything that is not moving into a background image once. Each frame
  then pastes that image and draws only the moving components, the control points and the
  selection.
- Re-render the background when the viewport scrolls or the drag ends.
- **Stacking order:** a moving part that sits under other parts would draw on top of them while
  dragging. Either accept that for the length of the drag, or split the background into a layer
  below the lowest moving component and a layer above it, and draw the moving parts between them.
- Components that move along with the dragged ones without being selected (stuck components) must
  count as moving. `Presenter` knows which these are from the control-point map.
- This is the biggest structural win for dragging, and the most invasive change in this plan. It
  touches `CanvasPanel`, `Presenter.draw` and `DrawingManager.drawProject`.

## 3. The grid image is huge, blurry on HiDPI, and can go stale

**Problem.** `DrawingManager.drawGrid` caches the grid as one `TYPE_INT_ARGB` image the size of the
whole canvas at the current zoom. The profile confirms the cost: 426 MB of grid images were
allocated in 60 s of zooming.

- **Memory.** A 10-inch project at 4× zoom is roughly 8000 × 6000 pixels, about 190 MB. That falls
  outside the `DrawingCache` budget, and the image is rebuilt on every zoom change.
- **HiDPI.** The image is rendered at 1× and stretched by the HiDPI scale, so it is blurry on
  Retina, and the stretched copy is redrawn on every repaint.
- **Staleness.** The rebuild check covers zoom, canvas size, extra space and grid type (which is a
  constant). Changing the grid spacing, dot spacing or theme colours, or turning the grid on after
  starting with it off, would leave a stale grid on screen. *Read from the code, not reproduced.*
  Check this first. If it is real, it is a user-visible bug independent of performance.

**Fix.** Drop the image and draw only the grid lines and dots that fall inside the visible rect on
each repaint. That is a few dozen `draw` calls, which is cheaper than the stretched copy, and it
fixes memory, blur and staleness together. Export and print pass a null visible rect, so they need
the whole canvas: keep the full loop for that case. Rendering changes, so run the regression suite.

## 4. Image formats force slow compositing

- **The canvas back buffer** (`CanvasPanel.createBufferImage`) is `TYPE_INT_ARGB`, though the
  canvas is always filled with an opaque background. Use an opaque compatible image,
  `getGraphicsConfiguration().createCompatibleImage(w, h, Transparency.OPAQUE)`, kept HiDPI-scaled
  as `ScaledBufferedImage` does now, so that copying it to the screen takes the fast path.
- **Cached component images** (`DrawingCache.renderAndCache`) are `TYPE_INT_ARGB`, so every paste
  blends each pixel without premultiplied alpha. `TYPE_INT_ARGB_PRE` blends faster. Confirm that
  `ScaledBufferedImage` and anything else reading the pixels is fine with the format.

Each is a one-line change, but Java2D's speed varies by pipeline (Metal on macOS), so keep each
change only if the page-time comparison shows a gain.

## 5. Every paint redraws the whole visible area

**Done:** the back buffer used to be thrown away on every scroll-bar event. `CanvasPlugin` called
`invalidateCache()` from both scroll listeners, which allocated 386 MB in the profiled minute.
`CanvasPanel.paint` now re-creates the buffer only when its pixel size changes, which includes a
change of HiDPI scale.

**Problem.** `CanvasPanel.paint` ignores the clip that Swing passes in. It always redraws the full
visible rect into a back buffer sized and positioned to the viewport. Scrolling, which only
exposes a thin strip, and partial repaints after a tooltip or balloon closes, both pay for a full
redraw.

**Fix.** Redraw only `g.getClipBounds()` (intersected with
the visible rect), passing that as the visible rect so that culling and the cache limit the work.
The buffer has to survive scrolling: either shift its contents and redraw the exposed strip, or
make it larger than the viewport. Moderate work. Do it after item 3, since the grid currently
assumes it draws the whole canvas.

## 6. Bookkeeping in `Presenter.draw` on every repaint

- **Grouped components.** Confirmed by the profile: about 19% of the EDT's own samples.
  `groupedComponents` calls `findAllGroupedComponents` for every component. For each one it scans every group's ids with a stream, and for a grouped component it
  filters the whole component list as well. On every repaint that is roughly components × group
  size, plus the stream allocations.
- **Locked components.** `getLockedComponents()` rebuilds its set on every repaint.

**Fix.** Compute both once and reuse them until the project changes: on `PROJECT_MODIFIED` /
`PROJECT_LOADED`, group and lock changes, and layer locking. `mouseMoved` calls
`findAllGroupedComponents` for selected components on every move too, and should use the same
cached data.

## 7. Per-component overhead in the drawing loop

Each item is small, but each is paid for every component on every repaint:

- **Stats key string.** `getCanonicalName().replace("org.diylc.components.", "")` builds a new
  string twice per component (in `DrawingManager` and `DrawingCache.getRenderStats`). Look the key
  up per class instead, e.g. with a `ClassValue<String>`.
- **Wrapper allocations.** `G2DWrapper.startedDrawingComponent` / `finishedDrawingComponent`
  allocate an `Area`, two `HashMap`s, two `AffineTransform` copies, a `ComponentArea` and two
  `ArrayList`s, even when no areas are being recorded. Allocate them only when recording, and
  return `null` otherwise. The loop in `DrawingManager.drawProject` already ignores the result when
  it is not recording.
- **Logging on the render path.** `LOG.warn("Empty outline area detected ...")` in
  `DrawingCache.renderAndCache` can fire on every render of an affected component. Remove it or
  downgrade it, per the repository's no-logging-on-hot-paths rule.

## 8. Individual components

**`TwistedWire.recalculate`** was more than half of all CPU samples in the profile. Its cost grows
roughly with the square of the wire's length:

- each lead's area is built by adding one stroked segment at a time
  (`outputAreas[j].add(new Area(stroke.createStrokedShape(p)))`);
- at every crossing, a small square is intersected with the *entire* other lead and subtracted from
  the *entire* lead, which is two operations over the full area per crossing.

**Fix.**

- Build each lead with a single `new Area(stroke.createStrokedShape(path))`, where `path` holds all
  of that lead's segments as separate sub-paths.
- Combine the crossing squares into one mask per lead. Intersect the mask with the other lead once
  and subtract the result once.

The squares (3.5 × thickness) are smaller than the segment spacing (6 × thickness), so they do not
overlap and the result should be unchanged. Compare rendering before and after with the regression
suite.

**This is also a thread-safety bug.** The crossing loop runs in parallel, and
`pointRect.intersect(outputAreas[0])` reads one lead while another thread may be subtracting from
it. Only the writes are synchronised, and `Area` is not thread-safe, so the result can depend on
thread timing. The fix removes that loop, and with it the nested parallel streams that run inside
the already-parallel `bulkPrepare`.

**`HookupWire`** is now cached and no longer shows up in the profile.

## Not worth optimizing now

- **Mouse-move handling.** `Presenter.mouseMoved` walks every control point twice per move, but it
  does not repaint. It only becomes worth indexing at much larger project sizes.
- **Type lookups and locking.** The `ComponentProcessor.extractComponentTypeFrom` lookups (three
  per component per repaint) and the uncontended `synchronized (this)` around each component in
  the drawing loop are cheap.

## Order of work

| Step | Item | Size | Risk | Needs |
| --- | --- | --- | --- | --- |
| 1 | §8 `TwistedWire.recalculate` | small | medium | before/after rendering comparison, regression run |
| 2 | §6 cached grouping and locking | small | low | — |
| 3 | §3 draw the grid directly (first check whether the stale grid reproduces) | small | low | regression run |
| 4 | §7 per-component overhead | small | low | — |
| 5 | §2a lazy or batched area merging | medium | medium | `DrawingManagerTest`, netlist checks, regression run |
| 6 | §4 image formats | small | low | page-time comparison on macOS |
| 7 | §2b frozen drag background | large | medium | manual drag testing incl. stacking order |
| 8 | §5 redraw only the damaged area | medium | medium | scroll and tooltip testing |
| 9 | §1 cached reflection metadata for `equalsTo` / `clone` | small | low | unit test for `equalsTo` |

Re-profile after steps 1–3. The ranking of steps 4–9 still comes from reading the code.
