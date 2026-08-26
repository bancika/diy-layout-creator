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

@ComponentDescriptor(name = "Teensy", category = "Controllers",
    author = "Branislav Stojkovic", description = "PJRC Teensy 4.0 / 4.1 Development Board (ARM Cortex-M7, 600 MHz)",
    instanceNamePrefix = "MCU", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class Teensy extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public enum TeensyVersion {
    Teensy_4_0("Teensy 4.0"),
    Teensy_4_1("Teensy 4.1");

    private final String label;

    TeensyVersion(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  // Board color matches standard green PCB (like Pi Zero)
  public static Color TEENSY_GREEN = Color.decode("#1B5E20");
  public static Color BUTTON_BODY_COLOR = Color.decode("#383838");
  public static Color BUTTON_BORDER_COLOR = Color.decode("#666666");
  public static Color BUTTON_ACTUATOR_COLOR = Color.decode("#A0A0A0");
  public static Color SILK_COLOR = Color.WHITE;

  public static Color PAD_COLOR = GOLD_COLOR;
  public static Size HOLE_SIZE = new Size(0.7d, SizeUnit.mm);

  // ===== Teensy 4.0 dimensions (PJRC dimensions_teensy40.png) =====
  // 35.56 mm x 17.78 mm (1.4" x 0.7")
  public static Size BOARD_WIDTH_40 = new Size(17.78d, SizeUnit.mm);
  public static Size BOARD_LENGTH_40 = new Size(35.56d, SizeUnit.mm);

  // ===== Teensy 4.1 dimensions (PJRC dimensions_teensy41.png) =====
  // 60.96 mm x 17.78 mm (2.4" x 0.7")
  public static Size BOARD_WIDTH_41 = new Size(17.78d, SizeUnit.mm);
  public static Size BOARD_LENGTH_41 = new Size(60.96d, SizeUnit.mm);

  // Common pitch and spacing
  // Pin 1 offset from top and left edges is 0.05" (1.27 mm)
  // Row spacing is 0.60" (15.24 mm)
  // Pin spacing along row is 0.10" (2.54 mm)
  public static Size PIN1_OFFSET_X = new Size(1.27d, SizeUnit.mm);
  public static Size PIN1_OFFSET_Y = new Size(1.27d, SizeUnit.mm);
  public static Size ROW_SPACING = new Size(15.24d, SizeUnit.mm);

  // ===== Teensy 4.0 Pinout (34 pins total) =====
  // Left row (0..13, top to bottom): GND, 0..12
  // Right row (14..27, top to bottom): VIN, GND, 3.3V, 23..13
  // End cluster (28..32, bottom edge left to right): VBAT, 3.3V, GND, Program, On/Off
  // VUSB (33, near VIN): VUSB
  public static final String[] PIN_NAMES_40 = new String[] {
      // Left row (0..13)
      "GND", "0 (RX1/CS1/CRX2)", "1 (TX1/MISO1/CTX2)", "2 (OUT2)", "3 (LRCLK2)", "4 (BCLK2)",
      "5 (IN2)", "6 (OUT1D)", "7 (RX2/OUT1A)", "8 (TX2/IN1)", "9 (OUT1C)", "10 (CS/MQSR)",
      "11 (MOSI/CTX1)", "12 (MISO/MQSL)",
      // Right row (14..27)
      "VIN (3.6-5.5V)", "GND", "3.3V (250mA)", "23 (A9/CRX1)", "22 (A8/CTX1)", "21 (A7/RX5)",
      "20 (A6/TX5)", "19 (A5/SCL0)", "18 (A4/SDA0)", "17 (A3/TX4/SDA1)", "16 (A2/RX4/SCL1)",
      "15 (A1/RX3/SPDIF IN)", "14 (A0/TX3/SPDIF OUT)", "13 (SCK/CRX1/LED)",
      // End row (28..32, left to right along bottom edge)
      "VBAT", "3.3V (End)", "GND (End)", "Program", "On/Off",
      // VUSB (33)
      "VUSB"
  };

  // ===== Teensy 4.1 Pinout (65 pins total) =====
  // Left row (0..23, top to bottom): GND, 0..12, 3.3V, 24..32
  // Right row (24..47, top to bottom): VIN, GND, 3.3V, 23..13, GND, 41..33
  // Middle cluster (48..52, left to right): VBAT, 3.3V, GND, Program, On/Off
  // Ethernet header (53..58, 2x3): ETH_Rx+, ETH_LED, ETH_Tx-, ETH_Rx-, ETH_GND, ETH_Tx+
  // USB Host header (59..63, 1x5): USB_5V, USB_D-, USB_D+, USB_GND1, USB_GND2
  // VUSB (64, near VIN): VUSB
  public static final String[] PIN_NAMES_41 = new String[] {
      // Left row (0..23)
      "GND", "0 (RX1/CS1/CRX2)", "1 (TX1/MISO1/CTX2)", "2 (OUT2)", "3 (LRCLK2)", "4 (BCLK2)",
      "5 (IN2)", "6 (OUT1D)", "7 (RX2/OUT1A)", "8 (TX2/IN1)", "9 (OUT1C)", "10 (CS/MQSR)",
      "11 (MOSI/CTX1)", "12 (MISO/MQSL)", "3.3V", "24 (A10/TX6/SCL2)", "25 (A11/RX6/SDA2)",
      "26 (A12/MOSI1)", "27 (A13/SCK1)", "28 (RX7)", "29 (TX7)", "30 (CRX3)", "31 (CTX3)",
      "32 (OUT1B)",
      // Right row (24..47)
      "VIN (3.6-5.5V)", "GND", "3.3V (250mA)", "23 (A9/CRX1)", "22 (A8/CTX1)", "21 (A7/RX5)",
      "20 (A6/TX5)", "19 (A5/SCL)", "18 (A4/SDA)", "17 (A3/TX4/SDA1)", "16 (A2/RX4/SCL1)",
      "15 (A1/RX3/SPDIF IN)", "14 (A0/TX3/SPDIF OUT)", "13 (SCK/LED)", "GND", "41 (A17)",
      "40 (A16)", "39 (A15/MISO1)", "38 (A14/CS1)", "37 (CS)", "36 (CS)", "35 (TX8)",
      "34 (RX8)", "33 (MCLK2)",
      // Middle cluster (48..52, left to right near push button)
      "VBAT", "3.3V (Mid)", "GND (Mid)", "Program", "On/Off",
      // Ethernet header (53..58, 2x3)
      "ETH_Rx+", "ETH_LED", "ETH_Tx-", "ETH_Rx-", "ETH_GND", "ETH_Tx+",
      // USB Host header (59..63, 1x5)
      "USB_5V", "USB_D-", "USB_D+", "USB_GND1", "USB_GND2",
      // VUSB (64)
      "VUSB"
  };

  private TeensyVersion version = TeensyVersion.Teensy_4_0;

  public Teensy() {
    super();
    this.bodyColor = TEENSY_GREEN;
    updateControlPoints();
  }

  @EditableProperty(name = "Version")
  public TeensyVersion getVersion() {
    return version;
  }

  public void setVersion(TeensyVersion version) {
    this.version = version;
    updateControlPoints();
    invalidateCache();
  }

  private String[] getPinNames() {
    return (version == TeensyVersion.Teensy_4_0) ? PIN_NAMES_40 : PIN_NAMES_41;
  }

  @Override
  public String getControlPointNodeName(int index) {
    String[] names = getPinNames();
    if (index >= 0 && index < names.length) {
      return names[index];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();
    double rowSpacing = ROW_SPACING.convertToPixels();

    if (version == TeensyVersion.Teensy_4_0) {
      double[][] relativeOffsets = new double[PIN_NAMES_40.length][2];

      // Left row (0..13): X=0, Y=0..13*spacing
      for (int i = 0; i < 14; i++) {
        relativeOffsets[i][0] = 0;
        relativeOffsets[i][1] = i * spacing;
      }

      // Right row (14..27): X=rowSpacing, Y=0..13*spacing
      for (int i = 0; i < 14; i++) {
        relativeOffsets[14 + i][0] = rowSpacing;
        relativeOffsets[14 + i][1] = i * spacing;
      }

      // End row (28..32): 5 pins between pin 12 and pin 13 at Y=13*spacing
      for (int k = 1; k <= 5; k++) {
        relativeOffsets[28 + k - 1][0] = k * spacing;
        relativeOffsets[28 + k - 1][1] = 13 * spacing;
      }

      // VUSB (33): near VIN at X=rowSpacing - spacing, Y=spacing
      relativeOffsets[33][0] = rowSpacing - spacing;
      relativeOffsets[33][1] = spacing;

      rotatePoints(firstPoint, relativeOffsets);
    } else {
      double[][] relativeOffsets = new double[PIN_NAMES_41.length][2];

      // Left row (0..23): X=0, Y=0..23*spacing
      for (int i = 0; i < 24; i++) {
        relativeOffsets[i][0] = 0;
        relativeOffsets[i][1] = i * spacing;
      }

      // Right row (24..47): X=rowSpacing, Y=0..23*spacing
      for (int i = 0; i < 24; i++) {
        relativeOffsets[24 + i][0] = rowSpacing;
        relativeOffsets[24 + i][1] = i * spacing;
      }

      // Middle cluster (48..52): 5 pins near push button at Y=17*spacing
      for (int k = 1; k <= 5; k++) {
        relativeOffsets[48 + k - 1][0] = k * spacing;
        relativeOffsets[48 + k - 1][1] = 17 * spacing;
      }

      // Ethernet Header (53..58): 2x3 header (2.0mm pitch)
      double ethPitch = new Size(2.0d, SizeUnit.mm).convertToPixels();
      double ethCenterX = rowSpacing / 2.0;
      double ethBaseY = 5 * spacing;
      // Col 1 (left): Rx+, LED, Tx-
      relativeOffsets[53] = new double[] {ethCenterX - ethPitch / 2.0, ethBaseY - ethPitch};
      relativeOffsets[54] = new double[] {ethCenterX - ethPitch / 2.0, ethBaseY};
      relativeOffsets[55] = new double[] {ethCenterX - ethPitch / 2.0, ethBaseY + ethPitch};
      // Col 2 (right): Rx-, GND, Tx+
      relativeOffsets[56] = new double[] {ethCenterX + ethPitch / 2.0, ethBaseY - ethPitch};
      relativeOffsets[57] = new double[] {ethCenterX + ethPitch / 2.0, ethBaseY};
      relativeOffsets[58] = new double[] {ethCenterX + ethPitch / 2.0, ethBaseY + ethPitch};

      // USB Host Header (59..63): 1x5 header (2.54mm pitch)
      for (int i = 0; i < 5; i++) {
        relativeOffsets[59 + i][0] = spacing;
        relativeOffsets[59 + i][1] = (4 + i) * spacing;
      }

      // VUSB (64): near VIN at X=rowSpacing - spacing, Y=spacing
      relativeOffsets[64][0] = rowSpacing - spacing;
      relativeOffsets[64][1] = spacing;

      rotatePoints(firstPoint, relativeOffsets);
    }
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();

    double boardW = (version == TeensyVersion.Teensy_4_0) ? BOARD_WIDTH_40.convertToPixels() : BOARD_WIDTH_41.convertToPixels();
    double boardH = (version == TeensyVersion.Teensy_4_0) ? BOARD_LENGTH_40.convertToPixels() : BOARD_LENGTH_41.convertToPixels();
    double pin1OffsetX = PIN1_OFFSET_X.convertToPixels();
    double pin1OffsetY = PIN1_OFFSET_Y.convertToPixels();

    double boardX = x - pin1OffsetX;
    double boardY = y - pin1OffsetY;
    return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 4, 4);
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

    drawingObserver.startTracking();

    // Fill board body (green PCB)
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(boardShape);

    // Board outline
    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      Rectangle2D boardBounds = boardShape.getBounds2D();
      double boardX = boardBounds.getX();
      double boardY = boardBounds.getY();
      double boardW = boardBounds.getWidth();
      double boardH = boardBounds.getHeight();
      double centerX = boardX + boardW / 2.0;

      // Micro-USB connector on top edge (overhangs edge slightly)
      double usbW = new Size(7.5d, SizeUnit.mm).convertToPixels();
      double usbH = new Size(5.6d, SizeUnit.mm).convertToPixels();
      double usbX = centerX - usbW / 2.0;
      double usbY = boardY - new Size(1.0d, SizeUnit.mm).convertToPixels();
      drawMetalConnector(g2d, usbX, usbY, usbW, usbH, "USB");

      // Main MCU chip (NXP i.MX RT1062 BGA)
      double chipW = new Size(7.5d, SizeUnit.mm).convertToPixels();
      double chipH = new Size(7.5d, SizeUnit.mm).convertToPixels();
      double chipX = centerX - chipW / 2.0;
      double chipY = (version == TeensyVersion.Teensy_4_0)
          ? boardY + new Size(9.5d, SizeUnit.mm).convertToPixels()
          : boardY + new Size(19.0d, SizeUnit.mm).convertToPixels();
      drawChip(g2d, chipX, chipY, chipW, chipH, "");

      // Pushbutton (Program button)
      double btnW = new Size(3.5d, SizeUnit.mm).convertToPixels();
      double btnH = new Size(3.0d, SizeUnit.mm).convertToPixels();
      double btnX = centerX - btnW / 2.0;
      double btnY = (version == TeensyVersion.Teensy_4_0)
          ? boardY + new Size(24.5d, SizeUnit.mm).convertToPixels()
          : boardY + new Size(35.5d, SizeUnit.mm).convertToPixels();
      g2d.setColor(BUTTON_BODY_COLOR);
      g2d.fill(new RoundRectangle2D.Double(btnX, btnY, btnW, btnH, 2, 2));
      g2d.setColor(BUTTON_BORDER_COLOR);
      g2d.draw(new RoundRectangle2D.Double(btnX, btnY, btnW, btnH, 2, 2));
      g2d.setColor(BUTTON_ACTUATOR_COLOR);
      g2d.fillOval((int) (btnX + btnW / 2.0 - 2.5), (int) (btnY + btnH / 2.0 - 2.5), 5, 5);

      // Teensy 4.1 extras: Ethernet PHY + SD card slot
      if (version == TeensyVersion.Teensy_4_1) {
        // MicroSD card slot at bottom edge
        double sdW = new Size(12.0d, SizeUnit.mm).convertToPixels();
        double sdH = new Size(14.0d, SizeUnit.mm).convertToPixels();
        double sdX = centerX - sdW / 2.0;
        double sdY = boardY + boardH - sdH + new Size(1.0d, SizeUnit.mm).convertToPixels();
        drawMetalConnector(g2d, sdX, sdY, sdW, sdH, "SD");
      }

      // Silkscreen white outline boxes around outer pins
      g2d.setColor(Color.WHITE);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      int outerRowCount = (version == TeensyVersion.Teensy_4_0) ? 14 : 24;
      double padBoxMargin = new Size(0.9d, SizeUnit.mm).convertToPixels();

      // Left row silkscreen box
      Point2D pLeftFirst = controlPoints[0];
      Point2D pLeftLast = controlPoints[outerRowCount - 1];
      g2d.draw(new Rectangle2D.Double(
          pLeftFirst.getX() - padBoxMargin, pLeftFirst.getY() - padBoxMargin,
          padBoxMargin * 2, (pLeftLast.getY() - pLeftFirst.getY()) + padBoxMargin * 2));

      // Right row silkscreen box
      Point2D pRightFirst = controlPoints[outerRowCount];
      Point2D pRightLast = controlPoints[outerRowCount * 2 - 1];
      g2d.draw(new Rectangle2D.Double(
          pRightFirst.getX() - padBoxMargin, pRightFirst.getY() - padBoxMargin,
          padBoxMargin * 2, (pRightLast.getY() - pRightFirst.getY()) + padBoxMargin * 2));

      // Silkscreen "TEENSY" label
      g2d.setColor(SILK_COLOR);
      g2d.setFont(SILK_FONT);
      String silkText = (version == TeensyVersion.Teensy_4_0) ? "Teensy 4.0" : "Teensy 4.1";
      double textY = (version == TeensyVersion.Teensy_4_0)
          ? chipY + chipH + new Size(2.5d, SizeUnit.mm).convertToPixels()
          : chipY + chipH + new Size(3.0d, SizeUnit.mm).convertToPixels();
      StringUtils.drawCenteredText(g2d, silkText, centerX, textY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    drawingObserver.stopTracking();

    g2d.setTransform(oldTx);

    // Render gold solder pads with drill holes (square for Pin 1 / GND)
    drawSolderPads(g2d, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  /**
   * Helper to draw GPIO solder pads (copper/gold pads with drill holes, square for Pin 1).
   */
  protected void drawSolderPads(Graphics2D g2d, boolean outlineMode, IDrawingObserver drawingObserver) {
    drawPcbSolderPads(g2d, 0, controlPoints.length, true, outlineMode, drawingObserver);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(TEENSY_GREEN);
    g2d.fill(new RoundRectangle2D.Double(5, 2, width - 10, height - 4, 3, 3));
    g2d.setColor(TEENSY_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(5, 2, width - 10, height - 4, 3, 3));

    // GPIO Solder Pads in icon
    g2d.setColor(PAD_COLOR);
    for (int y = 5; y <= height - 6; y += 3) {
      g2d.fillRect(6, y, 2, 2);
      g2d.fillRect(width - 8, y, 2, 2);
    }

    // MCU chip
    g2d.setColor(IC_BODY_COLOR);
    g2d.fillRect(10, 8, 12, 8);

    // USB connector at top
    g2d.setColor(USB_METAL_COLOR);
    g2d.fillRect(12, 2, 8, 3);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "TEENSY", width / 2, height / 2 + 8,
        HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
