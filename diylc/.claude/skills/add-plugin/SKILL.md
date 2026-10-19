---
name: add-plugin
description: Add or modify a DIYLC Swing plugin, menu action, toolbar item, dialog or side panel. Use when asked to add a UI feature, a menu entry, a new tool window, a background task, to react to domain events like selection or project changes, or to localize UI text and update the language files.
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
- Every user-visible string goes through `LangUtil.translate("...")` — see *Localization* below.
- Icons come from `IconLoader`.

## Localization

DIYLC ships translations in `diylc-swing/src/main/resources/lang/`. `LangUtil.translate(text)` looks
the whole English string up, case-insensitively, and falls back to the English text when there is no
entry. A string that never reaches `translate` is never localized, so every new piece of UI needs
both the call and the dictionary entries. Adding the entries and translating them into **every**
language file is part of the change itself — do not leave it as a follow-up or ship English-only
keys.

**What `MainFrame` translates for you.** Menu names and the `Action.NAME` of every action passed to
`injectMenuAction`, `injectSubmenu` and `injectDynamicSubmenu` are translated on injection. Pass the
English literal there and do not translate it yourself: `ToggleAction` and `ComponentBrowserAction`
use their English title as the persisted config value, and translating a translated string can hit
an unrelated entry.

**What you must translate yourself.** Everything else the user reads:

- the message *and* the title of `showMessage`, `showConfirmDialog` and `showInputDialog` — titles
  like `"Error"`, `"Success"` and `"Export"` included;
- dialog titles, button and label text, tooltips, placeholder text and borders;
- text rendered into panes, including HTML (translate the sentence, keep the markup outside it).

**Text with variables uses placeholders, never concatenation.** The key passed to `translate` is the
whole sentence with `%s` where the values go, and `String.format` fills them in afterwards:

```java
// BAD — every file name produces a different key, so nothing is ever translated
swingUI.showMessage("Variants exported to \"" + file.getName() + "\".", "Success", ...);

// GOOD
swingUI.showMessage(
    String.format(LangUtil.translate("Variants exported to \"%s\"."), file.getName()),
    LangUtil.translate("Success"), ISwingUI.INFORMATION_MESSAGE);
```

- Use `%s` for every value, numbers included; a translator whose grammar needs another order writes
  `%2$s ... %1$s`. A literal percent sign must be `%%`.
- One key per complete sentence. Do not build a sentence from translated fragments — word order
  differs between languages. A long paragraph may be several keys, one per sentence.
- URLs, file names, HTML links and other values that must not change go in through `%s`, not into
  the key.
- Log messages and exception text stay in English and are not translated.

**Dictionary entries.** For each new key:

- append the English text on its own line to `English.txt`;
- append `English text|Translation` to every other file in `lang/`, keeping the terminology that
  file already uses (grep it for `Error`, `Export`, `Variants`, ...). The translation must contain
  the same number of placeholders as the key, and neither side may contain `|`;
- recent entries are appended at the end of each file; do the same, and preserve each file's line
  endings (most use CRLF, `हिंदी.txt` and `简体中文.txt` use LF) and UTF-8 encoding.

Running the app with `-Dorg.diylc.storeDict=true` writes every string that reached `translate`
without a dictionary entry to `pending_translations.txt` in the working directory, which is a quick
way to find gaps after a UI change.

## Verify

```bash
mvn -q -pl diylc-swing -am compile
```

Confirm that every new user-facing string reaches `LangUtil.translate` and has an entry in each file
under `diylc-swing/src/main/resources/lang/`.

Compilation is not evidence that the UI works. Say so, and offer to walk through launching the app
via `.run/DIYLCStarter.run.xml` (which carries the required `--add-opens` / `--add-exports` flags) for
a visual check.
