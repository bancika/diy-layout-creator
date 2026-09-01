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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
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

@ComponentDescriptor(name = "ESP32 DevKit", category = "Controllers",
    author = "Branislav Stojkovic", description = "ESP32 DevKit Wi-Fi and Bluetooth Development Board (30-Pin / 38-Pin / 44-Pin S3)",
    instanceNamePrefix = "MCU", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class ESP32DevKit extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public enum DevKitVersion {
    DevKit_V1_30Pin("ESP32 DevKit V1 (30-Pin)"),
    DevKitC_V4_38Pin("ESP32 DevKitC V4 (38-Pin)"),
    ESP32_S3_DevKitC_44Pin("ESP32-S3 DevKitC-1 (44-Pin)");

    private final String label;

    DevKitVersion(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  public static Color ESP_BLACK = Color.decode("#3E3E3E");
  public static Color ANTENNA_BG_COLOR = Color.decode("#1E1E1E");
  public static Color SILK_COLOR = Color.WHITE;

  public static Size BOARD_WIDTH_30 = new Size(28.33d, SizeUnit.mm);
  public static Size BOARD_LENGTH_30 = new Size(51.45d, SizeUnit.mm);
  public static Size TOP_MARGIN_30 = new Size(6.7d, SizeUnit.mm);
  public static Size ANTENNA_LENGTH = new Size(7.0d, SizeUnit.mm);
  public static Size ANTENNA_WIDTH = new Size(15.0d, SizeUnit.mm);
  public static Size SHIELD_WIDTH = new Size(15.0d, SizeUnit.mm);
  public static Size SHIELD_LENGTH = new Size(18.0d, SizeUnit.mm);
  public static Size HOLE_DIAMETER_30 = new Size(2.8d, SizeUnit.mm);
  public static Size HOLE_EDGE_MARGIN_30 = new Size(0.8d, SizeUnit.mm);

  // 38-Pin (DevKitC V4) Dimensions
  public static Size BOARD_WIDTH_38 = new Size(27.9d, SizeUnit.mm);
  public static Size BOARD_LENGTH_38 = new Size(54.4d, SizeUnit.mm);
  public static Size MAIN_BODY_LENGTH_38 = new Size(48.2d, SizeUnit.mm);
  public static Size ANTENNA_LENGTH_38 = new Size(6.2d, SizeUnit.mm);

  // Common Spacing
  public static Size ROW_SPACING = new Size(1.0d, SizeUnit.in);

  // Pin Label Silkscreen
  public static Size PIN_LABEL_OFFSET = new Size(1.8d, SizeUnit.mm);
  public static Font PIN_FONT = new Font("SansSerif", Font.PLAIN, 8);

  // ESP32-S3 DevKitC-1 (44-Pin) Dimensions
  // BOARD_LENGTH_S3 is the main PCB body only; antenna extends above separately
  public static Size BOARD_WIDTH_S3 = new Size(25.40d, SizeUnit.mm);
  public static Size BOARD_LENGTH_S3 = new Size(62.74d, SizeUnit.mm);
  public static Size ROW_SPACING_S3 = new Size(22.86d, SizeUnit.mm);
  public static Size TOP_MARGIN_S3 = new Size(1.40d, SizeUnit.mm);
  public static Size BOTTOM_MARGIN_S3 = new Size(8.0d, SizeUnit.mm);
  public static Size ANTENNA_WIDTH_S3 = new Size(18.0d, SizeUnit.mm);
  public static Size ANTENNA_LENGTH_S3 = new Size(6.0d, SizeUnit.mm);
  public static Size SHIELD_WIDTH_S3 = new Size(16.0d, SizeUnit.mm);
  public static Size SHIELD_LENGTH_S3 = new Size(16.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES_30 = new String[] {
      // Left row (pins 0..14, top to bottom)
      "EN", "VP (GPIO36)", "VN (GPIO39)", "D34", "D35", "D32", "D33", "D25", "D26", "D27", "D14", "D12", "GND1", "D13", "3V3",
      // Right row (pins 15..29, bottom to top)
      "VIN", "GND2", "D15", "D2", "D4", "D16 (RX2)", "D17 (TX2)", "D5", "D18", "D19", "D21", "RX0 (GPIO3)", "TX0 (GPIO1)", "D22", "D23"
  };

  public static final String[] PIN_NAMES_38 = new String[] {
      // Left row (J2: pins 0..18, top to bottom)
      "3V3", "EN", "VP", "VN", "IO34", "IO35", "IO32", "IO33",
      "IO25", "IO26", "IO27", "IO14", "IO12", "GND", "IO13",
      "D2", "D3", "CMD", "5V",
      // Right row (J3: pins 19..37, top to bottom)
      "GND", "IO23", "IO22", "TX", "RX", "IO21", "GND",
      "IO19", "IO18", "IO5", "IO17", "IO16", "IO4", "IO0", "IO2",
      "IO15", "D1", "D0", "CLK"
  };

  public static final String[] PIN_NAMES_S3_44 = new String[] {
      // Left row (J1: pins 0..21, top to bottom)
      "3V3", "3V3", "RST", "4", "5", "6", "7",
      "15", "16", "17", "18", "8", "3",
      "46", "9", "10", "11", "12", "13",
      "14", "5V", "G",
      // Right row (J3: pins 22..43, top to bottom)
      "G", "TX", "RX", "1", "2",
      "42", "41", "40", "39", "38",
      "37", "36", "35", "0", "45",
      "48", "47", "21", "20", "19",
      "G", "G"
  };

  private DevKitVersion version = DevKitVersion.DevKit_V1_30Pin;
  protected boolean headers = false;

  public ESP32DevKit() {
    super();
    this.bodyColor = ESP_BLACK;
    updateControlPoints();
  }

  @EditableProperty(name = "Version")
  public DevKitVersion getVersion() {
    return version;
  }

  public void setVersion(DevKitVersion version) {
    this.version = version;
    updateControlPoints();
    invalidateCache();
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
    if (version == DevKitVersion.DevKit_V1_30Pin) {
      if (index >= 0 && index < PIN_NAMES_30.length) {
        return PIN_NAMES_30[index];
      }
    } else if (version == DevKitVersion.DevKitC_V4_38Pin) {
      if (index >= 0 && index < PIN_NAMES_38.length) {
        return PIN_NAMES_38[index];
      }
    } else if (version == DevKitVersion.ESP32_S3_DevKitC_44Pin) {
      if (index >= 0 && index < PIN_NAMES_S3_44.length) {
        return PIN_NAMES_S3_44[index];
      }
    }
    return "Pin " + (index + 1);
  }

  /**
   * Calculates unrotated relative offsets of all control points for the current version.
   */
  private double[][] getRelativeOffsets() {
    double spacing = PIN_SPACING.convertToPixels();

    if (version == DevKitVersion.DevKit_V1_30Pin) {
      double rowSpacing = ROW_SPACING.convertToPixels();
      double[][] relativeOffsets = new double[30][2];
      for (int i = 0; i < 15; i++) {
        relativeOffsets[i][0] = 0;
        relativeOffsets[i][1] = i * spacing;
      }
      for (int i = 0; i < 15; i++) {
        relativeOffsets[15 + i][0] = rowSpacing;
        relativeOffsets[15 + i][1] = (14 - i) * spacing;
      }
      return relativeOffsets;
    } else if (version == DevKitVersion.DevKitC_V4_38Pin) {
      double rowSpacing = ROW_SPACING.convertToPixels();
      double[][] relativeOffsets = new double[PIN_NAMES_38.length][2];
      for (int i = 0; i < 19; i++) {
        relativeOffsets[i][0] = 0;
        relativeOffsets[i][1] = i * spacing;
      }
      for (int i = 0; i < 19; i++) {
        relativeOffsets[19 + i][0] = rowSpacing;
        relativeOffsets[19 + i][1] = i * spacing;
      }
      return relativeOffsets;
    } else if (version == DevKitVersion.ESP32_S3_DevKitC_44Pin) {
      double rowSpacingS3 = ROW_SPACING_S3.convertToPixels();
      double[][] relativeOffsets = new double[PIN_NAMES_S3_44.length][2];
      for (int i = 0; i < 22; i++) {
        relativeOffsets[i][0] = 0;
        relativeOffsets[i][1] = i * spacing;
      }
      for (int i = 0; i < 22; i++) {
        relativeOffsets[22 + i][0] = rowSpacingS3;
        relativeOffsets[22 + i][1] = i * spacing;
      }
      return relativeOffsets;
    }
    return new double[0][0];
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double[][] relativeOffsets = getRelativeOffsets();
    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();

    if (version == DevKitVersion.DevKit_V1_30Pin) {
      double rowSpacing = ROW_SPACING.convertToPixels();
      double boardW = BOARD_WIDTH_30.convertToPixels();
      double boardH = BOARD_LENGTH_30.convertToPixels();
      double topMargin = TOP_MARGIN_30.convertToPixels();
      double boardX = (x + rowSpacing / 2.0) - boardW / 2.0;
      double boardY = y - topMargin;
      return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 8, 8);
    } else if (version == DevKitVersion.ESP32_S3_DevKitC_44Pin) {
      double rowSpacingS3 = ROW_SPACING_S3.convertToPixels();
      double boardW = BOARD_WIDTH_S3.convertToPixels();
      double boardH = BOARD_LENGTH_S3.convertToPixels();
      double antennaW = ANTENNA_WIDTH_S3.convertToPixels();
      double antennaH = ANTENNA_LENGTH_S3.convertToPixels();
      double topMargin = TOP_MARGIN_S3.convertToPixels();

      double mainX = (x + rowSpacingS3 / 2.0) - boardW / 2.0;
      double mainY = y - topMargin;
      double antennaX = (x + rowSpacingS3 / 2.0) - antennaW / 2.0;
      double antennaY = mainY - antennaH;

      Area bodyArea = new Area(new Rectangle2D.Double(mainX, mainY, boardW, boardH));
      bodyArea.add(new Area(new Rectangle2D.Double(antennaX, antennaY, antennaW, antennaH + 1)));
      return bodyArea;
    } else {
      double rowSpacing = ROW_SPACING.convertToPixels();
      double mainW = BOARD_WIDTH_38.convertToPixels();
      double mainH = MAIN_BODY_LENGTH_38.convertToPixels();
      double antennaW = ANTENNA_WIDTH.convertToPixels();
      double antennaH = ANTENNA_LENGTH_38.convertToPixels();

      double mainX = (x + rowSpacing / 2.0) - mainW / 2.0;
      double mainY = (y + 18 * PIN_SPACING.convertToPixels() / 2.0) - mainH / 2.0;
      double antennaX = (x + rowSpacing / 2.0) - antennaW / 2.0;
      double antennaY = mainY - antennaH;

      Area bodyArea = new Area(new RoundRectangle2D.Double(mainX, mainY, mainW, mainH, 8, 8));
      bodyArea.add(new Area(new Rectangle2D.Double(antennaX, antennaY, antennaW, antennaH + 1)));
      return bodyArea;
    }
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

    Shape boardShape = getBodyShape();
    Composite oldComposite = applyAlpha(g2d, componentState);

    // Determine the center X for the current version
    double centerX;
    if (version == DevKitVersion.ESP32_S3_DevKitC_44Pin) {
      centerX = x + ROW_SPACING_S3.convertToPixels() / 2.0;
    } else {
      centerX = x + ROW_SPACING.convertToPixels() / 2.0;
    }

    drawingObserver.startTracking();
    if (outlineMode) {
      g2d.setColor(Constants.TRANSPARENT_COLOR);
      g2d.fill(boardShape);
    } else {
      if (version == DevKitVersion.DevKit_V1_30Pin) {
        g2d.setColor(bodyColor);
        g2d.fill(boardShape);
      } else if (version == DevKitVersion.ESP32_S3_DevKitC_44Pin) {
        double boardW = BOARD_WIDTH_S3.convertToPixels();
        double boardH = BOARD_LENGTH_S3.convertToPixels();
        double antennaW = ANTENNA_WIDTH_S3.convertToPixels();
        double antennaH = ANTENNA_LENGTH_S3.convertToPixels();
        double topMargin = TOP_MARGIN_S3.convertToPixels();

        double mainX = centerX - boardW / 2.0;
        double mainY = y - topMargin;
        double antennaX = centerX - antennaW / 2.0;
        double antennaY = mainY - antennaH;

        // Main board body (sharp rectangular edges)
        g2d.setColor(bodyColor);
        g2d.fill(new Rectangle2D.Double(mainX, mainY, boardW, boardH));

        // Antenna tab in #1e1e1e
        g2d.setColor(ANTENNA_BG_COLOR);
        g2d.fill(new Rectangle2D.Double(antennaX, antennaY, antennaW, antennaH));
      } else {
        double rowSpacing = ROW_SPACING.convertToPixels();
        double mainW = BOARD_WIDTH_38.convertToPixels();
        double mainH = MAIN_BODY_LENGTH_38.convertToPixels();
        double antennaW = ANTENNA_WIDTH.convertToPixels();
        double antennaH = ANTENNA_LENGTH_38.convertToPixels();
        double mainX = (x + rowSpacing / 2.0) - mainW / 2.0;
        double mainY = (y + 18 * PIN_SPACING.convertToPixels() / 2.0) - mainH / 2.0;
        double antennaX = (x + rowSpacing / 2.0) - antennaW / 2.0;
        double antennaY = mainY - antennaH;

        // Main board body
        g2d.setColor(bodyColor);
        g2d.fill(new RoundRectangle2D.Double(mainX, mainY, mainW, mainH, 8, 8));

        // Antenna tab in #1e1e1e
        g2d.setColor(ANTENNA_BG_COLOR);
        g2d.fill(new Rectangle2D.Double(antennaX, antennaY, antennaW, antennaH));
      }
    }
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      double shift1mm = new Size(1.0d, SizeUnit.mm).convertToPixels();

      if (version == DevKitVersion.DevKit_V1_30Pin) {
        double btnW = BUTTON_LENGTH.convertToPixels();
        double btnH = BUTTON_WIDTH.convertToPixels();
        double rowSpacing = ROW_SPACING.convertToPixels();
        double boardW = BOARD_WIDTH_30.convertToPixels();
        double boardH = BOARD_LENGTH_30.convertToPixels();
        double topMargin = TOP_MARGIN_30.convertToPixels();
        double boardX = centerX - boardW / 2.0;
        double boardY = y - topMargin;

        double antennaW = ANTENNA_WIDTH.convertToPixels();
        double antennaH = ANTENNA_LENGTH.convertToPixels();
        double antennaX = centerX - antennaW / 2.0;
        double antennaY = boardY;

        // Antenna (dark rectangle underneath + gold serpentine trace)
        drawPcbAntenna(g2d, antennaX, antennaY, antennaW, antennaH);

        // ESP32-WROOM-32 metal shield module below antenna
        double shieldW = SHIELD_WIDTH.convertToPixels();
        double shieldH = SHIELD_LENGTH.convertToPixels();
        double shieldX = centerX - shieldW / 2.0;
        double shieldY = boardY + antennaH;
        drawMetalConnector(g2d, shieldX, shieldY, shieldW, shieldH, "ESP32-WROOM-32");

        // 4 Corner Mounting Holes (diameter 2.8mm, 0.8mm away from edges in both directions)
        double holeDiameter = HOLE_DIAMETER_30.convertToPixels();
        double holeRadius = holeDiameter / 2.0;
        double edgeMargin = HOLE_EDGE_MARGIN_30.convertToPixels();
        double leftHoleX = boardX + edgeMargin + holeRadius;
        double rightHoleX = boardX + boardW - edgeMargin - holeRadius;
        double topHoleY = boardY + edgeMargin + holeRadius;
        double bottomHoleY = boardY + boardH - edgeMargin - holeRadius;

        drawMountingHole(g2d, leftHoleX, topHoleY, holeDiameter);
        drawMountingHole(g2d, rightHoleX, topHoleY, holeDiameter);
        drawMountingHole(g2d, leftHoleX, bottomHoleY, holeDiameter);
        drawMountingHole(g2d, rightHoleX, bottomHoleY, holeDiameter);

        // Micro-USB Jack at bottom (7.5mm x 5.6mm, 0.5mm overhang)
        double usbW = USB_MICRO_WIDTH.convertToPixels();
        double usbH = USB_MICRO_LENGTH.convertToPixels();
        double usbOverhang = USB_MICRO_OVERHANG.convertToPixels();
        double usbX = centerX - usbW / 2.0;
        double usbY = boardY + boardH - usbH + usbOverhang;
        drawMicroUsb(g2d, usbX, usbY, usbW, usbH, "USB");

        // EN & BOOT tactile buttons at bottom (flanking the Micro-USB port)
        double btnY = boardY + boardH - 25 - 1.5 * shift1mm;
        double btnLeftX = leftHoleX + 14 + 0.5 * shift1mm;
        double btnRightX = rightHoleX - 14 - 0.5 * shift1mm - btnW;
        drawButtons(g2d, btnLeftX, btnRightX, btnY, btnW, btnH, "EN", "BOOT");

        // Silkscreen
        g2d.setColor(SILK_COLOR);
        g2d.setFont(SILK_FONT);
        StringUtils.drawCenteredText(g2d, "ESP32 DevKit V1", centerX, shieldY + shieldH + 45, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      } else if (version == DevKitVersion.ESP32_S3_DevKitC_44Pin) {
        double btnW = BUTTON_WIDTH.convertToPixels();
        double btnH = BUTTON_LENGTH.convertToPixels();
        double boardW = BOARD_WIDTH_S3.convertToPixels();
        double boardH = BOARD_LENGTH_S3.convertToPixels();
        double topMargin = TOP_MARGIN_S3.convertToPixels();
        double antennaW = ANTENNA_WIDTH_S3.convertToPixels();
        double antennaH = ANTENNA_LENGTH_S3.convertToPixels();

        double mainX = centerX - boardW / 2.0;
        double mainY = y - topMargin;
        double antennaX = centerX - antennaW / 2.0;
        double antennaY = mainY - antennaH;

        // Antenna at top (dark rectangle + gold serpentine trace)
        drawPcbAntenna(g2d, antennaX, antennaY, antennaW, antennaH);

        // ESP32-S3-WROOM-1 metal shield module below antenna
        double shieldW = SHIELD_WIDTH_S3.convertToPixels();
        double shieldH = SHIELD_LENGTH_S3.convertToPixels();
        double shieldX = centerX - shieldW / 2.0;
        double shieldY = mainY + 4;
        drawMetalConnector(g2d, shieldX, shieldY, shieldW, shieldH, "ESP32-S3-WROOM-1");

        // BOOT & RESET tactile buttons (per DXF: BOOT on left near pin 13/14, RESET on right near pin 20/19)
        double btnY = y + 18.5 * PIN_SPACING.convertToPixels() - btnH / 2.0;
        double btnLeftX = mainX + 49;
        double btnRightX = mainX + boardW - 49 - btnW;
        drawButtons(g2d, btnLeftX, btnRightX, btnY, btnW, btnH, "BOOT", "RST");

        // Two Micro-USB connectors at bottom edge (UART on left, USB on right)
        // Each is 7.5x5.6mm, centered 6mm from the board edge, protruding 0.5mm below
        double usbW = USB_MICRO_WIDTH.convertToPixels();
        double usbH = USB_MICRO_LENGTH.convertToPixels();
        double usbOverhang = USB_MICRO_OVERHANG.convertToPixels();
        double usbEdgeDist = new Size(6.0d, SizeUnit.mm).convertToPixels();
        double usbLeftX = mainX + usbEdgeDist - usbW / 2.0;
        double usbRightX = mainX + boardW - usbEdgeDist - usbW / 2.0;
        double usbY = mainY + boardH - usbH + usbOverhang;
        drawMicroUsb(g2d, usbLeftX, usbY, usbW, usbH, "UART");
        drawMicroUsb(g2d, usbRightX, usbY, usbW, usbH, "USB");

        // Silkscreen
        g2d.setColor(SILK_COLOR);
        g2d.setFont(SILK_FONT);
        StringUtils.drawCenteredText(g2d, "ESP32-S3 DevKitC-1", centerX, shieldY + shieldH + 45, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      } else {
        // 38-Pin (DevKitC V4)
        double btnW = BUTTON_LENGTH.convertToPixels();
        double btnH = BUTTON_WIDTH.convertToPixels();
        double rowSpacing = ROW_SPACING.convertToPixels();
        double mainW = BOARD_WIDTH_38.convertToPixels();
        double mainH = MAIN_BODY_LENGTH_38.convertToPixels();
        double mainX = centerX - mainW / 2.0;
        double mainY = (y + 18 * PIN_SPACING.convertToPixels() / 2.0) - mainH / 2.0;

        double antennaW = ANTENNA_WIDTH.convertToPixels();
        double antennaH = ANTENNA_LENGTH_38.convertToPixels();
        double antennaX = centerX - antennaW / 2.0;
        double antennaY = mainY - antennaH;

        // Antenna (dark rectangle underneath + gold serpentine trace)
        drawPcbAntenna(g2d, antennaX, antennaY, antennaW, antennaH);

        // ESP32-WROOM-32 metal shield module below antenna
        double shieldW = SHIELD_WIDTH.convertToPixels();
        double shieldH = SHIELD_LENGTH.convertToPixels();
        double shieldX = centerX - shieldW / 2.0;
        double shieldY = mainY + 8;
        drawMetalConnector(g2d, shieldX, shieldY, shieldW, shieldH, "ESP32-WROOM-32");

        // Micro-USB Jack at bottom (7.5mm x 5.6mm, 0.5mm overhang)
        double usbW = USB_MICRO_WIDTH.convertToPixels();
        double usbH = USB_MICRO_LENGTH.convertToPixels();
        double usbOverhang = USB_MICRO_OVERHANG.convertToPixels();
        double usbX = centerX - usbW / 2.0;
        double usbY = mainY + mainH - usbH + usbOverhang;
        drawMicroUsb(g2d, usbX, usbY, usbW, usbH, "USB");

        // EN & BOOT tactile buttons at bottom
        double btnY = mainY + mainH - 25 - new Size(2.0d, SizeUnit.mm).convertToPixels();
        double btnLeftX = mainX + 38;
        double btnRightX = mainX + mainW - 38 - btnW;
        drawButtons(g2d, btnLeftX, btnRightX, btnY, btnW, btnH, "EN", "BOOT");

        // Silkscreen
        g2d.setColor(SILK_COLOR);
        g2d.setFont(SILK_FONT);
        StringUtils.drawCenteredText(g2d, "ESP32 DevKitC V4", centerX, shieldY + shieldH + 45, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }

      // Draw pin labels next to control points
      drawPinLabels(g2d, x, y);
    }

    g2d.setTransform(oldTx);

    if (headers) {
      drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);
    } else {
      drawPcbSolderPads(g2d, 0, controlPoints.length, true, outlineMode, drawingObserver);
    }

    g2d.setComposite(oldComposite);
  }

  /**
   * Helper to format a pin name for silkscreen display by omitting any parenthesized content.
   */
  public static String getDisplayPinLabel(String name) {
    if (name == null) {
      return "";
    }
    int parenIdx = name.indexOf('(');
    if (parenIdx != -1) {
      name = name.substring(0, parenIdx);
    }
    return name.trim();
  }

  /**
   * Helper to draw rotated control point / pin names next to the pins.
   */
  private void drawPinLabels(Graphics2D g2d, double x, double y) {
    double[][] offsets = getRelativeOffsets();
    double labelOffset = PIN_LABEL_OFFSET.convertToPixels();
    int count = offsets.length;
    int half = count / 2;

    g2d.setColor(SILK_COLOR);
    g2d.setFont(PIN_FONT);

    for (int i = 0; i < count; i++) {
      String name = getControlPointNodeName(i);
      String label = getDisplayPinLabel(name);
      if (label.isEmpty()) {
        continue;
      }
      double pinX = x + offsets[i][0];
      double pinY = y + offsets[i][1];
      double textX = (i < half) ? (pinX + labelOffset) : (pinX - labelOffset);
      double textY = pinY;

      AffineTransform oldLabelTx = g2d.getTransform();
      g2d.rotate(Math.PI / 2, textX, textY);
      StringUtils.drawCenteredText(g2d, label, textX, textY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      g2d.setTransform(oldLabelTx);
    }
  }

  /**
   * Helper to draw a pair of tactile buttons with labels.
   */
  private void drawButtons(Graphics2D g2d, double btnLeftX, double btnRightX, double btnY, double btnW, double btnH,
      String leftLabel, String rightLabel) {
    drawButton(g2d, btnLeftX, btnY, btnW, btnH);
    drawButton(g2d, btnRightX, btnY, btnW, btnH);

    g2d.setColor(SILK_COLOR);
    g2d.setFont(SILK_FONT_SMALL);
    StringUtils.drawCenteredText(g2d, leftLabel, btnLeftX + btnW / 2.0, btnY - 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(g2d, rightLabel, btnRightX + btnW / 2.0, btnY - 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(ESP_BLACK);
    g2d.fill(new RoundRectangle2D.Double(5, 2, width - 10, height - 4, 3, 3));
    g2d.setColor(Color.GRAY);
    g2d.draw(new RoundRectangle2D.Double(5, 2, width - 10, height - 4, 3, 3));

    // Metal shield
    g2d.setColor(METAL_SHIELD_COLOR);
    g2d.fillRect(8, 6, width - 16, 12);

    g2d.setColor(SILK_COLOR);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "ESP32", width / 2, height / 2 + 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
