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
package org.diylc.components.modules;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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

@ComponentDescriptor(name = "Relay Module (1/2/4-Channel)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "5V Optocoupler-Isolated Relay Module with Screw Terminals",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class RelayModule extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public enum RelayChannels {
    _1_Channel("1-Channel (50x26mm)", 1, 50.0, 26.0),
    _2_Channel("2-Channel (50x38mm)", 2, 50.0, 38.0),
    _4_Channel("4-Channel (75x55mm)", 4, 75.0, 55.0);

    private final String label;
    private final int count;
    private final double widthMm;
    private final double heightMm;

    RelayChannels(String label, int count, double widthMm, double heightMm) {
      this.label = label;
      this.count = count;
      this.widthMm = widthMm;
      this.heightMm = heightMm;
    }

    @Override public String toString() { return label; }
    public int getCount() { return count; }
    public double getWidthMm() { return widthMm; }
    public double getHeightMm() { return heightMm; }
  }

  public static Color PCB_BLUE = Color.decode("#0055A5");
  public static Color RELAY_CUBE_BLUE = Color.decode("#1565C0");

  private RelayChannels channels = RelayChannels._2_Channel;

  public RelayModule() {
    super();
    this.bodyColor = PCB_BLUE;
    updateControlPoints();
  }

  @EditableProperty(name = "Channels")
  public RelayChannels getChannels() {
    return channels;
  }

  public void setChannels(RelayChannels channels) {
    this.channels = channels;
    updateControlPoints();
    invalidateCache();
  }

  @Override
  public String getControlPointNodeName(int index) {
    int chCount = channels.getCount();
    int termPinCount = chCount * 3;

    if (index < termPinCount) {
      int ch = index / 3 + 1;
      int pin = index % 3;
      switch (pin) {
        case 0: return "CH" + ch + "_NO";
        case 1: return "CH" + ch + "_COM";
        case 2: return "CH" + ch + "_NC";
      }
    } else {
      int ctrlIndex = index - termPinCount;
      if (ctrlIndex == 0) return "VCC";
      if (ctrlIndex == 1) return "GND";
      return "IN" + (ctrlIndex - 1);
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    int chCount = channels.getCount();
    double termSpacing = new Size(5.08d, SizeUnit.mm).convertToPixels();
    double pinSpacing = PIN_SPACING.convertToPixels();

    int totalPins = chCount * 3 + 2 + chCount; // 3 per relay + VCC, GND + IN1..INn
    double[][] relativeOffsets = new double[totalPins][2];

    // High Voltage screw terminals (NO, COM, NC per channel) along left edge
    for (int ch = 0; ch < chCount; ch++) {
      double chY = ch * (3.5 * termSpacing);
      for (int p = 0; p < 3; p++) {
        int idx = ch * 3 + p;
        relativeOffsets[idx][0] = 0;
        relativeOffsets[idx][1] = chY + p * termSpacing;
      }
    }

    // Low Voltage Control Header (VCC, GND, IN1..INn) on opposite right edge
    double rightX = new Size(channels.getWidthMm() - 10, SizeUnit.mm).convertToPixels();
    int ctrlPins = 2 + chCount;
    for (int p = 0; p < ctrlPins; p++) {
      int idx = chCount * 3 + p;
      relativeOffsets[idx][0] = rightX;
      relativeOffsets[idx][1] = p * pinSpacing;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardW = new Size(channels.getWidthMm(), SizeUnit.mm).convertToPixels();
    double boardH = new Size(channels.getHeightMm(), SizeUnit.mm).convertToPixels();
    double boardX = x - 18;
    double boardY = y - 18;
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

    double boardW = new Size(channels.getWidthMm(), SizeUnit.mm).convertToPixels();
    double boardH = new Size(channels.getHeightMm(), SizeUnit.mm).convertToPixels();
    double boardX = x - 18;
    double boardY = y - 18;

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
      // 4 Mounting holes
      drawMountingHole(g2d, boardX + 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + 16, boardY + boardH - 16, 16);
      drawMountingHole(g2d, boardX + boardW - 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + boardW - 16, boardY + boardH - 16, 16);

      // Blue cubic relay blocks (Songle 10A style)
      int chCount = channels.getCount();
      double relayW = 120;
      double relayH = (boardH - 40) / chCount;

      for (int ch = 0; ch < chCount; ch++) {
        double ry = boardY + 20 + ch * relayH;
        double rx = boardX + 55;

        // Terminal block
        RoundRectangle2D tb = new RoundRectangle2D.Double(boardX + 5, ry + 2, 45, relayH - 4, 3, 3);
        g2d.setColor(SCREW_TERMINAL_COLOR);
        g2d.fill(tb);
        g2d.setColor(SCREW_TERMINAL_BORDER);
        g2d.draw(tb);

        // Blue Relay cube
        g2d.setColor(RELAY_CUBE_BLUE);
        g2d.fill(new RoundRectangle2D.Double(rx, ry + 2, relayW, relayH - 4, 4, 4));
        g2d.setColor(RELAY_CUBE_BLUE.darker());
        g2d.draw(new RoundRectangle2D.Double(rx, ry + 2, relayW, relayH - 4, 4, 4));

        g2d.setColor(Color.WHITE);
        g2d.setFont(SILK_FONT_SMALL);
        StringUtils.drawCenteredText(g2d, "SONGLE", rx + relayW / 2.0, ry + relayH / 2.0 - 6, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        StringUtils.drawCenteredText(g2d, "10A 250VAC", rx + relayW / 2.0, ry + relayH / 2.0 + 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        // Status LED
        g2d.setColor(Color.RED);
        g2d.fillOval((int) (rx + relayW + 15), (int) (ry + relayH / 2.0 - 4), 8, 8);
      }
    }

    g2d.setTransform(oldTx);

    // Draw terminals and control pins
    int chCount = channels.getCount();
    drawScrewTerminals(g2d, 0, chCount * 3, 40, outlineMode, drawingObserver);
    drawPins(g2d, chCount * 3, 2 + chCount, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(PCB_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    g2d.setColor(PCB_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));

    // Relay block
    g2d.setColor(RELAY_CUBE_BLUE);
    g2d.fillRect(10, 8, 14, height - 16);

    // Terminal
    g2d.setColor(SCREW_TERMINAL_COLOR);
    g2d.fillRect(4, 9, 5, height - 18);

    g2d.setColor(Color.RED);
    g2d.fillOval(width - 7, height / 2 - 2, 4, 4);
  }
}
