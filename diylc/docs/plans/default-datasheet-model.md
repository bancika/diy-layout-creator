# Default Datasheet Model

Letting a datasheet model be pinned as a component's default, the way a user-defined variant can.

## 1. Goal

A saved variant in a component's drop-down has two mini buttons: a pin that makes it the default,
so a plain click on the component in the toolbox or tree places that variant, and a bin that
deletes it. Datasheet models (the TO-1 transistors, resistor series, IC part numbers, ...) sit in
the same drop-down but have no pin, so a user who always places a 2N3904 has to walk the submenu
every time. Datasheet model leaves get the same pin, and only one default can exist per component
type, whether it is a variant or a model.

## 2. Current state

| Piece | Where | What it does |
| --- | --- | --- |
| Storage | `IVariantProcessor.DEFAULT_TEMPLATES_KEY` (`"defaultTemplates"`) | A `Map<String, String>` in the configuration: component class name (or the legacy `category.name` key) to the default variant's name. |
| Write | `Presenter.setDefaultVariant` | Toggles: clicking the current default removes it, anything else replaces it. The logic lives in `Presenter`, unlike the read side. |
| Read | `VariantManager.getDefaultVariant` | Looks up the class-name key, then the legacy key. |
| Apply | `Presenter.setNewComponentTypeSlot` | When called with no template, finds the variant named by the default and uses it. |
| UI | `ComponentButtonFactory.createVariantItem` | Adds the pin (`IconLoader.PinGreen` / `PinGrey`) and bin labels to a variant's `JMenuItem`; the pin hides the popup and calls `setDefaultVariant`. |
| Model items | `ComponentButtonFactory.createDatasheetItems` | Builds the datasheet submenus recursively; leaves are plain `JMenuItem`s that hand the row to a `Consumer<String[]>`. |

`createDatasheetItems` has three callers: the variants button in the component tree
(`TreePanel.addVariantItems`), the toolbox drop-down (`ComponentTabbedPane`), and the canvas
right-click "apply model" submenu (`ComponentPopupMenu`), which applies a model to the selection
rather than placing a new component.

Because the map holds one value per component type, "only one default" already holds as long as
models are written to the same map.

## 3. Design decisions

| # | Decision | Choice | Rationale |
| --- | --- | --- | --- |
| D1 | Storage | Same `defaultTemplates` map and key. A model default is stored as the string `datasheet:` followed by the row's menu path, the first `datasheetCreationStepCount` columns joined by `\|`, e.g. `datasheet:BJT (NPN)\|2N3904` | One entry per type means pinning a model replaces a pinned variant and the other way round, with no extra bookkeeping. The menu path is exactly what identifies a leaf in `createDatasheetItems` (it already resolves a click by matching the path and taking the first row). No datasheet contains `\|` today. |
| D2 | Compatibility | No config migration. An older DIYLC reading a `datasheet:` value finds no variant of that name and places the component with no default | The configuration is shared between installed versions, so the value has to be harmless to old readers, and it is. |
| D3 | Name collision | The `datasheet:` prefix | A variant would have to be named `datasheet:...` to collide. |
| D4 | Core API | `IVariantProcessor` gains `setDefaultModel(ComponentType, String[] model)` and `isDefaultModel(ComponentType, String[] model)`; encoding and decoding stay in `VariantManager`. `Presenter.setDefaultVariant`'s body moves into `VariantManager` so both toggles share one write path; `Presenter` only delegates | Keeps the string format out of Swing, and keeps new logic out of `Presenter`. |
| D5 | Applying the default | In `setNewComponentTypeSlot`, when both template and model are null: if the default is a model, resolve its path against `componentType.getDatasheet()` and pass that row as the model; otherwise the current variant lookup. A path that no longer matches any row (a model dropped from a datasheet) falls through to no default | Same entry point every toolbox click, tree click, double click and drag already uses. Picking a model from the menu no longer also picks up a pinned variant's shape, which it used to because the lookup only checked for a missing template. |
| D6 | Pin in the UI | Only on datasheet **leaves**, and only where the item places a new component: `createDatasheetItems` gets a `boolean showDefaultPin`, true from `TreePanel` and `ComponentTabbedPane`, false from `ComponentPopupMenu` | A default makes no sense in "apply model to selection". |
| D7 | Shared pin code | The pin `JLabel` (icon, tooltip, hide-popup-then-toggle listener, border) is extracted from `createVariantItem` into one private helper used by both items; model leaves get only the pin, so their extra preferred width is 16 rather than 32 | Variant and model pins then look and behave identically. |
| D8 | Tooltips | "Set default model" / "Remove default model", and the previously hard-coded "Set default variant", "Remove default variant", "Delete variant" and the delete confirmation, all through `LangUtil.translate`, in `English.txt` and all nine translations | Required for new UI text; the variant strings sit in the same code and are localized with it. |
| D9 | Visibility of a nested default | Only the pinned leaf shows a green pin; the submenus leading to it do not | Green pins that cannot be clicked next to the one that can would be confusing. Pinning another model or a variant replaces the default anyway. |

## 4. Changes by file

| Module | File | Change |
| --- | --- | --- |
| core | `IVariantProcessor` | Add `setDefaultModel`, `isDefaultModel`. |
| core | `VariantManager` | Add `setDefaultVariant` (moved from `Presenter`), `setDefaultModel`, `isDefaultModel`, `getDefaultModel(ComponentType)` returning the resolved row or null, and the private encode helper. |
| core | `Presenter` | `setDefaultVariant` delegates; add the two delegating model methods; extend the default lookup in `setNewComponentTypeSlot` per D5. |
| swing | `ComponentButtonFactory` | Extract the pin helper; pin on datasheet leaves behind `showDefaultPin`, threaded through the recursion. |
| swing | `TreePanel`, `ComponentTabbedPane`, `ComponentPopupMenu` | Pass `showDefaultPin` (true, true, false). |
| swing | `lang/*.txt` | Seven new keys in all ten files, each file's own line endings. |
| core | `update.xml` | One `NEW_FEATURE` line in the unreleased 6.10.0 block. |

## 5. Tests

`VariantManagerTest` in `diylc-core` with a map-backed `IConfigurationManager`:

- pinning a model stores the prefixed path and `isDefaultModel` reports it;
- pinning the same model again clears it;
- pinning a model replaces a pinned variant and pinning a variant replaces a pinned model;
- `getDefaultModel` resolves the row, and returns null when the path is no longer in the datasheet;
- a legacy `category.name` key is still read and is removed on the next write.

Verification: `mvn -q -pl diylc-core -am test`, then `mvn -q -pl diylc-swing -am compile`, then a
manual check in the running app (pin a TO-1 model, click the toolbox button, confirm the part;
pin a variant and confirm the model's pin turns grey). No rendering, serialization or netlist
change, so no regression run is needed.
