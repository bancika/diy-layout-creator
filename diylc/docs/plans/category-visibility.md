# Component Category Visibility

A `Config` submenu that lets the user hide whole component categories from the
component tree and the tabbed component bar, with the filtering applied in the
`Presenter` and announced to the views through a new event.

## 1. Goal

The library ships sixteen categories (`Boards`, `Connectivity`, `Controllers`,
`Displays & Outputs`, `Electro-Mechanical`, `Guitar`, `Misc`,
`Modules & Breakouts`, `Passive`, `Robotics`, `SMD`, `Schematic Symbols`,
`Semiconductors`, `Sensors`, `Shapes`, `Tubes`). A user who only ever draws
guitar wiring has to scroll past tabs and tree nodes they will never open. The
feature is a per-user toggle list: every category visible by default, any subset
hidden, the change visible immediately in whichever browser is active.

New in `ConfigPlugin`:

```
Config
  ...
  Auto-Edit Mode
  Component Categories >
      Reset All
      ---------------
      [x] Boards
      [x] Connectivity
      ...
      [x] Tubes
  Continuous Creation
  ...
```

Disabling a category changes **only what the two browsers list**. Nothing is
hidden on the canvas, no existing project is altered, and no `.diy` file is
affected, so the serialized format and the regression references are untouched.

## 2. Four decisions taken up front

**The filtered view is a second accessor, not a change to `getComponentTypes()`.**
That method has six callers and only two of them are browsers:

| Caller | Uses the map for | Needs every category |
| --- | --- | --- |
| `CustomTreeModel` | tree nodes | no - filter here |
| `ComponentTabbedPane` | category tabs | no - filter here |
| `CanvasPanel` | type cache, keyboard-shortcut lookup | yes |
| `ExplorerPane` | list cell renderer | yes |
| `NetlistImportDialog` | netlist type mapping | yes |
| `ExportVariantsAction` | variant export tree | yes |

Had the existing method started filtering, a hidden category would silently
break the explorer's rendering of components already on the canvas and the
netlist importer's type lookup. So `getComponentTypes()` keeps its contract and
a new `getVisibleComponentTypes()` is added for the browsers.

**Favorites, Recently Used and keyboard shortcuts stay reachable.** Only the
per-category listings are filtered. A part the user explicitly pinned does not
vanish because its category was collapsed, and `CustomTreeModel`'s favorites
branch - which dereferences `typesByClass.get(fav.getName())` without a null
check - keeps working because that map is still built from the complete type
list.

**One config key holds the disabled categories**, as a `List<String>` written
through `writeValue` / read through `readObject`, the same shape as
`FAVORITES_KEY` and `RECENT_COMPONENTS_KEY`:

```
disabledComponentCategories = ["Robotics", "Tubes"]
```

Storing the *disabled* set rather than the enabled one means an empty value is
the default state, and a category introduced in a future release shows up
enabled for every existing user without a migration. "Reset All" is then simply
writing an empty list. The alternative - one boolean key per category - would
have let `ConfigAction` be reused verbatim, at the cost of config keys derived
from display names and one listener per category inside the `Presenter`.

**The submenu sits directly under `Config`**, a sibling of `Snap To`, `Theme`
and `Toolbox`, rather than nested inside `Toolbox`.

## 3. Core: the event

`diylc-core/src/main/java/org/diylc/common/EventType.java` gains a constant at
the end of the enum, documented like its neighbours:

```java
/**
 * Called when the set of enabled component categories changes. The first parameter is a
 * <code>Set&lt;String&gt;</code> of disabled category names. All categories not included are
 * enabled.
 */
COMPONENT_CATEGORIES_CHANGED
```

The payload mirrors `LAYER_VISIBILITY_CHANGED`, which passes the set of hidden
layers. Views are free to ignore it and re-read `getVisibleComponentTypes()`,
which is what both of them will do.

## 4. Core: `IPlugInPort`

Next to `FAVORITES_KEY`:

```java
public static final String DISABLED_CATEGORIES_KEY = "disabledComponentCategories";
```

and four methods, the accessor documented immediately after
`getComponentTypes()` so the difference between the two is impossible to miss:

```java
/**
 * Returns the {@link ComponentType}s that should be offered to the user, i.e. the result of
 * {@link #getComponentTypes()} without the categories the user disabled. Intended for component
 * browsers; anything that needs to resolve an arbitrary component class must use
 * {@link #getComponentTypes()} instead.
 */
Map<String, List<ComponentType>> getVisibleComponentTypes();

boolean isCategoryEnabled(String category);

void setCategoryEnabled(String category, boolean enabled);

void resetCategoryVisibility();
```

Keeping the set arithmetic behind these three mutators means the Swing action
never parses the stored list and never touches the config key, so the meaning of
"disabled" lives in exactly one place.

## 5. Core: `Presenter`

The disabled set is read from `configManager` on demand rather than cached in a
field. None of these calls is on a hot path - they run when a browser rebuilds
or a menu item is toggled - and a stateless read cannot go stale:

```java
@SuppressWarnings("unchecked")
private Set<String> getDisabledCategories() {
  return new HashSet<String>(
      (List<String>) configManager.readObject(DISABLED_CATEGORIES_KEY, new ArrayList<String>()));
}
```

`getVisibleComponentTypes()` copies the entries it keeps into a new map;
`ComponentProcessor` hands back its own cached instance, which must not be
mutated.

`setCategoryEnabled` adds or removes one name and writes a sorted
`ArrayList<String>` back, so the config file stays stable across sessions.
`resetCategoryVisibility()` writes an empty list.

The event is fired from a config listener, registered in the constructor next to
the existing `SHOW_NODE_NAME_TOOLTIPS_KEY` one, rather than from the setters:

```java
configManager.addConfigListener(IPlugInPort.DISABLED_CATEGORIES_KEY, new IConfigListener() {
  @Override
  public void valueChanged(String key, Object value) {
    messageDispatcher.dispatchMessage(EventType.COMPONENT_CATEGORIES_CHANGED,
        getDisabledCategories());
  }
});
```

Any writer then refreshes the views, not just the menu. The dispatcher is
constructed with `new MessageDispatcher<EventType>(true)` - synchronous - so the
views are rebuilt on the same EDT call that toggled the checkbox, and no
`invokeLater` is needed.

## 6. Swing: the menu

Two new action classes beside `ConfigAction` and `ComponentBrowserAction` in
`org.diylc.swing.actions`:

- **`ComponentCategoryAction`** - one per category. Modelled on `ConfigAction`:
  `NAME` is the category, `IView.CHECK_BOX_MENU_ITEM` is `true`,
  `SELECTED_KEY` is seeded from `plugInPort.isCategoryEnabled(category)`, and a
  listener on `DISABLED_CATEGORIES_KEY` re-reads that flag so "Reset All"
  re-checks every item in the submenu. `actionPerformed` calls
  `setCategoryEnabled`.
- **`ResetCategoriesAction`** - a plain item calling
  `resetCategoryVisibility()`.

`ActionFactory` gets `createComponentCategoryAction(plugInPort, category)` and
`createResetCategoriesAction(plugInPort)` next to
`createComponentBrowserAction`.

`ConfigPlugin` gains `CATEGORIES_MENU = "Component Categories"` and, placed
alphabetically between "Auto-Edit Mode" and "Continuous Creation" to match the
ordering of the rest of the menu:

```java
swingUI.injectSubmenu(CATEGORIES_MENU, IconLoader.FolderPreferences.getIcon(), CONFIG_MENU);
swingUI.injectMenuAction(
    ActionFactory.getInstance().createResetCategoriesAction(plugInPort), CATEGORIES_MENU);
swingUI.injectMenuAction(null, CATEGORIES_MENU);
plugInPort.getComponentTypes().keySet().stream().sorted()
    .forEach(category -> swingUI.injectMenuAction(
        ActionFactory.getInstance().createComponentCategoryAction(plugInPort, category),
        CATEGORIES_MENU));
```

The items are built from the **complete** keyset - a disabled category still
needs a checkbox to turn it back on. `injectMenuAction(null, ...)` is the
established way to add a separator, and `MainFrame.injectMenuAction` already
pushes action names through `LangUtil.translate`, so category names are
translated like every other menu label.

## 7. Swing: the component tree reacts

`CustomTreeModel` takes its categories from the filtered map while keeping the
class lookup complete:

```java
this.componentTypes = plugInPort.getVisibleComponentTypes();
...
for (Map.Entry<String, List<ComponentType>> e : plugInPort.getComponentTypes().entrySet())
  for (ComponentType c : e.getValue())
    typesByClass.put(c.getInstanceClass().getCanonicalName(), c);
```

and gains a `refresh()` that re-reads the visible map, rebuilds `categories`
with `(Favorites)`, `(Recently Used)` and `(Building Blocks)` back on front, and
calls the existing `updateVisibleCategories()`, which already clears the leaf
cache and fires `treeStructureChanged`. The constructor's category setup moves
into that method so there is one copy of it. `TreePanel` exposes it through its
existing `getTreeModel()`.

`ComponentTree` currently returns `null` from `getSubscribedEventTypes()` with a
TODO and does nothing in `processMessage`. Both are filled in:

```java
@Override
public EnumSet<EventType> getSubscribedEventTypes() {
  return EnumSet.of(EventType.COMPONENT_CATEGORIES_CHANGED);
}

@Override
public void processMessage(EventType eventType, Object... params) {
  if (eventType == EventType.COMPONENT_CATEGORIES_CHANGED) {
    getTreePanel().getTreeModel().refresh();
  }
}
```

Search text survives the refresh because `updateVisibleCategories()` re-applies
it; tree expansion state does not, which is acceptable for an explicit menu
action.

## 8. Swing: the tabbed toolbar reacts

`ComponentTabbedPane` builds its category tabs in the constructor, after the
three fixed tabs. That loop moves into `refreshCategoryTabs()`, which removes
every tab from index 3 onwards and re-adds one per visible category. Each
category's panel is cached in a `Map<String, Component>`, because
`createComponentPanel` instantiates a `ComponentButtonFactory` button - with its
icon and variant popup - for every type in the category; toggling a category
must not pay that cost again for the fifteen categories that did not change.

The selected tab is remembered by title and restored if it is still present. A
`refreshing` guard keeps the existing `ChangeListener` from persisting
intermediate `lastSelectedTab` indices while tabs are being removed.

`ToolBox` fills in the same `null`/TODO pair as `ComponentTree`, delegating to
`getComponentTabbedPane().refreshCategoryTabs()`.

## 9. What is deliberately not touched

`CanvasPanel`, `ExplorerPane`, `NetlistImportDialog` and `ExportVariantsAction`
keep calling `getComponentTypes()` and keep seeing every category, so keyboard
shortcuts, the project explorer's renderer, netlist import and variant export
behave identically whatever is hidden.

## 10. Verification

```bash
mvn -q -pl diylc-swing -am compile    # core + library + swing
mvn -q test                           # reactor, since core changed
```

Then a manual pass in the app: toggle a category with the tree active and with
the tabbed toolbar active, confirm both update without reopening the menu,
confirm "Reset All" re-checks every item, confirm a favorited part from a hidden
category is still clickable, and confirm the setting survives a restart.

The change touches neither rendering, serialization nor netlist output, so a
regression-suite run is not called for.
