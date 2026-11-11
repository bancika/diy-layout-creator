# Bottom-Side Components

Let a component be mounted on the bottom of a PCB, so that its silkscreen goes to the bottom
Gerber layers and the canvas shows it as a part on the far side of the board.

Status: 45 parts have a Board Side property, and Gerber export sends a bottom-side part's
silkscreen to the bottom layer with its text mirrored. Parts can be selected, dragged and stretched
through the Underside Display mirror. The canvas draws a bottom-side part faded in the main view and
solid in the mirror. The silkscreen print-out has a separate page for the bottom, and new parts
can be placed through the mirror. Every step is done, and the regression suite passes on all 97
projects; the open questions below remain.

## 1. Current state

- Traces, pads, PCB text and ground fills carry a `PCBLayer`: `Bottom` and `Top`, plus the
  deprecated `Inner 1..6`, kept only so that old files load and hidden in the editor. It feeds
  `PCBLayer.toGerberCopperLayer()`, `PCBLayerFilter`, the trace-mask print and continuity
  (`ILayeredComponent.getLayerId()`).
- `IDrawingObserver.startDrawingSide(PCBLayer)` / `stopDrawingSide()` already exist
  (`copper-side-view.md`): paint on the side facing away from the viewer is drawn faded, in both
  the main view and the Underside Display mirror.
- Every other Gerber-aware component implements `IGerberComponentSimple`, whose
  `getGerberLayer()` defaults to `SilkscreenTop`. About 35 classes in `diylc-library` rely on that
  default (resistors, film and radial capacitors, diodes, LEDs, transistor packages, DIL/SIL ICs,
  SMD passives, switches, jacks, relays, transformers, tube sockets, ...). None of them emit copper.
  Their pads are separate `SolderPad` components, which already have a layer.
- One method decides the layer for a simple component: `GerberExporter.drawComponent`. It is
  package-private so that `GerberExporterTest` can call it. It uses `getGerberLayer()`, flipped
  with `GerberLayer.flip()` when the component is an `IBoardSideComponent` on `_1` (Bottom).
- **`org.diylc.core.IBoardSideComponent`**: `PCBLayer getBoardSide()`, returning `_1` or `_2`
  only. It is deliberately separate from `ILayeredComponent` (see Decisions). Core knows a part's
  side only through this interface.
- Z-order is paint order only. A trace on the bottom layer already paints above the board
  (`zOrder = TRACE`), so the codebase already treats the physical side as separate from z-order.

## 2. Decisions

| Decision | Rationale |
| --- | --- |
| The side is a `PCBLayer` (`_2` Top, `_1` Bottom). No separate side enum | DIYLC only models two-sided boards. The inner layers are deprecated and hidden in the editor (`copper-side-view.md`), so only the two valid choices are offered. |
| A part's side goes through its own `IBoardSideComponent`, never through `ILayeredComponent` | `getLayerId()` drives continuity, and a through-hole part's leads must connect to copper on both sides. Reporting a resistor as layer `_1` there would cut it off from top copper. |
| The side is the physical fact. Z-order stays paint order and is not tied to it | Boards are drawn opaque, so painting bottom parts beneath the board would hide them. Bottom-layer traces already follow this rule. |
| The canvas shows the side visually and does not rely on z-order for it | That is what removes the ambiguity of a "bottom" part drawn above the board. |
| The side is a plain property, and the app never mirrors a part for the user | Seen from above, a part under the board is a mirror image, so a part with an asymmetric footprint moved to the bottom also needs Mirror Horizontally. Doing that for the user was tried as a Flip to Other Side action and dropped as menu clutter; the user sets the side and mirrors the part where needed. |
| The Gerber layer is swapped centrally, not inside each component | Components keep emitting their `*Top` layer. One place maps it to the other side. |
| Default `_2` (Top), with `null` meaning Top | Fields added to existing classes deserialize as `null` in old files. |
| Bottom-side text is mirrored centrally in the Gerber output | Parts draw their labels readable from the top, which is backwards on the bottom silkscreen once the board is turned over. While `GerberExporter.drawComponent` draws a bottom-side part, `GerberG2DWrapper.setMirrorText` mirrors each string about the centre of its layout box, so no component changes and the canvas keeps readable labels. |
| A part without a side placed in the mirror is placed unchanged | A jumper, eyelet or wire has no side to set, so it is placed at the reflected point like any other part rather than refused. |
| A `SolderPad` opens the solder mask on its own side | It used to write `SolderMaskBot` whatever its layer, which was wrong for top-layer pads. It now writes `SolderMaskTop` for a pad on `_2`. |

## 3. Design

### diylc-core

- **Canvas rendering (done)**: bottom-side parts use the far-side fade from `copper-side-view.md`,
  set centrally rather than in each component. Wherever a component is drawn - the main loop in
  `DrawingManager`, `DrawingCache` rendering an image, and the Underside Display mirror - the
  wrapper's undeclared side is set to `DrawingManager.getMountedSide(component)` with
  `G2DWrapper.setUndeclaredSide`. A bottom part is therefore faded in the main view (with Fade
  Copper Under Board on) and solid in the mirror, the same as bottom copper. In the main loop the
  side is set after the locked composite, which is set on the canvas directly and would otherwise
  replace the fade. `DrawingCache` pastes a cached image as both-sides paint, because the fade is
  already in the image.

### diylc-library

- **Done.** Every part that uses the `SilkscreenTop` default implements `IBoardSideComponent`: a
  nullable `PCBLayer boardSide` field and a getter/setter pair with
  `@EditableProperty(name = "Board Side")` on the getter, which returns `_2` (Top) when the field
  is `null`. The key is in every language file. 45 parts and `Dot` have it.
  - On a shared base, covering every subclass: `AbstractFilmCapacitor`, `AbstractPotentiometer`,
    `AbstractRadialComponent`, `AbstractTransistorPackage`, `PassiveSMDComponent`.
  - On the class itself: `Resistor`, `HeatSinkResistor`, `AxialElectrolyticCapacitor`,
    `CrystalOscillator`, `ToroidalInductor`, `AudioTransformer`, `MultiSectionCapacitor`,
    `DiodePlastic`, `DiodeGlass`, `BridgeRectifier`, `DIL_IC`, `SIL_IC`, `MultiwattIC`,
    `TubeSocket`, `CliffJack1_4`, `CliffJack1_8`, `ClosedJack1_4`, `PlasticDCJack`,
    `MiniToggleSwitch`, `SlideSwitch`, `TactileMicroSwitch`, `RotarySwitchOpen`,
    `RotarySwitchSealed`, `RotarySelectorSwitch`, `MiniRelay`, `PCBTerminalBlock`.
  - Whether a part can be mirrored does not matter: the app never mirrors it for the user.
  - `Dot` has it too, though it has no silkscreen: a dot used as a solder joint on stripboard can
    be put on the copper side (see `copper-side-view.md`).
  - `Label` is left out: it is text, not a mounted part, and `PCBText` already has a layer.
  - `BoardSideComponentsTest` checks that every such part starts on Top and takes Bottom, and
    that every part writing `SilkscreenTop` has a side, so a new part cannot be left out by
    accident.
- `CopperTrace`, `CurvedTrace`, `GroundFill`, `SolderPad`, `PCBText` and `DrillHole` do **not**
  implement it. Their `PCBLayer` already says where they are.

### diylc-swing

- **PCB print-outs (done)**: `SilkScreenDrawingProvider` prints the top side, and a second page for
  the bottom when a part is mounted there, so a project without bottom parts still prints one page.
  `SilkScreenFilter` takes the side: a part with a Board Side goes on its own side's page, parts
  without one count as top, and board outlines go on both pages so that they line up. Neither
  page is mirrored or captioned, the same as the trace-mask pages.

### Editing through the Underside Display mirror

The mirror is a second drawing of the same components, not a separate space: every component
lives in main-view coordinates. Editing in the mirror therefore means mapping the mouse back
through the mirror's reflection before the existing logic runs. A reflection is its own inverse,
so drawing and hit-testing use the same transform. Each stage is useful by itself and ships in
order.

- **Shared mirror geometry (done).** `org.diylc.presenter.UndersideMirror` holds the reflection
  for a board, the region its mirror occupies (the board rectangle reflected about the pivot), and
  which components a mirror shows: the board and every component with a control point on it.
  `DrawingManager` draws the mirror with it, so drawing and hit-testing cannot drift apart.
- **Stage 1: selecting in the mirror (done).** `DrawingManager.findMirroredComponentsAt` reflects a
  point that falls in a mirror region and tests it again, limited to the components that board
  mirrors. `Presenter.findComponentsAtScaled` falls back to it when nothing selectable is hit in
  the main view; locked and hidden parts are filtered out first, so a locked part under the mirror
  does not block it. Clicking a part in the mirror selects the original, so the property editor,
  delete, copy and the popup menu all work unchanged. Selection highlighting and the selected
  control points are drawn on both copies.
- **Stage 2: dragging in the mirror (done).** A drag that starts on a part in a mirror, or on a
  control point of a selected part there, keeps that mirror's reflection in
  `Presenter.dragMirrorTx` and reflects every mouse point of the drag before it reaches the existing
  drag code. Movement along the mirrored axis is therefore reversed, which is what the user sees,
  and snapping and the shift axis lock apply to the reflected point. Control-point hover
  (`Presenter.findControlPointsAt`) and the hand cursor look through the mirror when the main view
  has nothing. A drag that starts on empty mirror space still draws a selection rectangle in
  screen space. Moving the board, or changing its Underside Offset, cancels nothing; the mirror
  simply follows.
- **Stage 3: placing in the mirror (done).** While a component is being placed,
  `Presenter.placementMirrorBoard` holds the board whose mirror the cursor is in; a single-click
  part follows the cursor in and out of mirrors, and a part placed point by point stays in the
  mirror its first point went into. The cursor is reflected before it is snapped to the grid, so
  the part lands on the grid, and `DrawingManager.setSlotMirrorBoard` draws the part being placed
  through that mirror, under the cursor. On placement, `MirrorPlacement.placeOnBottom` sets an
  `IBoardSideComponent` to `_1` (Bottom) and a component with a `PCBLayer` (`setLayer`, found the
  same way `PCBLayerFilter` reads `getLayer`) to `_1`; nothing is mirrored. Anything else, such as
  a jumper, eyelet or wire, is placed at the reflected point unchanged. A part dragged in from
  the component tree or the toolbox goes through `Presenter.dragOver` and `dragEnded` instead,
  which pick the mirror and put the part on the bottom the same way.

## 4. Open questions

- **Locked bottom-side parts drawn from the cache.** An uncached locked part takes the stronger of
  the locked alpha and the far-side alpha. A cached one is pasted at the locked alpha onto an
  image that already carries the far-side fade, so it ends up fainter. The same applies to
  bottom-side paint inside any cached component.
- **Components inside two boards.** A component whose control points fall inside two boards with
  Underside Display on appears in both mirrors. Selecting through either is fine. Check that
  dragging through one mirror while the other redraws does not confuse the user.

## 5. Verification

- `GerberLayerTest` covers `flip()`. `GerberExporterTest` covers the dispatch for simple components,
  including a stub `IBoardSideComponent` on top, bottom and `null`.
- `MirrorPlacementTest` covers what placing through the mirror changes, and
  `PresenterTests.testPlaceThroughUndersideMirror` and `testPlaceUnsidedPartThroughUndersideMirror`
  place a resistor and an eyelet in a mirror and check that they land on the board, the resistor on
  the bottom; `testDropFromComponentTreeIntoUndersideMirror` does the same for a drag and drop.
- `SilkScreenFilterTest` covers which side's page each part and board outline goes on, and
  `SilkScreenDrawingProviderTest` checks that the bottom page appears only for a bottom part.
- `DrawingManagerTest` checks rendered pixels for a part that reports its own side: a bottom part
  is faded only with `FADE_FAR_SIDE`, also when drawn from the image cache; it is solid in the
  mirror, where a top part is faded; and a locked bottom part is faded once, not twice.
- Load an old `.diy` with PCB parts and confirm every component reports `_2` (Top) and that the Gerber
  output is byte-identical to before.
- Export a board with one part on each side and open the files in a Gerber viewer: the bottom part
  appears only in `.gbo`, mirrored, and in the right position.
- `UndersideMirrorTest` covers the mirror region for each `BoardUndersideDisplay` direction and
  offset, that reflecting a point twice returns it, and which components a mirror shows.
  `DrawingManagerTest.testComponentFoundThroughUndersideMirror` covers the hit test, and
  `PresenterTests.testStretchThroughUndersideMirror` and `testDragThroughUndersideMirror` check
  that a drag through a mirror below the board moves the part up on the board.
- In the app, with Underside Display set to each direction: select, drag and stretch a trace in
  the mirror and confirm that the original follows correctly. Place a part in the mirror and confirm
  that it is bottom-side, faded on the main view and solid in the mirror.
- The regression suite passes on all 97 projects with the whole plan in place.

## 6. Next steps

1. Settle the open questions above.
2. Check Gerber output of a board with parts on both sides in a Gerber viewer, and try the
   Underside Display editing in each direction in the app, as listed under Verification.
