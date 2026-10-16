# Web and Mobile

Options for running DIYLC in a browser or on a phone, given that every
component draws through AWT `Graphics2D` and the shell is Swing.

## 1. Goal

Decide how DIYLC could reach the web and mobile without giving up the
component library, the `.diy` file format, or rendering parity with the
desktop build. This document records where the codebase stands, what each
route would cost, and the order in which to explore them.

## 2. Where the codebase stands

| | core | library | swing |
| --- | --- | --- | --- |
| Lines of Java | ~28k | ~81k | ~24k |
| Files importing `java.awt` | 63 / 241 | 278 / 317 | 151 / 168 |
| Files importing `javax.swing` | 7 | 3 | 148 |

### 2.1 What makes a port easier

- **Editing logic does not depend on Swing.** `Presenter`, through
  `IPlugInPort`, takes model-level events: `mouseClicked(Point, button, ctrl,
  shift, alt, clickCount)`, `mouseMoved`, `dragStarted`, `dragActionChanged`,
  `dragEnded`, `keyPressed`. Selection, dragging, snapping, undo and netlists
  live in core, so a non-Swing front end could drive them.
- **All drawing goes through one `Graphics2D`.** `G2DWrapper extends
  Graphics2D` (`diylc-core/.../presenter/G2DWrapper.java`) already sits between
  the components and the real renderer, and `DrawingManager.drawProject`
  accepts any `Graphics2D`. A canvas, SVG or Android-Canvas `Graphics2D` plugs
  in at the same point.
- **Rendering already works without a window.** `RegressionTestRunner` exports
  PNGs and netlists for ~700 projects with no UI.
- **Swing has leaked into core in only a few places** (see 4.1).

### 2.2 What makes a port hard

- **`java.awt.geom` is everywhere.** `Point2D` is imported ~250 times,
  `AffineTransform` ~160, `Shape` ~175. `Area` boolean operations appear about
  745 times in core and library, and continuity, the netlist and hit-testing
  all depend on them. No JS/WASM Java runtime except CheerpJ ships these
  classes.
- **Text is measured through AWT.** 38 `new Font(...)` calls in the library
  and 38 files using `FontMetrics`, `GlyphVector` or `FontRenderContext`.
- **A custom AWT paint:** `org.diylc.awt.ShadedPaint` / `ShadedPaintContext`.
- **Heavy runtime reflection.** XStream reads and writes the file format and
  Reflections discovers components at startup (`ComponentProcessor`). Both
  work badly or not at all under ahead-of-time compilers such as TeaVM or
  GraalVM's web image.
- **Mobile has no AWT.** Android has no `java.awt`, and Gluon's JavaFX-on-mobile
  stack does not support Swing or AWT drawing. A mobile build needs either a
  web build or an AWT replacement layer.
- **Build targets Java 21.** Only a handful of Java-21-only calls were found,
  so retargeting to 17 for a runtime that needs it looks feasible.

## 3. Options

| # | Route | Effort | Result | Mobile |
| --- | --- | --- | --- | --- |
| A | Existing JAR in the browser via CheerpJ | days | Full desktop app in a tab | Loads, unusable |
| B | Stream the Swing app via Webswing | days | Full app, no download | Poor |
| C | Headless Java service + thin web client | weeks | Viewer / exporter | Good for viewing |
| D | Engine compiled to JS/WASM + new web UI | months | Touch-capable editor | Good (PWA / Capacitor) |
| E | Rewrite in TypeScript or Kotlin Multiplatform | very large | Anything | Good |

### A. CheerpJ

A JVM compiled to WebAssembly that runs the current JAR client-side. Swing,
AWT, XStream and reflection all work. Costs: large download, slow first load,
a desktop UI on touch screens, and possibly a commercial license.

Unverified: which Java versions the current CheerpJ release supports (may
require a `--release 17` build) and its license terms for an open-source
project.

### B. Webswing

Runs the Swing app in a server-side JVM and paints it into a browser canvas.
Costs: one JVM per user, ongoing hosting, drag latency, commercial license.

### C. Headless service with thin client

Core and library run as a service that renders PNG or SVG and accepts
`IPlugInPort` calls. Strong as a viewer and exporter: open a `.diy`, get
images, BOM and netlist. Fits beside the existing cloud and sharing features,
though `diylc-server-api` is PHP, so this would be a separate Java service.
Too much latency for smooth drag editing.

### D. Engine to JS/WASM with a new UI

Compile core and library with TeaVM (or GraalVM's web image) and supply:

1. **A replacement `java.awt.geom`** — port OpenJDK's pure-Java geometry
   classes, including `Area`. Check license compatibility of OpenJDK's
   GPLv2 + Classpath Exception sources with DIYLC's GPLv3 before copying.
2. **A canvas-backed `Graphics2D`** — strokes, transforms, composites, clips,
   gradients; text measured through canvas `measureText`. `ShadedPaint`
   reimplemented as canvas gradients.
3. **A build-time component list** replacing the Reflections scan (an
   annotation processor emitting the list of `@ComponentDescriptor` classes).
4. **A `.diy` reader/writer without XStream** — hand-written, or a small
   server-side conversion step. The serialized shape is a public contract, so
   validate against `diylc-regression-data/input`.
5. **A new TypeScript UI** replacing the ~24k lines of Swing, driving
   `IPlugInPort`.

Shipped as a PWA, or wrapped with Capacitor for the app stores, this covers
iOS and Android. The same geometry layer plus an Android-Canvas `Graphics2D`
would also allow a native Android build. Expect text-metric differences to
show up as regression-suite diffs.

### E. Rewrite

About 130k lines and a file format to keep compatible. Not worth it compared
with D, which keeps the library intact.

## 4. Groundwork that pays off on every route

### 4.1 Remove Swing from `diylc-core`

| File | Swing / desktop use |
| --- | --- |
| `presenter/Presenter.java` | `JOptionPane` error dialog |
| `presenter/ComponentProcessor.java` | `Icon`, `ImageIcon` |
| `common/ComponentType.java` | `Icon` |
| `core/images/IconLoader.java` | `javax.swing.*` |
| `plugins/cloud/service/CloudService.java` | Swing import |
| `plugins/compare/util/CompareResultsHtmlReport.java` | Swing import |
| `plugins/chatbot/service/JsonToHtmlConverter.java` | Swing import |
| `utils/Constants.java` | `Toolkit` screen resolution |

Move dialogs behind an interface implemented in `diylc-swing`, and represent
icons as image data rather than `Icon`. The three Swing imports in
`diylc-library` (`MultimeterAnalyzer`, `GuitarDiagramAnalyzer`,
`components/misc/Image`) need the same review.

### 4.2 Build-time component discovery

Replace the runtime Reflections scan with a generated list. Speeds up desktop
startup and removes a blocker for routes C and D. Discovery must still find
every `@ComponentDescriptor` class with no registry to maintain by hand.

### 4.3 Measure the AWT surface precisely

Before committing to D, list every AWT class and method actually called from
core and library (e.g. with `jdeps` or a bytecode scan). That inventory sizes
the replacement layer and shows which `Graphics2D` operations the canvas
backend must support.

## 5. Next steps

1. **CheerpJ proof of concept (days).** Run the current JAR in a browser,
   confirm the Java version and license questions in A. Tells you whether
   "DIYLC in a browser" is wanted with almost no code change.
2. **Remove Swing from core (4.1).**
3. **Build-time component discovery (4.2).**
4. **Headless viewer / export service (C).** Mobile viewing and sharing
   cheaply, building on the cloud features.
5. **Only if a real web or mobile editor is the goal:** do 4.3, then start D
   with the geometry layer and the canvas `Graphics2D`. Use the regression
   suite's ~700 reference PNGs to check that browser and desktop render the
   same.

## 6. Open questions

- Is the target a full editor, or is viewing and sharing on mobile enough?
  This alone decides between C and D.
- Should the web build be offline-capable (PWA), or may it require a server?
- Is a commercial runtime (CheerpJ, Webswing) acceptable for a GPLv3 project?
