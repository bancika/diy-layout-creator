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

@ComponentDescriptor(name = "Raspberry Pi Zero", category = "Controllers",
    author = "Branislav Stojkovic", description = "Raspberry Pi Zero / Zero W Compact Single Board Computer",
    instanceNamePrefix = "SBC", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class RaspberryPiZero extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color RPI_GREEN = Color.decode("#1B5E20");
  public static Size BOARD_WIDTH = new Size(65.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(30.0d, SizeUnit.mm);

  public static Color PAD_COLOR = GOLD_COLOR;
  public static Size PAD_SIZE = new Size(0.065d, SizeUnit.in);
  public static Size HOLE_SIZE = new Size(0.7d, SizeUnit.mm);

  public RaspberryPiZero() {
    super();
    this.bodyColor = RPI_GREEN;
    updateControlPoints();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < RaspberryPi.PIN_NAMES.length) {
      return RaspberryPi.PIN_NAMES[index];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    double[][] relativeOffsets = new double[40][2];
    for (int col = 0; col < 20; col++) {
      int pinOdd = col * 2;      // Pin 1, 3, 5... (bottom/inner row of header)
      int pinEven = col * 2 + 1; // Pin 2, 4, 6... (top/outer row of header)
      relativeOffsets[pinOdd][0] = col * spacing;
      relativeOffsets[pinOdd][1] = 0;
      relativeOffsets[pinEven][0] = col * spacing;
      relativeOffsets[pinEven][1] = -spacing;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double pin1OffsetX = new Size(8.37d, SizeUnit.mm).convertToPixels();
    double pin1OffsetY = new Size(3.5d, SizeUnit.mm).convertToPixels() + PIN_SPACING.convertToPixels() / 2.0;
    double boardX = x - pin1OffsetX;
    double boardY = y - pin1OffsetY;
    double cornerArc = new Size(6.0d, SizeUnit.mm).convertToPixels();

    Area boardArea = new Area(new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, cornerArc, cornerArc));

    double holeD = new Size(2.75d, SizeUnit.mm).convertToPixels();
    double holeR = holeD / 2.0;
    double mX1 = boardX + new Size(3.5d, SizeUnit.mm).convertToPixels();
    double mX2 = boardX + boardW - new Size(3.5d, SizeUnit.mm).convertToPixels();
    double mY1 = boardY + new Size(3.5d, SizeUnit.mm).convertToPixels();
    double mY2 = boardY + boardH - new Size(3.5d, SizeUnit.mm).convertToPixels();

    boardArea.subtract(new Area(new Ellipse2D.Double(mX1 - holeR, mY1 - holeR, holeD, holeD)));
    boardArea.subtract(new Area(new Ellipse2D.Double(mX1 - holeR, mY2 - holeR, holeD, holeD)));
    boardArea.subtract(new Area(new Ellipse2D.Double(mX2 - holeR, mY1 - holeR, holeD, holeD)));
    boardArea.subtract(new Area(new Ellipse2D.Double(mX2 - holeR, mY2 - holeR, holeD, holeD)));

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

    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double pin1OffsetX = new Size(8.37d, SizeUnit.mm).convertToPixels();
    double pin1OffsetY = new Size(3.5d, SizeUnit.mm).convertToPixels() + PIN_SPACING.convertToPixels() / 2.0;
    double boardX = x - pin1OffsetX;
    double boardY = y - pin1OffsetY;

    Shape boardShape = getBodyShape();

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(boardShape);

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      // 4 Mounting holes (diameter 2.75mm, located 3.5mm from board edges)
      double holeDiameter = new Size(2.75d, SizeUnit.mm).convertToPixels();
      drawMountingHole(g2d, boardX + new Size(3.5d, SizeUnit.mm).convertToPixels(),
          boardY + new Size(3.5d, SizeUnit.mm).convertToPixels(), holeDiameter);
      drawMountingHole(g2d, boardX + new Size(3.5d, SizeUnit.mm).convertToPixels(),
          boardY + boardH - new Size(3.5d, SizeUnit.mm).convertToPixels(), holeDiameter);
      drawMountingHole(g2d, boardX + boardW - new Size(3.5d, SizeUnit.mm).convertToPixels(),
          boardY + new Size(3.5d, SizeUnit.mm).convertToPixels(), holeDiameter);
      drawMountingHole(g2d, boardX + boardW - new Size(3.5d, SizeUnit.mm).convertToPixels(),
          boardY + boardH - new Size(3.5d, SizeUnit.mm).convertToPixels(), holeDiameter);

      // MicroSD card slot on the left edge
      double sdW = new Size(11.0d, SizeUnit.mm).convertToPixels();
      double sdH = new Size(12.0d, SizeUnit.mm).convertToPixels();
      double sdX = boardX + new Size(1.5d, SizeUnit.mm).convertToPixels();
      double sdY = boardY + new Size(7.5d, SizeUnit.mm).convertToPixels();
      drawMetalConnector(g2d, sdX, sdY, sdW, sdH, "SD");

      // Mini HDMI Connector (center at X = 12.4mm, width = 11.2mm, height = 7.5mm, overhang = 1.5mm)
      double hdmiW = new Size(11.2d, SizeUnit.mm).convertToPixels();
      double hdmiH = new Size(7.5d, SizeUnit.mm).convertToPixels();
      double hdmiX = boardX + new Size(12.4d, SizeUnit.mm).convertToPixels() - hdmiW / 2.0;
      double hdmiY = boardY + boardH - new Size(6.0d, SizeUnit.mm).convertToPixels();
      drawMetalConnector(g2d, hdmiX, hdmiY, hdmiW, hdmiH, "HDMI");

      // Micro USB Data (center at X = 41.4mm, width = 7.5mm, height = 5.6mm, overhang = 1.4mm)
      double usbW = new Size(7.5d, SizeUnit.mm).convertToPixels();
      double usbH = new Size(5.6d, SizeUnit.mm).convertToPixels();
      double usbX = boardX + new Size(41.4d, SizeUnit.mm).convertToPixels() - usbW / 2.0;
      double usbY = boardY + boardH - new Size(4.2d, SizeUnit.mm).convertToPixels();
      drawMetalConnector(g2d, usbX, usbY, usbW, usbH, "USB");

      // Micro USB Power (center at X = 54.0mm, width = 7.5mm, height = 5.6mm, overhang = 1.4mm)
      double pwrX = boardX + new Size(54.0d, SizeUnit.mm).convertToPixels() - usbW / 2.0;
      double pwrY = boardY + boardH - new Size(4.2d, SizeUnit.mm).convertToPixels();
      drawMetalConnector(g2d, pwrX, pwrY, usbW, usbH, "PWR");

      // Camera Connector (CSI) on right edge
      double csiW = new Size(3.2d, SizeUnit.mm).convertToPixels();
      double csiH = new Size(16.5d, SizeUnit.mm).convertToPixels();
      double csiX = boardX + boardW - new Size(4.2d, SizeUnit.mm).convertToPixels();
      double csiY = boardY + new Size(6.75d, SizeUnit.mm).convertToPixels();
      drawChip(g2d, csiX, csiY, csiW, csiH, "CSI");

      // Main SoC chip (Broadcom BCM2835 / RP3A0)
      double socW = new Size(12.0d, SizeUnit.mm).convertToPixels();
      double socH = new Size(12.0d, SizeUnit.mm).convertToPixels();
      double socX = boardX + new Size(19.5d, SizeUnit.mm).convertToPixels();
      double socY = boardY + new Size(11.5d, SizeUnit.mm).convertToPixels();
      drawChip(g2d, socX, socY, socW, socH, "");

      // Raspberry Pi logo on SoC chip
      double logoSize = new Size(7.0d, SizeUnit.mm).convertToPixels();
      double logoW = logoSize * (72.51 / 92.604);
      double logoX = socX + (socW - logoW) / 2.0;
      double logoY = socY + (socH - logoSize) / 2.0;
      drawRaspberryPiLogo(g2d, logoX, logoY, logoSize);

      // White silkscreen outline around GPIO pads
      g2d.setColor(Color.WHITE);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      double hdrBoxX = boardX + new Size(7.0d, SizeUnit.mm).convertToPixels();
      double hdrBoxY = boardY + new Size(0.9d, SizeUnit.mm).convertToPixels();
      double hdrBoxW = new Size(51.0d, SizeUnit.mm).convertToPixels();
      double hdrBoxH = new Size(5.2d, SizeUnit.mm).convertToPixels();
      g2d.draw(new Rectangle2D.Double(hdrBoxX, hdrBoxY, hdrBoxW, hdrBoxH));

      // Silkscreen "GPIO" label next to the top pins
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "GPIO", boardX + new Size(47.5d, SizeUnit.mm).convertToPixels(),
          boardY + new Size(7.8d, SizeUnit.mm).convertToPixels(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    drawingObserver.stopTracking();

    g2d.setTransform(oldTx);

    drawSolderPads(g2d, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  /**
   * Helper to draw GPIO solder pads (copper pads with drill holes, square for Pin 1).
   */
  protected void drawSolderPads(Graphics2D g2d, boolean outlineMode, IDrawingObserver drawingObserver) {
    if (outlineMode) return;
    int diameter = getClosestOdd((int) Math.round(PAD_SIZE.convertToPixels()));
    int holeDiameter = getClosestOdd((int) Math.round(HOLE_SIZE.convertToPixels()));

    drawingObserver.startTrackingContinuityArea(true);
    for (int i = 0; i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      if (i == 0) {
        // Pin 1 is a square solder pad
        g2d.setColor(PAD_COLOR);
        g2d.fill(new Rectangle2D.Double(p.getX() - diameter / 2.0, p.getY() - diameter / 2.0, diameter, diameter));
        g2d.setColor(PAD_COLOR.darker());
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new Rectangle2D.Double(p.getX() - diameter / 2.0, p.getY() - diameter / 2.0, diameter, diameter));
      } else {
        // Pins 2..40 are round solder pads
        g2d.setColor(PAD_COLOR);
        g2d.fill(new Ellipse2D.Double(p.getX() - diameter / 2.0, p.getY() - diameter / 2.0, diameter, diameter));
        g2d.setColor(PAD_COLOR.darker());
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new Ellipse2D.Double(p.getX() - diameter / 2.0, p.getY() - diameter / 2.0, diameter, diameter));
      }

      // Central drill hole
      g2d.setColor(Constants.CANVAS_COLOR);
      g2d.fill(new Ellipse2D.Double(p.getX() - holeDiameter / 2.0, p.getY() - holeDiameter / 2.0, holeDiameter, holeDiameter));
      g2d.setColor(PAD_COLOR.darker());
      g2d.draw(new Ellipse2D.Double(p.getX() - holeDiameter / 2.0, p.getY() - holeDiameter / 2.0, holeDiameter, holeDiameter));
    }
    drawingObserver.stopTrackingContinuityArea();
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(RPI_GREEN);
    g2d.fill(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 3, 3));
    g2d.setColor(RPI_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 3, 3));

    // GPIO Solder Pads in icon
    g2d.setColor(PAD_COLOR);
    for (int x = 6; x <= width - 8; x += 3) {
      g2d.fillRect(x, 7, 2, 2);
    }

    // SoC
    g2d.setColor(IC_BODY_COLOR);
    g2d.fillRect(10, 12, 6, 6);

    // Connectors on bottom
    g2d.setColor(USB_METAL_COLOR);
    g2d.fillRect(6, height - 7, 4, 3);
    g2d.fillRect(18, height - 7, 3, 3);
    g2d.fillRect(23, height - 7, 3, 3);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "ZERO", width / 2 + 4, 15, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
