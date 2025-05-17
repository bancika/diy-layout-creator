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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

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

@ComponentDescriptor(name = "Eurorack Stripboard", category = "Boards",
    author = "Branislav Stojkovic",
    description = "David Haillant's eurorack format stripboard for prototyping and building simple modules",
    instanceNamePrefix = "BB", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME, enableCache = true,
    transformer = ProtoBoardTransformer.class)
public class EurorackStripboard extends AbstractProtoBoard {

  private static final long serialVersionUID = 1L;

  public static Color FILL_COLOR = FR4_COLOR;
  public static Size SPACING = new Size(0.1d, SizeUnit.in);
  public static Size CORNER_RADIUS = new Size(0.1d, SizeUnit.in);
  public static Color SELECTION_COLOR = Color.red;
  public static Color PAD_COLOR = COPPER_COLOR;
  public static Color TRACE_COLOR = PAD_COLOR.brighter();
  public static Color HOLE_COLOR = Color.white;

  public static float COORDINATE_FONT_SIZE = 10f;
  public static Color COORDINATE_COLOR = Color.decode("#DDDDDD");

  public static Size PAD_SIZE = new Size(2d, SizeUnit.mm);
  public static Size FILTER_CAP_SIZE = new Size(0.23d, SizeUnit.in);
  public static Size LARGE_PAD_SIZE = new Size(0.25d, SizeUnit.in);

  public static Size WIDTH_SIZE = new Size(2d, SizeUnit.in);
  public static Size LENGTH_SIZE = new Size(4d, SizeUnit.in);
  public static Size BODY_OFFSET_Y = new Size(0.1d, SizeUnit.in);

  protected Point2D point = new Point2D.Double(0, 0);

  protected Color boardColor = FILL_COLOR;
  protected Color padColor = PAD_COLOR;
  protected Color traceColor = TRACE_COLOR;
  protected Color textColor = COORDINATE_COLOR;

  private static String[] connectorLabels =
      new String[] {"Conn1", "GND", "GND", "GND", "Conn2", "+5V", "CV", "Gate"};
  transient private List<Segment> segments;
  transient private List<Pad> potPads;
  transient private List<Pad> filterPads;
  transient private List<Point2D[]> potTraces;

  public List<Segment> getSegments() {
    if (segments == null) {
      segments = new ArrayList<EurorackStripboard.Segment>();
      segments.add(new Segment(5, 0, 5, 1, "CenterLeft0"));
      segments.add(new Segment(10, 0, 5, 1, "CenterRight0"));
      segments.add(new Segment(5, 1, 5, 1, "CenterLeft1"));
      segments.add(new Segment(10, 1, 5, 1, "CenterRight1"));

      segments.add(new Segment(5, 2, 1, 30, "GND"));

      for (int i = 0; i < 29; i++) {
        segments.add(new Segment(6, 2 + i, 4, 1, "CenterLeft" + (2 + i)));
        segments.add(new Segment(10, 2 + i, 5, 1, "CenterRight" + (2 + i)));
      }
      segments.add(new Segment(6, 2 + 29, 4, 1, "CenterLeft31"));

      segments.add(new Segment(15, 3, 1, 30, "-"));
      segments.add(new Segment(16, 3, 1, 30, "GND"));
      segments.add(new Segment(17, 3, 1, 30, "+"));

      for (int i = 0; i < 5; i++) {
        for (int j = 0; j < 5; j++) {
          segments.add(new Segment(3, 8 * i + j, 2, 1, ((char) ('A' + j)) + Integer.toString(i), false));
        }
        if (i < 4) {
          for (int j = 0; j < 3; j++) {
            segments.add(new Segment(0, 8 * i + j + 5, 5, 1, i + "-" + (j + 5)));
          }
        }
      }
    }
    return segments;
  }

  public List<Pad> getPotPads() {
    if (potPads == null) {
      potPads = new ArrayList<EurorackStripboard.Pad>();
      potPads.add(new Pad(1.5, 0.2, "A"));

      potPads.add(new Pad(0.5, 0, "B", true));
      potPads.add(new Pad(0, 0.2, "B", false));
      potPads.add(new Pad(1, 1, "B", true));

      potPads.add(new Pad(0.5, 2, "C"));
      potPads.add(new Pad(1, 2, "C"));

      potPads.add(new Pad(0.5, 4, "D"));
      potPads.add(new Pad(0, 3.8, "D"));
      potPads.add(new Pad(1, 3, "D"));

      potPads.add(new Pad(1.5, 3.8, "E"));
    }
    return potPads;
  }

  public List<Pad> getFilterPads() {
    if (filterPads == null) {
      filterPads = new ArrayList<EurorackStripboard.Pad>();

      filterPads.add(new Pad(6, 32.8, "GND", true));
      filterPads.add(new Pad(6, 33.8, "-"));

      filterPads.add(new Pad(8, 33, "-"));
      filterPads.add(new Pad(8, 34, "Conn1", true));

      filterPads.add(new Pad(9, 33, "+", true));
      filterPads.add(new Pad(9, 34, "Conn2"));

      filterPads.add(new Pad(11, 32.8, "+", true));
      filterPads.add(new Pad(11, 33.8, "GND"));

      filterPads.add(new Pad(11.5, 31, "+5V", true));
      filterPads.add(new Pad(12.5, 31, "CV"));
      filterPads.add(new Pad(13.5, 31, "Gate"));
    }
    return filterPads;
  }

  public List<Point2D[]> getPotTraces() {
    if (potTraces == null) {
      potTraces = new ArrayList<Point2D[]>();
      potTraces.add(new Point2D[] {new Point2D.Double(1.5, 0.2), new Point2D.Double(3, 0.2)});
      potTraces.add(new Point2D[] {new Point2D.Double(1, 2), new Point2D.Double(3, 2)});
      potTraces.add(new Point2D[] {new Point2D.Double(1, 1), new Point2D.Double(3, 1)});
      potTraces.add(new Point2D[] {new Point2D.Double(1, 3), new Point2D.Double(3, 3)});
      potTraces.add(new Point2D[] {new Point2D.Double(1.5, 3.8), new Point2D.Double(3, 3.8)});

      potTraces.add(new Point2D[] {new Point2D.Double(0.8, 0.2), new Point2D.Double(0.8, 1)});
      potTraces.add(new Point2D[] {new Point2D.Double(0.8, 3.8), new Point2D.Double(0.8, 3)});
    }
    return potTraces;
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
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
      g2d.rotate(theta, point.getX(), point.getY());
    }

    double spacing = SPACING.convertToPixels();

    // draw body
    g2d.setColor(boardColor);
    int width = (int) WIDTH_SIZE.convertToPixels();
    int length = (int) LENGTH_SIZE.convertToPixels();
    int radius = (int) CORNER_RADIUS.convertToPixels();
    int bodyOffsetY = (int) BODY_OFFSET_Y.convertToPixels();

    Shape rect = new RoundRectangle2D.Double(point.getX(), point.getY() - bodyOffsetY, width,
        length, radius, radius);
    g2d.fill(rect);
    g2d.setColor(
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
            ? SELECTION_COLOR
            : boardColor.darker());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    drawingObserver.stopTracking();

    g2d.draw(rect);

    int padSize = getClosestOdd(PAD_SIZE.convertToPixels());
    int holeSize = getClosestOdd(padSize / 3);

    g2d.setFont(LABEL_FONT.deriveFont(COORDINATE_FONT_SIZE));
    g2d.setColor(textColor);

    double x0 = point.getX() + spacing;
    double y0 = point.getY() + spacing;

    for (int i = 0; i < 3; i++) {
      String text = (char) ((int) 'A' + (i * 5)) + "";
      StringUtils.drawCenteredText(g2d, text, x0 + spacing * i * 5, y0 - spacing - 2,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      text = (char) ((int) 'A' + (i * 5 + 4)) + "";
      StringUtils.drawCenteredText(g2d, text, x0 + spacing * (i * 5 + 4), y0 - spacing - 2,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    StringUtils.drawCenteredText(g2d, "GND", x0 + spacing * 16, y0 + 2 * spacing - 2,
        HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
    StringUtils.drawCenteredText(g2d, "+", x0 + spacing * 17, y0 + 2 * spacing,
        HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
    StringUtils.drawCenteredText(g2d, "GND", x0 + spacing * 16, y0 + 33 * spacing + 2,
        HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    StringUtils.drawCenteredText(g2d, "+", x0 + spacing * 17, y0 + 33 * spacing + 2,
        HorizontalAlignment.CENTER, VerticalAlignment.TOP);

    g2d.setFont(LABEL_FONT);
    StringUtils.drawCenteredText(g2d, "-", x0 + spacing * 15, y0 + 2 * spacing + 2,
        HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
    StringUtils.drawCenteredText(g2d, "-", x0 + spacing * 15, y0 + 33 * spacing,
        HorizontalAlignment.CENTER, VerticalAlignment.TOP);

    g2d.setFont(LABEL_FONT.deriveFont(COORDINATE_FONT_SIZE));
    for (int i = 0; i < 6; i++) {
      StringUtils.drawCenteredText(g2d, Integer.toString((i + 1) * 5), x0 + spacing * 18 - 4,
          y0 + (5 * (i + 1) - 1) * spacing, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
    }

    StringUtils.drawCenteredText(g2d, "Eurorack Stripboard", x0 - 8, y0 + (36.8) * spacing,
        HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM);
    StringUtils.drawCenteredText(g2d, "davidhaillant.com", x0 - 8, y0 + (37.3) * spacing,
        HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM);

    StringUtils.drawCenteredText(g2d, "GND", x0 + 17 * spacing, y0 + (37) * spacing,
        HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);

    // filter caps
    g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(2));
    int filterCapSize = (int) FILTER_CAP_SIZE.convertToPixels();

    int centerX = (int) (x0 + 6 * spacing);
    int centerY = (int) (y0 + 33.3 * spacing);

    Ellipse2D filterCap = new Ellipse2D.Double(centerX - filterCapSize / 2,
        centerY - filterCapSize / 2, filterCapSize, filterCapSize);
    g2d.draw(filterCap);
    Area area = new Area(filterCap);
    area.subtract(new Area(new Rectangle2D.Double(centerX - filterCapSize, centerY - filterCapSize,
        filterCapSize * 2, filterCapSize)));
    g2d.fill(area);

    centerX = (int) (x0 + 11 * spacing);
    filterCap = new Ellipse2D.Double(centerX - filterCapSize / 2, centerY - filterCapSize / 2,
        filterCapSize, filterCapSize);
    g2d.draw(filterCap);
    area = new Area(filterCap);
    area.subtract(new Area(new Rectangle2D.Double(centerX - filterCapSize, centerY - filterCapSize,
        filterCapSize * 2, filterCapSize)));
    g2d.fill(area);

    int circleSize = getClosestOdd(padSize * 1.2);
    centerX = (int) (x0 + 8 * spacing);
    centerY = (int) (y0 + 33 * spacing);
    g2d.drawOval(centerX - circleSize / 2, centerY - circleSize / 2, circleSize, circleSize);
    g2d.drawLine(centerX - circleSize / 2, (int) (centerY + circleSize / 2 + spacing + 1), centerX + circleSize / 2, (int) (centerY + circleSize / 2 + spacing + 1));
    
    centerX = (int) (x0 + 9 * spacing);
    centerY = (int) (y0 + 34 * spacing);
    g2d.drawOval(centerX - circleSize / 2, centerY - circleSize / 2, circleSize, circleSize);        
    g2d.drawLine(centerX - circleSize / 2, (int) (centerY - circleSize / 2 - spacing), centerX + circleSize / 2, (int) (centerY - circleSize / 2 - spacing));
        
    StringUtils.drawCenteredText(g2d, "GND", x0 + spacing * 5.1, y0 + 31.6 * spacing,
        HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
    StringUtils.drawCenteredText(g2d, "+5V", x0 + spacing * 11.3, y0 + 31.5 * spacing,
        HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
    StringUtils.drawCenteredText(g2d, "CV", x0 + spacing * 12.5, y0 + 31.5 * spacing,
        HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
    StringUtils.drawCenteredText(g2d, "Gate", x0 + spacing * 13.7, y0 + 31.5 * spacing,
        HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);

    // pot traces
    List<Point2D[]> potTraces = getPotTraces();
    g2d.setColor(traceColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(padSize / 4));
    for (int i = 0; i < 5; i++) {
      for (Point2D[] trace : potTraces) {
        int padX1 = (int) (x0 + trace[0].getX() * spacing);
        int padY1 = (int) (y0 + (trace[0].getY() + 8 * i) * spacing);
        int padX2 = (int) (x0 + trace[1].getX() * spacing);
        int padY2 = (int) (y0 + (trace[1].getY() + 8 * i) * spacing);
        g2d.drawLine(padX1, padY1, padX2, padY2);
      }
    }

    // segments
    for (Segment segment : getSegments()) {

      if (segment.spanX > 1) {
        drawingObserver.stopTrackingContinuityArea();
        g2d.setColor(textColor);
        Rectangle2D segmentRect = new Rectangle2D.Double(x0 + segment.x * spacing - spacing / 2,
            y0 + segment.y * spacing - spacing / 2, (segment.spanX) * spacing, spacing);
        g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(2));
        g2d.draw(segmentRect);

        if (segment.includeTrace) {
          g2d.setColor(traceColor);
          g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(padSize / 4));
          g2d.drawLine((int) (x0 + segment.x * spacing), (int) (y0 + segment.y * spacing),
              (int) (x0 + (segment.x + segment.spanX - 1) * spacing),
              (int) (y0 + segment.y * spacing));
        }

      } else if (segment.spanY > 1) {
        drawingObserver.stopTrackingContinuityArea();
        g2d.setColor(textColor);
        Rectangle2D segmentRect = new Rectangle2D.Double(x0 + segment.x * spacing - spacing / 2,
            y0 + segment.y * spacing - spacing / 2, spacing, (segment.spanY) * spacing);
        g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(2));
        g2d.draw(segmentRect);

        if (segment.includeTrace) {
          g2d.setColor(traceColor);
          g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(padSize / 4));
          g2d.drawLine((int) (x0 + segment.x * spacing), (int) (y0 + segment.y * spacing),
              (int) (x0 + segment.x * spacing),
              (int) (y0 + (segment.y + segment.spanY - 1) * spacing));
        }
      }

      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

      for (int i = 0; i < segment.spanX; i++) {
        for (int j = 0; j < segment.spanY; j++) {

          int padX = (int) (x0 + (segment.x + i) * spacing);
          int padY = (int) (y0 + (segment.y + j) * spacing);

          drawingObserver.startTrackingContinuityArea(true);
          drawingObserver.setContinuityMarker(segment.label);

          g2d.setColor(padColor);
          g2d.fillRect(padX - padSize / 2, padY - padSize / 2, padSize, padSize);

          drawingObserver.setContinuityMarker(null);

          drawingObserver.stopTrackingContinuityArea();

          g2d.setColor(padColor.darker());
          g2d.drawRect(padX - padSize / 2, padY - padSize / 2, padSize, padSize);

          g2d.setColor(HOLE_COLOR);
          g2d.fillOval(padX - holeSize / 2, padY - holeSize / 2, holeSize, holeSize);
        }
      }
    }

    g2d.setColor(textColor);
    g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(3));
    g2d.drawLine((int) (x0 + 5 * spacing), (int) (y0 + 35 * spacing), (int) (x0 + 15 * spacing),
        (int) (y0 + 35 * spacing));
    g2d.drawLine((int) (x0 + 15 * spacing), (int) (y0 + 35 * spacing), (int) (x0 + 15 * spacing),
        (int) (y0 + 38 * spacing) - 2);
    g2d.drawLine((int) (x0 + 5 * spacing), (int) (y0 + 35 * spacing), (int) (x0 + 5 * spacing),
        (int) (y0 + 38 * spacing) - 2);
    
    g2d.setStroke(ObjectCache.getInstance().fetchStroke(1, new float[] { 3, 6 }, 6, BasicStroke.CAP_SQUARE));
    g2d.drawLine((int) (x0 + 11 * spacing), (int) (y0 + 35.25 * spacing), (int) (x0 + 11 * spacing),
        (int) (y0 + 38 * spacing) - 2);
    
    
    g2d.fillRect((int) (x0 + 5 * spacing + 4), (int) (y0 + 35 * spacing + 4), 8,
        (int) (3 * spacing) - 4);

    // pots
    int potPadSize = (int) (padSize * 0.9);
    List<Pad> potPads = getPotPads();
    for (int i = 0; i < 5; i++) {

      g2d.setColor(textColor);
      g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(3));
      g2d.drawRect((int) (x0 - 0.8 * spacing), (int) (y0 + (8 * i) * spacing), (int) (3 * spacing),
          (int) (4 * spacing));

      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

      for (Pad potPad : potPads) {
        int padX = (int) (x0 + potPad.x * spacing);
        int padY = (int) (y0 + (potPad.y + 8 * i) * spacing);

        drawingObserver.startTrackingContinuityArea(true);
        drawingObserver.setContinuityMarker(potPad.label + i);

        g2d.setColor(padColor);
        if (potPad.square)
          g2d.fillRect(padX - potPadSize / 2, padY - potPadSize / 2, potPadSize, potPadSize);
        else
          g2d.fillOval(padX - potPadSize / 2, padY - potPadSize / 2, potPadSize, potPadSize);

        drawingObserver.setContinuityMarker(null);

        drawingObserver.stopTrackingContinuityArea();

        g2d.setColor(padColor.darker());
        if (potPad.square)
          g2d.drawRect(padX - potPadSize / 2, padY - potPadSize / 2, potPadSize, potPadSize);
        else
          g2d.drawOval(padX - potPadSize / 2, padY - potPadSize / 2, potPadSize, potPadSize);

        g2d.setColor(HOLE_COLOR);
        g2d.fillOval(padX - holeSize / 2, padY - holeSize / 2, holeSize, holeSize);
      }

      g2d.setColor(textColor);
      g2d.setFont(LABEL_FONT.deriveFont(16f));

      StringUtils.drawCenteredText(g2d, Integer.toString(i + 1), x0 - 4, y0 + (2 + i * 8) * spacing,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      g2d.setFont(LABEL_FONT.deriveFont(COORDINATE_FONT_SIZE));

      StringUtils.drawCenteredText(g2d, "B", x0 - 2, y0 + (1 + i * 8) * spacing - 4,
          HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "D", x0 - 2, y0 + (3 + i * 8) * spacing + 4,
          HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      
      g2d.setFont(LABEL_FONT.deriveFont(COORDINATE_FONT_SIZE * 0.9f));
      for (int j = 0; j < 5; j++) {
        StringUtils.drawCenteredText(g2d, (char)('A' + j) + "", x0 + 3.5 * spacing, y0 + (j + i * 8) * spacing,
            HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }
    }

    List<Pad> filterPads = getFilterPads();
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    // filter pads
    for (Pad potPad : filterPads) {
      int padX = (int) (x0 + potPad.x * spacing);
      int padY = (int) (y0 + potPad.y * spacing);

      drawingObserver.startTrackingContinuityArea(true);
      drawingObserver.setContinuityMarker(potPad.label);

      g2d.setColor(padColor);
      if (potPad.square)
        g2d.fillRect(padX - potPadSize / 2, padY - potPadSize / 2, potPadSize, potPadSize);
      else
        g2d.fillOval(padX - potPadSize / 2, padY - potPadSize / 2, potPadSize, potPadSize);

      drawingObserver.setContinuityMarker(null);

      drawingObserver.stopTrackingContinuityArea();

      g2d.setColor(padColor.darker());
      if (potPad.square)
        g2d.drawRect(padX - potPadSize / 2, padY - potPadSize / 2, potPadSize, potPadSize);
      else
        g2d.drawOval(padX - potPadSize / 2, padY - potPadSize / 2, potPadSize, potPadSize);

      g2d.setColor(HOLE_COLOR);
      g2d.fillOval(padX - holeSize / 2, padY - holeSize / 2, holeSize, holeSize);
    }


    // bottom connector
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 2; j++) {
        int padX = (int) (x0 + (6.5 + i) * spacing);
        int padY = (int) (y0 + (36 + j) * spacing);

        drawingObserver.startTrackingContinuityArea(true);
        drawingObserver.setContinuityMarker(connectorLabels[i]);

        g2d.setColor(padColor);
        if (i == 0 && j == 1)
          g2d.fillRect(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
        else
          g2d.fillOval(padX - padSize / 2, padY - padSize / 2, padSize, padSize);

        drawingObserver.setContinuityMarker(null);
        drawingObserver.stopTrackingContinuityArea();

        g2d.setColor(padColor.darker());
        if (i == 0 && j == 1)
          g2d.drawRect(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
        else
          g2d.drawOval(padX - padSize / 2, padY - padSize / 2, padSize, padSize);

        g2d.setColor(HOLE_COLOR);
        g2d.fillOval(padX - holeSize / 2, padY - holeSize / 2, holeSize, holeSize);
      }
    }

    // GND pad
    int largePadSize = (int) LARGE_PAD_SIZE.convertToPixels();
    int largeHoleSize = (int) (LARGE_PAD_SIZE.convertToPixels() * 0.6);

    int padX = (int) (x0 + (17) * spacing);
    int padY = (int) (y0 + 35.5 * spacing);

    drawingObserver.startTrackingContinuityArea(true);
    drawingObserver.setContinuityMarker("GND");

    g2d.setColor(padColor);
    g2d.fillOval(padX - largePadSize / 2, padY - largePadSize / 2, largePadSize, largePadSize);
    g2d.setColor(HOLE_COLOR);
    g2d.fillOval(padX - largeHoleSize / 2, padY - largeHoleSize / 2, largeHoleSize, largeHoleSize);

    drawingObserver.setContinuityMarker(null);
    drawingObserver.stopTrackingContinuityArea();

    padY = (int) (y0 + 0.5 * spacing);

    g2d.setColor(textColor);
    g2d.fillOval(padX - largePadSize / 2, padY - largePadSize / 2, largePadSize, largePadSize);
    g2d.setColor(HOLE_COLOR);
    g2d.fillOval(padX - largeHoleSize / 2, padY - largeHoleSize / 2, largeHoleSize, largeHoleSize);
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
    g2d.fillRect(2 * width / 3 - 2 / factor, 6 / factor, size, size);
    g2d.setColor(PAD_COLOR.darker());
    g2d.drawRect(2 * width / 3 - 2 / factor, 6 / factor, size, size);

    g2d.setColor(PAD_COLOR);
    g2d.fillRect(width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, size, size);
    g2d.setColor(PAD_COLOR.darker());
    g2d.drawRect(width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, size, size);

    g2d.setColor(PAD_COLOR);
    g2d.fillRect(2 * width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, size, size);
    g2d.setColor(PAD_COLOR.darker());
    g2d.drawRect(2 * width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, size, size);

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
  public Rectangle2D getBoardRectangle() {
    Point2D finalSecondPoint = getControlPoint(1);
    double offset = BODY_OFFSET_Y.convertToPixels();
    return new Rectangle2D.Double(Math.min(point.getX(), finalSecondPoint.getX()) - offset,
        Math.min(point.getY(), finalSecondPoint.getY()) - offset,
        Math.abs(finalSecondPoint.getX() - point.getX()) + offset,
        Math.abs(finalSecondPoint.getY() - point.getY()) + offset);
  }

  @Override
  public Rectangle2D getCachingBounds() {
    Point2D finalSecondPoint = getControlPoint(1);
    double offset = BODY_OFFSET_Y.convertToPixels();
    return new Rectangle2D.Double(Math.min(point.getX(), finalSecondPoint.getX()) - offset,
        Math.min(point.getY(), finalSecondPoint.getY()) - offset,
        Math.abs(finalSecondPoint.getX() - point.getX()) + offset + 2,
        Math.abs(finalSecondPoint.getY() - point.getY()) + offset + 2);
  }

  class Segment {
    int x;
    int y;
    int spanX;
    int spanY;
    String label;
    boolean includeTrace = true;

    public Segment(int x, int y, int spanX, int spanY, String label) {
      super();
      this.x = x;
      this.y = y;
      this.spanX = spanX;
      this.spanY = spanY;
      this.label = label;
    }
    
    public Segment(int x, int y, int spanX, int spanY, String label, boolean includeTrace) {
      this(x, y, spanX, spanY, label);
      this.includeTrace = includeTrace;
    }
  }

  class Pad {
    double x;
    double y;
    String label;
    boolean square;

    public Pad(double x, double y, String label, boolean square) {
      this.x = x;
      this.y = y;
      this.label = label;
      this.square = square;
    }

    public Pad(double x, double y, String label) {
      this(x, y, label, false);      
    }
  }
}
