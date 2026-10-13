# Canvas Rendering: Caching and Off-Screen Culling

How the main canvas avoids redrawing work on every repaint. There are two mechanisms, both in
`diylc-core/.../presenter`:

- **Image caching** (`DrawingCache`) turns an expensive component into a bitmap copy while it is
  unchanged.
- **Off-screen culling** (`DrawingManager` + `G2DWrapper`) skips components that cannot be seen.

Both are optimisations only. Exports, printing and the regression suite pass no visible rect, so
culling never applies to them, and with caching off the output is the same.

## 1. The repaint pipeline

1. `CanvasPanel.paint` takes `getVisibleRect()`, translates its back buffer to it and calls
   `IPlugInPort.draw(..., visibleRect)`. That reaches `DrawingManager.drawProject`.
2. `drawProject` scales `visibleRect` by `1 / zoom`, so from here on it is in **project
   coordinates**, the same space as component control points.
3. If `DrawOption.ENABLE_CACHING` is set, `DrawingCache.bulkPrepare` renders every stale cacheable
   component that intersects the clip, **in parallel** on the common fork-join pool.
4. Components are then drawn one by one in z-order. For each one:
   - `trackArea = lastDrawnStateMap.get(c) != state`. The selection and continuity areas in
     `componentAreaMap` are recorded only on a draw where this is true: the first draw after load,
     an invalidation or a state change.
   - `offScreen = !trackArea && isOffScreen(c, visibleRect)` (see §3).
   - `g2dWrapper.startedDrawingComponent(i)`. Then either `startRecordingDrawnBounds()` (when
     tracking) or `stopTracking()`.
   - Unless `offScreen`, call `drawComponent`. That goes through `DrawingCache.draw` when caching
     is on, and straight to `component.draw` otherwise.
   - `finishedDrawingComponent()` returns a `ComponentArea`. When tracking and the outline is not
     empty, it is stored in `componentAreaMap` together with the state it was drawn in.
   - Mirrored copies for board undersides are drawn after that, outside both mechanisms.

**Areas are invalidated** by `DrawingManager.invalidateComponent`, which Presenter calls for
components moved by a drag step and for edited components. They are cleared wholesale by
`clearComponentAreaMap`: project load, undo/redo (which goes through `loadProject`), delete,
rotate, mirror and operation-mode changes. **Zoom does not invalidate areas.** They live in project coordinates.

## 2. Image caching (`DrawingCache`)

### What gets cached

A component is cached when both of these hold:

- its type has `@ComponentDescriptor(enableCache = true)` (the default is `false`);
- `shouldCache` passes:
  - **not in `ComponentState.DRAGGING`**. A dragged component changes every frame, so its image
    would be thrown away immediately while still pushing useful entries out;
  - **at most `MAX_CACHED_IMAGE_PIXELS` (8 Mi pixels, 32 MB)**, measured as
    `getCachingBounds()` × `zoom` × `scaleFactor` on each axis. A big board at high zoom on a
    HiDPI screen would otherwise take a large share of the budget on its own.

Anything else is drawn directly. The same component can switch between the two paths as zoom
changes.

### Entries

- **Key:** the component instance. Components do not override `equals`/`hashCode`, so this is
  identity.
- **Value (`CacheValue`):**
  - a `TYPE_INT_ARGB` `ScaledBufferedImage` of `getCachingBounds()` at the current zoom and HiDPI
    scale;
  - the outline and continuity areas captured while rendering it;
  - the cache bounds;
  - the parameters the entry is valid for.
- **Validity:** an entry is reused only if the component still `equalsTo` the clone taken at
  render time, and state, zoom, scale factor, outline mode and `DrawOption`s all match. Otherwise
  it is re-rendered and replaced.
- **Placement:** the image is drawn at the first control point minus an integer offset, so it can
  sit up to one unit away from the cache bounds.
- **Off-screen components are not rendered into the cache.** `renderAndCache` returns `null` when
  the caching bounds miss the clip, and `DrawingCache.draw` then draws the component directly.
  Java2D discards the pixels, but the wrapper still records the component's areas. This matters
  because the netlist, "highlight connected areas", compare and the chatbot all read continuity
  from `componentAreaMap` (`DrawingManager.getContinuityAreas`). A cached conductor such as an
  eyelet, solder pad, trace cut, jack, pot or stripboard must not drop out of it just because it is
  off-screen. Culling (§3) means this direct draw happens once per invalidation, not on every
  repaint.

### Memory: a byte budget with LRU eviction

- `imageCache` is an **access-ordered `LinkedHashMap`**. The cache keeps a running total of image
  bytes (`width × height × 4`).
- **Budget:** `MAX_CACHE_BYTES = min(maxMemory / 4, 768 MB)`, which is 768 MB with the shipped
  `-Xmx4096m`.
  - A quarter of the heap leaves room for the project model, undo clones, area maps and exports.
  - The 768 MB cap is a few full Retina canvases' worth of overlapping images.
- **Eviction:** after each insert, least recently used entries are evicted until the total is back
  under budget. The entry just inserted is never evicted, even if it alone exceeds the budget.
- **Release:** every entry that leaves the cache (evicted, replaced, `invalidate`d or `clear`ed)
  has `BufferedImage.flush()` called on it. That drops Java2D's accelerated (VRAM or native) copy,
  which would otherwise live until finalisation.
- **Why not `SoftReference`:** this replaced a `SoftReference` map. HotSpot keeps soft referents
  until the heap is nearly full. That kept the large pixel arrays alive, which G1 allocates as
  humongous objects, so they could only be reclaimed by a full GC. That was the reason the
  status-bar GC button helped. Strong references with explicit eviction let G1 reclaim the arrays
  in ordinary young collections.
- **Clearing:** `clear()` runs on `Presenter.loadProject` (including undo/redo).
  `invalidate(components)` runs on delete.

### Threading

`bulkPrepare` calls `renderAndCache` from parallel-stream threads.

- Every read and write of `imageCache`, and of the byte total, is under `synchronized (imageCache)`.
- The lock covers only the lookup and the insert. Rendering happens outside it.
- `renderStatsByType` is a `ConcurrentHashMap`. `Counter.add` is `synchronized`.
- The hit/miss counters are `AtomicLong`s.

## 3. Off-screen culling

### Measured drawn bounds

Culling needs a rectangle that contains *everything* a component draws. Neither existing candidate
can serve:

- **the tracked outline area** leaves out whatever is drawn after `stopTracking()`, typically
  labels and decoration;
- **`getCachingBounds()`** is `null` by default and is a hand-tuned guess where it is implemented.

So `G2DWrapper` measures what each component actually draws:

- **When it measures.** Only while recording, which `DrawingManager` turns on for draws where
  `trackArea` is true. Ordinary repaints pay nothing.
- **What it covers.** All drawing methods feed `appendDrawnBounds`, independent of
  `trackingAllowed`. The result is transformed by `currentTx` into project coordinates, the same
  space as the areas.
- **Bounds per drawing call:**
  - Strokes: the shape bounds padded by `lineWidth / 2 × max(√2, miterLimit)` for `JOIN_MITER`, or
    `× √2` otherwise. That covers square caps and miter spikes. A non-`BasicStroke` uses
    `createStrokedShape(s).getBounds2D()`.
  - Strings: `FontMetrics.getStringBounds`.
  - Attributed strings: `TextLayout.getBounds`.
  - Glyph vectors: `getVisualBounds`.
  - Images: their destination rectangle, or the transformed image rectangle.
- **Cached components** never draw through the canvas wrapper. `DrawingCache.draw` reports the
  entry's cache bounds through `mergeDrawnBounds` instead.
- **Storage.** The rectangle is stored as `ComponentArea.getDrawnBounds()`. It is `null` when
  nothing was recorded.

### The rule

`DrawingManager` skips `drawComponent` when **all** of these hold:

1. the component's areas are already recorded for its current state (`!trackArea`);
2. a visible rect was passed in;
3. the component has measured drawn bounds;
4. those bounds, grown by `OFF_SCREEN_MARGIN` (4 units, for antialiasing and whole-pixel image
   placement), do not intersect the visible rect.

The first condition is what keeps culling safe. Areas are only ever produced by drawing, so a
component is always drawn, visible or not, until its areas exist. Clicking to select, hover,
continuity and the netlist therefore never lose a component because it was off-screen.

These are never culled:

- the first draw after load or invalidation;
- components whose outline area came out empty (they are never stored, so `trackArea` stays true);
- every draw when no visible rect is passed (exports, printing, regression);
- mirrored underside copies, control points and the component slot.

Culled components are not counted in the per-type render stats.

## 4. Rules for component code

Each rule exists so that one of the mechanisms above stays correct:

- **Draw only through the `Graphics2D` passed to `draw()`.** Do not use `create()` or reach for
  an underlying graphics object. Anything drawn that way is missing from the measured bounds, and
  the component can be culled while part of it is visible. No component in core or library does
  this today.
- **Anything that changes appearance must invalidate the component.** Use
  `Presenter`/`DrawingManager.invalidateComponent`, or a wholesale clear. Otherwise both the
  stored areas and the drawn bounds go stale. With culling, stale bounds show up as a component
  missing at the edge of the view.
- **For cached types, `getCachingBounds()` must contain everything drawn.** The cached image is
  clipped to it, and the culling bounds come from it.
- **`draw()` is hot.** Fetch strokes from `ObjectCache` and do not log. See `CLAUDE.md`.

## 5. Diagnostics

Logging is at `DEBUG` (`diylc-swing/src/main/resources/log4j.properties`). Output goes to the
console and to a per-run file in `~/diylc/logs/`. `DrawingManager.logStats` writes at most once
every `statReportFrequencyMs` (60 s), after a repaint:

```
DrawingManager - Render stats: {type=count time; ...}        drawn components only
DrawingManager - Page stats: <repaints> <avg ms>
DrawingCache - Render cache contains N elements, approx size is X MB of <budget> MB
DrawingCache - Render cache hits: H, misses: A not cached + B outdated, hit rate: R%,
               re-rendered while painting: T
DrawingCache - Cache render distribution stats: {type: render: ..., paste: ...}
```

All counters accumulate over the session.

- **hits / misses** count only the `bulkPrepare` lookups. The second lookup in `draw()` would
  otherwise hold the hit rate at 50% or more.
- **not cached** means the entry was missing: a first render or an eviction. It falls as the
  budget grows. If it stays high while the size sits at the budget, the budget is too small.
- **outdated** means the component changed. The budget cannot reduce it.
- **re-rendered while painting** counts entries evicted between `bulkPrepare` and `draw` in the
  same repaint. Anything above 0 means the visible set alone exceeds the budget.

Baseline from a complex layout before culling: 327 repaints averaging 26 ms.

- The cache sat at 61 MB of 768 MB with a 95.5% hit rate, nothing evicted, and nothing
  re-rendered while painting.
- The largest single cost was the uncached `HookupWire`: about 224 drawn per repaint, roughly
  9 ms of the 26 ms.
- `TwistedWire` cost about 96 ms per render.

## 6. Known limitations and next steps

Ordered by expected payoff.

1. **Measure culling.** Repeat the baseline above on the same layout, zoomed in so that part of it
   is off-screen. Compare the "Page stats" average, not per-type counts, since culled draws are
   no longer counted.
2. **Run the regression suite.** It has not been run on these changes. Exports draw everything, so
   the run guards against exceptions in the new bounds code rather than against culling.
3. **Components that move with a drag but are not selected** (stuck components) stay in `NORMAL`
   state. They are re-rendered and re-cached every frame. Fixing this needs Presenter to tell the
   cache which components are moving.
4. **Zoom does not invalidate areas or drawn bounds.** Content that is larger at another zoom
   could be culled at the edge of the view. Raise `OFF_SCREEN_MARGIN`, or invalidate on zoom, if
   that shows up.
5. **`HookupWire` and `TwistedWire`.** If culling is not enough, either enable caching for
   `HookupWire` (large, mostly empty bounding boxes, but plenty of budget) or profile it. Profile
   `TwistedWire.draw` in any case.
6. **Components over the pixel limit are drawn directly.** That may make large perfboards and
   stripboards slow at high zoom. Caching only the visible tile of such components would fix it.
7. **Reuse the image buffer** when a stale entry has the same pixel size: clear it and redraw.
   That removes allocation churn for property edits. It must be safe against an export rendering
   concurrently.
8. **`CacheValue` keeps a full clone** of each component just for `equalsTo`. That is costly for
   components carrying large data (images, long point lists).
9. **`DrawingCache.Instance` is a static singleton.** `ProjectPreview` calls `loadProject`, which
    clears it. If the preview runs its own `Presenter`, browsing files wipes the main canvas
    cache. Check, and give each `Presenter` its own cache if so.
10. **Shrink the budget under memory pressure** with a `MemoryPoolMXBean` usage threshold. This
    is only worth doing if users run with a much smaller `-Xmx`.
