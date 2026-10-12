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

* **Pan Canvas**: Spacebar + Drag or Middle Mouse Button Drag
* **Zoom**: Mouse Scroll Wheel or `Ctrl` / `Cmd` + Scroll
* **Edit Component Properties**: Double-Click component or press `Ctrl` + `E` (`Cmd` + `E`)
* **Nudge Selected Component**: Arrow Keys (`Shift` + Arrow Keys for larger jumps)
* **Rotate Component**: `R` or `Ctrl` + `R`
* **Change Z-Order / Layer**: `Page Up` / `Page Down`
* **Delete Component**: `Delete` or `Backspace`

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
