/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.components.boards;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.transform.ProtoBoardTransformer;
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

@ComponentDescriptor(name = "Proto Board 780 Holes", category = "Boards",
    author = "Branislav Stojkovic",
    description = "Prototyping board similar to Radio Shack 276-168B or MPJA 33304",
    instanceNamePrefix = "BB", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME, enableCache = true,
    transformer = ProtoBoardTransformer.class)
public class ProtoBoard780 extends AbstractProtoBoard {

  private static final long serialVersionUID = 1L;

  public static Color FILL_COLOR = Color.decode("#6B9B6B");
  public static Size SPACING = new Size(0.1d, SizeUnit.in);
  public static Color SELECTION_COLOR = Color.red;
  public static Color PAD_COLOR = COPPER_COLOR;
  public static Color TRACE_COLOR = PAD_COLOR.brighter();

  public static Color HOLE_COLOR = Color.white;

  public static float COORDINATE_FONT_SIZE = 9f;
  public static Color COORDINATE_COLOR = Color.decode("#DDDDDD");

  public static Size PAD_SIZE = new Size(2d, SizeUnit.mm);
  
  public static Size X_OFFSET = new Size(0.5d, SizeUnit.mm);

  public static Size WIDTH_SIZE = new Size(72d, SizeUnit.mm);
  public static Size LENGTH_SIZE = new Size(95d, SizeUnit.mm);

  protected Point2D point = new Point2D.Double(0, 0);

  protected Color boardColor = FILL_COLOR;
  protected Color padColor = PAD_COLOR;
  protected Color traceColor = TRACE_COLOR;
  protected Color textColor = COORDINATE_COLOR;

  public static Size MOUNTING_HORIZONTAL_SPACING_MPJA = new Size(60d, SizeUnit.mm);
  public static Size MOUNTING_HORIZONTAL_SPACING_RS = new Size(56d, SizeUnit.mm);
  public static Size MOUNTING_HOLE_SIZE = new Size(3d, SizeUnit.mm);

  protected ProtoBoard780Type type = ProtoBoard780Type.MPJA;
  
  protected Boolean translateToGrid = true;

  @SuppressWarnings("incomplete-switch")
  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    
    double spacing = SPACING.convertToPixels();
    double offsetX = X_OFFSET.convertToPixels();
    
    if (translateToGrid) {
      g2d.translate(-offsetX, spacing / 2);
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
      g2d.rotate(theta, point.getX(), point.getY());
    }

    // draw body
    g2d.setColor(boardColor);
    int width = (int) WIDTH_SIZE.convertToPixels();
    int length = (int) LENGTH_SIZE.convertToPixels();

    int mountingHoleSize = (int) MOUNTING_HOLE_SIZE.convertToPixels();

    Shape rect = new Rectangle2D.Double(point.getX(), point.getY(), width, length);
    g2d.fill(rect);
    Color finalBorderColor =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
            ? SELECTION_COLOR
            : boardColor.darker();
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    drawingObserver.stopTracking();

    g2d.draw(rect);

    int padSize = getClosestOdd(PAD_SIZE.convertToPixels());
    int holeSize = getClosestOdd(padSize / 3);

    g2d.setFont(LABEL_FONT.deriveFont(COORDINATE_FONT_SIZE));

    double offsetY = spacing * 0.5;

    // top section
    for (int i = 0; i < 15; i++) {
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(padSize - 2f));

      g2d.setColor(traceColor);
      int padX = (int) (point.getX() + width / 2 - 7 * spacing + i * spacing);
      drawingObserver.startTrackingContinuityArea(true);
      g2d.drawLine(padX, (int) (point.getY() + offsetY), padX,
          (int) (point.getY() + spacing + offsetY));
      drawingObserver.stopTrackingContinuityArea();

      for (int j = 0; j < 2; j++) {
        int padY = (int) (point.getY() + spacing * j + offsetY);

        g2d.setColor(padColor);

        drawingObserver.startTrackingContinuityArea(true);

        g2d.fillOval(padX - padSize / 2, padY - padSize / 2, padSize, padSize);

        drawingObserver.stopTrackingContinuityArea();

        g2d.setColor(padColor.darker());
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));

        g2d.drawOval(padX - padSize / 2, padY - padSize / 2, padSize, padSize);

        g2d.setColor(HOLE_COLOR);
        g2d.fillOval(padX - holeSize / 2, padY - holeSize / 2, holeSize, holeSize);
      }
    }
    // top section labels
    g2d.setColor(textColor);
    for (int i = 0; i < 4; i++) {
      int x = i == 0 ? 1 : i * 5;
      int padX = (int) (point.getX() + width / 2 - 8 * spacing + x * spacing);
      int padY = (int) (point.getY() + spacing * 2);

      // // flip the coordinates for MPJA
      // if (type == ProtoBoard780Type.MPJA) {
      // x = i == 3 ? 1 : (3 - i) * 5;
      // }

      StringUtils.drawCenteredText(g2d, Integer.toString(x), padX, padY, HorizontalAlignment.CENTER,
          VerticalAlignment.BOTTOM);
    }


    offsetY = spacing * 5.5;

    // draw 3-pad traces and vertical traces
    drawingObserver.startTrackingContinuityArea(true);
    g2d.setColor(traceColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(padSize - 2f));

    // the outer bus
    g2d.drawLine((int) (point.getX() + width / 2 - 13 * spacing),
        (int) (point.getY() - 2 * spacing + offsetY),
        (int) (point.getX() + width / 2 + 13 * spacing),
        (int) (point.getY() - 2 * spacing + offsetY));
    g2d.drawLine((int) (point.getX() + width / 2 - 13 * spacing),
        (int) (point.getY() - 2 * spacing + offsetY),
        (int) (point.getX() + width / 2 - 13 * spacing),
        (int) (point.getY() + (29 + (type == ProtoBoard780Type.RS ? 2 : 0)) * spacing + offsetY));
    g2d.drawLine((int) (point.getX() + width / 2 + 13 * spacing),
        (int) (point.getY() - 2 * spacing + offsetY),
        (int) (point.getX() + width / 2 + 13 * spacing),
        (int) (point.getY() + (29 + (type == ProtoBoard780Type.RS ? 2 : 0)) * spacing + offsetY));

    // horizontal long traces
    g2d.drawLine((int) (point.getX() + width / 2 - 12 * spacing), (int) (point.getY() + offsetY),
        (int) (point.getX() + width / 2 + 12 * spacing), (int) (point.getY() + offsetY));
    g2d.drawLine((int) (point.getX() + width / 2 - 12 * spacing),
        (int) (point.getY() + spacing * 29 + offsetY),
        (int) (point.getX() + width / 2 + 12 * spacing),
        (int) (point.getY() + spacing * 29 + offsetY));

    for (int i = 0; i < 5; i++) {
      int padX = (int) (point.getX() + width / 2 - 12 * spacing + (i * 5 + 1) * spacing);

      // vertical traces
      g2d.drawLine((int) (padX - spacing), (int) (point.getY() + offsetY), (int) (padX - spacing),
          (int) (point.getY() + spacing * 28 + offsetY));
      g2d.drawLine((int) (padX + 3 * spacing), (int) (point.getY() + spacing + offsetY),
          (int) (padX + 3 * spacing), (int) (point.getY() + spacing * 29 + offsetY));

      for (int j = 1; j < 29; j++) {
        int padY = (int) (point.getY() + spacing * j + offsetY);

        // 3-pad traces
        g2d.drawLine(padX, padY, (int) (padX + 2 * spacing), padY);
      }
    }
    drawingObserver.stopTrackingContinuityArea();

    // draw main pads
    for (int i = 0; i < 25; i++) {
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(padSize - 2f));

      g2d.setColor(padColor);
      int padX = (int) (point.getX() + width / 2 - 12 * spacing + i * spacing);
      // drawingObserver.startTrackingContinuityArea(true);
      // g2d.drawLine(padX, (int)(point.getY() + offsetY), padX, (int)(point.getY() + spacing +
      // offsetY));
      // drawingObserver.stopTrackingContinuityArea();

      for (int j = 0; j < 30; j++) {
        int padY = (int) (point.getY() + spacing * j + offsetY);

        g2d.setColor(padColor);

        drawingObserver.startTrackingContinuityArea(true);

        g2d.fillOval(padX - padSize / 2, padY - padSize / 2, padSize, padSize);

        drawingObserver.stopTrackingContinuityArea();

        g2d.setColor(padColor.darker());
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));

        g2d.drawOval(padX - padSize / 2, padY - padSize / 2, padSize, padSize);

        g2d.setColor(HOLE_COLOR);
        g2d.fillOval(padX - holeSize / 2, padY - holeSize / 2, holeSize, holeSize);
      }
    }

    g2d.setColor(textColor);

    // horizontal coordinates
    for (int i = 0; i < 6; i++) {
      int x = i == 0 ? 1 : i * 5;
      int padX = (int) (point.getX() + width / 2 - 13 * spacing + x * spacing);
      int padY = (int) (point.getY() - spacing + offsetY);

      String label = String.valueOf((char) ('A' + i));

      StringUtils.drawCenteredText(g2d, label, padX, padY, HorizontalAlignment.CENTER,
          VerticalAlignment.BOTTOM);
      // StringUtils.drawCenteredText(g2d, label, padX, padY + spacing * 31,
      // HorizontalAlignment.CENTER,
      // VerticalAlignment.TOP);
    }

    // vertical coordinates
    for (int i = 0; i < 7; i++) {
      int y = i == 0 ? 1 : i * 5;

      int padX = (int) (point.getX() + width / 2 - 14 * spacing);
      int padY = (int) (point.getY() + spacing * (y - 1) + offsetY);

      StringUtils.drawCenteredText(g2d, Integer.toString(y), padX, padY, HorizontalAlignment.LEFT,
          VerticalAlignment.BOTTOM);
    }

    // model name
    g2d.setFont(LABEL_FONT.deriveFont(COORDINATE_FONT_SIZE * 1.8f));
    int x = (int) (point.getX() + width / 2);
    int y = (int) (point.getY() + length - spacing * 1.5);
    StringUtils.drawCenteredText(g2d,
        type == ProtoBoard780Type.RS ? "Radio Shack 276-168B" : "MPJA.COM 33304 PB", x, y,
        HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

    // mounting holes
    int dx = (int) (type == ProtoBoard780Type.RS ? 11 * spacing : 12 * spacing);
    int holeY = (int) (point.getY() + spacing * (type == ProtoBoard780Type.RS ? 1 : 1.5));

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(HOLE_COLOR);

    g2d.fillOval((int) (point.getX() + width / 2) - dx - mountingHoleSize / 2,
        holeY - mountingHoleSize / 2, mountingHoleSize, mountingHoleSize);
    g2d.fillOval((int) (point.getX() + width / 2) + dx - mountingHoleSize / 2,
        holeY - mountingHoleSize / 2, mountingHoleSize, mountingHoleSize);

    g2d.setColor(finalBorderColor);

    g2d.drawOval((int) (point.getX() + width / 2) - dx - mountingHoleSize / 2,
        holeY - mountingHoleSize / 2, mountingHoleSize, mountingHoleSize);
    g2d.drawOval((int) (point.getX() + width / 2) + dx - mountingHoleSize / 2,
        holeY - mountingHoleSize / 2, mountingHoleSize, mountingHoleSize);

    holeY = (int) (point.getY() + length - spacing);

    g2d.setColor(HOLE_COLOR);

    g2d.fillOval((int) (point.getX() + width / 2) - dx - mountingHoleSize / 2,
        holeY - mountingHoleSize / 2, mountingHoleSize, mountingHoleSize);
    g2d.fillOval((int) (point.getX() + width / 2) + dx - mountingHoleSize / 2,
        holeY - mountingHoleSize / 2, mountingHoleSize, mountingHoleSize);

    g2d.setColor(finalBorderColor);

    g2d.drawOval((int) (point.getX() + width / 2) - dx - mountingHoleSize / 2,
        holeY - mountingHoleSize / 2, mountingHoleSize, mountingHoleSize);
    g2d.drawOval((int) (point.getX() + width / 2) + dx - mountingHoleSize / 2,
        holeY - mountingHoleSize / 2, mountingHoleSize, mountingHoleSize);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    g2d.setColor(FILL_COLOR);
    g2d.fillRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);
    g2d.setColor(FILL_COLOR.darker());
    g2d.drawRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);

    g2d.setColor(PAD_COLOR.brighter());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2 / factor));

    int size = (int) (8.0 / factor);

    g2d.drawLine(width / 3 - 2 / factor + size / 2, 6 / factor + size / 2, width,
        6 / factor + size / 2);
    g2d.drawLine(width / 3 - 2 / factor + size / 2, 2 * width / 3 - 2 / factor + size / 2, width,
        2 * width / 3 - 2 / factor + size / 2);


    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    g2d.setColor(PAD_COLOR);
    g2d.fillRect(width / 3 - 2 / factor, 6 / factor, size, size);
    g2d.setColor(PAD_COLOR.darker());
    g2d.drawRect(width / 3 - 2 / factor, 6 / factor, size, size);

    g2d.setColor(PAD_COLOR);
    g2d.fillOval(2 * width / 3 - 2 / factor, 6 / factor, size, size);
    g2d.setColor(PAD_COLOR.darker());
    g2d.drawOval(2 * width / 3 - 2 / factor, 6 / factor, size, size);

    g2d.setColor(PAD_COLOR);
    g2d.fillRect(width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, size, size);
    g2d.setColor(PAD_COLOR.darker());
    g2d.drawRect(width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, size, size);

    g2d.setColor(PAD_COLOR);
    g2d.fillOval(2 * width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, size, size);
    g2d.setColor(PAD_COLOR.darker());
    g2d.drawOval(2 * width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, size, size);

    g2d.setColor(HOLE_COLOR);
    g2d.fillOval(width / 3 - 2 / factor + size / 2 - 1, 6 / factor + size / 2 - 1, 3, 3);
    g2d.fillOval(width / 3 - 2 / factor + size / 2 - 1, 2 * width / 3 - 2 / factor + size / 2 - 1,
        3, 3);
    g2d.fillOval(2 * width / 3 - 2 / factor + size / 2 - 1, 6 / factor + size / 2 - 1, 3, 3);
    g2d.fillOval(2 * width / 3 - 2 / factor + size / 2 - 1,
        2 * width / 3 - 2 / factor + size / 2 - 1, 3, 3);
  }

  @EditableProperty(name = "Board Color")
  public Color getBoardColor() {
    return boardColor;
  }

  public void setBoardColor(Color boardColor) {
    this.boardColor = boardColor;
  }

  @EditableProperty(name = "Pad Color")
  public Color getPadColor() {
    return padColor;
  }

  public void setPadColor(Color padColor) {
    this.padColor = padColor;
  }

  @EditableProperty(name = "Trace Color")
  public Color getTraceColor() {
    return traceColor;
  }

  public void setTraceColor(Color traceColor) {
    this.traceColor = traceColor;
  }

  @EditableProperty(name = "Text Color")
  public Color getTextColor() {
    return textColor;
  }

  public void setTextColor(Color textColor) {
    this.textColor = textColor;
  }

  @EditableProperty
  public ProtoBoard780Type getType() {
    return type;
  }

  public void setType(ProtoBoard780Type type) {
    this.type = type;
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
  public Point2D getControlPoint(int index) {
    if (index == 0)
      return point;

    // create a synthetic second control point for clipping check and to make sure that we cannot
    // drag the right side of the board off the screen

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
    int width = (int) WIDTH_SIZE.convertToPixels();
    int length = (int) LENGTH_SIZE.convertToPixels();
    Point2D secondPoint = new Point2D.Double(point.getX() + width, point.getY() + length);
    if (theta != 0) {
      AffineTransform tx = AffineTransform.getRotateInstance(theta, point.getX(), point.getY());
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
  public void setControlPoint(Point2D point, int index) {
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
    double spacing = SPACING.convertToPixels();
    double offsetX = X_OFFSET.convertToPixels();
    
    double dx = translateToGrid ? -offsetX : 0;
    double dy = translateToGrid ? spacing / 2 : 0;
    
    Point2D finalSecondPoint = getControlPoint(1);
    return new Rectangle2D.Double(Math.min(point.getX(), finalSecondPoint.getX()) + dx - 1,
        Math.min(point.getY(), finalSecondPoint.getY()) + dy - 1,
        Math.abs(finalSecondPoint.getX() - point.getX()) + 2,
        Math.abs(finalSecondPoint.getY() - point.getY()) + 2);
  }

  public static enum ProtoBoard780Type {
    RS, MPJA
  }
}
