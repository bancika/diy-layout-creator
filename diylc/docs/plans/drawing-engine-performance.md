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
| ~52% of CPU samples | `TwistedWire.recalculate` and the `Area` operations under it, mostly on the `bulkPrepare` workers (fixed, see §7) | §7 |
| ~7% of CPU samples | `G2DWrapper.appendShape` recording `TwistedWire`'s outline (`currentArea.add`) | §2a |
| 16% of CPU samples | Marlin rasterisation (`Renderer._endRendering`): the drawing itself | — |
| ~19% of EDT samples | `Presenter.findAllGroupedComponents`, from `Presenter.draw` and `mouseMoved` | §6 |
| 958 MB / 60 s | pixel buffers for cache renders | — |
| 426 MB / 60 s | grid image rebuilt on zoom changes (fixed, see §3) | §3 |
| 386 MB / 60 s | canvas back buffer re-created on every scroll step (fixed, see §5) | §5 |

GC pauses totalled 348 ms. Most were under 40 ms, with one young-generation pause of 115 ms during
heavy allocation.

**Profile of 2026-10-14, later (P4),** mostly dragging parts with twisted wires attached: 707 CPU
samples, 383 of them `TwistedWire` geometry on the `bulkPrepare` workers, and 4.75 GB of cached
images allocated in 60 s. Wires stuck to a dragged part kept the `NORMAL` state, so every frame
re-rendered them into a new cache image. Moved-along components now bypass the cache (ADR §2).
The geometry itself still runs on every frame, because the wire's shape really changes; see §7.
Grouping fell to 12 samples after §6, and the per-component overhead (now under "Not worth
optimizing now") showed 1 sample.

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

**Done:** `G2DWrapper.appendShape` used to do `new Area(s)` followed by
`area.transform(currentTx)`. `Area.transform` rebuilds the whole geometry even for an identity
transform, so every tracked shape was processed twice. It now builds the area once: from the
shape itself when the transform is the identity, or from `currentTx.createTransformedShape(s)`
otherwise. Same geometry, checked by `DrawingManagerTest.testOutlineAreaFollowsComponentTransform`.

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

## 3. Grid: done

`DrawingManager.drawGrid` used to cache the grid as one image the size of the whole canvas at the
current zoom. That was up to ~190 MB at high zoom, outside the cache budget, and 426–1,041 MB of
grid images were allocated per profiled minute of zooming. It was rendered at 1× and stretched on
HiDPI, and its rebuild check ignored grid spacing, dot spacing and theme.

It now lives in `GridRenderer` (tested by `GridRendererTest`), which draws the lines and dots
directly, and only those inside the visible rect:

- **Antialiasing off for the grid.** The lines are axis-aligned, so each one snaps to crisp whole
  pixels instead of being smeared across two. The thickness formula is unchanged: 1 px at zoom
  ≥ 1, and `zoom` px below that.
- **Dense grids are thinned.** When lines would be closer than `GridRenderer.MIN_GRID_LINE_SPACING` (3 canvas
  pixels at the current zoom), only every 5th line is drawn, then every 25th, and so on. Snapping
  still uses the real spacing. A 0.5 mm grid shows every line at 100% and every 5th line from
  75% down.
- **Exports and printing** pass no visible rect and get the whole grid as vector lines, instead of
  a raster image embedded in the PDF or SVG.

One viewport of a 0.5 mm grid draws in under a millisecond. There is no cached state any more, so
the grid cannot go stale.

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

## 6. Grouping and locking bookkeeping: done

`Presenter.draw` used to rebuild group membership and the locked-component set on every repaint,
and `mouseMoved` looked groups up on every move: about a fifth of the EDT's samples in all three
profiles. Both now come from `GroupAndLockIndex` (tested by `GroupAndLockIndexTest`), which works
them out once per project state:

- group membership is indexed by component id, so a component outside the project, such as one
  being placed, resolves the same way as before; the sets it hands out are read-only;
- `Presenter` calls `invalidate()` from `notifyProjectModifiedIfNeeded`, `loadProject`, paste
  (which copies the clipboard's groups straight in), `ungroupComponents`, `setLayerLocked` (which
  updates the selection before notifying) and `lockComponent`;
- asking about a different project object rebuilds the index without an explicit invalidation.

**Watch.** Code that changes the current project's groups, locks or component list without going
through `Presenter` would leave the index stale until the next edit. Nothing does today; plugins
change the project through `IPlugInPort`, and `LoadlineEditorFrame` builds its own project.

## 7. Individual components

**`TwistedWire`: done in two steps.**

1. `recalculate` used to build and cut each lead by growing one area segment by segment and
   cutting every crossing out of the whole lead, so its cost grew with the square of the wire's
   length. The segment areas are now merged pairwise and each kind of crossing is cut in one
   operation. That also removed a data race (the parallel crossing loop read one lead while
   another thread subtracted from it) and a bug that left the last crossings near the end of many
   wires uncut.
2. P4 still showed the geometry on every frame for wires whose ends move with a drag. Opaque
   wires are now drawn with strokes and no `Area` operations: per lead, a dark stroke one pixel
   wider than the lead and a coloured one a pixel narrower give the outline, the stripe is a
   dashed stroke on top, and redrawing the first lead clipped to its own crossing squares puts it
   on top there. `buildLeadGeometry` produces the lead paths and squares once per shape change and
   is cached in `leadGeometry`. The first stroke of each lead is the one recorded as the
   component's area.

Translucent wires (alpha below 100%) keep the area path, because strokes paint some pixels twice
and that would show through; its output is pixel-identical to before. On eight test renders the
stroke path is about 8x faster (97 ms against 772 ms). Opaque output matches apart from
antialiasing along the edges and a slightly finer outline, and selected wires lose faint seams the
area cuts used to leave. Stroking a lead as one path whose segments are separate sub-paths is fine:
stroke outlines are filled with the non-zero rule, so overlapping segments merge.

**`HookupWire`** is now cached and no longer shows up in the profile.

## Not worth optimizing now

- **Per-component overhead in the drawing loop** (formerly §7): the stats key built with
  `getCanonicalName().replace(...)`, the `G2DWrapper` allocations on draws that record nothing, and
  the `LOG.warn("Empty outline area detected ...")` in `DrawingCache.renderAndCache`. P3 and P4
  show it at 1 sample each. The warning still breaks the no-logging-on-hot-paths rule and can go
  whenever that file is next touched.

- **Mouse-move handling.** `Presenter.mouseMoved` walks every control point twice per move, but it
  does not repaint. It only becomes worth indexing at much larger project sizes.
- **Type lookups and locking.** The `ComponentProcessor.extractComponentTypeFrom` lookups (three
  per component per repaint) and the uncontended `synchronized (this)` around each component in
  the drawing loop are cheap.

## Order of work

| Step | Item | Size | Risk | Needs |
| --- | --- | --- | --- | --- |
| 1 | §2a lazy or batched area merging | medium | medium | `DrawingManagerTest`, netlist checks, regression run |
| 2 | §4 image formats | small | low | page-time comparison on macOS |
| 3 | §2b frozen drag background | large | medium | manual drag testing incl. stacking order |
| 4 | §5 redraw only the damaged area | medium | medium | scroll and tooltip testing |
| 5 | §1 cached reflection metadata for `equalsTo` / `clone` | small | low | unit test for `equalsTo` |

Re-profile next: the ranking of the remaining steps still comes from reading the code.
