DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - System requirements';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - System requirements','* [Java JRE/JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.8 or newer 
* 1GB free RAM memory is recommended, 2GB or more is ideal 
* **Mac OSX** users must allow 3rd party apps before installing DIYLC. [This document](https://support.apple.com/en-us/HT202491) covers how to do it.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Installing the app - Windows';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Installing the app - Windows','There are several ways to run DIYLC on Windows:  
**Windows 64bit** – Download the Windows installer build (ending with *-win64.exe*) and follow the wizard to install it. Java JRE is not required as it comes bundled with the installation.  
– Download the Windows ZIP build (ending with *-win64.zip*) and unpack to the desired folder. Java JRE is not required as it comes bundled with the installation.  
**Windows 32bit** – Download the universal ZIP build (ending with *-universal.zip*) and unpack to the desired folder. Java JRE 17 or newer is required because universal builds come without a bundled JRE.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Installing the app - Mac OS';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Installing the app - Mac OS','Download the OSX build matching your Mac\'s architecture (ending with *-osx-arm.zip* for Apple Silicon M1/M2/M3 processors or *-osx-x86.zip* for older Intel-based Macs) and unpack it into the Applications folder. Java JRE is not required as it comes bundled with the app. There are two ways to get DIYLC to run on a Mac:  
– Download a signed and notarized version from DIYLC Patreon (no extra steps needed). Visit DIYLC\'s Patreon to become a member.  
– Download the unsigned build and allow DIYLC to run after OSX Gatekeeper blocks it initially as unsafe (details below).');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Installing the app - Mac OS - Installing on OSX Sierra and El Captain';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Installing the app - Mac OS - Installing on OSX Sierra and El Captain','Since version 4.0.0, DIYLC runs on Java 8 which supports newer Mac machines, but can cause issues on older Sierra and El Capitan Macs. If the OSX application bundle *diylc-4.X.Y-osx.zip* fails on Sierra or El Capitan, you can still run the multi-platform version:  
1. Download the latest *diylc-4.X.Y.zip* (without "osx" in the name) and extract it to a folder.  
2. Open the Terminal, navigate to the extracted DIYLC directory, and change the permissions of *run.sh*:  
   `sudo chmod 755 run.sh`  
3. Run the app using: `./run.sh`  
If DIYLC still fails to start, try double-clicking on **diylc.jar** to launch the app.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Installing the app - Linux';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Installing the app - Linux','DIYLC for Linux is packaged as an AppImage (since version 5.0.0) and includes a bundled JRE 17. If you encounter issues with the AppImage build, you can always use the universal build.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Installing the app - Universal';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Installing the app - Universal','The universal build should work on any Linux/Unix machine as well as MacOS, provided Java JRE is installed. To run DIYLC universal build:  
1. Extract the entire *diylc-[version]-universal.zip* file into a separate directory.  
2. Open the Terminal.  
3. Change directory (`cd`) into the extracted DIYLC folder.  
4. Make *run.sh* executable: `chmod +x run.sh`.  
5. Launch DIYLC by running: `./run.sh` (or `sh run.sh`).');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - User interface';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - User interface','The DIYLC user interface is divided into four main sections:  
1. **Canvas** – A WYSIWYG editor area for creating the project layout.  
2. **Component tree (toolbar)** – Lists all available components, organized in categories. You can select a component here to add to the project.  
3. **Main menu** – Provides file operations (save, load, export), clipboard actions, etc.  
4. **Status bar** – Displays context-specific information (selected component details, zoom level, memory usage, update notifications).');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Mouse operation';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Mouse operation','**Left mouse button**:  
* Single-click a component on the canvas to select it.  
* **Ctrl + Click** to add a component to an existing selection on the canvas.  
* Single-click a component type in the Toolbox/Toolbar to select it (for placement).  
* Drag a component type from the Toolbox onto the canvas to create that component.  
* Click and drag on the canvas to draw a selection rectangle (multi-select).  
* Double-click a component on the canvas to edit its properties.  

**Right mouse button**:  
* Single-click empty canvas space for the project context menu.  
* Single-click a component for the component context menu.  

**Middle mouse button**:  
* Click and drag on the canvas to pan (move the visible area).');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Configuring the application';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Configuring the application','DIYLC settings can be adjusted via the **Config** menu. Key options include:  
* **Anti-Aliasing** – Smooths graphics and text (better visuals, slightly slower). Disable if experiencing performance issues.  
* **Auto-Create Pads** – When enabled, automatically adds solder pads when a component is placed (for applicable layouts).  
* **Auto-Edit Mode** – When enabled, the component edit dialog pops up each time a new component is added (like DIYLC 1.x behavior).  
* **Continuous Creation** – When enabled, after placing a component, the tool remains active for repeated placements of that same component type (speeds up multiple inserts).  
* **Enable Cache Boost** – Improves rendering speed at the cost of higher memory usage (generally recommended unless low on memory).  
* **Export Grid** – If enabled, grid lines are included in exported images, PDFs, or prints.  
* **Extra Working Area** – Adds extra space around project boundaries for auxiliary components (excluded from final output).  
* **Hi-Quality Rendering** – Slightly improves rendered image quality (minor performance tradeoff).  
* **Highlight Connected Areas** – Switches project to a read-only debug mode (cursor becomes crosshairs). Clicking a component highlights all directly connected conductive parts (wires, traces, copper, etc.) in green. (To exit, disable this mode in the Config menu.)  
* **Language** – Changes the UI language.  
* **Mouse Wheel Zoom** – When enabled, zoom is controlled by the mouse wheel (instead of scrolling the canvas).  
* **Outline Mode** – When enabled, components are drawn as outlines only (no fill or decoration).  
* **Renumber on Paste** – When enabled, pasted components get new unique names instead of retaining duplicates.  
* **Show Rulers** – Toggles display of rulers along the canvas edges.  
* **Show Grid** – Toggles visibility of grid lines on the canvas.  
* **Snap to** – Controls snapping behavior of control points: to grid, to other components, or no snap. (Tip: Hold **Ctrl + Shift** while dragging to temporarily disable grid snap, allowing free movement.)  
* **Sticky Points** – If enabled, components snap/stick to each other when moved. (Tip: Hold **Ctrl** while dragging to temporarily toggle sticky behavior.)  
* **Theme** – Select a theme (background and grid colors). Themes are XML files in the DIYLC *themes* directory; you can create custom themes by copying and editing an existing theme file.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Adding components to the project';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Adding components to the project','To add an existing component from the library to your project:  
1. Find the component in the component tree (components are grouped in category folders, e.g., Passive, ICs, Guitar). Use the search box at the top of the component tree to filter the list if needed.  
2. Click the desired component type in the tree. (The status bar will update to reflect the active component type.)  
3. Click on the canvas where you want to place the component. Some components (e.g., solder pads, trace cuts) place with a single click; others (e.g., resistors, wires) require two clicks to set start and end points. Follow the instructions in the status bar during component placement. The component will appear semi-transparent until placement is complete.  

**Tip**: Press the tilde (~) key to repeat the last added component type at the current mouse location.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Moving components around';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Moving components around','There are multiple ways to move components on the canvas:  
**Method 1:** Click and drag the selected component(s) to the desired location. When hovering over a component, the cursor changes to a hand icon and the status bar shows the name of the component that will be moved.  

**Method 2:** Use the keyboard: select component(s), then press arrow keys while holding **Ctrl** to nudge them.  

**Notes for Methods 1 & 2:**  
– If multiple components are selected, all will move together.  
– If a moved component has other components stuck to its control points, those stuck components will move or stretch accordingly. (The status bar lists components affected by the move.) Hold **Ctrl** during the drag or nudge to temporarily unstick components, moving only the selected items.  

**Method 3:** Use the **Nudge** function from the *Transform* menu (shortcut **Ctrl + Q**). This opens a dialog where you can specify an exact X and Y offset and whether to include stuck components in the move.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Control points';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Control points','Many components have one or more **control points**, which determine their position and allow connections to other components. For components like resistors or wires, each control point can often be repositioned individually when the component is selected.  

**Key points:**  
* The status bar will list all components that will be affected when dragging a control point (especially if components are stuck together at that point).  
* Control points become draggable (turn green) when the component is selected. You can only drag a control point when it is highlighted (green).');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Editing component properties';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Editing component properties','Newly added components start with default parameters (size, color, value, name, etc.), but you can edit these properties in several ways:  
* **Double-click** a single component to open its edit dialog.  
* **Right-click** and choose "Edit Selection" (or press **Ctrl + E**) to edit one or multiple selected components at once.  

When editing **multiple components** together, only properties common to all selected components are shown. If a property has different values among selections (e.g., a resistor\'s resistance vs. a capacitor\'s capacitance), its input field will appear highlighted in yellow, indicating a mixed value. Changing a yellow-highlighted field will apply that new value to all selected components.  

**Default Values:** In the edit dialog, each property has a "Default" checkbox. Checking it sets the current value as the new default for that component type, so future added components will use that value instead of the original default.  

**Units & Expressions:** For numeric properties with units (size, resistance, capacitance, etc.), you can enter math expressions. For example, typing `3/32` will automatically convert to a decimal, and you can use parentheses and basic arithmetic operators (+, -, *, /) for complex expressions.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Assigning keyboard shortcuts to components';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Assigning keyboard shortcuts to components','DIYLC allows assigning up to 12 component types to function keys (F1–F12) as shortcuts for quick access:  
* In the component tree, click the drop-down menu (triangle icon) next to a component category, and choose **Assign Shortcut** for the desired component. Select one of the available **F** keys.  
* Components with an assigned shortcut display an indicator (e.g., "F2") next to their name in the component tree.  
* To remove a shortcut, use the Assign Shortcut menu and choose "None".  

Once set, pressing that function key will activate the component tool, allowing you to place that component without needing to click in the toolbar.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Using component variants';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Using component variants','Every component type starts with default values (size, color, etc.), but you can create **variants** with customized properties:  
* After editing a component to the desired settings, right-click it and choose **Save as Variant** from the context menu. This stores the current component properties as a reusable variant.  
* To apply a variant to an existing component, right-click the component and select the variant from the context menu. The component will update to match the variant\'s properties.  
* To add a new component using a saved variant, right-click the component type in the component tree; a context menu will list available variants – choose one to create a component with those properties.  
* To set a variant as the default for new components, right-click the component type in the tree and click the pin icon next to the variant (pin icon means "use this variant by default"). New components of that type will now use the variant\'s properties. (A trash icon in that menu can delete a variant.)');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Grouping components together';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Grouping components together','DIYLC supports grouping components, similar to grouping objects in vector drawing tools, to manipulate them as a single unit:  
* Select two or more components and press **Ctrl + G** (or use *Group Selection* from the menu) to create a group. Once grouped, moving or editing will apply to all components in the group.  
* Double-click any component in a group to open a combined edit dialog showing all properties common to the group\'s components (so you can batch-edit them).  
* While components are grouped, you cannot individually drag their control points; you must ungroup to do so.  
* To ungroup, select the group and press **Ctrl + U** (or use *Ungroup Selection* from the menu).  

**Note:** Nested grouping (grouping groups into larger groups) is not supported – grouping two existing groups will merge them into one single group.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Using building blocks';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Using building blocks','When you frequently use a cluster of components configured in a specific way, you can save them as a **Building Block** for reuse:  
1. Arrange and configure the components (e.g., a transistor with three wires forming a TO-92 package).  
2. Select all these components, right-click, and choose **Save as Building Block**.  
3. The new building block will appear under the **(Building Blocks)** category in the component tree. You can now add this cluster to any project with a single click, just like a normal component.  
4. To delete a building block, right-click it in the component tree and choose the delete option from its context menu.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Working with layers';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Working with layers','Each component in DIYLC has not only X and Y position but also a **Z-order layer** that controls stacking (what appears on top of what). Unlike free-form graphics editors, DIYLC uses fixed layers based on component types: **Chassis**, **Board**, **Trace**, **Component**, **Wiring**, **Text** (from bottom to top). For example, all traces are drawn above boards, and all components above traces, etc.  

**Bring to Front/Send to Back** commands usually operate within a component\'s designated layer. If you move a component beyond its layer bounds, DIYLC will prompt to confirm crossing into a different layer (unusual cases).  

Layer management features:  
* Temporarily **hide** an entire layer (useful to work on buried components).  
* **Lock/unlock** a layer, which keeps its components visible but not selectable/editable.  
* **Select all** components on a given layer (for quick bulk edits).');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Showing board underside';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Showing board underside','To simplify designing and assembling circuit boards, DIYLC can display a board\'s underside along with its top side:  
* Double-click a board component (or open its edit dialog through other means) and adjust the underside settings:  
  - **Underside Display**: Choose how to position the bottom view relative to the top (None, Above, Below, Left, Right of the original).  
  - **Underside Offset**: Set the distance (spacing) between the top view and the bottom view.  
  - **Underside Transparency**: If enabled, the bottom side is drawn semi-transparent to differentiate it from the top side.  

Once configured, DIYLC will render both sides of the board simultaneously, making it easier to visualize double-sided layouts.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Using the status bar';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Using the status bar','The status bar (typically at the bottom of the window) provides several info panels from left to right:  
* **Information bar** – Contextual hints and messages guiding you through actions (e.g., instructions during component placement, or details about the current tool).  
* **Selection size** – The dimensions of the bounding box around all selected components (respects current unit, e.g., inches or cm).  
* **Zoom control** – A slider or control to zoom in/out on the canvas.  
* **Announcements** – Displays any public announcements (if present).  
* **Auto-update** indicator – A light bulb icon that lights up when a new version of DIYLC is available. Click it for more info.  
* **Memory bar** – A blue bar indicating current memory usage. Hover to see details, or click it to trigger garbage collection (free up memory). It turns red if memory usage is high, signaling potential performance issues.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Using ''Highlight Connected Areas'' Feature';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Using ''Highlight Connected Areas'' Feature','Introduced in DIYLC 3.42.0, the **Highlight Connected Areas** feature helps trace connections in your layout by highlighting conductive paths:  
* Activate it from **Analyze → Highlight Connected Areas**. The cursor changes to crosshairs and the project becomes temporarily read-only (you cannot move components in this mode).  
* Click on any component or connection point. DIYLC will highlight in green all components and traces that are electrically connected to that point (assuming zero-resistance connections).  
* This is particularly useful for debugging stripboard/veroboard layouts, PCBs, breadboards, or point-to-point wiring – it visually confirms which nodes are connected.  
* To exit this mode and resume editing, uncheck **Highlight Connected Areas** in the “Analyze” menu (which will turn the feature off).');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Drawing and Analyzing Guitar Wiring Diagrams (Part 1)';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Drawing and Analyzing Guitar Wiring Diagrams (Part 1)','DIYLC can be used to create and analyze guitar wiring diagrams. Drawing the diagram uses the same tools and steps as any DIYLC project (with guitar-specific components under the **Guitar** category and common parts like jacks and switches under **Electro-Mechanical**). The powerful part is the **Guitar Wiring Analyzer** feature, which helps you understand and troubleshoot the wiring:  

*The analyzer is accessed via File → Analyze → Guitar Wiring Diagrams, and requires a fully drawn diagram.* You should have at least one guitar pickup wired through any switches/pots and ultimately connected to an output jack (the jack is required for analysis). The analyzer will perform these checks:  

- Identify all switches and consider every combination of switch positions. (For example, a 3-way toggle plus a push-pull pot yields 3 x 2 = 6 distinct configurations.) For each configuration, it will:  
  - Determine which pickup coils are active.  
  - Check if humbuckers are in full humbucking mode or split-coil.  
  - Check if active coils are wired in series or parallel.  
  - Check the phase relationship of each coil (and if multiple coils are active, whether they are in-phase or out-of-phase with each other).  
  - Determine if the active pickup combination is noise-cancelling (hum canceling).  
  - See if any potentiometers are functioning as volume or tone controls in that configuration.  
  - Check if a treble bleed network is present on any volume control.');

DELETE FROM diylc_knowledge_base WHERE section = 'User Manual - Drawing and Analyzing Guitar Wiring Diagrams (Part 2)';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'User Manual - Drawing and Analyzing Guitar Wiring Diagrams (Part 2)','Below is an example of a fully wired guitar diagram analyzed by DIYLC: it has two rail humbucker pickups, a 3-way toggle switch, and one push-pull volume pot (which splits the humbuckers when pulled) plus a treble bleed circuit on the volume.  

*(You can download this example as a DIYLC file named "travelcaster.diy".)*  

To run the analyzer, go to File → Analyze → Guitar Wiring Diagrams. The output will be divided into sections for each switch configuration. For instance, here\'s the output for the middle position of a 3-way switch with the push-pull not engaged:  

- It shows a **parallel/series connectivity tree** representing the circuit from the output jack\'s perspective for that configuration. The tree uses parentheses for hierarchy, "+" for series connections, and "||" for parallel connections. Pickup coils are labeled like **PickupName.North** or **PickupName.South**, with an arrow indicating polarity.  
- Below the tree, DIYLC lists notes deduced from the connectivity, such as which coils are active, their phase relationship, whether the combination is hum-cancelling, etc. This is where the analysis "magic" happens – it provides insights into the wiring\'s behavior.  

For a full detailed analysis of this example, DIYLC provides an HTML report (accessible via an online viewer for *travelcaster_analysis.html*).');

DELETE FROM diylc_knowledge_base WHERE section = 'Component API - General advice';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'Component API - General advice','When creating a new DIYLC component, strive for consistency with existing components in both appearance and behavior, as well as code style. This ensures a uniform user experience and maintainable codebase across all components. **Key advice:** follow the established visual design patterns and coding standards in DIYLC for any custom component.');

DELETE FROM diylc_knowledge_base WHERE section = 'Component API - Prerequisites';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'Component API - Prerequisites','Before developing DIYLC components, ensure you have:  
* Java **JDK 17** (or later) installed.  
* A Java IDE, e.g. IntelliJ IDEA
* Maven
* A working **Git** client (to fetch and manage DIYLC code).  
* Solid Java knowledge (including generics, annotations, and familiarity with **Graphics2D** for rendering). 
* Docker (only for building the deployment packages)

Note: Unlike DIYLC v2, versions 3 and higher components require actual Java coding for custom appearance and behavior. This raises the bar for non-programmers but gives greater flexibility and performance.');

DELETE FROM diylc_knowledge_base WHERE section = 'DIYLC Cloud';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'DIYLC Cloud',
'The DIYLC Cloud feature connects users to a central hub and offers several powerful capabilities:
- Upload and Share Projects: Save your DIYLC projects online and share them with the community.
- Search and Download Projects: Access a library of existing projects uploaded by other users.
- Access AI Assistant: Use the AI Assistant to get help with using the app, designing circuits, and troubleshooting projects.
The Cloud feature is open to all users but requires creating an account with a valid email address. Your email is used only for login purposes and will never be shared or used for any other reason.
The only way to create and access a DIYLC Cloud account is through the "Cloud" tab in the main menu of the DIYLC application.');

DELETE FROM diylc_knowledge_base WHERE section = 'AI Assistant';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'AI Assitant/Chatbot/ChatGPT/GPT',
'DIYLC AI Assistant is an integrated feature that allows users to interact with DIY Layout Creator (DIYLC) using natural language. It functions as a chatbot powered by the OpenAI API to generate helpful responses. The AI Assistant is available only while logged into the DIYLC Cloud with an active account and requires an internet connection.
There are two subscription tiers for using the AI Assistant:
- Free Tier: Provides limited monthly usage and access to basic AI models. This option is automatically available to all registered DIYLC Cloud users.
-Premium Tier: Offers up to 1000 requests per month using more advanced AI models. The Premium subscription is exclusively available to Patreon supporters with the "Premium AI" tier.
');

DELETE FROM diylc_knowledge_base WHERE section = 'AI Assistant - Using the AI Assistant';
INSERT INTO diylc_knowledge_base(category, section, content) VALUES ('User Manual', 'AI Assitant/Chatbot/ChatGPT/GPT - Using the AI Assistant',
'DIYLC AI Assistant can assist users with a wide variety of tasks, including:
- General Electronics Questions: Ask about electronics theory, parts, components, guitars, and related topics.
-Circuit Design Assistance: Describe the type of circuit you want to build, and the AI Assistant will suggest design ideas and possible circuit layouts.
- Circuit Troubleshooting: Get help with specific questions or troubleshoot problems with your circuit. The AI Assistant can read your project file, analyze all components and connections, and provide detailed explanations of the circuit’s behavior.
- Learning DIYLC Features: Ask questions about DIY Layout Creator (DIYLC). The AI Assistant is trained on the complete DIYLC user manual and can explain software features, tools, and workflows.');