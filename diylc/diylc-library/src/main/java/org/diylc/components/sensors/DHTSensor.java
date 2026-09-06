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
package org.diylc.components.sensors;

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

@ComponentDescriptor(name = "Temp/Humidity Sensor (DHT11 / DHT22)", category = "Sensors",
    author = "Branislav Stojkovic", description = "DHT11 / DHT22 Digital Temperature and Humidity Sensor Module",
    instanceNamePrefix = "SEN", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class DHTSensor extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public enum DHTModel {
    DHT11("DHT11 (Blue)"), DHT22("DHT22 (White)");

    private final String label;
    DHTModel(String label) { this.label = label; }
    @Override public String toString() { return label; }
  }

  public static Color DHT11_BLUE = Color.decode("#1E88E5");
  public static Color DHT22_WHITE = Color.decode("#ECEFF1");

  public static Size BOARD_WIDTH = new Size(15.5d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(28.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES_3PIN = new String[] {"VCC", "DATA", "GND"};

  private DHTModel model = DHTModel.DHT11;

  public DHTSensor() {
    super();
    this.bodyColor = DHT11_BLUE;
    updateControlPoints();
  }

  @EditableProperty(name = "Model")
  public DHTModel getModel() {
    return model;
  }

  public void setModel(DHTModel model) {
    this.model = model;
    this.bodyColor = (model == DHTModel.DHT11) ? DHT11_BLUE : DHT22_WHITE;
    invalidateCache();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES_3PIN.length) {
      return PIN_NAMES_3PIN[index];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    double[][] relativeOffsets = new double[3][2];
    for (int i = 0; i < 3; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
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
    double boardX = x - (boardW - 2 * PIN_SPACING.convertToPixels()) / 2.0;
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
    double boardX = x - (boardW - 2 * PIN_SPACING.convertToPixels()) / 2.0;
    double boardY = y - boardH + 12;

    Shape boardShape = getBodyShape();

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : (model == DHTModel.DHT11 ? DHT11_BLUE : DHT22_WHITE));
    g2d.fill(boardShape);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      // Slotted grille lines
      Color slotColor = (model == DHTModel.DHT11) ? DHT11_BLUE.darker() : Color.decode("#B0BEC5");
      g2d.setColor(slotColor);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f));
      for (double sy = boardY + 25; sy < boardY + boardH - 45; sy += 14) {
        g2d.drawLine((int) (boardX + 15), (int) sy, (int) (boardX + boardW - 15), (int) sy);
      }

      // Label text
      g2d.setColor((model == DHTModel.DHT11) ? Color.WHITE : Color.DARK_GRAY);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, model.name(), boardX + boardW / 2.0, boardY + boardH - 25, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(DHT11_BLUE);
    g2d.fill(new RoundRectangle2D.Double(7, 3, width - 14, height - 6, 3, 3));
    g2d.setColor(DHT11_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(7, 3, width - 14, height - 6, 3, 3));

    // Grille lines
    g2d.setColor(Color.WHITE);
    g2d.drawLine(10, 8, width - 10, 8);
    g2d.drawLine(10, 13, width - 10, 13);
    g2d.drawLine(10, 18, width - 10, 18);

    // Pins
    g2d.setColor(PIN_COLOR);
    g2d.fillRect(width / 2 - 4, height - 5, 2, 2);
    g2d.fillRect(width / 2 - 1, height - 5, 2, 2);
    g2d.fillRect(width / 2 + 2, height - 5, 2, 2);
  }
}
