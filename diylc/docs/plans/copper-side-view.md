# Copper-Side View

Make it obvious which side of a board the canvas shows. The main view is the component side, and
copper on the bottom of the board is drawn faded, as if seen through it. The Underside Display
mirror is a true view from below: copper solid, components faded.

Status: done and committed; the regression suite passes on all 97 projects and its reference images
were regenerated for the faded copper. The open questions below remain.

## 1. Background

- Z-order is paint order (`BOARD` < `TRACE` < `COMPONENT`), so boards, strips and traces were all
  painted with copper on top and users mistook the main view for the copper side.
- Strips and pads are not separate components: the boards paint them inside their own `draw()`.
- `CopperTrace`, `CurvedTrace`, `SolderPad`, `GroundFill` and `PCBText` carry a `PCBLayer` that
  defaults to `_1` (Bottom), so the model already placed them on the bottom.

## 2. Decisions

| Decision | Rationale |
| --- | --- |
| One rule for every view: near side solid, far side at reduced opacity, as if seen through the board | The same rule covers the main view, the mirror and, later, bottom-side components (`bottom-side-components.md`). |
| Paint order and z-order stay unchanged. Opacity alone shows the side | Boards are opaque, so painting copper beneath the board would hide it. Editing, hit-testing and selection are untouched. |
| Rendering-only change. No serialized field is added, renamed or retyped | Old files open with the new look and nothing to migrate. |
| `PCBLayer` is the side; no separate side enum. Inner layers are deprecated, not removed | DIYLC models two-sided boards only. XStream resolves enums by name, so `_3`-`_8` must stay for old files to load. |
| Far-side paint uses plain reduced opacity (`DrawingManager.FAR_SIDE_ALPHA`, 55%), not raised when selected | Simple, and reads clearly as "behind". 45% was too faint on the default phenolic strips. |
| The fade takes the stronger of `FAR_SIDE_ALPHA` and the component's own alpha; it does not multiply them | Multiplying with the 50% locked alpha left a locked board's copper at about 22%. The cost is that a bottom part already more translucent than `FAR_SIDE_ALPHA` does not fade further. |
| A global Config toggle, **Fade Copper Under Board**, on by default and not saved in projects | Users who prefer the old look can turn it off without a per-board property. |
| The canvas and the layout export, PDF and print follow the toggle. Trace mask, silkscreen and Gerber are unaffected | The layout outputs show what the canvas shows; the fabrication outputs build their own `DrawOption` sets. |
| The canvas draws no side labels. Users label the sides themselves | A caption drawn outside the mirror can sit on top of wiring, and only the user knows where a label fits. |
| `undersideTransparency` no longer fades the whole mirror: `true` (default) fades the far side, `false` draws the mirror fully solid | The near side of a true underside view has to be solid. The field keeps its name, so old files load. |
| Paint a component does not declare is component side, except a board's own paint in the mirror, which is drawn as near side | Components need no changes. A board's substrate is seen from both sides, so it must not fade in the mirror. |
| Holes and trace cuts are visible from both sides (`startDrawingBothSides()`) and never fade; a board's own holes are already solid as board paint | A faded hole reads as copper, and a faded cut hides the break the user is looking for. |
| `Jumper`, `Eyelet`, `Turret`, `Top`-layer copper and `EyeletBoard` eyelets are component side | Jumpers run over the board; eyelets and turrets are visible from the top. |
| `ProtoBoard780` declares its traces and pads bottom side, like `PerfBoard`. `ProtoBoard` declares only the traces and pads its code paints in the underside colours. `EurorackStripboard` declares nothing | The 780-hole boards carry copper on the bottom only. `ProtoBoard` is plated through with some traces on top. Which side `EurorackStripboard`'s traces are on is not sourced. |
| `Dot` has a Board Side property, Top by default | Dots are junctions in schematics and wiring diagrams, which must not fade, and solder joints on stripboard, which are on the copper side. Setting a solder joint's side to Bottom fades it in the main view and draws it solid in the mirror; a dot placed in the mirror goes to the bottom on its own (`bottom-side-components.md`). |
| `Breadboard`, `BlankBoard`, `MarshallPerfBoard`, `TagStrip`, `TerminalStrip` and the maker boards declare nothing | They have no bottom copper to show. |

## 3. Design as built

### diylc-core

- `PCBLayer`: `_3`-`_8` are `@Deprecated`, in their original order so that `getId()` is stable.
  They are not remapped and keep their own continuity layer. For fading they count as Bottom.
- `IDrawingObserver.startDrawingSide(PCBLayer)`, `startDrawingBothSides()` and
  `stopDrawingSide()` are default no-op methods, so `GerberG2DWrapper` and test observers need no
  change.
- `G2DWrapper` keeps the viewed side, the side assumed for undeclared paint, and the side being
  painted. While the painted side differs from the viewed side and fading is on, it draws at the
  smaller of `FAR_SIDE_ALPHA` and the component's own alpha. Both-sides paint is treated as the
  viewed side. `getComposite()` returns the
  component's own composite and `setComposite()` re-applies the fade, so components that save and
  restore their composite keep working. Outline mode never fades. Only the composite changes, so
  outline and continuity areas are identical with and without fading.
- `DrawOption.FADE_FAR_SIDE` turns fading on in the main view. `IPlugInPort.FADE_FAR_SIDE_KEY`
  (`fadeFarSide`) is the config key.
- `DrawingManager.drawMirroredComponent` views the mirror from `_1`, with fading controlled by
  `undersideTransparency`. The reflection comes from `UndersideMirror.createTransform`, which
  `bottom-side-components.md` added so that drawing and editing through the mirror share it.

### diylc-library

- `VeroBoard` and `TriPadBoard` declare each strip `_1`; `PerfBoard` declares each pad `_1`. The
  holes stay undeclared.
- `ProtoBoard780` declares its traces and pads `_1`; holes, mounting holes and labels stay
  undeclared.
- `ProtoBoard` declares its underside traces and underside mini pads `_1`.
- `CopperTrace` and `PCBText` declare their whole canvas `draw` on `getLayer()`. `CurvedTrace` and
  `GroundFill` declare their copper fill. `SolderPad` declares the pad on its layer and its
  drilled hole as both sides; a pad with a plated through hole declares its pad as both sides
  too, because it has copper on both. `Via` declares its copper and hole as both sides.
- `TraceCut` draws its cut and hole as both sides.

### diylc-swing

- The **Fade Copper Under Board** checkbox is in the Config menu after Extra Working Area.
  `CanvasPanel.getDrawOptions` and `ProjectDrawingProvider.draw` add `FADE_FAR_SIDE` from it.
- `EnumEditor` hides `@Deprecated` constants unless one is the current value, and
  `ComponentCatalogGenerator` leaves them out of the AI catalog's possible values; both use
  `ReflectionUtils.isDeprecated`.
- The language key **Fade Copper Under Board** is in every file in `lang/`.

## 4. Open questions

- **`EurorackStripboard`.** Unchanged until the side of its traces is sourced.

## 5. Verification

- `DrawingManagerTest` checks rendered pixels: undeclared paint is solid; bottom and inner-layer
  paint is faded only with `FADE_FAR_SIDE`; both-sides paint is solid in both views; a locked
  bottom part is faded once, not twice. In the mirror, copper is solid and the component side
  is faded, and `undersideTransparency=false` draws everything solid. The composite is restored,
  setting a composite mid-side re-fades it, outline mode never fades, and continuity areas do not
  change.
- `InnerLayerCompatibilityTest` loads a trace saved on `_5` and checks that its layer and layer id
  are kept.
- The regression reference PNGs were regenerated for the faded copper (81 images); no netlist
  changed.

## 6. Next steps

1. Settle the open questions above.
