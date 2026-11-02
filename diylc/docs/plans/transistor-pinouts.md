# Transistor Pinouts and Datasheet Models

Giving every transistor package a pinout, and applying a known part's pinout from a datasheet.

## 1. Goal

Builds on [DIP IC Pin Names](dip-ic-pin-names.md). A transistor whose pins are anonymous gives the
netlist, the AI and the schematic view nothing to tell a base from a collector. The packages that
extend `AbstractTransistorPackage` (TO-1, TO-92, TO-126, TO-220) already have a "Pinout" property,
but users rarely set it: of 35 package transistors in the sample corpus, 6 have a pinout. A
datasheet of the parts DIYers actually use (guitar pedals, effects, hi-fi, radio, synth) sets the
pinout and the value in one step, from the toolbox or from the AI.

TO-3 was the one package without a pinout: a separate class with two numbered pins and no way to
reach the case, which is the collector.

## 2. Design decisions

| # | Decision | Choice | Rationale |
|---|---|---|---|
| T1 | Datasheet row | `group, part, pinout`, e.g. `BJT (NPN),2N3904,BJT_EBC`. The pinout column is a `TransistorPinout` constant name | The enum already carries everything a part needs. Constant names are unambiguous and greppable; the display strings (`E-B-C     BJT`) are not. |
| T2 | Model identity | `datasheetModelColumn = 1`, `datasheetCreationStepCount = 2` on every package, as on the ICs | The toolbox groups by family while the AI names the part. |
| T3 | `applyModel` | Sets value and pinout only, implemented once in `AbstractTransistorPackage` (and in `TransistorTO3`, which does not extend it). Each package has a `(String[] model)` constructor | The datasheet says what the part is, not how it is drawn. |
| T4 | Pin order | A pinout lists control points in order, and control point 0 is pin 1: for TO-92 facing the flat side, for TO-126 and TO-220 facing the printed front, legs down, left to right. Rows are written as the manufacturer's pin 1, 2, 3 | Already the convention of the TO-92 and TO-220 `@AiNote`s, so existing projects keep their meaning. |
| T5 | One package per datasheet | A part goes in the datasheet of the package it ships in. `TransistorTO1` is the only metal can, so it also holds TO-18, TO-5, TO-39 and the germanium cans | Datasheets are per class. |
| T6 | SPICE mapping | `TransistorPinout.mapToSpiceNode` places each letter in SPICE order (C B E, D G S); regulators keep their own order | Replaces a hand-written switch with fall-throughs that had no `BJT_ECB` case, so ECB parts went out in the wrong order. Shared by `AbstractTransistorPackage` and `TransistorTO3`. |
| T7 | TO-3 case | A third control point at the centre of the mounting hole on the side the pins are offset towards. The pinout lists pin 1, pin 2, case (`BJT_BEC` for a 2N3055) | A wire or solder lug at the hole puts the collector into the netlist. Without it a TO-3 collector could never be connected. |
| T8 | TO-3 compatibility | Old files hold a two-point `controlPoints`; `getControlPoints()` extends it lazily and places the case point from the two pins. `TransistorTO3` keeps extending `AbstractLabeledComponent` | Moving it under `AbstractTransistorPackage` would change field types. Lazy migration is the established pattern. |
| T9 | TO-3 display | New `transistorDisplay` field (`TransistorDisplay`), shown as "Display", read lazily from the deprecated `display` (`Display`) field | Adds the Pinout option the other packages have without changing a serialized field's type. All old `Display` values exist in `TransistorDisplay`. |
| T10 | Pins over the body | Every package draws its pins after the body: `AbstractTransistorPackage.drawPins` for TO-1, TO-92, TO-126 and TO-220, its own loop in `TransistorTO3`. Round pins in the TO-92 colours (`#00B2EE`, border one shade darker), outline only in outline mode; folded TO-126/TO-220 leads use the same colours instead of `METAL_COLOR`. TO-3 pinout letters sit beside each pin and beside the case hole, towards the body centre | Bodies used to hide the pins at full alpha and tint them below it, so pin colour varied with the body, and the folded leads were grey. Now pins look the same on every package and show at any alpha, as on `MultiwattIC`. |
| T11 | New pinout values | Added only when a sourced part needs them, inserted next to their family in the enum | Values are serialized by name, so order is free. Added: `BJT_BEC` (TO-3 transistors, MPSH10), `BJT_CEB` (BF199, BF494), `REGULATOR_AIO` (LM337T, LM317K), `REGULATOR_IOG` (TO-3 fixed regulators). |

### Explicitly out of scope

- TO-247, TO-3P, TO-264 packages (2SC5200, TIP3055, IRFP240, 2SK1058) - no package in DIYLC yet.
- SCRs, triacs and TL431-style references: their roles are not in `TransistorPinout`.
- SPICE export of regulators (`getPrefix` returns `R`, a resistor). Pre-existing, left as is.
- Translating datasheet group names, as for the ICs.

## 3. Changes

### diylc-library

- `TransistorPinout`: `mapToSpiceNode(int)` (T6), `BJT_BEC` (T7).
- `AbstractTransistorPackage`: `implements IDatasheetSupport`, `applyModel`; `mapToSpiceNode`
  delegates to the enum; `drawPins` (T10).
- `TransistorTO1`, `TransistorTO92`, `TransistorTO126`, `TransistorTO220`: datasheet attributes on
  the descriptor, a model constructor, and pins drawn through `drawPins` after the body (T10).
- `TransistorTO3`: case point (T7, T8), `pinout`, `ISpiceMapper`, `IDatasheetSupport`, model
  constructor, `transistorDisplay` (T9), pins over the case and pinout letters (T10).
- `datasheets/TransistorTO1.datasheet`, `TransistorTO3`, `TransistorTO92`, `TransistorTO126`,
  `TransistorTO220`: rows per T1, each read from a manufacturer pin table or two independent
  sources.

### Generated

- `catalog_full.json` regenerated (run `ComponentCatalogGenerator` from `diylc-swing`; with a
  hand-built classpath it reads the version as 5.0.0, so check that the `version` line matches the
  latest release in `update.xml`).
- `diylc-library/src/test/resources/pinout-drivers.txt`: TO-3 is driven by "Pinout".

## 4. Datasheet inventory

Groups: `BJT (NPN)`, `BJT (PNP)`, `Darlington (NPN)`, `Darlington (PNP)`, `Germanium (NPN)`,
`Germanium (PNP)`, `JFET (N)`, `JFET (P)`, `MOSFET (N)`, `MOSFET (P)`, `Regulator`.

`TransistorTO1.datasheet` holds 31 parts, all `BJT_EBC`:

| Group | Parts |
|---|---|
| BJT (NPN) | BC107, BC108, BC109, 2N2222A, 2N2369A, 2N2219A, 2N1613, 2N1711, 2N3053, 2N3866, BFY51 |
| BJT (PNP) | BC177, BC178, BC179, 2N2907A, 2N2905A |
| Germanium (NPN) | AC127, AC176, AC187, 2N1308 |
| Germanium (PNP) | AC126, AC128, AC188, OC44, OC45, OC71, OC75, OC76, OC81, 2N404, 2N1305 |

TO-1 sourcing and modelling notes:

- Read from Mullard/Valvo data-book scans (AC, OC), Central and Philips datasheets (silicon cans),
  and TI bulletin DL-S 1136 (2N1305/2N1308). 2N2219A, 2N2905A and BFY51 were seen only through
  search snippets of the Philips sheets - the weakest rows.
- Seen from the bottom, E-B-C run clockwise from the tab, so from above (DIYLC's view) they run
  counter-clockwise: control points 0, 1, 2 in the default orientation. The AC cans have B at the
  apex of the triangle, matching control point 1. No tab is drawn.
- The OC series are glass envelopes with in-line leads (wider B-C gap, red dot at the collector), not
  TO-1 cans; they are listed here because TO-1 is DIYLC's only small round package and the lead
  order is the same.
- On 2N1305/2N1308 the case is the base, not the collector.
- Left out, no manufacturer drawing or two agreeing sources: AC125, OC72, OC139/140, NKT275, 2N527,
  2N388A, 2N2613, 2SB75, 2SB175, AC151, 2N5320. Soviet MP39-MP42 use a flanged can that is not
  TO-1-like. 2N4416 has four leads (S-D-G-case) and cannot fit.

`TransistorTO92.datasheet` bipolar rows, 68 parts:

| Group | Pinout | Parts |
|---|---|---|
| BJT (NPN) | `BJT_EBC` | 2N3904, 2N4401, PN2222A, 2N4124, 2N5088, 2N5089, 2N5210, 2N5551, MPSA06, MPSA18, MPSA42, MPS5179, S8050, S9013, S9014, S9018 |
| BJT (NPN) | `BJT_CBE` | BC546-BC550, BC337, BC182, BC184, BC239, P2N2222A |
| BJT (NPN) | `BJT_ECB` | BC639, BC182L, BC184L, 2SC1815, 2SC945, 2SC2240, 2SC1845, KSC1845, 2SC2458 |
| BJT (NPN) | `BJT_CEB` / `BJT_BEC` | BF199, BF494 / MPSH10 |
| BJT (PNP) | `BJT_EBC` | 2N3906, 2N4403, PN2907A, 2N4126, 2N5087, 2N5401, MPSA56, MPSA92, S8550, S9012, S9015, KSP2907A |
| BJT (PNP) | `BJT_CBE` | BC556-BC560, BC327, BC212, BC214 |
| BJT (PNP) | `BJT_ECB` | BC640, 2SA1015, 2SA970, 2SA992, KSA992, KSP2907AC |
| Darlington (NPN) / (PNP) | `BJT_EBC` | MPSA13, MPSA14 / MPSA63, MPSA64 |

TO-92 sourcing notes:

- **Philips/NXP (SOT54) and CDIL number TO-92 pins in reverse**, from the right as seen facing the
  flat side; onsemi, Central, Toshiba, NEC, UTC and MCC number from the left. Philips "1 emitter 2
  base 3 collector" on a BC547 is physically C-B-E. Copying Philips or CDIL numbers literally is the
  usual source of wrong pinouts online; rows here are the physical order. onsemi CASE 29 style 1 =
  EBC, style 14 = ECB, style 17 = CBE.
- Same name, different pinout, each listed under its full name: BC182L/BC184L (ECB) vs BC182/BC184
  (CBE); P2N2222A (CBE) vs PN2222A (EBC); KSP2907AC (ECB) vs KSP2907A (EBC); MPS5179 (EBC) vs
  PN5179 (BEC, not listed). onsemi's MPSA92 package page prints style 14, contradicting its own
  symbol and Central (EBC).
- 2N5179 itself is TO-72 only. 2SC2458 is Toshiba's smaller 2-4E1A body at 1.27 mm pitch.
- Weakest rows: BF494 (one Philips sheet plus a search quote), 2SC1845 and 2SA992 (second-source
  and NEC text copies), 2N4124/2N4126 (Central only), BC239 (CDIL only).
- Left out: BC212L/BC214L (CDIL table is an image; inferred only), 2SC828 (second source only),
  PN5179, KTC3198/KTA1266 (mirror previews only), 8050S/8550S (ECB at UTC; 8550S not read).

`TransistorTO126.datasheet` holds 22 parts, all `BJT_ECB`: BD135/137/139, BD136/138/140,
BD237/238, MJE340/350, MJE170/180, 2SC3423/2SA1360, KSC3503/KSA1381, 2SD669A/2SB649A, BF469/470,
and the BD681/682 darlingtons.

`TransistorTO220.datasheet` holds 67 parts:

| Group | Parts | Pinout |
|---|---|---|
| BJT (NPN) / (PNP) | TIP29C-TIP42C, MJE3055T/2955T, MJE15028-15033, BD243C/244C, BD911/912 | `BJT_BCE` |
| Darlington (NPN) / (PNP) | TIP120-122, TIP125-127 | `BJT_BCE` |
| MOSFET (N) / (P) | IRF510, IRF520, IRF530, IRF540N, IRF610, IRF640, IRF840, IRFZ44N, IRLZ44N, IRF3205, FQP30N06L; IRF9530, IRF9540N, IRF9610, IRF9640 | `MOSFET_GDS` |
| Regulator (+) | 7805-7824 and LM2940CT-5.0/-12 | `REGULATOR_IGO` |
| Regulator (+) | LM317T, LM338T, LM350T, LM1084/1085/1086-ADJ, LM1117T-ADJ, LD1117V | `REGULATOR_AOI` |
| Regulator (+) | LM1086-3.3/-5.0, LM1117T-3.3/-5.0, LD1117V33/V50 | `REGULATOR_GOI` |
| Regulator (-) | 7905, 7912, 7915 / LM337T | `REGULATOR_GIO` / `REGULATOR_AIO` |

`TransistorTO3.datasheet` holds 27 parts; the pinout's third letter is the case:

| Group | Parts | Pinout |
|---|---|---|
| BJT (NPN) / (PNP) | 2N3055, 2N3442, 2N3773, MJ15003, MJ15015, MJ15022, MJ15024; MJ2955, 2N6609, MJ15004, MJ15016, MJ15023, MJ15025 | `BJT_BEC` |
| Darlington (NPN) / (PNP) | MJ11016, MJ11032; MJ11015, MJ11033 | `BJT_BEC` |
| MOSFET (N) | IRF150 | `MOSFET_GSD` |
| Regulator (+) | LM317K, LM338K, LM350K | `REGULATOR_AIO` |
| Regulator (+) | LM309K, LM323K, LM340K-5.0/-12/-15 | `REGULATOR_IOG` |
| Regulator (-) | LM337K | `REGULATOR_AOI` |

Power package sourcing notes:

- Read mostly from onsemi (TO-225 style 1, TO-220 CASE 221A styles 1 and 5, TO-3 CASE 1-07 style 1)
  and TI regulator pin tables. Rows taken from a family rule rather than the part's own sheet:
  BD136/138/140, MJE170/180, BD682, BD238, 2SA1360, BF469, TIP29C/30C, BD911/912, the IRF parts
  other than IRF510/IRF540N, MJ15015/16, 2N6609, LM350K. IRF150 is from two search snippets.
- TO-3 geometry from onsemi CASE 1-07 (with ST and Central agreeing): pins 10.92 mm apart on a line
  square to the hole axis, about 1.8 mm off centre towards the nearer hole. Seen from above with the
  holes left and right and the pins left of centre, pin 1 is the upper pin - control point 0 in the
  default orientation, so the TO-3 geometry needed no change. ST numbers TO-3 pins the other way
  round (its LM137 "1 = Output"); the rows follow onsemi/TI/National, whose pin functions match ST's
  physically.
- LM309K/LM323K/LM340K: TI and National draw the pin positions but give no pin numbers; numbers were
  assigned from the physical position. The weakest TO-3 rows.
- Left out: TIP142/147 (SOT-93/TO-247 at onsemi), LM2941 (5 leads), ECX10N20/P20 (TO-247), the
  lateral 2SK134/135 and 2SJ49/50 (case is probably source, not drain - no Hitachi sheet found), and
  TO-3 79xx (unsourced).

`TransistorTO92.datasheet` FET and regulator rows, 41 parts:

| Group | Pinout | Parts |
|---|---|---|
| JFET (N) | `JFET_DSG` | J201, J202, J111, J112, J113, J310, 2N5457, 2N5458, 2N5459, 2N5484, 2N5485, 2N5486, PN4416, MPF102, BF245A/B/C |
| JFET (N) | `JFET_SGD` / `JFET_GSD` / `JFET_DGS` | 2N3819 / BF256B / 2SK170, LSK170 |
| JFET (P) | `JFET_DGS` / `JFET_SDG` | J174, J175, J176, 2N3820 / 2N5460 |
| MOSFET (N) | `MOSFET_SGD` / `MOSFET_DGS` | 2N7000, LND150 / BS170 |
| Regulator (+) | `REGULATOR_OGI` | 78L05, 78L09, 78L12, 78L15, LP2950-5.0, LM2936Z-5.0 |
| Regulator (+) | `REGULATOR_AOI` / `REGULATOR_IOA` | LM317LZ / LR8 |
| Regulator (-) | `REGULATOR_GIO` / `REGULATOR_AOI` | 79L05, 79L12, 79L15 / LM337LZ |

TO-92 FET and regulator notes:

- TI's TO-92 outline (LP0003A) and its TL431, LM317L and LM385 tables number from the right, like
  Philips; the parts are physically the same as onsemi's. Rows are the physical order.
- JFET makers often differ by a drain/source swap (InterFET lists J201, J310, 2N5457, 2N5484-86 and
  PN4416 as S-D-G; Fairchild, Linear Systems, Central and onsemi as D-S-G). The gate position, which
  is what matters, agrees, and the rows follow the majority. 2N3819: S-G-D at onsemi, InterFET and
  Vishay, D-G-S at Central. J174/J175: D-G-S at Linear Systems, S-G-D at InterFET.
- **BF245 is a real conflict**: Philips (the original, the classic European part) is D-S-G, onsemi
  is G-S-D - the gate at opposite ends. Both are discontinued; the rows follow Philips. A layout
  built around an onsemi BF245 needs `JFET_GSD`.
- BS170 follows onsemi's TO-92 (D-G-S); Diodes' "TO-92 compatible" E-Line BS170P is marked on the
  rounded face and is S-G-D seen from the flat side.
- LND150 is from Microchip's numbered pin table. LR8 (IN, OUT, ADJ - not the LM317 order) is from
  the same Supertex 3D drawing, read with the LND150 as calibration; the weakest row in the file.
  2N5459 and 2N3820 rest on one sheet each.
- Left out: MPF4393, 2SK30A, 2SK117, 2SJ74, LSJ74, VN10KM (unsourced); BS250 and VP2106 (drawing
  only, not requested); 79L09 (not a listed voltage); TL431, LM385 and LM336 (roles are not in
  `TransistorPinout`).

## 5. Tests

- `TransistorPinoutTest`: SPICE order per type, including the `BJT_ECB` fix; every value is a
  permutation of its type's roles.
- `TransistorTO3Test`: case point sits in a mounting hole in every orientation; a two-point
  instance gains the case point where a new one has it; node names follow the pinout; display is
  read from the legacy field.
- `TransistorDatasheetTest`: across all five datasheets, every row has three columns, parses and
  applies; part numbers unique per package; `findSeries` finds a part and not a group.

## 6. Verification

- Done: `mvn -q test` over the whole reactor passes (core 227, library 504, swing 29).
  `pinout-drivers.txt` was updated because TO-3 terminals are now driven by "Pinout".
- Done: TO-3 rendered headlessly in all four orientations at 100 % and 50 % alpha with Display =
  Pinout - pins show over the case, the case point sits in the pin-side hole, letters beside each.
  All five packages rendered standing, folded and at 50 % alpha: pins on top in one colour.
- Open: the regression suite. Every project with a transistor package now renders differently
  (pins visible over the body, blue folded leads), TO-3 projects carry a third, normally
  unconnected, node, and ECB parts change SPICE order. Reference outputs are regenerated only after the diffs are confirmed to
  be exactly that.

## 7. Phases

1. **Pinout everywhere**: done (T6 to T11, `TransistorPinoutTest`, `TransistorTO3Test`, `@AiNote`s on
   pin order for TO-1, TO-126 and TO-3).
2. **Datasheet support**: done (T1 to T3, `TransistorDatasheetTest`).
3. **Library**: done - 256 parts: TO-92 109, TO-220 67, TO-1 31, TO-3 27, TO-126 22.
   `catalog_full.json` regenerated. Further parts on request, each read from a manufacturer pin
   table first.
4. **Next**: the regression run of §6; then, if wanted, the TO-247/TO-3P package (2SC5200/2SA1943,
   TIP3055/2955, IRFP240/9240, lateral MOSFETs) and a role set for shunt references (TL431).
