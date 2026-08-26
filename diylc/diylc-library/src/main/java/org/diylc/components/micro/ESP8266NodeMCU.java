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

@ComponentDescriptor(name = "ESP8266 NodeMCU", category = "Controllers",
    author = "Branislav Stojkovic", description = "ESP8266 NodeMCU V2/V3 Wi-Fi Development Board",
    instanceNamePrefix = "MCU", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class ESP8266NodeMCU extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color NODEMCU_BLACK = Color.decode("#3E3E3E");
  public static Color ANTENNA_COLOR = Color.decode("#DAA520");
  public static Color ANTENNA_BG_COLOR = Color.decode("#1E1E1E");
  public static Color BUTTON_BODY_COLOR = Color.decode("#383838");
  public static Color BUTTON_BORDER_COLOR = Color.decode("#666666");
  public static Color BUTTON_ACTUATOR_COLOR = Color.decode("#A0A0A0");
  public static Color SILK_COLOR = Color.WHITE;
  public static Size BOARD_WIDTH = new Size(25.7d, SizeUnit.mm);
  public static Size BOARD_LENGTH = new Size(48.0d, SizeUnit.mm);
  public static Size TOP_MARGIN = new Size(6.22d, SizeUnit.mm);
  public static Size ANTENNA_LENGTH = new Size(7.0d, SizeUnit.mm);
  public static Size ANTENNA_WIDTH = new Size(15.0d, SizeUnit.mm);
  public static Size SHIELD_WIDTH = new Size(15.0d, SizeUnit.mm);
  public static Size SHIELD_LENGTH = new Size(15.0d, SizeUnit.mm);
  public static Size ROW_SPACING = new Size(0.9d, SizeUnit.in);
  public static Size HOLE_DISTANCE_X = new Size(21.0d, SizeUnit.mm);
  public static Size HOLE_DISTANCE_Y = new Size(44.0d, SizeUnit.mm);
  public static Size HOLE_DIAMETER = new Size(3.2d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      // Left row (pins 0..14)
      "A0 (ADC0)", "RSV1", "RSV2", "SD3", "SD2", "SD1", "CMD", "SD0", "CLK", "GND1", "3V3_1", "EN", "RST", "GND2", "VIN",
      // Right row (pins 15..29)
      "D0 (GPIO16)", "D1 (GPIO5)", "D2 (GPIO4)", "D3 (GPIO0)", "D4 (GPIO2)", "3V3_2", "GND3", "D5 (GPIO14)", "D6 (GPIO12)", "D7 (GPIO13)", "D8 (GPIO15)", "RX (GPIO3)", "TX (GPIO1)", "GND4", "3V3_3"
  };

  protected boolean headers = false;

  public ESP8266NodeMCU() {
    super();
    this.bodyColor = NODEMCU_BLACK;
    updateControlPoints();
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
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();
    double rowSpacing = ROW_SPACING.convertToPixels(); // 180px

    double[][] relativeOffsets = new double[30][2];
    for (int i = 0; i < 15; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
    }
    for (int i = 0; i < 15; i++) {
      relativeOffsets[15 + i][0] = rowSpacing;
      relativeOffsets[15 + i][1] = i * spacing;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double rowSpacing = ROW_SPACING.convertToPixels();
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_LENGTH.convertToPixels();
    double topMargin = TOP_MARGIN.convertToPixels();
    double boardX = (x + rowSpacing / 2.0) - boardW / 2.0;
    double boardY = y - topMargin;
    return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 8, 8);
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
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_LENGTH.convertToPixels();
    double topMargin = TOP_MARGIN.convertToPixels();
    double boardX = (x + rowSpacing / 2.0) - boardW / 2.0;
    double boardY = y - topMargin;

    double antennaW = ANTENNA_WIDTH.convertToPixels();
    double antennaH = ANTENNA_LENGTH.convertToPixels();
    double antennaX = (x + rowSpacing / 2.0) - antennaW / 2.0;
    double antennaY = boardY;

    double shieldW = SHIELD_WIDTH.convertToPixels();
    double shieldH = SHIELD_LENGTH.convertToPixels();
    double shieldX = (x + rowSpacing / 2.0) - shieldW / 2.0;
    double shieldY = antennaY + antennaH;

    Shape boardShape = getBodyShape();

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(boardShape);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      // 4 Corner Mounting Holes (diameter 3.2mm, spaced 44mm lengthwise, 21mm widthwise)
      double holeDiameter = HOLE_DIAMETER.convertToPixels();
      double holeDistX = HOLE_DISTANCE_X.convertToPixels();
      double holeDistY = HOLE_DISTANCE_Y.convertToPixels();
      double topHoleY = boardY + (boardH - holeDistY) / 2.0;
      double bottomHoleY = boardY + (boardH + holeDistY) / 2.0;
      double leftHoleX = (x + rowSpacing / 2.0) - holeDistX / 2.0;
      double rightHoleX = (x + rowSpacing / 2.0) + holeDistX / 2.0;

      drawMountingHole(g2d, leftHoleX, topHoleY, holeDiameter);
      drawMountingHole(g2d, rightHoleX, topHoleY, holeDiameter);
      drawMountingHole(g2d, leftHoleX, bottomHoleY, holeDiameter);
      drawMountingHole(g2d, rightHoleX, bottomHoleY, holeDiameter);

      // Antenna (dark rectangle underneath + gold serpentine trace)
      drawPcbAntenna(g2d, antennaX, antennaY, antennaW, antennaH);

      // ESP-12 Metal shield module below antenna
      drawMetalConnector(g2d, shieldX, shieldY, shieldW, shieldH, "ESP8266");

      // Micro-USB Jack at bottom
      double usbW = 54;
      double usbH = 32;
      double usbX = (x + rowSpacing / 2.0) - usbW / 2.0;
      double usbY = boardY + boardH - 22;
      drawMetalConnector(g2d, usbX, usbY, usbW, usbH, "USB");

      // RST & FLASH tactile buttons at bottom (flanking the Micro-USB port)
      double btnW = 18;
      double btnH = 18;
      double btnY = boardY + boardH - 25;
      double shift1mm = new Size(1.0d, SizeUnit.mm).convertToPixels();
      double btnLeftX = leftHoleX + 10 + shift1mm;
      double btnRightX = rightHoleX - 10 - shift1mm - btnW;

      g2d.setColor(BUTTON_BODY_COLOR);
      g2d.fill(new RoundRectangle2D.Double(btnLeftX, btnY, btnW, btnH, 3, 3));
      g2d.fill(new RoundRectangle2D.Double(btnRightX, btnY, btnW, btnH, 3, 3));
      g2d.setColor(BUTTON_BORDER_COLOR);
      g2d.draw(new RoundRectangle2D.Double(btnLeftX, btnY, btnW, btnH, 3, 3));
      g2d.draw(new RoundRectangle2D.Double(btnRightX, btnY, btnW, btnH, 3, 3));

      // Button actuators
      g2d.setColor(BUTTON_ACTUATOR_COLOR);
      g2d.fillOval((int) (btnLeftX + btnW / 2.0 - 3.5), (int) (btnY + btnH / 2.0 - 3.5), 7, 7);
      g2d.fillOval((int) (btnRightX + btnW / 2.0 - 3.5), (int) (btnY + btnH / 2.0 - 3.5), 7, 7);

      g2d.setColor(SILK_COLOR);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "RST", btnLeftX + btnW / 2.0, btnY - 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "FLASH", btnRightX + btnW / 2.0, btnY - 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Silkscreen
      g2d.setFont(SILK_FONT);
      double labelY = shieldY + shieldH + new Size(4.0d, SizeUnit.mm).convertToPixels();
      StringUtils.drawCenteredText(g2d, "NodeMCU", x + rowSpacing / 2.0, labelY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    if (headers) {
      drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);
    } else {
      drawPcbSolderPads(g2d, 0, controlPoints.length, true, outlineMode, drawingObserver);
    }

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(NODEMCU_BLACK);
    g2d.fill(new RoundRectangle2D.Double(5, 2, width - 10, height - 4, 3, 3));
    g2d.setColor(Color.GRAY);
    g2d.draw(new RoundRectangle2D.Double(5, 2, width - 10, height - 4, 3, 3));

    // Metal shield
    g2d.setColor(METAL_SHIELD_COLOR);
    g2d.fillRect(8, 6, width - 16, 12);

    g2d.setColor(SILK_COLOR);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "8266", width / 2, height / 2 + 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
