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
 * along with DIYLC. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.components.modules;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
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
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Current/Power Sensor (INA219)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "INA219 I2C High-Side Current and Power Sensor Breakout",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class INA219CurrentSensor extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color PCB_GREEN = Color.decode("#115E1B");
  public static Color TERMINAL_BLUE = Color.decode("#1565C0");
  public static Color TERMINAL_BLUE_BORDER = Color.decode("#0D47A1");
  public static Color SHUNT_BODY = Color.decode("#7D6608");
  public static Color SHUNT_METAL = Color.decode("#D4AC0D");
  public static Color SHUNT_CORE = Color.decode("#2C3E50");
  public static Color SILK_YELLOW = Color.decode("#F4D03F");

  public static Size BOARD_WIDTH = new Size(25.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(25.0d, SizeUnit.mm);

  // 6 header pins at bottom (0-5) + 2 screw terminal pins at top (6-7)
  private static final String[] PIN_NAMES = {
      "VCC", "GND", "SCL", "SDA", "VIN-", "VIN+",
      "VIN- (Term)", "VIN+ (Term)"
  };

  public INA219CurrentSensor() {
    super();
    this.bodyColor = PCB_GREEN;
    updateControlPoints();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return Integer.toString(index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();

    // 8 control points total:
    // Pins 0-5: Bottom horizontal 6-pin header (VCC, GND, SCL, SDA, VIN-, VIN+)
    // Pins 6-7: Top 2-pin screw terminal block (VIN-, VIN+)
    double termSpacing = new Size(5.08d, SizeUnit.mm).convertToPixels();
    double termCenterX = 2.5 * spacing;
    double termY = -boardH + 24.0;

    double[][] relativeOffsets = new double[][] {
      { 0, 0 },
      { spacing, 0 },
      { spacing * 2, 0 },
      { spacing * 3, 0 },
      { spacing * 4, 0 },
      { spacing * 5, 0 },
      { termCenterX - termSpacing / 2.0, termY },
      { termCenterX + termSpacing / 2.0, termY }
    };

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double spacing = PIN_SPACING.convertToPixels();
    double boardX = x - (boardW - 5 * spacing) / 2.0;
    double boardY = y - boardH + 12;
    return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 6, 6);
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

    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double spacing = PIN_SPACING.convertToPixels();
    double boardX = x - (boardW - 5 * spacing) / 2.0;
    double boardY = y - boardH + 12;

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
      // 4 Large Gold Ring Mounting Holes in corners (20mm x 20mm pitch)
      double holeMargin = 10.0;
      drawMountingHole(g2d, boardX + holeMargin, boardY + holeMargin, 13.0);
      drawMountingHole(g2d, boardX + boardW - holeMargin, boardY + holeMargin, 13.0);
      drawMountingHole(g2d, boardX + holeMargin, boardY + boardH - holeMargin, 13.0);
      drawMountingHole(g2d, boardX + boardW - holeMargin, boardY + boardH - holeMargin, 13.0);

      // Top Blue Screw Terminal Block (5.08mm 2-pin block)
      double termW = 44.0;
      double termH = 30.0;
      double termX = boardX + (boardW - termW) / 2.0;
      double termY = boardY + 2.0;

      // Blue outer block body
      g2d.setColor(TERMINAL_BLUE);
      g2d.fill(new RoundRectangle2D.Double(termX, termY, termW, termH, 3, 3));
      g2d.setColor(TERMINAL_BLUE_BORDER);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.draw(new RoundRectangle2D.Double(termX, termY, termW, termH, 3, 3));

      // Terminal block wire entry slots on top
      g2d.setColor(Color.decode("#0D47A1"));
      g2d.fillRect((int) (termX + 4), (int) (termY + 1), (int) (termW - 8), 4);

      // Screw contacts
      double termSpacing = new Size(5.08d, SizeUnit.mm).convertToPixels();
      double screw1X = termX + termW / 2.0 - termSpacing / 2.0;
      double screw2X = termX + termW / 2.0 + termSpacing / 2.0;
      double screwY = termY + termH / 2.0 + 2.0;
      double screwD = 14.0;

      g2d.setColor(SCREW_CIRCLE_COLOR);
      g2d.fill(new Ellipse2D.Double(screw1X - screwD / 2.0, screwY - screwD / 2.0, screwD, screwD));
      g2d.fill(new Ellipse2D.Double(screw2X - screwD / 2.0, screwY - screwD / 2.0, screwD, screwD));
      g2d.setColor(SCREW_CIRCLE_COLOR.darker());
      g2d.draw(new Ellipse2D.Double(screw1X - screwD / 2.0, screwY - screwD / 2.0, screwD, screwD));
      g2d.draw(new Ellipse2D.Double(screw2X - screwD / 2.0, screwY - screwD / 2.0, screwD, screwD));
      // Screw diagonal slots
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
      g2d.drawLine((int) (screw1X - 4), (int) (screwY - 4), (int) (screw1X + 4), (int) (screwY + 4));
      g2d.drawLine((int) (screw2X - 4), (int) (screwY - 4), (int) (screw2X + 4), (int) (screwY + 4));

      // Shunt Resistor (Center-Upper, directly below terminal block)
      double shuntW = 32.0;
      double shuntH = 14.0;
      double shuntX = boardX + (boardW - shuntW) / 2.0;
      double shuntY = boardY + termH + 6.0;

      // Copper outer tabs
      g2d.setColor(Color.decode("#C0392B"));
      g2d.fill(new RoundRectangle2D.Double(shuntX, shuntY, 7, shuntH, 2, 2));
      g2d.fill(new RoundRectangle2D.Double(shuntX + shuntW - 7, shuntY, 7, shuntH, 2, 2));

      // Metal sense element (arched center)
      g2d.setColor(Color.decode("#BDC3C7"));
      g2d.fill(new Rectangle2D.Double(shuntX + 6, shuntY + 1, shuntW - 12, shuntH - 2));
      g2d.setColor(Color.decode("#7F8C8D"));
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.draw(new Rectangle2D.Double(shuntX + 6, shuntY + 1, shuntW - 12, shuntH - 2));

      // "R100" label on shunt
      g2d.setColor(Color.decode("#2C3E50"));
      g2d.setFont(new Font("SansSerif", Font.BOLD, 7));
      StringUtils.drawCenteredText(g2d, "R100", shuntX + shuntW / 2.0, shuntY + shuntH / 2.0,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // INA219 SOIC-8 IC (Center-Lower, directly below shunt)
      double icW = 20.0;
      double icH = 14.0;
      double icX = boardX + (boardW - icW) / 2.0;
      double icY = shuntY + shuntH + 4.0;

      // IC body
      g2d.setColor(Color.decode("#1C2833"));
      g2d.fill(new RoundRectangle2D.Double(icX, icY, icW, icH, 2, 2));
      g2d.setColor(Color.decode("#111111"));
      g2d.draw(new RoundRectangle2D.Double(icX, icY, icW, icH, 2, 2));

      // 8 silver IC pins (4 top, 4 bottom)
      g2d.setColor(Color.decode("#BDC3C7"));
      for (int i = 0; i < 4; i++) {
        double px = icX + 2.5 + i * 4.5;
        g2d.fillRect((int) px, (int) (icY - 2), 2, 2);
        g2d.fillRect((int) px, (int) (icY + icH), 2, 2);
      }
      // Pin 1 dot
      g2d.setColor(Color.decode("#F4D03F"));
      g2d.fill(new Ellipse2D.Double(icX + 2.5, icY + 2.5, 2.5, 2.5));

      // Vertical silkscreen text on left edge
      AffineTransform preTextTx = g2d.getTransform();
      g2d.translate(boardX + 14, boardY + boardH / 2.0);
      g2d.rotate(-Math.PI / 2.0);
      g2d.setColor(SILK_YELLOW);
      g2d.setFont(new Font("SansSerif", Font.BOLD, 7));
      StringUtils.drawCenteredText(g2d, "INA219 SENSOR", 0, 0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      g2d.setTransform(preTextTx);

      // Vertical silkscreen labels above bottom 6 header pins (VCC, GND, SCL, SDA, VIN-, VIN+)
      String[] headerLabels = new String[] {"VCC", "GND", "SCL", "SDA", "VIN-", "VIN+"};
      g2d.setFont(new Font("SansSerif", Font.BOLD, 7));
      g2d.setColor(SILK_YELLOW);

      for (int i = 0; i < 6; i++) {
        double pinX = x + i * spacing;
        AffineTransform pTx = g2d.getTransform();
        g2d.translate(pinX, y - 11);
        g2d.rotate(-Math.PI / 2.0);
        StringUtils.drawCenteredText(g2d, headerLabels[i], 0, 0, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        g2d.setTransform(pTx);
      }
    }

    g2d.setTransform(oldTx);

    // Draw bottom 6 header pins
    drawPins(g2d, 0, 6, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    // Green square PCB
    g2d.setColor(PCB_GREEN);
    g2d.fill(new RoundRectangle2D.Double(2, 2, width - 4, height - 4, 3, 3));
    g2d.setColor(PCB_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 2, width - 4, height - 4, 3, 3));

    // Blue terminal block at top
    g2d.setColor(TERMINAL_BLUE);
    g2d.fillRoundRect(width / 2 - 8, 3, 16, 8, 2, 2);

    // Shunt in middle
    g2d.setColor(Color.decode("#C0392B"));
    g2d.fillRect(width / 2 - 6, 13, 12, 4);
    g2d.setColor(Color.decode("#BDC3C7"));
    g2d.fillRect(width / 2 - 3, 13, 6, 4);

    // Small IC below shunt
    g2d.setColor(Color.decode("#1C2833"));
    g2d.fillRect(width / 2 - 4, 19, 8, 5);

    // 6 pin dots at bottom
    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < 6; i++) {
      g2d.fillRect(4 + i * 4, height - 5, 2, 2);
    }
  }
}
