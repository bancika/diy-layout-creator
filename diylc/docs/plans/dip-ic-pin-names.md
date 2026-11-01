# DIP IC Pin Names and Datasheet Models

Naming every pin of a `DIL_IC`, and applying a known part's pinout from a datasheet.

## 1. Goal

A DIP IC today is anonymous: its pins carry numbers only, so the netlist, the AI and the schematic
view cannot tell an op-amp output from a supply pin. This plan gives every pin an optional name, the
way `TubeSocket.electrodeLabels` already does, and then adds a datasheet of common parts (op-amps,
timers, power amps, BBDs, ...) that fills those names in. Named pins make an IC identifiable, give
the AI an accurate picture of the circuit, and are the prerequisite for converting between layout
and schematic.

Every consumer already reads names through `getControlPointNodeName`: the netlist (`Node`,
`NetlistBuilder`), hover tooltips (`Presenter`), the AI project context (`AiProjectBuilder`,
`AiEditContext`, `ReplaceHandler`, `PinoutAnalyzer`) and, on the `schem-view` branch,
`GenericBoxSchematicFactory`, which turns them into a labelled box. Changing `DIL_IC` alone is
enough for names to reach all of them.

## 2. Design decisions

| # | Decision | Choice | Rationale |
|---|---|---|---|
| D1 | Storage | New `String pinNames` field, comma-separated, in pin-number order (pin 1 first), editable as "Pin Names". `null` means unnamed | Same shape as `TubeSocket.electrodeLabels`; a plain string needs no new editor, serializes trivially and is one property for the AI to set. Old files deserialize with `null` and behave exactly as before. |
| D2 | Pin numbering | A private `getPinNumber(int index)` returns the physical pin number under the active "Pin Labels" scheme. `NO` and `NAMES` mean DIP numbering, not control-point order | Under `NO` the node name used to be `index + 1`, so on an 8-pin part the top-right pin reported `5` while it is pin `8`. That fed wrong pins to the AI and the netlist. Fixing it changes netlist text for every existing project with an IC, hence a regression run (§6). `DIP_MIRROR` and both `CONNECTOR` schemes keep their current meaning. |
| D3 | Name lookup | Node name = the name at `getPinNumber(i) - 1` if present and not blank, otherwise the pin number | Fewer names than pins leaves the rest numbered; extra names are ignored but kept in the string, so changing the pin count back restores them. Rotation and mirroring need no change because names follow pin numbers. |
| D4 | Rails and sections | The maker-board convention, already implemented by `Node.railName`: a trailing `_<digits>` marks pins of one net, anything else is a separate net. `GND_1`, `GND_2` are the same ground; `1IN-`, `2IN-` are inputs of two sections | `NetlistBuilder` joins pins of one component that share a rail name, so the name is the electrical statement. Names stay unique per pin, which keeps the netlist able to say which pad a connection reached. `NC` and `RSV` are never joined. |
| D5 | Naming style | TI style, as printed in the manufacturer datasheet: section number first (`1OUT`, `1IN-`, `1IN+`, `2IN+`, ...), supplies `VCC+`/`VCC-` on op-amps and `VCC`/`GND` elsewhere. No spaces, no commas. Parenthesized notes are allowed and stripped by `Node.sanitizeNodeName` (`CONT (CV)`) | One consistent scheme across groups; the leading digit separates sections without an underscore, so it can never collide with the rail suffix of D4. |
| D6 | Drawing | Maker-board style: each name is centred `DIL_IC.PIN_NAME_OFFSET` (1.35 mm, 2 px closer than the maker boards' offset) inside the body from its pin and runs along the row, rotated like `AbstractMakerBoard.drawPinLabels` (rows running horizontally keep names level). Formatted by `AbstractMakerBoard.getDisplayPinLabel` (`GND_1` renders `GND`, annotations dropped). Font is `DIL_IC.PIN_NAME_FONT`, 6 px. A name that would touch the previous one in its row, or run past the end of the body, is left out | Running along the row costs only the font height across the body, so the middle of a 0.3 in body stays free for the name/value label. The pitch is the limit: 0.1 in is 20 px, the maker 8 px font makes `1OUT` 22 px, 6 px fits every op-amp, timer and quad name. Longer names (`COMP/BAL`, `1THRES`) are dropped individually, as maker boards drop labels with no room. |
| D7 | What a pin shows | One property, "Pin Labels" (formerly "Display Pin #s"; field and enum `displayNumbers`/`DisplayNumbers` unchanged for file compatibility): `NO` (shown as None) draws nothing, `NAMES` draws the pin names, the four numbering schemes draw numbers outside the body. Names and numbers are never drawn together. `NAMES` is the default for new parts and the factory DIP8/DIP16 variants | A pin carries one label at a time; drawing both cluttered a 0.3 in body. Saved user variants keyed by the old property name lose only this setting and fall back to `NAMES`. Names under `NAMES` follow DIP numbering, so a part drawn from the solder side (`DIP_MIRROR`) shows numbers, not names. |
| D8 | Datasheet row | `group, part, row spacing, name1, ..., nameN`, e.g. `Op-Amp (Dual),TL072,0.3 in,1OUT,1IN-,1IN+,VCC-,2IN+,2IN-,2OUT,VCC+`. The pin count is the number of name columns | Names are trailing columns, so the comma split in `DatasheetService.loadDatasheet` needs no second separator. Row spacing covers 0.6 in parts. |
| D9 | Model identity | New `@ComponentDescriptor` attribute `datasheetModelColumn` (default `0`); `DatasheetService.findSeries` and `ComponentCatalogGenerator` use it instead of hard-coding column 0. `DIL_IC` sets it to `1` | ICs need a data model in which the toolbox groups by family while the AI names the part. Putting the part number first would flatten the toolbox into one long menu. Existing datasheets keep the default and are unaffected. Core stays free of concrete types. |
| D10 | Value preservation | `DatasheetService.findModel` treats a blank value like `null` | `DIL_IC.value` defaults to `""`, which otherwise makes every row look like a value change and the AI's model lookup fail. |
| D11 | `applyModel` | Sets pin count, row spacing, pin names and value (the part number). Leaves pin spacing, colours, orientation and display settings alone | The datasheet states what the part is, not how the user chose to draw it. |
| D12 | One row per part | Parts sharing a pinout each get their own row (TL072, NE5532, JRC4558, OPA2134) | Readable and greppable; a pinout-reference indirection is not worth its complexity at this size. |
| D13 | AI catalog | Part numbers reach the catalog through D9. Per-part pinouts are not emitted; the AI sees names in the project context once a part is placed | Smallest change. If single-shot "add a TL072 and wire it" prompts prove unreliable, emit a compact `part -> names` map for `DIL_IC` (~100 parts, a few KB). |

### Explicitly out of scope

- `SIL_IC` and `MultiwattIC`. Power amps in TO-220/Multiwatt packages (LM1875, TDA2030, TDA2050,
  LM3886) belong there and follow once the DIP version has settled. The pin-name logic lives in
  `DIL_IC` until a second class adopts it, then moves to a shared helper.
- Translating datasheet group names. Toolbox datasheet menus are untranslated, like the resistor
  families.
- Inferring a pinout from the value text. A model is applied explicitly, from the toolbox or by the
  AI.

## 3. Changes

### diylc-core

- `ComponentDescriptor`: `int datasheetModelColumn() default 0`, read back through
  `DatasheetService.getModelColumn(Class)` rather than added to `ComponentType`.
- `DatasheetService.findSeries`: match on the model column of the class's descriptor.
- `DatasheetService.findModel`: a blank `String` value counts as no value.
- `ComponentCatalogGenerator`: distinct values of the model column become `datasheetModels`.
- `import-defaults/variants.xml`: the DIP8 and DIP16 factory variants of `DIL_IC` are retired
  (`deletedOn` 2026-11-01T21:00) in favour of the datasheet models, as the Resistor variants were.

### diylc-library

- `DIL_IC`:
  - field `pinNames` with its `@EditableProperty` "Pin Names" and a transient parsed copy;
  - `getPinNumber(int)`, with `getControlPointNodeName` rewritten on top of it (D2, D3);
  - `drawPinNames` (D6), called from `draw()` after the name/value label;
  - `implements IDatasheetSupport`, `enableDatasheet = true`, `datasheetCreationStepCount = 2`,
    `datasheetModelColumn = 1`, `applyModel` (D11), and a `DIL_IC(String[] model)` constructor like
    `Resistor`;
  - `@AiNote` extended: "Pin Names" lists names in pin order starting at the notch, the rail
    suffix rule of D4, and that applying a datasheet model fills names, pin count and row spacing.
- `datasheets/DIL_IC.datasheet`: rows per D8, each checked against the manufacturer's datasheet.
  A part whose pinout cannot be sourced is left out.

### diylc-swing

- "Pin Names" in `English.txt` and translated in every other file in `lang/`.

### Generated

- `catalog_full.json` regenerated.

## 4. Datasheet inventory

`DIL_IC.datasheet` holds 92 parts:

| Group | Parts |
|---|---|
| Op-Amp (Single) | TL061, TL071, TL081, LM741, LF356, OPA134, NE5534, LF351, UA741, OP07, LM308 |
| Op-Amp (Dual) | TL062, TL072, TL082, NE5532, JRC4558, RC4580, OPA2134, OPA2604, LM4562, LM358, LF353, MC1458, RC4558, LM833, TL022, OP275, LME49720, OPA2132, OPA2227 |
| Op-Amp (Quad) | TL074, TL084, OPA4134, LM324, TL064 |
| Comparator | LM393 |
| Timer | NE555, ICM7555, TLC555, NE556, ICM7556, LM555 |
| OTA / VCA | LM13700, CA3080, SSM2164, V2164, LM13600, AS3080, AS2164 |
| Power Amp | LM386, LM380 (14-pin LM380N), TDA7052, TDA2822 |
| BBD | MN3007, MN3207, BL3207, BL3208A, V3207, MN3008, MN3005, BL3208B |
| BBD Clock | MN3101, MN3102, V3102 |
| Delay / Compander | PT2399, NE570, NE571, SA571 |
| Charge Pump | ICL7660, TC1044, MAX1044, LT1054 |
| CMOS Logic / Switch | CD4049, CD4069, CD40106, CD4011, CD4013, CD4017, CD4066, CD4051, CD4053, CD4016, 74HC14, 74HC4051 |
| Optocoupler | PC817, 4N35, 6N138, H11L1, 6N139 |
| MCU | ATtiny85, ATmega328P, ATtiny13A |

Rules for every row: names as in D5; a repeated net only through the `_n` suffix and only where the
pins are joined inside the package (otherwise distinct names, e.g. `AGND` and `DGND` on the
PT2399, `GAIN1` and `GAIN8` on the LM386); unused pins `NC`. A new part is added only after its pin
table has been read from the manufacturer's datasheet or from two independent sources.

Sourcing and modelling notes that are not obvious from the rows:

- The groups added in phase 3 were each read from a manufacturer pin table or two agreeing sources.
  The weakest are the TDA7052 (pin 7 NC from one indexed copy), the CD4017 output order (one site),
  MN3101 (manufacturer scan only), and the ATmega328P grounds, named `GND_1`/`GND_2` as
  one net although the datasheet only says both must be connected.
- The 24 phase 2 rows other than NE5534 and OPA134, and the 24 rows added from the candidate list
  (LF351 ... ATtiny13A, all sharing a pinout already in the file), are well-established pinouts not
  yet read one by one against a datasheet; of the latter only TL022 was read from its datasheet.
- MN3005, MN3008 and BL3208B have 8 leads in a 14-pin body (leads at positions 1, 2, 6, 7, 8, 9, 13,
  14), so they are modelled as 14-pin parts with `NC` at the empty positions. The position mapping
  was derived from the package drawing, not from a datasheet table.
- BL3208 exists in two pinouts: BL3208A follows the MN3207, BL3208B the MN3008. Both are listed by
  their full names.
- The TDA7052 row is the plain part; the TDA7052A/B have a volume-control pin 4 and differ.
- Pin names drawn on a 0.3 in body are dropped where they collide (D6), so long names such as
  `1RECTCAP` or `COMP/BAL` often show only in tooltips and the netlist.

### Candidates for further growth

Ranked from two sources: the ICs used in 784 user projects (Nov 2026 sample: 232 identifiable IC
instances, about 74% covered by the library) and a survey of guitar, synth, hi-fi and radio/ham
DIY communities. Each part still needs its pin table read before it is added.

- **Next, for coverage of the user sample (about 86% now, about 94% with these eight):** LM3915,
  74HC595, LM723, CD4046, CD4081, CD4510, INA128, INA217.
- **New pinouts with community demand:** LM3914/LM3915/LM3916 (one pinout),
  74HC595, AS3340, AS3320, CD4052, CD4007, CD4046, CD4093, CD4024, CD4040, THAT340, CA3046,
  CA3094, MCP4822, LM339, NE602/SA602/SA612, MC1496, LM567, CD4060, uPC1237 (SIP-8), INA217,
  INA128, PGA2311, TDA1543, LM723, CD4081, CD4510.
- **Multiwatt, existing package:** TDA7297, TDA7377 (Multiwatt 15), TDA2005, TDA2009A
  (Multiwatt 11), LM3875 (TO-220-11).
- **Pentawatt / TO-220-5, needs the package back:** LM1875, TDA2030A, TDA2050, TDA2003, LM2576,
  LM2596 - the most requested missing package across guitar, hi-fi and power supplies. LM4766
  (TO-220-15) needs its lead spacing compared with Multiwatt 15 first.
- **Niche:** MC1350, MC3361, TDA7000, CD2003, TA7358, PIC12F675, PIC16F628A, Electric Druid chips,
  XR2206, ICL8038, AD633, DG411, AS3310, AS3394, KA2281, KA2284, LM1036, TDA1541A, MAX7219.
- **Out of scope:** SMD-only parts (SSI2130/2140, OPA1612, FV-1, PCM179x, Si5351, AD9850), TO-92
  parts and modules.

### SIP and Multiwatt packages

`SIL_IC` and `MultiwattIC` follow the same model: a `pinNames` string in pin-number order, a
`PinLabels` property ("Pin Labels": None / Names / Numbers, default Names; a new top-level enum
shared by both), and datasheet support with `datasheetModelColumn = 1`. Name parsing and the
outside-the-body label drawing live in `ICPinNames`, which `DIL_IC` also uses for parsing and for
the shared 6 px font. Labels on these packages sit outside the body, square to the row, in the
border colour, because the bodies are too narrow to hold them.

- `SIL_IC.datasheet`: `group, part, pin spacing, names...` - 12 parts read from manufacturer
  datasheet pages: LA4440, LA4485, LA4425A, TA7205AP, TDA1013B, TDA1010A, TDA1521A, TDA7056,
  TDA7056A, BA5406, BA5417 (Power Amp) and BA3121N (Preamp). TA7205AP and TDA1010A have no pin
  table, so their names come from the manufacturer's application circuit.
- `MultiwattIC.datasheet`: `group, part, names...`, the pin count picks the type - LM3886, TDA7265,
  TDA7293, TDA7294. Supply pins the datasheet repeats without stating an internal connection are
  separate nets, named after their pin or role as with the LM386: `VCC+1`/`VCC+5` (LM3886),
  `VS-1`/`VS-6` (TDA7265), `VS+SIG`/`VS+PWR` and `VS-SIG`/`VS-PWR` (TDA7293/94).
- `MultiwattIC` geometry, from the manufacturer package drawings: consecutive leads are 1.7 mm
  apart on Multiwatt 11 / TO-220-11 and 1.27 mm on Multiwatt 15 (`MultiwattType` carries the lead
  pitch; Multiwatt 11 was previously drawn at 1.27 mm, so existing Multiwatt 11 parts change
  shape). The rows are 5.08 mm apart. Odd pins form the row bent forward in front of the body; the
  body (5 mm, tab at the back, `BACK_TO_NEAR_ROW` 4.55 mm) is drawn around the even row, whose pins
  are painted over it. All labels read forward, away from the tab: an even pin's from the front
  edge of the body, in the gap between the forward leads either side of it; an odd pin's past the
  end of its lead. Node names follow the physical numbering, which alternates between rows;
  before this they were control-point order.
- Pentawatt (TO-220-5: LM1875, TDA2030A, TDA2050, LM2596...) was researched and dropped for now.
  If it returns: 1.7 mm lead pitch, 4.0 mm row offset, odd pins forward; TI's straight-leaded
  KC0005A and horizontal NEB forms are different footprints.

## 5. Tests

- `DIL_ICTest`: pin number per scheme across several pin counts, including `NO` now matching `DIP`;
  names mapped through each scheme; too few, too many and blank names; `applyModel` round trip.
- `DIL_ICDatasheetTest`, after `ResistorDatasheetTest`: every row parses; its name count is a
  `PinCount` value; part numbers are unique; within a row every name is unique and no two names
  share a rail unless they carry the `_n` suffix; `findSeries("TL072")` returns exactly one row.
- `DatasheetServiceTest`: model column other than 0, and the blank-value case of D10.

## 6. Verification

- `mvn -q test` over the whole reactor, since core, library and swing all change.
- Render pin names and look at them: 8, 14 and 16 pins, 0.3 and 0.6 in row spacing, all four
  orientations, Display NAME / VALUE / BOTH.
- Regression suite run is required: D2 changes netlist output of every sample project containing an
  unnamed DIP IC with "Pin Labels" (then "Display Pin #s") set to `NO`. Reference netlists are regenerated only after
  the diffs are confirmed to be exactly that renumbering.

## 7. Phases

1. **Pin names**: done (D1 to D7, `DIL_ICTest`, language files, `catalog_full.json`). The
  regression run of §6 is still outstanding and should precede the release.
2. **Datasheet support**: done (D8 to D13, the core changes, 26 op-amps and timers,
  `DIL_ICDatasheetTest`, `catalog_full.json`). NE5534 and OPA134 were read from TI's current pin
  tables (the OPA134 no longer has offset trim: pins 1, 5 and 8 are NC); the other 24 rows are the
  well-established industry pinouts and have not yet been read against a datasheet one by one.
3. **Library growth**: done - 68 DIP parts, 12 SIP parts and 4 Multiwatt parts. Further parts on
  request, each read from a manufacturer pin table first.
