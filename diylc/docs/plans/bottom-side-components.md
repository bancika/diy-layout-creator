# Bottom-Side Components

Let a component be mounted on the bottom of a PCB, so that its silkscreen goes to the bottom
Gerber layers and the canvas shows it as a part on the far side of the board.

Status: not started.

## 1. Current state

- Traces, pads, PCB text and ground fills carry a `PCBLayer` (`Bottom`, `Top`, `Inner 1..6`).
  It feeds `PCBLayer.toGerberCopperLayer()`, `PCBLayerFilter`, the trace-mask print and
  continuity (`ILayeredComponent.getLayerId()`).
- Every other Gerber-aware component implements `IGerberComponentSimple`, whose
  `getGerberLayer()` defaults to `SilkscreenTop`. About 35 classes in `diylc-library` rely on that
  default (resistors, film and radial capacitors, diodes, LEDs, transistor packages, DIL/SIL ICs,
  SMD passives, switches, jacks, relays, transformers, tube sockets, ...). None of them emit copper.
  Their pads are separate `SolderPad` components, which already have a layer.
- Two copies of the same dispatch decide the layer for a simple component:
  `GerberExporter.drawComponent` and `GerberPathRenderer.drawComponent`. Both call
  `g2d.startGerberOutput(getGerberLayer(), ...)`.
- Z-order is paint order only. A trace on the bottom layer already paints above the board
  (`zOrder = TRACE`), so the codebase already treats the physical side as separate from z-order.

## 2. Decisions

| Decision | Rationale |
| --- | --- |
| A new two-value `BoardSide` (`Top`, `Bottom`), not a reuse of `PCBLayer` | Parts mount on the outer faces only. Inner layers would be invalid choices in the editor. |
| The side is the physical fact. Z-order stays paint order and is not tied to it | Boards are drawn opaque, so painting bottom parts beneath the board would hide them. Bottom-layer traces already follow this rule. |
| The canvas shows the side visually and does not rely on z-order for it | That is what removes the ambiguity of a "bottom" part drawn above the board. |
| Flipping a part is an action that sets the side, mirrors the part and sends it back | A part seen from above through the board is a mirror image. Changing the side without mirroring gives a footprint that is wrong for fabrication. |
| The Gerber layer is swapped centrally, not inside each component | Components keep emitting their `*Top` layer. One place maps it to the other side. |
| Default `Top`, with `null` meaning `Top` | Fields added to existing classes deserialize as `null` in old files. |

## 3. Design

### diylc-core

- **`org.diylc.common.BoardSide`**: enum `Top`, `Bottom`, with a `toString()` label like `PCBLayer`.
- **`org.diylc.core.IBoardSideComponent`**: `BoardSide getBoardSide();` with Javadoc and
  `@author`, in the same shape as `ILayeredComponent`. Core knows the side only through this
  interface.
- **`GerberLayer.flip()`**: maps `CopperTop`/`CopperBot`, `SilkscreenTop`/`SilkscreenBot` and
  `SolderMaskTop`/`SolderMaskBot` to each other. `Outline` and the drill layers map to themselves.
- **Gerber dispatch**: in the simple-component branch, use
  `getGerberLayer().flip()` when the component is an `IBoardSideComponent` on `Bottom`. Merge
  `GerberExporter.drawComponent` and `GerberPathRenderer.drawComponent` into one method first, so
  the rule is written once.
- **Canvas rendering**: `DrawingManager.drawProject` draws bottom-side components with a
  composite that sets them apart. Locked components already use a 0.5 alpha composite
  (`lockedComposite`), so the bottom-side treatment has to look different from that, for example
  a lower alpha or a tint. Pick one and check it against a locked bottom-side part.

### diylc-library

- The ~35 classes that use the `SilkscreenTop` default implement `IBoardSideComponent`. Each adds a
  nullable `BoardSide boardSide` field and a getter/setter pair, with
  `@EditableProperty(name = "Board Side")` on the getter. The getter returns `Top` when the field
  is `null`. This duplicates the field across classes in the same way the `PCBLayer` field is
  duplicated today. Where several of these classes share an abstract base
  (`AbstractRadialComponent`, `AbstractFilmCapacitor`, `AbstractTransistorPackage`, ...), put it on
  the base.
- `CopperTrace`, `CurvedTrace`, `GroundFill`, `SolderPad`, `PCBText` and `DrillHole` do **not**
  implement it. Their `PCBLayer` already says where they are.
- Before allowing `Bottom` on a class, check that its transformer can mirror it
  (`canMirror`). A class that cannot be mirrored must not offer `Bottom`, or the footprint would be
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

## 5. Verification

- Unit tests in `diylc-core` for `GerberLayer.flip()` and for the merged dispatch method, using a
  stub `IBoardSideComponent` (top, bottom and `null`).
- Load an old `.diy` with PCB parts and confirm every component reports `Top` and that the Gerber
  output is byte-identical to before.
- Export a board with one part on each side and open the files in a Gerber viewer: the bottom part
  appears only in `.gbo`, mirrored, and in the right position.
- Run the regression suite, since rendering and serialization both change.

## 6. Next steps

1. Merge the two `drawComponent` copies in `diylc-core` and add `GerberLayer.flip()` with tests.
2. Add `BoardSide` and `IBoardSideComponent`, and swap the Gerber layer in the dispatch.
3. Add the field to the library components, putting it on shared bases where they exist. Settle
   the silkscreen-text question while doing so.
4. Add the bottom-side composite in `DrawingManager`.
5. Add the Flip to Other Side action.
6. Split the silkscreen print-out by side.
7. Run the regression suite.
