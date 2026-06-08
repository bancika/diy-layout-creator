/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
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
package org.diylc.components.guitar;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;

import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractAngledComponent;
import org.diylc.components.transform.AngledComponentTransformer;
import org.diylc.core.*;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.DynamicEditableProperty;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Freeway Blade", category = "Guitar", author = "Martin Morrell, Branislav Stojkovic", description = "Several variations of Freeway Blade switches", zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "SW", keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "Guitar Wiring Diagram", transformer = AngledComponentTransformer.class)
public class FreewayBlade extends AbstractAngledComponent<String> implements ISwitch {

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = Color.lightGray;
  private static Color WAFER_COLOR = FR4_LIGHT_COLOR;
  private static Color LUG_COLOR = METAL_COLOR;

  private static Size BASE_WIDTH = new Size(10d, SizeUnit.mm);
  private static Size BASE_LENGTH = new Size(47.5d, SizeUnit.mm);
  private static Size WAFER_LENGTH = new Size(40d, SizeUnit.mm);
  private static Size WAFER_THICKNESS = new Size(1.27d, SizeUnit.mm);
  private static Size HOLE_SIZE = new Size(2d, SizeUnit.mm);
  private static Size HOLE_SPACING = new Size(41.2d, SizeUnit.mm);
  private static Size TERMINAL_WIDTH = new Size(2d, SizeUnit.mm);
  private static Size TERMINAL_SPACING = new Size(0.1d, SizeUnit.in);

  public static Size LABEL_OFFSET = new Size(0.11d, SizeUnit.in);

  private String value = "";
  private Point2D[] controlPoints = new Point2D[] { new Point2D.Double(0, 0) };
  private FreewayBladeType type = FreewayBladeType.B3_01;
  private Color labelColor = Color.gray;

  private transient Double labelDx = null;
  private transient Double labelDy = null;

  private Integer selectedPosition;
  private Boolean showMarkers;

  public FreewayBlade() {
    super();
    updateControlPoints();
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (componentState != ComponentState.DRAGGING) {
      Composite oldComposite = applyAlpha(g2d, componentState);
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
      g2d.fill(body[0]);
      drawingObserver.stopTracking();
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : WAFER_COLOR);
      g2d.fill(body[1]);
      g2d.setComposite(oldComposite);
    }

    drawingObserver.stopTracking();

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalBorderColor = componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
          ? SELECTION_COLOR
          : theme.getOutlineColor();
    } else {
      finalBorderColor = componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
          ? SELECTION_COLOR
          : BASE_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);

    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalBorderColor = componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
          ? SELECTION_COLOR
          : theme.getOutlineColor();
    } else {
      finalBorderColor = componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
          ? SELECTION_COLOR
          : WAFER_COLOR.darker();
    }

    g2d.draw(body[1]);

    g2d.setColor(LUG_COLOR);
    drawingObserver.startTracking();
    g2d.fill(body[2]);
    drawingObserver.stopTracking();
    g2d.setColor(LUG_COLOR.darker());
    g2d.draw(body[2]);
    for (int i = 3; i < body.length; i++) {
      g2d.setColor(ISwitch.POLE_COLORS[i - 3]);
      drawingObserver.startTracking();
      g2d.fill(body[i]);
      drawingObserver.stopTracking();
      g2d.setColor(ISwitch.POLE_COLORS[i - 3].darker());
      g2d.draw(body[i]);
    }

    g2d.setColor(labelColor);
    for (int i = 0; i < controlPoints.length; i++) {
      StringUtils.drawCenteredText(g2d, getControlPointNodeName(i),
          controlPoints[i].getX() + getLabelDx(), controlPoints[i].getY() + getLabelDy(),
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    int lastPointIdx = controlPoints.length - 1;

    double x = (controlPoints[0].getX() + controlPoints[lastPointIdx].getX()) / 2 + getLabelDx() * 2.25;
    double y = (controlPoints[0].getY() + controlPoints[lastPointIdx].getY()) / 2 + getLabelDy() * 2.25;

    double theta = getAngle().getValueRad() - Math.PI / 2;

    if ((theta >= Math.PI / 2 && theta <= Math.PI) || (theta < -Math.PI / 2 && theta > -Math.PI)) {
      theta += Math.PI;
    }

    if (theta != 0) {
      g2d.rotate(theta, x, y);
    }

    g2d.setFont(project.getFont().deriveFont((float) (project.getFont().getSize2D() * 1.25)));
    StringUtils.drawCenteredText(g2d, "Freeway Blade " + type.toString(), x, y,
        HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }

  public Shape[] getBody() {
    if (body == null) {

      body = new Shape[3];

      int lastPointIdx = controlPoints.length - 1;

      double x = (controlPoints[0].getX() + controlPoints[lastPointIdx].getX()) / 2;
      double y = (controlPoints[0].getY() + controlPoints[lastPointIdx].getY()) / 2;
      int baseWidth = (int) BASE_WIDTH.convertToPixels();
      int baseLength = (int) BASE_LENGTH.convertToPixels();
      int holeSize = getClosestOdd(HOLE_SIZE.convertToPixels());
      int holeSpacing = (int) HOLE_SPACING.convertToPixels();
      int waferLength = (int) WAFER_LENGTH.convertToPixels();
      int waferThickness = (int) WAFER_THICKNESS.convertToPixels();
      int terminalSize = getClosestOdd(TERMINAL_WIDTH.convertToPixels());

      int yOffset = controlPoints.length;
      if (yOffset > 12) {
        yOffset /= 2;
      }

      int xOffset = 0;
      double baseX = x - terminalSize / 2 - baseWidth + waferThickness / 2 + xOffset;
      double baseY = y - baseLength / 2;
      Area baseArea = new Area(new Rectangle2D.Double(baseX, baseY, baseWidth, baseLength));
      baseArea.subtract(new Area(new Ellipse2D.Double(baseX + baseWidth / 2 - holeSize / 2,
          baseY + (baseLength - holeSpacing) / 2 - holeSize / 2, holeSize, holeSize)));
      baseArea.subtract(new Area(new Ellipse2D.Double(baseX + baseWidth / 2 - holeSize / 2,
          baseY + (baseLength - holeSpacing) / 2 - holeSize / 2 + holeSpacing, holeSize,
          holeSize)));
      body[0] = baseArea;

      double waferX = x + xOffset - terminalSize / 2 - waferThickness / 2;
      double waferY = y - waferLength / 2;
      Area waferArea = new Area(new Rectangle2D.Double(waferX, waferY, waferThickness, waferLength));

      body[1] = waferArea;
      body[2] = new Area();

      double theta = getAngle().getValueRad();

      for (int i = 0; i < controlPoints.length; i++) {
        Point2D point = controlPoints[i];
        Area terminal = new Area(new RoundRectangle2D.Double(point.getX() - terminalSize / 2,
            point.getY() - terminalSize / 2, terminalSize, terminalSize, terminalSize / 2,
            terminalSize / 2));
        terminal.subtract(new Area(new RoundRectangle2D.Double(point.getX() - terminalSize / 4,
            point.getY() - terminalSize / 4, terminalSize / 2, terminalSize / 2, terminalSize / 2,
            terminalSize / 2)));

        if (theta != 0) {
          // Skip the last two because terminals are already rotated
          AffineTransform rotation = AffineTransform.getRotateInstance(theta, point.getX(), point.getY());
          terminal.transform(rotation);
        }

        ((Area) body[2]).add(terminal);
      }

      // Rotate if needed
      if (theta != 0) {
        // Skip the last one because terminals are already rotated
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
        for (int i = 0; i < body.length - 1; i++) {
          Shape shape = body[i];
          Area area = (Area) shape;
          area.transform(rotation);
        }
      }
    }
    return body;
  }

  @SuppressWarnings("incomplete-switch")
  protected void updateControlPoints() {
    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();
    double terminalSpacing = TERMINAL_SPACING.convertToPixels();
    double terminalWidth = TERMINAL_WIDTH.convertToPixels();
    int waferThickness = (int) WAFER_THICKNESS.convertToPixels();

    int pointCount = switch (type) {
      case B3_01 -> 7;
      case B5_01 -> 10;
      case B5_02 -> 11;
    };

    controlPoints = new Point2D[pointCount];
    if (pointCount < 12) {
      terminalSpacing *= 1.5;
      for (int i = 0; i < controlPoints.length; i++) {
        controlPoints[i] = new Point2D.Double(x, y - i * terminalSpacing);
        ;
      }
    } else {
      for (int i = 0; i < controlPoints.length / 2; i++) {
        controlPoints[i] = new Point2D.Double(x, y - i * terminalSpacing);
        controlPoints[controlPoints.length - i - 1] = new Point2D.Double(x + terminalWidth + waferThickness / 2,
            y - i * terminalSpacing);
      }
    }

    // Rotate if needed
    double theta = getAngle().getValueRad();
    if (theta != 0) {
      AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
      for (Point2D point : controlPoints) {
        rotation.transform(point, point);
      }
    }
  }

  @Override
  public void setAngle(Angle angle) {
    super.setAngle(angle);
    updateLabelPositions();
  }

  private void updateLabelPositions() {
    double labelOffset = LABEL_OFFSET.convertToPixels();
    double theta = getAngle().getValueRad() - Math.PI;
    labelDx = Math.cos(theta) * labelOffset;
    labelDy = Math.sin(theta) * labelOffset;
  }

  private double getLabelDx() {
    if (labelDx == null) {
      updateLabelPositions();
    }
    return labelDx;
  }

  public double getLabelDy() {
    if (labelDy == null) {
      updateLabelPositions();
    }
    return labelDy;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setClip(width / 32, width / 32, width, height);
    g2d.setColor(BASE_COLOR);
    g2d.fillRect(0, 0, width * 3 / 4, height);
    g2d.setColor(BASE_COLOR.darker());
    g2d.drawRect(0, 0, width * 3 / 4, height);
    g2d.setColor(WAFER_COLOR);
    g2d.fillRect(width / 8 * 4, 0, width / 8, height);
    g2d.setColor(WAFER_COLOR.darker());
    g2d.drawRect(width / 8 * 4, 0, width / 8, height);
    Area terminals = new Area();
    int terminalLength = getClosestOdd(7 * width / 32);
    int terminalWidth = getClosestOdd(7 * width / 32);
    Area terminal = new Area(new RoundRectangle2D.Double(width / 16 * 9, 4 * width / 32,
        terminalLength, terminalWidth, terminalWidth / 2, terminalWidth / 2));
    terminal.subtract(new Area(new RoundRectangle2D.Double(width / 16 * 9 + terminalLength / 4 + 1,
        4 * width / 32 + terminalWidth / 4 + 1, terminalLength / 2, terminalWidth / 2,
        terminalWidth / 4, terminalWidth / 4)));
    terminals.add(terminal);
    terminal = new Area(terminal);
    terminal.transform(AffineTransform.getTranslateInstance(0, terminalWidth + 2 * width / 32));
    terminals.add(terminal);
    terminal = new Area(terminal);
    terminal.transform(AffineTransform.getTranslateInstance(0, terminalWidth + 2 * width / 32));
    terminals.add(terminal);
    g2d.setColor(LUG_COLOR);
    g2d.fill(terminals);
    g2d.setColor(LUG_COLOR.darker());
    g2d.draw(terminals);
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    this.controlPoints[index].setLocation(point);
    // Invalidate the body
    body = null;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  // @EditableProperty(name = "Label")
  public Color getLabelColor() {
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty
  public FreewayBladeType getType() {
    return type;
  }

  public void setType(FreewayBladeType type) {
    this.type = type;
    updateControlPoints();
    // Invalidate body
    this.body = null;
  }

  public enum FreewayBladeType {
    B3_01("3B3-01", 6),
    B5_01("5B5-01", 10),
    B5_02("5B5-02", 10);

    private final String title;
    private final int positionCount;

    private FreewayBladeType(String title, int positionCount) {
      this.title = title;
      this.positionCount = positionCount;
    }

    public int getPositionCount() {
      return positionCount;
    }

    @Override
    public String toString() {
      return title;
    }
  }

  @Override
  public int getPositionCount() {
    return type.getPositionCount();
  }

  @Override
  public String getPositionName(int position) {
    return Integer.toString(position + 1);
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    List<List<int[]>> connections = switch (type) {
      case B3_01 -> B3_01_CONNECTIONS;
      case B5_01 -> B5_01_CONNECTIONS;
      case B5_02 -> B5_02_CONNECTIONS;
    };

    List<int[]> positionConnections = connections.get(position);
    for (int[] arr : positionConnections) {
      if (arr[0] == index1 && arr[1] == index2) {
        return true;
      }
    }

    return false;
  }

  @Override
  public String getControlPointNodeName(int index) {
    switch (type) {
      case B3_01: {
        String[] arr = { "GD", "BH", "B", "OP", "NG", "A", "NH" };
        return arr[index];
      }
      case B5_01: {
        String[] arr = { "BH", "BT", "BG", "MH", "A", "B", "GD", "OP", "NH", "NG" };
        return arr[index];
      }
      case B5_02: {
        String[] arr = { "BH", "2+", "3+", "4+", "G3", "GD", "OP", "MH", "NT", "BT", "NH" };
        return arr[index];
      }
      default:
        return "";
    }
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @DynamicEditableProperty(source = FreewayBladePositionPropertyValueSource.class)
  @EditableProperty(name = "Selected Position")
  @Override
  public Integer getSelectedPosition() {
    return selectedPosition;
  }

  public void setSelectedPosition(Integer selectedPosition) {
    this.selectedPosition = selectedPosition;
    this.body = null;
  }

  @EditableProperty(name = "Markers")
  public Boolean getShowMarkers() {
    if (showMarkers == null) {
      showMarkers = false;
    }
    return showMarkers;
  }

  public void setShowMarkers(Boolean showMarkers) {
    this.showMarkers = showMarkers;
  }

  private static final List<List<int[]>> B3_01_CONNECTIONS = Arrays.asList(
      Arrays.asList(
          new int[] { 3, 1 }), // position 1
      Arrays.asList(
          new int[] { 6, 3 },
          new int[] { 3, 1 },
          new int[] { 6, 1 },
          new int[] { 4, 0 }), // position 2
      Arrays.asList(
          new int[] { 6, 3 },
          new int[] { 4, 0 }), // position 3
      Arrays.asList(
          new int[] { 5, 3 },
          new int[] { 3, 1 },
          new int[] { 1, 5 },
          new int[] { 0, 6 }), // position 4
      Arrays.asList(
          new int[] { 5, 3 },
          new int[] { 5, 2 },
          new int[] { 3, 2 },
          new int[] { 6, 1 }), // position 5
      Arrays.asList(
          new int[] { 6, 3 },
          new int[] { 6, 2 },
          new int[] { 3, 2 },
          new int[] { 4, 1 }) // position 6
  );

  private static final List<List<int[]>> B5_01_CONNECTIONS = Arrays.asList(
      Arrays.asList(
          new int[] { 7, 0 },
          new int[] { 6, 1 }), // position 1
      Arrays.asList(
          new int[] { 7, 0 },
          new int[] { 7, 3 },
          new int[] { 3, 0 },
          new int[] { 6, 1 }), // position 2
      Arrays.asList(
          new int[] { 7, 3 }), // position 3
      Arrays.asList(
          new int[] { 8, 7 },
          new int[] { 8, 3 },
          new int[] { 7, 3 },
          new int[] { 9, 6 }), // position 4
      Arrays.asList(
          new int[] { 8, 7 },
          new int[] { 9, 6 }), // position 5
      Arrays.asList(
          new int[] { 7, 0 },
          new int[] { 9, 4 },
          new int[] { 5, 2 }), // position 6
      Arrays.asList(
          new int[] { 8, 7 },
          new int[] { 8, 3 },
          new int[] { 8, 0 },
          new int[] { 7, 3 },
          new int[] { 7, 0 },
          new int[] { 3, 0 },
          new int[] { 9, 6 },
          new int[] { 9, 1 },
          new int[] { 6, 1 }), // position 7
      Arrays.asList(
          new int[] { 8, 7 },
          new int[] { 0, 9 },
          new int[] { 6, 1 }), // position 8
      Arrays.asList(
          new int[] { 8, 7 },
          new int[] { 8, 0 },
          new int[] { 7, 0 },
          new int[] { 9, 6 },
          new int[] { 9, 1 },
          new int[] { 6, 1 }), // position 9
      Arrays.asList(
          new int[] { 8, 7 },
          new int[] { 9, 3 },
          new int[] { 6, 1 }) // position 10
  );

  private static final List<List<int[]>> B5_02_CONNECTIONS = Arrays.asList(
      Arrays.asList(
          new int[] { 6, 0 }), // position 1
      Arrays.asList(
          new int[] { 6, 1 },
          new int[] { 6, 0 },
          new int[] { 1, 0 }), // position 2
      Arrays.asList(
          new int[] { 10, 6 },
          new int[] { 10, 2 },
          new int[] { 10, 0 },
          new int[] { 6, 2 },
          new int[] { 6, 0 },
          new int[] { 2, 0 },
          new int[] { 9, 8 },
          new int[] { 9, 4 },
          new int[] { 8, 4 }), // position 3
      Arrays.asList(
          new int[] { 10, 6 },
          new int[] { 10, 3 },
          new int[] { 6, 3 }), // position 4
      Arrays.asList(
          new int[] { 10, 6 }), // position 5
      Arrays.asList(
          new int[] { 6, 0 },
          new int[] { 9, 5 }), // position 6
      Arrays.asList(
          new int[] { 7, 6 },
          new int[] { 7, 0 },
          new int[] { 6, 0 },
          new int[] { 9, 5 }), // position 7
      Arrays.asList(
          new int[] { 7, 6 }), // position 8
      Arrays.asList(
          new int[] { 10, 7 },
          new int[] { 10, 6 },
          new int[] { 7, 6 },
          new int[] { 8, 5 }), // position 9
      Arrays.asList(
          new int[] { 10, 6 },
          new int[] { 8, 5 }) // position 10
  );
}
