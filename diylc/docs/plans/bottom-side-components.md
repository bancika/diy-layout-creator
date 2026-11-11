# Bottom-Side Components

Let a component be mounted on the bottom of a PCB, so that its silkscreen goes to the bottom
Gerber layers and the canvas shows it as a part on the far side of the board.

Status: the core Gerber side is done, with tests: `IBoardSideComponent` exists and the export
sends a bottom-side part to the bottom layers. No component implements it yet; the next steps below
remain.

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
| Flipping a part is an action that sets the side, mirrors the part and sends it back | A part seen from above through the board is a mirror image. Changing the side without mirroring gives a footprint that is wrong for fabrication. |
| The Gerber layer is swapped centrally, not inside each component | Components keep emitting their `*Top` layer. One place maps it to the other side. |
| Default `_2` (Top), with `null` meaning Top | Fields added to existing classes deserialize as `null` in old files. |

## 3. Design

### diylc-core

- **Canvas rendering**: bottom-side components use the far-side fade from `copper-side-view.md`,
  wrapping their painting in `startDrawingSide(getBoardSide())`. They are then faded in the main
  view and solid in the Underside Display mirror, the same as bottom copper.

### diylc-library

- The ~35 classes that use the `SilkscreenTop` default implement `IBoardSideComponent`. Each adds a
  nullable `PCBLayer boardSide` field and a getter/setter pair, with
  `@EditableProperty(name = "Board Side")` on the getter. The getter returns `_2` (Top) when the
  field is `null`. This duplicates the field across classes in the same way the `PCBLayer` field is
  duplicated today. Where several of these classes share an abstract base
  (`AbstractRadialComponent`, `AbstractFilmCapacitor`, `AbstractTransistorPackage`, ...), put it on
  the base.
- `CopperTrace`, `CurvedTrace`, `GroundFill`, `SolderPad`, `PCBText` and `DrillHole` do **not**
  implement it. Their `PCBLayer` already says where they are.
- Before allowing Bottom on a class, check that its transformer can mirror it
  (`canMirror`). A class that cannot be mirrored must not offer Bottom, or the footprint would be
  wrong.

### diylc-swing

- **Flip to Other Side** action in the Edit menu and the canvas popup menu, following the
  align-distribute actions. For each selected `IBoardSideComponent` it:
  1. toggles the side,
  2. mirrors horizontally about the selection centre with the component's `IComponentTransformer`,
  3. sends the component to the back of its z-order band.

  The whole action is one undo step, and it is enabled only when the selection holds at least one
  `IBoardSideComponent`. The work is done in `Presenter` behind `IPlugInPort`, with the plugin
  only wiring the menu items.
- **PCB print-outs**: `SilkScreenDrawingProvider` prints one page of everything that has a
  silkscreen outline. Split it into top and bottom pages, filtered by side, the same way
  `TraceMaskDrawingProvider` gives one page per `PCBLayer`.

### Editing through the Underside Display mirror

The mirror is a second drawing of the same components, not a separate space: every component
lives in main-view coordinates. Editing in the mirror therefore means mapping the mouse back
through the mirror's reflection before the existing logic runs. A reflection is its own inverse,
so drawing and hit-testing use the same transform. Each stage is useful by itself and ships in
order.

- **Shared mirror geometry.** Move the transform built in `DrawingManager.drawMirroredComponent`
  into a helper in `org.diylc.presenter` (not into `Presenter`). It answers two questions: the
  reflection for a given `IBoard`, and which board's mirror region, if any, contains a model point.
  The region is the board rectangle reflected about the pivot. `drawMirroredComponent` uses the
  helper, so drawing and hit-testing cannot drift apart.
- **Stage 1: selecting in the mirror.** In `Presenter.findComponentsAt` and the click and hover
  paths, a point that falls in a mirror region and hits nothing in main-view space is reflected
  and tested again, limited to the components that board mirrors (those whose control point lies
  inside it, as in `drawMirroredComponent`). Clicking a part in the mirror selects the original, so
  the property editor, delete, copy and the popup menu all work unchanged. Selection highlighting
  and control points are drawn on both copies.
- **Stage 2: dragging in the mirror.** A drag that starts in a mirror region reflects every
  subsequent mouse point before it reaches the existing drag code. Movement along the mirrored axis
  is therefore reversed, which is what the user sees. Snapping applies to the reflected point. Moving
  the board, or changing its Underside Offset, cancels nothing; the mirror simply follows.
  Control-point drags (stretching a trace) go through the same mapping.
- **Stage 3: placing in the mirror.** A component instantiated with its first point in a mirror
  region is created at the reflected point. If it is an `IBoardSideComponent`, it is created with
  side `_1` (Bottom) and mirrored, as Flip to Other Side would do. A `PCBLayer` component is created on
  `_1`. Anything else is placed at the reflected point unchanged.

## 4. Open questions

- **Bottom silkscreen text.** Component labels drawn through `IGerberComponentSimple` have to read
  correctly once the board is turned over, which means they must look mirrored from the top. Check
  whether mirroring through the transformer does that for every class, or whether labels are drawn
  unmirrored regardless. `GerberLayer.mirrored` is currently `false` for every layer, and the
  exporter output must stay in top view.
- **SolderPad solder mask.** `SolderPad` always writes its mask opening to `SolderMaskBot`,
  whatever its own `PCBLayer`. Confirm whether this is intended for top-layer SMD pads before
  `GerberLayer.flip()` is introduced next to it.
- **Plain property or action only.** A plain editable property lets a user change the side without
  mirroring. Either keep both (the property for already-mirrored parts) or show the side as
  read-only and change it only through the action.
- **Components that belong to no side.** In mirror stage 3 a jumper, eyelet or wire dropped in the
  mirror has no side to set. Confirm that placing it at the reflected point unchanged is what users
  expect, or whether such parts should be refused in the mirror.
- **Components inside two boards.** A component whose control points fall inside two boards with
  Underside Display on appears in both mirrors. Selecting through either is fine. Check that
  dragging through one mirror while the other redraws does not confuse the user.

## 5. Verification

- `GerberLayerTest` covers `flip()`. `GerberExporterTest` covers the dispatch for simple components,
  including a stub `IBoardSideComponent` on top, bottom and `null`.
- Load an old `.diy` with PCB parts and confirm every component reports `_2` (Top) and that the Gerber
  output is byte-identical to before.
- Export a board with one part on each side and open the files in a Gerber viewer: the bottom part
  appears only in `.gbo`, mirrored, and in the right position.
- Unit tests for the mirror geometry helper: the region for each `BoardUndersideDisplay` direction
  and offset, and that reflecting a point twice returns it.
- In the app, with Underside Display set to each direction: select, drag and stretch a trace in
  the mirror and confirm that the original follows correctly. Place a part in the mirror and confirm
  that it is bottom-side, mirrored, faded on the main view and solid in the mirror.
- Run the regression suite, since rendering and serialization both change.

## 6. Next steps

1. Add the field to the library components, putting it on shared bases where they exist. Settle
   the silkscreen-text question while doing so.
2. Wrap bottom-side painting in `startDrawingSide(getBoardSide())`.
3. Add the Flip to Other Side action.
4. Split the silkscreen print-out by side.
5. Extract the mirror geometry helper, with tests, and use it in `drawMirroredComponent`.
6. Mirror stage 1: selecting through the mirror. It does not depend on steps 1-4 and can ship
   early.
7. Mirror stage 2: dragging through the mirror.
8. Mirror stage 3: placing in the mirror, after Flip to Other Side exists.
9. Run the regression suite.
