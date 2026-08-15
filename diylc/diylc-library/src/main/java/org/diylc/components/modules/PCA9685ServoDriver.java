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

@ComponentDescriptor(name = "16-Channel PWM/Servo Driver (PCA9685)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "PCA9685 16-Channel 12-Bit PWM I2C Servo Controller Board",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class PCA9685ServoDriver extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color PCA_BLUE = Color.decode("#1B4F72");
  public static Color PWM_YELLOW = Color.decode("#F1C40F");
  public static Color V_RED = Color.decode("#E74C3C");
  public static Color GND_BLACK = Color.decode("#1C1C1C");
  public static Color CAP_BODY_COLOR = Color.decode("#1C2833");
  public static Color CAP_STRIPE_COLOR = Color.decode("#BDC3C7");
  public static Color CAP_BORDER_COLOR = Color.decode("#7F8C8D");
  public static Color SMD_BODY_COLOR = Color.decode("#2C3E50");
  public static Color SMD_ARRAY_COLOR = Color.decode("#34495E");
  public static Color GOLD_PAD_COLOR = Color.decode("#D4AC0D");
  public static Color POWER_LED_COLOR = Color.decode("#2ECC71");

  public static Font LABEL_FONT_BOLD = new Font("SansSerif", Font.BOLD, 9);
  public static Font LABEL_FONT_TINY = new Font("SansSerif", Font.PLAIN, 8);

  // Real PCA9685 board dimensions: ~62.5mm wide x 25.4mm tall
  public static Size BOARD_WIDTH = new Size(62.5d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(25.4d, SizeUnit.mm);

  private static final double EDGE_MARGIN = 15.0;

  // Pin name arrays
  private static final String[] LEFT_I2C_NAMES = {"GND", "OE", "SCL", "SDA", "VCC", "V+"};
  private static final String[] RIGHT_I2C_NAMES = {"GND", "OE", "SCL", "SDA", "VCC", "V+"};

  public PCA9685ServoDriver() {
    super();
    this.bodyColor = PCA_BLUE;
    updateControlPoints();
  }

  @Override
  public String getControlPointNodeName(int index) {
    // 0..5: Left I2C header
    if (index < 6) {
      return LEFT_I2C_NAMES[index];
    }
    // 6..11: Right I2C header
    if (index < 12) {
      return RIGHT_I2C_NAMES[index - 6];
    }
    // 12..13: Screw terminal (V+, GND)
    if (index == 12) return "V+ (Ext Power)";
    if (index == 13) return "GND (Ext Power)";
    // 14..61: 16 channels x 3 pins (PWM, V+, GND)
    if (index < 62) {
      int chanIdx = index - 14;
      int channel = chanIdx / 3;
      int row = chanIdx % 3;
      switch (row) {
        case 0: return "PWM" + channel;
        case 1: return "V+" + channel;
        case 2: return "GND" + channel;
      }
    }
    return Integer.toString(index + 1);
  }

  /**
   * Calculates the X coordinate of a specific channel column (0..15) relative to board center.
   * Channels are split into 4 distinct sections with 4 pins each:
   * Sec 0: ch 0..3 (left-outer)
   * Sec 1: ch 4..7 (left-inner)
   * Center Gap (housing IC and PWM/V+/GND labels)
   * Sec 2: ch 8..11 (right-inner)
   * Sec 3: ch 12..15 (right-outer)
   */
  private double getChannelColumnX(int col, double centerX) {
    int sec = col / 4;
    int colInSec = col % 4;

    switch (sec) {
      case 0: // Channels 0..3
        return centerX - 191.0 + colInSec * 20.0;
      case 1: // Channels 4..7
        return centerX - 95.0 + colInSec * 20.0;
      case 2: // Channels 8..11
        return centerX + 35.0 + colInSec * 20.0;
      case 3: // Channels 12..15
      default:
        return centerX + 131.0 + colInSec * 20.0;
    }
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double topMargin = (boardH - 5 * spacing) / 2.0;
    double centerX = boardW / 2.0 - EDGE_MARGIN;

    // 62 pins total:
    // Left I2C header (6 pins): 0..5 (GND, OE, SCL, SDA, VCC, V+)
    // Right I2C header (6 pins): 6..11
    // Screw terminal (2 pins): 12 = V+, 13 = GND (centered horizontally at top)
    // 16 Servo Channels x 3 rows (PWM, V+, GND) = 48 pins (14..61) in 4 sections
    double[][] relativeOffsets = new double[62][2];

    // Left I2C header — vertically centered, spaced equally from left edge
    for (int i = 0; i < 6; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
    }

    // Right I2C header — vertically centered, spaced equally from right edge
    double rightHeaderX = boardW - 2 * EDGE_MARGIN;
    for (int i = 0; i < 6; i++) {
      relativeOffsets[6 + i][0] = rightHeaderX;
      relativeOffsets[6 + i][1] = i * spacing;
    }

    // Screw terminal (V+, GND) — top center, pitch 28px (~3.5mm)
    double screwY = 25.0 - topMargin;
    relativeOffsets[12][0] = centerX - 14.0;
    relativeOffsets[12][1] = screwY;
    relativeOffsets[13][0] = centerX + 14.0;
    relativeOffsets[13][1] = screwY;

    // 16-Channel servo pins in 4 distinct sections along the bottom:
    // 3 rows: Row 0 (PWM / Yellow), Row 1 (V+ / Red), Row 2 (GND / Black)
    double row0_Y = 140.0 - topMargin;
    double row1_Y = 160.0 - topMargin;
    double row2_Y = 180.0 - topMargin;

    int idx = 14;
    for (int col = 0; col < 16; col++) {
      double colX = getChannelColumnX(col, centerX);
      // Row 0: PWM
      relativeOffsets[idx][0] = colX;
      relativeOffsets[idx][1] = row0_Y;
      idx++;
      // Row 1: V+
      relativeOffsets[idx][0] = colX;
      relativeOffsets[idx][1] = row1_Y;
      idx++;
      // Row 2: GND
      relativeOffsets[idx][0] = colX;
      relativeOffsets[idx][1] = row2_Y;
      idx++;
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
    double spacing = PIN_SPACING.convertToPixels();
    double topMargin = (boardH - 5 * spacing) / 2.0;
    double boardX = x - EDGE_MARGIN;
    double boardY = y - topMargin;
    return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 10, 10);
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

    double spacing = PIN_SPACING.convertToPixels();
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double topMargin = (boardH - 5 * spacing) / 2.0;
    double boardX = x - EDGE_MARGIN;
    double boardY = y - topMargin;
    double centerX = boardX + boardW / 2.0;
    double rightHeaderX = boardW - 2 * EDGE_MARGIN;

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
      // 4 Mounting holes with annular silver rings
      drawMountingHole(g2d, boardX + 16, boardY + 16, 15);
      drawMountingHole(g2d, boardX + 16, boardY + boardH - 16, 15);
      drawMountingHole(g2d, boardX + boardW - 16, boardY + 16, 15);
      drawMountingHole(g2d, boardX + boardW - 16, boardY + boardH - 16, 15);

      // 2-Pin Screw Terminal Block — centered horizontally at top of the board
      double termW = 56.0;
      double termH = 34.0;
      double termX = centerX - termW / 2.0;
      double termY = boardY + 8.0;
      RoundRectangle2D termBody = new RoundRectangle2D.Double(termX, termY, termW, termH, 4, 4);
      g2d.setColor(SCREW_TERMINAL_COLOR);
      g2d.fill(termBody);
      g2d.setColor(SCREW_TERMINAL_BORDER);
      g2d.draw(termBody);

      // Terminal Silk labels (V+ on left, GND on right of the terminal block)
      g2d.setColor(Color.WHITE);
      g2d.setFont(LABEL_FONT_BOLD);
      StringUtils.drawCenteredText(g2d, "V+", termX - 8, termY + termH / 2.0, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "GND", termX + termW + 8, termY + termH / 2.0, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);

      // PCA9685 TSSOP-28 IC — centered vertically right below the terminal block
      double chipW = 28.0;
      double chipH = 50.0;
      double chipX = centerX - chipW / 2.0;
      double chipY = termY + termH + 6.0;
      drawChip(g2d, chipX, chipY, chipW, chipH, "PCA9685");

      // Large Filter Electrolytic Capacitor (Top-Left)
      double capX = boardX + 65.0;
      double capY = boardY + 30.0;
      double capD = 32.0;
      g2d.setColor(CAP_BODY_COLOR);
      g2d.fill(new Ellipse2D.Double(capX - capD / 2.0, capY - capD / 2.0, capD, capD));
      // Negative stripe on left side
      g2d.setColor(CAP_STRIPE_COLOR);
      Shape oldClip = g2d.getClip();
      g2d.clip(new Ellipse2D.Double(capX - capD / 2.0, capY - capD / 2.0, capD, capD));
      g2d.fill(new Rectangle2D.Double(capX - capD / 2.0, capY - capD / 2.0, 9, capD));
      g2d.setClip(oldClip);
      g2d.setColor(CAP_BORDER_COLOR);
      g2d.draw(new Ellipse2D.Double(capX - capD / 2.0, capY - capD / 2.0, capD, capD));

      // Power LED & resistor (between capacitor and terminal block)
      g2d.setColor(POWER_LED_COLOR);
      g2d.fill(new RoundRectangle2D.Double(centerX - 60, boardY + 22, 8, 8, 2, 2));
      g2d.setColor(Color.WHITE);
      g2d.setFont(LABEL_FONT_TINY);
      StringUtils.drawCenteredText(g2d, "POWER", centerX - 56, boardY + 14, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // I2C Address Solder Jumpers A0..A5 (Top-Right)
      double addrX = centerX + 55.0;
      double addrY = boardY + 12.0;
      g2d.setColor(SMD_BODY_COLOR);
      g2d.fill(new RoundRectangle2D.Double(addrX, addrY, 60, 16, 2, 2));
      g2d.setColor(GOLD_PAD_COLOR);
      for (int a = 0; a < 6; a++) {
        g2d.fillRect((int) (addrX + 3 + a * 9.5), (int) (addrY + 3), 7, 10);
      }
      g2d.setColor(Color.WHITE);
      g2d.setFont(LABEL_FONT_TINY);
      StringUtils.drawCenteredText(g2d, "I2C Address", addrX + 30, addrY + 24, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Middle Area: 4 SMD Resistor Array packs
      g2d.setColor(SMD_ARRAY_COLOR);
      // Left 2 packs (for channels 0..7)
      g2d.fill(new RoundRectangle2D.Double(centerX - 120, boardY + 75, 42, 14, 2, 2));
      g2d.fill(new RoundRectangle2D.Double(centerX - 68, boardY + 75, 42, 14, 2, 2));
      // Right 2 packs (for channels 8..15)
      g2d.fill(new RoundRectangle2D.Double(centerX + 26, boardY + 75, 42, 14, 2, 2));
      g2d.fill(new RoundRectangle2D.Double(centerX + 78, boardY + 75, 42, 14, 2, 2));

      g2d.setColor(Color.WHITE);
      g2d.setFont(LABEL_FONT_TINY);
      StringUtils.drawCenteredText(g2d, "16 x 12-bit PWM", centerX - 80, boardY + 65, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // 4 Sections of 3-row color-coded header strips for 16 channels
      double row0_Y = boardY + 140.0;
      double row1_Y = boardY + 160.0;
      double row2_Y = boardY + 180.0;
      double stripH = 14.0;

      double[] secStarts = {
          centerX - 201.0, // Sec 0 (ch 0..3)
          centerX - 105.0, // Sec 1 (ch 4..7)
          centerX + 25.0,  // Sec 2 (ch 8..11)
          centerX + 121.0  // Sec 3 (ch 12..15)
      };
      double secW = 80.0;

      for (int sec = 0; sec < 4; sec++) {
        double secStartX = secStarts[sec];

        // PWM row (Yellow)
        g2d.setColor(PWM_YELLOW);
        g2d.fill(new RoundRectangle2D.Double(secStartX, row0_Y - stripH / 2.0, secW, stripH, 2, 2));
        // V+ row (Red)
        g2d.setColor(V_RED);
        g2d.fill(new RoundRectangle2D.Double(secStartX, row1_Y - stripH / 2.0, secW, stripH, 2, 2));
        // GND row (Black)
        g2d.setColor(GND_BLACK);
        g2d.fill(new RoundRectangle2D.Double(secStartX, row2_Y - stripH / 2.0, secW, stripH, 2, 2));
      }

      // Center Column Labels between Section 1 and Section 2 (PWM, V+, GND)
      g2d.setColor(Color.WHITE);
      g2d.setFont(LABEL_FONT_BOLD);
      StringUtils.drawCenteredText(g2d, "PWM", centerX, row0_Y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "V+", centerX, row1_Y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "GND", centerX, row2_Y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Channel numbers silkscreen above each channel column
      g2d.setFont(LABEL_FONT_TINY);
      for (int ch = 0; ch < 16; ch++) {
        double colX = getChannelColumnX(ch, centerX);
        StringUtils.drawCenteredText(g2d, Integer.toString(ch), colX, row0_Y - 12.0,
            HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }

      // Left I2C header labels
      g2d.setFont(LABEL_FONT_BOLD);
      for (int i = 0; i < 6; i++) {
        StringUtils.drawCenteredText(g2d, LEFT_I2C_NAMES[i], boardX + 28, boardY + 50.0 + i * spacing,
            HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
      }

      // Right I2C header labels
      for (int i = 0; i < 6; i++) {
        StringUtils.drawCenteredText(g2d, RIGHT_I2C_NAMES[i], boardX + boardW - 28, boardY + 50.0 + i * spacing,
            HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      }
    }

    g2d.setTransform(oldTx);

    // Draw I2C header pins (left & right)
    drawPins(g2d, 0, 6, false, outlineMode, drawingObserver);
    drawPins(g2d, 6, 6, false, outlineMode, drawingObserver);

    // Draw screw terminal contacts (V+, GND)
    drawScrewTerminals(g2d, 12, 2, 0, outlineMode, drawingObserver);

    // Draw all 48 servo channel pins
    drawPins(g2d, 14, 48, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(PCA_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    g2d.setColor(PCA_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    // 4 Channel pin strip blocks
    g2d.setColor(PWM_YELLOW);
    g2d.fillRect(5, height - 11, width / 2 - 7, 2);
    g2d.fillRect(width / 2 + 2, height - 11, width / 2 - 7, 2);
    g2d.setColor(V_RED);
    g2d.fillRect(5, height - 8, width / 2 - 7, 2);
    g2d.fillRect(width / 2 + 2, height - 8, width / 2 - 7, 2);
    g2d.setColor(GND_BLACK);
    g2d.fillRect(5, height - 5, width / 2 - 7, 2);
    g2d.fillRect(width / 2 + 2, height - 5, width / 2 - 7, 2);
    // Screw terminal
    g2d.setColor(SCREW_TERMINAL_COLOR);
    g2d.fillRect(width / 2 - 4, 6, 8, 5);
  }
}
