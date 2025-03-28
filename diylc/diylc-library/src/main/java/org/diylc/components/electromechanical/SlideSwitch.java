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
package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
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
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.awt.StringUtils;
import org.diylc.common.*;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.guitar.Freeway3x4_03SwitchPositionPropertyValueSource;
import org.diylc.components.transform.MiniToggleSwitchTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.DynamicEditableProperty;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.gerber.IGerberComponentSimple;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;
import org.diylc.utils.SwitchUtils;

@ComponentDescriptor(name = "Slide Switch", category = "Electro-Mechanical",
    author = "Branislav Stojkovic", description = "Panel mounted slide switch",
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "SW", autoEdit = false,
    enableCache = true, transformer = MiniToggleSwitchTransformer.class)
public class SlideSwitch extends AbstractTransparentComponent<SlideSwitchType> implements ISwitch, IGerberComponentSimple {

  private static final long serialVersionUID = 1L;

  private static Size ROW_SPACING = new Size(0.25d, SizeUnit.in);
  private static Size COLUMN_SPACING = new Size(0.2d, SizeUnit.in);
  private static Size MARGIN = new Size(0.08d, SizeUnit.in);
  private static Size LUG_WIDTH = new Size(0.020d, SizeUnit.in);
  private static Size LUG_THICKNESS = new Size(0.1d, SizeUnit.in);
  private static Size HOLE_DIAMETER = new Size(3d, SizeUnit.mm);
  private static Size MARKER_OFFSET = new Size(0.05d, SizeUnit.in);

  private static Color BODY_COLOR = PHENOLIC_DARK_COLOR;
  private static Color BORDER_COLOR = Color.gray;
  public static Color TERMINAL_COLOR = METAL_COLOR;
  public static Color LABEL_COLOR = Color.white;
  public static Color BRACKET_COLOR = Color.lightGray;
  public static Color MARKER_COLOR = Color.lightGray;

  protected Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  transient protected Shape[] body;
  protected SlideSwitchType switchType = SlideSwitchType.DP3T;
  private OrientationHV orientation = OrientationHV.VERTICAL;
  private Size rowSpacing = ROW_SPACING;
  private Size columnSpacing = COLUMN_SPACING;

  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  private Color bracketColor = BRACKET_COLOR;
  private Boolean showBracket = true;

  private Integer selectedPosition;
  private Boolean showMarkers;

  public SlideSwitch() {
    super();
    updateControlPoints();
  }

  private void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    int rowSpacing = (int) getRowSpacing().convertToPixels();
    int columnSpacing = (int) getColumnSpacing().convertToPixels();
    switch (switchType) {
      case SPDT:
        controlPoints = new Point2D[] {firstPoint,
            new Point2D.Double(firstPoint.getX(), firstPoint.getY() + rowSpacing),
            new Point2D.Double(firstPoint.getX(), firstPoint.getY() + 2 * rowSpacing)};
        break;
      case DPDT:
        controlPoints = new Point2D[] {firstPoint,
            new Point2D.Double(firstPoint.getX(), firstPoint.getY() + rowSpacing),
            new Point2D.Double(firstPoint.getX(), firstPoint.getY() + 2 * rowSpacing),
            new Point2D.Double(firstPoint.getX() + columnSpacing, firstPoint.getY()),
            new Point2D.Double(firstPoint.getX() + columnSpacing, firstPoint.getY() + rowSpacing),
            new Point2D.Double(firstPoint.getX() + columnSpacing,
                firstPoint.getY() + 2 * rowSpacing)};
        break;
      case DP3T:
        controlPoints = new Point2D[] {firstPoint,
            new Point2D.Double(firstPoint.getX(), firstPoint.getY() + rowSpacing),
            new Point2D.Double(firstPoint.getX(), firstPoint.getY() + 2 * rowSpacing),
            new Point2D.Double(firstPoint.getX(), firstPoint.getY() + 3 * rowSpacing),
            new Point2D.Double(firstPoint.getX() + columnSpacing, firstPoint.getY()),
            new Point2D.Double(firstPoint.getX() + columnSpacing, firstPoint.getY() + rowSpacing),
            new Point2D.Double(firstPoint.getX() + columnSpacing,
                firstPoint.getY() + 2 * rowSpacing),
            new Point2D.Double(firstPoint.getX() + columnSpacing,
                firstPoint.getY() + 3 * rowSpacing)};
        break;
    }

    if (getOrientation() == OrientationHV.HORIZONTAL) {
      AffineTransform xform =
          AffineTransform.getRotateInstance(-Math.PI / 2, firstPoint.getX(), firstPoint.getY());
      for (int i = 1; i < controlPoints.length; i++) {
        xform.transform(controlPoints[i], controlPoints[i]);
      }
    }
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
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    // Reset body shape.
    body = null;
  }

  @EditableProperty(name = "Type")
  @Override
  public SlideSwitchType getValue() {
    return switchType;
  }

  @Override
  public void setValue(SlideSwitchType value) {
    this.switchType = value;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }

  @EditableProperty
  public OrientationHV getOrientation() {
    if (orientation == null) {
      orientation = OrientationHV.VERTICAL;
    }
    return orientation;
  }

  public void setOrientation(OrientationHV orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }

  @EditableProperty(name = "Row Spacing", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getRowSpacing() {
    return rowSpacing;
  }

  public void setRowSpacing(Size rowSpacing) {
    this.rowSpacing = rowSpacing;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }

  @EditableProperty(name = "Column Spacing", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getColumnSpacing() {
    return columnSpacing;
  }

  public void setColumnSpacing(Size columnSpacing) {
    this.columnSpacing = columnSpacing;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Shape[] body = getBody();
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
        Constants.DEFAULT_THEME);
    // Draw body if available.
    if (body != null) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }

      if (getShowBracket()) {
        g2d.setColor(getBracketColor());
        g2d.fill(body[1]);
      }

      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
      g2d.fill(body[0]);

      g2d.setComposite(oldComposite);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      Color finalBorderColor;
      if (outlineMode) {
        finalBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
                ? SELECTION_COLOR
                : theme.getOutlineColor();
      } else {
        finalBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
                ? SELECTION_COLOR
                : getBorderColor();
      }

      g2d.setColor(finalBorderColor);
      if (getShowBracket()) {
        g2d.draw(body[1]);
      }
      g2d.draw(body[0]);
    }
    // Do not track these changes because the whole switch has been tracked
    // so far.
    drawingObserver.stopTracking();
    // Draw lugs.

    int lugWidth;
    int lugHeight;

    if (getOrientation() == OrientationHV.HORIZONTAL) {
      lugHeight = getClosestOdd((int) LUG_WIDTH.convertToPixels());
      lugWidth = getClosestOdd((int) LUG_THICKNESS.convertToPixels());
    } else {
      lugWidth = getClosestOdd((int) LUG_WIDTH.convertToPixels());
      lugHeight = getClosestOdd((int) LUG_THICKNESS.convertToPixels());
    }

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    for (int i = 0; i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      if (outlineMode) {
        g2d.setColor(theme.getOutlineColor());
        g2d.drawRect((int) (p.getX() - lugWidth / 2d), (int) (p.getY() - lugHeight / 2d), lugWidth,
            lugHeight);
      } else {
        g2d.setColor(TERMINAL_COLOR);
        g2d.fillRect((int) (p.getX() - lugWidth / 2d), (int) (p.getY() - lugHeight / 2d), lugWidth,
            lugHeight);
        g2d.setColor(TERMINAL_COLOR.darker());
        g2d.drawRect((int) (p.getX() - lugWidth / 2d), (int) (p.getY() - lugHeight / 2d), lugWidth,
            lugHeight);
      }
    }

    if (getShowMarkers()) {
      String[] markers =
          SwitchUtils.getSwitchingMarkers(this, getControlPointCount(), false);

      g2d.setColor(MARKER_COLOR);

      double offset = MARKER_OFFSET.convertToPixels();
      for (int i = 0; i < getControlPointCount(); i++) {
        if (markers[i] == null)
          continue;

        Point2D p = getControlPoint(i);
        int direction = switchType == SlideSwitchType.SPDT || i < getControlPointCount() / 2 ? 1 : -1;
        Point2D labelPoint = new Point2D.Double(direction * offset, 0);

        AffineTransform tx = null;
        if (getOrientation() == OrientationHV.HORIZONTAL) {
          tx = AffineTransform.getRotateInstance(-Math.PI / 2, 0, 0);
        }
        if (tx != null) {
          tx.transform(labelPoint, labelPoint);
        }
        StringUtils.drawCenteredText(g2d, markers[i], p.getX() + labelPoint.getX(), p.getY() + labelPoint.getY(),
            HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }
    }
  }

  public Shape[] getBody() {
    if (body == null) {
      Point2D firstPoint = controlPoints[0];
      int margin = (int) MARGIN.convertToPixels();
      int rowSpacing = (int) getRowSpacing().convertToPixels();
      int columnSpacing = (int) getColumnSpacing().convertToPixels();
      int holeDiameter = (int) HOLE_DIAMETER.convertToPixels();
      double width = 0;
      double height = 0;
      switch (switchType) {
        case SPDT:
          width = 2 * margin;
          height = 2 * margin + 2 * rowSpacing;
          break;
        case DPDT:
          width = 2 * margin + columnSpacing;
          height = 2 * margin + 2 * rowSpacing;
          break;
        case DP3T:
          width = 2 * margin + columnSpacing;
          height = 2 * margin + 3 * rowSpacing;
          break;
      }

      Area mainBody = new Area(new Rectangle2D.Double(firstPoint.getX() - margin,
          firstPoint.getY() - margin, width, height));
      Area bracket = new Area(new RoundRectangle2D.Double(firstPoint.getX() - margin,
          firstPoint.getY() - margin - holeDiameter * 2, width, height + holeDiameter * 4,
          holeDiameter / 2, holeDiameter / 2));
      bracket.subtract(
          new Area(new Ellipse2D.Double(firstPoint.getX() - margin + width / 2 - holeDiameter / 2,
              firstPoint.getY() - margin - holeDiameter * 3 / 2, holeDiameter, holeDiameter)));
      bracket.subtract(
          new Area(new Ellipse2D.Double(firstPoint.getX() - margin + width / 2 - holeDiameter / 2,
              firstPoint.getY() - margin + height + holeDiameter / 2, holeDiameter, holeDiameter)));
      bracket.subtract(mainBody);

      if (getOrientation() == OrientationHV.HORIZONTAL) {
        AffineTransform xform =
            AffineTransform.getRotateInstance(-Math.PI / 2, firstPoint.getX(), firstPoint.getY());
        mainBody.transform(xform);
        bracket.transform(xform);
      }

      body = new Shape[] {mainBody, bracket};
    }
    return body;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int circleSize = (int) (5f * width / 32);
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(width / 8, 1, width * 6 / 8, height - 2);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(width / 8, 1, width * 6 / 8, height - 2);
    g2d.setColor(TERMINAL_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f * width / 32));
    for (int i = 1; i <= 3; i++) {
      g2d.drawLine(width / 4, i * height / 4 - circleSize / 2 - 1, width / 4,
          i * height / 4 + circleSize / 2 - 1);
      g2d.drawLine(width * 3 / 4, i * height / 4 - circleSize / 2 - 1, width * 3 / 4,
          i * height / 4 + circleSize / 2 - 1);
    }
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    if (bodyColor == null)
      bodyColor = BODY_COLOR;
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    if (borderColor == null)
      borderColor = BORDER_COLOR;
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  // switch stuff
  //
  // @Override
  // public String getControlPointNodeName(int index) {
  // // we don't want the switch to produce any nodes, it just makes connections
  // return null;
  // }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public int getPositionCount() {
    return switchType.getPositionCount();
  }

  @Override
  public String getPositionName(int position) {
    return "ON" + Integer.toString(position + 1);
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    // Method assumes index1 < index2
    switch (switchType) {
      case SPDT:
        // For SPDT, index 1 is the common (center) terminal
        // Position 1: connects terminals 0-1
        // Position 2: connects terminals 1-2
        if (position == 0) {
          return index1 == 0 && index2 == 1;
        } else if (position == 1) {
          return index1 == 1 && index2 == 2;
        }
        return false;
      case DPDT:
        // For DPDT, indices 1 and 4 are the common (center) terminals
        // Position 1: connects terminals 0-1 and 3-4
        // Position 2: connects terminals 1-2 and 4-5
        if (position == 0) {
          return (index1 == 0 && index2 == 1) || (index1 == 3 && index2 == 4);
        } else if (position == 1) {
          return (index1 == 1 && index2 == 2) || (index1 == 4 && index2 == 5);
        }
        return false;
      case DP3T:
        // For DP3T, following the schematic:
        // Position 1: A1-A2 and B1-B2 (0-1 and 4-5)
        // Position 2: A2-A3 and B2-B3 (1-2 and 5-6)
        // Position 3: A3-A4 and B3-B4 (2-3 and 6-7)
        if (position == 0) { // Position 1
          return (index1 == 0 && index2 == 1) || // A1-A2
                 (index1 == 4 && index2 == 5);   // B1-B2
        } else if (position == 1) { // Position 2
          return (index1 == 1 && index2 == 2) || // A2-A3
                 (index1 == 5 && index2 == 6);   // B2-B3
        } else if (position == 2) { // Position 3
          return (index1 == 2 && index2 == 3) || // A3-A4
                 (index1 == 6 && index2 == 7);   // B3-B4
        }
    }
    return false;
  }

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty(name = "Show Bracket")
  public Boolean getShowBracket() {
    return showBracket;
  }

  public void setShowBracket(Boolean showBracket) {
    this.showBracket = showBracket;
  }

  @EditableProperty(name = "Bracket")
  public Color getBracketColor() {
    return bracketColor;
  }

  public void setBracketColor(Color bracketColor) {
    this.bracketColor = bracketColor;
  }

  @DynamicEditableProperty(source = SlideSwitchPositionPropertyValueSource.class)
  @EditableProperty(name = "Selected Position")
  @Override
  public Integer getSelectedPosition() {
    return selectedPosition;
  }

  public void setSelectedPosition(Integer selectedPosition) {
    this.selectedPosition = selectedPosition;
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

  @Override
  public Rectangle2D getCachingBounds() {
    int margin = 20;
    Rectangle2D bounds = getBody()[1].getBounds2D();
    return new Rectangle2D.Double(bounds.getX() - margin, bounds.getY() - margin,
        bounds.getWidth() + 2 * margin, bounds.getHeight() + 2 * margin);
  }
}
