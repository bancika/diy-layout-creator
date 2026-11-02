# Chassis and Enclosures

The Hammond die-cast and amplifier chassis lineup as a datasheet on a single `Enclosure`
component that draws either one panel or an exploded view of the whole box.

## 1. Current state

- `ChassisPanel` (`diylc-library`, `org.diylc.components.chassis`) is shown as **Enclosure** in the
  new **Hardware** category, with instance prefix `ENC`. The class name is unchanged, so files,
  favourites, recent components and variants, all keyed by class, are unaffected. The three
  cutouts, Drill Hole and Dial Scale moved to Hardware with it.
- It is a two-point `AbstractShape` on the `CHASSIS` layer. The control points are the top panel.
  With `Height` > 0 the sides are unfolded around it (front above, back below, left and right to
  the sides; never the bottom), per `Sides`: `All`, `Front and Back` or `None`, set `Spacing`
  (default 0, edge to edge) apart from the top.
- A new enclosure is an upright 125B with all sides, square corners and no spacing (`DEFAULT_*`
  constants; the size matches its datasheet row).
  Old files are unaffected: XStream runs no constructor or initializer, so their saved size,
  radius and empty value stay and their missing `height` keeps them a single panel. `Height` null
  (old files) or 0, or `Sides = None`, draws a single panel, as `ChassisPanel` always did.
- A datasheet of 132 Hammond and generic models (§4) sets `Value` (the model), the top panel size
  and `Height`; it leaves `Radius` alone. One component is one enclosure, so the BOM lists `Enclosure / 1590BB`.
- The four old factory variants (`125B`, `A`, `B`, `BB`) are withdrawn as `deletedOn` stubs.
- Composites take the lowest layer of their children (`ComponentProcessor.getZOrder`), so a
  building block made of chassis parts sits on the Chassis layer and hides and locks with it.

## 2. Design decisions

| # | Decision | Rationale |
|---|---|---|
| D1 | One component draws the whole enclosure; no per-side panels and no exploded building blocks | Every datasheet model is an exploded enclosure in one click, the BOM gets one line per box, stretching the top drags the sides along, rotation needs no special code, and the component sits on the Chassis layer natively. Building blocks would have needed import tracking, a flat toolbar of ~10 buttons, and a per-panel BOM workaround. |
| D2 | Row format `Group,Model,X,Y,Height`: the top panel's size as placed, then the wall height, every size with its unit (`119.5 mm`, `17 in`), parsed with `Size.parseSize` like `DIL_IC` | Pedal drawings are metric, Hammond chassis are nominal inches; writing each in its native unit keeps the file checkable against the drawings. |
| D3 | `Value` is backed by `AbstractShape.value` through `getModel()` with `@EditableProperty(name = "Value")`; `height` and `sides` are new fields | `value` is a `String` every shape already serializes and nothing read, and `AbstractShape.getValue()` is typed `Void`. New fields read `null` from old files: `getHeight()` null means no sides, `getSides()` defaults to `All`. |
| D4 | Die-cast boxes up to 125 mm long (1590, 1550, 125B) are listed upright, with Hammond's length along Y; longer boxes and all 1441/1444 chassis horizontal. A new enclosure is an upright 125B. `applyModel` sets `Value`, `Height` and the top panel size, keeping the first control point and the direction the panel is drawn in | Pedals use the small boxes upright, with the jacks and DC socket on the short ends, which the front and back panels then show; bigger boxes and amp chassis are used lying down. |
| D5 | The datasheet carries no corner radius; `Radius` defaults to 0 and only the top panel uses it, the sides are drawn square | Hammond does not publish the die-cast corner radius (§5), so rounded corners are set by hand when wanted. |
| D6 | Length/Width are Hammond's outside dimensions; Height is the drillable wall: box height without the lid for die-cast boxes, outside height for 1441/1444 | The lid of a 1590 is the pedal's bottom, which the exploded view leaves out. |
| D7 | Die-cast taper is ignored: the top uses the outer size at the lid opening and the sides are rectangles | Hammond does not publish the floor size; the walls slope at most 2 deg, so the floor is 0.7-1.9 mm smaller per side (§5). |
| D8 | A composite's z-order is the minimum of its children's, computed per instance | A composite is drawn as a unit, so it should sit under what is placed on it afterwards; a maker board built from a board, pads and silkscreen text belongs on the Board layer, not the Text layer. |
| D9 | Die-cast boxes come from the 1590 and 1550 series only, natural finish only, plus generic sizes such as 125B under `Others`. HWCHAS walnut chassis and rack cases are out | Painted and black versions share the natural part's dimensions, and the shaped 1590 STOMP boxes (TRPB, TRPC, STPC) are sold painted only. |

## 3. Implementation

### 3.1 Composite z-order (core) - done

- `ComponentProcessor.getZOrder(IDIYComponent<?>)`: for a `CompositeComponent`, the minimum over
  its children, recursing into nested composites; a childless composite and any other component
  use the type's z-order. Not cached: a type lookup is a map hit and composites are few.
- Every class-level layer read goes through it: `Presenter` insertion, hidden and locked layers,
  `selectAll(layer)`, send to back / bring to front; `GroupAndLockIndex.isLocked`;
  `ComparatorFactory.getComponentZOrderComparator`; the `[L#]` tag in `ExplorerPane`.
- Tests: `CompositeComponentTest` and
  `PresenterTests.testChassisCompositeIsPlacedUnderExistingComponents`.

### 3.2 Enclosure (library) - done

- `ChassisPanel` implements `IDatasheetSupport` (two creation steps, model column 1) with a
  `ChassisPanel(String[] model)` constructor; rows in `datasheets/ChassisPanel.datasheet`.
- `getSidePanels()` derives the side rectangles from the top panel's normalized corners and
  `Spacing` (a new field; `null` from files reads as 0); `draw()`
  fills them before `stopTracking()` so they take part in hit-testing, and outlines them with the
  top. `getValueForDisplay()` returns the model.
- Category `Hardware` for `ChassisPanel`, the three cutouts, `DrillHole` and `DialScale`. New
  language keys in every `lang/` file: `Hardware`, `Sides`, `All`, `Front and Back` (`Height`
  and `None` existed). `catalog_full.json` and `catalog_index.json` regenerated.
- Tests: `ChassisPanelDatasheetTest` (every row applies, unique model names, inch rows,
  radius left alone by a model, side layout for All / Front and Back / None, spacing, sides follow a stretched top, no height, old-file
  panel, BOM).

### 3.3 Verify

`mvn -q test`, then the regression suite: the z-order change alters hide/lock of existing
composites, and `ChassisPanel` gained serialized fields and a new category.

## 4. Datasheet rows

Sources: Hammond per-part drawings, `https://www.hammfg.com/files/parts/pdf/<PART>.pdf`; series
tables on hammfg.com (1590, 1590-STOMP, 1441, 1444). Die-cast sizes are drawing outside dimensions
at the lid opening; heights are the box without the 4 mm lid. Chassis sizes are Hammond's nominal
outside W x D x H. Rows of die-cast boxes up to 125 mm long have their two top sizes swapped so
they place upright (D4). There is no radius column (D5).

Die-cast boxes are grouped by series only (1590, 1550), in Hammond's catalogue order, and only
natural-finish (unpainted) part numbers are rows. Chassis are grouped by family, then height. The
1590 group has 37 rows; the toolbox already splits a datasheet submenu longer than
`MAX_MENU_ITEMS` (30) into ranged submenus, so it needs no further split.

The rows are in `diylc-library/src/main/resources/datasheets/ChassisPanel.datasheet`.


Group sizes: 37 (1590), 15 (1550), 1 (Others); 23 / 24 / 25 / 7 chassis (132 rows). `Others`
holds generic sizes sold by several makers under one name, currently only 125B (122 x 66 x 40 mm
with the backplate, per a maker's listing). The other 1590 rows were read from the per-part
drawings, except 1590H, U, V, CE, KK, K, E, F, R1, DE, EE, BX2 and BX, which come from Hammond's
series table (whole mm, lid included) like all 1550 rows but 1550B.

Finish and option suffixes are not rows: `BK3` (black 1441), `BK` (black 1550), colour codes on
1590 (`RD`, `CB`, ...), `W` prefix (watertight; IP66/IP67 gaskets on the 1550), `F`/`FL`
(flanged), `CWW` (walnut sides on sizes 12/16/22/24/32/33, which shorten the usable front, back and
top by about 22 mm).

## 5. Unsourced or conflicting figures

| Item | Status |
|---|---|
| Die-cast corner radius (top view) and top-to-side edge fillet | Not published, so not in the datasheet (D5). Measure from Hammond STEP models or a box if it is ever wanted. |
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
- Factory building blocks are still imported once, behind the boolean
  `DEFAULT_BLOCKS_IMPORTED_KEY`, so a block added to `blocks.xml` later never reaches existing
  users. Fix by giving factory blocks `createdOn`/`deletedOn` and an import date, as `VariantManager`
  does, when a new factory block is next needed.
- Configs older than class-keyed variants store keys like `Electro-Mechanical.Chassis Panel`;
  `VariantManager`'s upgrade maps them by the current category and name, so such variants would
  not carry over. Map the legacy name if it turns out to matter.
