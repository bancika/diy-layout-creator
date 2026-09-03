# 0004 — The Schematic tab reuses the real canvas via a second `Presenter` + `CanvasPlugin`

**Status:** accepted

## Context

The Schematic View lives on a tab next to Layout. It must render *exactly* like the layout
canvas: rulers, scroll bars, mouse-wheel / trackpad-gesture zoom, the same look. Early
iterations used a plain `JScrollPane` around a lightweight custom panel with its own zoom
buttons; the user rejected that — same UI, no extra controls.

`CanvasPanel.paint()` calls `plugInPort.draw(...)`, `RulerScrollPane` is built around a
`ProjectDrawingProvider(plugInPort, ...)`, and zoom is `plugInPort.setZoomLevel(...)`. In
other words, the canvas is inseparable from a `Presenter`. `Presenter` is a ~3000-line class
bound to a single `currentProject`.

## Decision

`SchematicTabPlugin` owns a **second `Presenter`** and a **second `CanvasPlugin`**:

```java
schematicPresenter = new Presenter(swingUI, configManager, /*importVariantsAndBlocks*/ false);
schematicCanvasPlugin = new CanvasPlugin(swingUI, configManager);
schematicPresenter.installPlugin(() -> schematicCanvasPlugin);      // injects its RulerScrollPane
schematicPresenter.installPlugin(SchematicChangeListener::new);     // reroute-on-move listener
```

- Both `CanvasPlugin`s inject their `RulerScrollPane` into the same center `BoxLayout` panel;
  `SchematicTabPlugin` toggles which one is `setVisible(true)`. `CanvasPlugin` gained a
  `getCanvasScrollComponent()` accessor for this.
- The tab strip is a `JToolBar` with two `JToggleButton`s, injected below the scroll panes.
- Switching to Schematic: `SchematicSynchronizer.synchronize(layoutProject, continuityAreas)`,
  then wrap `layoutProject.getOrCreateSchematicView()` in a throwaway `Project` (shares the
  same component instances) and `schematicPresenter.loadProject(wrapper, true, null)`, then
  swap visibility and `scrollToCenterAndShowContents()`.
- `IPlugInPort` / `INetlistProcessor` gained `getContinuityAreas()` (delegates to the
  drawing manager) so the tab can build the netlist without reaching into `Presenter`
  internals.

The detached *Analyze → Schematic View…* window (`SchematicViewFrame` / `SchematicPanel`)
is a separate, `Presenter`-free read-only renderer kept for PNG export.

## Consequences

- **+** Pixel-identical canvas: rulers, scroll, wheel/gesture zoom, theme — all for free.
- **+** Independent zoom/scroll state per tab.
- **+** Symbol selection and drag work through the standard `Presenter`.
- **−** A second `Presenter` is heavyweight: it builds its own `DrawingManager`,
  `ProjectFileManager`, `InstantiationManager`, `VariantManager`, `BuildingBlockManager` and
  registers config listeners. `importVariantsAndBlocks=false` skips the expensive part.
- **−** `loadProject` on the schematic presenter calls `DrawingCache.Instance.clear()`, a
  process-wide cache — a minor cross-effect on the layout canvas.
- **−** The schematic canvas is a full editing `Presenter`; nothing yet blocks symbol
  add/delete/paste/value-edit, and its undo stack is separate from the layout's.
- The tab and the detached window are two independent renderers of the same data. Acceptable
  redundancy; could be unified later.
