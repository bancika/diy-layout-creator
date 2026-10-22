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
| Leaded | `Resistor`, `DiodePlastic`, `AxialFilmCapacitor`, `DiodeGlass` (body between the points, standing when closer than 'Length'; the last two also name 'Reverse (standing)'), `AxialElectrolyticCapacitor` (no standing mode), `LED` (own 'Lead Spacing', 'Size' is the lens) |
| Radial | `AbstractRadialComponent` ('Lead Spacing'), `RadialFilmCapacitor` (same, plus 'Length'/'Width' vs datasheet), `RadialElectrolytic` ('Diameter', 'Height' only when folded), `RadialCeramicDiskCapacitor` (disc diameter and thickness), `TantalumCapacitor`, `RadialInductor`, `Buzzer` (same 'Diameter'/'Height' note as the electrolytic) |
| Multi-pin | `TransistorTO92`, `PotentiometerPanel` (view, anchor, lug direction per orientation, 16mm/25mm sizes), `DIL_IC` |
| Connectivity | `Jumper`, `HookupWire`, `CopperTrace`, `CurvedTrace`, `SolderPad`, `Dot` (selection notes that name each other; both traces are copper along their whole length; Dot connects nothing), `TwistedWire` (selection note: one conductor like Hookup Wire, for looks only; use a pair of Hookup or Right Angle Wires for an analyzable circuit) |
| Terminals | `Eyelet`, `Turret` (every end inside the circle connects), `GroundSymbol` (all symbols are one GND node) |
| Guitar | `AbstractGuitarPickup` (coils horizontal in DEFAULT, clockwise rotation), `AbstractSingleOrHumbuckerPickup` ('Coil(s)' values; coil direction the wiring analysis takes as in phase), `SingleCoilPickup` (terminals along the bottom edge), `HumbuckerPickup`, `P90Pickup`, `JazzBassPickup`, `PBassPickup`, `StingrayBassPickup` (terminals down the top-right edge; all turn with the body; the humbucker and Stingray also give the in-phase series and parallel connections), `LeverSwitch`, `LPSwitch`, `FreewayBlade`, `SchallerMegaSwitch`, `Freeway3x3_03`, `Freeway3x3_05` (terminal layout relative to the anchor), `S1Switch` (lug grid around the disc; same 'Orientation' as its pot) |
| Lugs and sockets | `MiniToggleSwitch` (pole grid; HORIZONTAL turns it counterclockwise), `TrimmerPotentiometer` (wiper offset per 'Type'), `OpenJack1_4` (lugs around a centre 0.5in below the Tip lug), `TubeSocket` (anchor is the centre; pins clockwise from the gap or key), `CliffJack1_4` (named lugs on a square, nose direction), `PilotLampHolder` (which holes belong to which lug; the two holes of a lug are not joined), `FuseHolderPanel` (lug layout; 'Fuse' must be on for the lugs to conduct) |
| Misc | `BOM`, `TraceCut` (on-hole cut leaves the hole unconnected; 'Cut Orientation' is the cut, not the strip), `TransformerCoil` (winding side per orientation) |

`Label` deliberately has none: its alignment properties already say how the text sits on the anchor.

How the netlist connects things, which several notes rely on (`NetlistBuilder`): control points at
the same coordinates are one node; a copper area connects every terminal it covers and every copper
area it overlaps on the same layer; an `IContinuity` part such as a wire joins its own points. A
component with a null node name, like Dot, takes no part.

Open items:

- `PotentiometerPanel`: the size note says the choice depends on the type of circuit; an example of
  which circuits take which size would make it actionable.
- `ProtoBoard` (P-0+o): the central V+/GND/V- bus is not described; its trace geometry does not line
  up obviously with the pad columns and needs reading carefully before it can be noted.
- `ProtoBoard780` and `EurorackStripboard`: no notes. 780 shifts its grid by `X_OFFSET` and half a
  pitch when `translateToGrid` is set; Eurorack has an irregular pad map.
- The fuse symbol never joins its two ends in the netlist, so a fuse drawn in a schematic reads as
  open. `FuseHolderPanel` now conducts when 'Fuse' is on; decide whether the symbol should too.
- `Breadboard` draws both power rails as `+` then `-` left to right rather than mirrored: on each
  side the red line runs beside the outer rail column and the blue beside the inner one. The rail
  note matches the drawing; confirm it matches real boards.

## 4. Priority

Ranked by the number of the 709 sample projects in `diylc-regression-data/input` that use the type,
with total instances in brackets. The set is weighted toward older cloud and V1 projects (amps,
pedals, strip and perf builds) and under-represents maker modules. Tier 1 (Label, Resistor, Solder
Pad, Copper Trace, the radial caps, TO-92, Jumper, Plastic Diode, BOM, Hookup Wire, Panel
Potentiometer, DIL IC; 100+ projects each) is done, see section 3.

### 4.1 Tier 2: common in particular build styles (40-90 projects)

Done, see section 3.

### 4.2 Tier 3: guitar, amp and chassis (15-30 projects)

Done, see section 3. `CapacitorSymbol`
and `ResistorSymbol` deliberately have none: the index lists them as "Capacitor (Schematic)" and
"Resistor (Schematic)", their geometry is that of any two-lead part, and the pinout gives the
capacitor's polarity.

### 4.3 Outside the sample

Maker boards, breakout modules and displays barely appear in the sample. Maker support is still
emerging, so they come last: established circuit types (amps, pedals, guitar wiring, strip and perf
builds) are covered first.

## 5. Next steps

1. The rest of the established circuit types below Tier 3 in the usage count.
2. The board leftovers and the `PotentiometerPanel` items in section 3.
3. Maker components, once maker support settles: list them and see what the board notes already
   cover before writing any.

After each batch, read the entries back as the model sees them. Run `ComponentCatalogGenerator`
with an output directory outside the repository so the committed catalogs stay untouched;
`ComponentCatalogGeneratorTest` covers note collection. Launch it with the `--add-opens` and
`--add-exports` flags from `.run/DIYLCStarter.run.xml`: without them `ReleaseHistory` cannot read
`update.xml` and the catalogs silently carry version 5.0.0.

## 6. Measuring usage

The ranking counts direct children of `<components>` in each `.diy` file under
`diylc-regression-data/input`, once per project and once per instance. Element names are XStream
aliases: `diylc.<package>.<Class>`, with `_` in class names escaped as `__` (`DIL__IC` is `DIL_IC`).
Rerun the count when the sample set grows or a different corpus is available.
