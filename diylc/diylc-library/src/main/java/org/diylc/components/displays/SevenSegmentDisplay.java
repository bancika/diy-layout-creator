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
package org.diylc.components.displays;

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
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "7-Segment Display", category = "Displays & Outputs",
    author = "Branislav Stojkovic", description = "7-Segment LED Display (1-Digit, 4-Digit, or TM1637 I2C Driver Module)",
    instanceNamePrefix = "DISP", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class SevenSegmentDisplay extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public enum DisplayType {
    SingleDigit_10Pin("1-Digit 0.56\" (10-Pin DIP)"),
    FourDigit_12Pin("4-Digit Bare (12-Pin DIP)"),
    TM1637_Module_4Pin("4-Digit TM1637 Module (4-Pin)");

    private final String label;
    DisplayType(String label) { this.label = label; }
    @Override public String toString() { return label; }
  }

  public static Color BODY_BLACK = Color.decode("#1C1C1C");
  public static Color LED_RED = Color.decode("#E53935");
  public static Color LED_OFF = Color.decode("#2E2E2E");

  public static final String[] PIN_NAMES_1DIGIT = new String[] {
      "e", "d", "COM1", "c", "DP", "b", "a", "COM2", "f", "g"
  };

  public static final String[] PIN_NAMES_4DIGIT = new String[] {
      "D1", "a", "f", "D2", "D3", "b", "e", "d", "DP", "c", "g", "D4"
  };

  public static final String[] PIN_NAMES_TM1637 = new String[] {"CLK", "DIO", "VCC", "GND"};

  private DisplayType displayType = DisplayType.SingleDigit_10Pin;
  private Color ledColor = LED_RED;

  public SevenSegmentDisplay() {
    super();
    this.bodyColor = BODY_BLACK;
    updateControlPoints();
  }

  @EditableProperty(name = "Type")
  public DisplayType getDisplayType() {
    return displayType;
  }

  public void setDisplayType(DisplayType displayType) {
    this.displayType = displayType;
    updateControlPoints();
    invalidateCache();
  }

  @EditableProperty(name = "LED Color")
  public Color getLedColor() {
    return ledColor;
  }

  public void setLedColor(Color ledColor) {
    this.ledColor = ledColor;
    invalidateCache();
  }

  @Override
  public String getControlPointNodeName(int index) {
    switch (displayType) {
      case SingleDigit_10Pin:
        if (index >= 0 && index < PIN_NAMES_1DIGIT.length) return PIN_NAMES_1DIGIT[index];
        break;
      case FourDigit_12Pin:
        if (index >= 0 && index < PIN_NAMES_4DIGIT.length) return PIN_NAMES_4DIGIT[index];
        break;
      case TM1637_Module_4Pin:
        if (index >= 0 && index < PIN_NAMES_TM1637.length) return PIN_NAMES_TM1637[index];
        break;
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    if (displayType == DisplayType.SingleDigit_10Pin) {
      double rowSpacing = new Size(0.6d, SizeUnit.in).convertToPixels(); // 120px
      double[][] relativeOffsets = new double[10][2];
      for (int i = 0; i < 5; i++) {
        relativeOffsets[i][0] = i * spacing;
        relativeOffsets[i][1] = 0;
      }
      for (int i = 0; i < 5; i++) {
        relativeOffsets[5 + i][0] = (4 - i) * spacing;
        relativeOffsets[5 + i][1] = -rowSpacing;
      }
      rotatePoints(firstPoint, relativeOffsets);
    } else if (displayType == DisplayType.FourDigit_12Pin) {
      double rowSpacing = new Size(0.6d, SizeUnit.in).convertToPixels();
      double[][] relativeOffsets = new double[12][2];
      for (int i = 0; i < 6; i++) {
        relativeOffsets[i][0] = i * spacing;
        relativeOffsets[i][1] = 0;
      }
      for (int i = 0; i < 6; i++) {
        relativeOffsets[6 + i][0] = (5 - i) * spacing;
        relativeOffsets[6 + i][1] = -rowSpacing;
      }
      rotatePoints(firstPoint, relativeOffsets);
    } else {
      // TM1637 Module 4-pin
      double[][] relativeOffsets = new double[4][2];
      for (int i = 0; i < 4; i++) {
        relativeOffsets[i][0] = i * spacing;
        relativeOffsets[i][1] = 0;
      }
      rotatePoints(firstPoint, relativeOffsets);
    }
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardW;
    double boardH;
    double boardX;
    double boardY;

    if (displayType == DisplayType.SingleDigit_10Pin) {
      boardW = new Size(12.5d, SizeUnit.mm).convertToPixels();
      boardH = new Size(19.0d, SizeUnit.mm).convertToPixels();
      boardX = x - 10;
      boardY = y - boardH + 15;
    } else if (displayType == DisplayType.FourDigit_12Pin) {
      boardW = new Size(30.0d, SizeUnit.mm).convertToPixels();
      boardH = new Size(19.0d, SizeUnit.mm).convertToPixels();
      boardX = x - (boardW - 5 * PIN_SPACING.convertToPixels()) / 2.0;
      boardY = y - boardH + 15;
    } else {
      boardW = new Size(42.0d, SizeUnit.mm).convertToPixels();
      boardH = new Size(24.0d, SizeUnit.mm).convertToPixels();
      boardX = x - (boardW - 3 * PIN_SPACING.convertToPixels()) / 2.0;
      boardY = y - boardH + 15;
    }

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

    double boardW;
    double boardH;
    double boardX;
    double boardY;

    if (displayType == DisplayType.SingleDigit_10Pin) {
      boardW = new Size(12.5d, SizeUnit.mm).convertToPixels();
      boardH = new Size(19.0d, SizeUnit.mm).convertToPixels();
      boardX = x - 10;
      boardY = y - boardH + 15;
    } else if (displayType == DisplayType.FourDigit_12Pin) {
      boardW = new Size(30.0d, SizeUnit.mm).convertToPixels();
      boardH = new Size(19.0d, SizeUnit.mm).convertToPixels();
      boardX = x - (boardW - 5 * PIN_SPACING.convertToPixels()) / 2.0;
      boardY = y - boardH + 15;
    } else {
      // TM1637 Module
      boardW = new Size(42.0d, SizeUnit.mm).convertToPixels();
      boardH = new Size(24.0d, SizeUnit.mm).convertToPixels();
      boardX = x - (boardW - 3 * PIN_SPACING.convertToPixels()) / 2.0;
      boardY = y - boardH + 15;
    }

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
      // Draw 7-segment digit(s)
      int digits = (displayType == DisplayType.SingleDigit_10Pin) ? 1 : 4;
      double digitW = (boardW - 20) / digits;
      for (int d = 0; d < digits; d++) {
        double dx = boardX + 10 + d * digitW;
        double dy = boardY + 18;
        double dw = digitW - 8;
        double dh = boardH - 45;

        // Draw 7 segments
        drawSevenSegmentDigit(g2d, dx, dy, dw, dh, "8", ledColor);
      }
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  private void drawSevenSegmentDigit(Graphics2D g2d, double x, double y, double w, double h, String charToDisplay, Color onColor) {
    g2d.setColor(onColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3f));

    // a (top)
    g2d.drawLine((int) (x + 5), (int) y, (int) (x + w - 5), (int) y);
    // b (top-right)
    g2d.drawLine((int) (x + w), (int) (y + 4), (int) (x + w), (int) (y + h / 2 - 2));
    // c (bottom-right)
    g2d.drawLine((int) (x + w), (int) (y + h / 2 + 2), (int) (x + w), (int) (y + h - 4));
    // d (bottom)
    g2d.drawLine((int) (x + 5), (int) (y + h), (int) (x + w - 5), (int) (y + h));
    // e (bottom-left)
    g2d.drawLine((int) x, (int) (y + h / 2 + 2), (int) x, (int) (y + h - 4));
    // f (top-left)
    g2d.drawLine((int) x, (int) (y + 4), (int) x, (int) (y + h / 2 - 2));
    // g (middle)
    g2d.drawLine((int) (x + 5), (int) (y + h / 2), (int) (x + w - 5), (int) (y + h / 2));

    // DP (decimal point)
    g2d.fill(new Ellipse2D.Double(x + w + 3, y + h - 3, 4, 4));
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BODY_BLACK);
    g2d.fill(new RoundRectangle2D.Double(6, 4, width - 12, height - 8, 3, 3));
    g2d.setColor(Color.DARK_GRAY);
    g2d.draw(new RoundRectangle2D.Double(6, 4, width - 12, height - 8, 3, 3));

    // Draw single '8'
    drawSevenSegmentDigit(g2d, 10, 8, width - 20, height - 16, "8", LED_RED);
  }
}
