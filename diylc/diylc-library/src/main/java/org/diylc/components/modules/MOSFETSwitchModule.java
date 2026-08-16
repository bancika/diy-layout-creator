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

@ComponentDescriptor(name = "MOSFET Driver Module (IRF520)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "IRF520 High-Current DC MOSFET Power Switch Driver Module",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class MOSFETSwitchModule extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color MOSFET_RED = Color.decode("#922B21");

  public static Size BOARD_WIDTH = new Size(33.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(24.0d, SizeUnit.mm);

  private static final String[] PIN_NAMES = {"SIG", "VCC", "GND", "VIN", "GND (Power)", "V+", "V-"};

  public MOSFETSwitchModule() {
    super();
    this.bodyColor = MOSFET_RED;
    updateControlPoints();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return Integer.toString(index + 1);
  }

  private static final Size TERMINAL_PITCH = new Size(5.08d, SizeUnit.mm);
  private static final double PAD_MARGIN_X = 15.0;

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();
    double termSpacing = TERMINAL_PITCH.convertToPixels();
    double w = BOARD_WIDTH.convertToPixels();
    double h = BOARD_HEIGHT.convertToPixels();

    // 7 pins total:
    // Pin 0..2: Control header (SIG, VCC, GND on left, 2.54mm pitch)
    // Pin 3, 4: VIN, GND (Power in screw terminal, top right, 5.08mm pitch)
    // Pin 5, 6: V+, V- (Load out screw terminal, bottom right, 5.08mm pitch)
    double rightX = w - PAD_MARGIN_X - 18.0;
    double topTermY = 18.0;
    double btmTermY = h - 18.0 - termSpacing;

    double[][] relativeOffsets = new double[][] {
      { 0, 0 },                         // Pin 0: SIG
      { 0, spacing },                   // Pin 1: VCC
      { 0, spacing * 2 },               // Pin 2: GND
      { rightX, topTermY - (h - 2 * spacing) / 2.0 },               // Pin 3: VIN
      { rightX, topTermY + termSpacing - (h - 2 * spacing) / 2.0 }, // Pin 4: GND (Power)
      { rightX, btmTermY - (h - 2 * spacing) / 2.0 },               // Pin 5: V+
      { rightX, btmTermY + termSpacing - (h - 2 * spacing) / 2.0 }  // Pin 6: V-
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
    double boardX = x - PAD_MARGIN_X;
    double boardY = y - (boardH - 2 * PIN_SPACING.convertToPixels()) / 2.0;
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
    double boardX = x - PAD_MARGIN_X;
    double boardY = y - (boardH - 2 * PIN_SPACING.convertToPixels()) / 2.0;

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
      // 2 Mounting Holes
      drawMountingHole(g2d, boardX + 14, boardY + 14, 14);
      drawMountingHole(g2d, boardX + 14, boardY + boardH - 14, 14);

      // TO-220 Power MOSFET (Center)
      double mosX = boardX + 50;
      double mosY = boardY + boardH / 2.0 - 24;
      g2d.setColor(Color.decode("#222222"));
      g2d.fill(new RoundRectangle2D.Double(mosX, mosY, 36, 48, 3, 3));
      // Heatsink tab
      g2d.setColor(Color.decode("#BDC3C7"));
      g2d.fill(new RoundRectangle2D.Double(mosX, mosY - 10, 36, 12, 2, 2));
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "IRF520", mosX + 18, mosY + 24, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Gate Trigger Status LED (Red)
      g2d.setColor(Color.RED);
      g2d.fill(new RoundRectangle2D.Double(boardX + 32, boardY + boardH / 2.0 - 5, 10, 10, 2, 2));

      // Silk Screen Pin Labels
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      String[] labels = new String[] { "SIG", "VCC", "GND" };
      double spacing = PIN_SPACING.convertToPixels();
      for (int i = 0; i < 3; i++) {
        StringUtils.drawCenteredText(g2d, labels[i], x + 16, y + i * spacing, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
      }
      double termX = boardX + boardW - 42;
      StringUtils.drawCenteredText(g2d, "VIN", termX - 6, boardY + 36, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "OUT", termX - 6, boardY + boardH - 36, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw control header pins on left
    drawPins(g2d, 0, 3, false, outlineMode, drawingObserver);

    // Draw dual terminal blocks on right matching PCBTerminalBlock style
    drawTerminalBlock(g2d, 3, 2, false, 1, 38.0, outlineMode, drawingObserver);
    drawTerminalBlock(g2d, 5, 2, false, 1, 38.0, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(MOSFET_RED);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    g2d.setColor(Color.decode("#222222"));
    g2d.fillRect(width / 2 - 4, height / 2 - 6, 8, 12);
    g2d.setColor(SCREW_TERMINAL_COLOR);
    g2d.fillRect(width - 10, height / 2 - 6, 8, 12);
  }
}
