# Component Pinout Catalog

Teaching the LLM what terminals a component type has before any instance of it exists.

## 1. Goal

`ComponentCatalogGenerator` produces `catalog_full.json` and `catalog_index.json`
offline. The files are uploaded once to the diy-fever.com backend, which reads
out only the entries it needs when serving a request. Each entry today carries
the type's identity, its category, its creation method and its editable
properties.

It carries nothing about terminals. For components already on the canvas the
model gets terminals from `AiProjectBuilder`, which emits an `AiTerminal` per
sticky control point with its index, its node name and its position. For a type
the model has never seen instantiated there is no equivalent, so when it writes
an `AiEditOperation` with `fromTerminal: "SW1.3"` it is guessing.

The guess is only safe for the trivial cases. A resistor is always two pins. An
axial electrolytic is `+` and `-` when polarized and `1`/`2` when not. A mini
toggle switch is anywhere from two to fifteen lugs depending on its `Type`, and
which lug does what changes with it.

This document plans a `pinout` block on every catalog entry that needs one,
derived from the code rather than declared by hand.

## 2. What the code actually does

The numbers below come from a probe run over the whole library: instantiate
every discovered `ComponentType`, sweep every non-read-only enum and boolean
editable property one value at a time, and record the ordered list of sticky
control points with their node names.

| Measurement | Value |
| --- | --- |
| Component types discovered by `ComponentProcessor` | 144 |
| `@ComponentDescriptor` annotations in the source tree | 153 |
| Types whose sticky pinout changes with at least one property | 51 |
| Types with more than one independent driver | 8 |
| Types implementing `ISwitch` | 12 |

The first sweep covered enums and booleans only and found 49. Adding the integer sweep of §2
brought in `TagStrip` and a second driver on `TerminalStrip`.

The eight multi-driver types are `DIL_IC` (pin count and numbering scheme),
`SIL_IC`, `SubminiTube` (lead count, folded, pin arrangement),
`SwitchLatchingSymbol` (poles and configuration), `AudioTransformer` (two
booleans), and the tube symbols `TriodeSymbol`, `PentodeSymbol` and
`DuoDiodeSymbol` (heaters and directly-heated, plus suppressor grid on the
pentode).

Four facts from the probe shape everything below.

**Sticky is the only filter that matches the project view.** Raw
`getControlPointCount()` over-counts. The moveable label anchor on most leaded
components is a control point, so a plain resistor reports three points, not
two. `AiProjectBuilder.mapComponent` already skips non-sticky points, and the
catalog must use the identical filter or the indices in the catalog will not be
the indices the model sends back in a terminal reference. Filtering on sticky
also removes the false driver "Moveable Label", which otherwise shows up on
thirty-two types that have no electrical variation at all.

**Indices are not contiguous.** `RotarySwitchOpen` and `RotarySwitchSealed`
have a non-sticky point at index 0, so their first terminal is index 1.
`TubeSocket` is the same. The pinout must carry explicit indices, not a bare
ordered list.

**Some drivers are unbounded integers.** `TerminalStrip.getTerminalCount()`,
`TagStrip.getTerminalCount()`, `PinHeader.getRowCount()` and
`PinHeader.getColumnCount()` are plain `int` editable properties that call
`updateControlPoints()`. There is no enum to enumerate. Any design that assumes
a finite value domain silently omits these.

**Some node names are unusable as pin labels.** See §6.

## 3. Design decisions

| # | Decision | Choice | Rationale |
|---|---|---|---|
| D1 | How drivers are identified | Derived by probing, not declared by annotation | An annotation is correct in spirit but rots. There is no test that fails when a new component ships without it, and 8 of 49 types need more than one annotation anyway. Derivation re-reads the truth on every build. |
| D2 | Which points count as terminals | Sticky control points only | Identical filter to `AiProjectBuilder.mapComponent`, so catalog indices and project indices agree. |
| D3 | Property domains swept | Enum, `Boolean`/`boolean`, and integer properties sampled over a small range | Covers all 49 observed cases plus the four integer drivers the enum-only sweep misses. |
| D4 | Emission shapes | `fixed`, `variants`, `parametric` | Three genuinely different situations. Forcing all three into one table is what makes the file large. |
| D5 | Regular label runs | Emitted as a rule, not a table | `DIL_IC` is 24 pin counts times 5 numbering schemes. Its labels are a formula, so it collapses to one line instead of 120 variants. Irregular label sets, such as the 15 `TransistorPinout` values, stay a table. |
| D6 | Multiple drivers | Cartesian product, deduplicated by signature, capped at 64 combinations | Product order is small once D5 removes the regular cases. The cap is a guard, not an expected path. |
| D7 | Switch behaviour | A `switching` block per pinout variant, mirroring `AiSwitch` | Knowing a DPDT-on-on-on has six lugs is useless without knowing which lugs bridge in which position. The same probe produces it from `ISwitch.arePointsConnected`. |
| D8 | Unusable node names | Fixed at source in the eight offending components | Preferred over a heuristic in the generator. Changes netlist text output, so it needs a regression run. See §6. |
| D9 | Escape hatch | `@EditableProperty(pinoutDriver = ...)` with `AUTO`, `FORCE`, `EXCLUDE` | Derivation will be wrong somewhere. The annotation stays in the design as an override, not as the mechanism. Default `AUTO` means nothing has to be touched. |
| D10 | Preventing rot | A snapshot test in `diylc-library` asserting the derived driver set per type | This is the safety the annotation was really buying. Adding a component with an undeclared new driver fails the build until the snapshot is regenerated. |
| D11 | Where the analyzer lives | `diylc-core`, beside `ComponentCatalogGenerator` | It touches only `IDIYComponent`, `PropertyWrapper` and `ComponentProcessor`. No concrete component type is referenced, so the core boundary holds. |
| D12 | Boards | No pinout block at all | `AbstractBoard.isControlPointSticky` returns `false` for every index, so the sticky filter yields zero terminals and the entry is simply omitted. Components are placed on a board by coordinate, not by terminal. |

### Explicitly out of scope

- Relative pin geometry in the catalog. The model currently places a multi-pin
  component by a single anchor and then wires to terminals by reference, which
  `AiEditScriptEditor` resolves against the real instance. Pin offsets would
  help it reason about spacing, but that is a separate change.
- The datasheet axis. Applying a datasheet model to an electrolytic can flip
  `Polarized` and therefore the pinout, because `applyModel` calls
  `setPolarized(!d.getNonPolarized())`. The pinout block describes the
  `Polarized` property honestly; it does not enumerate which datasheet rows set
  it. Five component types are affected.
- Changing how the server prompts with this data.

## 4. The three shapes

A description of one set of terminals carries exactly one of three key groups, so each key has a
stable type wherever it appears:

| Keys | Meaning |
| --- | --- |
| `pins` | An explicit list of `{ i, label }`, used whenever the labels follow no rule or the indices are not contiguous. |
| `pinCount` plus `labels` | A count and a rule, where `labels` is `sequential` or `none`. |
| `pinCountExpression` plus `labels` plus `pinCountSamples` | A formula over numeric properties, with a few worked values as evidence. |

An `ISwitch` also carries `switching`, which always names the `positionCount` and lists `positions`
when there are few enough of them to be worth reading.

### Fixed

65 of 144 types. One description, no drivers, emitted at the root of `pinout`.

```json
"pinout": {
  "pins": [ { "i": 0, "label": "A" }, { "i": 1, "label": "K" } ]
}
```

### Variants

Finite drivers. One entry per combination.

This is what the mini toggle switch actually produces, abbreviated to two of its sixteen types:

```json
"pinout": {
  "drivers": ["Type"],
  "variants": [
    {
      "when": { "Type": "SPST" },
      "pinCount": 2,
      "labels": "sequential",
      "switching": {
        "positionCount": 2,
        "positions": [
          { "name": "ON1", "connected": [[0, 1]] },
          { "name": "ON2", "connected": [] }
        ]
      }
    },
    {
      "when": { "Type": "DPDT" },
      "pinCount": 6,
      "labels": "sequential",
      "switching": {
        "positionCount": 2,
        "positions": [
          { "name": "ON1", "connected": [[0, 1], [3, 4]] },
          { "name": "ON2", "connected": [[1, 2], [4, 5]] }
        ]
      }
    }
  ]
}
```

`when` carries enum constant names, matching the `possibleValues` already
emitted for the property and matching what `AiEditScriptEditor.parsePropertyValue`
accepts.

### Parametric

A numeric property drives the count. The generator samples it, fits a formula, and verifies the
fit against every sample before emitting it. A driver counts as numeric when it is an `int`
property, or an enum whose constants read as numbers either through `toString` or through a
`getValue` or `getCount` accessor. That last case is what collapses the dual inline package from
120 combinations to five.

```json
"pinout": {
  "drivers": ["Center Terminal", "Terminals"],
  "variants": [
    {
      "when": { "Center Terminal": "false" },
      "pinCountExpression": "2 * Terminals",
      "labels": "custom",
      "pinCountSamples": { "10": 20, "11": 22, "12": 24 },
      "example": { "when": { "Terminals": "10" }, "pins": [ { "i": 0, "label": "A1" } ] }
    }
  ]
}
```

One numeric driver is fitted to `a * n + b`, two to a product or a sum. Anything else, or a fit
that fails verification, falls back to enumerating the drivers instead. `labels` takes
`sequential` when the labels are `1..N`, `none` when every label is null, and `custom` otherwise,
and a custom rule brings along one worked `example` because there is no array to show.

### The record model

The analyzer builds records in `org.diylc.plugins.chatbot.model`, beside the ones that already
describe a project to the model, and Jackson serializes them. `AiPinout` holds the drivers and
either one `AiTerminals` or a list of `AiPinoutVariant`, each of which pairs a `when` map with its
own `AiTerminals`. `AiTerminals` carries the three key groups above, and its three static factories
are the only way to build one, so a description cannot end up claiming to be two shapes at once.
`AiPinLabels` replaces the label rule strings, and `AiPin`, `AiSwitching`, `AiSwitchingPosition` and
`AiPinoutExample` cover the rest.

`AiTerminals` is composed into `AiPinout` and `AiPinoutVariant` through `@JsonUnwrapped`, which
keeps the model free of duplicated components while leaving the emitted JSON flat.

## 5. Implementation

Four pieces, in order.

1. **`PinoutAnalyzer` in `org.diylc.plugins.chatbot.service`.** Entry points
   `analyze(ComponentType, ComponentProcessor)` and `analyze(Class, ComponentProcessor)`, both
   returning the record model described at the end of §4. Internally: read the baseline terminals; sweep candidate properties
   one at a time to find drivers; split them into numeric and finite; walk the product of the
   finite ones; fit a formula over the numeric ones, falling back to enumeration when nothing
   fits; attach switching tables for `ISwitch`. Every instantiation and every property write is
   wrapped, and a failure downgrades that property to "not a driver" rather than failing the
   entry. Drivers are sorted by name, because `extractProperties` follows `Class.getMethods()` and
   the JVM makes no promise about that order.

2. **`ComponentCatalogGenerator.buildFullEntry`** gains a call to the analyzer
   and puts the result under `pinout`. Two existing defects get fixed while the
   method is open: the index loop dereferences `comp` outside its own null
   check, and the `VERSION` constant reads `6.2.0` against a pom at `6.4.0`.

3. **`pinoutDriver` on `EditableProperty`** as a new element with default
   `AUTO`. Adding an element with a default is safe for a runtime-retained
   annotation and touches no existing component.

4. **The node name fixes** of §6, in `diylc-library`.

### Bounds

Three caps keep one component from swamping the file. At most 64 variants, dropping drivers
smallest domain first and naming the dropped ones in `driversHeldAtDefault`. At most four rows of
formula evidence, since the full domain of a numeric enum is already published as the property's
possible values. At most 24 switch positions written out in full, which matters because a DIP
switch reports two to the power of its switch count: without the cap its entry alone came to
1.27 MB, against 98 KB for all 144 types together afterwards.

## 6. Node names that need fixing

`getControlPointNodeName` serves the netlist, and `Node.toString()` already
renders `component.getName() + "." + displayName`. Eight components prepend the
component name a second time or return a hard-coded class name, so a netlist
node reads `V1.V1.+` today.

| Component | Returns now | Should return |
| --- | --- | --- |
| `BatterySymbol` | `getName() + "." + "+"` | `+` / `-` |
| `BulbSchematicSymbol` | same | `+` / `-` |
| `BuzzerSymbol` | same | `+` / `-` |
| `CurrentSourceSymbol` | same | `+` / `-` |
| `VoltageSourceSymbol` | same | `+` / `-` |
| `PhonoJackSymbol` | `getName() + "." + index` | `Tip` / `Sleeve`, and `Ring` when the type is stereo |
| `TerminalStrip` | `"TerminalStrip" + index` | `A1`…`An` for the front row, `B1`…`Bn` for the row it is bonded to, `C1`…`Cn` for the center row |
| `TagStrip` | `"TerminalStrip" + index` | nothing, the override is removed so the inherited `1`…`N` applies |

`PhonoJackSymbol` follows `ClosedJack1_4` and `OpenJack1_4`, which already name their points this
way. `TagStrip` had a verbatim copy of `TerminalStrip`'s string despite having a single row, so
deleting its override is both the smallest fix and the right one.

This is a real defect independent of the catalog, but it changes netlist text
output for any project using those parts. It needs a regression suite run and
regeneration of the affected reference netlists, which is why it is listed last
in §5 and can ship separately.

`PCBTerminalBlock` is deliberately excluded. It returns null until the user
supplies node names in a free-text property, and null correctly means "not a
named node". The catalog emits `"labels": "none"` for it.

## 7. Testing

- **Unit, `diylc-core`.** `PinoutAnalyzerTest` runs the analyzer against thirteen hand-built stub
  components: no drivers, a label anchor that must not count as a terminal, no sticky points at
  all, indices that do not start at zero, unnamed terminals, an enum driver, a boolean driver, two
  drivers combined, an integer driver, both annotation overrides, a setter that rejects one of its
  own values, and a switch.
- **Snapshot, `diylc-library`.** `PinoutDriverSnapshotTests` runs the analyzer over every
  `ComponentType` and compares the derived driver names against
  `src/test/resources/pinout-drivers.txt`, one line per type. Per D10 this is what catches a new
  component with an undeclared pinout driver. It also pins the 51 known varying types so a
  refactor cannot quietly drop one. On failure it writes the regenerated file to `target` and
  names it, so accepting a real change is a copy.
- **Regression suite.** Required for §6 only. The analyzer itself never runs at
  drawing time and cannot affect rendering or netlist output.

## 8. Running the generator

`ComponentProcessor` finds components by scanning the runtime classpath for
`org.diylc.components`, and those classes ship in `diylc-library`. The generator's own class sits
in `diylc-core`, which does not depend on `diylc-library`, so launching it against the
`diylc-core` module scans an empty package set and writes a catalog with no components in it. That
is a trap worth knowing about, because nothing about the failure looked like a failure.

Three things now stand in the way of hitting it again. The generator refuses to write an empty
catalog and says what to fix. `.run/ComponentCatalogGenerator.run.xml` launches it against the
`diylc-library` module, writing into the `diylc` directory where the committed catalogs live.
`ComponentCatalogGeneratorTest` moved from `diylc-core` to `diylc-library`, where the classpath
holds real components, and it now asserts that the catalog comes out populated and that a couple
of known entries carry the pinout they should.

The generator also sorts editable properties the way the property editor does, using
`ComparatorFactory.getDefaultPropertyComparator`. Property extraction follows
`Class.getMethods()`, whose order the JVM does not promise, so without that every regeneration
produced a large meaningless diff in a file that is committed. Two consecutive runs are now
byte-identical.
