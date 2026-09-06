# Maker Board Roadmap

Closing the gap between `org.diylc.components.micro` and what people building with
microcontroller boards today actually reach for.

Status: proposed
Scope: `diylc-library`, package `org.diylc.components.micro`
Supersedes: `tier-1-modern-boards.md`

## 1. Goal

The `micro` package models 10 board classes across roughly 16 selectable variants. The
scaffolding in `AbstractMakerBoard` — USB connector types, PCB meander antenna, castellated
and through-hole pads, terminal blocks, FPC connectors, mounting holes, vendor logos — is
complete enough that **adding a board is mostly pin arrays and a draw branch**. The expensive
part is already paid for.

This document inventories what is modelled, lists what a modern maker cannot draw today, and
sorts the additions into three tiers by how often their absence would actually block someone.

## 2. Decisions that govern this roadmap

| # | Decision | Choice | Rationale |
|---|---|---|---|
| D1 | How coverage grows | **Named boards, drawn faithfully.** No generic/parametric "configurable dev board" component. | A parametric board would cover more of the long tail per hour, but the palette entry a maker searches for is the product name, and accurate silk/connectors/logos are the point of these components. One was built and reverted; the question is settled. |
| D2 | New class vs. new `Version` constant | **Form factor decides.** Same outline and pin positions → append a `Version` enum constant. Different footprint → new class. | Already the established paradigm (`ArduinoUno`, `RaspberryPiPico`, `RaspberryPiZero`, `ESP32DevKit`). Keeps the palette short without forcing unrelated geometry into one draw method. |
| D3 | Where pin data lives | **Hardcoded `String[]` arrays in the component class.** | Matches every existing board. An external pin-definition resource and per-pin type metadata (`POWER_5V`, `GND`, `ADC`, `I2C_SDA`…) were both considered and declined; no pin-data infrastructure is introduced here. |
| D4 | Package boundary | **`micro` only.** | Adjacent gaps (Qwiic/STEMMA QT/Grove connectors, shields and HATs, stackable headers) are real but belong to `connectivity` / `electromechanical` and are out of scope for this document. |
| D5 | Backward compatibility | Append-only enums, unchanged defaults, unchanged control-point counts for existing variants. | `.diy` files are a public contract; see §4.3. No file-format migration is needed anywhere in this roadmap. |

### Explicitly out of scope

- A configurable catch-all board component (D1).
- Pin-type metadata and any electrical rule check built on it (D3).
- Shields, HATs, stacking, and the Qwiic/STEMMA QT/Grove connector family (D4).
- Bumping the project version, packaging, and the deployment build.

## 3. Current coverage

| Class | Variants | Notes |
|---|---|---|
| `ArduinoUno` | R3, R4 WiFi, **R4 Minima**, **Leonardo** | |
| `ArduinoMega` | — | |
| `ArduinoNano` | **classic, Every, 33 IoT, 33 BLE, 33 BLE Sense, RP2040 Connect, Nano ESP32** | |
| `ESP32DevKit` | V1 30-pin, DevKitC V4 38-pin, S3 DevKitC-1 44-pin, **C3 DevKitM-1 30-pin**, **C6 DevKitC-1 32-pin** | per-version pin arrays, geometry and draw branches |
| `ESP8266NodeMCU` | — | |
| `WemosD1Mini` | — | hardcodes `SILK_PIN_NAMES_LEFT/RIGHT` instead of `drawPinLabels` |
| `RaspberryPi` | 3B+, 4B, 5 | |
| `RaspberryPiZero` | Zero, Zero W, **Zero 2 W** | |
| `RaspberryPiPico` | Pico, Pico W, **Pico 2**, **Pico 2 W** | castellated pad rendering |
| `Teensy` | 4.0, 4.1, **Teensy 3.2** | |

Variants in bold landed with this roadmap; everything in §5.1 and §5.2 is now **done**. All ten are
`category = "Controllers"`, `bomPolicy = SHOW_ONLY_TYPE_NAME`, `enableCache = true`, and extend
`AbstractMakerBoard`.

## 4. Shared mechanics

Applies to every item in every tier.

### 4.1 Component discovery

Components are found by classpath annotation scan in `ComponentProcessor.getComponentTypes()`
(Reflections, `TypesAnnotated`, `includePackage("org.diylc.components")`). **Any class carrying
`@ComponentDescriptor` is picked up automatically** — there is no manifest or registry to edit.
Adding enum constants to an existing class needs nothing. Adding a *new* class needs nothing for
discovery, but should be added to the hand-maintained list in
`org.diylc.components.maker.MakerComponentsTest#allMakerComponentClasses`.

### 4.2 Version property pattern

```java
public enum XxxVersion { A("Label A"), B("Label B"); ... toString() returns label; }

protected XxxVersion version = XxxVersion.A;          // default initializer

@EditableProperty(name = "Version")
public XxxVersion getVersion() {
  if (version == null) version = XxxVersion.A;        // null-guard for old files
  return version;
}
public void setVersion(XxxVersion version) {
  this.version = version;
  updateControlPoints();                              // only if geometry changes
  invalidateCache();                                  // required: enableCache = true
}
```

New variants must be **appended** to the enum (XStream serializes enum constants by name;
appending is backward compatible, reordering or removing is not). Keep the default constant and
the null-guard so existing `.diy` files deserialize unchanged.

### 4.3 Serialization safety checklist (per changed class)

- `serialVersionUID` stays `1L`.
- The default `version` field value is unchanged, so old files keep their look.
- **Control-point count must not change for existing variants.** If a new variant has a
  different pin count, `updateControlPoints()` must size the array from the per-version pin list
  (as `ESP32DevKit` already does), and any code assuming a fixed `PIN_NAMES.length` must switch
  on version.
- `drawIcon()` reflects only the default variant — leave it alone unless the default changes.

### 4.4 Test pattern

Each board has a `*Test.java` under `diylc-library/src/test/java/org/diylc/components/micro/`:

- `testControlPointCountAndNames()` — count, every name non-empty, spot checks
- `testDimensionsAndPinGeometry()` — `getBodyShape().getBounds2D()` against expected mm, and
  control-point offsets from board edges
- `testVersionProperty()` — default, `toString()` labels, round-trip through the setter
- a `*Drawing` smoke test — `draw()` in NORMAL / SELECTED / outline, headers on and off, against
  a `BufferedImage` with a no-op `IDrawingObserver`

Extend these in place; add one geometry test and one draw smoke test per new variant.

### 4.5 Sourcing dimensions and pin names

Every dimension constant and every pin array below must be cross-checked against vendor
documentation before it is committed. Where this document states a size or a pin count without
naming a source, it is an estimate to scope the work, not a datasheet citation.

What actually works as a source:

- **Arduino** — `docs.arduino.cc/resources/pinouts/<SKU>-full-pinout.pdf` is canonical for PWM
  tildes and silkscreen names; `docs.arduino.cc/resources/datasheets/<SKU>-datasheet.pdf` carries
  the numbered header tables. The HTML product and cheat-sheet pages render client-side and yield
  nothing to a fetch.
- **Espressif** — the `esp-dev-kits` user guides carry complete J1/J3 header tables as HTML.
- **PJRC** — pinout cards, not yet used for the Teensy 3.2 bottom cluster (see §5.1 item 4).

Note that these PDFs need downloading and text extraction; a plain fetch returns binary. Do not
assume variants of one board share a pinout — across the Nano family the PWM sets, the bus
annotations and the power pins all differ per variant.

### 4.6 Build and verify

```bash
cd diylc
mvn -pl diylc-library -am test
```

Rendering-regression fixtures live in `../diylc-regression-data/` but are not wired into the
library test run, so new variants do not break it. When those baselines are next regenerated,
add sample placements for the new variants.

---

## 5. Tier 1

Boards a maker is very likely to reach for and cannot draw today.

### 5.1 Enum appends to existing classes — **done**

The cheapest fidelity wins in the whole roadmap — the geometry is already correct, so each is a
pin array, a silk label and a few draw branches.

| # | Board | Host class | Change |
|---|---|---|---|
| 1 | Raspberry Pi Pico 2 / Pico 2 W (RP2350) | `RaspberryPiPico` | 2 constants |
| 2 | Raspberry Pi Zero 2 W | `RaspberryPiZero` | 1 constant |
| 3 | Arduino Uno R4 Minima | `ArduinoUno` | 1 constant |
| 4 | Teensy 3.2 | `Teensy` | 1 constant + pin array |
| 5 | Arduino Leonardo | `ArduinoUno` | 1 constant + pin array |

**Item 1 — Pico 2 / Pico 2 W.** The Pico 2 is pin-, size- and connector-identical to the Pico
(21 × 51 mm, 2×20 castellated plus through-hole, 3-pad SWD, micro-USB, BOOTSEL). The visible
differences: the SoC silk reads `RP2350` instead of `RP2040`, and Pico 2 W carries the same
Infineon CYW43439 radio in the same position as Pico W, so its 3-pad SWD block sits at the
Pico W offset.

1. `PicoVersion`: append `PICO_2("Pi Pico 2")`, `PICO_2_W("Pi Pico 2 W")`.
2. Add helpers so the existing checks stay readable:
   ```java
   private boolean isW()  { return version == PicoVersion.PICO_W || version == PicoVersion.PICO_2_W; }
   private boolean isV2() { return version == PicoVersion.PICO_2 || version == PicoVersion.PICO_2_W; }
   ```
3. In `updateControlPoints()`, `getBodyShape()`, `drawCastellatedPads()` and `draw()`, replace
   `getVersion() == PicoVersion.PICO_W` with `isW()`.
4. The chip label becomes `isV2() ? "RP2350" : "RP2040"`.
5. `PIN_NAMES` is unchanged and shared across all four variants.
6. `drawIcon()` unchanged (default is still `PICO`).

Tests: extend `testVersionProperty()`; add `testPico2GeometryMatchesPico()` asserting identical
body bounds and control-point count for `PICO`/`PICO_2` and `PICO_W`/`PICO_2_W`; draw smoke test
for `PICO_2_W`. **~2–3 h, minimal risk.**

**Item 2 — Pi Zero 2 W.** Identical 65 × 30 mm outline, identical 40-pin GPIO position, identical
connector layout (mini-HDMI, 2× micro-USB, CSI FPC on the right edge, 4 mounting holes). The SoC
is the RP3A0 package-on-package with integrated radio — physically a ~12 mm square where the
drawing already places a 12 mm chip. Silk and label only.

1. `ZeroVersion`: append `PI_ZERO_2_W("Pi Zero 2 W")`.
2. No geometry change; pin arrays untouched.
3. `draw()` already prints `"Raspberry " + getVersion()` — verify the longer string still fits
   inside `hdrBoxW`; if not, drop to `SILK_FONT` or print `getVersion().toString()` alone for
   this case.
4. Optional cosmetic touch: draw the SoC as a plain dark PoP square with no Broadcom split.

Tests: extend `testVersionProperty()`; geometry-equivalence assertion against `PI_ZERO_W`; draw
smoke test. **~1–2 h, minimal risk — only watch the label width.**

**Item 3 — Uno R4 Minima.** `ArduinoUno` models R3 and R4 WiFi but not the Minima, which shares
the R4 WiFi board outline and header positions and differs in silk (no ESP32-S3 radio module, no
LED matrix) and in the absence of the WiFi can. Append `R4_MINIMA("UNO R4 Minima")` and branch the
draw code that places the radio module and matrix.

**Item 4 — Teensy 3.2.** Same 1.4 × 0.7" outline and 2×14 header positions as Teensy 4.0, so this
is a pin array plus silk. Implemented with 34 control points — the two 1×14 edge rows, the
five-hole bottom cluster and the severable VUSB pad — and an `isCompact()` helper that groups the
3.2 with the 4.0 against the longer 4.1. **The bottom-cluster labels (`VBAT`, `3.3V`, `GND`,
`Program`, `A14/DAC`) are the one part not taken from a primary source and are worth checking
against the PJRC pinout card.**

**Item 5 — Arduino Leonardo.** Shares the Uno R3 shield footprint and header positions exactly.
Differences are the ATmega32U4 silk, micro-USB in place of USB-B (`drawMicroUsb` instead of
`drawUsbB`), and no separate USB-serial chip. Pin names are the R3 map with the native-USB
annotations.

### 5.2 Version retrofit and new pin arrays — **done**

**Item 6 — Modern Arduino Nano family.** `ArduinoNano` has no version property; Tier 1 introduces
one and adds the current lineup. All share the classic 0.73 × 1.70" outline and the 2×15 pin
positions, but **only the classic Nano populates the 2×3 ICSP block** — on every later board that
end of the PCB carries the radio module or USB bridge instead. So the classic keeps its 36 control
points and the modern variants have 30. Serialization is safe because the classic is the default
and is untouched; the shorter counts belong to enum constants no shipped file references.

```java
public enum NanoVersion {
  CLASSIC("Nano (ATmega328)"),           // default; current behavior
  EVERY("Nano Every"),
  NANO_33_IOT("Nano 33 IoT"),
  NANO_33_BLE("Nano 33 BLE"),
  NANO_33_BLE_SENSE("Nano 33 BLE Sense"),
  NANO_RP2040_CONNECT("Nano RP2040 Connect"),
  NANO_ESP32("Nano ESP32");
}
```

What varies by version:

| Aspect | CLASSIC | EVERY | 33 IoT | 33 BLE / Sense | RP2040 Connect | Nano ESP32 |
|---|---|---|---|---|---|---|
| Separate MCU package | `m328P` (rot. 45°) | `m4809` | `SAMD21` | — (in module) | `RP2040` | — (in module) |
| Part at the far end | ICSP 2×3 header | `SAMD11` bridge | `NINA-W102` can | `NINA-B306` can | `NINA-W102` can | `NORA-W106` can |
| USB | Mini-B | Micro-B | Micro-B | Micro-B | Micro-B | **USB-C** |
| Pin-name array | classic | modern | modern | modern | modern | modern + `GPIOxx` |
| Control points | 36 | 30 | 30 | 30 | 30 | 30 |

The enum carries the silk label, the MCU marking and the end-module marking, so `draw()` branches
only on USB type, whether the MCU package is drawn separately (it is not when the die sits inside
the module) and whether the end part is a shielded can or a bare QFN.

`drawMiniUsb`, `drawMicroUsb`, `drawUsbC`, `drawMetalConnector` and `drawChip` all already exist
in `AbstractMakerBoard`.

Pin arrays: `PIN_NAMES` keeps the classic 36 entries. Each later board gets **its own 30-entry
array**, transcribed from its official Arduino pinout diagram (`docs.arduino.cc/resources/pinouts/
<SKU>-full-pinout.pdf`) rather than assumed from the classic. A single shared "modern" array was
wrong: the PWM sets and the power pins genuinely differ.

| Version | PWM pins | Position 17 | Position 18 | Position 27 |
|---|---|---|---|---|
| CLASSIC | D3 D5 D6 D9 D10 D11 | RST2 | 5V | AREF |
| EVERY | D3 D5 D6 D9 D10 | RST2 | 5V | AREF |
| NANO_33_IOT | D2 D3 D5 D6 D9 D10 | RST2 | 5V | AREF |
| NANO_33_BLE / _SENSE | D2–D13 (all) | RST2 | 5V | AREF |
| NANO_RP2040_CONNECT | D2–D13 (all) | **REC** (BOOTSEL) | 5V | AREF |
| NANO_ESP32 | D0–D13 and A0–A7 | **B1** | **VUSB** | **B0** |

`PIN_NAMES_ESP32` additionally dual-labels with the Espressif GPIO number, e.g. `D2 (~, GPIO5)`;
those numbers are taken from the ABX00083 pinout diagram.

`draw()` gains version switches for the MCU chip label (and whether it is drawn rotated 45° like
the classic TQFP or axis-aligned like a QFN), the end module, and the USB connector helper. Board
outline and mounting holes are unchanged. Because the pin count is now version-dependent,
`setVersion()` must call `updateControlPoints()` — it previously only invalidated the cache, which
was harmless while the geometry was fixed.

The `5V` / `3V3` logic-level silk from the original plan was dropped: the Nano draws no pin labels
at all, so a lone voltage annotation would be invented silkscreen rather than what is on the board.

The modern Nanos also have **castellated edges** rather than plain through-holes: the outline is
notched at every pin and the pads run out to the board edge, exactly as on the Pico. That rendering
now lives in `AbstractMakerBoard` as `drawCastellatedPads` (one column of pads toward a given edge)
and `subtractCastellationNotches` (the matching bites out of the outline), with
`getCastellatedPadSize` / `HoleSize` / `NotchSize` / `PadColor` as the per-board overrides.
`RaspberryPiPico` was refactored onto the same pair — its own method survives only as the wrapper
that places the two columns and draws the SWD pads, so its rendering is unchanged. Any future
castellated board (the ESP32 SuperMini and XIAO in §5.3 / §6) should reuse these rather than copy
the geometry a third time.

Tests: `testVersionProperty()` (default `CLASSIC`, labels, round-trip);
`testPinNamesPerVersion()` over all seven versions asserting no empty names, the `A4 (SDA)`
annotation on a modern version and the GPIO dual-label on `NANO_ESP32`;
`testGeometryIsVersionIndependent()` and `testModernVersionsHaveNoIcspBlock()` pinning the 36/30
split and the outline; draw smoke test looping all versions × headers on/off. **The value is
entirely in getting the labels and per-board silk right; the rendering scaffold is a light touch.**

**Item 7 — ESP32-C3 DevKitM-1 and ESP32-C6 DevKitC-1.** The heaviest enum-style item, because
`ESP32DevKit` gives every variant its own pin array and its own branch in `getRelativeOffsets()`,
`getBodyShape()` and `draw()`. The C3 is a genuinely different board (single RISC-V core,
ESP32-C3-MINI-1 module, one addressable RGB LED), not a re-label.

Both pin arrays were transcribed from the Espressif `esp-dev-kits` user guides (the J1/J3 header
tables), not from memory — see D3's note in §4.5.

- **ESP32-C3-DevKitM-1**: two 1×15 headers (**30 pins**) on 0.1" pitch, row spacing 22.86 mm
  (0.9"), ESP32-C3-MINI-1 module with PCB antenna at the top, a **single micro-USB** port at the
  bottom, `BOOT` + `RST` buttons.
- **ESP32-C6-DevKitC-1**: two 1×16 headers (**32 pins**, the last position on each is `NC`) on the
  same 0.9" body, ESP32-C6-WROOM-1 module, **two USB-C** connectors (USB-to-UART and native).

Espressif publishes these outlines only as dimension drawings, so `BOARD_WIDTH_RISCV` (25.4 mm) and
the row spacing are taken from the shared DevKitC form factor and the body length is *derived* as
top margin + pin field + bottom margin using the same margins as the S3 DevKitC-1. That derivation
is called out in a comment on the constants and is the thing to check if a board ever measures
wrong.

1. `DevKitVersion`: append `ESP32_C3_DevKitM_1("ESP32-C3 DevKitM-1 (32-Pin)")` and
   `ESP32_C6_DevKitC_1("ESP32-C6 DevKitC-1 (32-Pin)")`.
2. Add `PIN_NAMES_C3` and `PIN_NAMES_C6`.
3. Add `BOARD_WIDTH_C3` / `BOARD_LENGTH_C3` (and `_C6`) plus module and antenna sizes; the C3
   module is smaller than WROOM-32, so `SHIELD_*_S3` is the better starting point.
4. `getControlPointNodeName(int)`: two new branches.
5. `getRelativeOffsets()`: two branches. Both are the simple two-straight-columns layout — copy
   the 30-pin branch and change the loop bounds and row-spacing constant.
6. `getBodyShape()`: rounded rectangle plus PCB-antenna notch at the top, as the 30-pin and S3
   cases already do.
7. `draw()`: module shield labelled `ESP32-C3-MINI-1` / `ESP32-C6-WROOM-1` via
   `drawMetalConnector`, PCB antenna via `drawPcbAntenna`, `BOOT`/`RST` via `drawButton`, USB via
   `drawMicroUsb` ×2 (C3) or `drawUsbC` ×2 (C6), one RGB LED dot.
8. `drawIcon()` unchanged (default stays 30-pin V1).

Tests mirror the existing per-version blocks. **~1 day, moderate risk** — geometry constants need
datasheet cross-checking, but the change is contained entirely within `ESP32DevKit`.

### 5.3 New classes

Genuinely different footprints, so per D2 each gets its own class.

| # | Board | Shape | Why Tier 1 |
|---|---|---|---|
| 8 | **ESP32-CAM** | ~40 × 27 mm, 2×8 headers, OV2640 camera FPC, microSD slot | After the DevKit, the most-used ESP32 board in hobby projects. Nothing close exists. |
| 9 | **ESP32 SuperMini** | ~22.5 × 18 mm, 2×8 castellated, PCB antenna. One class, `Version` {C3, S3} | Now the default cheap ESP32 board. Model on `RaspberryPiPico`'s castellated pad rendering. |
| 10 | **Arduino Pro Micro** | ~33 × 18 mm, 2×12, ATmega32U4, micro-USB | The HID and custom-keyboard staple; no equivalent in the set. |
| 11 | **Arduino Pro Mini** | ~33 × 18 mm, 2×12 + 6-pin FTDI header. `Version` {5V 16 MHz, 3.3V 8 MHz} | Still ubiquitous on breadboards. Identical outline between the two variants; differs in silk and regulator. |
| 12 | **STM32 Pill** | ~53 × 22 mm, 2×20 on 0.1". `Version` {Blue Pill F103, Black Pill F401, Black Pill F411} | Cheapest ARM path, still everywhere. Shared outline makes this one class. |

Item 9 replaces the "Tier 2, note only" treatment the SuperMini had in the superseded document:
it is a different footprint from the DevKit (so a separate class, per D2) and is common enough now
to belong in Tier 1.

---

## 6. Tier 2

Whole ecosystems worth a deliberate push. Each is one class plus a `Version` enum where the vendor
standardised a footprint, and separate classes where they did not.

| Board / family | Structure | Notes |
|---|---|---|
| **Seeed XIAO** | one class, `Version` {ESP32-C3, ESP32-S3, RP2040, nRF52840, SAMD21} | Seeed standardised a ~21 × 17.5 mm castellated footprint across the family — a textbook single-class case under D2. |
| **Adafruit Feather** | one class, `Version` {M0 Express, M4 Express, ESP32-S3, RP2040} | Standardised 0.9 × 2.0" outline. |
| **Adafruit QT Py / ItsyBitsy** | one class each | Different footprints from Feather and from each other. |
| **BBC micro:bit** | one class, `Version` {v1, v2} | Enormous in education. The gold edge connector is genuinely new drawing work, which is why it is not Tier 1. |
| **Arduino MKR** | one class, `Version` {WiFi 1010, Zero, NB 1500} | Shared ~25 × 61.5 mm outline. |
| **Heltec WiFi LoRa 32** | one class, `Version` {V2, V3} | LoRa + OLED. The Meshtastic community is large and entirely unserved today. |
| **LilyGO T-Display-S3** | one class | ESP32-S3 with an integrated TFT. |
| **TTGO LoRa32** | one class | Distinct footprint from the Heltec boards. |
| **M5Stack** | `M5StackCore` (Core2), `M5StickC` (Plus / Plus2), `M5Atom` (Atom / StampS3) | Four distinct form factors; group by footprint, not by brand. |
| **Waveshare RP2040-Zero** | one class | ~18 × 23.5 mm castellated. |
| **Pimoroni Tiny 2040** | one class | Sibling footprint to RP2040-Zero but not identical. |
| **Pi-compatible SBCs** | `OrangePiZero`, `RadxaZero`, `LePotato` | Same 40-pin header, different outlines, so named classes rather than one shared one (D1, D2). |

---

## 7. Tier 3

Long tail and specialty. Add on request or when a specific project needs one.

- **Pro / industrial Arduino** — Giga R1, Portenta H7, Nicla Sense, Due, Zero, Micro.
- **Raspberry Pi variants** — CM4 / CM5 plus IO board, Pi 400 / 500.
- **AI accelerators** — Jetson Orin Nano, Google Coral Dev Board.
- **Other SBCs and RISC-V** — BeagleBone Black, Milk-V Duo, ESP32-P4, CH32V003.
- **Legacy** — Teensy LC, Teensy 2.0++, Digispark ATtiny85, Wemos D1 R32.
- **Bare modules for custom-PCB work** — ESP-01, ESP-12F, ESP32-WROOM-32, nRF52840 module. These
  are footprints rather than dev boards; if they are ever added, check whether they belong in
  `micro` at all or alongside the SMD packages.

---

## 8. Cross-cutting fixes

Small, independent, and worth folding into whichever Tier 1 item is done first.

1. **`Teensy` z-order.** `Teensy.java:54` declares `zOrder = IDIYComponent.BOARD`, while every
   other breadboard-friendly board in the package (`ArduinoNano`, `RaspberryPiPico`,
   `ESP32DevKit`, `ESP8266NodeMCU`, `WemosD1Mini`) uses `COMPONENT`. `Breadboard` is also `BOARD`,
   so a Teensy dropped on a breadboard renders in placement order rather than reliably on top.
   Looks like an oversight; changing it affects only draw order, not serialization.
2. **`Version` never reaches the BOM.** `getValue()` returns `null` and every board uses
   `bomPolicy = SHOW_ONLY_TYPE_NAME`, so a Uno R3 and a Uno R4 WiFi both print as "Arduino Uno".
   A maker's BOM is a shopping list and the variant is the part they buy. Worth deciding on a
   uniform approach before the roadmap roughly triples the number of variants.
3. **Editable-property set drifts across the family.** `Headers` exists on `ArduinoNano` and
   `RaspberryPiZero` but not on `ArduinoUno`, `Teensy` or `ESP32DevKit`; there is no "show pin
   labels" toggle anywhere; `WemosD1Mini` hardcodes `SILK_PIN_NAMES_LEFT` / `SILK_PIN_NAMES_RIGHT`
   instead of using the shared `drawPinLabels`. Settling a uniform Version / Headers / Show Labels
   / Board Color set now stops ten more boards inheriting the inconsistency.
4. **Pin-name conventions differ.** `ESP32DevKit` uses `GND_1`, `ArduinoNano` uses `GND1`. And
   `AbstractMakerBoard.getDisplayPinLabel` truncates at the first `_` and `(`, so `ArduinoNano`'s
   `D3 (~)` silently loses its PWM marker on the silkscreen. Pick one convention before adding
   more arrays.

Items 1 and 4 are behavioural changes small enough to carry their own regression-suite run;
items 2 and 3 are decisions to make before the bulk of the roadmap lands.

## 9. Suggested sequencing

1. ~~**§5.1 enum appends**~~ — **done**: Pico 2 / 2 W, Zero 2 W, Uno R4 Minima, Leonardo,
   Teensy 3.2.
2. ~~**§5.2 Item 6, modern Nano**~~ and ~~**Item 7, ESP32-C3 / C6**~~ — **done**.
3. **Cross-cutting fixes** (§8) — cheap, and fix 4 is better done before more pin arrays exist.
4. **§5.3 new classes** — ESP32-CAM and SuperMini first; they close the largest remaining gaps.
5. **Tier 2** — one family at a time, each an independent commit.

Each item is an independent commit; none depends on another except where sequencing is noted.

## 10. Open decisions

1. **BOM treatment of `Version`** (§8.2). Options: leave as-is; switch these boards to a BOM
   policy that includes the variant; or expose the variant through a value-like property. Affects
   every board in the package, so worth deciding once.
2. **Uniform property set** (§8.3). Which of `Headers` and a new "Show Pin Labels" toggle should
   exist on all boards, and whether retrofitting `Headers` onto `ArduinoUno` / `Teensy` /
   `ESP32DevKit` is wanted. New boolean fields deserialize as `false` in old files, so the default
   must be chosen to preserve the current look.
3. **ESP32-C6 in the same pass as C3?** §5.2 Item 7 is written for both since they share a body.
   If time-boxing, ship C3 first and add C6 as a one-branch follow-up.
4. **Nano pin-label verbosity** (§5.2 Item 6). Recommendation as written: annotate bus pins on all
   modern variants, add the GPIO number only for `NANO_ESP32`.
5. **i18n.** Component display names in `@ComponentDescriptor` are not currently in
   `diylc-swing/src/main/resources/lang/*.txt` for these boards, and the enum labels are plain
   strings shown in the property editor. No translation work is required by this roadmap; noted
   for a future i18n pass.
