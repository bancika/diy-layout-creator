# Audio Coverage Roadmap

Closing the gap between the component library and what the guitar, amplifier, pedal and hi-fi
communities actually build.

Status: proposed
Scope: `diylc-library` (`components.passive`, `.tube`, `.electromechanical`, `.guitar`,
`.connectivity`), one bug fix in `diylc-core/serialization`
Companion: the sibling roadmaps in this directory, which apply the same method to other packages

## 1. Goal

The audio side of the library has never had a systematic coverage pass, and it is the side the
sample corpus is made of. The headline number: **three components in the `Tubes` category**,
against 50 corpus projects that place a tube socket.

This document inventories what ships, measures what people hand-draw because it does not exist,
and sorts the additions into three tiers by evidence of demand rather than by appetite.

## 2. Decisions that govern this roadmap

| # | Decision | Choice | Rationale |
|---|---|---|---|
| D1 | What counts as a gap | **A part people are drawing by hand today.** Measured from label text in `diylc-regression-data/input`, not from catalogue browsing. | Keeps the roadmap honest. Several things that looked like gaps from the palette listing (pedal enclosures, EMG pickups, Filtertrons, Tele single coils, the switch-type matrix) turned out to ship already; several things that look exotic turn out to be in 25 projects. |
| D2 | Evidence vs. judgement | **Tiers 1 and 2 are measured. Tier 3 is reasoned and labelled as such.** | The corpus is overwhelmingly pedal and amp. It cannot speak to hi-fi demand at all, and saying so is better than inventing a number. See §4.3. |
| D3 | New class vs. new enum constant | **Same footprint family → enum constant; different structure → new class.** | Already the established paradigm, and `diylc/CLAUDE.md` states the preference directly: a `Version` property on one class beats a class per revision. |
| D4 | Where iron lives | **One `Transformer` class with a `Type` property (power / output / choke), in `components.passive`.** Not three classes. | A bell-end power transformer, an output transformer and a choke share an outline, a mounting footprint and a tap-strip; they differ in tap count and label. `AudioTransformer` already sits in `passive` and already extends `AbstractMultiPartComponent`, so the neighbour is right there. |
| D5 | Backward compatibility | Append-only enums, unchanged defaults, unchanged control-point counts for existing variants. | `.diy` files are a public contract. Only §6.1 touches serialization, and it *restores* compatibility rather than risking it. |
| D6 | Pre-set data before components | **Where a variant or a datasheet row closes a gap, it ships before any new class.** | Both are configuration rather than Java, and both are already covered by tests. §4.2 shows which of the most-placed parts have neither. |
| D7 | Composable parts are not components | **If a part can be assembled from existing components and saved as a building block, it ships as a factory building block, not a new class.** | A new class has to be drawn, transformed, tested, BOM-policed and supported forever, and it crowds the toolbox. A building block is data. The test is whether the assembly is electrically and visually honest without a new primitive: a push-pull pot is a pot plus a DPDT switch and passes, a chassis transformer is not a rectangle plus lugs and fails. See §5.4. |

### Explicitly out of scope

- The dormant classes outside the packages in scope above. They were parked deliberately
  ("comment out future stuff", `27eb2599`) and belong to the roadmaps for their own packages.
- Any electrical simulation: load-line *plotting* is in scope only because the component already
  exists (§6.2), impedance and frequency-response maths are not.
- Speaker cabinet and enclosure mechanical drawing beyond what `ChassisPanel` already does.
- Bumping the project version, packaging, and the deployment build.

## 3. Current coverage

**169 components register at runtime.** 208 classes carry a `@ComponentDescriptor`, but 39 have it
commented out and are invisible to the annotation scan.

| Category | Active | | Category | Active |
|---|---|---|---|---|
| Schematic Symbols | 30 | | Semiconductors | 12 |
| Electro-Mechanical | 28 | | Guitar | 12 |
| Passive | 17 | | Boards | 12 |
| Connectivity | 16 | | Misc | 10 |
| Displays & Outputs | 13 | | Controllers | 10 |
| Shapes | 4 | | **Tubes** | **3** |
| SMD | 2 | | | |

What is genuinely strong and should not be touched: the switch matrix (`ToggleSwitchType` runs SPST
through 5PDT with both on/on/on variants; sealed and open rotaries; Lever, LP, Freeway 3x3,
Schaller Megaswitch in six types, S1), the pickup models (`SingleCoilPickup` does Strat and Tele
bodies with rod/rail/dual-rail poles, `HumbuckerPickup` does PAF/Mini/Filtertron with an EMG-style
variant), and resistor coverage (22 variants from 1/4 W carbon comp to 100 W ceramic).

### 3.1 Dormant classes relevant to audio

Five of the 39 fall inside this roadmap's scope. Two are finished code behind a commented
annotation; three are not.

| Class | State | To revive |
|---|---|---|
| `passive/AirCoreInductor` | Body live, 4 commented descriptor lines | Uncomment + restore 4 imports |
| `passive/BoxTrimmer` | Body live, 9 commented lines, `BoxTrimmerTransformer` exists | Uncomment + restore imports |
| `misc/Loadline` | Body live, 4 commented lines, extends `AbstractBoard` | Uncomment + restore imports |
| `passive/AxialAirCoreInductor` | **Stub** — 69 of 70 lines commented, and it extended `AbstractFilmCapacitor` | Rewrite, or delete the file |
| `smd/SOT23Package` | **Stub** — 427 of 449 lines commented | Out of scope here |

## 4. Evidence

### 4.1 Method

`diylc-regression-data/input` holds 709 `.diy` projects (cloud 379, v1 284, user-files 34,
diy-fever 12). The 284 v1-format files carry no class names, so component tallies reflect the 425
modern files; label-text searches cover all 709.

**Searching raw file text does not work.** `Image` components store base64, and three-letter
queries hit it: `XLR` returned 17 "files" of which zero were real. Every figure below comes from
extracting `<text>`, `<title>`, `<name>`, `<value>` and `<description>` element contents first
(118,619 strings) and searching those.

### 4.2 Two coverage mechanisms, and which parts have neither

Pre-set data reaches a component two ways, and they are easy to confuse.

**Variants** (`variants.xml`, 176 of them) are saved property sets, authored per class, listed under
*Apply Variant*.

**Datasheets** are parametrized part catalogues: a `<ClassName>.datasheet` CSV on the classpath, one
row per real orderable part, listed under *Apply Model*. Five capacitor classes have one, and
between them they carry **3,539 rows covering 34 models** — each row a part number, a voltage, a
capacitance, exact body dimensions in mm, and body/border/label/marker colours:

| Class | Rows | Models |
|---|---|---|
| `RadialFilmCapacitor` | 1,523 | 14 (WIMA MKS-4/MKP-4/FKP/FKS, Orange Drop 225P/715P/716P, Epcos MKT) |
| `RadialElectrolytic` | 833 | 7 (Panasonic FC/FM/NHG, Nichicon FW/FG/KZ, Elna Silmic II) |
| `AxialElectrolyticCapacitor` | 455 | 4 (F&T Typ A, Sprague TVA Atom, Illinois TTA, JJ ANH) |
| `RadialMicaCapacitor` | 433 | 7 (Cornell Dubilier CD10 through CD42, CDV19/CDV30) |
| `AxialFilmCapacitor` | 293 | 2 (Solen Fast, Mallory 150) |

**Capacitors are therefore already covered, at a fidelity no variant could reach** — exact
dimensions per value and voltage, not a size estimate. Any roadmap item proposing to "add
Mallory 150 / Orange Drop / WIMA variants" is proposing something that shipped long ago; an
earlier draft of this document made exactly that mistake by counting only `variants.xml`.

The gap is the classes with *neither* mechanism:

| Component | Placements | Variants | Datasheet |
|---|---|---|---|
| `Resistor` | 2,388 | 22 | — |
| `RadialFilmCapacitor` | 754 | 0 | 1,523 rows |
| `RadialElectrolytic` | 602 | 0 | 833 rows |
| `TransistorTO92` | 363 | 0 | **none** |
| `DiodePlastic` | 324 | 0 | **none** |
| `RadialCeramicDiskCapacitor` | 283 | 0 | **none** |
| `PotentiometerPanel` | 195 | 2 | **none** |

`RadialCeramicDiskCapacitor` is the odd one out among the capacitors — the only one of the six with
no datasheet and no variants, despite 283 placements. `TantalumCapacitor` and `MultiSectionCapacitor`
are likewise uncovered.

The opportunity is that **the datasheet mechanism is generic and nothing outside capacitors uses
it.** `IDatasheetSupport` is a one-method interface in `diylc-core` implemented by exactly those five
classes; see §5.3 for what extending it costs.

### 4.3 Hand-written labels for parts that do not exist

Out of 709 projects:

| Part | Files | Mentions | Status in library |
|---|---|---|---|
| Output transformer (`OT`, "output transformer") | 27 / 10 | 84 / 10 | **absent** |
| Power transformer (`PT`, "power transformer") | 15 / 12 | 37 / 25 | **absent** |
| Speaker | 25 | 45 | **absent**, not even a symbol |
| Choke | 21 | 38 | **absent** |
| 3PDT / stomp / footswitch | 12 / 6 / 5 | 12 / 8 / 8 | electrically yes, body no |
| Push-pull pot | 12 | 15 | **absent** |
| Standby | 10 | 31 | covered by toggle |
| LDR | 10 | 15 | **absent** as a discrete part |
| Reverb tank | 6 | — | **absent** |
| 1590-series enclosure | 5 | 5 | covered — `ChassisPanel` variants |

Corroborating signals: 15 files assemble a transformer by hand from `TransformerCoil` pieces, and
88 fall back to `shapes.Rectangle`.

**Zero hits**, and therefore Tier 3 at best: `xlr`, `binding post`, `piezo`, `jazzmaster`,
`lipstick`, `varitone`, `concentric`, `vactrol`. The corpus is pedals and amps; its silence on
hi-fi connectors is a property of the sample, not evidence of no demand.

### 4.4 Why `EyeletBoard` is used 8 times and discrete turrets 40

`EyeletBoard.draw()` paints a full grid of eyelets with `fillOval`. They are decorative: no control
points, no node names, no conductive areas, nothing in the netlist. So builders ignore it and place
individual `Turret` components, which do carry a node name, on a `BlankBoard`.

This kills the obvious-looking fix. A `Type = Turret` flag on `EyeletBoard` would inherit the same
flaw, and a full grid is wrong for turret boards anyway — they are drilled per design. The real fix
is selectable, connected positions on both boards, which is a `composite-building-blocks.md`-scale
design question. Tier 3, §8.6.

## 5. Shared mechanics

### 5.1 Component discovery

Classpath annotation scan in `ComponentProcessor.getComponentTypes()`. **Any class carrying
`@ComponentDescriptor` is picked up automatically** — no manifest, no registry. Uncommenting an
annotation is the entire registration step.

### 5.2 Serialization safety

Per `diylc/CLAUDE.md`: never rename or delete a serialized field or change its type; fields added
to an existing class deserialize as `null` in old files, so default in the getter, not the
constructor; `serialVersionUID` stays `1L`.

Worth knowing before touching `ProjectFileManager`: its anonymous `MapperWrapper` overrides
`shouldSerializeMember` and drops elements that map to no field, so **unknown fields in old files
are already tolerated**. It does *not* override `realClass`, so an **unknown class throws**. That
asymmetry is the whole of §6.1.

### 5.3 Variant and datasheet mechanics

**Variants** live in `diylc-core/src/main/resources/import-defaults/variants.xml`, an
XStream-serialized `Map<String, List<Template>>` keyed by fully-qualified class name.
`FactoryVariantsTest` (`diylc-library/src/test/java/org/diylc/components/`) loads it, resolves every
key, and asserts every template names a live property and applies cleanly — so a stale variant fails
the build rather than failing silently at runtime.

**Datasheets** are cheaper than they look. `DatasheetService.loadDatasheet(Class)` resolves
`/datasheets/<SimpleClassName>.datasheet` off the classpath and splits each line on `\s*,\s*`.
Adding one to a component is therefore two steps and no registration:

1. implement `IDatasheetSupport.applyModel(String[] model)` on the component, mapping columns onto
   setters — `RadialFilmCapacitor.applyModel` is 20 lines and the model to copy;
2. drop `<ClassName>.datasheet` into `diylc-library/src/main/resources/datasheets/`.

`ComponentPopupMenu.updateApplyModelMenu` then builds the *Apply Model* submenu from
`ComponentType.getDatasheet()` automatically, and `Presenter.applyModelToSelection` routes the
chosen row back to `applyModel`.

Two cautions, both learned from §6.5:

- **The column format is positional and unforgiving.** `CapacitorDatasheetService` reads
  `model[1].split(" ")[0..1]` for voltage and `model[2].split(" ")[0..1]` for value, so a cell
  must be `"<number> <unit>"` with exactly one space. `4.7uF` throws
  `ArrayIndexOutOfBoundsException`; there is no validation anywhere.
- **`DatasheetService` reads with `new InputStreamReader(in)` and no charset**, so files are decoded
  with the platform default. Keep datasheet files ASCII — write `uF`, never `µF`. Per JEP 400 the
  default is UTF-8 on the supported JVM, so a latin-1 byte becomes U+FFFD rather than failing loudly.

There is also an unused path worth knowing about: `DatasheetService.lookup` /
`CapacitorDatasheetService.lookup` match a datasheet row by approximate value within a tolerance,
for auto-sizing a component from its value. **It has no production caller** — only
`CapacitorDimensionServiceTests` — so it is latent infrastructure, not live behaviour.

### 5.4 Factory building block mechanics

Building blocks are named groups of components, saved from a selection and re-placed as a unit.
`BuildingBlockManager.importDefaultBlocks()` ships a default set in
`diylc-core/src/main/resources/import-defaults/blocks.xml`, stored as a
`Map<String, List<IDIYComponent<?>>>` using the `diylc` package alias
(`aliasPackage("diylc", "org.diylc.components")`), and merges it into the user's own blocks by name.

Three ship today: `TO-92 w/ 3 Leads`, `TO-92 w/ 2 Leads`, `Title Block`.

Authoring one is a UI job, not a code job: place the components, select them, *Save as Building
Block*, then lift the entry out of the user config into `blocks.xml`. Per D7 this is the default
route for anything composable.

**The blocker (fixed in §6.6).** `importDefaultBlocks()` is wrapped in a one-shot flag:

```java
if (!configManager.readBoolean(IPlugInPort.DEFAULT_BLOCKS_IMPORTED_KEY, false)) {
```

Once `defaultBlocksImported` is true it never runs again, so **a factory block added to `blocks.xml`
reaches new installs only** — never anyone who has already launched the app. Any plan that
distributes composites this way needs §6.6 first.

`VariantManager.importDefaultVariants()` does not have this problem, and the asymmetry is worth
knowing: it keeps a `defaultTemplatesImportDate` watermark, re-reads `variants.xml` on every start,
and imports any variant whose `Template.createdOn` is newer than the watermark and whose name the
user does not already have. New factory *variants* do reach existing users today.

### 5.5 Tests

JUnit 4 under `diylc-library/src/test/java/org/diylc/`, with `components/passive/`,
`components/misc/` and `netlist/` already populated. Switching and netlist behaviour is unit-tested
(`AbstractSwitchTests` and its fifteen subclasses); component rendering is not — that is the
regression suite's job. Any new class with switching belongs in `netlist/`.

### 5.6 Build and verify

```bash
cd diylc
mvn -pl diylc-library -am test
```

Baseline at the time of writing, on `controllers` at `2a08fbe5`: `diylc-core` 148 tests,
`diylc-library` 331 tests, BUILD SUCCESS, project version 6.6.0.

Everything in Tiers 1 and 2 affects rendering or serialization, so each item also wants a
regression run (`.claude/skills/regression-test/SKILL.md`) before it is called done. §6.1
specifically cannot be verified any other way.

### 5.7 Changelog

User-visible additions get a `<Change>` entry in `diylc-core/src/main/resources/update.xml`, in the
block for the unreleased version. `NEW_FEATURE` for components and variants, `BUG_FIX` for §6.1.

---

## 6. Tier 1 — no new components

Enum constants, catalogue data, building blocks and uncommenting. Cheapest fidelity per hour in
the roadmap, and where D6 and D7 send most of the work.

§6.1, §6.5 and §6.6 are correctness fixes and should go first regardless of the rest; §6.6 also
gates §6.7, which is the channel the composable parts travel through.

### 6.1 Restore the `ElectrolyticCanCapacitor` alias — **bug, not a gap**

`diylc-regression-data/input/cloud/diy/282.diy` contains three
`<org.diylc.components.passive.ElectrolyticCanCapacitor>` elements. That class exists nowhere in
the tree, in any module, or in the vendored jars, and `ProjectFileManager.configure()` registers no
alias for it. Per §5.2 the file cannot deserialize. It came from the cloud corpus, so real user
files are affected.

This is the exact failure `diylc/CLAUDE.md` forbids: a shipped component class was renamed or
removed without an XStream alias.

The old element carries `name`, `alpha`, `value` (four nulls), `orientation`, `controlPoints`
(three), `bodyColor`, `baseColor`, `borderColor`, `labelColor`, `display`, `sections` (`_1`) and
`diameter` (1.0 in). `MultiSectionCapacitor` is the successor and matches all of it except
`sections`, which it does not have — it derives section count from `value.length`.

1. In `ProjectFileManager.configure()`, beside the existing aliases:
   ```java
   xStream.alias("org.diylc.components.passive.ElectrolyticCanCapacitor",
       MultiSectionCapacitor.class);
   ```
2. Confirm `ClassAliasingMapper` wins over class lookup for a dotted element name. If it does not,
   fall back to restoring a `@Deprecated` `ElectrolyticCanCapacitor` that extends
   `MultiSectionCapacitor` and carries no descriptor — invisible in the palette, resolvable by
   XStream.
3. `sections` is dropped by the §5.2 mapper, so the file loads but renders four sections where it
   said one. Decide whether that drift is acceptable or whether it needs a converter that maps
   `sections` onto `value` length. **Recommend accepting it** — the old field disagreed with its own
   `value` array, so there is no correct answer to preserve.
4. Verify by loading `282.diy`. A regression run is the only real check.

**~2 h.** Gate the rest of Tier 1 behind this.

### 6.2 Revive the three live dormant classes

Per §3.1, `AirCoreInductor`, `BoxTrimmer` and `Loadline` are finished code behind a commented
annotation. `AirCoreInductor` needs `ComponentDescriptor`, `CreationMethod`, `IDIYComponent` and
`SimpleComponentTransformer` (the last lives in `diylc-core`) re-imported; the other two need their
own commented imports restored the same way.

Each one: uncomment, restore imports, compile, check it appears in the palette and draws. `Loadline`
is the most interesting of the three — tube and transistor load-line plotting is a design feature
no other item here provides, and it is already written.

Then decide `AxialAirCoreInductor`'s fate. It is a 70-line stub that extended `AbstractFilmCapacitor`,
which is wrong for an inductor. **Recommend deleting the file** and treating axial air-core as a
variant of a revived `AirCoreInductor` if it is wanted at all; a commented-out stub inheriting from
the wrong base is worse than nothing.

**~3 h for three, plus a decision on the fourth.**

### 6.3 Tube socket bases

`TubeSocket.Base` covers B9A, OCTAL, B7G and B12C. `Jumbo4PinTubeSocket` is a separate class scoped
by its own description to 211/805/845. Missing bases the amp community uses constantly:

| Base | Pins | Tubes |
|---|---|---|
| UX4 | 4 | 300B, 2A3, 45, 5U4G |
| UX5 | 5 | type 80, 807 |
| B8G Loctal | 8 | 7N7, 7F7 |
| B9D Magnoval | 9 | EL509, PL519 |

UX4 is the valuable one and the jumbo socket does not substitute — wrong diameter, wrong spacing.

The class is clean to extend: `updateControlPoints()` switches on `base` for pin count, spacing and
a `hasEmptySpace` flag, `getBody()` switches for body diameter, and `draw()` carries per-base
keyway branches. **Both switches end in `default: throw new RuntimeException`, so a constant added
without touching both crashes at runtime rather than failing to compile.** Add the constant, the
`Size` constants, and a case in each switch.

One geometry wrinkle: UX4 and UX5 have two large filament pins and two or three small ones, so pin
diameter becomes per-pin where today it is per-socket. Scope that before committing to UX4.

Dimensions must come from datasheets, not from this table. **~6-8 h for all four, UX4 first.**

### 6.4 Datasheets for the uncovered high-usage parts

Per §4.2 and D6, and **not** the capacitor work an earlier draft of this document proposed — that
ships already. The parts with neither mechanism, in placement order:

| Class | Placements | What it needs |
|---|---|---|
| `TransistorTO92` | 363 | BC547/BC557, 2N3904/2N3906, J201, MPF102, BC108, with pinout per part |
| `DiodePlastic` | 324 | 1N4148, 1N400x, 1N5817 |
| `RadialCeramicDiskCapacitor` | 283 | NPO/X7R by value and voltage, with disc diameter |
| `PotentiometerPanel` | 195 | Alpha/Bourns 16 mm and 24 mm bodies by resistance and taper |

`RadialCeramicDiskCapacitor` is the natural first one: it is a capacitor, so
`CapacitorDatasheetService` already knows its column layout, and it is the only capacitor class of
the six most-placed with no coverage at all.

`TransistorTO92` is the most valuable and the most work, because a transistor datasheet needs a
pinout column that the capacitor format has no equivalent for — the existing E-C-B / E-B-C options
would have to become a datasheet column. Scope that before starting.

Per §5.3 each one is an `applyModel` implementation plus a CSV. Keep every cell ASCII and every
value `"<number> <unit>"`.

**~6-10 h each, `RadialCeramicDiskCapacitor` first.**

### 6.5 Fix six malformed datasheet rows — **bug**

Six of the 3,539 rows have a value cell the parser cannot read. Found by validating every row
against `parseCapacitorDatasheet`'s own logic:

| File | Rows | Cell | Failure |
|---|---|---|---|
| `RadialElectrolytic.datasheet` | 4 (Elna Silmic II 4.7 uF at 25/35/50/63 V) | `4.7uF` | no space, so `split(" ")` yields one element and `capacitanceParts[1]` throws `ArrayIndexOutOfBoundsException` |
| `RadialFilmCapacitor.datasheet` | 2 (WIMA MKS-4 400 V) | `0.01µF A`, `0.01µF B` | latin-1 `0xb5` in a file read with the platform charset (§5.3), no space, plus an ` A`/` B` suffix no other row has; `parseCapacitance` throws `IllegalArgumentException` |

User-visible effect: picking any of those six models from *Apply Model* throws instead of applying.

Latent effect: the same cells break `DatasheetService.lookup`, whose `Double.parseDouble` runs over
every row in a `model|voltage` bucket. Three of the five affected buckets have the bad row first, so
the whole bucket would throw — **80 rows across 5 buckets, including all 41 WIMA MKS-4 400 V rows**.
That path has no production caller today (§5.3), so this is a trap set for whoever wires up
value-based auto-sizing, not a live fault.

1. Rewrite the four `4.7uF` cells as `4.7 uF`.
2. Replace the two `0.01µF A` / `0.01µF B` cells with ASCII. The ` A`/` B` suffix looks like an attempt
   to give one value two body sizes (10x3 at 7.5 mm pitch, 13x4 at 10 mm pitch) — a real need the
   positional format cannot express, since model and voltage are the whole lookup key. **Recommend
   splitting them into two models**, `WIMA MKS-4` and `WIMA MKS-4 (10mm)`, matching how
   `Epcos MKT (5mm)` / `(7.5mm)` / `(10mm)` and the WIMA 2.5 mm/5 mm entries already encode pitch in
   the model name.
3. Add this row validation as a test beside `FactoryVariantsTest` — same shape, same
   collect-all-problems-then-assert style — so the next malformed cell fails the build. **This is
   the part that matters**; the six fixes are typos, the missing validation is why they survived.

**~3 h including the test.**

### 6.6 Make factory building blocks reach existing users — **bug, and a prerequisite**

Per §5.4, `importDefaultBlocks()` runs once per installation and never again, so every factory
building block this roadmap proposes would land only on fresh installs. D7 routes most composable
parts through that mechanism, so this is a prerequisite for §6.7, §7.5 and §8.9, not a nicety.

Two ways to fix it, in `BuildingBlockManager`:

**Recommended — drop the guard.** Delete the `DEFAULT_BLOCKS_IMPORTED_KEY` check and let the
existing merge run on every start. The merge body already skips any name the user has
(`if (!blocksMap.containsKey(entry.getKey()))`), so user blocks and user edits survive untouched and
new factory names simply appear. Leave the flag being written so a downgrade still sees it.

One behavioural cost, which should be a deliberate choice rather than a surprise: a user who
*deletes* a factory block gets it back on the next launch. Mitigate by recording deleted factory
names in config if that turns out to annoy anyone — not before.

**Thorough — mirror the variant watermark.** Copy `VariantManager.importDefaultVariants()`: a
`defaultBlocksImportDate` watermark plus a per-block `createdOn`. The obstacle is that `blocks.xml`
is a `Map<String, List<IDIYComponent<?>>>` and `BuildingBlock` — which *does* have a name field but
is not used in the stored format at all — carries no timestamp. Adding one means changing the shape
of the `BLOCKS_KEY` config value, which also holds the user's own blocks. That is a config-format
migration for a gain the simpler option already delivers.

Take the recommended option. Extend `BuildingBlockManagerTest` (8 tests, `diylc-core`) with a case
asserting that a second `importDefaultBlocks()` on a config that already has the flag set still
picks up a newly added default name. **~3 h.**

### 6.7 Ship the first factory building blocks

Data, not code, and per D7 this is where composable parts go. Each is authored in the app and lifted
into `blocks.xml`. Gated on §6.6.

| Block | Built from | Why |
|---|---|---|
| Push-pull pot | `PotentiometerPanel` + `MiniToggleSwitch` (DPDT) | 12 files, 15 mentions — the highest-demand composable in the corpus |
| Transformer symbol | `TransformerCoil` x2 + `TransformerCore` | 15 files already hand-assemble exactly this; closes most of §8.1's transformer-symbol gap for free |
| Treble bleed | `Resistor` + `RadialFilmCapacitor` | Named network every guitar builder adds; trivial to author |
| Tube gain stage | `TubeSocket` + grid stopper + cathode R/C + plate R | 50 corpus files place a tube socket and then rebuild this by hand every time |
| Strat / Tele / LP harness | `guitar` pickups + pots + switch + jack | `guitar` components appear in only 8 of 425 files, which may be discoverability as much as demand |

Author the push-pull pot first: it is the one with measured demand, and it is the worked example that
proves D7 before the rest follow. **~2-3 h each once §6.6 lands.**

---

## 7. Tier 2 — new components, ranked by measured demand

Parts that cannot be composed from what already ships, so D7 does not apply to them. §7.6 is
the exception and is kept only to record why it was declined.

### 7.1 `Transformer` — the largest single gap

Per D4: one class in `components.passive` extending `AbstractMultiPartComponent`, as
`AudioTransformer` does, with a `Type` enum of Power / Output / Choke, a bell-end or open-frame
body, and a configurable tap count per winding.

What the absence costs today: no BOM line, no terminals, no netlist presence, and 15 projects
assembling one by hand out of `TransformerCoil` pieces. Every tube amp has two or three.

Needs: `startTrackingContinuityArea` around the tap strip, mirroring a neighbouring multi-part
component exactly; an `IComponentTransformer` for rotation; `drawIcon()` hand-drawn for the
toolbox; `bomPolicy = SHOW_ONLY_TYPE_NAME` with the type and tap count in the variant label.

A matching schematic symbol is §8.1. Ship the physical part first — the corpus demand is for
layouts. **~16-20 h.**

### 7.2 `Speaker`

Physical component plus a schematic symbol, 25 files and 45 hand-written mentions, and the only
item here that serves amp and hi-fi equally. Round frame with a configurable diameter, two
terminals, an impedance property. The symbol is a few hours; the physical part is a day.
**~10-12 h for both.**

### 7.3 Chassis `Choke`

21 files, 38 mentions. Once §7.1 exists this is largely its Choke type plus a single-winding body,
which is the main argument for building it as part of the `Transformer` class rather than beside it.
`RadialInductor` and `ToroidalInductor` are PCB parts and do not substitute. **~4 h if it rides on
§7.1, ~10 h standalone.**

### 7.4 `Footswitch`

`MiniToggleSwitch` already provides 3PDT electrically, but draws as a rounded-rectangle mini toggle.
A stomp switch is a round body with a 3x3 lug grid, and pedal layouts are built around that
geometry. ~20 files reference 3PDT, stomp or footswitch by hand.

New class in `electromechanical`, reusing `ToggleSwitchType` so the netlist behaviour and the
existing `MiniToggleSwitchTests` patterns carry over. Add a momentary SPST variant for
non-latching relay bypass. Needs a `netlist/` test per §5.5. **~10-12 h.**

### 7.5 `Photoresistor`, and `Vactrol` as a building block

10 files, 15 mentions, and nothing discrete in the library — the only LDR class, `LDRSensorModule`,
is a dormant breakout board rather than the bare part. Blocks tremolo, optical compressors and Uni-Vibe-style circuits.

Split by D7:

- **`Photoresistor`** is a new component. It is a two-lead primitive with its own body and no
  existing part to compose it from, so it has to be drawn. Extends `AbstractLeadedComponent`.
- **Vactrol** is then an `LED` plus a `Photoresistor`, which is a factory building block
  (§6.7), not a class. A real vactrol is exactly those two parts in opaque heatshrink, so the
  assembly is honest.

**~5 h for the component, then an hour to author the block.**

### 7.6 Push-pull pot — **declined as a component**

Kept here as a record of the decision, because it is the case D7 exists for.

12 files and 15 mentions make this the best-evidenced composable part in the corpus, and an earlier
draft of this roadmap proposed a `PushPullPotentiometer` class extending `AbstractPotentiometer`.
That is the wrong answer. A push-pull pot is a `PotentiometerPanel` and a DPDT `MiniToggleSwitch`,
both of which already draw correctly, already report the right terminals, and already have netlist
coverage — `MiniToggleSwitchTests` alone is 15 cases. A new class would re-derive all of it and add
a toolbox entry.

It ships as a factory building block instead: §6.7, first in the list.

The same reasoning retires the alternative that was also considered, a `switch` property on
`PotentiometerPanel`. Besides D7, it would change the control-point count of a class placed 195
times in the corpus, which §5.2 forbids for existing files.

---

## 8. Tier 3 — reasoned, not measured

Per D2 these have no corpus evidence. Ordered by how confident the judgement is, not by cost.

**8.1 Schematic symbol holes.** Thirty symbols with specific omissions: no speaker (§7.2), no
momentary/push-button switch (only `SwitchLatchingSymbol`), no single transformer symbol with taps
(only the `TransformerCoil` + `TransformerCore` primitives), no tetrode or beam-power tube and no
dual-triode envelope for a 12AX7, no LDR, opto-isolator, thermistor, varistor, relay or SCR/Triac,
and no crystal symbol although a physical `CrystalOscillator` ships. `GroundSymbol` offers only
Default and Triangle with no chassis-versus-earth distinction, which matters for amp safety
drawings. The `IC` symbol's 3- and 5-contact forms do serve as an op-amp triangle. Each is a few
hours.

Two of these are not component work at all. The **transformer symbol** is `TransformerCoil` twice
plus `TransformerCore`, so per D7 it is a factory building block (§6.7) and needs no class. The
**opto-isolator symbol** is likewise an `LEDSymbol` facing a photoresistor symbol, once the latter
exists. That leaves the **momentary switch** as the one genuinely missing primitive here, and the
first worth drawing.

**8.2 Hi-fi connectors.** XLR (3-pin, male and female, chassis and PCB), binding post, banana,
speakON, 5-pin DIN, BNC. Zero corpus hits, high confidence from domain knowledge, and the weakest
domain in the library — there is no hi-fi category at all.

**8.3 Speaker crossover parts.** Reviving `AirCoreInductor` in §6.2 is the prerequisite and the
cheapest hi-fi win available. Beyond it: a mains toroid (`ToroidalInductor` is an inductor, not a
transformer with taps) and non-polar film caps at crossover values.

**8.4 Modern power packages.** No TO-247, TO-264 or TO-3P, so a current power amp cannot be drawn;
vintage TO-3 ships. Plus a bolt-on heatsink — `HeatSinkResistor` is a resistor, not a heatsink.

**8.5 Pickups not covered.** Jazzmaster (a wide soapbar, geometrically distinct from the P-90),
Danelectro lipstick, Rickenbacker toaster, gold foil, piezo/under-saddle. All zero corpus hits,
which is why they sit below the Tier 2 guitar item despite looking more visible.

**8.6 Connected board positions.** The `EyeletBoard` problem from §4.4: selectable eyelet and
turret positions that carry node names and reach the netlist. Benefits both boards and would
retire the discrete-turret workaround in 40 projects. Design question, not a component.

**8.7 Mains safety parts.** NTC inrush thermistor, MOV, X and Y capacitors. One corpus file
mentions NTC.

**8.8 Reverb tank.** Accutronics-style, 6 files. Simple geometry, narrow audience.

**8.9 Dual-concentric pot.** Jazz Bass, '62 Strat, Rickenbacker. Zero corpus hits, and per D7 not a
component: two `PotentiometerPanel` instances sharing a position is a factory building block, so it
joins the §6.7 list rather than this one once someone asks for it.

---

## 9. Sequencing

| Step | Items | Rough effort |
|---|---|---|
| 1 | §6.5 six malformed datasheet rows, plus the validation test | 3 h |
| 2 | §6.1 alias fix, verified by regression run | 2 h |
| 3 | §6.6 factory building block import guard | 3 h |
| 4 | §6.7 push-pull pot block, as the D7 worked example | 3 h |
| 5 | §6.2 revive three, decide on the fourth | 3 h |
| 6 | §6.3 UX4, then UX5 / B8G / B9D | 6-8 h |
| 7 | §6.4 datasheets, `RadialCeramicDiskCapacitor` first | 6-10 h each |
| 8 | §6.7 remaining blocks: transformer symbol, treble bleed, tube gain stage, harnesses | 2-3 h each |
| 9 | §7.1 `Transformer`, with §7.3 Choke as its third type | 20-24 h |
| 10 | §7.2 Speaker, physical plus symbol | 10-12 h |
| 11 | §7.4 Footswitch | 10-12 h |
| 12 | §7.5 `Photoresistor`, then the Vactrol block | 6 h |

Steps 1-4 are about a day and a half: both known bugs cleared, the building block channel unblocked,
and the highest-demand composable shipped as data rather than as a class. Step 9 is the one that
matters most.

Three items that looked like components when this roadmap started are now data (§6.7, §7.6, §8.9),
and one is split into a primitive plus a block (§7.5). That is D7 working as intended; expect the
same question of anything added to Tier 2 later.

Figures are estimates for scoping, not commitments, and every dimension in this document is an
estimate until it is checked against a datasheet (§6.3, §6.4).
