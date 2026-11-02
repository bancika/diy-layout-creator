# Tube Socket Datasheets

Naming every electrode of a `TubeSocket` from the tube that plugs into it.

## 1. Goal

Follows [DIP IC Pin Names](dip-ic-pin-names.md) and [Transistor Pinouts](transistor-pinouts.md). A
tube socket already carries a free-text "Electrode Labels" property, and every consumer reads it
through `getControlPointNodeName`: the netlist, hover tooltips, the AI project context and the
schematic view. In practice it is almost never filled in. Of 95 tube sockets in the sample corpus,
the user typed a designation on about half (12AY7 24 times, 6L6GT, 5Y3, KT88, 12AX7) but real
electrode names on only four; the rest carry the default `1,2,...,9`. A datasheet of the tubes
DIYers use (guitar amps, hi-fi, radio) sets the base, the electrode names and the designation in one
step, from the toolbox or from the AI.

### What is wrong today

Pins of one component that share a name are one net (`NetlistBuilder`, `Node.railName`). The six
factory variants that carry electrode names break that rule:

| Variant | Labels | Effect in the netlist |
|---|---|---|
| Chassis Noval 9A (12A?7) | `A,G,C,H,H,A,G,C,CT` | both anodes, both grids, both cathodes and both heater ends shorted together |
| Chassis Noval 9CV (EL84) | `n/c,G,C,H,H,n/c,P,n/c,S` | heater ends shorted; the three `n/c` pins joined (only `NC` and `RSV` are exempt) |
| Chassis Octal 5T | `n/c,H,n/c,P,n/c,P,n/c,H` | both rectifier plates shorted, filament ends shorted, `n/c` pins joined |
| Chassis Octal 7AC (6?6) | `n/c,H,P,S,G,n/c,H,C` | heater ends shorted, `n/c` pins joined |
| Chassis 7-pin 7BK | `G,IS,H,H,P,S,C` | heater ends shorted |
| Chassis Octal 8ET (EL34) | `IS,H,P,S,G,n/c,H,C` | heater ends shorted |

Heater ends are separate nets: a 12AX7 runs on 12.6 V across pins 4-5 or on 6.3 V with 4 and 5
joined against 9, and that choice is made by the wiring, not the tube.

## 2. Design decisions

| # | Decision | Choice | Rationale |
|---|---|---|---|
| V1 | Datasheet row | `group, tube, base, basing, name1, ..., nameN`, e.g. `Dual Triode,12AX7,B9A,9A,2P,2G,2K,H4,H5,1P,1G,1K,HCT`. `base` is a `TubeSocket.Base` constant; `basing` is the RETMA basing code, or `-` for a tube that has none (most Soviet types); the name count must equal the base's pin count | The trailing-names shape of the IC datasheets. The basing code is what tube data books print, so rows can be checked against them, and it lets a test assert that tubes sharing a basing share their names. |
| V2 | Model identity | `enableDatasheet = true`, `datasheetCreationStepCount = 2`, `datasheetModelColumn = 1` | The toolbox groups by family, the AI names the tube. |
| V3 | `applyModel` | Sets base, electrode labels and designation. Leaves mount, angle and colours alone | The datasheet says what the tube is, not how the user mounts the socket. |
| V4 | One row per tube | Equivalents get their own rows (12AX7, ECC83, 7025, 5751; 6L6GC, 5881, KT66), as the ICs list TL072 and NE5532 separately | Users and the AI look a tube up by the number printed on it. |
| V5 | Electrode names | Role letters as in American data books: `P` plate, `G` grid of a triode, `G1`/`G2`/`G3` control, screen and suppressor grids, `K` cathode, `BP` beam plates, `IS` internal shield, `SH` the metal shell or base sleeve (pin 1 of metal and GT octals), `HCT` heater centre tap. A tube with two or more sections puts the section number first, as the ICs do: `1P`, `1G`, `1K`, `2P` (and `T`/`P` prefixes for triode-pentodes: `TP`, `TG`, `PG1`) | Section-first keeps `G1` meaning control grid, never "grid of section 1". |
| V6 | Heater and filament ends | Named after their pin: `H4`, `H5` on a noval, `H2`, `H7` on an octal. A directly heated filament, which is also the cathode, is `F2`/`F8`. The `_n` suffix is never used for them | Each end is its own net (§1). Naming by pin follows `VCC+1`/`VCC+5` on the LM3886, the precedent for repeated roles that the package does not join. |
| V7 | Joined pins | A repeated name with the `_n` suffix, numbered by occurrence as on the ICs, only where the data book says the pins are connected inside the tube (e.g. the EF80 cathode on pins 1 and 3, `K_1`/`K_2`) | Same rule as the ICs, so `NetlistBuilder` joins exactly what the tube joins. |
| V8 | Unused pins | `NC` for no connection. Pins the data book marks "IC" (internal connection - do not use as a tie point) are `IC`, and `IC` joins `NC` and `RSV` in `Node.NON_NET_NAMES` | Several `IC` pins on one tube must not become one net. One-line core change. |
| V9 | Factory variants | The six named variants of §1 are retired (`deletedOn`), as the DIP8/DIP16 and resistor variants were; the four plain ones (PCB/Chassis Noval/Octal) stay as mount presets | The datasheet replaces the named ones with correct names; leaving them would keep handing out shorted netlists. The plain ones preset the mount, which a datasheet model deliberately leaves alone (V3), and their numbered labels short nothing, so a plain preset plus a model is the intended way to place a tube. |
| V11 | Drawn labels | Drawn through `AbstractMakerBoard.getDisplayPinLabel`, so `G1_1` shows as `G1` and parenthesised notes are dropped, as on the ICs; node names keep the suffix | Joined pins carry one role; printing the suffix crowded small sockets (6S19P). |
| V12 | Equivalents on one basing | A tube given a RETMA basing takes that basing's names, including its section numbers, even where its own data sheet numbers the units the other way (6N8S and 6N9S use the 6SN7's); tubes with no RETMA code keep their sheet's numbering | Swapping a 6N8S for a 6SN7 must not relabel the socket; section numbers are labels, the pin grouping is identical. |
| V13 | Property name | The `electrodeLabels` field and its accessors keep their names; the property is shown as "Pin Names", as on the ICs | One name for the same idea across ICs and tubes, for users and the AI. Variants store values by property name with no alias mechanism, so the two factory presets are re-keyed; a user's own saved tube variants lose their labels, as user variants lost "Display Pin #s" when the ICs renamed it. |
| V14 | What a pin shows | New `pinLabels` field with the shared `PinLabels` enum, shown as "Pin Labels": None, Names (default), Numbers. Numbers are the base's pin numbers, drawn where the names go | Matches `SIL_IC` and `MultiwattIC`. Old files deserialize with `null`, which reads as Names - exactly what every socket drew before. |
| V10 | Existing projects | Unchanged. A project placed from a retired variant keeps its labels and its shorted nets; nothing rewrites user data | Labels are user data. The release note can say how to re-apply a model. |

### Explicitly out of scope

- `Jumbo4PinTubeSocket` (fixed `G,F1,P,F2` for 211/805/845) and the UX4 base of 300B, 2A3 and 45:
  a 4-pin base is not a `TubeSocket.Base`. Candidate for a second phase.
- Magnoval (B9D: EL509, PL519), loctal and the B12C compactrons: no datasheet rows until a user
  asks; B12C exists as a base but few DIY tubes use it.
- `SubminiTube` and the tube schematic symbols.
- Translating group names, as for the ICs and transistors.

## 3. Changes

### diylc-core

- `Node.NON_NET_NAMES`: add `IC` (V8).
- `import-defaults/variants.xml`: retire the six named `TubeSocket` variants (V9).

### diylc-library

- `TubeSocket`: `implements IDatasheetSupport`, descriptor attributes (V2), `applyModel` (V3), a
  `TubeSocket(String[] model)` constructor. `@AiNote` extended with the naming rules of V5-V8 and
  the datasheet model.
- `datasheets/TubeSocket.datasheet`: rows per V1, each read from a manufacturer data sheet or a
  tube data book (RCA, GE, Sylvania, Tung-Sol, Philips/Mullard, JJ, Svetlana, Electro-Harmonix), or
  two independent agreeing sources.

### Generated

- `catalog_full.json` regenerated; `pinout-drivers.txt` updated if the analyzer starts reporting
  "Electrode Labels" differently.

## 4. Datasheet inventory

`TubeSocket.datasheet` small-signal rows, 46 tubes:

| Group | Basing | Tubes |
|---|---|---|
| Dual Triode | 9A | 12AX7, ECC83, 7025, 5751, 12AT7, ECC81, 12AU7, ECC82, 12AY7, 6072, 12BH7, 12DW7, 12AZ7, 5965, ECC99 |
| Dual Triode | 9AJ | 6922, E88CC, 6DJ8, ECC88, 7308, 6CG7, ECC85 |
| Dual Triode | 9LP / 9H / - | 6FQ7 / 5687 / 6N1P, 6N2P, 6H30P |
| Dual Triode | 8BD | 6SN7, 6SL7, 6080, 6AS7, 6BX7, 6BL7 |
| Triode | 9AG / 6Q | 12B4A / 6J5, 6C5 |
| Pentode | 9CQ / 9AD / 9AQ | EF86, 6267 / 5879 / EF80, EF184 |
| Pentode | 7BK / 7BD / 8N | 6AU6, 6BA6 / 6AK5 / 6SJ7 |

Small-signal sourcing notes:

- Read from RCA, Tung-Sol, GE, Sylvania, Philips, Brimar and JJ sheets (frank.pocnet.net mirror,
  tubebooks.org) cross-checked against TDSL basing codes. 6SL7, 6080, 6BL7, 6BA6 and 6C5 rest on
  their basing code without the sheet opened; 6H30P on a blurry lead drawing - the weakest rows.
- Lookalikes that differ: 6FQ7 has pin 9 NC, not a shield, so it is 9LP, not 6CG7's 9AJ; 5687 is
  not a 12AU7 (plate 1 on pin 9, centre tap on pin 8); 6N2P has a shield on pin 9 and no centre tap,
  so it is not a 12AX7; 6N1P has the 9AJ functions but its sheet numbers the units the other way.
- 12DW7: unit 1 (pins 6-8) is the 12AX7-like section, unit 2 (pins 1-3) the 12AU7-like one.
- Left out: 6C45P (no pin list found), 6N8S (said to equal 6SN7, unit order unconfirmed), 6SF5.

`TubeSocket.datasheet` radio rows, 54 tubes:

| Group | Tubes |
|---|---|
| Converter | 12BE6, 6BE6, 6SA7, 12SA7, ECH81, ECH83, UCH81 |
| Pentode (RF/IF) | 12BA6, 6BD6, 6SK7, 12SK7, 6SG7, EF85 |
| Diode-Triode / Diode-Pentode | 12AV6, 12AT6, 6AV6, 6AT6, 6SQ7, 12SQ7, EABC80 / EBF89, UBF89 |
| Triode-Pentode | UCL82 |
| Beam Tetrode (radio output) | 50C5, 50L6GT |
| Rectifier | 35W4, 35Z5GT |
| Tuning Indicator | EM84, EM80, EM81, 6AF6, 1629 |
| Space-Charge | 12K5, 12AD6, 12EG6, 12AF6, 12CX6, 12DZ6, 12EK6, 12AC6, 12BL6, 12AE6, 12FM6, 12FK6, 12EL6, 12FX8, 12DS7, 12EM6, 12J8, 12DL8 |

`TubeSocket.datasheet` power, rectifier and reference rows, 63 tubes:

| Group | Tubes |
|---|---|
| Power Pentode | EL84, 6BQ5, 7189, 7189A, 6CW5, EL86, EL34, 6CA7, 7591, 6F6G, 6K6GT, 6EH5, 6DL5, EL95, 6AK6, 6AR5, 6CL6 |
| Beam Tetrode | 6V6GT, 6V6, 6L6GC, 6L6, 5881, 7581A, KT66, KT88, 6550, KT120, KT150, 6Y6G, 6AQ5, 6BF5, 50B5, 6973, 12AB5, 6AS5, 6CU5, 35C5, 6CZ5, 6EM5, 6BW6, 6CM6 |
| Triode-Pentode | 6BM8, ECL82, ECL86, 6GW8 |
| Rectifier | 5AR4, GZ34, 5V4G, GZ32, 5Y3GT, 5U4GB, EZ81, EZ80, 6CA4, 6X4, 6X5GT, 6X5, 6AX5GT |
| Voltage Reference | 0A2, 0B2, 0D3, 0C3, 0A3 |

Radio and power sourcing notes:

- Read from RCA, GE, Tung-Sol, Sylvania, Philips, Amperex, GEC/M-OV, JJ, Brimar and Svetlana sheets
  (frank.pocnet.net), with TDSL as the second source. Where TDSL's basing code disagrees with the
  printed data book the book wins: EL34 8ET (TDSL 8EP), 6X4 7CF (5BS), 6F6 7S (7AC), 7591 8KG
  (Sylvania; TDSL 8KQ).
- A pin carrying several electrodes takes the main one's name: K for cathode + g3/beam plates/shield,
  G3 for g3 + shield on 7BK, G5 for shell + g5 on 6SA7, H5 for heater end + triode cathode on 12FX8.
- Space-charge tubes follow each data book's grid numbering: on 12K5, 12DS7 and 12DL8 G1 is the
  space-charge grid and G2 the control grid; on 12J8 and 12EM6 G1 is the control grid.
- Other names introduced: `HT` the panel-lamp heater tap on 35W4/35Z5GT (not a centre tap); `HK8`
  the heater-cathode pin of 5AR4-type rectifiers; `J_1`/`J_2` the base jumper of octal regulators
  (0A3/0C3/0D3), which opens the supply when the tube is pulled; `TA` target, `DE` deflection
  electrode, `1RC`/`2RC` ray-control electrodes of tuning indicators; `DK` a separate diode cathode.
- Rectifier plate numbers follow each book: 5L (Amperex) has plate 1 on pin 4, 5T (RCA) plate 2.
- **KT88 conflict:** the row follows GEC (pin 1 base shell); JJ brings g3 out on pin 1.
- The same-basing test treats `NC`, `IC` and `SH` as interchangeable, because the unused pin 1 of a
  basing is a shell, a no-connect or an internal connection depending on envelope and maker.
- Left out: EF89 (basing code and joined shield pins unconfirmed), 12DK6 (no sheet), metal 6F6
  (shell pin assumed), 6V4 and 6005 (TDSL only), 6AV5 (octal TV sweep tube); 6E5 and 6U5 are on the
  unsupported UX6 base - 1629 is their octal counterpart.

`TubeSocket.datasheet` Soviet rows and EL91, 30 tubes (6N1P and 6N2P are counted with the
small-signal rows):

| Group | Tubes |
|---|---|
| Dual Triode | 6N1P-EV, 6N2P-EV, 6N6P, 6N23P, 6N23P-EV, 6N24P, 6N3P, 6N8S, 6N9S, 6N13S, 6N5S |
| Triode / Power Triode | 6S45P / 6S19P, 6S4S |
| Pentode | 6Zh32P, 6Zh1P |
| Power Pentode | 6P14P, 6P14P-EV, 6P18P, 6P15P, EL91, 6AM5 |
| Beam Tetrode | 6P1P, 6P3S, 6P3S-E, 6P6S, 6P27S |
| Rectifier | 5C3S, 5C4S, 6C4P |

Soviet sourcing notes:

- Ranked by use in guitar and hi-fi DIY (6N2P, 6P14P, 6N23P, 6N1P, 6P3S, 6N8S lead); read from
  Soviet reference sheets (magictubes.ru, r-type.org handbook scans, chipdip, radiolamps.ru) and
  Voskhod/Svetlana/Sovtek sheets.
- Compatibility, never assumed: 6P14P and 6P18P are EL84 pinout; 6P15P is not (g3 + shield on pins 1
  and 6); 6P3S/6P6S are 6L6/6V6 pinout; 6P27S fits an EL34 socket but is a 7AC beam tetrode, not
  8ET; 6N8S/6N9S/6N13S/6N5S are 6SN7/6SL7/6AS7 pinout; 6N23P is 6922 pinout; 6N1P and 6N6P have the
  9AJ functions but no RETMA code; 6Zh32P is EF86; 6Zh1P is close to 6AK5 but carries k + g3 +
  shield on both pins 2 and 7; 6C4P is not a 6X4.
- 5C4S is indirectly heated with its cathode on pin 8, so it takes 5AR4's names. EL91/6AM5 is listed
  as 7BZ by its source but does not match the 6AQ5, which is 7BZ, so its basing is left as `-`.
- Weakest rows: 6S19P and 6P27S (one sheet each), 6S45P (Sovtek sheet only), 5C3S and 6C4P (pinout
  pictures without labels), 6P3S-E and the -EV grades (assumed identical to the base type).
- Left out: 6N30P (lead drawing unlabelled - the existing 6H30P row covers it), 6P43P, 6S41S,
  6Zh52P, 6P9, 6Zh4 (not sourced or conflicting); 6S33S/6C33C, GU-50, GU-19, 6D22S and 6P45S sit on
  bases `TubeSocket` does not have.

Rules for every row: names as in V5-V8; a part is added only after its basing diagram has been read
from a data book or manufacturer sheet, or from two independent sources; equivalents are listed only
where the basing is confirmed identical (the 6L6 family is not uniformly 7AC, and Russian
"equivalents" such as 6N1P are not pin-compatible with 6DJ8).

## 5. Tests

- `TubeSocketDatasheetTest`, after `SIL_ICDatasheetTest`: every row parses and applies; the name
  count equals the base's pin count; designations are unique; within a row no two names share a net
  unless both carry the `_n` suffix; rows sharing a basing code have identical names; `findSeries`
  finds a tube and not a group.
- `TubeSocketTest`: `applyModel` sets base, labels and designation and leaves mount and angle;
  `IC` and `NC` pins stay apart in the netlist.

## 6. Verification

- `mvn -q test` over the whole reactor (core and library change).
- Done: whole reactor passes (core 227, library 508, swing 29).
- Done: 12AX7, EL84, EL34, 5AR4, 6AQ5, 12K5, 0A2 and 6S19P rendered headlessly from the datasheet;
  labels sit by their pins and fit once the suffix is dropped (V11).
- Regression suite: no rendering or netlist change is expected for existing projects (V10); the run
  confirms it.

## 7. Phases

1. **Plan**: this document.
2. **Code**: done - V2, V3, V8 (`IC` in `Node.NON_NET_NAMES`, covered by `NodeNameTest`), V9 (six
   variants retired 2026-11-02T14:00), `TubeSocketTest`, `TubeSocketDatasheetTest`, `@AiNote`s.
   The datasheet test fails until the datasheet exists.
3. **Library**: done - 188 tubes (46 small-signal, 54 radio and space-charge, 58 power, rectifier
   and reference, 30 Soviet and EL91). `catalog_full.json` regenerated; release notes in 6.10
   (datasheet, and the shorted-variant fix).
4. **Pin labels**: done - V13 ("Pin Names"), V14 ("Pin Labels"), factory presets re-keyed,
   `TubeSocketTest` covers the old-file default; rendered in all three modes.
5. **Later, on request**: the UX4/jumbo 4-pin bases (300B, 2A3, 211, 845), magnoval.
