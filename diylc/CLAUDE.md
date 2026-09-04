# DIY Layout Creator — agent guidelines

DIYLC is a Java 21 Swing desktop application for drawing circuit layouts, schematics and guitar
wiring diagrams. It is a mature codebase (~690 source files) with long-standing conventions and a
file format that must stay readable by every version. **Consistency with the surrounding code
outweighs personal preference or modern idiom in every case.**

## The one rule that matters most

Before writing anything, read two or three existing files that do the same kind of job — the
neighbouring component, the sibling plugin, the analyzer next to the one you are touching — and
match them. Match their naming, their structure, their comment density, their ordering of members,
their choice of helper classes. If your change looks different from its neighbours, it is wrong even
if it compiles and works.

## Modules

Maven reactor rooted at `diylc/pom.xml`, version `6.4.0`, three modules with a strict one-way
dependency chain:

| Module | Contains | Depends on |
| --- | --- | --- |
| `diylc-core` | Domain model, `Presenter`, rendering pipeline, messaging, serialization, netlist engine, annotations | — |
| `diylc-library` | Concrete component classes, transformers, datasheets, legacy file parsers, component-aware analyzers | `diylc-core` |
| `diylc-swing` | Swing shell, plugins, dialogs, editors, `DIYLCStarter` | `diylc-core`, `diylc-library` |

**`diylc-core` must never reference a concrete component type.** It knows components only through
`IDIYComponent` and the annotations. If core logic seems to need knowledge of a specific component,
that knowledge belongs in `diylc-library` (an analyzer, a transformer, a parser) or is exposed
through a new interface in core that the component implements — look at
`org.diylc.core.gerber.IGerberComponentSimple` for the established shape of that escape hatch.

Component and plugin classes are discovered at runtime by Reflections scanning the `org.diylc`
package (`ComponentProcessor`). There is no registry to update when adding a component.

## Build and verify

Run from `diylc/`. Before reporting a change as done:

```bash
mvn -q -pl diylc-core -am compile          # after core changes
mvn -q -pl diylc-library -am test          # after library changes (~20s)
mvn -q -pl diylc-swing -am compile         # after swing changes
mvn -q test                                # whole reactor, when the change spans modules
```

Compile the module you touched plus everything downstream of it, and run the unit tests of any
module whose tests could plausibly cover the change. Do not report success without having actually
run these — quote the failure if something breaks rather than describing it.

Do not run `mvn package`, `build-all-profiles.sh`, or the regression suite unless asked; they are
slow and the deployment build needs Docker and signing certificates. The regression suite is
described in `.claude/skills/regression-test/SKILL.md` and is worth *offering* whenever a change
affects rendering, serialization or netlist output.

The app runs from the `.run/DIYLCStarter.run.xml` IntelliJ configuration, which carries the required
`--add-opens`/`--add-exports` JVM flags. Copy them if launching from a terminal.

## Code style

The formatter is `eclipse-java-google-style.xml` (Google Java Style):

- **Two-space indent**, four spaces for continuation lines. No tabs.
- **100-column** line limit.
- Source encoding is **ISO-8859-1** — do not introduce non-ASCII characters into `.java` files. Use
  Unicode escapes or the resource bundles instead.
- Braces on the same line; single-statement `if` bodies without braces are common in this codebase
  and acceptable when you are matching the surrounding style.
- Imports: `java.*` first, then third-party, then `org.diylc.*`, separated by blank lines. No
  wildcard imports.
- Constants are `UPPER_SNAKE_CASE`. Note that component appearance constants are deliberately
  `public static` and **not** `final` (`BODY_COLOR`, `DEFAULT_WIDTH`), so they can be overridden at
  runtime; keep that convention when adding new ones.
- `private static final Logger LOG = Logger.getLogger(TheClass.class);` — log4j 1.2, declared first
  among the static members.

### Comments

Write comments only where the code cannot speak for itself: a non-obvious electrical or geometric
reason, a workaround for a library bug, a compatibility constraint, a unit or coordinate-system
assumption. Never restate the code.

```java
// BAD — adds nothing
// set the body color
g2d.setColor(bodyColor);

// GOOD — explains something the reader cannot infer
// the outer foil marking is drawn on the lead that is closer to the first control point,
// which is the convention printed on most film capacitors
```

Javadoc on public interfaces and abstract base classes in `diylc-core` is expected and should carry
`@author`. Concrete component classes generally carry no Javadoc — do not add it just to fill space.

### License header

**Every new source file starts with the GPL header**, copied verbatim from `HEADER.txt` (or from any
existing class), immediately followed by the `package` declaration with no blank line between the
closing `*/` and `package`. This applies to Java files in all three modules, including tests.

## File-format backward compatibility

DIYLC projects are XStream-serialized object graphs. Users open decade-old `.diy` files with the
current build, so **the serialized shape of a component class is a public contract**.

- Never rename or delete a serialized field, and never change its type. To replace one, keep the old
  field, add a new one, mark the old accessors `@Deprecated`, and migrate lazily inside the new
  getter. The canonical examples are `AbstractTransparentComponent.getAlpha()` (legacy `Byte alpha`
  → `Percentage alphaPercent`) and `Resistor.getPowerNew()` / `AbstractFilmCapacitor.getVoltageNew()`.
- Fields added to an existing class deserialize as `null` in old files. Every read path must tolerate
  that, typically by defaulting in the getter rather than the constructor.
- Never rename or move a component class that has shipped. If a component genuinely must change
  identity, an alias has to be registered on the XStream side and the legacy parsers in
  `V1FileParser` / `V2FileParser` reviewed.
- Prefer **a `Version` property on a single component class over a new class per revision** when a
  board or part gains a new variant. One class with a version enum keeps old files loading and keeps
  the toolbox uncluttered.
- Any change here calls for a regression-suite run.

## Components

See `.claude/skills/add-component/SKILL.md` for the full recipe. The rules that apply to *editing*
existing components too:

- Extend the most specific abstract base that fits (`AbstractLeadedComponent`,
  `AbstractRadialComponent`, `AbstractBoard`, `AbstractTransparentComponent`, …) rather than
  reimplementing shared behaviour.
- Every editable property needs a matching getter/setter pair with `@EditableProperty` on the
  **getter**; the property name is derived from the getter unless `name` is given.
- Drawing code must fetch strokes from `ObjectCache.getInstance().fetchBasicStroke(...)` /
  `fetchZoomableStroke(...)` / `fetchStroke(...)` rather than allocating `new BasicStroke(...)`.
  `draw()` runs on every repaint; do not allocate what can be cached or reused.
- Conductive areas are reported to the `IDrawingObserver` via
  `startTrackingContinuityArea(true)` / `stopTrackingContinuityArea()`. Getting this wrong silently
  breaks continuity checks and the netlist, so mirror a neighbouring component exactly.
- Rotation and mirroring live in an `IComponentTransformer` referenced from `@ComponentDescriptor`,
  never inside the component.
- `drawIcon()` draws into a small fixed box for the toolbox; it is hand-drawn, not scaled from
  `draw()`.

## Plugins and Swing

- Everything in the UI is an `IPlugIn` installed from `MainFrame` via `presenter.installPlugin(...)`.
- Plugins talk to the domain only through `IPlugInPort`, and react to domain changes by returning a
  narrow `EnumSet<EventType>` from `getSubscribedEventTypes()`. Subscribe to as little as possible —
  `PROJECT_MODIFIED` fires constantly.
- UI is injected through `ISwingUI.injectMenuAction` / `injectGUIComponent` / `injectSubmenu`, not by
  reaching into frames directly.
- Persisted settings go through `ConfigurationManager.getInstance().readString/writeValue` with a
  `String` key constant.
- User-visible strings are wrapped in `LangUtil.translate(...)`.
- Long-running work belongs on a background executor, never on the EDT.

## Testing

JUnit 4 under `src/test/java` in each module. Add tests where the project already tests: switching
logic, netlist and analyzer behaviour, datasheet parsing, core utilities. Component rendering and
user interaction are covered by the regression suite instead, not by unit tests. Test classes are
named `*Test` or `*Tests` and carry the license header like any other file.

## Git

**Do not commit or push.** Make the change in the working tree and stop; the maintainer reviews and
commits. If a commit is explicitly requested, commit only what was asked for, never to a remote, and
never `git add -A` over unrelated dirty files.

## Out of scope unless explicitly asked

- `diylc-server-api/` — PHP backend that runs in an isolated environment on diy-fever.com and cannot
  be tested locally.
- `deploy/`, `build-all-profiles.sh`, packaging and notarization.
- `lib/` — vendored jars for dependencies not on Maven Central.
- `diylc-regression-data/reports/` — historical evidence, append-only.
- Bumping the project version in the POMs.

## Task recipes

Detailed, step-by-step recipes live in `.claude/skills/` and are worth reading in full before
starting the corresponding kind of work:

| Skill | Covers |
| --- | --- |
| `add-component` | Creating or substantially changing a component in `diylc-library` |
| `add-plugin` | Adding UI: plugins, menu actions, panels, dialogs, background tasks |
| `regression-test` | Running the end-to-end regression suite and reading its report |

## Reference

Chapter 13 of *Mastering DIY Layout Creator* is the authoritative developer guide and covers the
architecture, extension points and regression suite in prose.
