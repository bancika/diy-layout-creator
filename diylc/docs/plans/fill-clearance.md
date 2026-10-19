# Ground Fill Clearance

Let a copper ground fill keep an isolation gap around traces and pads on its own layer, so a
board can be poured with ground without every trace shorting to it. The fill sets the gap; a
trace or pad can override it, including overriding it to zero so that it connects to the fill.

## 1. Behaviour

**Fill clearance.** `GroundFill` gains a **Clearance** property (`Size`). The fill draws its
polygon minus, for every clearance source on the same layer, that source's copper grown by the
effective clearance. A clearance of zero (or `null`, which is what old files deserialize to)
cuts nothing, so existing projects render and net exactly as before.

**Override.** Every clearance source gains a **Fill Clearance** property (`Size`):

| Value | Effect |
| --- | --- |
| blank (`null`, the default) | Use the clearance of whichever fill it overlaps |
| `0` | No gap: the source overlaps the fill and is electrically connected to it |
| any positive size | Use this gap instead of the fill's |

A ground trace is therefore "a trace with Fill Clearance 0". The override is a property of the
source, not of a source/fill pair; a trace overlapping two fills with different clearances
takes each fill's own value unless overridden.

**Layers.** A fill is cut only by sources whose copper is on the fill's layer
(`ILayeredComponent.getLayerId()` / `PCBLayer.getId()`). A bottom-layer trace passing under a
top-layer fill does not cut it. Sources that put copper on several layers report their shapes
per layer (see §2).

**Z-order does not matter.** Every source on the layer cuts the fill regardless of whether it
is drawn before or after it. "Send to back" never changes the copper.

**What does not cut a fill.** Other fills (two overlapping fills on one layer still merge, as
today), boards (`VeroBoard`, `PerfBoard`, `TriPadBoard`), `TraceCut`, and anything not on a
copper layer.

**Islands.** Clearance can split a fill into disjoint pieces. Each piece is a separate copper
area for continuity and netlist purposes. Islands are kept, not removed; an orphaned island is
visible and the user can fix it by moving the fill's points.

## 2. Architecture

```
diylc-core      IFillClearanceSource   (interface: copper shapes per layer + override)
                    ^ implemented by
diylc-library   CopperTrace, CurvedTrace, SolderPad, PCBText   (phase 1)
                footprint components with copper pads          (phase 2)
                    ^ queried by
                GroundFill.draw()  ->  FillClearanceCalculator  ->  cut Area, split into islands
                    |
                    +- netlist:  one positive continuity area per island
                    +- canvas:   fill() per island
                    +- Gerber:   same fill() calls through GerberG2DWrapper, dark polarity
```

The fill does all the cutting. Continuity analysis (`DrawingManager.getContinuityAreas`),
the drawing pipeline and `GerberExporter` stay unchanged, because the fill already *draws* its
final copper. Negative continuity areas are deliberately not used: a negative area subtracts
from every earlier positive area on the layer, so a trace's halo would sever traces and pads
it legitimately connects to.

### diylc-core

**`org.diylc.core.IFillClearanceSource`**, next to `ILayeredComponent`, Javadoc with
`@author`, in the shape of `IGerberComponentSimple`:

```java
public interface IFillClearanceSource {

  /** Copper this component places on the given PCB layer, in canvas coordinates; empty if none. */
  List<Shape> getCopperShapes(int layerId);

  /** Gap to keep from a ground fill; null inherits the fill's clearance, zero connects. */
  Size getFillClearance();
}
```

Per-layer shapes rather than a single shape plus `getLayerId()`, so that phase-2 footprint
components with pads on both sides fit the same interface.

### diylc-library

**`GroundFill`**
- New field `private Size clearance;` with getter `@EditableProperty(name = "Clearance")`,
  null-tolerant (treat `null` as zero). New fills default to `CLEARANCE`, a `public static`
  non-final constant (see §4, open question 1).
- `draw()` uses `project` to collect the `IFillClearanceSource` components, gets the cut area
  from `FillClearanceCalculator`, splits it with `AreaUtils.tryAreaBreakout` and `fill()`s
  each island inside the existing `startTrackingContinuityArea(true)` block. Each `fill()` with
  no continuity marker becomes its own continuity area in `G2DWrapper`.
- **Memoization.** The cut is expensive (`Area` operations over every source) and `draw()` runs
  on every repaint. Keep a `transient` cached `Area` plus a signature of its inputs: the fill's
  points, layer, clearance, and for each source on the layer its shapes' bounds and effective
  clearance. Recompute only when the signature changes. `enableCache` stays `false`, because the
  fill now depends on other components and `DrawingCache` keys only on the component itself.

**`FillClearanceCalculator`** (new, `org.diylc.components.connectivity`): pure geometry,
no Swing, unit-testable.
- Input: fill polygon, fill layer id, fill clearance, the sources.
- For each source: skip it if its effective clearance is zero or it has no shapes on the layer.
  Otherwise grow each shape by the clearance and union the result into a cut area.
- Return the fill area minus the cut area.
- Growing a shape: `shape ∪ roundStroke(2 × clearance).createStrokedShape(shape)`.
  `CopperTrace` and `CurvedTrace` can skip the union. Instead they report a centerline, and the
  calculator strokes it at `thickness + 2 × clearance`, which is exact and cheaper. Decide
  during implementation whether that special case is worth a second interface method.
  Otherwise use the generic path.

**Phase-1 sources.** Each one gets a nullable `Size fillClearance` field, a getter annotated
`@EditableProperty(name = "Fill Clearance")` that may return `null`, and `getCopperShapes`.
`draw()` and `getCopperShapes` must build their copper from the **same** private helper, so
the gap always matches the drawn copper.

| Component | Copper shape |
| --- | --- |
| `CopperTrace` | Stroked line, current thickness and `LineEndingStyle` cap |
| `CurvedTrace` | Stroked curve, same stroke as `draw()` |
| `SolderPad` | Pad outline per `Type` (round, oval H/V, square); the drill hole is ignored |
| `PCBText` | Glyph outlines (confirm it is copper and not silkscreen before including) |

`PCBTerminalBlock` tracks no continuity and is not layered today, so it waits for phase 2.

## 3. File format

- New fields only, nothing renamed. `GroundFill.clearance == null` behaves as zero, and
  `fillClearance == null` means "inherit". Both are safe for every existing file.
- With zero clearance an old file must produce identical canvas, netlist and Gerber output.
  Run the regression suite after implementation to confirm this. Expect zero diffs.

## 4. Open questions

1. **Default clearance on new fills.** Zero means a new fill behaves like today and clearance
   is opt-in. A non-zero default (e.g. 0.5 mm) is more useful, but a fill dropped onto an
   existing layout silently isolates every trace not set to `0`. Leaning towards zero.
2. **Phase-2 scope.** About 40 library components output copper pads to Gerber (`DIL_IC`,
   `SIL_IC`, `Resistor`, `AbstractRadialComponent`, `AbstractTransistorPackage`,
   `AbstractPotentiometer`, jacks, switches, …). Until they implement the interface, their
   pads short to a fill they overlap. Implementing it in the abstract bases covers most of
   them.
3. **Thermal relief** (spoked joins between a zero-clearance pad and the fill) is out of
   scope. If added later, it belongs in `FillClearanceCalculator` as a pattern of spokes
   added back after the cut.

## 5. Next steps

1. Add `IFillClearanceSource` to `diylc-core`.
2. Add `FillClearanceCalculator` with unit tests in `diylc-library/src/test`. Cover: layer
   filtering, inheritance vs. override vs. zero, a trace that crosses the fill edge, a cut that
   splits the fill into islands, and an empty source list returning the original polygon.
3. `GroundFill`: the `Clearance` property, the cut and islands in `draw()`, and memoization.
4. Implement the interface on `CopperTrace`, `CurvedTrace` and `SolderPad`, refactoring their
   `draw()` to share the shape helper. Then `PCBText` if question in §2 resolves to copper.
5. Check by hand: highlighting continuity on a fill with gaps, a netlist with a zero-clearance
   ground trace, dragging a trace across a large fill (check that repaints stay responsive),
   and a two-layer board with fills on both sides.
6. Check that Gerber export of a fill with holes produces correct regions with clear polarity
   inside them (`GerberPathRenderer.assignNegativeFlags`). Open the output in a Gerber viewer.
7. Build and test: `mvn -q -pl diylc-library -am test`, `mvn -q -pl diylc-swing -am compile`.
   Then run the regression suite.
8. Phase 2: footprint pads through the abstract component bases.
