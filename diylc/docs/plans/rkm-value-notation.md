# RKM Value Notation for Resistance and Capacitance

Let users read and type resistor and capacitor values in RKM (IEC 60062) notation, where the unit
letter replaces the decimal point (`4K7`, `0R22`, `2n2`, `0u22`), as well as in today's decimal
form (`4.7K`, `0.22uF`). Typing a value with a unit letter in it, in either notation, also sets the
unit, so `4K7` typed into a field whose unit is `Ω` becomes 4.7 K.

## 1. User-facing behaviour

### Config menu

One radio submenu in `ConfigPlugin` covering both resistance and capacitance, built the same way
as `Snap To` and `Ruler Inch Subdivision...` (`ToggleAction`, string config key, title stored as
the value):

```
Config
  ...
  Value Notation >            (after Sticky Points, alphabetical like the rest)
      (o) Decimal (4.7K, 0.22uF)
      ( ) RKM (4K7, 0u22)
  ...
```

- Key `IPlugInPort.VALUE_NOTATION_KEY = "valueNotation"`, values `VALUE_NOTATION_DECIMAL` /
  `VALUE_NOTATION_RKM`, default decimal.
- Switching notation repaints the canvas immediately, so labels on the board change at once.

### What the notation controls (display)

The notation governs `Resistance.toString()` and `Capacitance.toString()` and therefore everything
built on them: labels drawn on the canvas, `getValueForDisplay()` (search, BOM, netlist text,
project explorer, AI assistant context). The edit dialog's value field is the exception: it always
shows the bare decimal number, because the unit box beside it already carries the unit and an RKM
code there would repeat it.

| Value | Decimal (unchanged) | RKM |
| --- | --- | --- |
| 4.7 Ω | `4.7Ω` | `4R7` |
| 0.22 Ω | `0.22Ω` | `0R22` |
| 100 Ω | `100Ω` | `100R` |
| 4.7 K | `4.7K` | `4K7` |
| 47 K | `47K` | `47K` |
| 1 M | `1M` | `1M` |
| 0.22 uF | `0.22uF` | `0u22` |
| 2.2 nF | `2.2nF` | `2n2` |
| 100 nF | `100nF` | `100n` |
| 4.7 pF | `4.7pF` | `4p7` |
| 1000 uF | `1000uF` | `1000u` |

Rules: RKM keeps the stored unit (it never renormalises 4700 Ω to 4K7), uses `R` for ohms, drops
the trailing `F` on capacitance, puts the unit letter where the decimal point was, and writes it
as a suffix for whole numbers. The digits after the letter are rounded the same way as today's
`0.####` decimal format.

### What the user can type (input)

The value field accepts, in **both** notations:

- a bare number (`4.7`, `0,22`) — the unit is unchanged, as today;
- a decimal number with a unit (`4.7k`, `0.22uF`, `220 nF`, `4.7ohm`, `4.7Ω`);
- an RKM code (`4K7`, `4k7`, `0R22`, `R22`, `2n2`, `0u22`, `4µ7`, `1M`, `100R`).

When the text carries a unit, the unit combo switches to it and the property gets the new value
and unit together. Accepted unit letters, case-insensitive except where noted:

- Resistance: `R`, `Ω`, `ohm`/`ohms` → Ω; `K`/`k` → K; `M`/`m`/`Meg` → M (there is no milliohm
  unit, so a lower-case `m` on a resistor means mega, as it does on many parts lists).
- Capacitance: `p` → pF; `n` → nF; `u`/`µ` → uF; `m` → mF (**case-sensitive**: `M` is not
  accepted, to avoid reading "mega"); `F` alone → F. A trailing `F` after any of these is
  optional (`4u7F`, `2.2nF`).

A unit letter that does not belong to the measure (`4u7` on a resistor) shows the existing warning
icon, as any unparseable text does today.

**Commit behaviour.** The property is updated on every keystroke, as today — the edit dialog's
Enter key commits straight away, so it cannot wait for focus loss. When the field loses focus,
the text is rewritten as the bare decimal number (`4K7` typed → `4.7` with the combo showing `K`),
whichever notation is selected. The text is not reformatted while the user is typing, so the
caret never jumps.

## 2. Design

### Core (`diylc-core`, `org.diylc.core.measures`)

- **`ValueNotation`** — new enum `DECIMAL, RKM` holding the formatting and parsing logic, so the
  two measure classes stay thin:
  - `String format(double value, Unit unit, String rkmSymbol)` — decimal falls through to the
    existing `0.####` + `unit.toString()`; RKM implements the rules in §1.
  - `static ParsedMeasure parse(String text, ...)` — returns value + unit (unit `null` for a bare
    number), or throws `IllegalArgumentException`. One small record `ParsedMeasure<U>` at the
    bottom of the file.
- **Current notation.** One `private static volatile ValueNotation current` on `ValueNotation`
  with static `getCurrent()` / `setCurrent()`, shared by `Resistance` and `Capacitance`.
  `toString()` reads the field, never
  `ConfigurationManager`: it runs from `draw()` on every repaint. Default `DECIMAL`, so headless
  rendering, the regression suite and unit tests are unchanged unless they opt in.
- **Unit display symbol for RKM.** `ResistanceUnit` and `CapacitanceUnit` get an `rkmSymbol`
  constructor argument (`R`/`K`/`M`, `p`/`n`/`u`/`m`/`F`). Enum constant names and `toString()`
  are untouched — XStream stores the constant name, so files are unaffected.
- **Parsing.** `Resistance.parseResistance` / `Capacitance.parseCapacitance` delegate to
  `ValueNotation.parse`, so RKM is accepted everywhere a string is turned into a value: the
  editor, `V1FileParser`, `TangoNetlistParser`, `CapacitorDatasheetService` and — important once
  `toString()` can emit `4K7` — the AI assistant, which receives values as text and writes them
  back through these parsers. Existing accepted inputs must keep parsing identically.
- `Inductance` and `Power` are out of scope; their names are left as they are.

### Wiring the setting

`org.diylc.presenter.ValueNotationBinder.bind(configManager, refresh)` reads the key once,
pushes it into `ValueNotation.setCurrent()`, and on change updates the static, calls
`DrawingCache.Instance.clear()` (cached component images contain the old labels) and refreshes
the canvas. It is called from `ConfigPlugin.connect()`, not from the `Presenter` constructor:
the notation is global, and the many secondary presenters (project preview, cloud thumbnails,
batch converter, regression runner) are built on in-memory configurations and would reset it to
decimal.

### Swing (`diylc-swing`)

- **`MeasureTextField`** — new field in `org.diylc.swing.gui.editor`, beside `MeasureEditor`.
  Same look as `DoubleTextField` (warning icon on the right) but it parses through
  `ValueNotation.parse` for the measure type and fires a `MEASURE_PROPERTY` change carrying
  value + optional unit; on `focusLost` it reformats its text. It is generic over the unit enum
  so it can serve `Inductance` later without changes.

  Recommendation: keep it in DIYLC rather than extending `DoubleTextField` in
  `bancika-gui-framework`. The framework is consumed as the vendored
  `lib/swing-framework-1.1.jar`, so a change there means rebuilding and re-vendoring the jar for a
  widget whose parsing rules are DIYLC domain knowledge. `DoubleTextField` keeps serving
  `PercentageEditor`, `LoadlineEditorFrame` and every other plain-number measure.

  One `DoubleTextField` bug is worth fixing in the framework separately, and the new field must
  not copy it: `DecimalFormat.parseObject(String)` accepts a valid prefix, so `4R7` silently
  parses as `4` today, with no warning.
- **`MeasureEditor`** — uses `MeasureTextField` when the property type is `Resistance` or
  `Capacitance`, `DoubleTextField` otherwise. On a value + unit event it sets the combo's
  selected unit behind a guard flag, so the combo's own `ActionListener` does not fire a second
  `setValue`. Read-only properties keep today's behaviour (disabled field, combo converts).
- **`MeasureArrayEditor`** (`MultiSectionCapacitor`) — each section value is parsed with the same
  rules. Units travel per section in the text (`47u / 22u / 10u`) and a section without a unit
  takes the combo's unit, as today. The combo shows the first section's unit; in decimal notation a
  section in a different unit is written with its unit (`47 / 100nF`). Changing the combo applies
  its unit to every section, as before.
- **`ConfigPlugin`** — the `Value Notation` submenu (`IconLoader.Decimal`), and the call to
  `ValueNotationBinder.bind`.

### Localization

New keys in `English.txt` and every other file in `lang/`: `Value Notation`,
`Decimal (4.7K, 0.22uF)`, `RKM (4K7, 0u22)`.
Because `ToggleAction` stores its title as the config value, the stored values are the English
titles and `MainFrame` translates only what is shown.

## 3. Compatibility

- `.diy` files store `value` and the unit enum name; neither changes. No serialization impact.
- With the default (decimal) every string the app produces is byte-identical to today, so
  regression references do not move. A regression run is still worth doing after implementation,
  since `parseResistance` / `parseCapacitance` change.
- A user who switches to RKM gets RKM in BOM and netlist exports as well. That is the intended
  "everywhere" behaviour; anything that re-imports those files goes through the RKM-aware
  parsers.

## 4. Tests (`diylc-core`, JUnit 4)

`ValueNotationTest`:

- formatting: every row of the §1 table, both notations; zero, null, values ≥ 1000 in their unit,
  rounding at the fourth decimal;
- parsing: every input form in §1, per measure; case rules (`m` vs `M` for capacitance); comma as
  decimal separator; rejects `4u7` on a resistor, `4K7K`, `K`, empty text;
- round trip: `parse(format(x))` returns `x` for both notations;
- regression guard: every string currently accepted by `parseResistance` / `parseCapacitance`
  (the inputs in `V1FileParser`, `TangoNetlistParser` and `CapacitorDatasheetService` tests and
  `AiEditScriptEditorTest`) still parses to the same value.

## 5. Status and next steps

Implemented as described; `mvn -q test` passes, including `ValueNotationTest`.

1. Try it in the running app: the edit dialog (single value and `MultiSectionCapacitor`), canvas
   labels after switching notation, BOM export.
2. Run the regression suite to confirm decimal output is unchanged.
3. Optional, in `bancika-gui-framework`: stop `DoubleTextField` / `DoubleArrayTextField`
   accepting a valid prefix (`4R7` read as `4`).
