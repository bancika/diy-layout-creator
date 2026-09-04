---
name: add-plugin
description: Add or modify a DIYLC Swing plugin, menu action, toolbar item, dialog or side panel. Use when asked to add a UI feature, a menu entry, a new tool window, a background task, or to react to domain events like selection or project changes.
---

# Adding a plugin or UI feature to DIYLC

The Swing module is a shell that hosts plugins. Everything the user sees — the toolbox, the canvas,
every menu, the status bar, autosave — is an `IPlugIn`. Adding UI means adding or extending a plugin,
not reaching into frames.

## Where things go

| Path | Contents |
| --- | --- |
| `diylc-swing/src/main/java/org/diylc/swing/plugins/<name>/` | One package per plugin |
| `diylc-swing/src/main/java/org/diylc/swing/actions/` | `AbstractAction` implementations, grouped into `file`, `edit`, `analyze` sub-packages |
| `diylc-swing/src/main/java/org/diylc/swing/gui/` | `MainFrame`, dialogs, editors, shared components |
| `diylc-swing/src/main/java/org/diylc/swing/gui/MainFrame.java` | Where plugins are installed |

## Before writing anything

Read the plugin closest to what is being asked. `plugins/autosave/AutoSavePlugin.java` is the
smallest complete example (event subscription plus background work);
`plugins/statusbar/`, `plugins/layers/` and `plugins/analyze/` show menu and panel injection.
Match the one you read.

## The plugin

```java
/* ... GPL header from HEADER.txt ... */
package org.diylc.swing.plugins.myfeature;

public class MyFeaturePlugin implements IPlugIn {

  private static final Logger LOG = Logger.getLogger(MyFeaturePlugin.class);

  private ISwingUI swingUI;
  private IPlugInPort plugInPort;

  public MyFeaturePlugin(ISwingUI swingUI) {
    this.swingUI = swingUI;
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    swingUI.injectMenuAction(new MyAction(plugInPort), "Tools");
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.SELECTION_CHANGED);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    if (eventType == EventType.SELECTION_CHANGED) {
      ...
    }
  }
}
```

Install it in `MainFrame` alongside the others:

```java
presenter.installPlugin(() -> new MyFeaturePlugin(this));
```

Order matters for menus and for docked panels — insert the line where the resulting menu or panel
position is correct, not blindly at the end.

## Talking to the domain

Plugins reach the domain **only** through `IPlugInPort`. Never import a concrete component type into
a plugin unless the feature is genuinely component-specific, and never call into `Presenter`
directly. If `IPlugInPort` lacks what you need, adding a method there is the right move — but it also
means touching `diylc-core`, so keep the addition minimal and in the style of its neighbours.

## Reacting to events

`getSubscribedEventTypes()` returns an `EnumSet<EventType>`. Subscribe to the narrowest set that
works. `PROJECT_MODIFIED`, `REPAINT` and `MOUSE_MOVED` fire constantly — anything non-trivial in
response to those will be felt as lag. The available types are in
`diylc-core/src/main/java/org/diylc/common/EventType.java` (`PROJECT_LOADED`, `PROJECT_SAVED`,
`SELECTION_CHANGED`, `ZOOM_CHANGED`, `SLOT_CHANGED`, `LAYER_STATE_CHANGED`, `CLOUD_LOGGED_IN`, …).

## Injecting UI

- `swingUI.injectMenuAction(action, "Menu Name")` — a menu item; passing a `null` action inserts a
  separator, and a missing menu is created automatically.
- `swingUI.injectSubmenu(name, icon, parentMenuName)` and
  `injectDynamicSubmenu(name, icon, parentMenuName, handler)` for submenus.
- `swingUI.injectGUIComponent(component, SwingConstants.LEFT, collapsible, visibilityConfigKey)` for
  a docked panel. `TOP`, `BOTTOM`, `LEFT`, `RIGHT` are valid; `CENTER` is reserved for the canvas.
  Pass a config key for collapsible panels so the open/closed state persists.
- `swingUI.getOwnerFrame()` is the parent for dialogs.

## Actions

Actions are small classes extending `javax.swing.AbstractAction`, holding an `IPlugInPort`, setting
their label with `putValue(AbstractAction.NAME, ...)`, and delegating to `plugInPort` in
`actionPerformed`. See `actions/RenumberAction.java` for the minimal shape. They carry
`private static final long serialVersionUID = 1L;` like any Swing class.

## Background work

Never block the EDT. Use `swingUI.executeBackgroundTask(task, blockUI)` with an `ITask<T>`, which
handles the busy cursor and optional glass pane, or a dedicated executor for periodic work as
`AutoSavePlugin` does.

## Settings and strings

- Persisted preferences: `ConfigurationManager.getInstance().readString(KEY, default)` and
  `writeValue(KEY, value)`, with the key as a `String` constant. Implement `IConfigListener` to react
  to changes.
- Every user-visible string goes through `LangUtil.translate("...")`.
- Icons come from `IconLoader`.

## Verify

```bash
mvn -q -pl diylc-swing -am compile
```

Compilation is not evidence that the UI works. Say so, and offer to walk through launching the app
via `.run/DIYLCStarter.run.xml` (which carries the required `--add-opens` / `--add-exports` flags) for
a visual check.
