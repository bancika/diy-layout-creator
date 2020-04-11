/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.components.boards;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Breadboard", category = "Boards", author = "Branislav Stojkovic",
    description = "Prototyping solderless breadboard", instanceNamePrefix = "BB",
    zOrder = IDIYComponent.BOARD, bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME, enableCache = true)
public class Breadboard extends AbstractComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Color FILL_COLOR = Color.white;
  public static Color BORDER_COLOR = Color.black;
  public static Size BODY_ARC = new Size(3d, SizeUnit.mm);
  public static Size SPACING = new Size(0.1d, SizeUnit.in);
  public static Color SELECTION_COLOR = Color.red;
  public static Color HOLE_COLOR = Color.decode("#EEEEEE");

  public static Color PLUS_COLOR = Color.red;
  public static Color MINUS_COLOR = Color.blue;

  public static float COORDINATE_FONT_SIZE = 9f;
  public static Color COORDINATE_COLOR = Color.gray.brighter();

  public static Size HOLE_SIZE = new Size(1.5, SizeUnit.mm);
  public static Size HOLE_ARC = new Size(1d, SizeUnit.mm);

  protected Point point = new Point(0, 0);

  protected BreadboardSize breadboardSize;
  protected PowerStripPosition powerStripPosition;
  protected Orientation orientation;

  @SuppressWarnings("incomplete-switch")
  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }

    // adjust the angle
    double theta = 0;
    switch (getOrientation()) {
      case _90:
        theta = Math.PI / 2;
        break;
      case _180:
        theta = Math.PI;
        break;
      case _270:
        theta = Math.PI * 3 / 2;
        break;
    }
    if (theta != 0) {
      g2d.rotate(theta, point.x, point.y);
    }

    int bodyArc = (int) BODY_ARC.convertToPixels();
    double spacing = SPACING.convertToPixels();
    int holeCount = getBreadboardSize() == BreadboardSize.Full ? 63 : 30;

    // draw body
    g2d.setColor(FILL_COLOR);
    int width = (int) (23 * project.getGridSpacing().convertToPixels());
    int height = (int) ((holeCount + 1) * project.getGridSpacing().convertToPixels());
    g2d.fillRoundRect(point.x, point.y, width, height, bodyArc, bodyArc);
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : BORDER_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.drawRoundRect(point.x, point.y, width, height, bodyArc, bodyArc);

    drawingObserver.stopTracking();

    // draw lines
    g2d.setColor(PLUS_COLOR);
    g2d.drawLine((int) (point.x + spacing), (int) (point.y + spacing), (int) (point.x + spacing),
        (int) (point.y + holeCount * spacing));
    g2d.drawLine((int) (point.x + 19 * spacing), (int) (point.y + spacing), (int) (point.x + 19 * spacing),
        (int) (point.y + holeCount * spacing));
    g2d.setColor(MINUS_COLOR);
    g2d.drawLine((int) (point.x + 4 * spacing), (int) (point.y + spacing), (int) (point.x + 4 * spacing),
        (int) (point.y + holeCount * spacing));
    g2d.drawLine((int) (point.x + 22 * spacing), (int) (point.y + spacing), (int) (point.x + 22 * spacing),
        (int) (point.y + holeCount * spacing));

    int holeSize = getClosestOdd(HOLE_SIZE.convertToPixels());
    int holeArc = (int) HOLE_ARC.convertToPixels();

    g2d.setFont(LABEL_FONT.deriveFont(COORDINATE_FONT_SIZE));
    byte a = "a".getBytes()[0];

    // draw main holes
    for (int section = 0; section <= 1; section++) {
      double offset = section * 7 * spacing;

      for (int y = 0; y < holeCount; y++) {
        g2d.setColor(COORDINATE_COLOR);
        int coordinateX;
        if (section == 0) {
          coordinateX = (int) (point.x + offset + 5.5 * spacing);
        } else {
          coordinateX = (int) (point.x + offset + 10.5 * spacing);
        }
        StringUtils.drawCenteredText(g2d, String.valueOf(y + 1), coordinateX, (int) (point.y + (y + 1) * spacing),
            section == 0 ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        for (int x = 0; x < 5; x++) {
          int holeX = (int) (point.x + offset + (x + 6) * spacing);
          int holeY = (int) (point.y + (y + 1) * spacing);
          g2d.setColor(HOLE_COLOR);
          g2d.fillRoundRect(holeX - holeSize / 2, holeY - holeSize / 2, holeSize, holeSize, holeArc, holeArc);
          g2d.setColor(BORDER_COLOR);
          g2d.drawRoundRect(holeX - holeSize / 2, holeY - holeSize / 2, holeSize, holeSize, holeArc, holeArc);

          // Draw horizontal labels
          if (y == 0) {
            g2d.setColor(COORDINATE_COLOR);
            StringUtils.drawCenteredText(g2d, new String(new byte[] {(byte) (a + x + 5 * section)}), holeX, (int) (point.y),
                HorizontalAlignment.CENTER, VerticalAlignment.TOP);
            StringUtils.drawCenteredText(g2d, new String(new byte[] {(byte) (a + x + 5 * section)}), holeX, (int) (point.y
                + spacing * 30 + COORDINATE_FONT_SIZE / 2), HorizontalAlignment.CENTER, VerticalAlignment.TOP);
          }
        }
      }
    }

    double powerOffset =
        getPowerStripPosition() == PowerStripPosition.Inline ? (getBreadboardSize() == BreadboardSize.Full ? 2d : 1d)
            : (getBreadboardSize() == BreadboardSize.Full ? 1.5d : 0.5d);

    int psHoleCount = Math.round(holeCount / 10f) * 10;

    // draw power strip holes
    for (int section = 0; section <= 1; section++) {
      double offset = section * 18 * spacing;
      for (int y = 0; y < psHoleCount - 1; y++) {
        for (int x = 0; x < 2; x++) {
          if ((y + 1) % 6 == 0)
            continue;
          int holeX = (int) (point.x + offset + (x + 2) * spacing);
          int holeY = (int) (point.y + (y + 1 + powerOffset) * spacing);
          g2d.setColor(HOLE_COLOR);
          g2d.fillRoundRect(holeX - holeSize / 2, holeY - holeSize / 2, holeSize, holeSize, holeArc, holeArc);
          g2d.setColor(BORDER_COLOR);
          g2d.drawRoundRect(holeX - holeSize / 2, holeY - holeSize / 2, holeSize, holeSize, holeArc, holeArc);
        }
      }
    }
    
    // draw transparent connections just to designate connections
    drawingObserver.startTrackingContinuityArea(true);
    Composite oldComposite = g2d.getComposite();
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));
        
    for (int section = 0; section <= 1; section++) {
      double offset = section * 18 * spacing;
        for (int x = 0; x < 2; x++) {
          int holeX = (int) (point.x + offset + (x + 2) * spacing);
          int holeY1 = (int) (point.y + (1 + powerOffset) * spacing);
          int holeY2 = (int) (point.y + (psHoleCount -1 + powerOffset) * spacing);
          g2d.setColor(HOLE_COLOR);
          g2d.fillRoundRect(holeX - holeSize / 2, holeY1 - holeSize / 2, holeSize, holeSize + holeY2 - holeY1, holeArc, holeArc);
      }
    }
    
    for (int section = 0; section <= 1; section++) {
      double offset = section * 7 * spacing;

      for (int y = 0; y < holeCount; y++) {
        g2d.setColor(COORDINATE_COLOR);
        int coordinateX;
        if (section == 0) {
          coordinateX = (int) (point.x + offset + 5.5 * spacing);
        } else {
          coordinateX = (int) (point.x + offset + 10.5 * spacing);
        }
        StringUtils.drawCenteredText(g2d, String.valueOf(y + 1), coordinateX, (int) (point.y + (y + 1) * spacing),
            section == 0 ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
//        for (int x = 0; x < 5; x++) {
          int holeX1 = (int) (point.x + offset + 6 * spacing);
          int holeX2 = (int) (point.x + offset + 10 * spacing);
          int holeY = (int) (point.y + (y + 1) * spacing);
          g2d.setColor(HOLE_COLOR);
          g2d.fillRoundRect(holeX1 - holeSize / 2, holeY - holeSize / 2, holeSize + holeX2 - holeX1, holeSize, holeArc, holeArc);
//          }
        }
//      }
    }
    
    g2d.setComposite(oldComposite);
    drawingObserver.stopTrackingContinuityArea();
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    int arc = 4 / factor;
    g2d.setColor(FILL_COLOR);
    g2d.fillRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);

    g2d.setColor(HOLE_COLOR);
    g2d.fillRoundRect(width / 3 - 2 / factor, width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor), arc, arc);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(width / 3 - 2 / factor, width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor), arc, arc);

    g2d.setColor(HOLE_COLOR);
    g2d.fillRoundRect(2 * width / 3 - 2 / factor, width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor), arc, arc);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(2 * width / 3 - 2 / factor, width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor), arc, arc);

    g2d.setColor(HOLE_COLOR);
    g2d.fillRoundRect(width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor), arc, arc);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor), arc, arc);

    g2d.setColor(HOLE_COLOR);
    g2d.fillRoundRect(2 * width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor), arc, arc);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(2 * width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor), arc, arc);

    g2d.setColor(MINUS_COLOR);
    g2d.drawLine(width / 2, 2 / factor, width / 2, height - 4 / factor);
  }

  @EditableProperty
  public Orientation getOrientation() {
    if (orientation == null)
      orientation = Orientation.DEFAULT;
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
  }

  @EditableProperty(name = "Size")
  public BreadboardSize getBreadboardSize() {
    if (breadboardSize == null)
      breadboardSize = BreadboardSize.Half;
    return breadboardSize;
  }

  public void setBreadboardSize(BreadboardSize breadboardSize) {
    this.breadboardSize = breadboardSize;
  }

  @EditableProperty(name = "Power Strip")
  public PowerStripPosition getPowerStripPosition() {
    if (powerStripPosition == null)
      powerStripPosition = PowerStripPosition.Inline;
    return powerStripPosition;
  }

  public void setPowerStripPosition(PowerStripPosition powerStripPosition) {
    this.powerStripPosition = powerStripPosition;
  }

  @Override
  public int getControlPointCount() {
    return 2;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public Point getControlPoint(int index) {
    if (index == 0)
      return point;
    // create a synthetic second control point for clipping check and to make sure that we cannot drag the right side of the board off the screen
    double spacing = SPACING.convertToPixels();
    int holeCount = getBreadboardSize() == BreadboardSize.Full ? 63 : 30;
    // adjust the angle
    double theta = 0;
    switch (getOrientation()) {
      case _90:
        theta = Math.PI / 2;
        break;
      case _180:
        theta = Math.PI;
        break;
      case _270:
        theta = Math.PI * 3 / 2;
        break;
    }
    Point secondPoint = new Point((int) (point.x + 23 * spacing), (int) (point.y + (holeCount + 1) * spacing));
    if (theta != 0) {
      AffineTransform tx = AffineTransform.getRotateInstance(theta, point.x, point.y);
      tx.transform(secondPoint, secondPoint);
    }
    return secondPoint;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    if (index == 0)
      this.point.setLocation(point);
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}
  
  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    Point finalSecondPoint = getControlPoint(1); 
    return new Rectangle2D.Double(point.x, point.y, finalSecondPoint.x - point.x + 1, finalSecondPoint.y - point.y + 1);
  }

  public enum BreadboardSize {
    Half, Full
  }

  public enum PowerStripPosition {
    Inline, Offset
  }
}
