# Chassis and Enclosures

Covering the Hammond pedal and amplifier chassis lineup with a `ChassisPanel` datasheet, and
shipping the common pedal enclosures as exploded five-panel factory building blocks.

## 1. Current state

- `ChassisPanel` (`diylc-library`, `org.diylc.components.chassis`) is a two-point
  `AbstractShape` on the `CHASSIS` layer with an `Edge Radius`. One instance is one side of a
  chassis; its `@AiNote` already fixes the exploded-view convention (top in the centre, front
  against its upper edge, back against its lower edge, left and right against the side edges).
- Four factory variants in `variants.xml` (`125B`, `A`, `B`, `BB`) store nothing but corner points
  and a 5 mm radius. Their sizes are not Hammond's (the `125B` variant is 122.3 x 67.1 mm, the
  1590N1 drawing says 121.2 x 65.5).
- Factory building blocks (`import-defaults/blocks.xml`, three entries) are imported once, behind
  the boolean `DEFAULT_BLOCKS_IMPORTED_KEY`; blocks added later never reach existing users.
- Composites take the lowest layer of their children (`ComponentProcessor.getZOrder`), so a
  composite of chassis panels sits on the Chassis layer, under parts placed before it, and
  follows that layer's hide/lock. Done; see §3.1.

## 2. Design decisions

| # | Decision | Rationale |
|---|---|---|
| D1 | Chassis sizes move to a datasheet, `datasheets/ChassisPanel.datasheet`, two creation steps (Group > Model) | Same mechanism as `DIL_IC` and `Resistor`; the AI can name a model; one row serves every side of the enclosure (D3). |
| D2 | Row format `Group,Model,Length,Width,Height,Radius`. Sizes in mm unless a unit follows (`8 in`), parsed like `Resistor.parseDatasheetSize` | Pedal drawings are metric, Hammond chassis are nominal inches; writing each in its native unit keeps the file checkable against the drawings. |
| D3 | `ChassisPanel` gets a `Value` property holding the model, and a new `Side` (`TOP`, `FRONT`, `BACK`, `LEFT`, `RIGHT`). `applyModel` sets `Value` to the model and sizes the panel from the row according to its side: top = Length x Width, front/back = Length x Height, left/right = Width x Height, keeping the first control point. Changing `Side` on a panel that has a model resizes it | One class, no per-side types. `Value` is backed by `AbstractShape.value`, a `String` that every shape already serializes and nothing reads (always `""`), so no new field is needed for it; since `AbstractShape.getValue()` is typed `Void`, the getter is `getModel()` with `@EditableProperty(name = "Value")`. `side` is new and reads `null` from old files: `getSide()` defaults to `TOP`. An empty value means "free-form panel" exactly as today. |
| D4 | Top panel is drawn landscape, Length along X | Matches the four existing variants and the amp chassis front-panel orientation; pedal builders rotate (the transformer already supports it, and a composite rotates as a unit). |
| D5 | Length/Width are Hammond's outside dimensions; Height is the drillable wall: box height without the lid for die-cast boxes, outside height for 1441/1444 | The lid of a 1590 is the pedal's bottom, which the exploded view leaves out. |
| D6 | Die-cast taper is ignored: the top panel uses the outer size at the lid opening and side panels are rectangles | Hammond does not publish the floor size; the walls slope at most 2 deg, so the floor is 0.7-1.9 mm smaller per side. Recorded in §5 as unsourced. |
| D7 | The four variants are withdrawn as `deletedOn` stubs | The mechanism from the Resistor and DIP migrations (`Template.deletedOn`, `DeletedVariantsTest`). |
| D8 | Factory blocks get variant-style import tracking: each factory block has `createdOn`/`deletedOn`, the import date is stored under a new key | Mirrors `VariantManager`; lets new factory blocks reach existing users. |
| D9 | Exploded factory blocks only for the common enclosures: pedals 1590A, 1590B, 125B, 1590BB, 1590XX, 1590DD; amps 1441-10, 1441-18, 1441-28, 1441-32 (§3.4) | The block toolbar is a flat list of buttons; the full lineup stays in the datasheet. |
| D10 | A composite's z-order is the **minimum** of its children's, computed per instance | A composite occupies one slot in the stack and is drawn as a unit, so it should sit under what is placed on it afterwards. For a chassis block min = max = `CHASSIS`. For a maker board built from a board, pads and silkscreen text, max would be `TEXT` and the board would cover every part placed later; min is `BOARD`, and locking the Board layer locks the whole board. |
| D11 | Die-cast boxes come from the 1590 and 1550 series only, natural finish only, plus generic sizes such as 125B under `Others`. HWCHAS walnut chassis and rack cases are out of this change | Painted and black versions share the natural part's dimensions, and the shaped 1590 STOMP boxes (TRPB, TRPC, STPC) are sold painted only. The others are listed in §6. |
| D12 | BOM: one line per enclosure, valued by model. `getValueForDisplay()` returns the model for a `TOP` panel and `null` for any other side; `bomPolicy` stays `SHOW_ONLY_TYPE_NAME` | `BomMaker` keys rows by type + value and skips a `null` value, so an exploded 1590BB reads `Chassis Panel / 1590BB / 1` instead of five panels. Free-form and old-file panels are `TOP` with an empty value and list as they do today. An exploded block placed as a composite lists under its block name, so the block's `CompositeComponent` carries the model as its value too (§3.4). |

## 3. Implementation

Step 4 depends on 2 and 3.

### 3.1 Composite z-order (core) - done

- `ComponentProcessor.getZOrder(IDIYComponent<?>)`: for a `CompositeComponent`, the minimum over
  its children, recursing into nested composites; a childless composite and any other component
  use the type's z-order. Not cached: a type lookup is a map hit and composites are few.
- Every class-level layer read goes through it: `Presenter` insertion (`addComponent`,
  `addComponents`), hidden and locked layers, `selectAll(layer)`, send to back / bring to front;
  `GroupAndLockIndex.isLocked`; `ComparatorFactory.getComponentZOrderComparator`; the `[L#]` tag
  in `ExplorerPane`, which now shows composites too.
- Paint order is the project list order, so existing files render the same; only hide/lock and
  future insertion change. `flexibleZOrder = true` stays.
- Tests: `CompositeComponentTest` (chassis-only, mixed, nested and childless composites; layer
  lock; comparator) and `PresenterTests.testChassisCompositeIsPlacedUnderExistingComponents`.

### 3.2 `ChassisPanel` datasheet (library)

- `enableDatasheet = true, datasheetCreationStepCount = 2, datasheetModelColumn = 1`,
  `implements IDatasheetSupport`, `ChassisPanel(String[] model)` constructor.
- `getModel()` / `setModel()` over the inherited `value` field as `Value`, and a new serialized
  `side` whose getter tolerates `null` (D3). Override `getValueForDisplay()` for the BOM (D12).
- `@AiNote`: a datasheet model is a Hammond part number such as 1590BB or 1444-32 and is stored as
  'Value'; it sets the panel size for the panel's `Side`, so set `Side` before or together with
  the model. Only the top panel carries the enclosure into the BOM. "125B" is a generic size
  sold by several makers (about 122 x 66 x 40 mm with the lid, often with thinner walls) and has
  its own row under `Others`; its Hammond equivalent is 1590N1.
- `ChassisPanel.datasheet` from §4. Rows are listed per group in the order given there.
- `variants.xml`: `125B`, `A`, `B`, `BB` become `deletedOn` stubs; extend `DeletedVariantsTest`.
- `ChassisPanelDatasheetTest`, modelled on `ResistorDatasheetTest`: every row parses; each side
  gets the expected size; changing `Side` resizes; a panel from an old file (empty value, null
  side) keeps its geometry; the BOM lists one line per top panel, valued by model, and none for
  side panels.
- Localization: `Side`, `Top`, `Front`, `Back`, `Left`, `Right` in
  every `lang/` file. Fix the description typo "withouth".
- Regenerate `catalog_full.json` as the Resistor change did.

### 3.3 Factory block import tracking (core)

- New `org.diylc.core.FactoryBlock` (`name`, `createdOn`, `deletedOn`, `components`), the shape of
  `Template`. `blocks.xml` becomes a list of these; the user's `BLOCKS_KEY` map is unchanged.
- `BuildingBlockManager.importDefaultBlocks` mirrors `VariantManager`: import a block when its
  `createdOn` is after the stored `DEFAULT_BLOCKS_IMPORT_DATE_KEY` and the user has no block of
  that name; remove it when `deletedOn` is after the import date.
- Migration: the three existing blocks keep `createdOn = null` and are imported only when the
  legacy `DEFAULT_BLOCKS_IMPORTED_KEY` is false (a fresh install), so users who deleted them do not
  get them back.
- Test alongside the variant import tests: fresh install, upgrade from the boolean flag, new block
  after an import, deleted block, user block with a clashing name.

### 3.4 Exploded blocks

- Ten blocks named `Hammond <model> (Exploded)`, or `<model> (Exploded)` for a generic size
  (`125B (Exploded)`): the six pedal enclosures from D9 and four amp chassis, one per size class.
  The amp picks are the steel 1441 sizes amp builders and kit
  suppliers stock (Tube Town, an amp-parts shop, carries 1441-10, -18, -28 and -32); steel over
  the 1444 because builders warn that thin chassis sag under heavy transformers.

| Class | Typical build | Block | W x D x H | Why this size |
|---|---|---|---|---|
| Small | Tweed Champ (5F1) | 1441-10 | 8 x 4 x 2 in | Closest Hammond to the original 5F1 chassis (8-3/4 x 4-1/8 x 2-5/8 in). |
| Medium | 18 W Marshall (TMB) | 1441-18 | 13.5 x 5 x 2 in | The Hammond size suggested for 18 W builds; dedicated 18 W chassis are similarly long and narrow (17 x 6-1/2 x 2-1/2 in), which fits the in-line tube row. |
| Larger | 50 W Plexi / JTM45 | 1441-28 | 16 x 8 x 3 in | 16 x 8 x 2-3 in is the size quoted for 50 W Marshall builds; 3 in clears the transformers. |
| Large | 100 W modern (SLO-style) | 1441-32 | 17 x 10 x 3 in | Largest common format; room for two channels, four power tubes and large iron. |

- No source names a Hammond for the SLO specifically (the real head is 25 in wide, wider than
  any Hammond chassis); 1441-32 is a size judgement.
- Each block is five `ChassisPanel`s with `Value` (model) and `Side` set; the block's composite
  value is the model as well, so the BOM line reads `Hammond 1590BB (Exploded) / 1590BB`. With the top at (0,0)-(L,W): front (0,-H)-(L,0), back (0,W)-(L,W+H), left (-H,0)-(0,W),
  right (L,0)-(L+H,W). Edge radius on the top panel only.
- Generate the XML once from the datasheet rather than by hand, and add a test that every panel of
  every factory chassis block matches its datasheet row and touches its neighbours edge to edge,
  so the two cannot drift.

### 3.5 Verify

`mvn -q test` (spans core and library), then offer the regression suite: §3.1 changes hide/lock
behaviour of existing composites and §3.2 touches serialization.

## 4. Datasheet rows

Sources: Hammond per-part drawings, `https://www.hammfg.com/files/parts/pdf/<PART>.pdf`; series
tables on hammfg.com (1590, 1590-STOMP, 1441, 1444). Die-cast sizes are drawing outside dimensions
at the lid opening; heights are the box without the 4 mm lid. Chassis sizes are Hammond's nominal
outside W x D x H. Radius 5 on die-cast boxes is carried over from the old variants, **unverified**
(§5); sheet-metal chassis are drawn square.

Die-cast boxes are grouped by series only (1590, 1550), in Hammond's catalogue order, and only
natural-finish (unpainted) part numbers are rows. Chassis are grouped by family, then height. The
1590 group has 37 rows; the toolbox already splits a datasheet submenu longer than
`MAX_MENU_ITEMS` (30) into ranged submenus, so it needs no further split.

```
Diecast (1590),1590LLB,50.5,50.5,21,5
Diecast (1590),1590LB,50.55,50.6,27,5
Diecast (1590),1590H,53,38,27,5
Diecast (1590),1590Y,92,92,38,5
Diecast (1590),1590A,92.6,38.5,27,5
Diecast (1590),1590G,100,50,21.5,5
Diecast (1590),1590G2,100,50,27,5
Diecast (1590),1590S,110.5,82.4,40,5
Diecast (1590),1590B,112.4,60.5,27,5
Diecast (1590),1590B2,112.4,60.5,33.5,5
Diecast (1590),1590BS,112,60.5,38,5
Diecast (1590),1590B3,116,77,33.5,5
Diecast (1590),1590BB,119.5,94,30,5
Diecast (1590),1590BB2,119.5,94,33.75,5
Diecast (1590),1590BBS,119.5,94,38.1,5
Diecast (1590),1590Q,120,120,30,5
Diecast (1590),1590U,120,120,55,5
Diecast (1590),1590V,120,120,90,5
Diecast (1590),1590C,119.5,94,52.5,5
Diecast (1590),1590T,120,80,55,5
Diecast (1590),1590CE,120,100,61,5
Diecast (1590),1590N1,121.2,65.5,35.8,5
Diecast (1590),1590KK,125,125,53,5
Diecast (1590),1590K,125,125,75,5
Diecast (1590),1590J,145,95,45.1,5
Diecast (1590),1590XX,145.2,121.2,35.2,5
Diecast (1590),1590X,145.2,121.2,52.3,5
Diecast (1590),1590P1,153,83.1,46.4,5
Diecast (1590),1590D,187.75,119.5,52,5
Diecast (1590),1590DD,188,120,33,5
Diecast (1590),1590E,188,120,78,5
Diecast (1590),1590F,188,188,63,5
Diecast (1590),1590R1,192,112,57,5
Diecast (1590),1590DE,200,120,60,5
Diecast (1590),1590EE,200,120,81,5
Diecast (1590),1590BX2,254,70,31,5
Diecast (1590),1590BX,254,70,46,5
Diecast (1550),1550Q,60,55,26,5
Diecast (1550),1550P,80,55,21,5
Diecast (1550),1550A,89,35,26,5
Diecast (1550),1550B,114.5,64,26,5
Diecast (1550),1550D,115,64,51,5
Diecast (1550),1550C,115,90,51,5
Diecast (1550),1550M,120,100,31,5
Diecast (1550),1550K,140,102,73,5
Diecast (1550),1550L,165,127,73,5
Diecast (1550),1550E,171,121,51,5
Diecast (1550),1550F,171,121,101,5
Diecast (1550),1550G,222,146,51,5
Diecast (1550),1550H,222,146,101,5
Diecast (1550),1550N,250,250,97,5
Diecast (1550),1550J,275,175,63,5
Others,125B,122,66,36,5
Chassis - Steel (1441),1441-9,8 in,4 in,1 in,0
Chassis - Steel (1441),1441-15,10 in,6 in,1 in,0
Chassis - Steel (1441),1441-6,4 in,4 in,2 in,0
Chassis - Steel (1441),1441-8,6 in,4 in,2 in,0
Chassis - Steel (1441),1441-12,7 in,5 in,2 in,0
Chassis - Steel (1441),1441-10,8 in,4 in,2 in,0
Chassis - Steel (1441),1441-14,9 in,5 in,2 in,0
Chassis - Steel (1441),1441-16,10 in,6 in,2 in,0
Chassis - Steel (1441),1441-22,12 in,8 in,2 in,0
Chassis - Steel (1441),1441-29,12 in,10 in,2 in,0
Chassis - Steel (1441),1441-18,13.5 in,5 in,2 in,0
Chassis - Steel (1441),1441-26,16 in,8 in,2 in,0
Chassis - Steel (1441),1441-30,17 in,10 in,2 in,0
Chassis - Steel (1441),1441-34,17 in,12 in,2 in,0
Chassis - Steel (1441),1441-24,12 in,8 in,3 in,0
Chassis - Steel (1441),1441-28,16 in,8 in,3 in,0
Chassis - Steel (1441),1441-20,17 in,4 in,3 in,0
Chassis - Steel (1441),1441-32,17 in,10 in,3 in,0
Chassis - Steel (1441),1441-36,17 in,12 in,3 in,0
Chassis - Steel (1441),1441-38,17 in,14 in,3 in,0
Chassis - Steel (1441),1441-33,17 in,10 in,4 in,0
Chassis - Steel (1441),1441-40,17 in,14 in,4 in,0
Chassis - Steel (1441),1441-42,17 in,14 in,5 in,0
Chassis - Aluminium (1444) 1-2 in,1444-45351,4.5 in,3.5 in,1 in,0
Chassis - Aluminium (1444) 1-2 in,1444-9,8 in,4 in,1 in,0
Chassis - Aluminium (1444) 1-2 in,1444-15,10 in,6 in,1 in,0
Chassis - Aluminium (1444) 1-2 in,1444-6415,6 in,4 in,1.5 in,0
Chassis - Aluminium (1444) 1-2 in,1444-84515,8 in,4.5 in,1.5 in,0
Chassis - Aluminium (1444) 1-2 in,1444-6,4 in,4 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-542,5 in,4 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-8,6 in,4 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-12,7 in,5 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-772,7 in,7 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-10,8 in,4 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-862,8 in,6 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-14,9 in,5 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-972,9 in,7 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-9552,9.5 in,5 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-16,10 in,6 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-1172,11 in,7 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-22,12 in,8 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-29,12 in,10 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-1372,13 in,7 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-18,13.5 in,5 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-26,16 in,8 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-30,17 in,10 in,2 in,0
Chassis - Aluminium (1444) 1-2 in,1444-17132,17 in,13 in,2 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-95525,9.5 in,5 in,2.5 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-10825,10 in,8 in,2.5 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-12825,12 in,8 in,2.5 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-643,6 in,4 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-753,7 in,5 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-9553,9.5 in,5 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-1053,10 in,5 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-1273,12 in,7 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-24,12 in,8 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-12103,12 in,10 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-12123,12 in,12 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-1463,14 in,6 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-14103,14 in,10 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-1573,15 in,7 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-1593,15 in,9 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-28,16 in,8 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-20,17 in,4 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-1763,17 in,6 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-1773,17 in,7 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-1783,17 in,8 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-32,17 in,10 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-17123,17 in,12 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-17133,17 in,13 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-17143,17 in,14 in,3 in,0
Chassis - Aluminium (1444) 2.5-3 in,1444-17173,17 in,17 in,3 in,0
Chassis - Aluminium (1444) 4-6 in,1444-33,17 in,10 in,4 in,0
Chassis - Aluminium (1444) 4-6 in,1444-17134,17 in,13 in,4 in,0
Chassis - Aluminium (1444) 4-6 in,1444-17154,17 in,15 in,4 in,0
Chassis - Aluminium (1444) 4-6 in,1444-17174,17 in,17 in,4 in,0
Chassis - Aluminium (1444) 4-6 in,1444-17105,17 in,10 in,5 in,0
Chassis - Aluminium (1444) 4-6 in,1444-17135,17 in,13 in,5 in,0
Chassis - Aluminium (1444) 4-6 in,1444-17156,17 in,15 in,6 in,0
```

Group sizes: 37 (1590), 15 (1550), 1 (Others); 23 / 24 / 25 / 7 chassis (132 rows). `Others`
holds generic sizes sold by several makers under one name, currently only 125B (122 x 66 x 40 mm
with the backplate, per a maker's listing). The 1590 rows already in the research were read from the per-part drawings; 1590H, U,
V, CE, KK, K, E, F, R1, DE, EE, BX2 and BX, and all 1550 rows except 1550B, come from Hammond's
series tables (whole mm, lid included).
Finish and option suffixes are not rows: `BK3` (black 1441), `BK` (black 1550), colour codes on
1590 (`RD`, `CB`, ...), `W` prefix (watertight; IP66/IP67 gaskets on the 1550), `F`/`FL`
(flanged), `CWW` (walnut sides on sizes 12/16/22/24/32/33, which shorten the usable front, back and
top by about 22 mm).

## 5. Unsourced or conflicting figures

| Item | Status |
|---|---|
| Die-cast corner radius (top view) and top-to-side edge fillet | Not published; 5 mm is the old variants' figure. Measure from Hammond STEP models or a box. |
| Die-cast floor size (pedal top face) | Not published; between the outer size and outer minus 2 x (box height x tan 2 deg). |
| 1590DD box height | Drawing gives 37.00 both overall and box; 33 assumes the usual 4 mm lid. |
| 1590D box height | ~52, same assumption. |
| Box heights of the table-only 1590 rows (H, U, V, CE, KK, K, E, F, R1, DE, EE, BX2, BX) | Table height minus the 4 mm lid; length and width are the table's rounded figures. Read the drawings to firm them up. |
| 125B box height | 40 mm with the backplate minus an assumed 4 mm backplate (the 1590 lid); makers do not state the plate thickness. |
| 1550 box heights (all but 1550B) | Series table height (lid included) minus 4 mm, the overall-to-box difference on the 1550B drawing (30.10 / 26.00). |
| 1590BS / 1590B2 height | Web table 38 for both; drawings 42.00 overall / 38.00 box and 37.50 overall. Drawings used. |
| 1444-10/-12/-14 height | Drawings say 49.78 mm; catalogue and inner height fit 50.80 (2 in). 2 in used. |
| 1441-42 sheet | Drawing 0.057", product page 18 ga. Irrelevant to the outline. |

## 6. Later

- HWCHAS walnut-frame chassis (HWCHAS1310 / 1710; aluminium top 298.45 x 222.25 / 400.05 x
  222.25 mm on a 19.05 mm walnut frame): a different construction from the folded sheet metal.
- Hammond rack cases (RM, RMCS) and the 1458 instrument enclosure.
