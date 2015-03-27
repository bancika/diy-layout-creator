### General advice ###

**When making a new component make sure to look around and be consistent with what's already there. This applies to component appearance, behavior but also the code. It is important to keep the visual identity and user experience consistent across the board and to have a maintainable code base that follows the same coding standards and patterns. Good luck.**

### Prerequisites ###

  * [JDK or JRE](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.6.0\_10 or newer installed.
  * Some Java IDE, preferably [Eclipse](http://www.eclipse.org/).
  * SVN client, [Subclipse](http://subclipse.tigris.org/) is recommended if you're using Eclipse. Follow [this guide](http://code.google.com/p/diy-layout-creator/source/checkout) to learn how to checkout the project from the source control.
  * Decent knowledge of Java language, in specific [generics](http://java.sun.com/j2se/1.5/pdf/generics-tutorial.pdf), [annotations](http://download.oracle.com/javase/tutorial/java/javaOO/annotations.html) and [Graphics2D](http://download.oracle.com/javase/tutorial/2d/overview/index.html). Unlike the previous version, you actually need to write Java code to make the component look and behave the way you want it. This makes it harder for non-programmers to make their own components or customize existing ones, but it allows greater flexibility and performance.

### Component design guidelines ###

  * Make sure that your code is formatted using **diylc** formatter (included in the source code) before committing.
  * Add JavaDoc on the new classes and public method you add. In particular, classes should have a brief description of what they represent and author name. Typical methods should have a one-two line description of what they do, what are the parameters and what's the output.
  * Output to console should go through log4j, as shown below. Make sure to use the appropriate logging levels (debug, info, warn, error).
```
private static final Logger LOG = Logger.getLogger(MyClass.class);
...
LOG.info("Something happened.");
```
  * Icons for leaded components (resistors, jumpers, wires, etc) should be drawn in the SW-NE direction, like shown below
<p align='center'><img src='http://www.diy-fever.com/diylc/images/icons.png' /></p>
  * Component should be drawn differently when selected. Where applicable, component border should be painted red (resistors). In some cases it's necessary to paint the whole component red (jumper, trace, etc). Components that are red by default (trace cut) should be painted blue when selected.
  * When a large component (board, chassis, etc) is dragged, only outline should be pained, together with elements that correspond to its control points. Smaller components that do not cover large areas should be painted the same as when they are selected.
  * When applying transparency - border, leads and label should not be made transparent to keep them readable. However, large surfaces, like component body should be made transparent.

Here's an example that should be a good guideline for painting components in different situations:
<p align='center'><img src='http://www.diy-fever.com/diylc/images/coloring.png' /></p>

### Adding to the main library ###

  1. Checkout **DIYLC** and **Library** folders from the SVN.
  1. New components should be created inside **org.diylc.components** package.
  1. When you are done developing and testing the new components, build the project into **DIYLC/library/main.jar**.
  1. Commit new classes to the source control.
  1. Commit the updated library/main.jar back to the source control.

### Creating your own component library ###

  1. Create a new Java project.
  1. Add **diylc.jar** to the dependencies.
  1. Since it's your own project, you can use whichever package name you like.
  1. When you are done developing and testing the new components, build your project as jar file (any name except for main.jar is fine).
  1. To add your components to the DIYLC project commit your new jar to **library** folder in the DIYLC source control. It should be automatically recognized by the app.

### Component API ###

Each component is a separate class. From coding perspective, components are **annotated entity bean classes that know how to draw themselves**. In order for a component class to be recognized by the application it has to meet the following:

  * It must implement (directly or indirectly) **`IDIYComponent<T>`** component interface. It is advisable, whenever possible, to extends one of the abstract classes instead of making a component from scratch. See below the list of them and more details.
  * It must have an empty constructor. It will be used by the application to instantiate the component, so there's no need for other constructors.
  * It is not mandatory but highly recommended to annotate each component class with **`@ComponentDescriptor`** annotation. Application will recognize and install the component even without it, but you will be unable to configure all the aspects of a component and they will be defaulted to values that are potentially unsuitable for the component.
  * All component properties should be annotated with **`@EditableProperty`** annotation.
  * It should be compiled, packed inside a jar and placed in **library** folder.
  * It must be serializable, i.e. all of it's fields need to be serializable. Any fields that are not necessary for serialization (typically caches) should be made `transient` as well as any fields that are not serializable. Component should know how to handle null values for those fields because they will be nulled after deserialization (when a project is loaded or copy/paste).

#### `IDIYComponent<T>` interface ####

Base interface for all components. Class parameter T represents type of component value. For instance, resistor will have it's value type set to `Resistance` and IC will have the type of `String` (to designate IC code). In some cases, components do not have a value (solder pads for instance) and it's ok to use `Void` type as a parameter. Below is the list of methods specified by the interface.

  * **getName** and **setName** used to obtain and update component instance name. Annotate it with `@EditableProperty` to allow the user to edit component name.
  * **getValue** and **setValue** similar to the previous.
  * **getControlPointCount**, **getControlPoint** and **setControlPoint** used to obtain and update component's control points. Note that two instances of the same component may have a different number of control points, e.g. 16 and 8 pin versions of DIL IC.
  * **isControlPointSticky** returns true for all control points that may stick to other components' control points. It may be the case that some control points may and the others may not, even within the same component. For instance, hookup wire component has 4 control points, but only two ending points may stick; the middle two are there only to control wire shape and should not stick to other control points.
  * **getControlPointVisibilityPolicy** tells when to render each control point. Use `ALWAYS` to show the control point all the time, `WHEN_SELECTED` to show it only when component is selected or `NEVER` to never render the control point. Use `ALWAYS` for control points of resistors or similar components with ending points. `WHEN_SELECTED` is useful for board ending points, or shape control points of hookup wire. `NEVER` is suitable for components like IC or toggle switch that already have visible pins and should not render additional control points.
  * **draw** pains the component instance onto the specified `Graphics2D`. Typically, this method would use the state of the component (control points, value and other user editable properties) to draw it.
  * **drawIcon** paints the component icon of the specified size onto the specified `Graphics2D`. It should act like a static method, i.e. not depend on state of the component.

#### `@ComponentDescriptor` annotation ####

May be applied only to classes that implement `IDIYComponent` interface. Contains the following attributes:

  * **name**: name of component type, e.g. "Resistor". This will be shown in the toolbox.
  * **description**: one or two sentence description of the component. Will be shown in the tooltip over toolbox buttons.
  * **author**: name of the developer who wrote the component.
  * **instanceNamePrefix**: prefix that will be used to generate component instance names, e.g. "R" for resistors or "Q" for transistors. Application will append the index automatically to form component instance names, such as "Q1". Do not leave it blank or null because components instances will be named "1", "2", etc. Not something we want.
  * **zOrder**: Z-order of the component, lower number meaning that component will be shown in the back. `IDIYComponent` lists major layers, but you can use any double number. Sometimes it's useful to add a small offset to an existing zOrder. For instance, solder pads and copper traces belong to the same layer but pads should be always rendered above traces to show the hole correctly. That's why it's zOrder is increased for 0.1.
  * **stretchable**: when false, moving one control point will cause all the others to move together with it, default is `true`.

Sample usage:
```
@ComponentDescriptor(name = "Resistor", author = "Branislav Stojkovic", category = "Passive", instanceNamePrefix = "R",
                     description = "Resistor layout symbol", zOrder = IDIYComponent.COMPONENT)
public class Resistor extends AbstractLeadedComponent<Resistance> {
```

#### `@EditableProperty` annotation ####

May be applied only to component a property getter that has a matching setter. Application will recognize all editable properties and allow the user to edit them through GUI. Based on the property type, different GUI control will be created that is suitable for that data type. Below is the list of supported property types:

  * **Byte** and **byte**: create a slider that has range from 0 to 127.
  * **Color**: creates a colored panel that opens the color picker when clicked.
  * **Enum**: creates a combo box with all values from the enum.
  * **Measure**: creates a text field and combo box that edit measure value and units respectively. See the section that explains measures for more details.
  * **String**: creates a text field.

Data types not listed above are currently not supported and should not be used with editable properties.

#### `AbstractComponent` class ####

Base class that should be use for pretty much all components that do not have a more suitable abstract class as a base. Defines component name and allows for editing. Also, this class contains a convenience method `getClosestOdd(int x)` that returns the closes odd number to the specified number. This is useful when drawing a component, as object with even size do no align perfectly to the baseline.

#### `AbstractTransparentComponent` class ####

Base class for all components that may be transparent. Extends `AbstractComponent` and defines alpha level ranging from 1 to 127. It is up to the component to use the protected byte field `alpha` when drawing.

#### `AbstractLeadedComponent` class ####

This class should be used as a base class for all components that have two flying leads and solid color body that may be represented with a `Shape` object, e.g. resistor or capacitor layout symbols. It extends `AbstractTransparentComponent` and adds common logic that makes it easier to create similar components. It has the following features:

  * Draws leads automatically.
  * Allows user to choose component body width and height.
  * Defines two control points for lead ends.
  * Defines component name and allows for editing.
  * Positions and draws component body based on component location on the canvas.

Child classes should implement the following methods:

  * **`getDefaultWidth`** and **`getDefaultHeight`** are used to determine default component body size. Note that users may change this later; do not use these when creating body shape.
  * **`getBodyColor`** and **`getBorderColor`** are used to determine colors for body and body border.
  * **`getBodyShape`** should return a `Shape` object that represents component body, e.g. rectangle for resistors or ellipse for ceramic capacitors. Shape should include the current component size into account, as specified in protected fields `width` and `height`. Shape should **not** include component placement into account and should be not be scaled or rotated. Also, the minimum bounding rectangle of the shape should have it's top left corner at point (0, 0). This is important because abstract class relies on that fact when drawing the component body on the canvas.

#### `AbstractCurvedComponent` class ####

This class should be used as a base class for all components that may be represented by a cubic (Bezier) curve. It extends `AbstractTransparentComponent` and adds 4 control points - two for start/end and two curve control points. Also, it allows the user to edit component color and draws 3 guidelines between the control points. It's up to the child class to draw the `CubicCurve2D` object and to provide the default color.

#### Measures ####

DIYLC supports several different measures. All of them extend **`AbstractMeasure<T>`** class. Type parameter is enum that implements **`Unit`** interface and designates measure units. To illustrate how it works, see the example below. It defines an enum for all possible resistance units (ohm, K, M) and then creates a measure that uses it.

```
public enum ResistanceUnit implements Unit {

        R(1, "\u03a9"), K(1e3, "K"), M(1e6, "M");

        double factor;
        String display;

        private ResistanceUnit(double factor, String display) {
                this.factor = factor;
                this.display = display;
        }

        @Override
        public double getFactor() {
                return factor;
        }

        @Override
        public String toString() {
                return display;
        }
}


public class Resistance extends AbstractMeasure<ResistanceUnit> {

        public Resistance(Double value, ResistanceUnit multiplier) {
                super(value, multiplier);
        }

        @Override
        public Resistance clone() throws CloneNotSupportedException {
                return new Resistance(value, unit);
        }
}

```

Measure class `Size` has a convenience method `convertToPixels` that converts any `Size` object to pixels.