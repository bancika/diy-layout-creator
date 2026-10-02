# Maker Peripherals

Releasing the staged `Displays & Outputs` components, and closing the gaps that would stop
someone drawing a common display.

Status: proposed
Scope: `diylc-library`, package `org.diylc.components.displays`
Companion to: `maker-board-roadmap.md` (which covers `org.diylc.components.micro` only)

## 1. Goal

The first maker slice shipped ten controller boards in 6.5.0. Forty-one further components sit in
the tree with their `@ComponentDescriptor` annotations commented out, across four packages:
`displays` (7), `sensors` (9), `modules` (21) and `robotics` (4). None of them is discoverable, and
`MakerComponentsTest` lists all forty-one in `unreleasedMakerComponentClasses`.

This document plans the release of the **seven display components**, the fixes they need first, and
the additions that make the category cover the market rather than sample it. Sensors, modules and
robotics are deliberately deferred; §10 records why.

## 2. Decisions that govern this slice

| # | Decision | Choice | Rationale |
|---|---|---|---|
| D1 | Which package ships second | **`displays` only.** | Displays are archetype-complete in a way sensors are not: the hobby world has roughly seven display archetypes and the drafts already cover all seven, whereas the nine drafted sensors are a sample of hundreds with no canonical members. Displays also reach the *existing* guitar and amp audience — 7-segment counters, character LCDs and addressable strips appear in builds with no microcontroller anywhere. |
| D2 | How coverage grows inside an archetype | **A `Version`-style enum on the existing class where the footprint family is shared; a new class where it is not.** | Same rule as the board roadmap's D2. `TFTDisplay` is now the only class with no variant enum at all, which makes it the largest structural gap left in the package; `LEDMatrix` gained its `Modules` enum in §6.4. |
| D3 | Where dimension and pin data live | **Hardcoded `Size` constants and `String[]` arrays in the component class,** per-variant where a variant changes them. | Matches every shipped board. No pin-definition resource is introduced. |
| D4 | Screen content | **A neutral dark panel, with at most a small part-number silk.** No faked user interface. | Four drafts currently render English demo text ("HELLO WORLD! 16x2", "OLED DISPLAY") at fixed font sizes, which does not scale with the board, bakes a language into a drawing, and dates quickly. A dark glass panel is what the part looks like when it is not powered, which is how every other component is drawn. |
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

Seven classes, all `category = "Displays & Outputs"`, all `bomPolicy = SHOW_ONLY_TYPE_NAME`, all
`enableCache = true`, all extending `AbstractMakerBoard`.

| Class | Variants today | Assessment |
|---|---|---|
| `SevenSegmentDisplay` | 1-digit 10-pin, 4-digit 12-pin, TM1637 module | **Best-*written* file in the set**, which is not the same as the best-sourced one. The segment-mask table is genuinely good, the 10-pin structure is corroborated, and the glyph itself is now the standard unit-grid construction: an upright 10 × 18 box with segments two units thick, mitred at 45 degrees so neighbours meet at a shared vertex, each outlined in the face colour so that they still read as separate segments. That replaced a set of hand-tuned polygons and retired both `SEGMENT_GAP` (the construction separates by outline rather than by gap) and `SLANT_DEGREES` (it is drawn upright, as the source is). What remains unchecked is everything *around* the glyph. The per-package dimensions now live on the `DisplayType` constants rather than in scattered branches, and the enum has been split so that the two bare four-digit packages — 0.36" and 0.56" — are separate variants sharing one pin array. The 0.56" set is what this class has always carried and remains unverified; the 0.36" set is now largely maintainer-supplied — body 30 × 14 mm, 7.5 mm digit pitch, 0.4" row spacing — leaving only its digit width inferred. See §11.10 and §11.11. |
| `WS2812BRing` | 12 / 16 / 24 LED | **Good.** Genuine polar maths, mm-based OD/ID per size, LED count driving the rendering, `drawSolderPads` rather than a header — correct for a ring. Its diameters, pad count and pad placement have all since been corrected from Adafruit's pages and the maintainer's measurements; the pads now sit on their own radius near the rim, each in one of the real uneven gaps between LEDs (§11.9). The LED packages are the real 5 mm 5050 part, shared with the Stick as `AbstractMakerBoard.RGB_LED_SIZE` — they had been drawn at a clamped 8-15 px, barely 1-2 mm — and the ring renders lit, in a continuous yellow-orange-red-purple-blue-green gradient with a palette per variant. |
| `CharacterLCD` | 16x2 / 20x4, I2C backpack / 16-pin parallel | Correct HD44780 and PCF8574 pin data. The bodies are now maintainer-given at 80 × 35 and 98 × 60 mm — the 16x2 height had been 36, so "correct bodies" was never true of it. The screen geometry was raw pixels — an eyeballed `bezelMarginX = (16x2) ? 45 : 40` and a window derived as a margin off the board — and is now measured: a 72.2 × 24.1 mm bezel around a 64.5 × 14.5 mm lit area on the 16x2, 77 × 25.5 around 70.4 × 20.4 on the 20x4, carried on `LCDSize` and centred rather than subtracted. Headers are placed from measurements too (§3.2 item 7). No pixel literal survives in `draw()`; only the mounting holes remain unsourced (§11.8). The `Font` it once allocated inside `draw()` went with the demo text in item 7; the one that remains is in `drawIcon`, which runs per toolbox icon rather than per repaint. |
| `OLEDDisplay` | I2C 4-pin / SPI 7-pin | Correct pin names, and the body is now the measured 26.7 × 19.3 mm panel rather than a 27 mm square. The glass had been drawn near-square against what is really a 2:1 letterbox; the lit area is now 21.744 × 10.864 mm, which is exactly 2:1, so that complaint is closed. What remains is the original gap: no size variants — the 0.91" and 1.3" boards are §6.3. |
| `LEDMatrix` | Single 8x8 / Compact 8x8 / 4-in-1 32x8 | Plausible single MAX7219 module with correct cascade headers, but a hardcoded `-44 mm` output-header offset and pixel-placed matrix and chip. Both are gone: the modules are the real 32 × 32 mm part drawn flush with the board, and the header rows sit one clearance in from their own edges with the spacing derived from the board rather than fitted. Whichever headers the modules cover are drawn before them and painted over, as on the real part, so the display face stays clean while the pins stay wireable (§3.2 item 9). §6.4 added the other two: the compact 8x8, which fits its driver under the module so the board is only the module (32 × 32 mm) and several can be butted together, and the 4-in-1 (128 × 32 mm, four flush modules), both standing their headers vertical on the short edges with every driver hidden. Carrying boards other than an 8x8 is what retired the `8x8` in the old class name. |
| `TFTDisplay` | none | Was the weakest file, on two counts that are now both gone: a footprint bug, and a descriptor reading 240x320 against on-screen silk reading `320x240`. The footprint was rebuilt from the module drawing (§5 item 3) and no `320x240` string survives anywhere in the class. What is left is the gap it always had and nothing worse: no variant enum, which is §6.2's job. |
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
3. **Fonts allocated inside `draw()`** in `CharacterLCD` (`new Font("Monospaced", ...)`) and
   `WS2812BRing` (`new Font("SansSerif", BOLD, 8)`). `draw()` runs on every repaint; the convention
   is a cached static.
4. **Non-ASCII characters** in `SevenSegmentDisplay` and `WS2812BStick`. Source encoding is
   ISO-8859-1 and all ten shipped `micro` classes are clean, so this arrived with the drafts.
5. **Unused `java.awt.Font` import** in `LEDMatrix`.
6. **The NeoPixel Ring's pad labels collide** — **resolved by dropping them.** They were drawn at
   `innerR + 5`, and because the four pads sit one pin spacing apart along the arc, each label had
   roughly 15 px of arc for 24-30 px of text. No radius or font size fixes that: a larger radius
   spreads the labels but they are anchored to pads that do not move, and a smaller font stops
   being legible long before it stops overlapping. Moving them away from their pads would defeat
   the point of a pad label, so the names are left to the node tooltips and the netlist, as on the
   Stick. The array also duplicated `PIN_NAMES` and had already drifted from it, printing `5V`
   where the node name says `+5V`.

   The centre silkscreen went the same way afterwards. A two-line `NeoPixel` / `N x LED` caption was
   drawn at the middle of the component, which on a ring is the hole rather than the board — the
   text floated in empty space, and the variant it announced is already in the BOM value, the
   property editor and the component name. Dropping it retired the class's last uses of
   `StringUtils`, `HorizontalAlignment` and `VerticalAlignment`, which were removed with it.
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
in §11.5 and §11.6. The TFT carries **four** mounting holes, all 3 mm in from the side edges, with
the pair nearest the header 6.92 mm down from that edge to clear the pin row and the lower pair
3 mm up from the bottom. They are drawn after the glass so that all four stay visible. The Stick's
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

### 6.1 NeoPixel Strip — new class `WS2812BStrip`

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

### 6.2 `TFTDisplay` variants — `Controller` enum

The biggest structural gap, and it lands on top of the §5 item 3 footprint fix, so do it in the same
pass rather than twice.

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
four. **~1.5-2 days, the largest item here.**

### 6.3 `OLEDDisplay` variants — `Version` enum

Do this in the same pass as the §5 item 5 glass fix.

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

### 6.5 Nokia 5110 LCD — new class `Nokia5110LCD`

Still in every starter kit, a distinct 8-pin footprint, and simple geometry: a board with a large
glass area and a single 1x8 header. PCD8544 controller, 84x48 pixels, `RST CE DC DIN CLK VCC BL GND`.
One class, no variants. **~0.5 day.**

### 6.6 LED Bar Graph — new class, base class to be decided

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

- **`WS2812BRing`**: append `_8_LED` and a `_7_LED` Jewel constant to `RingSize` with their OD/ID.
  Two lines plus sourcing. **~1 h.**
- **NeoPixel panel 8x8** as its own class, reusing `drawAddressableLed` and the ring's pad handling.
  Worth doing only after §6.1, whose helper it depends on. **~0.5 day.**

### 6.8 Deliberately not in this slice

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

`MakerComponentsTest` keeps its generic coverage; move the seven (then nine) classes into
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
5. **§7** tests alongside each of the above, not after.
6. **§8** regression samples and the `update.xml` block, last.

Nine palette entries at the end of it: the seven existing, plus the strip and the bar graph, plus
the Nokia and the panel if appetite holds.

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
2. **A uniform property set** — the board roadmap's §8.3. This slice adds `Version`-style enums to
   three more classes; deciding now whether a "Show Pin Labels" toggle and a consistent `Headers`
   property belong on every maker component avoids retrofitting nine more classes later.

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
3. **The bar graph's base class** (§6.6) — follow `DIL_IC` on `AbstractLabeledComponent` with a
   transformer, or extend `AbstractMakerBoard` for consistency with its category neighbours. The
   recommendation is `DIL_IC`, because the part is a DIP package and rotation for DIP parts already
   has a transformer.
4. **Whether `LedCount` on the strip should be a free integer or an enum** (§6.1). A free integer
   matches how someone thinks about a tape they will cut; an enum keeps the property editor
   predictable and bounds the control-point maths. The plan above assumes a bounded integer.
5. **The TFT's mounting holes** — **decided, from the vendor drawing.** Four holes: all of them
   3 mm in from the side edges, the pair nearest the header 6.92 mm down from that edge so they
   clear the pin row, the lower pair 3 mm up from the bottom edge. The glass is centred between the
   two hole rows rather than measured from an edge, which is what reconciles a 69.1 mm glass with
   holes only 3 mm from the bottom: its midpoint is 44.96 mm from the top, so it spans 10.41 mm to
   79.51 mm and clears both pairs. Both are derived in `draw()` from the hole constants, so moving
   a hole moves the glass with it.
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
