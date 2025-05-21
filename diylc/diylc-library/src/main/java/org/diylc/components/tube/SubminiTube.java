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
package org.diylc.components.tube;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.common.Display;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractLabeledComponent;
import org.diylc.components.transform.TO220Transformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Sub-Mini Tube", author = "Branislav Stojkovic", category = "Tubes",
    instanceNamePrefix = "V", description = "Sub-miniature (pencil) vacuum tube",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE,
    transformer = TO220Transformer.class)
public class SubminiTube extends AbstractLabeledComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.decode("#DDDDDD");
  public static Color BORDER_COLOR = Color.gray;
  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color LABEL_COLOR = Color.black;
  public static Size LEAD_SPACING = new Size(0.1d, SizeUnit.in);
  public static Size BODY_WIDTH = new Size(0.4d, SizeUnit.in);
  public static Size BODY_THICKNESS = new Size(4.5d, SizeUnit.mm);
  public static Size BODY_HEIGHT = new Size(9d, SizeUnit.mm);
  public static Size DIAMETER = new Size(0.4d, SizeUnit.in);
  public static Size LENGTH = new Size(1.375d, SizeUnit.in);
  public static Size LEAD_LENGTH = new Size(0.4d, SizeUnit.in);
  public static Size LEAD_THICKNESS = new Size(0.4d, SizeUnit.mm);
  public static Size EDGE_RADIUS = new Size(2d, SizeUnit.mm);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
  transient private Area[] body;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Display display = Display.NAME;
  private boolean folded = false;
  private Size leadLength = LEAD_LENGTH;
  private PinArrangement leadArrangement = PinArrangement.Circular;
  private boolean topLead = false;
  private Size diameter = DIAMETER;
  private Size length = LENGTH;
  private PinCount pinCount = PinCount._8;
  private Size leadSpacing = LEAD_SPACING;
  private Color labelColor = LABEL_COLOR;

  public SubminiTube() {
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
    return getFolded() ? VisibilityPolicy.ALWAYS : VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  private void updateControlPoints() {
    int leadSpacing = (int) getLeadSpacing().convertToPixels();
    // Update control points.
    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();
    int newPointCount = getPinCount().getValue()
        + ((getPinArrangement() == PinArrangement.Circular && !folded) || getTopLead() ? 1 : 0);
    // Need a new array
    if (newPointCount != controlPoints.length) {
      controlPoints = new Point2D[newPointCount];
      for (int i = 0; i < controlPoints.length; i++) {
        controlPoints[i] = new Point2D.Double(x, y);
      }
    }
    double length = getLength().convertToPixels(); 
    double leadLength = getLeadLength().convertToPixels();
    double diameter = getDiameter().convertToPixels();
    int dx;
    int dy;
    if (folded || getPinArrangement() == PinArrangement.Inline) {
      switch (orientation) {
        case DEFAULT:
          dx = 0;
          dy = -leadSpacing;
          break;
        case _90:
          dx = leadSpacing;
          dy = 0;
          break;
        case _180:
          dx = 0;
          dy = leadSpacing;
          break;
        case _270:
          dx = -leadSpacing;
          dy = 0;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      for (int i = 1; i < controlPoints.length; i++) {
        controlPoints[i].setLocation(controlPoints[0].getX() + i * dx, controlPoints[0].getY() + i * dy);
      }
      if (getTopLead()) {
        double centerX = (controlPoints[controlPoints.length - 2].getX() + x) / 2;
        double centerY = (controlPoints[controlPoints.length - 2].getY() + y) / 2;
        switch (orientation) {
          case DEFAULT:
            if (folded)
              dx -= length + leadLength + diameter / 2;
            else
              dx = leadSpacing;
            dy = 0;
            break;
          case _90:
            dx = 0;
            if (folded)
              dy -= length + leadLength + diameter / 2;
            else
              dy = leadSpacing;
            break;
          case _180:
            if (folded)
              dx += length + leadLength + diameter / 2;
            else
              dx = -leadSpacing;
            dy = 0;           
            break;
          case _270:
            dx = 0;
            if (folded)
              dy += length + leadLength + diameter / 2;
            else
              dy = -leadSpacing;
            break;
        }
        controlPoints[controlPoints.length - 1].setLocation(centerX + dx, centerY + dy);
      }
    } else {
      int pinCount = getPinCount().getValue();
      Point2D firstPoint = controlPoints[0];

      double angleIncrement = Math.PI * 2 / (pinCount + 1);
      double initialAngleOffset = angleIncrement;

      double r = (pinCount + 1) * leadSpacing / (2 * Math.PI);

      controlPoints = new Point2D[pinCount + 1];
      double theta = initialAngleOffset + getOrientation().toRadians();
      controlPoints[0] = firstPoint;
      for (int i = 0; i < pinCount; i++) {
        controlPoints[i + 1] = new Point2D.Double((int) (firstPoint.getX() + Math.cos(theta) * r),
            (int) (firstPoint.getY() + Math.sin(theta) * r));
        theta += angleIncrement;
      }
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      double x = controlPoints[0].getX();
      double y = controlPoints[0].getY();
      double length = getLength().convertToPixels();      
      double leadLength = getLeadLength().convertToPixels();
      double diameter = getDiameter().convertToPixels();
      double edgeRadius = EDGE_RADIUS.convertToPixels();
      
      double centerX;
      double centerY;
      if (getPinArrangement() == PinArrangement.Circular && !folded) {
        centerX = x;
        centerY = y;
      } else {
        centerX = (controlPoints[controlPoints.length - (getTopLead() ? 2 : 1)].getX() + x) / 2;
        centerY = (controlPoints[controlPoints.length - (getTopLead() ? 2 : 1)].getY() + y) / 2;
      }

      if (folded) {        
        switch (orientation) {
          case DEFAULT:  
            body[0] = new Area(new RoundRectangle2D.Double(centerX - leadLength - length, centerY - diameter / 2,
                length, diameter, edgeRadius, edgeRadius));
            body[0].add(new Area(new RoundRectangle2D.Double(centerX - leadLength - length - diameter / 2, centerY - diameter / 8, 
                diameter, diameter / 4, diameter / 4, diameter / 4)));
            break;
          case _90:
            body[0] = new Area(new RoundRectangle2D.Double(centerX - diameter / 2, centerY - leadLength - length,
                diameter, length, edgeRadius, edgeRadius));
            body[0].add(new Area(new RoundRectangle2D.Double(centerX - diameter / 8, centerY - leadLength - length - diameter / 2, 
                diameter / 4, diameter, diameter / 4, diameter / 4)));
            break;
          case _180:
            body[0] = new Area(new RoundRectangle2D.Double(centerX + leadLength, centerY - diameter / 2,
                length, diameter, edgeRadius, edgeRadius));
            body[0].add(new Area(new RoundRectangle2D.Double(centerX + leadLength + length - diameter / 2, centerY - diameter / 8, 
                diameter, diameter / 4, diameter / 4, diameter / 4)));
            break;
          case _270:
            body[0] = new Area(new RoundRectangle2D.Double(centerX - diameter / 2, centerY + leadLength,
                diameter, length, edgeRadius, edgeRadius));
            body[0].add(new Area(new RoundRectangle2D.Double(centerX - diameter / 8, centerY + leadLength + length - diameter / 2, 
                diameter / 4, diameter, diameter / 4, diameter / 4)));
            break;
          default:
            throw new RuntimeException("Unexpected orientation: " + orientation);
        }
      } else {
        body[0] = new Area(new Ellipse2D.Double(centerX - diameter / 2, centerY - diameter / 2, diameter,
            diameter));
      }
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    int pinSize = (int) LEAD_THICKNESS.convertToPixels() / 2 * 2;
    Shape mainArea = getBody()[0];
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(mainArea);

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
              : getBorderColor();
    }
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(mainArea);

    g2d.setComposite(oldComposite);

    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
        Constants.DEFAULT_THEME);

    // Draw pins.

    if (folded) {
      int leadThickness = getClosestOdd(LEAD_THICKNESS.convertToPixels());
      int leadLength = (int) getLeadLength().convertToPixels();
      Color finalPinColor;
      Color finalPinBorderColor;
      if (outlineMode) {
        finalPinColor = new Color(0, 0, 0, 0);
        finalPinBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
                ? SELECTION_COLOR
                : theme.getOutlineColor();
      } else {
        finalPinColor = METAL_COLOR;
        finalPinBorderColor = METAL_COLOR.darker();
      }
      
      double centerX = (controlPoints[controlPoints.length - (getTopLead() ? 2 : 1)].getX() + controlPoints[0].getX()) / 2;
      double centerY = (controlPoints[controlPoints.length - (getTopLead() ? 2 : 1)].getY() + controlPoints[0].getY()) / 2;      
      double increment = getDiameter().convertToPixels() / getPinCount().getValue();
      if (orientation == Orientation.DEFAULT || orientation == Orientation._270)
        increment = -increment;
      double startX = centerX - increment * (getPinCount().getValue() - 1) / 2;
      double startY = centerY - increment * (getPinCount().getValue() - 1) / 2;
      
      for (int i = 0; i < controlPoints.length - (getTopLead() ? 1 : 0); i++) {
        Point2D point = controlPoints[i];
        switch (orientation) {
          case DEFAULT:
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
            g2d.setColor(finalPinBorderColor);
            g2d.drawLine((int)point.getX(), (int)point.getY(), (int)(point.getX() - leadLength - leadThickness / 2), (int)(startY + increment * i));
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
            g2d.setColor(finalPinColor);
            g2d.drawLine((int)point.getX(), (int)point.getY(), (int)(point.getX() - leadLength - leadThickness / 2), (int)(startY + increment * i));
            break;
          case _90:
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
            g2d.setColor(finalPinBorderColor);
            g2d.drawLine((int)point.getX(), (int)point.getY(), (int)(startX + increment * i), (int)(point.getY() - leadLength));
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
            g2d.setColor(finalPinColor);
            g2d.drawLine((int)point.getX(), (int)point.getY(), (int)(startX + increment * i), (int)(point.getY() - leadLength));
            break;
          case _180:
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
            g2d.setColor(finalPinBorderColor);
            g2d.drawLine((int)point.getX(), (int)point.getY(), (int)(point.getX() + leadLength - leadThickness / 2), (int)(startY + increment * i));
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
            g2d.setColor(finalPinColor);
            g2d.drawLine((int)point.getX(), (int)point.getY(), (int)(point.getX() + leadLength - leadThickness / 2), (int)(startY + increment * i));
            break;
          case _270:
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
            g2d.setColor(finalPinBorderColor);
            g2d.drawLine((int)point.getX(), (int)point.getY(), (int)(startX + increment * i), (int)(point.getY() + leadLength - leadThickness / 2));
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
            g2d.setColor(finalPinColor);
            g2d.drawLine((int)point.getX(), (int)point.getY(), (int)(startX + increment * i), (int)(point.getY() + leadLength - leadThickness / 2));
            break;
        }
      }
      if (getTopLead()) {
        Point2D point = controlPoints[controlPoints.length - 1];
        g2d.setColor(PIN_COLOR);
        g2d.fillOval((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
        g2d.setColor(outlineMode ? theme.getOutlineColor() : PIN_BORDER_COLOR);
        g2d.drawOval((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
      }
    } else {
      if (!outlineMode) {
        for (int i = getPinArrangement() == PinArrangement.Circular && !topLead ? 1
            : 0; i < controlPoints.length; i++) {
          Point2D point = controlPoints[i];
          g2d.setColor(PIN_COLOR);
          g2d.fillOval((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
          g2d.setColor(outlineMode ? theme.getOutlineColor() : PIN_BORDER_COLOR);
          g2d.drawOval((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
        }
      }
    }

    // Draw label.
    g2d.setFont(project.getFont());
    
    // Override font size
    if (getFontSizeOverride() != null)
      g2d.setFont(g2d.getFont().deriveFont(1f * getFontSizeOverride()));
    
    Color finalLabelColor;
    if (outlineMode) {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? LABEL_COLOR_SELECTED
              : getLabelColor();
    }
    g2d.setColor(finalLabelColor);
    String label = "";
    label = (getDisplay() == Display.NAME) ? getName() : getValue();
    if (getDisplay() == Display.NONE) {
      label = "";
    }
    if (getDisplay() == Display.BOTH) {
      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
    }
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
    Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);
    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());
    // Center text horizontally and vertically
    Rectangle bounds = mainArea.getBounds();
    double x = bounds.getX() + (bounds.width - textWidth) / 2;
    double y = bounds.getY() + (bounds.height - textHeight) / 2 + fontMetrics.getAscent();

    int dx = 0;
    int dy = 0;
    if (getPinArrangement() == PinArrangement.Inline || getTopLead()) {
      switch (getOrientation()) {
        case DEFAULT:
          dx = (int) LEAD_THICKNESS.convertToPixels() * 4;
          break;
        case _90:
          dy = (int) LEAD_THICKNESS.convertToPixels() * 4;
          break;
        case _180:
          dx = -(int) LEAD_THICKNESS.convertToPixels() * 4;
          break;
        case _270:
          dy = -(int) LEAD_THICKNESS.convertToPixels() * 4;
          break;
      }
    }

    g2d.drawString(label, (int)(x + dx), (int)(y + dy));
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    Area area = new Area(new Ellipse2D.Double(2, 2, width - 4, width - 4));
    int center = width / 2;
    g2d.setColor(BODY_COLOR);
    g2d.fill(area);
    g2d.setColor(BODY_COLOR.darker());
    g2d.draw(area);

    int radius = width / 2 - 7;
    for (int i = 1; i < 8; i++) {
      int x = (int) (center + Math.cos(i * Math.PI / 4) * radius);
      int y = (int) (center + Math.sin(i * Math.PI / 4) * radius);
      g2d.setColor(PIN_COLOR);
      g2d.fillOval(x - 1, y - 1, 2, 2);
      g2d.setColor(PIN_BORDER_COLOR);
      g2d.drawOval(x - 1, y - 1, 2, 2);
    }
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty
  public boolean getFolded() {
    return folded;
  }

  public void setFolded(boolean folded) {
    this.folded = folded;
    updateControlPoints();
    // Invalidate the body
    this.body = null;
  }

  @EditableProperty(name = "Lead Length")
  public Size getLeadLength() {
    if (leadLength == null) {
      leadLength = LEAD_LENGTH;
    }
    return leadLength;
  }

  public void setLeadLength(Size leadLength) {
    this.leadLength = leadLength;
    // Invalidate the body
    this.body = null;
  }

  @EditableProperty
  public Display getDisplay() {
    if (display == null) {
      display = Display.NAME;
    }
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  @EditableProperty(name = "Pin Arrangement")
  public PinArrangement getPinArrangement() {
    return leadArrangement;
  }

  public void setPinArrangement(PinArrangement pinArrangement) {
    this.leadArrangement = pinArrangement;
    updateControlPoints();
    // invalidate the body
    this.body = null;
  }

  @EditableProperty(name = "Top Lead")
  public boolean getTopLead() {
    return topLead;
  }

  public void setTopLead(boolean topLead) {
    this.topLead = topLead;
    updateControlPoints();
  }

  @EditableProperty
  public Size getDiameter() {
    return diameter;
  }

  public void setDiameter(Size diameter) {
    this.diameter = diameter;
    updateControlPoints();
  }

  @EditableProperty
  public Size getLength() {
    return length;
  }

  public void setLength(Size length) {
    this.length = length;
  }

  @EditableProperty(name = "Lead Count")
  public PinCount getPinCount() {
    return pinCount;
  }

  public void setPinCount(PinCount pinCount) {
    this.pinCount = pinCount;
    updateControlPoints();
    // invalidate the body
    this.body = null;
  }

  @EditableProperty(name = "Lead Spacing")
  public Size getLeadSpacing() {
    return leadSpacing;
  }

  public void setLeadSpacing(Size leadSpacing) {
    this.leadSpacing = leadSpacing;
    updateControlPoints();
    // invalidate the body
    this.body = null;
  }

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {    
    return false;
  }

  public static enum PinArrangement {
    Inline("In-line"), Circular("Circular");

    private String label;

    private PinArrangement(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  public static enum PinCount {
    _3, _4, _5, _6, _7, _8, _9, _10;

    @Override
    public String toString() {
      return name().replace("_", "");
    }

    public int getValue() {
      return Integer.parseInt(toString());
    }
  }
}
