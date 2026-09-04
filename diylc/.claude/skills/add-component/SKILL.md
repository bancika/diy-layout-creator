---
name: add-component
description: Add a new component type to the DIYLC library, or substantially modify an existing one. Use when asked to create a new part (resistor variant, board, jack, switch, pickup, symbol, shape), add editable properties to a component, change how a component draws or reports connectivity, or make a component rotatable/mirrorable.
---

# Adding a component to DIYLC

Components live in `diylc-library/src/main/java/org/diylc/components/`. They are discovered at
runtime by Reflections scanning `org.diylc` — **there is no registry to update**. A class becomes a
component by carrying `@ComponentDescriptor` and implementing `IDIYComponent` (in practice, by
extending one of the abstract bases).

## Before writing anything

1. **Find the closest existing component and read it end to end.** A new axial part follows
   `passive/Resistor.java`; a new board follows `boards/VeroBoard.java`; a new schematic symbol
   follows `AbstractSchematicLeadedSymbol` subclasses; a new jack follows `electromechanical/`.
   The new class should read like a sibling of that file.
2. **Ask whether it is really a new class.** If the request is a revision or size variant of
   something that already exists, prefer adding a `Version` (or similar enum) property to the
   existing class over creating a parallel class. That keeps old project files loading and keeps the
   toolbox from sprawling.
3. **Pick the right package** — `passive`, `semiconductors`, `electromechanical`, `boards`, `guitar`,
   `tube`, `chassis`, `shapes`, `smd`, `connectivity`, `misc`, `autocreate`. The package should match
   the `category` in the descriptor.

## Choosing a base class

| Base | Use for |
| --- | --- |
| `AbstractComponent<T>` | Anything that fits nothing else |
| `AbstractLabeledComponent<T>` | Adds name/value label rendering |
| `AbstractTransparentComponent<T>` | Anything with an alpha/transparency property |
| `AbstractLeadedComponent<T>` | Two-lead axial parts drawn between two points; implement `getDefaultLength()`, `getDefaultWidth()`, `getBodyShape()` |
| `AbstractRadialComponent<T>` | Radial (two leads out the same side) parts |
| `AbstractCurvedComponent<T>` | Curves and traces defined by control points |
| `AbstractAngledComponent<T>` | Parts placed by point and angle |
| `AbstractBoard` / `AbstractProtoBoard` / `AbstractVeroBoard` | Boards |
| `AbstractMultiPartComponent<T>` | Components made of several independently drawn sections |
| `Abstract3LegSymbol`, `AbstractDiodeSymbol`, `AbstractTubeSymbol`, `AbstractGuitarPickup`, `AbstractShape` | The obvious specialisations |

Extend the most specific base that fits. Do not reimplement behaviour a base already provides.

## The class

```java
/* ... GPL header copied verbatim from HEADER.txt ... */
package org.diylc.components.passive;

import ...                                  // java.*, then third-party, then org.diylc.*

@ComponentDescriptor(name = "My Part", author = "Branislav Stojkovic", category = "Passive",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "X",
    description = "Short sentence shown in the toolbox tooltip", zOrder = IDIYComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class, enableCache = true)
public class MyPart extends AbstractLeadedComponent<Resistance> {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_WIDTH = new Size(1d / 2, SizeUnit.in);
  public static Color BODY_COLOR = Color.decode("#82CFFD");

  private Resistance value = null;

  public MyPart() {
    super();
    this.bodyColor = BODY_COLOR;
  }

  @EditableProperty(validatorClass = PositiveMeasureValidator.class)
  public Resistance getValue() {
    return value;
  }

  public void setValue(Resistance value) {
    this.value = value;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) { ... }
}
```

Requirements the framework enforces:

- **No-argument public constructor**, and the class must be serializable with a
  `private static final long serialVersionUID = 1L;`.
- `@ComponentDescriptor` needs `name`, `description`, `category`, `author`, `instanceNamePrefix` and
  `zOrder` at minimum. Set `enableCache = true` for anything whose drawing is expensive and does not
  depend on neighbouring components.
- Appearance constants are `public static` and deliberately **not final**, so they can be overridden
  at runtime. Follow that.

## Editable properties

- Annotate the **getter** with `@EditableProperty`; a matching `setXYZ` must exist. The property name
  comes from the getter unless you pass `name`.
- Use `sortOrder` when the order in the editor matters, `defaultable = false` for properties that
  must not be remembered as a user default, and `validatorClass` (e.g.
  `PositiveMeasureValidator`, `PositiveNonZeroMeasureValidator`) where a raw value would be invalid.
- Use the measure types from `org.diylc.core.measures` (`Size`, `Resistance`, `Capacitance`,
  `Voltage`, `Power`, `Percentage`) rather than raw numbers — they carry units and format themselves.
- For a runtime-computed list of allowed values, add `@DynamicList(availableValueFunction = "getXyz")`
  alongside `@EditableProperty`. (The `additionalOptions` example in `EditableProperty`'s own Javadoc
  is stale — that attribute no longer exists.)
- Override `getValueForDisplay()` when the canvas label should combine several properties.

## Drawing

`draw(Graphics2D, ComponentState, boolean outlineMode, Project, IDrawingObserver)` runs on every
repaint, for every instance. Treat it as a hot path.

- Fetch strokes from `ObjectCache.getInstance().fetchBasicStroke(w)`, `fetchZoomableStroke(w)` or
  `fetchStroke(w, dash, phase, cap)`. Do not allocate `new BasicStroke(...)` in `draw()`.
- Honour `outlineMode` and every `ComponentState` (`NORMAL`, `SELECTED`, `DRAGGING`) the way the
  neighbouring components do.
- `drawIcon(g2d, width, height)` is a separate, hand-drawn miniature for the toolbox — it is not a
  scaled call into `draw()`.

### Connectivity

If the component conducts, wrap the conductive drawing in
`drawingObserver.startTrackingContinuityArea(true)` / `stopTrackingContinuityArea()`, exactly as the
comparable component does. Getting this wrong silently corrupts continuity checks and the netlist
without any visible rendering difference. `startTracking()` / `stopTracking()` control whether
drawing contributes to the component's clickable area.

Also implement, where relevant:

- `getControlPointCount()`, `getControlPoint(int)`, `setControlPoint(Point2D, int)`,
  `isControlPointSticky(int)`, `getControlPointNodeName(int)` — the connection points.
- `getInternalLinkName(int, int)` and `getSectionNames(int)` — internal conduction between points,
  needed by the netlist and analyzers.
- `IGerberComponentSimple` or `IGerberComponentCustom` if the part should appear in Gerber output.

## Rotation and mirroring

These live in an `IComponentTransformer` under
`diylc-library/src/main/java/org/diylc/components/transform/`, referenced from the `transformer`
attribute of `@ComponentDescriptor` — never inside the component itself. Reuse
`SimpleComponentTransformer` or an existing transformer where the behaviour matches; write a new one
only when the part rotates or mirrors differently from everything that exists.

## Datasheets and variants

If the part has a catalogue of real-world values and sizes, set `enableDatasheet = true` and
`datasheetCreationStepCount`, and add the file under
`diylc-library/src/main/resources/datasheets/`. Follow the format of an existing `.datasheet` file
and add a spot-check to `DatasheetServiceTest`.

## Backward compatibility

Once a component ships, its serialized shape is a contract:

- Never rename, remove, or retype a serialized field, and never rename or move the class.
- To replace a property, keep the old field, mark the old accessors `@Deprecated`, add the new field,
  and migrate lazily in the new getter — see `AbstractTransparentComponent.getAlpha()`,
  `Resistor.getPowerNew()`, `AbstractFilmCapacitor.getVoltageNew()`.
- Fields added later arrive as `null` from old files; default them in the getter, not the
  constructor.

## Verify

```bash
mvn -q -pl diylc-library -am test
```

Then state plainly that the component still needs a visual check in the running app — automated tests
do not cover component appearance. Offer a regression-suite run (see the `regression-test` skill) for
any change to an existing component's drawing or serialization.
