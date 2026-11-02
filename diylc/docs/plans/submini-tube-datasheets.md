# Sub-Mini Tube Datasheets

Naming the leads of a `SubminiTube` and applying a known tube's leads and body from a datasheet.

## 1. Goal

Follows [Tube Socket Datasheets](tube-socket-datasheets.md). Sub-miniature ("pencil") tubes are
soldered in by their wire leads, so the tube itself carries what a socket carries for a noval: which
lead is the plate, which the grid. `SubminiTube` draws the body and leads (in-line flat press or
circular button, 3 to 10 leads, optional top lead) but cannot name a lead, so the netlist, tooltips
and the AI see only numbers. A datasheet of the popular types - Western (6021, 5840, 5672, 6111...)
and Soviet (6N16B, 6N17B, 6S6B, 6Zh1B...) - sets the lead names, lead count, arrangement and body
size in one step.

### What is wrong today

`SubminiTube` does not override `getControlPointNodeName`, so the inherited `index + 1` applies. In
the circular arrangement control point 0 is the body centre, not a lead: the netlist calls the centre
"1" and the real leads "2" ... "N+1". No sample project contains a sub-mini tube, so fixing it
changes no reference output.

## 2. Design decisions

| # | Decision | Choice | Rationale |
|---|---|---|---|
| S1 | Lead names | New `String pinNames` field, comma-separated in lead-number order, shown as "Pin Names"; `null` means unnamed. Node name = the name if present and not blank, otherwise the lead number | The shape used by `DIL_IC`, `SIL_IC` and `TubeSocket`. Old files deserialize with `null` and keep numbers. |
| S2 | Lead numbering | Lead 1 is the first lead control point: control point 0 on in-line and folded tubes, control point 1 on circular ones (0 is the centre and gets no node name). A top lead is the last lead | Fixes §1. Matches how data books number flat-press leads across the row and button leads around the circle. |
| S3 | Naming style | As on tube sockets (tube plan V5-V8): `P`, `G`, `G1`/`G2`/`G3`, `K`, `IS`, section prefixes `1P`, `2G`; filament ends `F` + lead number on directly heated battery tubes, heater ends `H` + lead number; `_n` only for leads joined inside; `NC` for an empty lead position, `IC` for "do not use" | One vocabulary across all tubes. |
| S4 | What a lead shows | New `pinLabels` field with the shared `PinLabels` enum, shown as "Pin Labels": None, Names (default), Numbers. Names are drawn beside each lead end, away from the body, with `ICPinNames.drawOutside` and its 6 px font; unnamed leads draw nothing under Names | Same option as the ICs and tube sockets. Old files have no names, so under the default they draw exactly what they drew before. |
| S5 | Datasheet row | `group, tube, arrangement, top lead, width, thickness, length, name1, ..., nameN`, e.g. `Dual Triode,6021,Inline,no,0.4 in,0.3 in,1.5 in,...`. `arrangement` is a `PinArrangement` constant, `top lead` is `yes`/`no`, sizes are the maximum body dimensions from the data book; the lead count is the number of names, minus one when there is a top lead | Trailing names as in every pin-naming datasheet. |
| S6 | `applyModel` | Sets value, pin arrangement, top lead, pin count, diameter, thickness, length and pin names. Leaves orientation, folded, lead length, lead spacing (the board hole pitch, not the wire pitch) and colours alone | The datasheet says what the tube is; how it is bent onto the board is the user's choice. |
| S7 | Model identity | `enableDatasheet = true`, `datasheetCreationStepCount = 2`, `datasheetModelColumn = 1` | As for all other datasheets. |
| S9 | Bulb dimensions | Three sizes per tube: "Diameter" is the broad side of the cross-section - the width seen when the tube lies folded on the board - a new "Thickness" the narrow side, and "Length" the glass length excluding leads; all maximums from the data book outline. A standing in-line tube draws an oval, broad side along the lead row; circular tubes stay round. `thickness` is `null` in old files and reads as the diameter | Flat T-2 bulbs are oval, so one diameter cannot draw both the folded view (broad face down) and the standing top view. A new field keeps old files round, as drawn before. "Thickness" is already translated. |
| S10 | View | The tube is drawn as seen from above, from the component side: circular leads run counter-clockwise from the gap, the mirror of the data book's bottom view. In-line and folded tubes are unaffected - lead 1 is the first control point and nothing marks a side. `TubeSocket` stays a bottom view | A sub-mini tube is soldered onto a board and laid out from the component side; a socket is wired from under the chassis. Circular leads used to run clockwise; files saved before keep their stored lead positions until the part is edited (none in the sample corpus). |
| S11 | Rod pentodes | 1Zh18B, 1Zh24B, 1Zh29B and 1P24B are listed as circular tubes with the anode as the top lead. Their real centre lead is drawn on the circle with the others | Popular in battery-tube builds. Names, netlist and AI are exact; only the drawn position of the centre lead is approximate. |
| S8 | Lead counts | The `PinCount` enum (3-10) covers the types; a tube outside it is left out rather than the enum widened | Keeps the change to new fields only. |

### Explicitly out of scope

- Nuvistors (6S51N, 7586, 13CW4): a different metal-can body; a separate component if wanted.
- Acorn tubes.
- Translating group names.

## 3. Changes

### diylc-library

- `SubminiTube`: fields `pinNames` and `pinLabels` with their properties; `getControlPointNodeName`
  per S1/S2; lead-name drawing (S4); `implements IDatasheetSupport`, descriptor attributes,
  `(String[] model)` constructor, `applyModel` (S6); `@AiNote`s on lead numbering, names and the
  model.
- `datasheets/SubminiTube.datasheet`: rows per S5, each from a data book (RCA, Raytheon, Sylvania,
  GE, Tung-Sol, Soviet reference books) or two independent sources.

### Generated

- `catalog_full.json`, and `pinout-drivers.txt` if "Pin Names" becomes a pinout driver.

## 4. Datasheet inventory

`SubminiTube.datasheet`, 35 tubes:

| Group | Western | Soviet |
|---|---|---|
| Dual Triode | 6021, 6111, 6112 | 6N16B, 6N17B, 6N21B |
| Triode | 5703, 5718, 5719, 5744, 5977, 6814 | 6S6B, 6S7B, 6S31B, 6S32B |
| Pentode / Pentode (RF/IF) | 5886 / 5636, 5678, 5840, 5899, 6205, 6206 | 1Zh18B, 1Zh24B, 1Zh29B (rod, top anode) / 6Zh1B, 6Zh2B |
| Power Pentode | 5672, 5902, 6088 | 6P30B, 1P24B (rod, top anode) |
| Duo-Diode / Diode | 5896 | 6D6A |

Sourcing notes:

- Western rows read from Tung-Sol, RCA, Raytheon and Sylvania sheets (frank.pocnet.net); Soviet rows
  from the Soviet export catalogue sheets on the same site, tec.org.ru redraws of the reference books
  and the Tubeclinic sub-miniature summary.
- Circular button tubes have 8 or 10 lead positions on a circle with one gap, numbered clockwise in
  the data book's bottom view from the lead after the gap; `SubminiTube` already spaces N leads on
  N + 1 positions with the gap between the last lead and lead 1. In-line tubes carry a colour dot or
  key mark by lead 1.
- Positions the data book numbers but where no lead exists, or the lead is cut as a key, are `NC`
  (5703 lead 2, 5886 leads 5-6, 6N21B lead 7, the cut leads of 6S31B/6S32B).
- Bulbs: Western T-3 round 0.400 in, T-2x3 flat 0.385-0.400 x 0.285 in (5672, 5678, 6088, 5886);
  every Soviet "B" bulb is round, 7.2-10.8 mm. Lengths are maximum glass length with the tip; the
  exhaust tip is always at the far end from the leads.
- Electrodes tied inside to a cathode or filament end are folded into that lead's name (5840 g3 on
  the cathode leads, 5672 g3 on a filament end).
- Weakest rows: 5886 and 6814 (one data book each), 6S6B, 6S32B, 6D6A and 6P30B (one source each).
- 6206 follows the Sylvania sheet (basing 8DL: K with G3 on leads 2, 4 and 8, as the 5899), although
  its text mentions an external suppressor connection and Tung-Sol gives 8DC (G3 on lead 4). 6Zh1B
  follows a Soviet reference-book page (flat press, 7 leads in a row, mark by lead 1, 10.2 x 36 mm);
  a round-press version is also sold under that name. 6Zh2B follows the 6Zh2B-V reference drawing
  (flat press, 7 leads, 10.2 x 38 mm).
- 6814 follows the Sylvania sheet alone (basing 8DK, the 5718's leads); the second source found,
  rtellason.com, refuses automated downloads.
- Left out: 6P30B-R and 6N16B-VR (variants
  with other pinouts, single sources), 1Zh17B (lead table only assumed to match 1Zh24B).
- Rod pentodes name the filament centre tap of 1Zh29B and 1P24B `HCT`; the leads cut short as a key
  on 1Zh18B are `NC`.

## 5. Tests

- `SubminiTubeTest`: node names per arrangement (circular centre unnamed, lead 1 first), names
  mapped through, top lead last, `applyModel` sets the S6 properties and leaves the rest.
- `SubminiTubeDatasheetTest`: every row parses and applies; name count fits `PinCount`; tubes are
  unique; no two names share a net unless both carry `_n`; `findSeries` finds a tube, not a group.

## 6. Verification

- `mvn -q test` over the whole reactor.
- Render in-line, circular and folded tubes with a top lead in all orientations, Pin Labels in each
  mode; check names sit by their leads.
- Regression suite optional: no sample project contains a sub-mini tube.

## 7. Phases

1. **Plan**: this document.
2. **Code**: done - S1-S4, S6-S7, `SubminiTubeTest` (old files round, circular centre unnamed, circular top lead at
   the centre, in-line top lead last, model sets and leaves). Lead names are drawn beside each row
   lead away from the body, radially past the bulb on a standing circular tube; the top lead is left
   unlabelled. `setDiameter`/`setLength` now reset the cached body, which they did not before.
   `SubminiTubeDatasheetTest` fails until the datasheet exists.
3. **Library**: done - 31 tubes, rendered in-line, circular, folded and rod in several orientations
   and label modes; whole reactor passes; `catalog_full.json` regenerated; `pinout-drivers.txt`
   updated (Top Lead now drives lead names); release notes in 6.10.
