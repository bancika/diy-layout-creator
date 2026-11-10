# RKM Value Notation

Resistance, capacitance, inductance and voltage values can be shown in RKM notation, where the unit
letter replaces the decimal point (`4K7`, `0R22`, `2n2`, `4u7`, `6V3`), instead of decimal (`4.7K`,
`0.22uF`, `6.3V`). Either notation can always be typed, and typing the unit together with the
value (`4K7`, `2.2nF`) also sets the unit.

IEC 60062 defines RKM for resistance and capacitance only. Inductance (`4u7`, `2n2`) and voltage
(`3V3`, `6V3`, zener `5V1`) follow common practice. Power, current and frequency are left out:
`0W25`, `1A6` or `4M43` are rare in practice.

## 1. User-facing behaviour

### Config menu

```
Config
  ...
  Value Notation >            (after Sticky Points; icon IconLoader.Decimal)
      (o) Decimal (4.7K, 0.22uF, 6.3V)
      ( ) RKM (4K7, 0u22, 6V3)
```

One setting for all four measures: key `IPlugInPort.VALUE_NOTATION_KEY = "valueNotation"`, values
`VALUE_NOTATION_DECIMAL` / `VALUE_NOTATION_RKM` (the English menu titles, since `ToggleAction`
stores its title), default decimal. Switching clears the drawing cache and repaints.

### Display

The notation governs `toString()` of `Resistance`, `Capacitance`, `Inductance` and `Voltage`, and
so canvas labels, `getValueForDisplay()`, search, BOM, netlist text and AI assistant context.
The edit dialog's value field is the exception: it always shows the bare decimal number, because
the unit box beside it already shows the unit.

| Value | Decimal | RKM |
| --- | --- | --- |
| 4.7 Ω / 0.22 Ω / 100 Ω | `4.7Ω` `0.22Ω` `100Ω` | `4R7` `0R22` `100R` |
| 4.7 K / 1 M | `4.7K` `1M` | `4K7` `1M` |
| 0.22 uF / 2.2 nF / 100 nF | `0.22uF` `2.2nF` `100nF` | `0u22` `2n2` `100n` |
| 4.7 uH / 10 mH | `4.7uH` `10mH` | `4u7` `10m` |
| 6.3 V / 400 V | `6.3V` `400V` | `6V3` `400V` |
| 500 mV / 1.5 KV | `500mV` `1.5KV` | unchanged: `500mV` `1.5KV` |

Rules: the value stays in its stored unit (4700 Ω is `4700R`, never `4K7`); `R` stands for ohms;
capacitance drops `F` and inductance drops `H`; whole numbers take the letter as a suffix; digits
are rounded like the `0.####` decimal format. Only volts have an RKM form, since `1m5` or `1k5`
would not say the value is a voltage.

### Input

Accepted in either notation: a bare number (keeps the current unit), a decimal with a unit
(`4.7k`, `220 nF`, `4.7ohm`, `1.5kV`), or an RKM code (`4K7`, `R22`, `2n2F`, `4µ7`, `6V3`, `1k5V`).
Unit text per measure:

- Resistance: `R`, `Ω`, `ohm(s)` → Ω; `K` → K; `M`, `Meg`, and lower-case `m` → M (there is no
  milliohm unit).
- Capacitance / inductance: `p`, `n`, `u`/`µ`, `m`, optionally followed by `F` / `H`; `F` / `H`
  alone. Case-insensitive with the trailing letter; without it, `M` is rejected so it is never
  read as mega.
- Inductance does **not** accept `R`: maker codes print `4R7` for 4.7 µH, which RKM would read as
  4.7 H.
- Voltage: `V`, `mV`, `kV`, case-insensitive.

The property updates on every keystroke (Enter in the dialog commits at once); on focus loss the
text is rewritten as the bare decimal number. Unparseable text shows the warning icon.

## 2. Design

- **`org.diylc.core.measures.ValueNotation`** — enum `DECIMAL, RKM` with the global
  `getCurrent()` / `setCurrent()` (a volatile static, read by `toString()` on every repaint),
  `formatRkm(value, symbol)` and `parse(text, unitResolver)` returning a `ParsedMeasure(value,
  unit)` record (unit `null` for a bare number).
- **Unit enums** — `ResistanceUnit`, `CapacitanceUnit`, `InductanceUnit` and `VoltageUnit` carry an
  `rkmSymbol` (`null` for mV and KV) and a static `fromSymbol(String)` resolver. Constant names and
  `toString()` are unchanged, so `.diy` files are unaffected.
- **Measures** — `toString()` returns RKM when selected and the unit has a symbol, else
  `super.toString()`. `parseResistance`, `parseCapacitance`, `Inductance.parseCapacitance`
  (historical name) and `parseVoltage` delegate to `ValueNotation.parse`, so the AI assistant,
  `V1FileParser`, `TangoNetlistParser` and `CapacitorDatasheetService` read RKM too.
  `parseVoltage` used to reject `1KV`; it now parses it.
- **`org.diylc.presenter.ValueNotationBinder`** — applies the config value and listens for changes.
  Bound from `ConfigPlugin.connect()` rather than the `Presenter` constructor, because the
  secondary presenters (previews, thumbnails, batch converter, regression runner) run on in-memory
  configurations and would reset the global notation to decimal.
- **Swing** — `MeasureTextField` (in DIYLC, not `bancika-gui-framework`, whose
  `lib/swing-framework-1.1.jar` would need re-vendoring) parses without rewriting the text while
  typing. `MeasureEditor.getUnitResolver` picks it for the four measures; `DoubleTextField` serves
  the rest. A typed unit switches the unit box behind a guard flag. `MeasureArrayEditor`
  (`MultiSectionCapacitor`) parses `/`-separated sections, each with an optional unit; the box
  shows the first section's unit and a section in another unit is written with it (`47 / 100nF`).
- **Localization** — `Value Notation`, `Decimal (4.7K, 0.22uF, 6.3V)` and `RKM (4K7, 0u22, 6V3)`
  in every file in `lang/`.

## 3. Status and next steps

Implemented; `mvn -q test` passes, including `ValueNotationTest` (formatting, parsing, rejection
and round trips for all four measures). With the default decimal notation every string the app
produces is unchanged.

1. Try it in the running app: the edit dialog (single value and `MultiSectionCapacitor`),
   inductor and capacitor voltage properties, canvas labels after switching, BOM export.
2. Run the regression suite to confirm decimal output is unchanged.
3. Optional, in `bancika-gui-framework`: stop `DoubleTextField` / `DoubleArrayTextField` accepting
   a valid prefix (`4R7` read as `4`).
