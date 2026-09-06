/*
 * 
 * DIY Layout Creator (DIYLC).
 * Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.components.micro;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Arduino Nano", category = "Controllers",
    author = "Branislav Stojkovic",
    description = "Arduino Nano Breadboard-Friendly Microcontroller Board (classic, Every, 33 IoT, "
        + "33 BLE, RP2040 Connect, Nano ESP32)",
    instanceNamePrefix = "MCU", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class ArduinoNano extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color SILK_COLOR = Color.WHITE;

  public static Size BOARD_WIDTH = new Size(0.73d, SizeUnit.in);
  public static Size BOARD_LENGTH = new Size(1.70d, SizeUnit.in);

  public static final int PINS_PER_ROW = 15;

  public enum NanoVersion {
    CLASSIC("Nano (ATmega328)", "NANO", "m328P", null),
    EVERY("Nano Every", "EVERY", "m4809", "SAMD11"),
    NANO_33_IOT("Nano 33 IoT", "33 IOT", "SAMD21", "NINA-W102"),
    NANO_33_BLE("Nano 33 BLE", "33 BLE", null, "NINA-B306"),
    NANO_33_BLE_SENSE("Nano 33 BLE Sense", "33 BLE SENSE", null, "NINA-B306"),
    NANO_RP2040_CONNECT("Nano RP2040 Connect", "RP2040 CONNECT", "RP2040", "NINA-W102"),
    NANO_ESP32("Nano ESP32", "NANO ESP32", null, "NORA-W106");

    private final String label;
    private final String silkLabel;
    private final String mcuLabel;
    private final String moduleLabel;

    NanoVersion(String label, String silkLabel, String mcuLabel, String moduleLabel) {
      this.label = label;
      this.silkLabel = silkLabel;
      this.mcuLabel = mcuLabel;
      this.moduleLabel = moduleLabel;
    }

    public String getSilkLabel() {
      return silkLabel;
    }

    /**
     * Marking of the separately visible MCU package, or null when the MCU is a die inside the
     * module rather than its own part on the board.
     */
    public String getMcuLabel() {
      return mcuLabel;
    }

    /**
     * Marking of the part occupying the end of the board opposite the USB jack, where the classic
     * Nano has its ICSP header. Null on the classic Nano, which has the header instead.
     */
    public String getModuleLabel() {
      return moduleLabel;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  public static final String[] PIN_NAMES = new String[] {
      // Left row (0..14, top to bottom)
      "D1 (TX)", "D0 (RX)", "RESET", "GND1", "D2", "D3 (~)", "D4", "D5 (~)", "D6 (~)", "D7", "D8", "D9 (~)", "D10 (~)", "D11 (~)", "D12",
      // Right row (15..29, top to bottom)
      "VIN", "GND2", "RST2", "5V", "A7", "A6", "A5", "A4", "A3", "A2", "A1", "A0", "AREF", "3.3V", "D13",
      // ICSP (30..35)
      "MISO", "5V_ICSP", "SCK", "MOSI", "RST_ICSP", "GND_ICSP"
  };

  // The modern Nanos keep the classic 2x15 pin positions and annotate the bus pins on the
  // silkscreen, but they have no 2x3 ICSP block: that end of the board carries the radio module or
  // USB bridge instead, so they are 30 control points rather than 36.
  public static final String[] PIN_NAMES_MODERN = new String[] {
      // Left row (0..14, top to bottom)
      "D1 (TX)", "D0 (RX)", "RESET", "GND1", "D2", "D3 (~)", "D4", "D5 (~)", "D6 (~)", "D7", "D8", "D9 (~)", "D10 (~)", "D11 (MOSI)", "D12 (MISO)",
      // Right row (15..29, top to bottom)
      "VIN", "GND2", "RST2", "5V", "A7", "A6", "A5 (SCL)", "A4 (SDA)", "A3", "A2", "A1", "A0", "AREF", "3.3V", "D13 (SCK)"
  };

  // The Nano ESP32 dual-labels its digital pins with the Espressif GPIO number, which is what the
  // Arduino core exposes as Dn and what the ESP-IDF documentation refers to.
  public static final String[] PIN_NAMES_ESP32 = new String[] {
      // Left row (0..14, top to bottom)
      "D1 (TX/GPIO43)", "D0 (RX/GPIO44)", "RESET", "GND1", "D2 (GPIO5)", "D3 (GPIO6)", "D4 (GPIO7)",
      "D5 (GPIO8)", "D6 (GPIO9)", "D7 (GPIO10)", "D8 (GPIO17)", "D9 (GPIO18)", "D10 (GPIO21)",
      "D11 (MOSI/GPIO38)", "D12 (MISO/GPIO47)",
      // Right row (15..29, top to bottom)
      "VIN", "GND2", "RST2", "5V", "A7 (GPIO14)", "A6 (GPIO13)", "A5 (SCL/GPIO12)", "A4 (SDA/GPIO11)",
      "A3 (GPIO4)", "A2 (GPIO3)", "A1 (GPIO2)", "A0 (GPIO1)", "AREF", "3.3V", "D13 (SCK/GPIO48)"
  };

  protected NanoVersion version = NanoVersion.CLASSIC;
  protected boolean headers = false;

  public ArduinoNano() {
    super();
    this.bodyColor = ARDUINO_TEAL;
    updateControlPoints();
  }

  @EditableProperty(name = "Version")
  public NanoVersion getVersion() {
    if (version == null) {
      version = NanoVersion.CLASSIC;
    }
    return version;
  }

  public void setVersion(NanoVersion version) {
    this.version = version;
    updateControlPoints();
    invalidateCache();
  }

  private String[] getPinNames() {
    switch (getVersion()) {
      case CLASSIC:
        return PIN_NAMES;
      case NANO_ESP32:
        return PIN_NAMES_ESP32;
      default:
        return PIN_NAMES_MODERN;
    }
  }

  private UsbPortType getUsbPortType() {
    switch (getVersion()) {
      case CLASSIC:
        return UsbPortType.MINI;
      case NANO_ESP32:
        return UsbPortType.TYPE_C;
      default:
        return UsbPortType.MICRO;
    }
  }

  /** Connector footprint {width, length, overhang} for a USB port type. */
  private Size[] getUsbPortSizes(UsbPortType type) {
    switch (type) {
      case MINI:
        return new Size[] {USB_MINI_WIDTH, USB_MINI_LENGTH, USB_MINI_OVERHANG};
      case TYPE_C:
        return new Size[] {USB_C_WIDTH, USB_C_LENGTH, USB_C_OVERHANG};
      default:
        return new Size[] {USB_MICRO_WIDTH, USB_MICRO_LENGTH, USB_MICRO_OVERHANG};
    }
  }

  /**
   * True for the boards whose end module is a shielded can. The Every's part there is a bare QFN
   * USB bridge, so it is drawn as a chip instead.
   */
  private boolean hasShieldedModule() {
    return getVersion() != NanoVersion.CLASSIC && getVersion() != NanoVersion.EVERY;
  }

  @EditableProperty(name = "Headers")
  public boolean getHeaders() {
    return headers;
  }

  public void setHeaders(boolean headers) {
    this.headers = headers;
    invalidateCache();
  }

  @Override
  public String getControlPointNodeName(int index) {
    String[] pinNames = getPinNames();
    if (index >= 0 && index < pinNames.length) {
      return pinNames[index];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels(); // 20px (0.10")
    double rowSpacing = new Size(0.60d, SizeUnit.in).convertToPixels(); // 120px (0.60")

    double[][] relativeOffsets = new double[getPinNames().length][2];

    // Left row (pins 0..14, top to bottom)
    for (int i = 0; i < 15; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
    }
    // Right row (pins 15..29, top to bottom)
    for (int i = 0; i < 15; i++) {
      relativeOffsets[15 + i][0] = rowSpacing;
      relativeOffsets[15 + i][1] = i * spacing;
    }
    // Only the classic Nano populates the 2x3 ICSP block; on every later board that end of the
    // PCB carries the radio module or USB bridge instead.
    if (getVersion() != NanoVersion.CLASSIC) {
      rotatePoints(firstPoint, relativeOffsets);
      return;
    }

    // ICSP header (2x3 pins, 30..35) flush with top edge:
    // Outer row is 0.05" (10px) from top edge (-20px / -0.10" relative to Pin 0)
    // Inner row is 0.15" (30px) from top edge (0px / 0.00" relative to Pin 0, aligned with Pin 0 & Pin 15)
    double icspOuterY = -spacing; // -20px (-0.10")
    double icspInnerY = 0;        // 0px (0.00")

    double col0X = rowSpacing / 2.0 - spacing; // 40px (0.20")
    double col1X = rowSpacing / 2.0;           // 60px (0.30")
    double col2X = rowSpacing / 2.0 + spacing; // 80px (0.40")

    // Pin 1 (MISO at col2X, outer row)
    relativeOffsets[30] = new double[] {col2X, icspOuterY};
    // Pin 2 (5V_ICSP at col2X, inner row)
    relativeOffsets[31] = new double[] {col2X, icspInnerY};
    // Pin 3 (SCK at col1X, outer row)
    relativeOffsets[32] = new double[] {col1X, icspOuterY};
    // Pin 4 (MOSI at col1X, inner row)
    relativeOffsets[33] = new double[] {col1X, icspInnerY};
    // Pin 5 (RST_ICSP at col0X, outer row)
    relativeOffsets[34] = new double[] {col0X, icspOuterY};
    // Pin 6 (GND_ICSP at col0X, inner row)
    relativeOffsets[35] = new double[] {col0X, icspInnerY};

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();

    double boardW = BOARD_WIDTH.convertToPixels();   // 146px (0.73")
    double boardH = BOARD_LENGTH.convertToPixels();  // 340px (1.70")
    double rowSpacing = new Size(0.60d, SizeUnit.in).convertToPixels(); // 120px (0.60")
    double boardMarginX = (boardW - rowSpacing) / 2.0; // 13px (0.065")
    double boardMarginY = new Size(0.15d, SizeUnit.in).convertToPixels(); // 30px (0.15")
    double cornerRadius = new Size(1.0d, SizeUnit.mm).convertToPixels();

    double boardX = x - boardMarginX;
    double boardY = y - boardMarginY;
    RoundRectangle2D outline =
        new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, cornerRadius * 2, cornerRadius * 2);
    if (getVersion() == NanoVersion.CLASSIC) {
      return outline;
    }

    // Every later Nano has castellated edges, so the pad columns bite notches out of both sides
    double spacing = PIN_SPACING.convertToPixels();
    Area boardArea = new Area(outline);
    subtractCastellationNotches(boardArea, y, PINS_PER_ROW, spacing, boardX);
    subtractCastellationNotches(boardArea, y, PINS_PER_ROW, spacing, boardX + boardW);
    return boardArea;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }

    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();

    AffineTransform oldTx = g2d.getTransform();
    if (orientation != Orientation.DEFAULT) {
      g2d.rotate(orientation.toRadians(), x, y);
    }

    double boardW = BOARD_WIDTH.convertToPixels();   // 146px (0.73")
    double boardH = BOARD_LENGTH.convertToPixels();  // 340px (1.70")
    double rowSpacing = new Size(0.60d, SizeUnit.in).convertToPixels();
    double boardMarginX = (boardW - rowSpacing) / 2.0; // 13px (0.065")
    double boardMarginY = new Size(0.15d, SizeUnit.in).convertToPixels(); // 30px (0.15")

    double boardX = x - boardMarginX;
    double boardY = y - boardMarginY;

    Shape boardShape = getBodyShape();

    Composite oldComposite = applyAlpha(g2d, componentState);

    // Draw PCB body
    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(boardShape);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      // 4 Corner Mounting Holes (non-plated drill holes matching Uno/Mega)
      double holeDiameter = new Size(0.07d, SizeUnit.in).convertToPixels();
      double topHoleY = boardY + new Size(0.05d, SizeUnit.in).convertToPixels();
      double bottomHoleY = boardY + new Size(1.65d, SizeUnit.in).convertToPixels();
      double leftHoleX = boardX + boardMarginX;
      double rightHoleX = boardX + boardW - boardMarginX;

      drawMountingHole(g2d, leftHoleX, topHoleY, holeDiameter);
      drawMountingHole(g2d, rightHoleX, topHoleY, holeDiameter);
      drawMountingHole(g2d, leftHoleX, bottomHoleY, holeDiameter);
      drawMountingHole(g2d, rightHoleX, bottomHoleY, holeDiameter);

      // USB jack at bottom (plain metal connector)
      UsbPortType usbType = getUsbPortType();
      Size[] usbSizes = getUsbPortSizes(usbType);
      double usbW = usbSizes[0].convertToPixels();
      double usbH = usbSizes[1].convertToPixels();
      double usbOverhang = usbSizes[2].convertToPixels();
      drawUsbPort(g2d, boardX + (boardW - usbW) / 2.0, boardY + boardH - usbH + usbOverhang, usbW, usbH,
          usbType, "USB");

      double chipSize = new Size(0.28d, SizeUnit.in).convertToPixels();
      double chipCenterX = boardX + boardW / 2.0;
      double chipCenterY = boardY + new Size(1.06d, SizeUnit.in).convertToPixels();

      if (getVersion() == NanoVersion.CLASSIC) {
        // The ATmega328P TQFP is mounted at 45 degrees on the classic Nano
        AffineTransform oldChipTx = g2d.getTransform();
        g2d.translate(chipCenterX, chipCenterY);
        g2d.rotate(Math.PI / 4.0);

        g2d.setColor(IC_BODY_COLOR);
        g2d.fill(new RoundRectangle2D.Double(-chipSize / 2.0, -chipSize / 2.0, chipSize, chipSize, 4, 4));
        g2d.setColor(IC_BORDER_COLOR);
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new RoundRectangle2D.Double(-chipSize / 2.0, -chipSize / 2.0, chipSize, chipSize, 4, 4));

        // Pin 1 dot
        g2d.setColor(PIN_MARKER_COLOR);
        g2d.fill(new Ellipse2D.Double(-chipSize / 2.0 + 3, -chipSize / 2.0 + 3, 3, 3));

        g2d.setColor(IC_TEXT_COLOR);
        g2d.setFont(SILK_FONT_SMALL);
        StringUtils.drawCenteredText(g2d, getVersion().getMcuLabel(), 0, 0, HorizontalAlignment.CENTER,
            VerticalAlignment.CENTER);

        g2d.setTransform(oldChipTx);
      } else if (getVersion().getMcuLabel() != null) {
        // Later Nanos carry an axis-aligned QFN in the same spot, unless the MCU is a die inside
        // the module at the far end of the board
        drawChip(g2d, chipCenterX - chipSize / 2.0, chipCenterY - chipSize / 2.0, chipSize, chipSize,
            getVersion().getMcuLabel());
      }

      // The end of the board opposite the USB jack: the classic Nano's ICSP header, and on every
      // later board the part that identifies it at a glance
      if (getVersion().getModuleLabel() != null) {
        double moduleY = boardY + new Size(1.5d, SizeUnit.mm).convertToPixels();
        if (hasShieldedModule()) {
          double moduleW = new Size(10.0d, SizeUnit.mm).convertToPixels();
          double moduleH = new Size(11.0d, SizeUnit.mm).convertToPixels();
          drawMetalConnector(g2d, boardX + (boardW - moduleW) / 2.0, moduleY, moduleW, moduleH,
              getVersion().getModuleLabel());
        } else {
          double bridgeSize = new Size(5.0d, SizeUnit.mm).convertToPixels();
          drawChip(g2d, boardX + (boardW - bridgeSize) / 2.0, moduleY, bridgeSize, bridgeSize,
              getVersion().getModuleLabel());
        }
      }

      // Reset Button
      double btnW = BUTTON_WIDTH.convertToPixels();
      double btnH = BUTTON_LENGTH.convertToPixels();
      double btnX = boardX + (boardW - btnW) / 2.0;
      double btnY = boardY + new Size(0.68d, SizeUnit.in).convertToPixels();
      drawButton(g2d, btnX, btnY, btnW, btnH);

      g2d.setColor(SILK_COLOR);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "RST", boardX + boardW / 2.0, btnY - 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Silkscreen, dropped 1mm below the nominal 0.50" so it clears the chips above it
      g2d.setFont(SILK_FONT);
      StringUtils.drawCenteredText(g2d, getVersion().getSilkLabel(), boardX + boardW / 2.0,
          boardY + new Size(0.50d, SizeUnit.in).convertToPixels() + new Size(1.0d, SizeUnit.mm).convertToPixels(),
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    // Castellated pads bite into the board edge, so they are drawn while the board rotation is
    // still applied rather than from the already-rotated control points
    if (!headers && getVersion() != NanoVersion.CLASSIC) {
      double spacing = PIN_SPACING.convertToPixels();
      drawCastellatedPads(g2d, x, y, PINS_PER_ROW, spacing, boardX, outlineMode, drawingObserver);
      drawCastellatedPads(g2d, x + rowSpacing, y, PINS_PER_ROW, spacing, boardX + boardW, outlineMode,
          drawingObserver);
    }

    g2d.setTransform(oldTx);

    // Draw pins or solder pads
    if (headers) {
      drawPinHeader(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);
    } else if (getVersion() == NanoVersion.CLASSIC) {
      drawPcbSolderPads(g2d, 0, controlPoints.length, true, outlineMode, drawingObserver);
    }

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    double boardX = 6;
    double boardY = 2;
    double boardW = width - 12;
    double boardH = height - 4;

    g2d.setColor(ARDUINO_TEAL);
    g2d.fill(new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 4, 4));
    g2d.setColor(ARDUINO_TEAL.darker());
    g2d.draw(new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 4, 4));

    // ICSP header at top
    g2d.setColor(HEADER_BODY_COLOR);
    g2d.fill(new Rectangle2D.Double(width / 2.0 - 4, boardY, 8, 4));

    // Mini USB at bottom
    g2d.setColor(USB_METAL_COLOR);
    g2d.fillRect((int) (width / 2.0 - 4), (int) (boardY + boardH - 4), 8, 4);

    // 45-degree rotated diamond IC chip in center
    AffineTransform oldTx = g2d.getTransform();
    g2d.translate(width / 2.0, boardY + boardH / 2.0 + 1);
    g2d.rotate(Math.PI / 4.0);
    g2d.setColor(IC_BODY_COLOR);
    g2d.fill(new RoundRectangle2D.Double(-4, -4, 8, 8, 1, 1));
    g2d.setColor(IC_BORDER_COLOR);
    g2d.draw(new RoundRectangle2D.Double(-4, -4, 8, 8, 1, 1));
    g2d.setTransform(oldTx);

    // Pin strips on sides
    g2d.setColor(PIN_COLOR);
    for (int y = 5; y < height - 5; y += 3) {
      g2d.fillRect(7, y, 2, 2);
      g2d.fillRect(width - 9, y, 2, 2);
    }
  }
}
