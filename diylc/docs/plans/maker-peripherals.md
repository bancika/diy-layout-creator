# Maker Peripherals

Releasing the staged `Displays & Outputs` components, and closing the gaps that would stop
someone drawing a common display.

Status: proposed
Scope: `diylc-library`, package `org.diylc.components.displays`
Companion to: `maker-board-roadmap.md` (which covers `org.diylc.components.micro` only)

## 1. Goal

The first maker slice shipped ten controller boards in 6.5.0. Forty-one further components sit in
the tree with theiryou  `@ComponentDescriptor` annotations commented out, across four packages:
`displays` (7), `sensors` (9), `modules` (21) and `robotics` (4). None of them is discoverable, and
`MakerComponentsTest` lists all forty-one in `unreleasedMakerComponentClasses`.

This document plans the release of the **seven display components**, the fixes they need first, and
the additions that make the category cover the market rather than sample it. Sensors, modules and
robotics are deliberately deferred; §10 records why.

## 2. Decisions that govern this slice

| # | Decision | Choice | Rationale |
|---|---|---|---|
| D1 | Which package ships second | **`displays` only.** | Displays are archetype-complete in a way sensors are not: the hobby world has roughly seven display archetypes and the drafts already cover all seven, whereas the nine drafted sensors are a sample of hundreds with no canonical members. Displays also reach the *existing* guitar and amp audience — 7-segment counters, character LCDs and addressable strips appear in builds with no microcontroller anywhere. |
| D2 | How coverage grows inside an archetype | **A `Version`-style enum on the existing class where the footprint family is shared; a new class where it is not.** | Same rule as the board roadmap's D2. `OLEDDisplay` is now the only class with no variant enum at all, which makes it the largest structural gap left in the package; `LEDMatrix` gained its `Modules` enum in §6.4 and `TFTDisplay` completed its `Controller` enum in §6.2. |
| D3 | Where dimension and pin data live | **Hardcoded `Size` constants and `String[]` arrays in the component class,** per-variant where a variant changes them. | Matches every shipped board. No pin-definition resource is introduced. |
| D4 | Screen content | **No faked user interface — but the glass prints the part's own spec.** Amended by §6.8. | The original ruling was a neutral dark panel with at most a part-number silk, because four drafts rendered English demo text ("HELLO WORLD! 16x2", "OLED DISPLAY") at fixed font sizes, which does not scale with the board, bakes a language into a drawing and dates quickly. None of that reasoning is withdrawn and the demo text stays out. What changed is the problem: once a class carries six variants a dark rectangle no longer says which one it is, and all three places that do say — the property editor, the BOM and the Explorer pane — are off the canvas. §6.8 puts the variant string itself on the lit area; it is generated from the properties rather than typed, carries no prose and no language, and `Display.NONE` restores this row's dark glass for anyone who wants it. |
| D5 | Shared LED rendering | **One helper in `MakerBoardPainter`,** used by every addressable-RGB part. | `WS2812BRing` and `WS2812BStick` already contain two copies of the 5050-package-plus-diffuser-plus-IC-dot drawing, one scaled and one in raw pixels. A strip and a panel would make four. |
| D6 | Backward compatibility | Nothing here is a compatibility risk. | These classes have never been discoverable, so no `.diy` file references them. Field names, control-point counts and defaults can still be changed freely — **this slice is the last chance to do so.** |
| D7 | Geometry expressed in `Size` | **Every dimension derives from a `Size` constant.** | The drafts place bezels, screens and mounting holes with raw pixel offsets (`boardX = x - 60`, `bezelY = boardY + 45`), so nothing moves if a board dimension is corrected. The shipped boards do not do this. |

### Explicitly out of scope

- Sensors, modules and robotics (§10).
- The `AbstractMotor` / `AbstractMakerBoard` duplication, which only `robotics` suffers from.
- The board roadmap's open decisions on BOM variant treatment and a uniform `Headers` property set
  (§11 restates them, because this slice adds variants that make them more pressing).
- Bumping the project version in the POMs.

## 3. Current coverage

Eleven classes now, all `category = "Displays & Outputs"` and all `enableCache = true`. Seven are
the drafts this slice set out to release; `WS2812BStrip` (§6.1), `LEDBarGraph` (§6.6),
`WS2812BJewel` (§6.7) and `Nokia5110LCD` (§6.5) are §6's additions so far.

Two of the generalisations that held when this section was written no longer do, and both are worth
keeping visible rather than quietly dropping. **Not every class extends `AbstractMakerBoard`**:
`LEDBarGraph` is a DIP-outline part rather than a board, so it follows `DIL_IC` on
`AbstractLabeledComponent` and carries its own transformer, inheriting none of the maker helpers.
**All eleven do set `bomPolicy = SHOW_ONLY_TYPE_NAME`**, but the bar graph only does so because it was
caught: it had inherited `DIL_IC`'s silence, and the annotation default is `SHOW_ALL_NAMES`, so
saying nothing had quietly opted it out of the category's convention. It also had to override
`getValueForDisplay` by hand to get its segment count into the value column, because `BomMaker`
keys rows on the type name and value together and `getVariantLabel` is an `AbstractMakerBoard`
hook. Both are recorded in §6.6.

The category is still the right home, because that is where someone looks for a bar graph. But
"all of them" statements about this package now need checking rather than assuming — this one
happened to come back true, and only after a fix.

| Class | Variants today | Assessment |
|---|---|---|
| `SevenSegmentDisplay` | 1-digit 10-pin, 4-digit 12-pin, TM1637 module | **Best-*written* file in the set**, which is not the same as the best-sourced one. The segment-mask table is genuinely good, the 10-pin structure is corroborated, and the glyph itself is now the standard unit-grid construction: an upright 10 × 18 box with segments two units thick, mitred at 45 degrees so neighbours meet at a shared vertex, each outlined in the face colour so that they still read as separate segments. That replaced a set of hand-tuned polygons and retired both `SEGMENT_GAP` (the construction separates by outline rather than by gap) and `SLANT_DEGREES` (it is drawn upright, as the source is). What remains unchecked is everything *around* the glyph. The per-package dimensions now live on the `DisplayType` constants rather than in scattered branches, and the enum has been split so that the two bare four-digit packages — 0.36" and 0.56" — are separate variants sharing one pin array. The 0.56" set is what this class has always carried and remains unverified; the 0.36" set is now largely maintainer-supplied — body 30 × 14 mm, 7.5 mm digit pitch, 0.4" row spacing — leaving only its digit width inferred. See §11.10 and §11.11. |
| `WS2812BRing` | 12 / 16 / 24 LED | **Good.** Genuine polar maths, mm-based OD/ID per size, LED count driving the rendering, `drawSolderPads` rather than a header — correct for a ring. Its diameters, pad count and pad placement have all since been corrected from Adafruit's pages and the maintainer's measurements; the pads now sit on their own radius near the rim, each in one of the real uneven gaps between LEDs (§11.9). The LED packages are the real 5 mm 5050 part, shared with the Stick as `AbstractMakerBoard.RGB_LED_SIZE` — they had been drawn at a clamped 8-15 px, barely 1-2 mm — and the ring renders lit, in a continuous yellow-orange-red-purple-blue-green gradient with a palette per variant. |
| `CharacterLCD` | 16x2 / 20x4, I2C backpack / 16-pin parallel | Correct HD44780 and PCF8574 pin data. The bodies are now maintainer-given at 80 × 35 and 98 × 60 mm — the 16x2 height had been 36, so "correct bodies" was never true of it. The screen geometry was raw pixels — an eyeballed `bezelMarginX = (16x2) ? 45 : 40` and a window derived as a margin off the board — and is now measured: a 72.2 × 24.1 mm bezel around a 64.5 × 14.5 mm lit area on the 16x2, 77 × 25.5 around 70.4 × 20.4 on the 20x4, carried on `LCDSize` and centred rather than subtracted. Headers are placed from measurements too (§3.2 item 7). No pixel literal survives in `draw()`; only the mounting holes remain unsourced (§11.8). The `Font` it once allocated inside `draw()` went with the demo text in item 7; the one that remains is in `drawIcon`, which runs per toolbox icon rather than per repaint. |
| `OLEDDisplay` | 0.96" SSD1306 / 0.91" SSD1306 / 1.3" SH1106, each I2C 4-pin or SPI 7-pin | Correct pin names, and the body is the measured 26.7 × 19.3 mm panel rather than a 27 mm square. The glass had been drawn near-square against what is really a 2:1 letterbox; the lit area is 21.744 × 10.864 mm, which is exactly 2:1. The gap this row used to record — no size variants — closed in §6.3, and closing it overturned that item's premise: the interface is **not** orthogonal to the size, because the 0.91" is sold as two different boards rather than one board with two headers, and it is the first display board in the package with its header on the left edge. |
| `LEDMatrix` | Single 8x8 / Compact 8x8 / 4-in-1 32x8 | Plausible single MAX7219 module with correct cascade headers, but a hardcoded `-44 mm` output-header offset and pixel-placed matrix and chip. Both are gone: the modules are the real 32 × 32 mm part drawn flush with the board, and the header rows sit one clearance in from their own edges with the spacing derived from the board rather than fitted. Whichever headers the modules cover are drawn before them and painted over, as on the real part, so the display face stays clean while the pins stay wireable (§3.2 item 9). §6.4 added the other two: the compact 8x8, which fits its driver under the module so the board is only the module (32 × 32 mm) and several can be butted together, and the 4-in-1 (128 × 32 mm, four flush modules), both standing their headers vertical on the short edges with every driver hidden. Carrying boards other than an 8x8 is what retired the `8x8` in the old class name. |
| `TFTDisplay` | ILI9341 2.8" / ST7735 1.8" / ST7789 1.54" / GC9A01 1.28" round | Was the weakest file, on two counts that are now both gone: a footprint bug, and a descriptor reading 240x320 against on-screen silk reading `320x240`. The footprint was rebuilt from the module drawing (§5 item 3) and no `320x240` string survives anywhere in the class. The gap it always had -- no variant enum -- is now closed: §6.2's `Controller` enum carries all four, the ILI9341, the ST7735, the 1.54" ST7789 and the round GC9A01. Board size, pin array, header offset, hole pattern and screen all come off the enum, and the round variant's body is a disc unioned with its tab. All four mounting holes on the 2.8" and the 1.8" are 3 mm across and 3 mm in from each of the two edges nearest them; the header-side pair was carried at a deeper inset for a while on the belief that it had to clear the pin row, which it does not. The 1.54" carries its own 2 mm drill at a 2.5 mm inset, so the figures are each board's rather than the package family's. It is also the first of these boards to print its pin names, which all four variants now do (§6.2). |
| `LEDBarGraph` | 8 / 10 / 12 segment | **New in this slice (§6.6), not a draft**, and the only class here that is not an `AbstractMakerBoard` — it is a DIP-outline part, so it follows `DIL_IC` on `AbstractLabeledComponent` with a sibling transformer. The package follows from the segment count rather than being fixed: anode and cathode per segment gives 16 / 20 / 24 pins, and the body lengthens one pin pitch per segment (20.32 / 25.4 / 30.48 mm) at a constant 10.3 mm width, with rows 7.62 mm apart. Unlike a DIP IC the body spans the rows instead of sitting between them, because the pins leave the underside rather than the sides, so the plastic covers them entirely — they are drawn first and painted over, never skipped, or they would drop out of the conductive areas. Each segment is centred on the pin pair that drives it, taken from the two control points rather than by dividing the body, which is also what makes it orientation-proof. Colour is a rule rather than a property — one red at the top, two yellow, green for the rest — so the green band grows with the count; the cost is that a single-colour bar can no longer be drawn. Pins are round and drawn over the package: hidden on the real part, but a pin that cannot be seen cannot be positioned against a board. Covered by `LEDBarGraphTest`, whose `bodyCoversEveryPin` replaces an earlier assertion that encoded the copied DIP arrangement and therefore passed while the component was wrong, and whose `segmentsAreCentredOnTheirPins` catches a placement that was inside the body and still off its pins. |
| `WS2812BStrip` | 30 / 60 / 144 LED/m, 1-144 LEDs | **New in this slice (§6.1), not a draft** — so unlike its neighbours this row describes a part rather than a rehabilitation. Everything follows from the density, which is how tape is sold: pitch is 1000 mm over the count (33.33 / 16.67 / 6.94 mm), length is linear in the LED count, and four control points hold at every density and count because the LEDs are drawn rather than wired. Two figures are not measured: the tape width (10 mm for the 30 and 60, 12 mm for the 144) is the plan's stated norm, and the lead-in that keeps the end pads clear of the first and last package is derived from the pad and package footprints rather than taken from a real tape. Covered by `WS2812BStripTest`, whose pad-clearance assertions exist because that geometry was got wrong three times before it was measured. |
| `WS2812BJewel` | none (a single part) | **New in this slice (§6.7), not a draft.** A sibling of `WS2812BRing` rather than another `RingSize` constant, per D2: it shares the part family and the pad footprint but not the footprint family. It is a solid 23 mm disc with no inner rim, six LEDs on a 16 mm circle around a seventh in the middle, five pads on a 9 mm circle *inside* the LED ring rather than inset from the rim, and two 3 mm mounting holes 19 mm apart that no ring carries — five variant-specific structures, which is a different part and not a variant. Every figure is maintainer-supplied. The pad arrangement is the part that could silently have been wrong: five pads share six gaps between the ring LEDs, so one gap carries none, and it works out to the gap pointing due left, which is where a mounting hole sits. `WS2812BJewelTest` pins that down, because every count and radius assertion passes whichever gap is left empty. Its packages — and the rings', which this changed too — are drawn turned to follow their own radius the way they are mounted; the centre package has no radius and stays square to the board. |
| `Nokia5110LCD` | none (a single part) | **New in this slice (§6.5), not a draft.** Every figure is maintainer-supplied and the whole outline is measured: a 43.8 x 45.8 mm board, a 40 x 35 mm bezel 4.5 mm down from the top edge, a 37 x 27 mm glass 11 mm down, and 3 mm mounting holes on a 34.5 x 41 mm pattern. It is the first display board whose header is on the **bottom** edge, so the board grows upwards from control point zero rather than down from it, and every feature is measured from an edge no control point touches. The glass is placed from the top edge rather than centred in the bezel, because it is not centred in it -- 6.5 mm of frame above, 1.5 mm below. It is also the first in the package to carry silkscreen: the pin names lie flat along the row under the header rather than standing on end like the Arduino boards', and the part name sits in the top left corner beside the mounting hole, where the module prints it (§6.5). One figure is unsourced: the 3.5 mm clearance from the bottom edge to the pin row. |
| `WS2812BStick` | none | Hardcoded to 8 LEDs, with every LED dimension in raw pixels (28 × 28 package, 68 px insets), so the LEDs do not scale with the board. See the node-name bug below. All since corrected: a `Size`-based 51.1 × 10.22 mm board, real 5 mm 5050 packages off the shared `RGB_LED_SIZE`, pads reordered and renamed for uniqueness, and the LED row respread between the pad columns and drawn lit from the shared colour wheel (§11.6). |

### 3.1 Two defects that are bugs, not style

**`TFTDisplay` header edge.** The 14-pin header is laid out along the 86 mm edge, but on the real
2.8" ILI9341 module that header runs along the short 50 mm edge — 14 pins on 0.1" pitch is 33 mm,
which fits in 50 mm and not in 86 mm. The module is portrait and the draft draws it landscape. The
board outline, the header edge and the screen aspect all need re-deriving together from the module
drawing.

**`WS2812BStick` duplicate node names.** `PIN_NAMES` declares `GND (In)` twice and `GND (Out)`
twice, so each end of the board carries two identically named nodes. This is the same duplicate-node
problem that was just fixed for building-block terminals, and it will reach the netlist. The real
pad count per end has to come from the product drawing; the draft's four-per-end with a repeated
ground is almost certainly wrong.

### 3.2 Cross-cutting items

1. **`zOrder` splits the package.** `CharacterLCD` and `TFTDisplay` declare `IDIYComponent.BOARD`;
   the other five declare `COMPONENT`. A display sits on top of a board rather than being one, so
   `COMPONENT` is right for all seven. Same class of oversight as the board roadmap's §8.1 Teensy
   item.
2. **No file uses the shared silk machinery.** `drawPinLabels` / `getSilkPinLabel` exist on
   `AbstractMakerBoard` and four shipped boards use them; here `WS2812BStick` and `WS2812BRing`
   hand-roll label arrays that duplicate their own `PIN_NAMES` and will drift from them.

   **Resolved, and then taken further: a header prints its pin names wherever they fit on bare
   board.** `Nokia5110LCD` was first, `TFTDisplay` followed in §6.2, and `CharacterLCD`'s parallel
   row and the single `LEDMatrix`'s input row were swept in afterwards. They all go through
   `getSilkPinLabel`, so a name is written once and the silk follows it.

   Two classes needed a `SILK_NAMES` array of their own, in each case because the default -- the
   node name with its annotation stripped -- is actively wrong rather than merely long. The 2.8"
   TFT's five touch pins are prefixed `T_` and would all have printed as `T`; the Character LCD's
   parallel row carries its functions in parentheses, so `V0 (Contrast)` would have been printed
   whole across its neighbours. What those arrays hold is what the module prints, which is also
   what fits.

   **Where the names do not fit, they are left off rather than squeezed in**, which was decided
   twice over: once by argument and once by rendering the attempt and undoing it.

   - The **I2C backpack** on the Character LCD stands its four pins in a column 2.5 mm from the
     left edge with the bezel 1.4 mm further in, so a label beside them would lie on the metal
     frame. The backpack prints its names on its own PCB behind the module, which this drawing
     does not show.
   - The **LED matrix's output row**, and both rows of the compact and the 4-in-1, sit under the
     modules -- the same fact that dropped the `IN`/`OUT` labels in item 9.
   - The **OLED** was labelled and undone. Only 2.05 mm separates its pin row from the panel and a
     flat label needs about 2.7 mm, so the names crossed the panel's top edge: legible, but with
     the boundary cutting through the glyphs. Nothing short of shrinking the shared label offset
     fixes it, and dropping that from 2.04 mm to 1.4 mm would bring every other board's labels
     within 0.2 mm of their pin blocks -- a visible defect traded for a subtler one.
   - The **seven-segment display** was labelled and undone too. Its bare packages are moulded
     plastic with no printing of any kind on them, so naming their pins would be drawing something
     the part does not have, and the TM1637 module's column had to read inward, where its widest
     name grazed the bezel. `drawFlatColumnPinLabels`, the column counterpart of the row helper
     written for that module, went with it.

   One rendering trap is worth recording, because it is invisible in the geometry and obvious on
   screen. `StringUtils.drawCenteredText` with `VerticalAlignment.CENTER` centres each string on
   *its own* glyph box, so a name with a descender sits higher than the names beside it: `IRQ`,
   the only such label in the package, rode a pixel above the other thirteen on the 2.8" TFT. The
   flat helpers now measure one reference glyph and pass that baseline through, which levels the
   row and leaves every label without a descender on exactly the pixel it was on -- checked across
   all 31 labels in the package, and only `IRQ` moved.

7. **Connectors are not centred on their edge.** In `CharacterLCD` and `TFTDisplay` the header
   starts at the control-point origin while the board is placed by a raw pixel offset (−60 and −70
   respectively), so the connector sits in a corner instead of centred where the product has it.
   `OLEDDisplay` was wrongly included here at first: its `boardX` formula does centre the pin row,
   and only its vertical offset was a raw pixel value. This is the visible face of D7, and it
   resolves when the geometry is re-derived from `Size` constants in item 6.

   **`CharacterLCD` has since been given its real header placement, and it is not centred at all.**
   The 16-pin parallel row starts 10 mm in from the left edge, and the I2C backpack brings its four
   pins out as a *vertical* column standing against the left edge, 2.5 mm in and centred on the
   board's height. For this class the item was therefore wrong twice over: the geometry was not
   merely off-centre by accident, and centring was never the right answer. `PARALLEL_HEADER_INSET`
   and `I2C_HEADER_INSET` are measurements of where the connectors actually are, not a correction
   applied to a drawing that had drifted. `TFTDisplay`'s centred header still stands as described
   above, so the item holds for it.

8. **The Ring's pad arc and LED arc coincide** — **resolved, with §11.9.** Both the solder pads and
   the LEDs were placed on the mid-radius circle, so the four pads sat on top of LEDs at the bottom
   of the ring. Nothing was hidden — `drawSolderPads` runs last, after the transform is restored, so
   the pads drew over the LEDs — but on the real board the pads occupy a gap in the LED sequence
   rather than sharing it. They now sit on their own radius near the outer edge, each in a named gap
   between two consecutive LEDs; §11.9 carries the sequences.

9. **`LEDMatrix`'s `OUT` label sat inside the matrix block** — **fixed by moving it outboard.**
   The output header row is 3.46 mm below the top edge and the matrix starts at 3.8 mm, so the
   label's +2.0 mm offset put its centre at 5.46 mm, which is 1.66 mm inside the matrix. This
   predated the `Size`-constant rewrite — the previous raw offset put it 0.5 mm inside the same
   block — so the rewrite preserved the overlap rather than causing it.

   The offset is now negative, placing the label at 1.46 mm, in the strip between the row and the
   top edge. The two labels therefore sit on opposite sides of their rows, which the code comment
   explains so it does not read as a slip: the `IN` row has the chip above and the board edge
   below, so its label goes inboard, while the `OUT` row has the matrix 0.34 mm beneath it and only
   the top edge free.

   One claim recorded here before was wrong and is worth correcting rather than quietly dropping.
   It said placing the label above the row "runs it into the board edge". It does not:
   `SILK_FONT_SMALL` is 10 px, about 1.27 mm, so a centre at 1.46 mm spans roughly 0.8-2.1 mm and
   stays on the board. The real constraint is tighter and different — it clears the pin field by
   about 0.2 mm, so it reads as close to the pins rather than as falling off the board.

   What this does **not** fix is the underlying overlap: the pin field spans 2.32-4.60 mm and the
   matrix starts at 3.8 mm, so the pins themselves still bite about 0.8 mm into the matrix block.
   Correcting that means moving the matrix, and `MATRIX_TOP_MARGIN` would become a fitted number
   with no source behind it — the kind of value this slice has been removing everywhere else. It
   waits for the real module's layout.

   **Superseded almost immediately, by that layout arriving.** The LED module is 32 × 32 mm and
   sits flush with the top edge, so it spans the board's full width and its top 32 mm. That
   dissolves the label problem rather than solving it: there is no strip above the module for `OUT`
   to occupy at all, so both labels were dropped and the names left to the node tooltips and the
   netlist, as on the Ring and the Stick. `MATRIX_TOP_MARGIN` and `SILK_OFFSET` went with them.

   **Modelling the bent pins was tried and reverted, and the attempt is worth recording.** Both
   headers are right-angle parts whose pins run 7.5 mm from the pad centre, so the control points
   were moved out to the pin tips, 4.96 mm beyond each edge, with the row spacing derived as the
   board plus one overhang at each end. It was accurate and it looked wrong: the tip rows stood
   clear of the board with nothing drawn between them and it, so the pins read as floating rather
   than as connectors reaching out. Accuracy about where a pin *ends* was not worth a drawing that
   misleads about what the part *is*.

   The pins are modelled straight again, on the board face, each row one clearance in from its own
   edge — which keeps the one gain from the attempt, a row spacing derived from the board
   (44.92 mm) rather than the fitted 44 mm it replaced.

   **That leaves the output header under the module, which is where it sits on the real board.** It
   is drawn *before* the module, so the module covers it and the display face stays clean; the pins
   remain selectable and wireable as control points. Drawing it and covering it is deliberate
   rather than skipping the call: `drawPinHeader` is what registers those pins as a conductive
   area, so omitting it would render identically and quietly drop five pins from continuity.

10. **Every one of the Ring's six diameters was wrong.** The enum carried 37.0/27.0, 44.5/32.0 and
    66.0/52.0 for the 12, 16 and 24 LED rings. The outer diameters were near-misses but the inner
    ones were not — the 12-LED ring was out by 3.7 mm on a 36.8 mm part. That is not cosmetic:
    `getMidRadius()` averages the two and every LED sits on that radius, so the 12-ring's LED circle
    sat about a millimetre out. The enum now carries exact conversions of the maintainer's measured
    figures — 1.45"/0.92", 1.75"/1.25" and 2.58"/2.06" — which corroborate Adafruit's published
    36.8/23.3, 44.5/31.7 and 65.5/52.3 to within a rounding. This was the first substantive error
    item 10 turned up, and it is the argument for finishing the sweep rather than trusting the
    remaining drafts.

    The LED circle was measured too — 1.16", 1.5" and 2.3" — and deliberately **not** stored. The
    mid-radius reproduces the 16-LED circle exactly and misses the other two by 0.64 mm and
    0.51 mm, judged not worth a third diameter per ring. The measured values are recorded in the
    enum's comment so that the derivation reads as a decision rather than an oversight.

Items 6 through 9 were found by rendering the components, not by reading them; item 10 was found by
checking a vendor page. Since no regression
baseline exists, an offscreen render harness — instantiate by class name, draw into a
`BufferedImage` sized from `getCachingBounds()`, write a PNG per component — is the cheapest way to
see this class of defect, and it works whether or not the descriptors are enabled. Note that moving
such a component requires shifting **every** control point: `setControlPoint(p, 0)` moves only that
point and does not re-run `updateControlPoints()`, so the body follows point 0 while the remaining
pins stay behind.

## 4. Shared mechanics

The board roadmap's §4 applies unchanged and is not repeated: component discovery is a classpath
annotation scan with no registry to edit, the `Version` enum pattern and its null-guard, XStream
serialising enum constants by name, and the sourcing rules in §4.5. What differs for this slice:

**Test support has to be promoted.** `MakerBoardTestSupport` is package-private in
`org.diylc.components.micro`, so a `displays` test package cannot see its `assertRow`,
`assertRowSpacing`, `assertBoardSize` and `assertSharedFootprint` helpers or its shared no-op
`IDrawingObserver`. Move it to `org.diylc.components.maker` beside `MakerComponentsTest` and make it
public, updating the ten `micro` tests' imports. Do this first; everything in §7 depends on it.

**One new painter helper.** `MakerBoardPainter.drawAddressableLed(g2d, cx, cy, size)` drawing the
5050 white package, the milky diffuser and the internal IC dot, all derived from `size`. Port
`WS2812BRing` onto it (its version is already correct, so its rendering must not change), then
`WS2812BStick`, before any new addressable part is written.

**Release mechanics.** Uncommenting a `@ComponentDescriptor` is what ships a component. Each release
also needs the class moved from `unreleasedMakerComponentClasses` to `releasedMakerComponentClasses`
in `MakerComponentsTest`, so that the discovery and icon assertions start covering it.

## 5. Prep pass on the existing seven

Ordered by risk, not by file size. None of these is a compatibility concern (D6).

| # | Work | Files | Rough effort |
|---|---|---|---|
| 1 | Promote `MakerBoardTestSupport` to `org.diylc.components.maker`, make it public, fix the ten `micro` imports — **done** | 11 test files | 1 h |
| 2 | Add `MakerBoardPainter.drawAddressableLed`; port `Ring` (no visual change) then `Stick` — **done** | 3 | 2-3 h |
| 3 | Re-derive the `TFTDisplay` footprint from the module drawing: outline, header edge, screen aspect, SD-slot position (§3.1) — **done**: portrait 50 × 86 mm, header centred on the short edge, screen derived from the glass, four holes placed from the vendor drawing and the glass centred between them. The front-face SD slot was removed rather than moved, because the reader is on the back | 1 | 3-4 h |
| 4 | Fix `WS2812BStick` pad names and count from the product drawing; rebuild its geometry from `Size` constants (§3.1) — **done**, with one correction to the premise: the **count was already right**. Four pads per end is what the board has, and the doubled ground is real hardware on one net. Only the *order* was wrong, so the pads were reordered to GND, data, power, GND and renamed `GND_1`…`GND_4` for uniqueness. Board corrected to 51.1 × 10.22 mm with real 5 mm packages | 1 | 2-3 h |
| 5 | Fix the `OLEDDisplay` glass aspect to the real active area — **done**: panel 26.7 × 19.3 mm centred between the hole rows, with the 21.744 × 10.864 mm lit area drawn inside it | 1 | 1 h |
| 6 | Replace raw pixel geometry with `Size` constants (D7) — **done for all five.** `CharacterLCD` was the last holdout and was unblocked by maintainer figures for its bezel, lit area and header placement; its window is now the part's own size rather than a margin off the board, and no pixel literal is left in `draw()`. One caveat worth keeping separate: its mounting-hole inset and diameter are *named* constants but still the draft's guessed 2.54 mm, so D7 is satisfied while §11.8's sourcing question is not | `CharacterLCD`, `OLEDDisplay`, `TFTDisplay`, `LEDMatrix`, `WS2812BStick` | 4-6 h |
| 7 | Replace demo screen text with a neutral panel (D4) — **done**: all three now draw an unpowered panel, and `CharacterLCD`'s `SCREEN_TEXT` colour went with its lettering | `CharacterLCD`, `OLEDDisplay`, `TFTDisplay` | 1-2 h |
| 8 | Route silk labels through `getSilkPinLabel` / `drawPinLabels` — **done**: the Stick's now come from `getSilkPinLabel` and the Ring's are dropped; removing the Ring's inline `new Font(...)` also settles half of item 9 | `WS2812BStick`, `WS2812BRing` | 1-2 h |
| 9 | `zOrder` to `COMPONENT` on `CharacterLCD` and `TFTDisplay`; cache the two `draw()` fonts; strip non-ASCII; drop the unused import — **done**: all seven are `COMPONENT`, both `draw()` fonts left with items 7 and 8, `SevenSegmentDisplay`'s degree signs were the last non-ASCII, and the orphaned imports plus `CharacterLCD.SCREEN_TEXT` are gone. The three surviving `new Font(...)` calls are all in `drawIcon`, which runs per toolbox icon rather than per repaint, so they are deliberately left | 5 | 1 h |
| 10 | Cross-check every surviving dimension and pin array against vendor documentation — **done.** Closed: `TFTDisplay` (outline, active area, header edge, hole positions, glass placement), `WS2812BStick` (board, 5050 package, pad count and order), `OLEDDisplay` (panel, lit area, and a 24 mm centre-to-centre hole pattern in both directions, modelled as spacing rather than an edge inset since that is how it is specified and what a builder drills), `WS2812BRing`'s diameters — **all six of which were wrong**, see §3.2 item 10 — and that same class's pad count, naming and arrangement (§11.9, maintainer-supplied, which also closed §3.2 item 8), `SevenSegmentDisplay`'s pin arrays and part identities (§11.10 — 5161AS, 3641AS and 5641AS, with the two four-digit parts confirmed to share a pinout), and `CharacterLCD`'s bodies, bezel, lit area, header placement and hole inset (§11.8). Closed by decision rather than by measurement: `SevenSegmentDisplay`'s digit widths, accepted as inferences because they size only the drawn glyph, which already departs from the package deliberately (§11.11). Also closed: `LEDMatrix`'s module size, its flush placement against the top edge, and its right-angle header geometry, all maintainer-supplied (§3.2 item 9) — which closed the silk question by removing the labels rather than placing them; and `CharacterLCD`'s mounting-hole diameter (§11.8), which was the last one. **Nothing remains open.** Two figures are closed as acknowledged inferences rather than measurements — `SevenSegmentDisplay`'s two digit widths (§11.11) — and are flagged as inferences in the source so they are not built upon | all 7 | the bulk of the slice |

Items 1 and 2 have landed. The five shared 5050-package colours now live on `AbstractMakerBoard` as
`RGB_LED_*`, and the duplicated copies are gone from both NeoPixel classes along with two dead
constants (`SOLDER_PAD_COLOR` / `SOLDER_PAD_BORDER`) that nothing referenced. `WS2812BRing` renders
identically, because the helper reproduces its geometry exactly; `WS2812BStick` changed very
slightly — corner radius, lens diameter and border stroke now match the ring, which is the intended
consequence of there being one renderer. The stick's LED size is still the literal 28 px it always
was, since deriving it from the real 5.0 mm package changes the drawing and belongs to item 4.

Items 3 and 4 have largely landed, against sourced figures. The TFT module is **50 × 86 mm
portrait** with a **43.2 × 57.6 mm** active area, and its fourteen pin names and their order already
matched the product, so only the geometry was wrong: the outline is now portrait, the header is
centred on the short top edge, the screen is derived from the glass rather than from pixel offsets,
and the front-face microSD block is gone because the reader is on the back. The NeoPixel Stick is
**51.1 × 10.22 mm** with **5.0 mm** packages, and its four-pads-per-end turned out to be correct —
the order was not. Each end reads GND, data, power, GND, and the doubled ground is real hardware on
one net, so the pads are now named `GND_1`…`GND_4` and the silkscreen prints "GND" for all four via
`getSilkPinLabel`. **The control-point count stays at 8.**

Both of the items these left open have since been settled from the vendor drawing and are recorded
in §11.5 and §11.6. The TFT carries **four** mounting holes, every one 3 mm across and 3 mm in from
each of the two edges nearest it. The header-side pair was carried at a deeper 6.92 mm inset for a while so that it
would clear the pin row; it sits well outside that row horizontally and never needed to, and the
inset is now uniform with the field that distinguished the two pairs removed. The holes are drawn
before the glass, which is placed to clear them, and `TFTDisplayTest` asserts that clearance. The Stick's
front face now carries no silkscreen at all, which is what the real board does — its pad names are
printed on the back — and with the labels gone the LED row takes its true layout, eight 5 mm
packages centred, with the inset derived rather than tuned.

The glass turned out to be centred between the two rows of mounting holes rather than measured from
an edge, which resolves the apparent conflict: its midpoint is 44.96 mm from the top, so a 69.1 mm
glass spans 10.41 mm to 79.51 mm and clears both pairs. The drawing derives it that way rather than
storing another offset constant, so it stays correct if a hole position is ever revised.

Item 10 is the real cost and cannot be shortcut. These classes were drafted without sources, so
**every number in them is an estimate until checked**, including the ones this document quotes as
correct. What works as a source: Adafruit product pages and their EagleCAD/Fritzing footprints for
the NeoPixel parts; the SSD1306, SH1106, ILI9341, ST7735, ST7789, PCD8544, MAX7219 and TM1637
datasheets for controllers; generic module drawings from the AliExpress or Waveshare listing for the
red SPI TFT and the I2C backpack, which have no canonical vendor.

## 6. Gap implementation plan

The archetypes are covered; the gaps are inside them. Each item below is an independent commit.

### 6.1 NeoPixel Strip — new class `WS2812BStrip` — **done**

Everything derives from the density, which is the one figure tape is actually sold by: the pitch is
1000 mm divided by the count (33.33, 16.67, 6.94 mm), the length is linear in the LED count, and
four control points hold at every density and count because the LEDs are drawn rather than wired.
The one figure not derived is the tape width, which is the plan's stated norm — 10 mm for the 30 and
60, 12 mm for the 144 — and remains unmeasured.

`LedCount` is the bounded integer §11.4 assumed. Note that the item's stated reason for a bound was
wrong: it worried about "an enormous control-point array", but the array is fixed at four whatever
the count. The bound exists because the *shape* grows — 144 LEDs of 30/m tape is nearly five metres.

**The output pad's position took four attempts, three of them wrong, and the way it went is the
point of recording it.** Each attempt reasoned from a formula and shipped; each time every numeric
check passed while the drawing was wrong. One edit was a net no-op that restored the original
expression while adding a comment claiming a symmetry the code did not implement. What settled it
was measuring instead of deriving: a probe showed the pad centre sitting exactly on the tape edge,
which exposed the cause in one line — offsets are measured from control point 0, which is itself one
inset inboard of the near cut, so reaching one inset in from the *far* cut costs two insets, not
one. The original expression had been right about the pad all along; the real defect was the
lead-in, and three edits went into fixing the wrong half of the geometry.

`WS2812BStripTest` encodes exactly those failures — `everyPadSitsOnTheTape` and
`outputPadClearsTheLastLed` would have caught every one — so the mistake cannot recur quietly. The
class was also added to `releasedMakerComponentClasses`: until then it was discoverable in the app
but asserted on by nothing, which is why the suite stayed green while the part was broken. That list
is hand-maintained and not exhaustive, so a new maker component is invisible to every test until
someone remembers to add it.

Silk was left off, following the Ring and the Stick. Worth revisiting: unlike those two, real tape
*does* print `+5V`, `DI` and `GND` between its pads, so this is the one case in the slice where
dropping silkscreen is a simplification rather than a fidelity gain.

The plan this was built from, kept for the record:

The most-used addressable form factor, absent entirely, and the one addition that also serves the
existing guitar and amp audience.

1. New class in `displays` extending `AbstractMakerBoard`, GPL header from `HEADER.txt`, descriptor
   `name = "NeoPixel Strip (WS2812B)"`, `zOrder = COMPONENT`, `enableCache = true`.
2. `Density` enum carrying LEDs per metre and the resulting pitch: `_30("30 LED/m")`,
   `_60("60 LED/m")`, `_144("144 LED/m")`. Strip width comes from the density (10 mm and 12 mm are
   the common tapes; 144/m is usually 12 mm).
3. `LedCount` as an `@EditableProperty` integer, defaulting to 8, with a sane upper bound so a
   careless value cannot allocate an enormous control-point array. Four control points regardless of
   LED count: `DIN`, `+5V`, `GND` at one end and `DOUT` at the other — the LEDs are drawn, not
   wired, exactly as the ring does it.
4. `getBodyShape()` is a rounded rectangle whose length is `ledCount * pitch`, so the part grows
   with the property.
5. `draw()` fills the tape, then loops `drawAddressableLed` at the pitch, then `drawSolderPads` at
   each end. Silk via `getSilkPinLabel`.
6. `drawIcon()` draws a short horizontal tape with four coloured dots, in the style the `Stick`
   icon already uses.

Tests: `assertBoardSize` at two densities, control-point count fixed at 4 across LED counts, a
length-scales-with-count assertion, draw smoke at both densities. **~1 day including sourcing.**

### 6.2 `TFTDisplay` variants — `Controller` enum — **done**

The biggest structural gap, and it lands on top of the §5 item 3 footprint fix, so do it in the same
pass rather than twice.

**Progress.** The `Controller` enum has landed with three of the four. Beside the existing
`ILI9341_2_8` there is now a
new `ST7735_1_8` (34 x 45.8 mm, eight pins `GND VCC SCK SDA RES DC CS BL`, header 1.5 mm in from the
top edge, four 3 mm holes 3 mm in from each edge). Board size, pin array, header offset, hole pattern
and screen now all come off the enum; `updateControlPoints` sizes the array from the variant's pin
list, and `getVariantLabel` feeds the controller into the BOM value so the variants no longer collapse
into one row. The descriptor was renamed from "TFT Touch Screen (ILI9341)" to "TFT Display", which the
class can still afford because it has not shipped (D6). `TFTDisplayTest` covers it in 11 tests, and
the default variant's render is byte-identical to the pre-refactor one.

Item 5 above is **stale and dropped**: `draw()` has no SD slot or touch hardware to branch on. The
slot was removed in §5 item 3 because the reader is on the back, and touch was only ever pins. The
only per-variant difference left is the pin array, which item 2 covers -- which also makes the
1.5-2 day estimate too high. The round outline is the one genuinely new piece of drawing remaining.

**`ST7789_1_54` has landed, and the contradiction turned out to be in what the figures described
rather than in the figures themselves.** The outline supplied first, 27.78 x 39.22 mm, cannot be a
PCB: a 1.54" 240x240 panel is square, so its active area is 39.116 / sqrt(2) = 27.66 mm, which
would leave 0.06 mm of bezel across the entire width. Neither guess recorded here was right. The
pair is self-consistent as two measurements of the *same screen* -- 27.78 x sqrt(2) is 39.29, and
the ratio sits within 0.17% of sqrt(2) -- where the two rectangular boards already in the enum have
outline ratios of 1.35 and 1.72. A square panel's side and its diagonal, in other words, and no
correction to either number recovers a board from them.

The maintainer's outline is **32 x 43.72 mm**, with a **33.7 mm** dark panel as wide as the board,
the header 1.5 mm in from the top edge, four 2 mm mounting holes 2.5 mm in from each of the two
edges nearest them, and eight pins `GND VCC SCL SDA RES DC CS BLK`. The active area is the usual
derivation, 27.66 mm square, flagged as such beside the others.

The panel was first given as 37.7 mm and corrected to 33.7, and it is worth recording that a test
caught that rather than the eye. `screenClearsTheMountingHoles` already asserted that the panel
clears both hole rows, and four 2 mm holes at a 2.5 mm inset leave only 36.72 mm between them, so
37.7 mm failed it by 0.49 mm at each end -- an overlap far too small to notice in a render. At
33.7 mm the panel clears each pair by 1.51 mm. That assertion was written because the 1.8" board's
panel very nearly reaches its own holes; it has now paid for itself on a board added two variants
later. A second assertion was added beside it for the constraint this variant introduced: with the
row only 1.5 mm in from the edge and the panel placed between the hole rows rather than from an
edge, a long panel can reach back over its own header, which `panelStartsBelowThePinRow` now
forbids.

**All four variants print their pin names**, flat along the row in the strip between the header and
the panel, the way the Nokia does. The flat helper is the only one that fits: the strip is about
3.5 mm deep on the two smallest boards and a label stood on end needs 4.7 mm. Three of the variants
print their node names unchanged, and the 2.8" needs its own `SILK_NAMES_ILI9341` for two reasons
that make the default wrong rather than merely untidy -- its node names carry the `MOSI (SDI)` and
`MISO (SDO)` aliases, and its five touch pins are prefixed `T_`, which the default label strips at
the underscore, so five adjacent pins would all have read `T`. The replacements were measured
rather than eyeballed: a flat label has 20 px of pin pitch to live in, `RESET` is 22 px and `T_CLK`
24 px, against 14 and 16 for `RST` and `TCK`. The full forms stay on the nodes, so the tooltips and
the netlist still spell them out.

**`GC9A01_1_28` has landed as well:** a 38 mm disc with a 22.9 mm tab projecting 7.5 mm, the header
1.76 mm in from the tab edge, no mounting holes, and seven pins `RST CS DC SDA SCL GND VCC` -- the
clock pin confirmed as `SCL` rather than the `SLC` typo some of these boards carry on their silk.

Two things about that outline are worth keeping. The body is the disc unioned with the tab, and the
tab has to be carried to the disc's centre line before the union: taken only as far as the rim, the
two shapes meet at a single tangent point and leave a notch either side of it. And the projection
is not stored, being whatever the outline is longer than it is wide, so the round board is
described by the same two figures as every other variant. The lit area is a 32.5 mm circle centred
on the disc rather than on the bounding box, since the tab offsets one from the other.

One thing the flip turned up. The enum's pin array ran `VCC GND SCL SDA DC CS RST`, the reverse
of the `RST CS DC SDA SCL GND VCC` recorded two paragraphs above, so the row was mirrored against
the part. With the tab drawn at the top either order looked plausible -- the row has no landmark
to be wrong against -- and turning the board over is what made it visible. The array now reads
RST first, which is to say leftmost, and `roundPinNamesComeFromTheController` pins it there.

**The tab hangs below the disc, not above it.** That makes the round board the second in the
package, after the Nokia, whose header is on the bottom edge and whose outline therefore grows
upwards from control point zero -- so its header offset is measured up from the bottom edge where
every other variant's is measured down from the top, and its pin names go above the row rather
than below it. `headerSitsOnTheEdgeItsBoardHangsFrom` asserts the distinction for all four
variants, because nothing else in the geometry would notice if the round one were flipped back.

1. `Controller` enum: `ILI9341_2_8("2.8\" ILI9341 240x320 (Touch + SD)")`,
   `ST7735_1_8("1.8\" ST7735 128x160")`, `ST7789_1_54("1.54\" ST7789 240x240")`,
   `GC9A01_1_28("1.28\" GC9A01 240x240 Round")`. Default stays the ILI9341 so the icon is unchanged.
2. Per-variant body size, pin count and pin array. The ILI9341 module is 14 pins with the touch and
   SD lines; the ST7735 and ST7789 boards are 8 pins (`GND VCC SCL SDA RES DC CS BLK`); the GC9A01
   is 7 or 8 depending on the board.
3. `updateControlPoints()` sizes the array from the per-variant pin list, as `ESP32DevKit` does.
4. `getBodyShape()` branches: rounded rectangle for three of them, a **circle with a tab** for the
   GC9A01, which is the one genuinely new outline.
5. `draw()` branches on the screen aspect and on whether the SD-slot and touch hardware exist — only
   the ILI9341 has them.
6. The round variant is worth the effort beyond its own popularity: it is what gauge, VU and clock
   projects reach for, which overlaps the existing audience.

Tests: per-variant `assertBoardSize` and pin-count, a round-outline assertion, draw smoke across all
four. `TFTDisplayTest` carries 19 of them. The two that were written against "the boards that have
holes" as a group had to be reopened for the 1.54", whose drill and inset are its own: what they
assert now is that each board's holes share one figure, not that the package family does.

### 6.3 `OLEDDisplay` variants — `Version` enum — **done**

Do this in the same pass as the §5 item 5 glass fix.

**Item 3's premise does not survive the maintainer's figures, and that is the whole shape of this
item.** It assumed the interface is "orthogonal to the size", which holds for the 0.96" and is
false for the 0.91": the I2C and SPI versions are not one board with two headers but **two
different boards**, 38 x 12 mm with a four-pin column standing against the left edge against
38 x 20 mm with a seven-pin row along the top, and no mounting holes against four. So board
length, header edge, hole pattern and panel position are all functions of the interface as well as
the size, and branching on the interface in the geometry methods -- which is what `CharacterLCD`
does, and the closest precedent in the package -- is not on its own enough: there the board is the
same for both interfaces and only the header moves.

What is settled so far, all maintainer-supplied:

- **0.91" SSD1306 I2C** — board 38 x 12 mm; four pins in a vertical column against the left edge,
  1.5 mm in and centred on the height; dark panel 30 x 12 mm, the full height of the module,
  starting 5 mm from the left edge, so it spans 5 to 35 mm and leaves 3 mm of bare PCB at the right
  end; no mounting holes. The 5 mm strip is what the pin column lives in, and at `PIN_SIZE` the
  pins span 1.0 to 2.0 mm, clearing the panel by 3 mm.
- **0.91" SSD1306 SPI** — board 38 x 20 mm, a seven-pin row on the top edge, four 2 mm mounting
  holes 2.5 mm in from each of the two edges nearest them. It carries the same physical 30 x 12 mm
  panel as the I2C board; its position on the larger board is still wanted.
- **Both 0.91" boards** — the lit area starts **2.1 mm inside the panel's left edge**. This is a
  figure of the panel part rather than of either board, which is why it carries across both and
  should not be stored per board. The lit area itself is the usual derivation, 22.384 x 5.584 mm
  for 128 x 32 on a 0.175 mm pitch, and is flagged as such.
- **1.3" SH1106** — board 35.4 x 33.5 mm, bezel 31.4 x 16.7 mm, sold in both interfaces. Its lit
  area is the derivation 29.42 x 14.7 mm (128 x 64 at 0.23 mm). Hole pattern, header offset and
  bezel position are still wanted.

The 1.3" came in at 35.4 x 33.5 mm with a 31.4 x 16.7 mm bezel, four 3 mm holes 2 mm in from each
edge, and one board for both interfaces. The hole proportion is worth recording because it looks
wrong until it is checked, as `CharacterLCD`'s did: a 3 mm hole centred 2 mm from an edge leaves
only 0.5 mm of board outside it, tighter than anything else in the package.

**What the shape turned out to be.** `Version` carries the display part -- panel, lit area, and the
lit area's offset inside the panel -- and hangs a `Layout` off itself for each interface, carrying
everything that belongs to a PCB: outline, header edge and clearance, hole inset and drill, and
where the panel sits on it. The 0.96" and the 1.3" pass **one** `Layout` through a second
constructor, so both interfaces return the identical instance and "one board, two headers" is a
fact the test can assert with `assertSame` rather than a convention.

That shape was not the first one tried, and the correction is the useful part. `Layout` initially
carried the pin array too, which forced the 0.96" and the 1.3" to build two `Layout` objects
differing in nothing else -- and `theSmallBoardIsTwoDifferentBoards` failed on exactly that, since
two instances are not one board. The array belongs to the **interface**: all three sizes bring the
same four or seven lines out in the same order, so `OLEDInterface` carries it and `Layout` is left
describing only the board. A test written against the intended meaning rather than the code found a
design flaw, which is the opposite of §6.6's `bodySitsBetweenThePinRows`.

Two figures are flagged rather than measured, both on the 1.3": its **header offset** is taken as
the 0.91"'s 1.5 mm, and its **bezel is placed centred** on the board. Neither was supplied.

**Which 0.91" board gets which panel offset was settled the hard way, and the episode is the
argument for a test that was missing.** The panel starts 5 mm from the left edge on the I2C board
and 1.25 mm on the SPI one, the lit area being the panel's own 2.1 mm further in on each, so the
lit areas land at 7.1 mm and 3.35 mm. The two offsets were swapped between the boards at one point
and **every one of the twenty geometry tests still passed**, because they all measure features
against each other: both arrangements have a panel on a board of the right size with its lit area
correctly placed inside it. What the swap actually produces is visible only in a render -- the I2C
board's panel then begins 0.25 mm *before* its own pin column, so the four pins are drawn on the
glass, and the 6.75 mm of bare board ends up at the far end where no connector is.

`pinsStayOffThePanel` is what replaces the render. It is worth noting what kind of assertion it is:
not another dimension, but the one relationship between the two halves of the drawing that nothing
else here constrains. The asymmetry it leaves standing is real -- the I2C board carries 3 mm of
bare PCB at its right end and the SPI board 6.75 mm -- and is now confirmed rather than merely
consistent.

The lit areas are the nominal pixel count at the panel's dot pitch, as the TFT's are: 128 x 64 at
0.17 mm, 128 x 32 at 0.175 mm, 128 x 64 at 0.23 mm. `litAreaMatchesThePixelAspect` holds them to
the 2:1 and 4:1 aspects their pixel counts imply, which is the check the 0.96" was drawn wrong
against for as long as its panel was near-square.

**The 0.96" did not move.** Its stored panel offsets and hole inset are the centred position and
the 24 mm centre-to-centre pattern expressed in the new model, and its render is byte-identical
before and after the refactor. Two tests keep that honest rather than leaving it to coincidence:
`centredPanelsStoreTheCentredOffsets` fails if a panel or board size is corrected without
recomputing the offset, and `holePatternMatchesTheSpecifiedSpacing` keeps the stored inset and the
specified 24 mm spacing in step.

`OLEDDisplayTest` carries 21 tests. The descriptor was renamed from `0.96" OLED Display (SSD1306)`
to `OLED Display`, free under D6 and following the TFT's rename in §6.2, and the `update.xml` entry
now lists the three sizes.

One follow-up this leaves: the 1.3" has 6.9 mm between its pin row and its bezel where the 0.96"
has 2.05 mm, so it is the one OLED with room for the pin-name silkscreen that §3.2 item 2 had to
drop for this class. `panelClearsThePinRow` records all three strips, so the figure is there when
someone wants it.

1. `Version` enum: `SSD1306_0_96("0.96\" SSD1306 128x64")`, `SSD1306_0_91("0.91\" SSD1306 128x32")`,
   `SH1106_1_3("1.3\" SH1106 128x64")`. Default is the 0.96, so nothing about the current look
   changes.
2. Per-variant board size — the 0.91" module is a small wide board, the 1.3" is larger than the
   0.96" — and per-variant glass size and position, taken from the active-area dimensions in the
   controller datasheets rather than guessed as a fraction of the board.
3. Pin arrays are unchanged: the interface property already covers I2C 4-pin and SPI 7-pin, and it
   is orthogonal to the size. Both properties coexist; guard the combinations that are not sold
   (the 0.91" is I2C only) by branching in `draw()`, not by removing the property.
4. `setVersion()` calls `updateControlPoints()` and `invalidateCache()`.

Tests: per-variant size, glass-aspect assertion, the interface property round-tripping against each
version. **~0.5 day.**

### 6.4 `LEDMatrix` cascade — `Modules` enum — **done**

Built from maintainer figures: the 4-in-1 is 128 × 32 mm with its four 32 mm modules flush and no
border, its two five-pin headers on the short edges standing vertical, and all four MAX7219s behind
the modules rather than visible on the front.

Worth recording that the plan's "board length scales with the module count" understated it. The two
variants are not one layout stretched: the single is a tall board carrying its driver and both
headers on the strip below the module, while the 4-in-1 is filled edge to edge, which rotates the
pin rows onto the short edges and leaves no driver showing. So `Modules` carries a board size and a
header orientation of its own, and `getBoardX`/`getBoardY` branch on it. The control-point count is
10 either way, so `PIN_NAMES` was untouched.

**A third variant followed, and cost one enum constant.** The compact 8x8 fits its driver under the
module, so the board is only the module: 32 x 32 mm, headers on the short edges, nothing visible on
the front. That is what makes it tileable — several can be butted together in a layout, which the
tall single cannot do because of its strip. The 4-in-1 is the same idea sold as one PCB, so it stays
a variant of its own rather than being modelled as four compacts.

It needed **no change to any drawing or geometry code**: the chip test already meant "is this the
tall single", the header-hiding test already keyed off whether the modules cover the board, and the
dot grid already centred its pattern. Worth recording as evidence that the per-variant split was cut
in the right place, since it absorbed a case it was not written for.

Two corrections went with it. The constants had been `_1` and `_4`, named for module counts, which
stops being true when two of three variants carry one module; they are now `Single_8x8`,
`Compact_8x8` and `FourInOne_32x8`. And `_1`'s label read "Single 8x8 (32x32mm)" while its board is
32 x 50 — harmless until a genuinely 32 x 32 board arrived, at which point two variants would have
advertised the same dimensions in the BOM. Both renames were free under D6.

Two things fell out of "flush, no border" that are worth keeping in mind for §6.7's panel. Because
the modules butt together the dot pitch runs unbroken across the strip, so the 32 x 8 grid is one
grid at `boardX + col * pitch` rather than four tiled ones — which is also what lets the demo
pattern be centred across the full width, as this item asked. And because the modules then cover the
whole board, **both** headers end up behind them on the 4-in-1, not just the output row: each is
drawn before the modules and painted over. They are drawn rather than skipped, because
`drawPinHeader` is what registers the pins as a conductive area and omitting a call renders
identically while silently dropping five pins from continuity. The cost is that the 4-in-1 shows no
connector at all, so its pins are discoverable only by selecting the component.

The plan this was built from, kept for the record:

1. `Modules` enum: `_1("Single 8x8 (32x32mm)")`, `_4("4-in-1 32x8 (128x32mm)")`. The 4-in-1 is
   probably the more commonly bought of the two.
2. Board length scales with the module count; the input and output headers stay at the two ends.
3. `draw()` loops the matrix block and its MAX7219 once per module, and draws a demo pattern across
   the full width rather than repeating an 8x8 pattern four times.
4. Rename freely. Outside its own source the class was named in one place, a test entry, and no
   .diy file anywhere serialises it, so neither the class name nor the descriptor is load-bearing
   (D6). This item originally said the name "has to stay", which was over-cautious and sat oddly
   beside its own next clause: CLAUDE.md guards classes that have *shipped*, and this one had its
   descriptor commented out until 6.7.0. The class is now `LEDMatrix` and the descriptor
   "LED Matrix (MAX7219)", both of which outgrew the 8x8 in the old name once the 4-in-1 landed.
5. While in the file, replace the hardcoded `-44 mm` header offset with a derived value.

Tests: per-variant size, header positions at both ends in both variants, draw smoke.
**~0.5-1 day.**

### 6.5 Nokia 5110 LCD — new class `Nokia5110LCD` — **done**

Two files, `Nokia5110LCD` and `Nokia5110LCDTest`, built entirely from maintainer figures: a
43.8 x 45.8 mm board, a 40 x 35 mm bezel in `LIGHT_METAL_COLOR` centred across it and 4.5 mm down
from the top edge, a 37 x 27 mm rounded glass 11 mm down, 3 mm mounting holes on a 34.5 x 41 mm pattern,
and the eight pins `RST CE DC DIN CLK VCC BL GND` centred on the bottom edge. No variants, so no enum and no
`getVariantLabel` override; the BOM value column is empty, as it is for the other parts sold in one
form.

**It is the first display board placed from its bottom edge, and that is the whole of what was new
here.** Every other board in the package hangs below a header on its top edge, so `getBoardY` is a
small clearance subtracted from the control point. Here the board is a whole board *above* the
pins, and the bezel, the glass and the hole rows are all measured from the top edge, which no
control point touches. `headerIsCentredOnTheBottomEdge` is the test that would catch the sign of
that getting flipped, since a board drawn downwards from the pins still passes every size and pitch
assertion.

The glass is placed from the top edge rather than centred in the bezel, because it is not centred
in it: 6.5 mm of frame above it against 1.5 mm below. Centring it would have been the cheaper
expression and would have put it 2.5 mm high, which is why `glassSitsInsideTheBezel` checks the two
offsets against each other rather than checking each against the board.

**The outline was first entered the wrong way round, and the geometry said so before anyone looked
at it.** Built as 45.8 wide by 43.8 long, the 41 mm vertical hole pattern left 1.4 mm to each edge,
so a 3 mm hole overran the board by 0.1 mm -- a third of a pixel at 1:1, invisible in the render and
passing every test that existed, since all of them compared features against each other rather than
against the edges. Turned the right way round, 43.8 x 45.8, every drill sits 2.4 mm or more from the
two edges nearest it and the bottom strip grows from 4.8 mm to 6.8 mm, which is where the header and
its silkscreen live. `mountingHolesClearTheBoardEdges` now asserts that clearance, and it fails if
the two figures are swapped again; the hole pattern itself is modelled as centre-to-centre spacing
rather than an edge inset, following `OLEDDisplay`, because that is how it is specified and what a
builder drills.

**The pin names are printed, and they lie flat along the row.** This is the first part in the package to
carry silkscreen at all -- the Ring, the Stick and the Strip all had theirs dropped -- and the
reason it can is the 3.5 mm strip between the pin row and the bottom edge. `drawRowPinLabels` turns
its labels on their side, which needs `PIN_ROW_LABEL_OFFSET` plus the length of the longest name:
2.0 + 2.7 = 4.7 mm, more than the strip has. So `AbstractMakerBoard` gained a sibling,
`drawFlatRowPinLabels`, which leaves them lying in the row's own direction. Flat, the constraint
flips from depth to width -- each label has the 2.54 mm pin pitch to fit into, where the widest, `GND`,
measures 2.67 mm at the rotated labels' 9 pt -- so the helper carries its own 8 pt font, at which
the same label is 2.29 mm and clears its neighbours by 0.25 mm. Both figures are `FontMetrics`
measurements rather than estimates, and the margin is deliberately slim: 7 pt was legible and left
0.6 mm between labels, and the maintainer chose the larger size on sight of the render. The names
come from `getSilkPinLabel` like every other board's, so nothing duplicates `PIN_NAMES`.

The one **unsourced number in the class** is the 3.5 mm clearance from the bottom edge to the pin
row, set by eye off the render so that the silkscreen has room to stand under the header. It places the row, and through the row the
silkscreen, so it is worth replacing with a measurement if the board is ever in hand.

### 6.6 LED Bar Graph — new class — **done**

Three files: `LEDBarGraph`, a sibling `LEDBarGraphTransformer`, and `LEDBarGraphTest`. Variants are
8, 10 and 12 segments, and the package follows from the count rather than being fixed: each segment
carries its own anode and cathode, so the pin count is twice the segment count (16 / 20 / 24) and
the body lengthens by one pin pitch per segment (20.32 / 25.4 / 30.48 mm) while staying 10.3 mm
wide. Rows are 7.62 mm apart, all maintainer-supplied.

**The part is a DIP outline but not a DIP, and that distinction governs the geometry.** An IC's
leads bend out of the sides of the plastic, so `DIL_IC` draws its body *between* the pin rows. A bar
graph's pins leave the underside within the footprint, so the package is wider than the row spacing
-- 10.3 against 7.62, leaving about 1.34 mm of body outboard of each row -- and covers its own pins
entirely. They are drawn first and painted over rather than skipped, because the draw loop is what
registers them as a conductive area; omitting it would render identically and lose every pin.

Three mistakes are worth recording, because all three were caught by something other than reasoning.

The first version copied `DIL_IC.getBody()` wholesale and so put the body between the rows, and
hardcoded 20 pins with a comment claiming an 8-segment part "simply leaves the end pairs unused" --
reasoned into, never checked. Both were wrong, and the test written alongside asserted the copied
behaviour, so it passed. A test named for the thing it does not check is worse than no test:
`bodySitsBetweenThePinRows` encoded the bug, and its replacement `bodyCoversEveryPin` would have
failed on the original.

The second was in the transformer. `DIL_ICTransformer` cannot be reused -- it guards on
`getClass().equals(DIL_IC.class)` and casts to `DIL_IC`, so it would have silently refused to rotate
a bar graph, and `MakerComponentsTest` asserts rotatability, so the failure would have surfaced only
if the component were registered there. A survey of all 32 transformers showed exact-class matching
is the near-universal convention, so a sibling follows the grain and touches nothing shipped. Its
`mirror` was then written wrong: reflecting points about the centre with unused variables, a dead
branch, and no orientation change at all, which would have mirrored the pins while leaving the body
facing as before. Reading `SIL_ICTransformer` properly fixed it.

The third was a harness bug reintroduced from earlier in the slice: positioning the component with
`setControlPoint(.., 0)`, which moves one pin and leaves the rest. The render showed a body detached
from its pins and looked like a component defect; a probe showed a fresh component was correct and
only the moved one broke. Worth knowing that `MakerComponentsTest` does the same at three sites and
gets away with it because those tests only assert that drawing did not throw -- so that suite would
not catch a genuinely detached component either.

**Four corrections followed from review, and three of them removed something rather than added it.**

*Segments are centred on their pins.* The field had been derived by dividing the body into equal
bars with margins, which is independent of where the pins actually are: on a ten segment part that
put segment zero about 5 px below its own pin pair. Each segment is now centred on the midpoint of
the pair that drives it, which is both correct and orientation-proof for free -- the midpoint of a
pin pair is the package centreline at that segment whichever way the part is turned, so the
four-way switch disappeared. The old placement passed every test that existed at the time;
`segmentsAreCentredOnTheirPins` is what makes the fix stick.

*The pins are drawn on top, and round.* On the part they are underneath the plastic and invisible
from above, which is what the first version drew. But this is a drawing someone lines up against a
board, and a pin that cannot be seen cannot be positioned, so legibility wins over the photograph
here. They are round because a bar graph's leads are drawn wire rather than the flat stamped
leadframe an IC has.

*The segments are the size of the real lit windows.* They had been derived as the package less a
margin and the pin pitch less a gap, which made them 8.3 x 2.04 mm -- most of the package, and
visibly too fat. They are now the maintainer's measured 4.8 x 1.7 mm, carried as `SEGMENT_WIDTH` and
`SEGMENT_LENGTH` rather than subtracted from something else, so a segment keeps its size if the
package or the pitch is ever corrected. One claim recorded here went with the old figures: the pins
no longer land inside the lit area, since the rows are 7.62 mm apart and the windows only 4.8 mm
across, so each row now sits on bare plastic outboard of the segments, which is where the real part
has them.

*The colour is a rule, not a property.* A bar graph is a scale and the colours carry the reading:
one red at the top, two yellow below it, green for the rest. The bands shift with the count rather
than being fixed fractions -- five green on an eight segment part, seven on a ten, nine on a twelve.
The trade is that a single-colour bar, all red or all green, can no longer be drawn, and both are
sold; a mode property would bring them back if that is wanted.

*Two properties were vestigial.* `labelColor` came across from `DIL_IC`, which renders its value on
the body; nothing here draws a label, so the colour had nothing to colour, and it is gone. `value`
stays, because `IDIYComponent` requires every component to carry one, but its javadoc now says what
it is for -- somewhere to record the part you bought, reaching the BOM and keyword search and
nothing else -- rather than leaving a reader to infer its purpose from a class it was copied from.

**The BOM policy was declared, and leaving it undeclared had not been neutral.** `bomPolicy`
defaults to `SHOW_ALL_NAMES`, so by saying nothing the bar graph had quietly become the only
component in the category that lists every instance by name -- `BAR1, BAR2, ...` -- rather than
collapsing to one row with a quantity. It now declares `SHOW_ONLY_TYPE_NAME` like its neighbours,
verified by reading the resolved `ComponentType` rather than the source, since the annotation
default is precisely what made the source misleading.

The value column needed the same attention and for a sharper reason. `BomMaker` keys rows on
`typeName + "|" + value`, and the inherited `getValueForDisplay` returns `getValue()`, which here
is the part number and is usually blank -- so an eight, a ten and a twelve segment part would have
**merged into a single row**. That is §11.1's defect exactly, reappearing in the one class that
could not inherit the fix: `getVariantLabel` lives on `AbstractMakerBoard` and the bar graph is on
a different base. `getValueForDisplay` is overridden locally to put the segment count in the value
column, appending a typed part number when there is one, which is the same decision written out by
hand. Worth remembering for §6.5 and anything else that leaves the maker hierarchy: the BOM
behaviour every board gets for free has to be re-established by hand.

The plan this was built from, kept for the record:

Ten segments in a 20-pin DIP outline. Nothing in the library covers it: `LED`, `LEDSymbol` and
`PilotLampHolder` are the closest, and none is a segmented bar. It is the cheapest item here and the
only one that pays off entirely within the **existing** audience, for VU and level meters.

The base class is an open question (§11.3). `AbstractMakerBoard` is the wrong fit — this is not a
board — and the established shape for a dual-row DIP part is `DIL_IC`, which extends
`AbstractLabeledComponent`, carries a `PinCount` enum and points at a dedicated
`DIL_ICTransformer`. Recommendation: follow `DIL_IC`, with a fixed 20-pin geometry rather than a pin
count property, a `Segments` enum for the 10-segment and 8-segment variants, and a `Color` property
for the lit colour. Category `Displays & Outputs` even though the neighbours in shape live in
`Semiconductors`, because that is where someone will look for it.

**~0.5-1 day once the base class is settled.**

### 6.7 Cheap additions

- **`WS2812BRing` 8-LED constant** — **dropped.** Adafruit does not list an 8-LED ring, and the
  third-party boards sold as one do not share a pad arrangement, so there is no single part to
  model. Its dimensions were available (30 mm outside, 15 mm inside) and would have appended
  cleanly; what was not available was a pad layout, and `RingSize` requires one — the constructor
  throws unless the pad gaps sum to the LED count. That check is the reason this was caught rather
  than guessed: filling it with a plausible arrangement is precisely the fitted geometry this slice
  has spent its time removing.

- **`WS2812BRing` 7-LED Jewel** — **done**, as the separate class `WS2812BJewel`, but **not the
  two-line append this item assumed**, and the hour this section originally budgeted for it was
  wrong. Two things broke:

  1. *It is a disc, not an annulus.* `draw()` subtracts the inner circle from the outer
     unconditionally. An inner diameter of zero would draw the right outline, but `getMidRadius()`
     averages the two rims, so the LEDs would land on a circle half the outer radius -- inside the
     body rather than on it.
  2. *Its seven LEDs are six around one in the centre.* The draw loop spaces every LED evenly at
     `2*pi*i/ledCount`, which would put all seven on one circle and leave the middle empty. That is
     an arrangement, not a dimension, so no measurement fixes it.

  Both were settled by making it a separate class, `WS2812BJewel`, which is what decision D2 already
  prescribes: a new class where the footprint family is not shared. The flag looked cheaper only
  until the measurements arrived, at which point it would have needed company -- an explicit LED
  circle (16 mm, with no inner rim for it to be the midpoint of), an explicit pad circle (9 mm, where
  the rings inset theirs from the outer rim), a mounting-hole pattern no ring has, and a gap
  assertion counting ring LEDs rather than all of them. Five variant-specific structures describe a
  different part, not a variant.

  Maintainer-supplied figures: a 23 mm disc with no hole; six LEDs on a 16 mm circle with the seventh
  in the centre; two 3 mm mounting holes 19 mm apart horizontally; and five pads on a 9 mm circle,
  `OUT` in the gap anticlockwise of the top LED, then clockwise `GND`, `GND`, `PWR`, `IN`. Five pads
  share six gaps, so one gap carries none; it works out to the gap pointing due left, which is where
  a mounting hole sits. `WS2812BJewelTest` pins that down, because every count and radius check
  passes whichever gap is left empty -- the arrangement is the part that can silently be wrong.

  One change reached back into the rings while this was built. `MakerBoardPainter`'s addressable-LED
  helper gained a rotation parameter, so packages laid out on a circle are drawn turned to face along
  their own radius the way they are mounted, rather than square to the board. It applies to all three
  ring sizes as well as the Jewel; the Jewel's centre package has no radius and stays square, and the
  linear strip and stick are unaffected. The package is square, so the turn only shows modulo a
  quarter turn, and no geometry assertion catches it -- it is visible only in a render.
- **NeoPixel panel 8x8** as its own class, reusing `drawAddressableLed` and the ring's pad handling.
  Worth doing only after §6.1, whose helper it depends on. **~0.5 day.**

### 6.8 On-screen identification — the glass prints its own spec — **done**

**Done.** The four displays that have a lit area — `OLEDDisplay`, `TFTDisplay`,
`CharacterLCD`, `Nokia5110LCD` — draw their variant string inside it, in the panel's own lit colour,
on by default and switchable off per component. This amends D4 for text that *identifies the part*
and leaves D4's ban on invented content standing.

**Why it is worth reversing a decision for.** Variant coverage is what §6 spent itself on: the
OLED now carries three sizes times two interfaces, the TFT four controllers, the Character LCD two
sizes times two interfaces. On the canvas all six OLED combinations are the same dark letterbox on
the same blue board, and the three places that say which one is selected are the property editor,
the BOM and the Explorer pane — none of them visible while drawing. A layout with four displays on
it cannot be read back at all.

**The string is the one that already exists.** `getValueForDisplay()` returns exactly what this
section wants for every class in the package, because §11 item 1 already made it the BOM's value
column:

| Class | Already returns |
|---|---|
| `OLEDDisplay` | `0.96" SSD1306 128x64, I2C` |
| `TFTDisplay` | `2.8" ILI9341 240x320 (Touch + SD)` |
| `CharacterLCD` | `16x2, I2C Backpack` |
| `Nokia5110LCD` | *nothing — see below* |

`Nokia5110LCD` is the exception and needs one small addition: it is a single part, so it never
overrode `getVariantLabel()` and `getValueForDisplay()` returns the empty string, which would leave
its glass blank. It should return `84x48 PCD8544` — the resolution and the controller, which is what
this section is for and which also fills the BOM's value column that §11 item 1 left empty on it.
A single part does not need the variant string for BOM *grouping*, but it is still the better answer
than nothing in that column.

It is used verbatim, word-wrapped to the lit area. The alternative — each class composing its own
diagonal, resolution and interface lines from the enum fields — was rejected because it creates a
second statement of the variant that can drift from the BOM's, which is the failure §3.2 kept
hitting with hand-rolled label arrays. One string per class, written once.

**Mechanism.** A static helper beside the others, `MakerBoardPainter.drawScreenText(g2d, area,
inkColor, font, text)`, which wraps the string to the area's width and draws nothing at all if the
wrapped block does not fit the area's height. Each display calls it on one line from inside its
orientation transform, straight after it fills the lit area, so the text turns with the board the
way the real screen's content does. Same shape as D5's LED helper: four call sites, one
implementation.

**Property.** `org.diylc.common.Display` on each of the four classes, `@EditableProperty(name =
"Screen")`, defaulting to `Display.VALUE`, with the usual null-tolerant getter. The shared enum is
reused rather than a boolean invented because it is the idiom every other labelled component in the
library already follows, so the control is where a user expects to find it; `NONE` is the off switch
this section owes D4, and `NAME` and `BOTH` come along with the enum rather than being designed in —
`NAME` on the glass is genuinely useful when tying a drawn board to a netlist. It does **not** go on
`AbstractMakerBoard`: the other seven components in the package have no glass to print on.

**There is room, and the fit rule is a guard rather than a routine.** At 200 px/inch the lit areas
measure, in pixels:

| Panel | Lit area |
|---|---|
| 0.91" SSD1306 | 176 × 44 |
| 0.96" SSD1306 | 171 × 86 |
| 1.3" SH1106 | 232 × 116 |
| 1.54" ST7789 | 218 × 218 |
| 1.28" GC9A01 | 256 dia. |
| 1.8" ST7735 | 225 × 281 |
| 2.8" ILI9341 | 340 × 453 |
| Nokia 5110 | 291 × 213 |
| 16x2 LCD | 508 × 114 |
| 20x4 LCD | 606 × 198 |

The tightest is the 0.91" OLED: two lines of the 11 px `SILK_FONT` need 28 px of its 44, measured
rather than estimated. So the drop-rather-than-squeeze rule holds here as it does for silkscreen,
but nothing in the package triggers it — every variant was rendered and looked at. The round GC9A01 is the one case where width is not the board's width: text
must fit the inscribed square of the 256 px disc, not its diameter.

**Ink colour is each class's to pass.** The OLED's `PIXEL_BLUE` on its near-black active area, white
on the TFT's screen fill, the Nokia's dark LCD ink on its grey-green glass. The Character LCD is the
one that cannot use a constant: its screen colour is an editable property, so the ink is chosen from
the fill's brightness rather than fixed, or a user who picks a pale backlight gets white on white.

**Refinement worth taking on the Character LCD.** Lay the text on the module's real character grid —
one glyph per cell, truncated at the column count rather than wrapped freely — so the `16x2` is
demonstrated instead of merely asserted. It fits: `16x2 HD44780` is twelve characters and
`I2C Backpack` twelve, against sixteen columns. This is the only display whose resolution is a count
of characters rather than of pixels, so it is the only one where this is possible.

**The other seven are untouched.** The 7-segment, the bar graph, the LED matrix and the four NeoPixel
parts have discrete emitters rather than a screen; spec text there would have to be rendered as lit
dots to be honest, and an 8x8 grid carries one glyph, which identifies nothing. `LEDMatrix` keeps the
fixed lit pattern it already draws. `drawIcon` is also unchanged throughout — the `OLED` / `TFT` /
`LCD` text in the toolbox icons is a legibility device at 32 px and was never what D4 was about.

**Testing.** Per class, the §7 drawing smoke test gains the property in each of its four states,
plus an assertion that what reaches the helper is `getValueForDisplay()` rather than a literal, so a
later correction to an enum label cannot leave stale text on the glass. Whether it *looks* right is
§8's business, and this is precisely the silent rendering change §8 says there is no
baseline to catch — so the §8 sample projects should be generated with this in place rather than
before it.

**Effort.** ~0.5 day for the helper, the property on four classes and the tests. ~0.5 day more for
the character-grid refinement, which is independent and can follow.

**What the build settled.** Four things were decided at the keyboard rather than here:

1. *The line break is the comma, not the width.* The variant string reads "size, interface", so
   breaking it there gives one property per line. Left to wrap on width alone every current variant
   came out as a single line filling 95% of the glass — the 0.96" OLED's description is 153 px
   against 162 px of usable panel — which is legible but reads as a line of text crammed into a
   window rather than a readout.
2. *The width check happens after wrapping, not before.* `StringUtils.wrap` cannot break a single
   word wider than the limit and hands one back oversized, so the helper measures each wrapped line
   and bails if any still overflows. Without it the drop rule would have had a hole in exactly the
   case it exists for.
3. *The Character LCD's ink is chosen by luminance,* through `CalcUtils.calculateLuminance` on the
   screen colour. Both inks are real parts — the blue-backlit module shows light characters, the
   yellow-green one dark — so this is faithful rather than merely defensive, and it is what stops a
   pale backlight from being printed on in white.
4. *`Nokia5110LCD` gained the `getVariantLabel()` this section asked for,* returning
   `84x48 PCD8544`, which also fills the BOM value column §11 item 1 left empty on it.

The character-grid refinement is **not** done; it remains the open item above.

**Tests.** `ScreenTextTest` covers the helper's own contract away from any component — that a panel
with room prints, that one too narrow or too shallow prints nothing at all, that `NONE` and an
empty variant leave the glass dark, and that the three printing settings differ. Each of the four
display tests then renders its panels twice, once printing and once with `Display.NONE`, and fails
if the two renderings match; rendering is what makes it a check on the drawing rather than on the
getter, since a screen that silently found no room would answer the getter either way. The shared
`MakerBoardTestSupport.renderPixels` added for this is reusable by anything else that needs to
catch a drawing change that moves no geometry. `CharacterLCDTest` is new — the class had no test at
all — and also pins down the two inks.

### 6.9 Deliberately not in this slice

E-paper (1.54" / 2.9" SSD1680) — growing but still niche, and the flexible-cable mounting is real
new drawing work. Also: 14- and 16-segment alphanumeric, HT16K33 backpacks, 1.2" and 2.3" large
digits, 3.5" ILI9488, VFD, DotStar and APA102, and the RGB-backlight 1602.

## 7. Testing

Per-class tests under `diylc-library/src/test/java/org/diylc/components/displays/`, named
`*Test.java`, GPL header like any other file, following the `micro` pattern and using the promoted
`MakerBoardTestSupport`:

- `testControlPointCountAndNames()` — count, no empty names, spot checks, and for
  `WS2812BStick` an explicit **no duplicate node names** assertion given §3.1.
- `testDimensionsAndGeometry()` — `assertBoardSize` against the sourced dimensions, header edge and
  pitch via `assertRow`.
- `testVariantProperty()` — default, labels, round-trip through the setter, for every class that
  gains an enum in §6.
- a drawing smoke test over every variant in both header states and all three draw modes.

`MakerComponentsTest` keeps its generic coverage; move the seven (then ten) classes into
`releasedMakerComponentClasses` as they ship, and add the new classes to the list as they are
written.

## 8. Regression suite and release

**There is no rendering-regression coverage for any maker component at all.** No `.diy` file among
the 1418 in `diylc-regression-data` references anything in `org.diylc.components.micro`, so batch 1
shipped with none either. This slice should add a small number of sample projects — one per display
class with a couple of variants each, ideally wired to a controller from batch 1 — and regenerate
the PNG and netlist baselines. That is the only mechanism that would catch a silent rendering or
node-naming change later, and the netlist baseline is what would have caught §3.1's duplicate
grounds.

Release notes go in a **new `6.7.0` block** in `diylc-core/src/main/resources/update.xml`. 6.6.0 is
already dated and released, and released blocks are not edited. One `NEW_FEATURE` entry per palette
entry, following the 6.5.0 wording for the controllers ("Arduino Nano - ATmega328, 33 BLE, ...":
part name, then the variant list).

## 9. Sequencing

1. ~~**§5 items 1 and 2**~~ — **done**: test support promotion and the shared LED helper. Everything
   else builds on them.
2. **§5 items 3-9** — the prep pass, finishing with the sweep in item 9.
3. **§5 item 10 and §6.2-6.4** — sourcing, folded into the variant work for the three classes that
   gain enums, since both touch the same constants.
4. **§6.1, §6.5, §6.6, §6.7** — the new classes, each an independent commit.
5. **§6.8** — on-screen identification, after the variant enums it reads from are final.
6. **§7** tests alongside each of the above, not after.
7. **§8** regression samples and the `update.xml` block, last.

Eleven palette entries so far: the seven existing, plus the strip, the bar graph, the Jewel and the
Nokia, plus the panel if appetite holds. The `update.xml` 6.7.0 block now carries all eleven,
alphabetically as the 6.5.0 block lists its boards. Two of the entries it already had were stale
rather than missing, and both were stale because a class had outgrown them: the matrix was still
described as an 8x8 and the TFT as a touch screen with an SD slot, which is one variant of four
and a part that was removed from the drawing. A twelfth entry is needed only if the panel is
built.

## 10. Later slices, and why they are later

**`sensors` (9 drafts).** The archetype argument fails here: there are hundreds of sensor breakouts
and no canonical member of most functions, so nine drafted parts are a sample rather than coverage.
Four of them — HC-SR04 ultrasonic, DHT11/22, PIR HC-SR501, MPU6050 — are genuinely the unrivalled
answer in their function and appear in every beginner kit; the other five (LDR, soil moisture,
TCRT5000, IR receiver KY-022, BME280) are commodity KY-style three-pin boards, visually near
identical, carrying most of the per-part sourcing cost for the least distinctiveness. When this
slice happens, ship the four and leave the rest. Do **not** collapse the commodity tail into a
parameterised "three-pin sensor module": that is the shape of the generic configurable board that
was already built and reverted, and it fails for the same reason — people search the palette for a
product name.

**`modules` (21 drafts).** The largest and least homogeneous batch — motor drivers, buck and boost
converters, chargers, radios, RTCs — and the worst geometry drift in the tree (`ULN2003Driver` has
24 raw pixel offsets, `PCA9685ServoDriver` 22, `L298NMotorDriver` 18). Worth splitting by function
rather than shipping as one category. One known overlap to settle first: the drafted
`ActiveBuzzerModule` (KY-012) against the existing `misc/Buzzer` ("PCB Buzzer, active or passive").

**`robotics` (4 drafts).** Blocked on an architectural decision, not on part selection.
`AbstractMotor` is a parallel fork of `AbstractMakerBoard`, re-implementing `rotatePoints`,
`drawPins`, `getFinalBorderColor` and `getCachingBounds` plus its own copies of `PIN_COLOR`,
`PIN_SIZE` and the silk fonts. Four components do not justify a second hierarchy; either lift the
shared behaviour into a common base or take the duplication deliberately. Either way it should not
gate the displays.

## 11. Open decisions

1. **BOM treatment of variants** — **decided; the board roadmap's §8.2 is settled by it too.** Every
   maker board uses `bomPolicy = SHOW_ONLY_TYPE_NAME` and returns `null` from `getValue()`, which
   left the BOM's value column empty. That was not merely a missing label: `BomMaker` groups rows on
   `typeName + "|" + value`, so every variant of a class **collapsed into one row** — two Unos of
   different versions printed as a single line of quantity two, and the BOM could not say which part
   to buy.

   A new `BomPolicy` constant was considered and rejected. The policy only chooses what goes in the
   *name* field (`SHOW_ALL_NAMES ? name : typeName`); it has no bearing on `value` or on the grouping
   key, folding the variant into the name column would break the two-column convention every other
   component follows (`R1, R2` / `10k`), and filling it would mean `diylc-core` reaching into
   concrete component types.

   Instead `AbstractMakerBoard` grows a `protected String getVariantLabel()` hook and overrides
   `getValueForDisplay()` to return it. The variant lands in the value column, which is already
   rendered, already exported and already part of the grouping key, so the rows split correctly with
   **no change to `BomMaker` at all**. Eleven classes override the hook; the six boards sold in a
   single form inherit the `null` default and keep an empty value.

   One consequence to remember: `getValueForDisplay()` also feeds component search in `Presenter` and
   the node labels in `AbstractNetlistAnalyzer`, so netlist output gains the variant for any project
   containing a maker board. No regression sample contains one today, which is precisely why this is
   cheaper to do now than after §8's samples are written.

   **The labels were later trimmed to suit the column they landed in.** Enum labels had been written
   for a property drop-down and read badly as shopping-list entries once they became BOM values —
   `OLEDDisplay` printed the wiring legend `I2C (4-Pin: GND, VCC, SCL, SDA)`, `CharacterLCD` printed
   `16x2 (80x36mm), I2C Backpack (4-Pin)`, and `WS2812BRing` carried an outside diameter that is a
   specification rather than part identity. Four components were trimmed in place — `OLEDDisplay`,
   `CharacterLCD`, `WS2812BRing`, `SevenSegmentDisplay` — so a cell now reads `16x2, I2C Backpack`
   or `4-Digit 0.36"`. The drop-down loses that detail too, which was the accepted trade: the pin
   legends are already in the node names, and the dimensions are in the fields beside the label.

   The seven boards were deliberately **left alone**: `UNO R4 WiFi`, `Pi Pico 2 W` and `Teensy 4.1`
   are already the name you would order. `ESP32DevKit` keeps its pin count — `ESP32 DevKit V1
   (30-Pin)` — because that is genuinely how those boards are distinguished in a catalogue.
   `ArduinoNano` needed nothing: its enum carries silkscreen and package markings in fields beside
   the label, so only the label itself was ever reaching the BOM.

   Two things this does not change: enum **constant names** are untouched, so nothing serialises
   differently, and the grouping still splits correctly on the shorter values — two Uno versions
   still produce two rows.
2. **A uniform property set** — the board roadmap's §8.3 — **decided, and the answer is that
   uniformity is the wrong goal.** The rule that settles all three parts of it: *a maker component
   gets a property when the real part gives the buyer a choice; a drawing preference is not a
   property.*

   **`Headers` stays where it is.** The roadmap called its distribution "drift", and a survey of
   all 21 maker classes says otherwise: it exists on the seven boards sold both ways -- Nano,
   ESP32DevKit, NodeMCU, Wemos D1 Mini, Pico, Zero and Teensy -- and is absent on exactly the three
   that are not, the Uno, the Mega and the full-size Pi, all of which ship with their headers
   soldered. The Zero carrying it while the full-size Pi does not is the tell: that is the product
   line, not an oversight. Retrofitting it onto the Uno would offer a configuration the part does
   not have. The roadmap's candidate list is also stale -- Teensy and ESP32DevKit have had the
   property for some time. If it is ever wanted on the display modules the same test decides it: an
   OLED or a Character LCD's parallel row genuinely ships with the strip loose in the bag; the
   Ring, Stick, Strip and Jewel have no header to choose.

   **"Show Pin Labels" is rejected**, for a compatibility reason first. A new `boolean
   showPinLabels` on a shipped class **deserializes as `false`**, so every existing `.diy` file
   would quietly lose its silkscreen; preserving the current look would force the field to be
   named `hidePinLabels`, an inversion that reads as a bug forever after. Beyond that, the
   silkscreen is a feature of the board rather than a view setting -- §3.2 item 2 is an extended
   argument about *where* names fit, which a switch does not improve -- and the library has
   essentially no precedent for it: one `"Show Bracket"` boolean against `Display`
   (NAME/VALUE/NONE/BOTH) on 31 classes, which governs a component's own name and value and which
   maker boards deliberately do not render. If the motivation is clutter, the better fix is drawing
   silk only above a zoom threshold: no property, no serialized field, and it helps everyone rather
   than whoever finds the checkbox.

   **Colour follows `SevenSegmentDisplay`'s rule**, which costs nothing because the package already
   obeys it: one property per distinct physical layer, named for the layer, ignored where the layer
   does not exist. The negative half is the part worth keeping: never let one colour field paint
   two different physical things, and never reset a colour from a variant setter.

   That last point was still being broken. `ArduinoUno.setVersion()` overwrote `bodyColor`
   unconditionally, so switching an R3 to an R4 discarded a colour the user had chosen, and the
   drafted `DHTSensor.setModel()` did the same. Both now repaint only while the colour is still one
   of the class's own defaults, so an untouched board still follows its version -- the R3 and R4
   are genuinely different colours -- while a chosen one survives.
   `ArduinoUnoTest.aChosenColourSurvivesAVersionSwitch` covers it, and fails against the old
   behaviour.

   **Colour is now part of this question, and `SevenSegmentDisplay` has a worked answer.** It
   carried the inherited "Board Color" plus a local "LED Color", which was wrong twice over: three
   of its four packages have no board at all, and the field painted two different physical things —
   the plastic moulding on a bare package, the PCB on a TM1637 — while the display background was
   hardcoded and not editable. It now carries three colours that each mean exactly one thing:
   **Board** (the PCB, ignored on the bare packages), **Body** (the display device itself: the
   moulding on a package, the module on a TM1637) and **LED**. The recessed window is derived as
   `bodyColor.darker()` rather than being a fourth property, which keeps a red package from having
   a black window.

   Two findings worth reusing. A subclass **can** relabel an inherited `@EditableProperty` — an
   `@Override` carrying its own annotation wins, verified by extracting the property list rather
   than inferred from a clean compile, and `SubminiTube`, `PCBTerminalBlock`, `IECSocket` and
   `DIPSwitch` all already do it. And per-variant defaults should **not** be set from the variant
   setter: `ArduinoUno.setVersion()` and the drafted `DHTSensor.setModel()` both overwrite
   `bodyColor`, discarding a colour the user chose. Separate fields for separate physical layers
   avoids needing a default that changes underneath someone.
3. **The bar graph's base class** (§6.6) — **decided: `DIL_IC` on `AbstractLabeledComponent`**, with
   a transformer of its own. The part is a DIP outline, not a board, so `AbstractMakerBoard` was
   the wrong fit.

   Two things this cost that the recommendation did not anticipate. `AbstractLabeledComponent`
   supplies almost nothing — it adds a font-size override to `AbstractTransparentComponent` and
   stops there — so the bar graph implements its own control points, body, draw, bounds and node
   names, exactly as `DIL_IC` does. And "rotation for DIP parts already has a transformer" turned
   out not to help: `DIL_ICTransformer` guards on `getClass().equals(DIL_IC.class)`, which is the
   convention across all 32 transformers in that package, so it refuses anything that is not
   literally a `DIL_IC`. The bar graph needed its own, which is additive and changes nothing
   shipped.

   It is also the first `Displays & Outputs` component that is not an `AbstractMakerBoard`, so it
   inherits none of the maker helpers — no `getVariantLabel`, no shared LED painters, no
   `drawSolderPads`. The category is still right: it is where someone looks for a bar graph.
4. **Whether `LedCount` on the strip should be a free integer or an enum** (§6.1) — **decided: a
   bounded integer**, as the plan assumed. An enum would invent a constraint the part does not
   have: tape is cut wherever the builder wants, so any fixed set of lengths would be arbitrary.

   One of the arguments recorded for the enum does not survive contact with the implementation. It
   said an enum "bounds the control-point maths" — but the strip carries four control points at any
   count, because the LEDs are drawn rather than wired, so there is no maths for it to bound. The
   bound that does exist is on the drawn shape, which grows without limit: 144 LEDs of 30 LED/m
   tape is already most of five metres. `MIN_LED_COUNT`, `MAX_LED_COUNT` and `DEFAULT_LED_COUNT`
   carry it, and the getter repairs a count that deserializes as zero.
5. **The TFT's mounting holes** — **decided, and since corrected.** Four holes, every one 3 mm across and
   3 mm in from each of the two edges nearest it. The header-side pair was carried at 6.92 mm for a while
   on the reasoning that it had to clear the pin row; it does not, that pair sitting outside the row
   horizontally, and the maintainer corrected it to a uniform inset. The glass is still centred
   between the two hole rows rather than measured from an edge, which with a uniform inset puts its
   midpoint at the middle of the board: 43 mm from the top, so a 69.1 mm glass spans 8.45 mm to
   77.55 mm and clears both pairs by 4.2 mm. Both are derived in `draw()` from the hole constants,
   so moving a hole moves the glass with it.
   The 1.54" board added later does not follow those figures: its holes are 2 mm at a 2.5 mm inset,
   so the uniformity is within a board rather than across the class, and the test says so.
6. **Where the Stick's pad labels go** — **decided: nowhere.** The front face carries no
   silkscreen, matching the board, which prints its pad names on the back; node-name tooltips and
   the netlist already carry the names. With the labels gone the LED row takes the real layout —
   eight 5 mm packages on the 51.1 mm board — and the field is derived rather than tuned to leave
   text room, so `LED_FIELD_INSET` is gone.

   The row was respread afterwards. The packages had been packed edge to edge at exactly one
   package width; they are now spaced evenly between the two pad columns, and the field starts
   clear of the header block `drawPinHeader` paints around each pin. That block reaches a pin width
   plus a pixel either side of the control point and is drawn after the LEDs, so deriving the field
   from `PAD_INSET` alone left it covering 1.14 mm of the first and last package — measured off the
   render, not guessed. The Stick is also drawn lit now, off the same shared colour wheel as the
   Ring, so the two read as one family of part.
7. **i18n** — component display names and enum labels are plain strings, absent from
   `diylc-swing/src/main/resources/lang/*.txt`, as they are for the shipped boards. No translation
   work is required here; noted for a future pass.
8. **`CharacterLCD`'s geometry could not be sourced consistently** — **now resolved except the
   mounting holes.** This is why item 6 was done for every other file and blocked for this one. "1602" names a character format, not a mechanical
   part, and vendors ship several outlines under it: the enum encodes an 80 × 36 mm module, while
   Raystar's RC1602A is 84 × 44 mm. The display window is worse, because sources quote different
   features under similar names — 64.5 × 16 mm and 66.0 × 16 mm as "viewing area" against
   56.2 × 11.5 mm as "active area" for the 1602, and 84 × 31 mm against 76 × 26 mm for the 2004.
   **No mounting-hole positions were found for any of them.** The render confirms this is a visible
   defect rather than a tidiness one: the bezel currently occupies roughly 70% of the board height
   when a 16 mm window on a 36 mm board should be under half. What would unblock it is picking one
   specific part — as the TFT slice settled on the MSP2807 — and taking the outline, window and
   hole positions from that one drawing, rather than assembling them from whichever vendor answers
   a search first.

   **The outlines are now maintainer-given: 80 × 35 mm for the 16x2, 98 × 60 mm for the 20x4.**
   That settles the first of the three figures this item needs, correcting the 16x2's height by a
   millimetre and confirming the 20x4 exactly as stored. It does **not** unblock prep item 6. The
   window and the mounting holes are still raw pixels with no source behind them: the bezel is
   derived as `boardH - 65` px, which on the corrected board still fills about 76% of its height
   when a 16 mm window on a 35 mm board should be under half. A one-millimetre change to the board
   cannot fix a bezel whose size does not come from the board at all. What is still wanted is the
   display window and the hole positions, ideally from one named part.

   **The window figures have since landed.** The 16x2 carries a 72.2 × 24.1 mm bezel around a
   64.5 × 14.5 mm lit area; the 20x4 a 77 × 25.5 mm bezel around 70.4 × 20.4 mm. Both are fields on
   `LCDSize` now, drawn centred on the board and within the bezel respectively. That is the actual
   fix rather than a retuning: the window is the part's own size instead of a margin subtracted
   from the board, so it no longer grows when the board does — which is why the earlier 1 mm board
   correction could not have helped it.

   **The hole inset has since been measured: 2.5 mm from each edge.** Note that this was a
   correction rather than a confirmation — the constant held the draft's 2.54 mm, inherited from
   the raw `20` px, and 2.54 is a suspiciously convenient number to inherit because it is exactly
   0.1", so it had the shape of a value chosen for the grid rather than for the part.

   **The diameter has since been measured too: 3 mm**, replacing the draft's 2.54 mm. That closes
   this item outright and leaves `CharacterLCD` with no unsourced geometry anywhere in it.

   The proportion is worth recording, because it looks wrong until it is checked. A 3 mm hole
   centred 2.5 mm from an edge leaves just 1 mm of board outside it and reaches 4 mm inward, which
   on the 16x2 overlaps the bezel's horizontal span. It does not actually collide: the holes are at
   the corners, spanning 1-4 mm vertically, while the bezel band starts at 5.45 mm on the 16x2 and
   10.35 mm on the 20x4. Both clear, in the one axis that matters.
9. **How many pads does a NeoPixel Ring have?** — **decided, from the maintainer: four on the
   12-LED ring, six on the 16 and the 24.** The guess recorded here, that the 2014 "extra ground and
   power breakout" made six universal, was half right — the two larger rings gained the extra pair
   and the 12-LED one did not, which is why no single count could be found for "the" ring.

   The more consequential half of the answer was the **arrangement**, which no source stated and
   which the component had wrong: the pads are not grouped together. Each sits toward the outer
   edge, in the gap between two consecutive LEDs, and those gaps are uneven:

   - 12 — `OUT`, 2 LEDs, `IN`, 4, `GND`, 2, `PWR`, 4 (wrapping)
   - 16 — `IN`, 2, `OUT`, 5, `G`, 1, `G`, 4, `V+`, 1, `V+`, 3 (wrapping)
   - 24 — `OUT`, 2, `IN`, 8, `G`, 2, `G`, 2, `PWR`, 2, `PWR`, 8 (wrapping)

   `RingSize` therefore carries a pad-name array beside a parallel array of LED gaps, and the
   constructor accumulates those gaps into absolute indices and **throws if they do not sum to the
   LED count.** That invariant is the point: a mistyped sequence fails at class-load instead of
   rendering as a plausible but wrong ring. Duplicate pad names are disambiguated as `G_1`/`G_2` and
   `V+_1`/`V+_2`, which keeps them distinct nets while `getDisplayPinLabel` truncates at the first
   `_` so the silkscreen still reads `G`.

   This also closes §3.2 item 8. The pads no longer share the LED arc: they sit on their own radius,
   `outerR` less a 1.4 mm `PAD_EDGE_INSET`, drawn 1.6 × 1.3 mm with a 0.7 mm hole. A ring pad is far
   smaller than a 0.1" header pad, so `AbstractMakerBoard` grew three
   `getSolderPad{Width,Length,HoleSize}` hooks. That is a deliberate exception to the usual
   preference for tuning the shared appearance constants globally, taken because only the Ring wants
   these values: the defaults reproduce the previous hardcoded 22 / 16 / 7 px exactly, confirmed by
   byte-comparing every maker render before and after and finding **only `WS2812BRing` changed**.

   Because the pad is an axis-aligned rectangle, what has to clear the rim is its corner diagonal
   (1.031 mm), not its half-length — leaving 0.37 mm of board outside the furthest corner, about
   3 px at 1:1. Both the full-size renders and a pixel scan appeared to show pads breaching the rim;
   both were misreading the board's own anti-aliased edge, which registers at every angle rather
   than only at the pads. Magnifying the tightest pad on each variant 12× showed unbroken board
   between every pad and the rim.
10. **`SevenSegmentDisplay`'s pin arrays** — **decided: the maintainer confirms they are correct.**
    Three questions closed together. The arrays are right as they stand; the two bare four-digit
    packages **share a pinout**; and each variant is now pinned to a named part — **5161AS** for the
    single digit, **3641AS** for the 0.36" four-digit package and **5641AS** for the 0.56" one.

    That also retires the suspicion recorded here before. The worry had been that the 12-pin array
    followed a 0.36" part while the dimensions described a 0.56" one, pairing one part's pins with
    another part's body. Since the two parts share pins, that pairing was never the error it looked
    like — though the dimensions themselves are a separate question, still open in §11.11.

    Worth being precise about what kind of confirmation this is. The order was corrected earlier
    from the maintainer's own reading of the parts, after it was found transcribed in reading order
    rather than DIP order, which had put every segment and digit common on the wrong pin. It has now
    been confirmed as correct. No datasheet was ever obtained — both xlitx pages carry only
    electro-optical tables and both linked PDFs are image-encoded — so this rests on the
    maintainer's word rather than on a published pinout. That is good enough to close the item, and
    it is a different claim from "verified against a datasheet".

    Naming the parts matters beyond this item. It is the move that settled the TFT on the MSP2807,
    it is what §11.8 still needs for `CharacterLCD`, and it turns the gap left in §11.11 from
    unanswerable into merely unmeasured.

    Note that replacing the digit glyph with the standard unit-grid construction did **not** touch
    this. The glyph governs how a lit segment is shaped; the pin arrays govern which physical pin
    that segment is wired to. The first is now on a documented footing and the second is not, and
    it would be easy to read the file and assume otherwise.

    The enum split does not touch it either, but it does improve the odds of settling it. Both bare
    four-digit packages share `PIN_NAMES_4DIGIT`, on the assumption that a family differs in size
    rather than in pin function — so **one datasheet now corrects both variants at once**. If that
    assumption turns out to be wrong, the arrays have to separate, and that is itself worth knowing.
    The suspicion recorded above, that the array follows the 0.36" part while the dimensions
    described the 0.56" one, is precisely what having both variants is meant to resolve.
11. **What the enum split left unsourced.** The split itself is sound; what follows is geometry that
    should not be mistaken for measurement.

    *A second round of maintainer figures has since landed.* The 5161AS body is 12.7 × 19.0 mm, a
    0.1 mm correction to the stored width; the 5641AS body (50.3 × 19.0 mm) and digit pitch
    (12.7 mm) are confirmed exactly as stored; and the TM1637 module's dimensions are confirmed as a
    set. That leaves a narrower gap than before but not an empty one: **every digit width is still
    unmeasured** — 5.2 mm on the 0.36" part and 8.1 mm on both 0.56" ones — and the 5.2 mm is still
    the only figure in the file invented outright rather than merely unchecked.

    *The apparent height conflict is resolved, and was never about the part.* The 5161AS is a
    0.56" part, the enum's 14.2 mm stands and so does the label. The 12.7 mm figure is the size the
    digit is deliberately **drawn** at, held under the true one so the glyph keeps clear of the
    pins — the same trade `DIGIT_DRAW_SCALE` exists for.

    **No value changed, because the existing mechanism already does it.** At a scale of 0.88 a
    14.2 mm digit draws at 12.50 mm, within 0.2 mm of the half inch intended. Forcing 12.7 exactly
    would mean either raising the scale, which is global and would grow the 0.36" and TM1637 digits
    too, or writing 12.7 into `digitHeight`, which would apply the shrink twice and draw at 11.2 mm.
    The separation the class already draws — `DisplayType` holds what the package measures,
    `DIGIT_DRAW_SCALE` holds the departure from it — is exactly the distinction the conflict
    collapsed, which is why reading the drawing code dissolved it rather than deciding it.

    *The 0.36" variant is now mostly sourced.* Its body (30 × 14 mm), digit pitch (7.5 mm) and row
    spacing (0.4", 10.16 mm) all came from the maintainer, and its digit height is definitional,
    because 9.14 mm is what 0.36 inches means. **One figure remains invented: the digit width
    (5.2 mm)**, derived by applying the 0.56" part's 8.1 : 14.2 ratio to a 9.14 mm digit — and the
    0.56" digit figures it borrowed from are themselves still unmeasured, so it rests on an
    unchecked foundation. It is at least
    not contradicted by the rest: four digits on 7.5 mm centres span 22.5 mm, which with one digit
    width leaves about 1.15 mm clear at each end of a 30 mm body. Worth replacing with a measurement
    rather than leaving as an inference — and cheaper to settle now that §11.10 has pinned this
    variant to the 3641AS and the 0.56" one to the 5641AS, so there is a specific part to measure
    rather than a generic four-digit display.

    **Decided: left as inferences, deliberately.** The digit widths are not being measured. They
    size the drawn glyph and nothing else, and the glyph is already a declared departure from the
    package at `DIGIT_DRAW_SCALE` 0.88, so an error here is cosmetic and bounded — unlike a wrong
    pin array, which is silent and wrong in the netlist, or a wrong outline, which is wrong against
    the board around it. The three figures stand as recorded: 5.2 mm inferred for the 0.36" part
    and 8.1 mm unverified for both 0.56" ones. They stay flagged here precisely so that a later
    reader does not mistake them for measurements and build something on top of them, which is how
    the 5.2 mm came to exist in the first place.

    For the record, the values this replaced were mine and were fitted rather than measured — a
    30.2 × 14.0 body, an 8.0 mm pitch, and a 12.7 mm row spacing chosen only so the rows would sit
    inside the body after the shared 0.6" value left them hanging past its edges.

    *The TM1637's header position is fixed.* Its single row of four pins had been centred on the
    body, putting it over the display bezel; it now sits 2.5 mm above the bottom edge on bare board,
    via `HEADER_EDGE_OFFSET`. Worth knowing that many real modules carry that header on the **left
    edge, vertical**, which would mean laying the single-row case out vertically rather than
    horizontally — a larger change that was considered and not made. The centring predated the
    split; what made it visible was rendering each variant rather than only the default one.
12. **Common cathode vs common anode** — **decided: a `Common` property, reaching the BOM only.**
    Both kinds are sold side by side and are identical in pinout, package and dimensions; the
    difference is internal polarity. So the property changes nothing that is drawn and nothing in
    the netlist — it exists to split two genuinely different parts into two BOM rows, which is
    exactly what §11.1's `getVariantLabel()` hook was built for. A cell now reads
    `1-Digit 0.56", Common Cathode`, following the comma pattern `CharacterLCD` already uses for its
    two-property variant.

    Three details settled with it. The default is **cathode**, matching the AS-suffixed parts named
    in §11.10. The **TM1637 omits it**, because the module drives the digits itself and brings out
    no common pins, so polarity is not part of what you order and its cell stays
    `4-Digit TM1637 Module`. And the **pin names are untouched**: `COM1`/`COM2` and `D1`-`D4` are
    physical designations identical on both polarities, so renaming them by polarity would have
    left the single-digit and four-digit parts labelling their pins on different principles for no
    gain.

    One thing deliberately not done: putting the part number in the value. That would be the most
    directly orderable form — `3641AS` against `3641BS` — but it rests on `AS` meaning cathode and
    `BS` meaning anode for this family, which has **not** been confirmed from a datasheet. Spelling
    the polarity out carries the same information without depending on an unverified convention.

    Adding the field now rather than after 6.7.0 is deliberate: the class has not shipped, so a new
    serialized field costs nothing in file-format terms. It is still defaulted in the getter rather
    than only at the field, per the house rule for fields that may deserialize as null.
13. **Decimal points and the colon** — **decided: a `Punctuation` property, drawn and in the BOM.**
    The parts are sold in every combination, and the component had been drawing one fixed
    combination — a decimal point on every digit *and* a colon — on all four variants at once.

    That combination is not merely a default, it is one a twelve-pin package cannot have. `A`-`G`
    plus `DP` is eight segment lines and `D1`-`D4` is four commons, which accounts for all twelve
    pins confirmed in §11.10, leaving nothing to drive a colon independently: on a real part the
    colon shares the decimal point's line or replaces it. So the drawing depicted a part that its
    own pinout rules out. The property describes the face of the package rather than a fifth pin,
    which is why it does not touch `PIN_NAMES_4DIGIT`.

    `Punctuation` has four values — none, decimal points, colon, both — defaulting to decimal
    points, which is what the committed twelve-pin array describes. It is **not** switched
    automatically when `DisplayType` changes, for the reason recorded in §11.2: per-variant
    defaults applied from a variant setter silently discard a choice the user has already made.
    The colon is ignored on the single-digit package, following `boardColor` on the bare packages
    and `Common` on the TM1637.

    One implementation trap worth recording. Suppressing a decimal point is **not** a matter of
    passing `"8"` instead of `"8."`: the glyph routine draws an *unlit* dot whenever no lit one is
    asked for, so the obvious change leaves a grey dot behind and looks correct at a glance. Both
    the lit and the unlit path are gated on the new flag.

    It reaches the BOM for the same reason `Common` does — it changes which part you order — so a
    cell now reads `4-Digit 0.36", Common Cathode, Colon`. That is three tokens, and the value
    column is getting long; worth revisiting if a fourth ever appears.
