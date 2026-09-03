# Implementation Plan — Tier 1 Modern Board Coverage

Status: proposed
Scope: `diylc-library`, package `org.diylc.components.micro`
Author: planning doc (for Branislav Stojkovic)

## 1. Goal

Close the "current‑generation" gaps in the single‑board‑computer / MCU set. Tier 1 is
the set of additions that ride on hardware **already modeled**, so each is a small,
low‑risk extension rather than a new component from scratch:

| # | Addition | Host class | Nature of change |
|---|----------|-----------|------------------|
| 1 | Raspberry Pi Pico 2 / Pico 2 W (RP2350) | `RaspberryPiPico` | 2 new enum constants |
| 2 | Raspberry Pi Zero 2 W | `RaspberryPiZero` | 1 new enum constant |
| 3 | ESP32‑C3 DevKitM‑1 (+ ESP32‑C6 DevKitC‑1) | `ESP32DevKit` | new pin arrays + geometry/draw branches |
| 4 | Modern Arduino Nano family (Every, 33 IoT, 33 BLE / BLE Sense, RP2040 Connect, Nano ESP32) | `ArduinoNano` | introduce a `Version` enum + per‑version overrides |

Items 1 and 2 are a few hours each. Item 3 is roughly a day. Item 4 is the largest
because `ArduinoNano` has no `Version` property yet and one has to be retrofitted, plus
per‑board pin labels and silk. In line with the rest of this package, every item stays a
**single component with a `Version` dropdown** — no new class per revision.

## 2. Shared mechanics (applies to every item)

### 2.1 Component discovery
Components are found by classpath annotation scan in
`ComponentProcessor.getComponentTypes()` (Reflections, `TypesAnnotated`,
`includePackage("org.diylc.components")`). **Any class carrying `@ComponentDescriptor`
is picked up automatically** — there is no manifest or registry to edit. Adding enum
constants to an existing class needs nothing. Adding a *new* class needs nothing for
discovery, but should be added to the hand‑maintained list in
`org.diylc.components.maker.MakerComponentsTest#allMakerComponentClasses`.

### 2.2 Version property pattern
Every multi‑variant board in this package follows the same shape (see `ArduinoUno`,
`RaspberryPiPico`, `RaspberryPiZero`, `ESP32DevKit`):

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

New variants must be **appended** to the enum (XStream serializes enum constants by
name; appending is backward compatible, reordering/removing is not). Keep the default
constant and the null‑guard so existing `.diy` files deserialize unchanged.

### 2.3 Serialization safety checklist (per changed class)
- `serialVersionUID` stays `1L`.
- Default `version` field value is unchanged → old files keep their look.
- **Control‑point count must not change for existing variants.** If a new variant has
  a different pin count, `updateControlPoints()` must size the array from the
  per‑version pin list (as `ESP32DevKit` already does), and any code that assumes a
  fixed `PIN_NAMES.length` must switch on version.
- `drawIcon()` reflects only the default variant — leave it alone unless the default
  changes.

### 2.4 Test pattern
Each board has a `*Test.java` under
`diylc-library/src/test/java/org/diylc/components/micro/` with:
- `testControlPointCountAndNames()` — count + every name non‑empty + spot checks
- `testDimensionsAndPinGeometry()` — `getBodyShape().getBounds2D()` vs expected mm, and
  control‑point offsets from board edges
- `testVersionProperty()` — default, `toString()` labels, round‑trip through setter
- a `*Drawing` smoke test — `draw()` in NORMAL / SELECTED / outline, headers on and off,
  against a `BufferedImage` with a no‑op `IDrawingObserver`

Extend these in place; add one geometry test and one draw smoke test per new variant.

### 2.5 Build / verify
```
cd diylc
mvn -pl diylc-library -am test
```
Rendering‑regression fixtures live in `../diylc-regression-data/` but are **not** wired
into the library test run, so new variants do not break it. If those baselines are
regenerated periodically, add sample placements for the new variants at that time.

---

## 3. Item 1 — Raspberry Pi Pico 2 / Pico 2 W

**Hardware facts that make this cheap:** the Pico 2 is pin‑, size‑ and connector‑
identical to the Pico (21 × 51 mm, 2×20 castellated + through‑hole, 3‑pad SWD, micro‑USB,
BOOTSEL). The only visible differences: the SoC silk reads **RP2350** instead of RP2040,
and Pico 2 W carries the same Infineon CYW43439 radio module in the same position as
Pico W (so its 3‑pad SWD block moves to the same offset Pico W uses).

### Changes — `RaspberryPiPico.java`
1. `PicoVersion` enum: append
   ```java
   PICO_2("Pi Pico 2"),
   PICO_2_W("Pi Pico 2 W");
   ```
2. Add helpers to keep the existing `== PICO_W` checks readable:
   ```java
   private boolean isW()  { return version == PicoVersion.PICO_W || version == PicoVersion.PICO_2_W; }
   private boolean isV2() { return version == PicoVersion.PICO_2 || version == PicoVersion.PICO_2_W; }
   ```
3. In `updateControlPoints()`, `getBodyShape()`, `drawCastellatedPads()` and `draw()`,
   replace `getVersion() == PicoVersion.PICO_W` with `isW()`. Geometry is otherwise
   untouched (board size and pin layout are identical).
4. In `draw()`, the RP2040 chip label becomes `isV2() ? "RP2350" : "RP2040"`.
5. `PIN_NAMES` is unchanged and stays shared across all four variants (RP2350 keeps the
   same 40‑pin GPIO map and the same `GPxx` numbering on the DevKit form factor).
6. `drawIcon()` unchanged (default is still `PICO`).

### Tests — `RaspberryPiPicoTest.java`
- Extend `testVersionProperty()` with the two new labels and setter round‑trips.
- Add `testPico2GeometryMatchesPico()` — assert `getBodyShape()` bounds and control‑point
  count (43) are identical for `PICO` and `PICO_2`, and for `PICO_W` and `PICO_2_W`.
- Add a draw smoke test for `PICO_2_W`.

### Effort: ~2–3 h. Risk: minimal.

---

## 4. Item 2 — Raspberry Pi Zero 2 W

**Hardware facts:** identical 65 × 30 mm outline, identical 40‑pin GPIO position, identical
connector layout (mini‑HDMI, 2× micro‑USB for DATA/PWR, CSI FPC on the right edge, 4
mounting holes at 3.5 mm). The SoC is the RP3A0 package‑on‑package (Broadcom BCM2710A1,
quad‑core) — physically a ~12 mm square in the same spot the current drawing already
places a 12 mm chip, with the WiFi/BT integrated (no separate can). So this is a silk /
label change only.

### Changes — `RaspberryPiZero.java`
1. `ZeroVersion` enum: append `PI_ZERO_2_W("Pi Zero 2 W")`.
2. No geometry change: `updateControlPoints()`, `getBodyShape()`, pin arrays untouched.
3. In `draw()`, the version label line already prints `"Raspberry " + getVersion()`, so
   it will read "Raspberry Pi Zero 2 W" automatically — verify it still fits inside
   `hdrBoxW`; if not, drop to `SILK_FONT` or shorten to `getVersion().toString()` for the
   2 W case.
4. Optional accuracy touch: for `PI_ZERO_2_W`, draw the SoC as a plain dark PoP square
   (no Broadcom split) — current rendering is already close enough that this is cosmetic.
5. `drawIcon()` unchanged.

### Tests — `RaspberryPiZeroTest.java`
- Extend `testVersionProperty()` (add label + round‑trip).
- Add geometry‑equivalence assertion vs `PI_ZERO_W`.
- Draw smoke test for the new constant.

### Effort: ~1–2 h. Risk: minimal (only watch the label width).

---

## 5. Item 3 — ESP32‑C3 DevKitM‑1 (+ ESP32‑C6 DevKitC‑1)

This is the heaviest of the enum‑style items because `ESP32DevKit` gives every variant
its own pin‑name array, its own branch in `getRelativeOffsets()`, `getBodyShape()` and
`draw()`. The C3 is a genuinely different board (single RISC‑V core, ESP32‑C3‑MINI‑1
module, single RGB LED), not a re‑label.

### Hardware facts
- **ESP32‑C3‑DevKitM‑1**: ~52 × 25.4 mm, two 1×15 headers on 0.1" pitch (row spacing
  ~22.86 mm / 0.9"), ESP32‑C3‑MINI‑1 module with PCB antenna at the top, micro‑USB **and**
  a USB‑UART micro‑USB (two connectors) at the bottom, `BOOT` + `RST` buttons, one
  addressable RGB LED. GPIO count 22 (0–21), broken out as `IO0…IO21` plus `5V`, `G`,
  `3V3`.
- **ESP32‑C6‑DevKitC‑1**: same 2×15 / 0.9" body, ESP32‑C6‑WROOM‑1 module, **two USB‑C**
  connectors, RGB LED. GPIO `IO0…IO23`.

### Changes — `ESP32DevKit.java`
1. `DevKitVersion` enum: append
   ```java
   ESP32_C3_DevKitM_1("ESP32-C3 DevKitM-1 (32-Pin)"),
   ESP32_C6_DevKitC_1("ESP32-C6 DevKitC-1 (32-Pin)");
   ```
2. Add `PIN_NAMES_C3` and `PIN_NAMES_C6` (`String[32]` each: 15 + 15 signal pins with the
   two power/ground groups — confirm exact silk order against the Espressif DevKit
   schematics before committing the arrays).
3. Add board‑dimension constants `BOARD_WIDTH_C3 / _LENGTH_C3` (and `_C6`), plus module /
   antenna sizes — the C3 module is smaller than WROOM‑32; reuse `SHIELD_*_S3` sizing as a
   starting point.
4. `getControlPointNodeName(int)`: add the two `else if` branches returning from the new
   arrays.
5. `getRelativeOffsets()`: add two branches. Both are the simple "two straight columns"
   layout — copy the 30‑pin branch, change the loop bound to 16/16 (or 15/15 + power)
   and use a C3/C6 row‑spacing constant.
6. `getBodyShape()`: add branches (rounded rectangle + PCB‑antenna notch at top, same
   idea as the 30‑pin and S3 cases).
7. `draw()`: add branches — module shield with "ESP32-C3-MINI-1" / "ESP32-C6-WROOM-1"
   label via `drawMetalConnector`, PCB antenna via the existing antenna drawing,
   `BOOT`/`RST` buttons via `drawButtons`, USB via `drawMicroUsb` ×2 for C3 and
   `drawUsbC` ×2 for C6, one RGB LED dot.
8. `drawIcon()` unchanged (default stays 30‑pin V1).

### Tests — `ESP32DevKitTest.java`
Mirror the existing per‑version blocks: count + names, body bounds vs the new mm
constants, column geometry (two straight rows, 0.1" pitch, correct row spacing),
`testVersionProperty()` labels, and a draw smoke test for each new constant in both
header modes.

### Effort: ~1 day (mostly getting the two pin arrays and the module/USB placement right).
### Risk: moderate — geometry constants need cross‑checking against datasheets; contained
entirely within `ESP32DevKit`.

### Optional companion (Tier 2, note only)
The **ESP32‑C3 SuperMini** (~22.5 × 18 mm, 2×8 castellated, PCB antenna) is a different
form factor and is better as its own class modeled on `RaspberryPiPico`'s castellated‑pad
rendering. Out of scope for Tier 1; listed so it is not forgotten.

---

## 6. Item 4 — Modern Arduino Nano family

`ArduinoNano` currently has **no** version property. Tier 1 introduces one and adds the
current lineup, which shares the classic Nano's 0.73" × 1.70" outline, the 2×15 pin
positions, and the 2×3 ICSP block — so **control‑point count stays 36 for every variant**
and serialization is safe.

### 6.1 Approach
Add a `Version` enum to `ArduinoNano` — one palette entry, one icon, a dropdown to pick
the board revision. This is the established paradigm for this package (`ArduinoUno`
R3/R4, `RaspberryPiPico`, `RaspberryPiZero`, `ESP32DevKit`) and is the paradigm to use
here: a single component with a `Version` property, not a class per variant. A separate
class is reserved for genuinely different form factors (see the SuperMini note in §5),
which the modern Nanos are not — they keep the classic 0.73" × 1.70" outline and pin
positions.

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

### 6.2 What varies by version

| Aspect | CLASSIC | EVERY | 33 IoT | 33 BLE / BLE Sense | RP2040 Connect | Nano ESP32 |
|---|---|---|---|---|---|---|
| MCU silk label | `ATmega328P` (rot. 45°) | `ATmega4809` | `SAMD21` | `nRF52840` | `RP2040` | `ESP32-S3` |
| Radio module | — | — | NINA‑W102 can | on‑SoC + chip antenna | NINA‑W102 can | ESP32‑S3 module + antenna |
| Logic level silk | `5V` | `5V` | `3V3` | `3V3` | `3V3` | `3V3` |
| USB connector | Mini‑B (`drawMiniUsb`) | Micro‑B (`drawMicroUsb`) | Micro‑B | Micro‑B | Micro‑B | **USB‑C** (`drawUsbC`) |
| Pin‑name array | current | current | modern 3V3 map | modern 3V3 map | modern 3V3 map | modern + `GPIOxx` dual labels |
| ICSP 2×3 | yes | pads only | pads | pads | pads | pads |

Helper methods `drawMiniUsb`, `drawMicroUsb`, `drawUsbC`, `drawMetalConnector`,
`drawChip` all already exist in `AbstractMakerBoard`.

### 6.3 Pin‑name arrays
- `PIN_NAMES_CLASSIC` = the current `PIN_NAMES` (rename, keep contents).
- `PIN_NAMES_MODERN` — same physical positions; the practical differences from classic
  are that `A4`/`A5` should be annotated `A4 (SDA)` / `A5 (SCL)`, all `A0..A7` present,
  and `D2..D12`/`D13` unchanged. Reset pin labels stay.
- `PIN_NAMES_NANO_ESP32` — as modern, but dual‑label the digital pins with the Espressif
  GPIO, e.g. `D2 (GPIO5)`. (Confirm the mapping from the Arduino Nano ESP32 pinout
  before committing.)

`getControlPointNodeName(int)` switches on version to pick the array; all arrays are
length 36 so `updateControlPoints()` is unchanged.

### 6.4 Draw changes
`draw()` gains version switches for: the MCU chip label (and whether it is drawn rotated
45° like the classic TQFP or axis‑aligned like a QFN), the optional radio can
(`drawMetalConnector` near the top of the board), the USB connector helper, and the
`5V` / `3V3` silk near the power pins. Board body shape and mounting holes are unchanged.

### 6.5 Castellated edges (defer)
The modern Nanos have castellated side edges in addition to the through‑hole rows. Adding
that visual (as in `RaspberryPiPico.drawCastellatedPads`) is a **follow‑up enhancement**,
not part of Tier 1 — keeping the current through‑hole pad rendering is acceptable.

### 6.6 Tests — `ArduinoNanoTest.java`
- New `testVersionProperty()` (default `CLASSIC`, labels, round‑trip).
- `testControlPointCountAndNames()` parameterized over all seven versions — count 36,
  no empty names, spot‑check the `A4 (SDA)` annotation on a modern version and the
  `GPIO` dual‑label on `NANO_ESP32`.
- Geometry test unchanged (outline is version‑independent) — assert that explicitly.
- Draw smoke test looping all versions × headers on/off.

### 6.7 Effort: ~1–1.5 days. Risk: moderate — the value is entirely in getting the pin
labels and per‑board silk right; the rendering scaffold is a light touch.

---

## 7. Cross‑cutting work

1. **`MakerComponentsTest`** — only needs edits if Item 4 becomes a new class
   (`ArduinoNanoModern`). With the recommended enum approach, no change.
2. **i18n** — component display names in `@ComponentDescriptor` are not currently in
   `diylc-swing/src/main/resources/lang/*.txt` for these boards; the new enum *labels*
   are plain strings shown in the property editor. No translation work required for
   Tier 1; note for a future i18n pass.
3. **`.diy` back‑compat** — covered by §2.2/§2.3: append‑only enums, unchanged defaults,
   unchanged control‑point counts. No file‑format migration.
4. **Regression data** — see §2.5; optional, only when baselines are regenerated.
5. **Changelog / release notes** — add a "New boards" line when cutting the release.

## 8. Suggested sequencing

1. **Item 2 (Zero 2 W)** — smallest, establishes the "append enum + label" rhythm.
2. **Item 1 (Pico 2 / 2 W)** — small, introduces the `isW()` / `isV2()` helper refactor.
3. **Item 4 (modern Nano)** — do before Item 3 so the `ArduinoNano` version‑enum
   retrofit is reviewed while the pattern is fresh; largest label‑research effort.
4. **Item 3 (ESP32‑C3 / C6)** — most drawing code; do last.

Each item is an independent PR/commit; none depends on another.

## 9. Open decisions for the author

1. **ESP32‑C6 in this pass?** Item 3 is written for C3 + C6 together since they share a
   body. If time‑boxing, ship C3 first and add C6 as a one‑branch follow‑up.
2. **Pin‑label verbosity on modern Nano.** Whether to dual‑label (`D2 (GPIO5)`) on
   `NANO_ESP32` only, or also annotate `SDA`/`SCL`/`SCK`/`MISO`/`MOSI` across all modern
   variants. Recommendation: annotate bus pins on all modern variants; add the GPIO
   number only for `NANO_ESP32`.
3. **ESP32‑C3 SuperMini** as a separate castellated class — Tier 2, confirm it is
   tracked separately. (This is the one place a separate class is right, because the
   SuperMini is a different footprint, not another revision of the DevKit outline.)
