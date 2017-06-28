### System requirements ###

  * [Java JRE/JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.6.0_45-b06 or newer
  * 1GB free RAM memory is recommended, 2GB or more is ideal
  * **Mac OSX** users must allow 3rd party apps before installing DIYLC. [This document](https://support.apple.com/en-us/HT202491) covers how to do it.
  
### Running the app ###

Make sure that you have downloaded the correct version. Windows build file ends with "win", Mac OSX build ends with "osx" and multi-platform build does not have any suffix and will work pretty much anywhere where Java JRE runs.

  * On **Windows** run the installer wizard and follow the steps
  * On **Linux/Unix** (will also work on **Mac OSX** if using multi-platform build): extract the entire ZIP file into a separate directory, open the terminal, change the directory to the directory where all the files are extracted (**cd `<`path to diylc3`>`**) and type **./run.sh**
  * On **OSX** just run the package to install DIYLC
  
### User interface ###

User interface can be separated into 4 major sections:

  1. **Canvas** is used as a [WYSIWYG](http://en.wikipedia.org/wiki/WYSIWYG) editor for the project.
  1. **Component tree (or toolbar)** lists all available components and allows you to pick the one you want to add to the project.
  1. **Main menu** offers file operations, such as saving, loading and exporting, clipboard operations, etc.
  1. **Status bar**: shows selection related information, zoom control, update information and memory consumption.

### Configuring the application ###

Application configuration may be changed from **Config** menu. It contains the following items:

  * **Anti-Aliasing**: when checked, objects and text will look smoother but it takes more time to render anti-aliased graphics. If you suffer from performance issues, try turning this option off.
  * **Auto-Create Pads**: when checked, application will automatically create solder pads whenever a component is added to the layout.
  * **Auto-Edit Mode**: when checked, the component edit dialog will appear after each component is created, the same way version 1.x works.
  * **Continuous Creation**: when checked, the last selected component type will remain active after you create a component, allowing you to create many components of the same type rapidly.
  * **Show Rulers**: controls whether the rulers should be displayed
  * **Show Grid**: controls whether the grid lines should be shown while working on the project.
  * **Export Grid**: controls whether the grid lines should be exported when saving the project to a file or printer.
  * **Hi-Quality Rendering**: when checked, image quality will improve slightly, but it may decrease performance.
  * **Mouse Wheel Zoom**: when checked, mouse wheel zooms in and out instead of scrolling the visible area of the project.
  * **Outline Mode**: when checked, components are drawn only as the the outline with no fill color or decorations.
  * **Snap to Grid**: when checked, drag & drop operations will snap control points to the grid instead of following the mouse cursor pixel by pixel. Hold **Ctrl + Shift** while dragging to temporarily disable 'Snap to Grid' functionality and move the selected components freely.
  * **Sticky Points**: when checked, components are allowed to stick to each other when moved. Hold **Ctrl** key while dragging to temporarily toggle the Sticky Point mode. See [Control points](#Control_points.md) for more info on control points.
  * **Theme**: allows selecting a theme. Themes are read from **themes** directory under the DIYLC root and they include background color and grid line color. You can create your own themes by adding an XML file to themes directory. It's easiest to start with a copy of one of the existing files.

### Adding components to the project ###

This sections explains how to add an existing component to the project. To learn how to develop your own components follow [this tutorial](https://github.com/bancika/diy-layout-creator/blob/wiki/ComponentAPI.md). To instantiate a component, follow these steps:

  1. Locate the component in the toolbox. Components are categorized into several folders, so make sure you're looking into the right one. To speed up the process, search box at the top of the component tree can be used to narrow down the list of displayed component types.
  2. Click on the desired component type. Note that text in the status bar changes to reflect this action.
  3. Click on the desired location on the canvas to create the component. Some components (solder pads, trace cuts, etc) will be created on single click, while others (like resistors, jumpers, etc) require two clicks to set both ending points. Instructions in the status bar will guide you through the process. Component will be drawn semi-transparent until the creation process is finalized.

Tip: pressing the tilde ('~') key repeats the last added component and places it at the current mouse location.

### Moving components around ###

Method 1: drag the selected component(s) across the canvas by clicking on the selection and moving the mouse to the desired location. When mouse cursor is over a component, it will change to "hand pointer" and status bar will read name of the component that will be dragged.
  
Method 2: move the selected components is using arrow keys with "Ctrl" key pressed.

For methods 1 and 2 note that:
  * if more than one component is selected, all of them will be moved
  * moving one (or more) components will move/stretch any components that are stuck to them. Status bar will inform you which components will be affected. Hold <b>Ctrl</b> to unstuck the selected components and move only the selected components.

Method 3: using the "Nudge" function from the "Transform" menu (or using the "Ctrl + Q" shortcut). It opens a dialog where you can type in the exact offset on both X and Y axis and specify whether to include stuck components or not.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/nudge_1.png' /></p>

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/nudge_2.png' /></p>

### Control points ###

A component may have one or more control points. Control points determine component's position on the layout and in some cases allows the component to get connected with other components. For some components, such as resistors, wires, etc, individual control points can be moved around when the component is selected.

Note that:
  * status bar lists all the components that will be affected by the drag&drop operation.
  * you can drag control points only when they turn green.

### Editing component properties ###

Newly added components are assigned with default parameters (size, color, value, name, etc), but it is possible to edit them in a few different ways. The simples way to edit properties of a single component is to double click on the component. Another way to do it is to select "Edit Selection" from the context menu aciton or press "Ctrl + E" on the keyboard. The second approach also allows editing multiple components at the same. When editing multiple components at the same time, only properties common for all the selected properties will be shown. If we, for example, want to edit a resistor and a capacitor at the same time, we will not be able to edit capacitance or resistance because they apply on to one of the selected components. Input boxes that represent properties that have mixed values are highlighted in yellow to designate that we cannot see all values from the selection. Changing a value in the yellowed box will appply it to all the selected components.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/edit_multiple.png' /></p>

Checking the "Default" checkbox on the right side will make the value for that property a default for the component. In other words, all the components created after that will inherit that value instead of the factory default value.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/edit_default_property.png' /></p>
  
Input boxes that represent measures (size, resistance, capacitance, etc) can take math expressions in addition to constants. For instance you can type 3/32 and it will be automatically converted to decimal. Even more complicated expressions with parenthesis and 4 basic numerical operators are possible.

### Assigning keyboard shortcuts to components ###

To speed up the workflow, it is possible to assign keyboard shortcuts to up to 12 most commonly used components and create them quickly without having to move the mouse away from the canvas. Only "F" keys can be assigned as keyboard shortcuts at the moment. To assign one of the "F" keys to a component, use "Assign Shortcut" menu from the drop down menu in the component toolbox.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/shortcuts_1.png' /></p>

Components that have keyboard shortcuts assigned show a visual indicator on the right side of the component name to designate the corresponding key.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/shortcuts_2.png' /></p>

To remove a keyboard shortcut, just select "None" from the "Assign Shortcut" menu.
  
### Using component variants ###

Each component is created using default values for size, color, etc. Users can make their own variants of components with different properties. After the component is edited to the desired state, it can be saved as a variant using the context menu action.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/save_variant.png' /></p>

After saving, the existing component variants can be employed in few different ways. One way would be to apply the variant to the existing component by using the context menu.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/apply_variant.png' /></p>

Applying a variant will copy all the properties associated with the variant to the selected component and transform it to conform the variant.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/apply_variant_after.png' /></p>

Another way to use variants is to add a component to the project using a saved variant as a blueprint. This can be done by right clicking on a component icon in the component tree and selecting a desired variant from the context menu.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/recall_variant.png' /></p>

Furthermore, a variant can be set as default and all components created after that will inherit their properties from the default variant. That can be done by using the pin button in the aforementioned context menu. Next to the pin button is the delete button we can use to delete a variant.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/use_variant.png' /></p>

### Grouping components together ###

Component grouping works similarly to the way object grouping works in most vector-based drawing tool. The idea is to keep two or more components together and move/edit/delete them at the same time. To group components, select them and press Ctrl+G (or select "Group Selection" from the menu). Double click on one of the grouped components will open the editor with all the mutual properties of selected components and allow editing them at the same time.
Note that while the components are grouped, you cannot edit drag their individual control points.
To un-group the components, select them and press Ctrl+U (or select "Ungroup Selection" from the menu).

Note that:
  * nested groups are not currently supported. If you group two groups of components together you'll end up with one large group instead of a group that contains two groups.
  
### Using building blocks ###

Often times we use a cluster of components arranged and configured in a certain way repetitively. To speed up the process, DIYLC allows for a group of components to be saved as a building block and recalled simply by one click. This saves time needed to add each individual component and configure it to look the way we want. Suppose we want to make a leaded TO92 component. DIYLC provides enough pieces to build one. We can add a folded TO92 body together with three separate wires.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/building_block_1.png' /></p>

To save this as a building block that we can use later, we need to select all four components and select "Save as Building Block" action from the context menu

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/building_block_2.png' /></p>

The newly created building block appears immediately under "(Building Blocks)" folder in the component tree and can be used just like any other component.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/building_block_3.png' /></p>

To delete an existing building block, use the context menu action.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/building_block_4.png' /></p>

### Working with layers ###

Component position is specified not only in X and Y axis, but also in Z axis, allowing us to control which components will be displayed on top of other components. Unlike Corel or Photoshop, Z-order of components in DIYLC is not completely free-form. Based on their nature, components are categorized into 5 "layers": Chassis, Board, Trace, Component, Text. All components in the "Board" layer will be assigned with lower Z-order than components in the "Trace" level, all components in the "Component" level will be put on top of components from "Trace" level, etc. 'Send to Back' and 'Bring to Front' operations typically operate within the given layer, but it is possible to force the component to move across other layers. DIYLC will prompt you to confirm this operation when selected component reaches the bounds of its layer.

DIYLC offers few functionalities for working with layers:

  * Temporarily hide the whole layer, helping to work at other layers if the drawing is getting crowded.
  * Lock/unlock the whole layer, keep all the components visible, but unable to select
  * Select all components beloging to a layer

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/layers.png' /></p>

### Using the status bar ###

Status bar contains the following sections (going left to right):

  * **Information bar** shows context related information and guidance through the app.
  * **Selection size** shows the dimensions of the minimal bounding rectangle that contains all the selected components. It takes default size units (in or cm) into account.
  * **Zoom control** allows you to zoom in or out.
  * **Announcements** shows public announcements if there are any posted.
  * **Auto-update** notification. When the bulb is on, there are updates available. Click on it for more information.
  * **Memory bar** is the blue-ish icon on the right side that shows the amount of memory occupied by the application. Move the mouse over it to see more details or click on it to try to cleanup as much memory as possible. Color will turn red when memory consumption is running high.
