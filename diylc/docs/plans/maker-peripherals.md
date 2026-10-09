# Maker Peripherals — `Displays & Outputs`

Status: **13 components shipped**, in the as-yet-unreleased `6.7.0` block of
`diylc-core/src/main/resources/update.xml`.
Scope: `diylc-library`, package `org.diylc.components.displays`.
Companion: `maker-board-roadmap.md`, which covers `org.diylc.components.micro`.

Nothing in this package has been in a release, so defaults, control-point counts and serialized
field names can still be changed freely. **That stops the moment 6.7.0 ships.**

## 1. What is in the palette

| Class | Variants | Other properties |
|---|---|---|
| `CharacterLCD` | 16x2, 20x4 × I2C backpack, parallel HD44780 | Backlight Color, Screen |
| `OLEDDisplay` | 0.96" SSD1306 128x64, 0.91" SSD1306 128x32, 1.3" SH1106 128x64 × I2C, 7-pin SPI, 6-pin SPI | Pixel Color, Screen |
| `TFTDisplay` | 2.8" / 2.4" ILI9341, 0.96" / 1.8" ST7735, 1.54" ST7789, 1.28" GC9A01 round | Screen |
| `Nokia5110LCD` | single part (PCD8544 84x48) | Backlight Color, Screen |
| `SevenSegmentDisplay` | 1-digit 0.56", 4-digit 0.36", 4-digit 0.56", 4-digit TM1637 module | Common, Punctuation, LED / Board / Body colour |
| `LEDMatrix` | MAX7219 single 8x8, compact 8x8, 4-in-1 32x8, 8-in-1 64x8 | Dot Color |
| `LEDBarGraph` | 8, 10, 12 segments | Orientation, body / border colour |
| `WS2812BBreakout` | single LED | — |
| `WS2812BStick` | 8 LEDs | — |
| `WS2812BJewel` | 7 LEDs | — |
| `WS2812BRing` | 12, 16, 24 LEDs | — |
| `WS2812BStrip` | 30 / 60 / 144 LED·m⁻¹ × 1–144 LEDs | — |
| `WS2812BPanel` | Adafruit 8x8 NeoMatrix | — |

Twelve extend `AbstractMakerBoard`. `LEDBarGraph` is the exception: a DIP-outline part, so it
follows `DIL_IC` on `AbstractLabeledComponent` with its own transformer and inherits none of the
maker helpers. It is still in this category because that is where someone looks for a bar graph.

## 2. Conventions this package follows

These are the rules worth knowing before touching anything here. Most were learned by getting them
wrong once.

**Variants.** A `Version`-style enum on the existing class where the footprint family is shared; a
new class where it is not. The test is structural, not cosmetic: `WS2812BJewel` is a separate class
from `WS2812BRing` because it is a disc rather than an annulus, with a centre LED and its own pad
arrangement — five variant-specific structures is a different part.

**Figures live on the variant.** Hardcoded `Size` constants and `String[]` arrays in the component
class, per-variant wherever a variant changes them. No pin-definition resource, and no pixel
literals in `draw()` — every dimension derives from a `Size`.

**A different silkscreen is not a variant; a different pin count is.** The same two pads on an
SSD1306 carry the clock and data lines in both interfaces — the module's BS jumpers choose the
protocol, not the pins — so `SCL`/`SCK`/`CLK`/`D0` are one pin and `SDA`/`MOSI`/`D1` are another,
and boards that print them differently are the same board. Node names pick one spelling and move on.
A 6-pin SPI board is the opposite case: its chip select is tied low on the PCB rather than brought
out, so it cannot share a bus, and that is a variant because it reaches the netlist.

**Say whether a figure is measured or derived.** Active areas computed from a nominal diagonal at a
panel's aspect ratio are derivations, and are commented as such so nothing is built on them. Where a
part's own measurement contradicts a derivation the measurement wins and carries its own field — but
check first that it really contradicts it. A supplied figure sitting within half a millimetre of
what an existing rule already produces is more likely a reading than a departure, and has twice
turned out to be one; query it before it earns a mechanism.

**Orientation is sourced per variant, never standardised.** Which edge a board's header leaves by is
a fact about the part. Control point zero is always the leftmost pin, so with the pin order fixed a
top header and a bottom header are mirror images, not the same board turned round. `TFTDisplay`
divides three and three, and every constant states its own edge — the short constructor takes the
flag too, so none can inherit a default. Standardising this family onto one orientation was tried
and had to be undone: it had moved two boards away from the truth.

**Do not let one property stand in for another.** `isRound()` was serving as "the header is at the
bottom" and broke the moment a rectangular board had a bottom header; `isDualRow()` was serving as
"this is a bare package". Carry both questions explicitly even while the answers agree.

**Pins are drawn and covered, never skipped.** `drawPinHeader` is what registers a conductive area,
so a header hidden under a module or a moulding is drawn first and painted over. Leaving the call
out looks identical and silently drops those pins from continuity and the netlist.

**Silk where it fits, nowhere else.** Pin names go through `getSilkPinLabel`, so a name is written
once. A row with no clear strip beside it gets no labels rather than crowded ones — the matrix's
covered rows, the LCD's I2C backpack and the TFTs' SD rows all go unlabelled deliberately. A
`SILK_NAMES` array exists only where the node name is actively wrong on the board, not merely long.

**The glass prints the part's own spec.** `Display.VALUE` puts the variant string on the lit area,
generated from the properties rather than typed, carrying no prose and no language. `Display.NONE`
gives the unpowered part. No invented demo content, ever.

**BOM.** Every class sets `bomPolicy = SHOW_ONLY_TYPE_NAME` and returns the variant string from
`getVariantLabel()`, because `BomMaker` groups rows on type name plus value — without it every
variant of a class collapses into one line and the BOM cannot say which part to buy.

**Shared arrays are shared by identity.** Where two variants have the same pinout they reference one
array constant and the test asserts `assertSame`, so a correction to one reaches both. Two copies
that happen to match would pass every name check and drift the day one is corrected.

**Nested types at the bottom of the file.** Where a variant needs more than a couple of extra
figures they go into a nested value class rather than onto the enum constructor —
`OLEDDisplay.Layout`, `TFTDisplay.Glass`, `SevenSegmentDisplay.Module`. A constructor past about
fifteen arguments is the signal.

## 3. Figures that are not measurements

Kept visible so no one mistakes them for sourced data and builds on them.

- **`SevenSegmentDisplay` digit widths** — 8.1 mm on both 0.56" packages (unverified) and 5.2 mm on
  the 0.36" (inferred from the 0.56" ratio, so resting on an unchecked figure). **Deliberately left
  as inferences:** they size the drawn glyph and nothing else, and the glyph is already a declared
  departure from the package at `DIGIT_DRAW_SCALE` 0.88. An error here is cosmetic and bounded,
  unlike a wrong pin array.
- **`SevenSegmentDisplay.Module` hole figures** — 1.778 and 1.524 mm, which are the 14 and 12 pixel
  offsets the class drew before they were figures at all, converted exactly so the drawing did not
  change.
- **`Nokia5110LCD`** — the 3.5 mm clearance from the bottom edge to the pin row.
- **`WS2812BStrip`** — tape width (10 mm / 12 mm) is a stated norm rather than a measurement, and
  the end-pad lead-in derives from the pad and package footprints.
- **`TFTDisplay`** — the SD rows' horizontal placement is centred across the board by derivation;
  only their offset from the edge was given.
- **`OLEDDisplay`** — the 6-pin SPI boards borrow the 7-pin boards' outlines. A 6-pin module is a
  PCB of its own with one fewer pad, and nothing but its outline would say whether it matches.

## 4. Testing

Per-class tests in `diylc-library/src/test/java/org/diylc/components/displays/`, using
`MakerBoardTestSupport` (`assertRow`, `assertRowSpacing`, `assertBoardSize`, `assertDrawsCleanly`,
`assertScreenTextIsDrawn`, `renderPixels`).

What these tests are for, beyond the obvious count-and-dimension checks:

- **Assert relationships, not transcriptions.** A board is exactly as wide as its modules; a glass
  clears the mounting holes; no hole is drilled where a pad is; a lit area that is measured really
  does disagree with the rule it replaced. A test that restates the constant passes while the
  component is wrong.
- **No duplicate node names,** on every variant. The cost is legibility, not connectivity: nodes are
  identified by component and control point index and the netlist graph is keyed on position, so two
  pads named `GND` stay two nodes that merely print the same, and a reader cannot tell which one a
  connection reached. They are invisible on the canvas too, since `getDisplayPinLabel` cuts the
  suffix off and both silk as `GND`. `Teensy` is the one board that still repeats a bare name.
- **A drawing smoke test** over every variant, state and orientation.
- When a refactor is meant to change nothing, **render before and after and compare pixels.** The
  `digitCount` work was verified that way, and it caught a sub-pixel hole shift the unit tests
  missed.

`MakerComponentsTest` carries the generic discovery and icon coverage. A class ships by moving from
`unreleasedMakerComponentClasses` to `releasedMakerComponentClasses` and uncommenting its
`@ComponentDescriptor`.

## 5. Release

The slice is worth shipping on its own: it is archetype-complete, part of it (7-segment, character
LCD, bar graph, addressable strips) reaches builds with no microcontroller in them, and the slices
after it are large and differently shaped, so batching would mean a much later and far less
reviewable release. A release is also the only validation loop that reaches people who own the
parts.

### 5.1 What shipping makes permanent

Only this list needs checking beforehand, because only this list is a contract afterwards:

- class names, and serialized field names and types
- **control-point counts**
- **control-point positions relative to point 0** — these are stored per instance, so moving a pin
  relative to the body misplaces every wire in an existing file

Everything else is fixable in a later release without breaking a file: bezel placement, colours,
silk, hole positions, digit widths. Every figure in §3 falls here, which is why none of them blocks
a release.

### 5.2 Pre-release checklist

1. **Audit the pin layout of all 13** — count, node names, and each pin's position relative to the
   body. Not the cosmetics; those are §5.1's second list.
2. **Settle `TFTDisplay`'s 2.8" SD row.** It is the least verified thing in the package: the row was
   added on the argument that the "(Touch + SD)" label implied a missing header, and its 3.0 mm
   offset was read from "the same offset as the bottom row" rather than measured. Four control
   points that become permanent.
3. **Re-check `TFTDisplay` generally.** It is where the churn concentrated — both ILI9341s went
   14 to 18 control points, and the 0.96", 1.8" and 1.54" each had every pin move relative to the
   body when their orientation was corrected.
4. **Render a contact sheet of all 13 and look at it.** Cheap, and it caught several real errors
   that the unit tests passed straight over.

Regression samples (§6.3) are deliberately *not* on this list. They protect against future change,
which is what a released contract makes dangerous — so they are the first task after shipping, not
a blocker on it.

### 5.3 Release notes

One `NEW_FEATURE` per palette entry in the `6.7.0` block, part name then variant list. **A released
block is never edited.** A new variant of a component that is new in the same block goes onto that
component's existing line rather than being logged as an `IMPROVEMENT` — there is no earlier release
for it to improve on.

## 6. Next steps

### 6.1 Parts worth adding, ranked by how often they appear in builds

1. **1.3" 240x240 ST7789**, and the 1.14" and 2.0" ST7789 the Pico ecosystem buys. `TFTDisplay` now
   absorbs offset panels, either header edge and second rows, so each is one `Controller` constant.
   Needs per board: outline, header offset and edge, pin names in printed order, hole inset and
   drill, bezel height, and whether the lit area is centred or measured.
2. **MAX7219 8-digit 7-segment module** — the clock and counter staple, and this package already
   models a MAX7219 cascade for the matrix. Unblocked: `DisplayType` now carries digit count, pin
   array and module board figures, so this is a constant.
3. **6-digit TM1637 and 2-digit 0.56"** — the rest of the clock and thermometer range. Same.
4. **E-paper, 1.54" / 2.13" / 2.9" SSD1680.** Once judged niche; that call is stale, as ESP32 badge
   builds and the Waveshare and Good Display modules made it ordinary. The flexible-cable mounting
   is real new drawing work, which is the only reason it is not higher.
5. **WS2812B 16x16 and flexible 8x32 matrices** — the one addressable form the package lacks.
6. **The bare WS2812B 5050 LED**, as against a breakout. People solder these directly.
7. **16x4 character LCD.** Also the RGB-backlight 1602, which is *not* the free colour default it
   looks like: it is an 18-pin part whose three backlight anodes are real pins, so it is an
   `LCDInterface` constant with its own array.
8. **Nixie tubes — IN-12, IN-14, B7971.** The strongest audience overlap on this list: DIYLC already
   has a `Tubes` category, and the people in it build Nixie clocks.
9. **2.42" SSD1309 OLED**, 14- and 16-segment alphanumeric, HT16K33 backpacks. Real, second tier.

Explicitly rejected: a tint property on `TFTDisplay`. A 240x320 colour panel has no tint to set, and
offering one invites a drawing that says something false about the hardware.

### 6.2 The "& Outputs" half of the name is unfulfilled

Every member of this category emits light. **There is no analog panel meter, no VU meter, no speaker
or driver, and no magic-eye tube anywhere in the library** — `misc/Buzzer` and
`electromechanical/PilotLampHolder` are the whole non-display output range.

This is a bigger gap than anything in §6.1, and it reaches the audience DIYLC already has: a VU
meter and a loudspeaker appear in amp and effect builds containing no microcontroller at all,
`Dial Scale` is already next door in `misc`, and a moving-coil meter is a simpler drawing than any
display here. Whether these belong in this category or in `Electro-Mechanical` is not an interesting
question; that they are absent is the finding.

### 6.3 Regression coverage — the standing risk

**No `.diy` file among the 1418 in `diylc-regression-data` references `org.diylc.components.micro`
or `org.diylc.components.displays`,** so 23 components have no rendering or netlist baseline. Unit
tests guard the figures we chose; they cannot catch a pin dropping out of the netlist or a component
misbehaving in a real project. Samples were declined for this slice; the case is stronger now that
orientations, pin arrays and geometry have all moved.

### 6.4 Later slices

**`sensors` (9 drafts).** Ship the four that are the unrivalled answer in their function — HC-SR04,
DHT11/22, PIR HC-SR501, MPU6050 — and leave the five commodity KY-style three-pin boards, which
carry most of the sourcing cost for the least distinctiveness. **Do not** collapse the tail into a
parameterised "three-pin sensor module": that is the generic configurable board that was already
built and reverted, and it fails for the same reason — people search the palette for a product name.

**`modules` (21 drafts).** The largest and least homogeneous batch, and the worst geometry drift in
the tree (`ULN2003Driver` has 24 raw pixel offsets, `PCA9685ServoDriver` 22, `L298NMotorDriver` 18).
Split by function rather than shipping as one category. Settle one overlap first: the drafted
`ActiveBuzzerModule` (KY-012) against the existing `misc/Buzzer`.

**`robotics` (4 drafts).** Blocked on an architectural decision, not on part selection.
`AbstractMotor` is a parallel fork of `AbstractMakerBoard`, re-implementing `rotatePoints`,
`drawPins`, `getFinalBorderColor` and `getCachingBounds` plus its own `PIN_COLOR`, `PIN_SIZE` and
silk fonts. Four components do not justify a second hierarchy: either lift the shared behaviour into
a common base or take the duplication deliberately.

**i18n.** Component display names and enum labels are plain strings, absent from
`diylc-swing/src/main/resources/lang/*.txt`, as they are for the shipped boards. No work required
here; noted for a future pass.
