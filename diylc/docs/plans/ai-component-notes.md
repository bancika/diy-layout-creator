# AI Component Notes

Hand-written `@AiNote`s for the component types the model uses most.

## 1. Goal

`ComponentCatalogGenerator` publishes every component type to the model with its editable
properties and, through `PinoutAnalyzer`, its terminals. What neither can say is how the part is
physically laid out relative to its control points, and when to pick it over a similar type. A note
carries exactly that, nothing more.

Notes are collected from the class and every superclass, most general first. `Scope.SELECTION`
notes also go into `catalog_index.json`, which is all the model sees when it chooses types;
`Scope.USAGE` (the default) goes only into the full catalog entry.

## 2. What belongs in a note

A note earns its place only if the model would otherwise guess wrong. Before writing one, check that
it is not already said by:

- **a property name or enum value**: `Orientation`, `Polarized`, `Type` values are self-explanatory;
- **the description** in `@ComponentDescriptor`;
- **the pinout block**: pin labels, polarity labels (`+`/`-`, `A`/`K`), lug numbering and which lugs
  a switch bridges in each position are all generated. Do not restate them.

What is left, and what notes should carry:

| Kind | Scope | Example |
| --- | --- | --- |
| Choosing between similar types | SELECTION | Blank Board as the base for eyelet and turret builds; Eyelet Board only when asked |
| Grid and anchor geometry | USAGE | First hole centre sits one `Spacing` in from the top and left edges |
| Positions of pins relative to the anchor | USAGE | Breadboard rows and columns as offsets from the control point |
| Connectivity the terminals do not show | USAGE | Which holes of a strip or rail are joined |
| Defaults that must change on creation | USAGE | Use `Explicit` dimension mode when placing a board |

Pin geometry is out of scope for the pinout catalog, which is why it is the most valuable thing a
note can add.

Writing rules:

- One fact per note, one sentence where possible. Refer to properties by their display name in
  single quotes and to enum values by `name()` (`DEFAULT`, `_90`, `ShaftDown`), which is how the
  catalog lists them.
- Take every number and position from the `draw()` and control-point code, not from the real part.
  Where the drawing and the real part disagree, fix the drawing or say nothing; never note around it.
- Put a note on the narrowest class it is true for. A base class gets a note only if it holds for
  every subclass, including schematic symbols and body-less types. `AbstractLeadedComponent` gets
  none: Jumper, Copper Trace and Line have no body, a dozen schematic symbols extend it, radial
  types take lead length from 'Lead Spacing' instead of the body, and only Resistor, both diodes and
  Axial Film support standing mode.
- Advice about another type belongs only in a SELECTION note that chooses between the two. A note
  that ties one type's geometry to another's (an IC straddling a breadboard gap) is noise for most
  layouts and rots when either type changes.
- ASCII only (`x` for multiplication, `-` for ranges); the sources are ISO-8859-1. Many component
  files have CRLF line endings, and an edit must keep them.
- Leave a type without notes rather than pad it. No captions, no restated descriptions.

### Description vs SELECTION note

The index entry carries only the type's name, class and SELECTION notes, not its description. The
description is what people read in the toolbox and tree tooltips and is not localized. So keep the
description a short phrase for people, and put "when to use this instead of X" in a SELECTION note
that names the alternative; alternatives should point at each other (Jumper and Hookup Wire do).

## 3. Current coverage

| Area | Types with notes |
| --- | --- |
| Boards | `AbstractBoard`, `BlankBoard`, `PerfBoard`, `EyeletBoard`, `MarshallPerfBoard`, `VeroBoard`, `TriPadBoard`, `Breadboard`, `ProtoBoard`, `TerminalStrip`, `TagStrip` |
| Leaded | `Resistor`, `DiodePlastic` (body between the points, standing when closer than 'Length') |
| Radial | `AbstractRadialComponent` ('Lead Spacing'), `RadialFilmCapacitor` (same, plus 'Length'/'Width' vs datasheet), `RadialElectrolytic` ('Diameter', 'Height' only when folded), `RadialCeramicDiskCapacitor` (disc diameter and thickness) |
| Multi-pin | `TransistorTO92`, `PotentiometerPanel` (view, anchor lug, lug direction per orientation, 16mm/25mm sizes), `DIL_IC` |
| Connectivity | `Jumper`, `HookupWire`, `CopperTrace`, `SolderPad` (selection notes that name each other) |
| Misc | `BOM` |

`Label` deliberately has none: its alignment properties already say how the text sits on the anchor.

Open items:

- `PotentiometerPanel`: the size note says the choice depends on the type of circuit; an example of
  which circuits take which size would make it actionable. The sentence naming the anchor lug per
  view repeats the pinout block, which already maps index 0 to "3" in the back views; drop it.
- Tantalum, Radial Inductor and PCB Buzzer have the same 'Diameter'/'Height'/'Folded' properties as
  `RadialElectrolytic`; check whether its note applies to them as written.
- `ProtoBoard` (P-0+o): the central V+/GND/V- bus is not described; its trace geometry does not line
  up obviously with the pad columns and needs reading carefully before it can be noted.
- `ProtoBoard780` and `EurorackStripboard`: no notes. 780 shifts its grid by `X_OFFSET` and half a
  pitch when `translateToGrid` is set; Eurorack has an irregular pad map.
- `Breadboard` draws both power rails as `+` then `-` left to right rather than mirrored. Confirm
  this is intended before relying on the rail note.

## 4. Priority

Ranked by the number of the 709 sample projects in `diylc-regression-data/input` that use the type,
with total instances in brackets. The set is weighted toward older cloud and V1 projects (amps,
pedals, strip and perf builds) and under-represents maker modules. Tier 1 (Label, Resistor, Solder
Pad, Copper Trace, the radial caps, TO-92, Jumper, Plastic Diode, BOM, Hookup Wire, Panel
Potentiometer, DIL IC; 100+ projects each) is done, see section 3.

### 4.1 Tier 2: common in particular build styles (40-90 projects)

| Type | Projects | Note should cover |
| --- | --- | --- |
| Dot | 87 | Schematic and wiring junctions only, not a pad |
| Mini Toggle Switch | 76 | Lug grid geometry relative to the anchor |
| Trimmer Potentiometer | 74 | Lug geometry per type |
| Trace Cut | 68 | On a hole vs `Cut Between Holes`; pairs with the Vero Board note |
| Curved Trace | 65 | Curved Trace vs Copper Trace, joining the connectivity selection notes |
| Axial Film | 56 | The Resistor standing note applies as written |
| Axial Electrolytic | 63 | Body between the points; no standing mode |
| Ground Symbol | 59 | Schematic only |
| Open 1/4" Jack | 57 | Lug geometry relative to the anchor |
| Tube Socket | 50 | Pin circle direction and key position |
| Eyelet / Turret | 46 / 39 | Placed on a Blank Board; one per connection point |
| LED | 44 | Has its own pin spacing, not the body; read before noting |

### 4.2 Tier 3: guitar, amp and chassis (15-30 projects)

Cliff Jack, Glass Diode (Resistor standing note applies), Single Coil Pickup, Capacitor and Resistor
symbols, Twisted Wire, Pilot Lamp Holder, Transformer Coil, Fuse Holder.

### 4.3 Outside the sample

Maker boards, breakout modules and displays barely appear in the sample because they postdate most
of it, but they are where the model is most likely to misplace or misorient a part. Treat them as
Tier 2.

## 5. Next steps

1. Close the Tier 1 open items in section 3.
2. Curved Trace and Dot, completing the connectivity selection notes.
3. Tier 2 electromechanical: Mini Toggle, Trimmer, jacks, Tube Socket, Trace Cut.
4. The leaded leftovers: Axial Film, Glass Diode, Axial Electrolytic, LED.
5. Maker components.
6. The board leftovers in section 3.

After each batch, read the entries back as the model sees them. Run `ComponentCatalogGenerator`
with an output directory outside the repository so the committed catalogs stay untouched;
`ComponentCatalogGeneratorTest` covers note collection.

## 6. Measuring usage

The ranking counts direct children of `<components>` in each `.diy` file under
`diylc-regression-data/input`, once per project and once per instance. Element names are XStream
aliases: `diylc.<package>.<Class>`, with `_` in class names escaped as `__` (`DIL__IC` is `DIL_IC`).
Rerun the count when the sample set grows or a different corpus is available.
