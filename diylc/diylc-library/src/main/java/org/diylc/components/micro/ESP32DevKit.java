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
    author = "Branislav Stojkovic", description = "ESP32 DevKit Wi-Fi and Bluetooth Development Board (30-Pin / 38-Pin)",
    instanceNamePrefix = "MCU", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class ESP32DevKit extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public enum DevKitVersion {
    DevKit_V1_30Pin("30-Pin (DevKit V1 / DOIT)"),
    DevKitC_V4_38Pin("38-Pin (DevKitC V4)");

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
  public static Color ANTENNA_COLOR = Color.decode("#DAA520");
  public static Color BUTTON_BODY_COLOR = Color.decode("#383838");
  public static Color BUTTON_BORDER_COLOR = Color.decode("#666666");
  public static Color BUTTON_ACTUATOR_COLOR = Color.decode("#A0A0A0");
  public static Color SILK_COLOR = Color.WHITE;

  // 30-Pin (DOIT V1) Dimensions
  public static Size BOARD_WIDTH_30 = new Size(28.2d, SizeUnit.mm);
  public static Size BOARD_LENGTH_30 = new Size(51.8d, SizeUnit.mm);
  public static Size TOP_MARGIN_30 = new Size(6.7d, SizeUnit.mm);
  public static Size ANTENNA_LENGTH_30 = new Size(6.2d, SizeUnit.mm);
  public static Size ANTENNA_WIDTH_30 = new Size(20.0d, SizeUnit.mm);
  public static Size HOLE_DIAMETER_30 = new Size(2.8d, SizeUnit.mm);
  public static Size HOLE_EDGE_MARGIN_30 = new Size(0.8d, SizeUnit.mm);

  // 38-Pin (DevKitC V4) Dimensions
  public static Size BOARD_WIDTH_38 = new Size(27.9d, SizeUnit.mm);
  public static Size BOARD_LENGTH_38 = new Size(54.4d, SizeUnit.mm);
  public static Size MAIN_BODY_LENGTH_38 = new Size(48.2d, SizeUnit.mm);
  public static Size ANTENNA_LENGTH_38 = new Size(6.2d, SizeUnit.mm);
  public static Size ANTENNA_WIDTH_38 = new Size(19.0d, SizeUnit.mm);

  // Common Spacing
  public static Size ROW_SPACING = new Size(1.0d, SizeUnit.in);

  public static final String[] PIN_NAMES_30 = new String[] {
      // Left row (pins 0..14, top to bottom)
      "EN", "VP (GPIO36)", "VN (GPIO39)", "D34", "D35", "D32", "D33", "D25", "D26", "D27", "D14", "D12", "GND1", "D13", "3V3",
      // Right row (pins 15..29, bottom to top)
      "VIN", "GND2", "D15", "D2", "D4", "D16 (RX2)", "D17 (TX2)", "D5", "D18", "D19", "D21", "RX0 (GPIO3)", "TX0 (GPIO1)", "D22", "D23"
  };

  public static final String[] PIN_NAMES_38 = new String[] {
      // Left row (pins 0..18, top to bottom)
      "3V3", "EN", "VP (GPIO36)", "VN (GPIO39)", "GPIO34", "GPIO35", "GPIO32", "GPIO33",
      "GPIO25", "GPIO26", "GPIO27", "GPIO14", "GPIO12", "GND1", "GPIO13",
      "D2 (GPIO9)", "D3 (GPIO10)", "CMD (GPIO11)", "5V",
      // Right row (pins 19..37, top to bottom)
      "GND2", "GPIO23", "GPIO22", "TX0 (GPIO1)", "RX0 (GPIO3)", "GPIO21", "GND3",
      "GPIO19", "GPIO18", "GPIO5", "GPIO17", "GPIO16", "GPIO4", "GPIO0", "GPIO2",
      "GPIO15", "D1 (GPIO8)", "D0 (GPIO7)", "CLK (GPIO6)"
  };

  private DevKitVersion version = DevKitVersion.DevKit_V1_30Pin;

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

  @Override
  public String getControlPointNodeName(int index) {
    if (version == DevKitVersion.DevKit_V1_30Pin) {
      if (index >= 0 && index < PIN_NAMES_30.length) {
        return PIN_NAMES_30[index];
      }
    } else {
      if (index >= 0 && index < PIN_NAMES_38.length) {
        return PIN_NAMES_38[index];
      }
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();
    double rowSpacing = ROW_SPACING.convertToPixels(); // 200px (1.00")

    if (version == DevKitVersion.DevKit_V1_30Pin) {
      double[][] relativeOffsets = new double[30][2];
      for (int i = 0; i < 15; i++) {
        relativeOffsets[i][0] = 0;
        relativeOffsets[i][1] = i * spacing;
      }
      for (int i = 0; i < 15; i++) {
        relativeOffsets[15 + i][0] = rowSpacing;
        relativeOffsets[15 + i][1] = (14 - i) * spacing;
      }
      rotatePoints(firstPoint, relativeOffsets);
    } else {
      double[][] relativeOffsets = new double[PIN_NAMES_38.length][2];
      for (int i = 0; i < 19; i++) {
        relativeOffsets[i][0] = 0;
        relativeOffsets[i][1] = i * spacing;
      }
      for (int i = 0; i < 19; i++) {
        relativeOffsets[19 + i][0] = rowSpacing;
        relativeOffsets[19 + i][1] = i * spacing;
      }
      rotatePoints(firstPoint, relativeOffsets);
    }
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double rowSpacing = ROW_SPACING.convertToPixels();

    if (version == DevKitVersion.DevKit_V1_30Pin) {
      double boardW = BOARD_WIDTH_30.convertToPixels();
      double boardH = BOARD_LENGTH_30.convertToPixels();
      double topMargin = TOP_MARGIN_30.convertToPixels();
      double boardX = (x + rowSpacing / 2.0) - boardW / 2.0;
      double boardY = y - topMargin;
      return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 8, 8);
    } else {
      double mainW = BOARD_WIDTH_38.convertToPixels();
      double mainH = MAIN_BODY_LENGTH_38.convertToPixels();
      double antennaW = ANTENNA_WIDTH_38.convertToPixels();
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

    double rowSpacing = ROW_SPACING.convertToPixels();
    Shape boardShape = getBodyShape();
    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    if (outlineMode) {
      g2d.setColor(Constants.TRANSPARENT_COLOR);
      g2d.fill(boardShape);
    } else {
      if (version == DevKitVersion.DevKit_V1_30Pin) {
        g2d.setColor(bodyColor);
        g2d.fill(boardShape);
      } else {
        double mainW = BOARD_WIDTH_38.convertToPixels();
        double mainH = MAIN_BODY_LENGTH_38.convertToPixels();
        double antennaW = ANTENNA_WIDTH_38.convertToPixels();
        double antennaH = ANTENNA_LENGTH_38.convertToPixels();
        double mainX = (x + rowSpacing / 2.0) - mainW / 2.0;
        double mainY = (y + 18 * PIN_SPACING.convertToPixels() / 2.0) - mainH / 2.0;
        double antennaX = (x + rowSpacing / 2.0) - antennaW / 2.0;
        double antennaY = mainY - antennaH;

        // Main board body
        g2d.setColor(bodyColor);
        g2d.fill(new RoundRectangle2D.Double(mainX, mainY, mainW, mainH, 8, 8));

        // Antenna tab in slightly lighter color
        Color antennaTabColor = new Color(
            Math.min(255, bodyColor.getRed() + 32),
            Math.min(255, bodyColor.getGreen() + 32),
            Math.min(255, bodyColor.getBlue() + 32)
        );
        g2d.setColor(antennaTabColor);
        g2d.fill(new Rectangle2D.Double(antennaX, antennaY, antennaW, antennaH));
      }
    }
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      double antennaX;
      double antennaY;
      double antennaW;
      double antennaH;
      double shieldY;
      double shieldH = new Size(18.0d, SizeUnit.mm).convertToPixels();
      double shieldW = new Size(18.0d, SizeUnit.mm).convertToPixels();
      double shieldX = (x + rowSpacing / 2.0) - shieldW / 2.0;
      double usbY;
      double btnLeftX;
      double btnRightX;
      double btnY;
      double btnW = 20;
      double btnH = 20;
      double shift1mm = new Size(1.0d, SizeUnit.mm).convertToPixels();

      if (version == DevKitVersion.DevKit_V1_30Pin) {
        double boardW = BOARD_WIDTH_30.convertToPixels();
        double boardH = BOARD_LENGTH_30.convertToPixels();
        double topMargin = TOP_MARGIN_30.convertToPixels();
        double boardX = (x + rowSpacing / 2.0) - boardW / 2.0;
        double boardY = y - topMargin;

        antennaW = ANTENNA_WIDTH_30.convertToPixels();
        antennaH = ANTENNA_LENGTH_30.convertToPixels();
        antennaX = (x + rowSpacing / 2.0) - antennaW / 2.0;
        antennaY = boardY;
        shieldY = boardY + antennaH + shift1mm;
        usbY = boardY + boardH - 22;

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

        btnY = boardY + boardH - 25 - shift1mm;
        btnLeftX = leftHoleX + 14 + shift1mm;
        btnRightX = rightHoleX - 14 - shift1mm - btnW;
      } else {
        double mainW = BOARD_WIDTH_38.convertToPixels();
        double mainH = MAIN_BODY_LENGTH_38.convertToPixels();
        double mainX = (x + rowSpacing / 2.0) - mainW / 2.0;
        double mainY = (y + 18 * PIN_SPACING.convertToPixels() / 2.0) - mainH / 2.0;

        antennaW = ANTENNA_WIDTH_38.convertToPixels();
        antennaH = ANTENNA_LENGTH_38.convertToPixels();
        antennaX = (x + rowSpacing / 2.0) - antennaW / 2.0;
        antennaY = mainY - antennaH;
        shieldY = mainY + 10;
        usbY = mainY + mainH - 22;

        btnY = mainY + mainH - 25;
        btnLeftX = mainX + 38;
        btnRightX = mainX + mainW - 38 - btnW;
      }

      // Antenna trace (gold/copper serpentine PCB trace)
      g2d.setColor(ANTENNA_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
      Path2D.Double antPath = new Path2D.Double();
      double antPadX = antennaX + 16;
      double antPadW = antennaW - 32;
      double antTopY = antennaY + 10;
      double antMidY = antennaY + antennaH - 12;
      antPath.moveTo(antPadX, antMidY);
      antPath.lineTo(antPadX, antTopY);
      antPath.lineTo(antPadX + antPadW * 0.25, antTopY);
      antPath.lineTo(antPadX + antPadW * 0.25, antMidY);
      antPath.lineTo(antPadX + antPadW * 0.50, antMidY);
      antPath.lineTo(antPadX + antPadW * 0.50, antTopY);
      antPath.lineTo(antPadX + antPadW * 0.75, antTopY);
      antPath.lineTo(antPadX + antPadW * 0.75, antMidY);
      antPath.lineTo(antPadX + antPadW, antMidY);
      antPath.lineTo(antPadX + antPadW, antTopY);
      g2d.draw(antPath);

      // ESP32-WROOM-32 metal shield module below antenna
      drawMetalConnector(g2d, shieldX, shieldY, shieldW, shieldH, "ESP32-WROOM-32");

      // Micro-USB Jack at bottom
      double usbW = 58;
      double usbH = 34;
      double usbX = (x + rowSpacing / 2.0) - usbW / 2.0;
      drawMetalConnector(g2d, usbX, usbY, usbW, usbH, "USB");

      // EN & BOOT tactile buttons at bottom (flanking the Micro-USB port)
      g2d.setColor(BUTTON_BODY_COLOR);
      g2d.fill(new RoundRectangle2D.Double(btnLeftX, btnY, btnW, btnH, 3, 3));
      g2d.fill(new RoundRectangle2D.Double(btnRightX, btnY, btnW, btnH, 3, 3));
      g2d.setColor(BUTTON_BORDER_COLOR);
      g2d.draw(new RoundRectangle2D.Double(btnLeftX, btnY, btnW, btnH, 3, 3));
      g2d.draw(new RoundRectangle2D.Double(btnRightX, btnY, btnW, btnH, 3, 3));

      // Button actuators
      g2d.setColor(BUTTON_ACTUATOR_COLOR);
      g2d.fillOval((int) (btnLeftX + btnW / 2.0 - 4), (int) (btnY + btnH / 2.0 - 4), 8, 8);
      g2d.fillOval((int) (btnRightX + btnW / 2.0 - 4), (int) (btnY + btnH / 2.0 - 4), 8, 8);

      g2d.setColor(SILK_COLOR);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "EN", btnLeftX + btnW / 2.0, btnY - 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "BOOT", btnRightX + btnW / 2.0, btnY - 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Silkscreen
      g2d.setFont(SILK_FONT);
      String silkText = (version == DevKitVersion.DevKit_V1_30Pin) ? "ESP32 DevKit V1" : "ESP32 DevKitC V4";
      StringUtils.drawCenteredText(g2d, silkText, x + rowSpacing / 2.0, shieldY + shieldH + 45, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
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
