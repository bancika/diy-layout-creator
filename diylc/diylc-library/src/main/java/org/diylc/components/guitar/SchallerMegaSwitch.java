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
package org.diylc.components.guitar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

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
import org.diylc.core.images.IconLoader;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

import static org.diylc.utils.SwitchUtils.getConnectedTerminals;

@ComponentDescriptor(name = "Schaller Megaswitch", category = "Guitar",
    author = "Branislav Stojkovic", description = "Several variations of Schaller Megaswitch",
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "SW",
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "Guitar Wiring Diagram", transformer = AngledComponentTransformer.class)
public class SchallerMegaSwitch extends AbstractAngledComponent<String> implements ISwitch,
    IContinuity {

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = Color.lightGray;
  private static Color WAFER_COLOR = FR4_LIGHT_COLOR;
  private static Color LUG_COLOR = GOLD_COLOR;

  private static Size BASE_WIDTH = new Size(10d, SizeUnit.mm);
  private static Size BASE_LENGTH = new Size(47.5d, SizeUnit.mm);
  private static Size WAFER_LENGTH = new Size(40d, SizeUnit.mm);
  private static Size WAFER_THICKNESS = new Size(1.27d, SizeUnit.mm);
  private static Size HOLE_SIZE = new Size(2d, SizeUnit.mm);
  private static Size HOLE_SPACING = new Size(41.2d, SizeUnit.mm);
  private static Size TERMINAL_WIDTH = new Size(2d, SizeUnit.mm);
  // private static Size TERMINAL_LENGTH = new Size(2d, SizeUnit.mm);
  private static Size TERMINAL_SPACING = new Size(0.1d, SizeUnit.in);

  // public static float LABEL_FONT_SIZE = 10f;
  public static Size LABEL_OFFSET = new Size(0.1d, SizeUnit.in);

  private String value = "";
  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  private MegaSwitchType type = MegaSwitchType.E;
  private Color labelColor = Color.gray;

  private transient Double labelDx = null;
  private transient Double labelDy = null;

  private Integer selectedPosition;
  private Boolean highlightConnectedTerminals;

  public SchallerMegaSwitch() {
    super();
    updateControlPoints();
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (componentState != ComponentState.DRAGGING) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
      g2d.fill(body[0]);
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : WAFER_COLOR);
      g2d.fill(body[1]);
      g2d.setComposite(oldComposite);
    }

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : BASE_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);

    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : WAFER_COLOR.darker();
    }

    g2d.draw(body[1]);

    g2d.setColor(LUG_COLOR);
    g2d.fill(body[2]);
    g2d.setColor(LUG_COLOR.darker());
    g2d.draw(body[2]);
    for (int i = 3; i < body.length; i++) {
      g2d.setColor(ISwitch.POLE_COLORS[i - 3]);
      g2d.fill(body[i]);
      g2d.setColor(ISwitch.POLE_COLORS[i - 3].darker());
      g2d.draw(body[i]);
    }

    g2d.setColor(labelColor);
    // g2d.setFont(project.getFont().deriveFont(LABEL_FONT_SIZE));
    if (type == MegaSwitchType.M) {
      for (int i = 0; i < controlPoints.length; i++) {
        double dx = getLabelDx();
        double dy = getLabelDy();        

        if (i >= controlPoints.length / 2) {
          dx = -dx;
          dy = -dy;
        }

        StringUtils.drawCenteredText(g2d, getControlPointNodeName(i), controlPoints[i].getX() + dx,
            controlPoints[i].getY() + dy, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }
    } else {
      for (int i = 0; i < controlPoints.length; i++) {
        StringUtils.drawCenteredText(g2d, getControlPointNodeName(i),
            controlPoints[i].getX() + getLabelDx(), controlPoints[i].getY() + getLabelDy(),
            HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }
    }

    int lastPointIdx =
        type == MegaSwitchType.M ? controlPoints.length / 2 - 1 : controlPoints.length - 1;

    double x =
        (controlPoints[0].getX() + controlPoints[lastPointIdx].getX()) / 2 + getLabelDx() * 2.25;
    double y =
        (controlPoints[0].getY() + controlPoints[lastPointIdx].getY()) / 2 + getLabelDy() * 2.25;

    double theta = getAngle().getValueRad() - Math.PI / 2;
    
    if ((theta >= Math.PI / 2 && theta <= Math.PI) || (theta < -Math.PI / 2 && theta > -Math.PI)) {
      theta += Math.PI;
    }

    if (theta != 0) {
      g2d.rotate(theta, x, y);
    }

    g2d.setFont(project.getFont().deriveFont((float) (project.getFont().getSize2D() * 1.25)));
    StringUtils.drawCenteredText(g2d, "Schaller Megaswitch " + type.toString(), x, y,
        HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }

  public Shape[] getBody() {
    if (body == null) {
      List<Set<Integer>> connectedTerminals = getConnectedTerminals(this, controlPoints.length);

      body = new Shape[3 + connectedTerminals.size()];

      int lastPointIdx =
          type == MegaSwitchType.M ? controlPoints.length / 2 - 1 : controlPoints.length - 1;

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
      if (type == MegaSwitchType.M) {
        xOffset = terminalSize + waferThickness / 4;
      }

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
      Area waferArea =
          new Area(new Rectangle2D.Double(waferX, waferY, waferThickness, waferLength));

      body[1] = waferArea;

      double theta = getAngle().getValueRad();

      for (int i = 0; i <= connectedTerminals.size(); i++) {
        body[2 + i] = new Area();
      }

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

        int finalI = i;
        int groupIndex = IntStream.range(0, connectedTerminals.size())
            .filter(j -> connectedTerminals.get(j).contains(finalI))
            .findFirst().orElse(-1);

        ((Area)body[3 + groupIndex]).add(terminal);
      }

      // Rotate if needed
      if (theta != 0) {
        // Skip the last two because terminals are already rotated
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
        for (int i = 0; i < body.length - 2; i++) {
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

    int pointCount = 0;

    switch (type) {
      case E:
      case P:
        pointCount = 7;
        break;
      case E_PLUS:
        pointCount = 9;
        break;
      case S:
      case T:
        pointCount = 8;
        break;
      case M:
        pointCount = 24;
        break;
    }

    controlPoints = new Point2D[pointCount];
    if (pointCount < 12) {
      terminalSpacing *= 1.5;
      for (int i = 0; i < controlPoints.length; i++) {
        controlPoints[i] = new Point2D.Double(x, y - i * terminalSpacing);;
      }
    } else {
      for (int i = 0; i < controlPoints.length / 2; i++) {
        controlPoints[i] = new Point2D.Double(x, y - i * terminalSpacing);
        controlPoints[controlPoints.length - i - 1] =
            new Point2D.Double(x + terminalWidth + waferThickness / 2, y - i * terminalSpacing);
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
    if (labelDy == null ) {
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

    Image logo = IconLoader.Schaller.getImage();
    g2d.drawImage(logo, 2, (int) (6.0 * width / 32), null);
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

//  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty
  public MegaSwitchType getType() {
    return type;
  }

  public void setType(MegaSwitchType type) {
    this.type = type;
    updateControlPoints();
    // Invalidate body
    this.body = null;
  }

  public enum MegaSwitchType {
    E("E", 5), E_PLUS("E+", 5), M("M", 5),
    P("P", 5), S("S", 5), T("T", 3);

    private String title;
    private final int positionCount;

    private MegaSwitchType(String title, int positionCount) {
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

  // @Override
  // public String getControlPointNodeName(int index) {
  // // we don't want the switch to produce any nodes, it just makes connections
  // return null;
  // }

  // switch stuff

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
    List<List<int[]>> connections = null;
    
    switch (type) {
      case E:
        connections = E_CONNECTIONS;
        break;
      case E_PLUS:
        connections = E_PLUS_CONNECTIONS;
        break;
      case S:
        connections = S_CONNECTIONS;
        break;
      case P:
        connections = P_CONNECTIONS;
        break;
      case T:
        connections = T_CONNECTIONS;
        break;
      case M:
        connections = M_CONNECTIONS;
        break;
    }
    
    if (connections != null) {
      List<int[]> positionConnections = connections.get(position);
      for (int[] arr : positionConnections) {
        if (arr[0] == index1 && arr[1] == index2) {
          return true;
        }
      }      
    }
    
    return false;   
  }

  @Override
  public boolean arePointsConnected(int index1, int index2) {
    if (this.selectedPosition == null) {
      return false;
    }
    return arePointsConnected(index1, index2, this.selectedPosition);
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    if (type == MegaSwitchType.M) {
      return String.valueOf((char) (index + 'A'));
    }
    return Integer.toString(index + 1);
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @DynamicEditableProperty(source = SchallerMegaSwitchPositionPropertyValueSource.class)
  @EditableProperty(name = "Selected Position")
  @Override
  public Integer getSelectedPosition() {
    return selectedPosition;
  }

  public void setSelectedPosition(Integer selectedPosition) {
    this.selectedPosition = selectedPosition;
    this.body = null;
  }

  @EditableProperty(name = "Highlight Connected")
  @Override
  public Boolean getHighlightConnectedTerminals() {
    if (highlightConnectedTerminals == null) {
      highlightConnectedTerminals = false;
    }
    return highlightConnectedTerminals;
  }

  public void setHighlightConnectedTerminals(Boolean highlightConnectedTerminals) {
    this.highlightConnectedTerminals = highlightConnectedTerminals;
    this.body = null;
  }

  private static final List<List<int[]>> E_CONNECTIONS = Arrays.asList(
      Arrays.asList(
          new int[] { 2, 6 }
          ), // position 1
      Arrays.asList(
          new int[] { 0, 2 },
          new int[] { 2, 6 },
          new int[] { 3, 5 }
          ), // position 2
      Arrays.asList(
          new int[] { 3, 4 },
          new int[] { 1, 2 },
          new int[] { 2, 5 }
          ), // position 3
      Arrays.asList(
          new int[] { 3, 4 },
          new int[] { 0, 2 },
          new int[] { 1, 2 }
          ), // position 4
      Arrays.asList(
          new int[] { 1, 2 }
          ) // position 5
      );
  
  private static final List<List<int[]>> E_PLUS_CONNECTIONS = Arrays.asList(
      Arrays.asList(
          new int[] { 2, 8 }
          ), // position 1
      Arrays.asList(
          new int[] { 3, 6 },
          new int[] { 2, 8 },
          new int[] { 0, 2 }
          ), // position 2
      Arrays.asList(
          new int[] { 3, 5 },
          new int[] { 2, 7 },
          new int[] { 1, 2 }
          ), // position 3
      Arrays.asList(
          new int[] { 3, 4 },
          new int[] { 0, 2 },
          new int[] { 1, 2 }
          ), // position 4
      Arrays.asList(
          new int[] { 1, 2 }
          ) // position 5
      );
  
  private static final List<List<int[]>> S_CONNECTIONS = Arrays.asList(
      Arrays.asList(
          new int[] { 2, 3 },
          new int[] { 6, 7 }
          ), // position 1
      Arrays.asList(
          new int[] { 2, 3 },
          new int[] { 0, 3 },
          new int[] { 6, 7 },
          new int[] { 4, 7 }
          ), // position 2
      Arrays.asList(
          new int[] { 0, 3 },
          new int[] { 4, 7 }
          ), // position 3
      Arrays.asList(
          new int[] { 1, 3 },
          new int[] { 0, 3 },
          new int[] { 5, 7 },
          new int[] { 4, 7 }
          ), // position 4
      Arrays.asList(
          new int[] { 1, 3 },
          new int[] { 5, 7 }
          ) // position 5
      );
  
  private static final List<List<int[]>> P_CONNECTIONS = Arrays.asList(
      Arrays.asList(
          new int[] { 1, 6 },
          new int[] { 3, 4 }
          ), // position 1
      Arrays.asList(
          new int[] { 1, 5 },
          new int[] { 0, 4 },
          new int[] { 1, 2 },
          new int[] { 3, 4 }
          ), // position 2
      Arrays.asList(
          new int[] { 1, 6 },
          new int[] { 0, 2 }
          ), // position 3
      Arrays.asList(
          new int[] { 1, 6 },
          new int[] { 0, 1 },
          new int[] { 2, 4 },
          new int[] { 3, 4 }
          ), // position 4
      Arrays.asList(
          new int[] { 1, 5 }
          ) // position 5
      );
  
  private static final List<List<int[]>> T_CONNECTIONS = Arrays.asList(
      Arrays.asList(
          new int[] { 2, 3 },
          new int[] { 6, 7 }
          ), // position 1
      Arrays.asList(
          new int[] { 0, 3 },
          new int[] { 4, 7 }
          ), // position 2
      Arrays.asList(
          new int[] { 1, 3 },
          new int[] { 5, 7 }
          ) // position 3
      );
  
  private static final List<List<int[]>> M_CONNECTIONS = Arrays.asList(
      Arrays.asList(
          new int[] { 4, 5 },
          new int[] { 10, 11 },
          new int[] { 16, 17 },
          new int[] { 22, 23 }
          ), // position 1
      Arrays.asList(
          new int[] { 3, 5 },
          new int[] { 9, 11 },
          new int[] { 15, 17 },
          new int[] { 21, 23 }
          ), // position 2
      Arrays.asList(
          new int[] { 2, 5 },
          new int[] { 8, 11 },
          new int[] { 14, 17 },
          new int[] { 20, 23 }
          ), // position 3
      Arrays.asList(
          new int[] { 1, 5 },
          new int[] { 7, 11 },
          new int[] { 13, 17 },
          new int[] { 19, 23 }
          ), // position 4
      Arrays.asList(
          new int[] { 0, 5 },
          new int[] { 6, 11 },
          new int[] { 12, 17 },
          new int[] { 18, 23 }
          ) // position 5
      );
}
