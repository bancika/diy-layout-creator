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

@ComponentDescriptor(name = "Wemos D1 Mini", category = "Controllers",
    author = "Branislav Stojkovic", description = "Wemos D1 Mini ESP8266 Wi-Fi Development Board",
    instanceNamePrefix = "MCU", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class WemosD1Mini extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color WEMOS_BLUE = Color.decode("#006699");
  public static Color ANTENNA_COLOR = Color.decode("#DAA520");
  public static Color ANTENNA_BG_COLOR = Color.decode("#1E1E1E");
  public static Color BUTTON_BODY_COLOR = Color.decode("#383838");
  public static Color BUTTON_BORDER_COLOR = Color.decode("#666666");
  public static Color BUTTON_ACTUATOR_COLOR = Color.decode("#A0A0A0");
  public static Color SILK_COLOR = Color.WHITE;

  public static Size BOARD_WIDTH = new Size(1.0d, SizeUnit.in);
  public static Size BOARD_LENGTH = new Size(1.34d, SizeUnit.in);
  public static Size ROW_SPACING = new Size(0.9d, SizeUnit.in);
  public static Size TOP_MARGIN = new Size(0.275d, SizeUnit.in);
  public static Size TOP_ROUNDING = new Size(4.6d, SizeUnit.mm);
  public static Size BOTTOM_ROUNDING = new Size(0.8d, SizeUnit.mm);

  public static Size MODULE_WIDTH = new Size(15.0d, SizeUnit.mm);
  public static Size MODULE_LENGTH = new Size(7.0d, SizeUnit.mm);
  public static Size ANTENNA_WIDTH = new Size(15.0d, SizeUnit.mm);
  public static Size ANTENNA_LENGTH = new Size(5.5d, SizeUnit.mm);
  public static Size SHIELD_WIDTH = new Size(15.0d, SizeUnit.mm);
  public static Size SHIELD_LENGTH = new Size(14.54d, SizeUnit.mm);

  public static Size USB_WIDTH = new Size(5.8d, SizeUnit.mm);
  public static Size USB_LENGTH = new Size(3.3d, SizeUnit.mm);
  public static Size USB_CUTOUT_WIDTH = new Size(6.8d, SizeUnit.mm);
  public static Size USB_CUTOUT_DEPTH = new Size(1.8d, SizeUnit.mm);

  public static Size RST_CUTOUT_WIDTH = new Size(2.0d, SizeUnit.mm);
  public static Size RST_CUTOUT_LENGTH = new Size(6.5d, SizeUnit.mm);
  public static Size RST_CUTOUT_SLANT = new Size(2.0d, SizeUnit.mm);
  public static Size RST_BTN_WIDTH = new Size(1.0d, SizeUnit.mm);
  public static Size RST_BTN_LENGTH = new Size(2.0d, SizeUnit.mm);
  public static Size RST_BTN_OFFSET_Y = new Size(2.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      // Left row (pins 0..7)
      "RST", "A0", "D0 (GPIO16)", "D5 (GPIO14)", "D6 (GPIO12)", "D7 (GPIO13)", "D8 (GPIO15)", "3V3",
      // Right row (pins 8..15)
      "5V", "G (GND)", "D4 (GPIO2)", "D3 (GPIO0)", "D2 (GPIO4)", "D1 (GPIO5)", "RX (GPIO3)", "TX (GPIO1)"
  };

  public static final String[] SILK_PIN_NAMES_LEFT = new String[] {
      "RST", "A0", "D0", "D5", "D6", "D7", "D8", "3V3"
  };

  public static final String[] SILK_PIN_NAMES_RIGHT = new String[] {
      "TX", "RX", "D1", "D2", "D3", "D4", "G", "5V"
  };

  protected boolean headers = false;

  public WemosD1Mini() {
    super();
    this.bodyColor = WEMOS_BLUE;
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
    double rowSpacing = ROW_SPACING.convertToPixels();

    double[][] relativeOffsets = new double[16][2];
    for (int i = 0; i < 8; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
    }
    for (int i = 0; i < 8; i++) {
      relativeOffsets[8 + i][0] = rowSpacing;
      relativeOffsets[8 + i][1] = (7 - i) * spacing;
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

    double rTop = TOP_ROUNDING.convertToPixels();
    double rBottom = BOTTOM_ROUNDING.convertToPixels();
    double usbCutoutW = USB_CUTOUT_WIDTH.convertToPixels();
    double usbCutoutH = USB_CUTOUT_DEPTH.convertToPixels();
    double rstCutoutW = RST_CUTOUT_WIDTH.convertToPixels();
    double rstCutoutH = RST_CUTOUT_LENGTH.convertToPixels();
    double rstCutoutSlant = RST_CUTOUT_SLANT.convertToPixels();

    double cx = boardX + boardW / 2.0;
    double bottomY = boardY + boardH;
    double rightX = boardX + boardW;

    Path2D.Double path = new Path2D.Double();
    // Start at top-left, after top-left corner curve
    path.moveTo(boardX + rTop, boardY);
    // Top edge
    path.lineTo(rightX - rTop, boardY);
    // Top-right corner curve (larger radius)
    path.quadTo(rightX, boardY, rightX, boardY + rTop);
    // Right edge down to bottom-right corner
    path.lineTo(rightX, bottomY - rBottom);
    // Bottom-right corner curve (smaller radius)
    path.quadTo(rightX, bottomY, rightX - rBottom, bottomY);
    // Bottom edge towards USB cutout
    path.lineTo(cx + usbCutoutW / 2.0, bottomY);
    // USB cutout (slightly wider than the USB connector)
    path.lineTo(cx + usbCutoutW / 2.0, bottomY - usbCutoutH);
    path.lineTo(cx - usbCutoutW / 2.0, bottomY - usbCutoutH);
    path.lineTo(cx - usbCutoutW / 2.0, bottomY);
    // Bottom edge towards Reset button cutout
    path.lineTo(boardX + rstCutoutW, bottomY);
    // Reset button cutout inner vertical wall
    path.lineTo(boardX + rstCutoutW, bottomY - (rstCutoutH - rstCutoutSlant));
    // Slanted edge transitioning to outer board edge
    path.lineTo(boardX, bottomY - rstCutoutH);
    // Left edge up to top-left corner
    path.lineTo(boardX, boardY + rTop);
    // Top-left corner curve (larger radius)
    path.quadTo(boardX, boardY, boardX + rTop, boardY);
    path.closePath();

    return path;
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
    double bottomY = boardY + boardH;

    double moduleW = MODULE_WIDTH.convertToPixels();
    double moduleH = MODULE_LENGTH.convertToPixels();
    double moduleX = (x + rowSpacing / 2.0) - moduleW / 2.0;

    double shieldW = SHIELD_WIDTH.convertToPixels();
    double shieldH = SHIELD_LENGTH.convertToPixels();
    double shieldX = (x + rowSpacing / 2.0) - shieldW / 2.0;
    double shieldY = boardY + moduleH;

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
      // Antenna (dark rectangle underneath + gold serpentine trace)
      drawPcbAntenna(g2d, moduleX, boardY, moduleW, moduleH);

      // ESP8266 Metal Shield (main chip module)
      drawMetalConnector(g2d, shieldX, shieldY, shieldW, shieldH, "ESP8266");

      // Micro-USB Jack drawn flush with the bottom edge of the board
      double usbW = USB_WIDTH.convertToPixels();
      double usbH = USB_LENGTH.convertToPixels();
      double usbX = (x + rowSpacing / 2.0) - usbW / 2.0;
      double usbY = bottomY - usbH;
      drawMetalConnector(g2d, usbX, usbY, usbW, usbH, "USB");

      // Reset button: small black rectangle touching the board in the bottom left cutout, moved 2mm up
      double rstCutoutW = RST_CUTOUT_WIDTH.convertToPixels();
      double btnW = RST_BTN_WIDTH.convertToPixels();
      double btnH = RST_BTN_LENGTH.convertToPixels();
      double btnOffsetY = RST_BTN_OFFSET_Y.convertToPixels();
      double btnX = boardX + rstCutoutW - btnW;
      double btnY = bottomY - btnH - btnOffsetY;

      g2d.setColor(BUTTON_BODY_COLOR);
      g2d.fill(new Rectangle2D.Double(btnX, btnY, btnW, btnH));

      // Reset button label on the board next to the button
      g2d.setColor(SILK_COLOR);
      g2d.setFont(new Font("SansSerif", Font.BOLD, 9));
      double rstLabelX = boardX + rstCutoutW + new Size(0.4d, SizeUnit.mm).convertToPixels();
      StringUtils.drawCenteredText(g2d, "RST", rstLabelX, btnY + btnH / 2.0,
          HorizontalAlignment.LEFT, VerticalAlignment.CENTER);

      // Silkscreen: D1 mini label
      g2d.setFont(SILK_FONT);
      double labelY = shieldY + shieldH + new Size(2.5d, SizeUnit.mm).convertToPixels();
      StringUtils.drawCenteredText(g2d, "D1 mini", x + rowSpacing / 2.0, labelY,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Silkscreen: Pin labels
      g2d.setFont(new Font("SansSerif", Font.BOLD, 9));
      double spacing = PIN_SPACING.convertToPixels();
      double labelOffset = new Size(1.4d, SizeUnit.mm).convertToPixels();
      for (int i = 0; i < 8; i++) {
        double pinY = y + i * spacing;
        // Left pin labels
        StringUtils.drawCenteredText(g2d, SILK_PIN_NAMES_LEFT[i], x + labelOffset, pinY,
            HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        // Right pin labels
        StringUtils.drawCenteredText(g2d, SILK_PIN_NAMES_RIGHT[i], x + rowSpacing - labelOffset, pinY,
            HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      }
    }

    g2d.setTransform(oldTx);

    if (headers) {
      drawPinHeader(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);
    } else {
      drawPcbSolderPads(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);
    }

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    Path2D.Double iconShape = new Path2D.Double();
    double rT = 8.0;
    double rB = 2.0;
    iconShape.moveTo(5 + rT, 3);
    iconShape.lineTo(width - 5 - rT, 3);
    iconShape.quadTo(width - 5, 3, width - 5, 3 + rT);
    iconShape.lineTo(width - 5, height - 4 - rB);
    iconShape.quadTo(width - 5, height - 4, width - 5 - rB, height - 4);
    iconShape.lineTo(width / 2.0 + 4, height - 4);
    iconShape.lineTo(width / 2.0 + 4, height - 7);
    iconShape.lineTo(width / 2.0 - 4, height - 7);
    iconShape.lineTo(width / 2.0 - 4, height - 4);
    iconShape.lineTo(8, height - 4);
    iconShape.lineTo(8, height - 9);
    iconShape.lineTo(5, height - 12);
    iconShape.lineTo(5, 3 + rT);
    iconShape.quadTo(5, 3, 5 + rT, 3);
    iconShape.closePath();

    g2d.setColor(WEMOS_BLUE);
    g2d.fill(iconShape);
    g2d.setColor(WEMOS_BLUE.darker());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(iconShape);

    // Black rectangle from top of board down to shield
    g2d.setColor(Color.BLACK);
    g2d.fillRect(8, 3, width - 16, 7);

    // Antenna trace
    g2d.setColor(ANTENNA_COLOR);
    g2d.drawLine(10, 4, 12, 4);
    g2d.drawLine(12, 4, 12, 6);
    g2d.drawLine(12, 6, 16, 6);
    g2d.drawLine(16, 6, 16, 4);
    g2d.drawLine(16, 4, 20, 4);
    g2d.drawLine(20, 4, 20, 6);
    g2d.drawLine(20, 6, 22, 6);

    // Metal shield
    g2d.setColor(METAL_SHIELD_COLOR);
    g2d.fill(new RoundRectangle2D.Double(8, 10, width - 16, 11, 2, 2));

    // Reset button in icon
    g2d.setColor(Color.BLACK);
    g2d.fillRect(6, height - 8, 2, 3);

    // USB connector flush with bottom edge
    g2d.setColor(USB_METAL_COLOR);
    g2d.fill(new RoundRectangle2D.Double(width / 2.0 - 3, height - 8, 6, 4, 1, 1));

    // Text
    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "D1", width / 2.0, height / 2.0 + 9, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
