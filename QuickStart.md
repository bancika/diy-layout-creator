# DIY Layout Creator (DIYLC) – Quick Start Guide

Welcome to **DIY Layout Creator (DIYLC)** — the open-source layout tool designed specifically for DIY electronics enthusiasts, pedal builders, tube amp makers, and guitar modders.

---

## 1. System Requirements & Installation Quick-Check

DIYLC runs cross-platform on Windows, macOS, and Linux. The current release is built on **Java 21 LTS**.

### Installation by Operating System
* **Windows (64-bit)**:
  * Download `diylc-6.x.x-win64.exe` (Installer) or `diylc-6.x.x-win64.zip`.
  * Includes bundled Java JRE. If Windows Defender flags the file, click **More Info** → **Run Anyway**.
* **macOS (Intel & Apple Silicon)**:
  * Download `diylc-6.x.x-osx-silicon.zip` (M-series) or `diylc-6.x.x-osx-intel.zip`.
  * Extract and drag `DIYLC.app` to your **Applications** folder.
  * If macOS blocks launch, navigate to **System Settings > Privacy & Security** and select **Open Anyway**.
* **Linux (64-bit)**:
  * Download `diylc-6.x.x-linux.zip`, extract, and double-click the executable `AppImage`.
* **Universal Package (All Platforms)**:
  * Requires **Java 21+** installed (`java -version`).
  * Run via `run.bat` (Windows) or `run.sh` (macOS/Linux).

---

## 2. Interface Overview

1. **Component Library (Left Drawer)**: Browse components grouped by category (*Boards, Passive, Semiconductors, Guitar, Tubes, Schematics, Shapes*).
2. **Canvas (Center Area)**: The grid-based work region with adjustable rulers (mm/inches) and zoom controls.
3. **Properties & Layer Tree (Right Panel)**: Displays properties of selected components and Z-order layer management.
4. **AI Assistant (Right Drawer)**: In-app chat for troubleshooting, design tips, and circuit questions.

---

## 3. Essential Key Commands & Controls

* **Q:** Jump to search the component tree.
* **~ (Tilde):** Repeat and place another instance of the previously created component.
* **Ctrl / Cmd + D:** Duplicate selected component(s).
* **Ctrl / Cmd + E:** Edit selected component properties (value, package, color, etc.).
* **Alt / Option + Left / Right Arrow:** Rotate selected component(s) counter-clockwise or clockwise.
* **Arrow Keys:** Nudge selected component by one grid increment.
* **Shift + Arrow Keys:** Nudge selected component by one pixel for precision alignment.
* **Shift (while dragging):** Lock movement along a single axis (horizontal or vertical).
* **Ctrl (while dragging):** Temporarily toggle Sticky Points to prevent unwanted snapping.
* **Ctrl / Cmd + G / Ctrl / Cmd + U:** Group or ungroup selected components.
* **Ctrl / Cmd + 1 – 6:** Toggle visibility of layers (Chassis, Board, Trace, Component, Wiring, Text).
* **Ctrl / Cmd + F:** Find a component by name, select it, and pan to it on canvas.

---

## 4. Key Workflows & Features

### Creating a Layout
1. **Place a Board**: Drag a *Veroboard*, *Perfboard*, or *PCB* shape from the **Boards** category onto the canvas.
2. **Add Components**: Drag resistors, capacitors, transistors, or ICs onto board holes/pads.
3. **Route Connections & Wires**: Use the **Connectivity** menu to add flexible wires, jumpers, trace cuts, or ground busses.

### Analysis & Debugging Tools (`Analyze` Menu)
* **Guitar Wiring Analyzer**: Verifies switch matrix combinations, coil splits, phase relationships, and hum-canceling positions.
* **Virtual Multimeter**: Graphically traces electrical paths between two points as series/parallel sub-circuits.
* **Compare With...**: Compares two layouts or schematics to check if they are electronically equivalent.
* **Highlight Connected Areas**: Click any point/node to highlight all connected paths on the board.

---

## 5. Exporting & Sharing Your Work

* **Image / PDF Export**: Go to **File > Export Image/PDF** for high-resolution documentation.
* **Bill of Materials (BOM)**: Go to **File > Export BOM** to generate a complete list of required parts and quantities.
* **Gerber PCB Fabrication**: Go to **File > Export Gerber** to generate manufacturing files for PCB fabricators.
* **DIYLC Cloud Integration**: Use the **Cloud** menu to search, download, and publish community layouts.

---

## 6. Keeping DIYLC Updated

* Look for the glowing **lightbulb icon** in the bottom-right corner of the status bar when an update is available.
* Official download channels: [GitHub Releases](https://github.com/bancika/diy-layout-creator/releases/latest) and [Patreon](https://www.patreon.com/c/u5213116).
