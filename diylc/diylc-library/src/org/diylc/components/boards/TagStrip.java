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
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.transform.TagStripTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IContinuity;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Tag Strip", author = "Branislav Stojkovic", category = "Boards",
    instanceNamePrefix = "TS", description = "Row of terminals for point-to-point construction",
    zOrder = IDIYComponent.BOARD, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME, transformer = TagStripTransformer.class)
public class TagStrip extends AbstractTransparentComponent<String> implements IContinuity {

  private static final long serialVersionUID = 1L;

  public static Color BOARD_COLOR = PHENOLIC_DARK_COLOR;
  public static Color TERMINAL_COLOR = Color.lightGray;
  public static Color TERMINAL_BORDER_COLOR = TERMINAL_COLOR.darker();
  public static int EDGE_RADIUS = 2;
  public static Size MOUNTING_HOLE_SIZE = new Size(0.07d, SizeUnit.in);
  public static Size MOUNTING_HOLE_DISTANCE = new Size(0.25d, SizeUnit.in);
  public static Size TERMINAL_THICKNESS = new Size(0.04d, SizeUnit.in);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  private int terminalCount = 7;
  private static Size BOARD_THICKNESS = new Size(0.05d, SizeUnit.in);
  private Size terminalSpacing = new Size(0.375d, SizeUnit.in);
  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  private Color boardColor = BOARD_COLOR;
  
  private TagStripMount mount = TagStripMount.Central;

  transient private Area[] body;

  public TagStrip() {
    super();
    updateControlPoints();
  }

  @EditableProperty
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }

  @EditableProperty(name = "Terminals")
  public int getTerminalCount() {
    return terminalCount;
  }

  public void setTerminalCount(int terminalCount) {
    this.terminalCount = terminalCount;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty(name = "Terminal Spacing", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getTerminalSpacing() {
    return terminalSpacing;
  }

  public void setTerminalSpacing(Size pinSpacing) {
    this.terminalSpacing = pinSpacing;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  private void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    controlPoints = new Point2D[getTerminalCount()];
    controlPoints[0] = firstPoint;
    int pinSpacing = (int) this.terminalSpacing.convertToPixels();
    // Update control points.
    int dx1;
    int dy1;
    for (int i = 0; i < getTerminalCount(); i++) {
      switch (orientation) {
        case DEFAULT:
          dx1 = 0;
          dy1 = i * pinSpacing;        
          break;
        case _90:
          dx1 = -i * pinSpacing;
          dy1 = 0;
          break;
        case _180:
          dx1 = 0;
          dy1 = -i * pinSpacing;
          break;
        case _270:
          dx1 = i * pinSpacing;
          dy1 = 0;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      controlPoints[i] = new Point2D.Double(firstPoint.getX() + dx1, firstPoint.getY() + dy1);  
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[1 + getTerminalCount()];
      double x = controlPoints[0].getX();
      double y = controlPoints[0].getY();
      int width;
      int height;
      int terminalSpacing = (int) getTerminalSpacing().convertToPixels();
      int boardThickness = getClosestOdd(BOARD_THICKNESS.convertToPixels());
      int boardLength = (int) ((getTerminalCount() - 0.5) * terminalSpacing + 2 * boardThickness);
      int holeSize = getClosestOdd(MOUNTING_HOLE_SIZE.convertToPixels());      
      int terminalThickness = (int) TERMINAL_THICKNESS.convertToPixels();
      int terminalSize = terminalSpacing / 2;
      int holeDistance = terminalSize / 2 + terminalThickness / 2 + 1;
      int terminalWidth;
      int terminalHeight;
      int holeDx;
      int holeDy;
      switch (orientation) {
        case DEFAULT:
          width = boardThickness;
          height = boardLength;
          terminalWidth = terminalThickness;
          terminalHeight = terminalSize;
          x -= boardThickness + terminalThickness / 2;
          y -= terminalSize / 2 + boardThickness;
          holeDx = holeDistance;
          holeDy = 0;
          break;
        case _90:
          width = boardLength;
          height = boardThickness;
          terminalWidth = terminalSize;
          terminalHeight = terminalThickness;
          x += boardThickness - boardLength + terminalSize / 2;
          y -= boardThickness + terminalThickness / 2;
          holeDx = 0;
          holeDy = holeDistance;
          break;
        case _180:
          width = boardThickness;
          height = boardLength;
          terminalWidth = terminalThickness;
          terminalHeight = terminalSize;
          x += terminalThickness / 2;
          y += boardThickness - boardLength + terminalSize / 2;
          holeDx = -holeDistance;
          holeDy = 0;
          break;
        case _270:
          width = boardLength;
          height = boardThickness;
          terminalWidth = terminalSize;
          terminalHeight = terminalThickness;
          x -= terminalSize / 2 + boardThickness;
          y -= -terminalThickness / 2;
          holeDx = 0;
          holeDy = -holeDistance;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      Area bodyArea = new Area(new Rectangle2D.Double(x, y, width, height));
      body[0] = bodyArea;

      for (int i = 0; i < getTerminalCount(); i++) {
        Point2D p1 = getControlPoint(i);       
        Area terminal =
            new Area(new Rectangle2D.Double(p1.getX() - terminalWidth/ 2, p1.getY() - terminalHeight / 2, terminalWidth, terminalHeight));    

        body[1 + i] = terminal;
        
        if ((getMount() == TagStripMount.Central && i == getTerminalCount() / 2) || (getMount() == TagStripMount.Outside && (i == 0 || i == getTerminalCount() - 1))) {
          Area mount = new Area(new Rectangle2D.Double(p1.getX() + holeDx - terminalSize / 2, p1.getY() + holeDy - terminalSize / 2, terminalSize, terminalSize));
          mount.subtract(new Area(new Ellipse2D.Double(p1.getX() + holeDx - holeSize / 2, p1.getY() + holeDy - holeSize / 2, holeSize, holeSize)));
          body[1 + i].add(mount);          
        }
      }      
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Area[] area = getBody();
    Area mainArea = area[0];

    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBoardColor());
    g2d.fill(mainArea);

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : getBoardColor().darker();
    }
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    g2d.setColor(finalBorderColor);
    g2d.draw(mainArea);

    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : TERMINAL_COLOR);
    drawingObserver.startTrackingContinuityArea(true);  
    for (int i = 1; i < body.length; i++)
      g2d.fill(body[i]);
    drawingObserver.stopTrackingContinuityArea();

    Color finalTerminalBorderColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalTerminalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalTerminalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : TERMINAL_BORDER_COLOR;
    }

    g2d.setColor(finalTerminalBorderColor);
    for (int i = 1; i < body.length; i++)
      g2d.draw(body[i]);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(BOARD_COLOR);
    int thickness = 2 * width / 32;
    g2d.fillRect(0, height / 2 - thickness / 2, width, thickness);
    g2d.setColor(TERMINAL_COLOR.darker());
    int terminalWidth = 7 * width / 32;
    g2d.fillRect(2, height / 2 + thickness / 2, terminalWidth, thickness);
    g2d.fillRect((int) (2 + terminalWidth * 1.5), height / 2 + thickness / 2, terminalWidth, thickness);
    g2d.fillRect((int) (2 + terminalWidth * 3), height / 2 + thickness / 2, terminalWidth, thickness);
    g2d.setColor(TERMINAL_COLOR);
    g2d.fillRect((int) (2 + terminalWidth * 1.5), height / 2 + thickness / 2 + 1, terminalWidth, terminalWidth);
    g2d.setColor(Color.white);
    g2d.fillOval((int) (2 + terminalWidth * 1.75), height / 2 + thickness / 2 + 1 + terminalWidth / 2, terminalWidth / 2, terminalWidth / 2);
//    int radius = 6 * width / 32;
//    int holeSize = 3 * width / 32;
//    g2d.setColor(BOARD_COLOR);
//    g2d.fillRect(width / 4, 1, width / 2, height - 4);
//    g2d.setColor(BORDER_COLOR);
//    g2d.drawRect(width / 4, 1, width / 2, height - 4);
//    int terminalSize = getClosestOdd(height / 5);
//    Area terminal =
//        new Area(new RoundRectangle2D.Double(2 * width / 32, height / 5, width - 4 * width / 32, terminalSize, radius,
//            radius));
//    terminal.subtract(new Area(new Ellipse2D.Double(2 * width / 32 + holeSize, height * 3 / 10 - holeSize / 2, holeSize, holeSize)));
//    terminal.subtract(new Area(new Ellipse2D.Double(width - 2 * width / 32 - holeSize * 2, height * 3 / 10 - holeSize / 2, holeSize, holeSize)));
//    
//    g2d.setColor(TERMINAL_COLOR);
//    g2d.fill(terminal);
//    g2d.setColor(TERMINAL_BORDER_COLOR);
//    g2d.draw(terminal);
//    g2d.translate(0, height * 2 / 5);
//    g2d.setColor(TERMINAL_COLOR);
//    g2d.fill(terminal);
//    g2d.setColor(TERMINAL_BORDER_COLOR);
//    g2d.draw(terminal);
  }

  @EditableProperty(name = "Board")
  public Color getBoardColor() {
    if (boardColor == null) {
      boardColor = BOARD_COLOR;
    }
    return boardColor;
  }

  public void setBoardColor(Color bodyColor) {
    this.boardColor = bodyColor;
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    return "TerminalStrip" + index;
  }
  
  @EditableProperty(name = "Mounting Lugs")
  public TagStripMount getMount() {
    return mount;
  }
  
  public void setMount(TagStripMount mount) {
    this.mount = mount;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @Override
  public boolean arePointsConnected(int index1, int index2) {
    return false;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
  
  public enum TagStripMount {
    Central, Outside;
  }
}
