# Diode Datasheets

Applying a known diode's package and appearance from a datasheet.

## 1. Goal

Follows [DIP IC Pin Names](dip-ic-pin-names.md), [Transistor Pinouts](transistor-pinouts.md) and
[Tube Socket Datasheets](tube-socket-datasheets.md). A diode's terminals are always anode and cathode
(`A`, `K`), so unlike those components there are no pins to name. What a part number does fix is the
package - a 1N5408 is twice the size of a 1N4007 - and, for the AI, what the part is: a rectifier,
a Schottky, a signal diode, a germanium clipper or a zener. Users already type part numbers into
"Value" (in the sample corpus 1N4007 13 times, 1N4148 8, 1N5819 6), but the body keeps the default
size whatever they type. A datasheet sets the value, body size and colours in one step, from the
toolbox or from the AI, as `Resistor.datasheet` already does for resistor families.

## 2. Design decisions

| # | Decision | Choice | Rationale |
|---|---|---|---|
| D1 | Components | `DiodePlastic` and `DiodeGlass` each get a datasheet. A part goes in the file of its envelope: epoxy (DO-41, DO-15, DO-201AD) in `DiodePlastic`, glass (DO-35, DO-34, DO-7, glass DO-41) in `DiodeGlass` | The two classes are the two envelopes; the drawing differs (glass shows the die). |
| D2 | Datasheet row | Plastic: `group, part, package, length, width, body color, marker color`. Glass: the same plus `inside color`. Sizes in mm, colours as `#RRGGBB`, e.g. `Rectifier,1N4007,DO-41,4.8 mm,2.6 mm,#333333,#DDDDDD` | Every value maps to an existing property; nothing new is serialized. The package column is for readers and for a test that keeps one package one size. |
| D3 | Model identity | `enableDatasheet = true`, `datasheetCreationStepCount = 2`, `datasheetModelColumn = 1` | Toolbox groups by family, the AI names the part. |
| D4 | `applyModel` | Sets value, length, width, body colour, marker colour (and inside colour on glass). Leaves lead colour, label, display and orientation alone | The datasheet says what the part is and how big; label and display are the user's. |
| D5 | Sizes | One body length and diameter per package, the nominal (mid-range) value of the manufacturer's outline drawing, shared by every part in that package | Packages are standardized (JEDEC DO outlines); a per-part size would only add noise. A test enforces it. |
| D6 | Colours | One appearance per package and family, as the parts usually look: black epoxy with a grey band for rectifiers and Schottkys; clear glass with an orange die and a black band for silicon signal diodes and zeners; a darker die for germanium | Diodes, unlike resistors, carry no colour code; a recognisable appearance is enough. |
| D7 | Standing mode | Unchanged. A model changes "Length", and a body longer than the control-point spacing is drawn standing, as today | Existing `@AiNote`s already explain this; applying a model to a placed part can flip it to standing only if the new body is longer than the hole spacing. |
| D8 | Zeners | Listed part by part (1N4728A ... 1N4764A, the 1N52xxB and BZX55/BZX79 values DIYers use), each row naming the part; the voltage is not a column | The part number is what is printed and searched for. A zener voltage column would need a new property. |
| D9 | Equivalents | One row per part number (1N4148 and 1N914; 1N5817, 1N5818, 1N5819), as for ICs and tubes | Users and the AI look up the printed number. |

### Explicitly out of scope

- LEDs, bridge rectifiers and SMD diodes. Bridge rectifiers are a separate, smaller follow-up (shape
  and pin order per W04M / DB107 / KBP / KBPC).
- The schematic diode symbols.
- A zener voltage or diode type property.
- Translating group names.

## 3. Changes

### diylc-library

- `DiodePlastic`, `DiodeGlass`: `implements IDatasheetSupport`, descriptor attributes (D3), a
  `(String[] model)` constructor and `applyModel` (D4); an `@AiNote` on the datasheet model.
- `datasheets/DiodePlastic.datasheet`, `datasheets/DiodeGlass.datasheet`: rows per D2. Package
  dimensions from manufacturer outline drawings; each part's package from its manufacturer data
  sheet.

### Generated

- `catalog_full.json` regenerated.

## 4. Datasheet inventory

Package sizes, nominal (mid-range) body length x diameter from the outline drawings:

| Package | Size | Source | Envelope, colours |
|---|---|---|---|
| DO-41 (plastic) | 4.6 x 2.4 mm | Vishay, Diodes Inc., onsemi case 59-10, JEDEC DO-204AL | epoxy `#1E1E1E`, band `#BDBDBD` |
| DO-201AD | 8.5 x 5.1 mm | Vishay 1N5400, Diodes DS28007 | epoxy |
| R-6 (P600) | 8.85 x 8.85 mm | Diodes DS28009, Vishay P600 | epoxy |
| DO-35 | 4.0 x 1.8 mm | JEDEC DO-204AH; onsemi, Vishay, Nexperia SOD27 differ (Vishay <= 3.4 long) | glass `#E1F0FF`, band `#141414`, die `#D9652B` (zener `#E07A3A`) |
| DO-34 | 2.6 x 1.6 mm | JEDEC DO-204AG, Nexperia SOD68 | glass |
| DO-41 (glass) | 4.3 x 2.4 mm | Vishay, Nexperia SOD66, Microsemi (shorter than plastic) | glass, zener die |
| DO-7 | 6.7 x 2.45 mm | JEDEC DO-204AA only | glass, germanium chip `#5E5E5E` |
| SOD-57 | 4.2 x 3.5 mm | Vishay BYV27, NXP SOD57 (NXP ~10 % larger) | black sinterglass `#202020`, band `#B0B0B0` |

`DiodePlastic.datasheet`, 77 parts:

| Group | Package | Parts |
|---|---|---|
| Rectifier | DO-41 | 1N4001-1N4007, 1N5391-1N5399 |
| Rectifier | DO-201AD / R-6 | 1N5400-1N5408, BY255 / 6A10 |
| Fast Rectifier | DO-41 | UF4001-UF4007, 1N4933-1N4937, FR101-FR107, HER101-HER108, MUR120, MUR160, BA157-BA159 |
| Fast Rectifier | DO-201AD | UF5400-UF5408, MUR460, BY399 |
| Schottky | DO-41 / DO-201AD | 1N5817-1N5819, SB140, SB160, SR160, 11DQ04 / 1N5820-1N5822, SB560 |

`DiodeGlass.datasheet`, 239 parts:

| Group | Package | Parts |
|---|---|---|
| Signal | DO-35 | 1N4148, 1N914, 1N4448, 1N916, 1N4151, BAV21, 1N3600, BA221, 1S1588, 1S2473, MA150 |
| Signal | DO-34 | 1SS133, MA856 |
| Schottky | DO-35 / DO-34 | BAT41, BAT42, BAT43, BAT46, BAT48, BAT85, 1N5711 / BAT86 |
| Germanium | DO-7 / DO-35 | 1N34A, 1N270, OA90, OA91, OA95, OA47, AA119, AA143 / 1N60, 1N60P |
| Fast Rectifier | SOD-57 | BYV26C, BYV26E |
| Zener (1N47xx), (BZX85) | DO-41 | 1N4728A-1N4764A, BZX85C3V3-C75 |
| Zener (1N52xx), (1N7xx), (BZX55), (BZX79) | DO-35 | 1N5221B-1N5267B, 1N746A-1N759A, BZX55C and BZX79C 2V4-75 |

Sourcing notes:

- Part packages read from Vishay, Diodes Inc., Taiwan Semiconductor, onsemi, Nexperia, ST,
  Microsemi and Central sheets. Weakest: FR10x and SR160 (second-tier makers agreeing), MUR460 and
  11DQ04 (mirrors), the European germanium types (DO-7 from secondary sources; their real bodies are
  "DO-7-like"), and BYV26 (package from the BYV27 sheet of the same family).
- Same number, different envelope: 1N5391-1N5399 are DO-41 at Vishay (current) and DO-15 at TSC and
  the old Diodes sheet; BAT85 is DO-35 at Vishay (current) and DO-34 at Nexperia (discontinued);
  1N47xxA are glass DO-41 at every maker that states an envelope, so they are glass parts here.
- 1N60 / 1N60P are sold as germanium but current Asian production is often a silicon Schottky.
- Left out: BAT54 (SMD), 1N695 (secondary sources only), Soviet D9/D18 (own glass cases, not JEDEC),
  DO-15 parts (none needed once 1N539x went to DO-41).
- Japanese clippers, kept only with a pedal schematic behind them: 1S1588 (Boss SD-1, King of
  Tone), 1S2473 (Boss HM-2, early DS-1/SD-1), MA150 (TS808, TS9), 1SS133 (Boss BD-2), MA856 (King
  of Tone); all from Toshiba, Rohm and Panasonic sheets and all obsolete. Their cathode bands follow
  the maker (yellow on Rohm and MA856, white on MA150), so band colour varies per row while size
  stays per package. Dropped for lack of pedal evidence: 1S2076A, 1SS270A, 1S953, 1S1555, 1SS176,
  MA165, 1SS53.
- BZX85C follows Vishay's current range (3V3-75, glass DO-41); older makers went to 2V7 and up to
  200 V. Nexperia's 1.3 W equivalent is BZV85, Diotec's ZPY.
- Popular additions not yet sourced: SF14/SF18, MBR160, SB5100, 1N4150, 1N456/1N457, BY228/BYW96 (SOD-64).

Rules for every row: a part's package is read from its manufacturer data sheet (or two independent
sources); where makers ship the same number in different envelopes (1N47xx in glass or plastic
DO-41; germanium parts in DO-7 or DO-35), the row follows the common current production and the
other is noted.

## 5. Tests

- `DiodeDatasheetTest`, over both files: every row parses and applies; parts are unique within a
  file and appear in only one of the two; rows naming the same package carry the same length and
  width; `findSeries` finds a part and not a group.

## 6. Verification

- `mvn -q -pl diylc-library -am test`, then the whole reactor.
- Render one part per package, lying and standing, and compare body proportions with the outline
  drawings.
- Regression suite not required: no existing project changes (nothing is applied to placed parts).

## 7. Phases

1. **Plan**: this document.
2. **Code**: done - D3, D4, `DiodeDatasheetTest`, `@AiNote` on both classes. The test fails until
   the datasheets exist.
3. **Library**: done - 316 parts in two datasheets, all five `DiodeDatasheetTest` cases pass, whole
   reactor passes (core 227, library 513, swing 29), parts rendered per package; `catalog_full.json`
   regenerated; release note in 6.10.
4. **Later**: bridge rectifiers; LEDs (3 mm / 5 mm / 10 mm bodies) if wanted.
