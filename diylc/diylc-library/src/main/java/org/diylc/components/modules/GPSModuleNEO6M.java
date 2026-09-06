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
import java.awt.geom.Line2D;
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

@ComponentDescriptor(name = "GPS Module (NEO-6M)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "u-blox NEO-6M GPS Module with ceramic patch antenna",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class GPSModuleNEO6M extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color PCB_GREEN = Color.decode("#1A6B1A");
  public static Color ANTENNA_BEIGE = Color.decode("#D5C4A1");
  public static Color ANTENNA_BORDER = Color.decode("#928374");
  public static Color SHIELD_COLOR = Color.decode("#B8B8B8");
  public static Color SHIELD_BORDER = Color.decode("#888888");
  public static Color BATTERY_COLOR = Color.decode("#D0D3D4");

  public static Size BOARD_WIDTH = new Size(35.5d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(25.5d, SizeUnit.mm);

  // Standard GY-GPS6MV2 pinout from pin 1 to 4: VCC, RX, TX, GND
  private static final String[] PIN_NAMES = {"VCC", "RX", "TX", "GND"};

  public GPSModuleNEO6M() {
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

    // 4 pins along the left edge: VCC, RX, TX, GND
    double[][] relativeOffsets = new double[][] {
      { 0, 0 },
      { 0, spacing },
      { 0, spacing * 2 },
      { 0, spacing * 3 }
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
    double boardX = x - 14.0;
    double boardY = y - (boardH - 3 * spacing) / 2.0;
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
    double boardX = x - 14.0;
    double boardY = y - (boardH - 3 * spacing) / 2.0;

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
      // 4 Small Mounting holes in the corners
      double holeMargin = 7.0;
      drawMountingHole(g2d, boardX + holeMargin, boardY + holeMargin, 8.0);
      drawMountingHole(g2d, boardX + holeMargin, boardY + boardH - holeMargin, 8.0);
      drawMountingHole(g2d, boardX + boardW - holeMargin, boardY + holeMargin, 8.0);
      drawMountingHole(g2d, boardX + boardW - holeMargin, boardY + boardH - holeMargin, 8.0);

      // Silkscreen pin labels (VCC, RX, TX, GND) placed neatly next to left header
      g2d.setColor(Color.WHITE);
      g2d.setFont(new Font("SansSerif", Font.BOLD, 8));
      String[] pinLabels = new String[] {"VCC", "RX", "TX", "GND"};
      for (int i = 0; i < 4; i++) {
        StringUtils.drawCenteredText(g2d, pinLabels[i], x + 9, y + i * spacing, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
      }

      // NEO-6M metal shield can (center of board, strictly to the right of pin labels)
      double icShieldX = boardX + 46.0;
      double icShieldW = 42.0;
      double icShieldH = 36.0;
      double icShieldY = boardY + (boardH - icShieldH) / 2.0 - 8.0;

      g2d.setColor(SHIELD_COLOR);
      g2d.fill(new RoundRectangle2D.Double(icShieldX, icShieldY, icShieldW, icShieldH, 4, 4));
      g2d.setColor(SHIELD_BORDER);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.draw(new RoundRectangle2D.Double(icShieldX, icShieldY, icShieldW, icShieldH, 4, 4));

      g2d.setColor(Color.decode("#333333"));
      g2d.setFont(new Font("SansSerif", Font.BOLD, 8));
      StringUtils.drawCenteredText(g2d, "NEO-6M", icShieldX + icShieldW / 2.0, icShieldY + icShieldH / 2.0 - 4,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      g2d.setFont(new Font("SansSerif", Font.PLAIN, 6));
      StringUtils.drawCenteredText(g2d, "GPS", icShieldX + icShieldW / 2.0, icShieldY + icShieldH / 2.0 + 5,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Rechargeable backup coin cell battery (CR1220 / MS621) below shield
      double battD = 18.0;
      double battX = boardX + 58.0;
      double battY = boardY + boardH - battD - 7.0;
      g2d.setColor(BATTERY_COLOR);
      g2d.fill(new Ellipse2D.Double(battX, battY, battD, battD));
      g2d.setColor(BATTERY_COLOR.darker());
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.draw(new Ellipse2D.Double(battX, battY, battD, battD));

      // Ceramic patch antenna on right side (20x20mm)
      double antSize = new Size(18.0d, SizeUnit.mm).convertToPixels();
      double antX = boardX + boardW - antSize - 8.0;
      double antY = boardY + (boardH - antSize) / 2.0;

      g2d.setColor(ANTENNA_BEIGE);
      g2d.fill(new RoundRectangle2D.Double(antX, antY, antSize, antSize, 2, 2));
      g2d.setColor(ANTENNA_BORDER);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.2f));
      g2d.draw(new RoundRectangle2D.Double(antX, antY, antSize, antSize, 2, 2));

      // Center silver electrode dot on antenna
      double antCX = antX + antSize / 2.0;
      double antCY = antY + antSize / 2.0;
      g2d.setColor(Color.decode("#7F8C8D"));
      g2d.fill(new Ellipse2D.Double(antCX - 2, antCY - 2, 4, 4));

      // Status LED (top left area)
      g2d.setColor(Color.RED);
      g2d.fill(new Ellipse2D.Double(boardX + 24.0, boardY + 8.0, 5, 5));
    }

    g2d.setTransform(oldTx);
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);
    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    // Green PCB
    g2d.setColor(PCB_GREEN);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    g2d.setColor(PCB_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));

    // Beige ceramic patch antenna (right half)
    int antS = height - 12;
    g2d.setColor(ANTENNA_BEIGE);
    g2d.fillRect(width - antS - 3, (height - antS) / 2, antS, antS);
    g2d.setColor(ANTENNA_BORDER);
    g2d.drawRect(width - antS - 3, (height - antS) / 2, antS, antS);

    // Shield
    g2d.setColor(SHIELD_COLOR);
    g2d.fillRoundRect(4, (height - 10) / 2, 10, 10, 2, 2);
  }
}
