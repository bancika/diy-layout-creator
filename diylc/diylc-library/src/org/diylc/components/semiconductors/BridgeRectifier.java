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
package org.diylc.components.semiconductors;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractLabeledComponent;
import org.diylc.components.AbstractLeadedComponent.LabelOriantation;
import org.diylc.components.RoundedPolygon;
import org.diylc.components.transform.DIL_ICTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Current;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.Voltage;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Bridge Rectifier", author = "Branislav Stojkovic", category = "Semiconductors",
    instanceNamePrefix = "BR", description = "Few variations of bridge rectifier chips",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE, transformer = DIL_ICTransformer.class,
    enableCache = true)
public class BridgeRectifier extends AbstractLabeledComponent<String> {

  private static final long serialVersionUID = 1L;
  
  private static Size MINI_LENGTH = new Size(6.7d, SizeUnit.mm);
  private static Size MINI_WIDTH = new Size(8.3d, SizeUnit.mm);
  private static Size MINI_HORIZONTAL_SPACING = new Size(0.3d, SizeUnit.in);
  private static Size MINI_VERTICAL_SPACING = new Size(0.2d, SizeUnit.in);
  
  private static int[] MINI1_LABEL_SPACING_X = new int[] { 1, -1, -1, 1 };  
  private static int[] MINI2_LABEL_SPACING_X = new int[] { 1, -1, 1, -1 };
  
  private static Size MINI_ROUND_DIAMETER = new Size(9.1d, SizeUnit.mm);
  private static Size MINI_ROUND_SPACING = new Size(0.2d, SizeUnit.in);  
  private static int[] MINI_ROUND_LABEL_SPACING_Y = new int[] { 1, 1, -1, -1 };
  
  private static Size INLINE_LENGTH = new Size(23.2d, SizeUnit.mm);
  private static Size INLINE_WIDTH = new Size(2.7d, SizeUnit.mm);
  private static Size INLINE_SPACING = new Size(0.2d, SizeUnit.in);
  private static int[] INLINE_LABEL_SPACING_Y = new int[] { -1, -1, 1, 1 };
  
  private static Size BR3_LENGTH = new Size(0.6d, SizeUnit.in);
  private static Size BR3_SPACING = new Size(0.425d, SizeUnit.in);
  private static Size BR3_HOLE_SIZE = new Size(3d, SizeUnit.mm);
  private static int[] BR3_LABEL_SPACING_Y = new int[] { 1, 1, -1, -1 };

  public static Color BODY_COLOR = Color.gray;
  public static Color BORDER_COLOR = Color.gray.darker();
  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color LABEL_COLOR = Color.white;
  public static int EDGE_RADIUS = 6;
  public static Size SQUARE_PIN_SIZE = new Size(0.04d, SizeUnit.in);
  public static Size ROUND_PIN_SIZE = new Size(0.032d, SizeUnit.in);
  public static Size INDENT_SIZE = new Size(0.07d, SizeUnit.in);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  private Point2D[] controlPoints = new Point2D[] { new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0) };
  private String[] pointLabels = new String[] { "+", "~", "~", "-" };
  protected Display display = Display.BOTH;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  private RectifierType rectifierType = RectifierType.MiniDIP1;
  private Voltage voltage;
  private Current current;
  transient private Area[] body;
  private LabelOriantation labelOriantation = LabelOriantation.Directional;

  public BridgeRectifier() {
    super();
    updateControlPoints();
    alpha = 100;
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

  @EditableProperty
  public Display getDisplay() {
    if (display == null) {
      display = Display.VALUE;
    }
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
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

  @SuppressWarnings("incomplete-switch")
  private void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    
    int hSpacing;
    int vSpacing;
    
    switch (rectifierType) {
      case MiniDIP1:
        hSpacing = (int) MINI_HORIZONTAL_SPACING.convertToPixels();
        vSpacing = (int) MINI_VERTICAL_SPACING.convertToPixels();
        controlPoints[1].setLocation(firstPoint.getX() + hSpacing, firstPoint.getY());
        controlPoints[2].setLocation(firstPoint.getX() + hSpacing, firstPoint.getY() + vSpacing);        
        controlPoints[3].setLocation(firstPoint.getX(), firstPoint.getY() + vSpacing);
        break;
      case MiniDIP2:
        hSpacing = (int) MINI_HORIZONTAL_SPACING.convertToPixels();
        vSpacing = (int) MINI_VERTICAL_SPACING.convertToPixels();
        controlPoints[1].setLocation(firstPoint.getX() + hSpacing, firstPoint.getY());
        controlPoints[2].setLocation(firstPoint.getX(), firstPoint.getY() + vSpacing);
        controlPoints[3].setLocation(firstPoint.getX() + hSpacing, firstPoint.getY() + vSpacing);                
        break;
      case MiniRound1:
        hSpacing = vSpacing = (int) MINI_ROUND_SPACING.convertToPixels();
        controlPoints[1].setLocation(firstPoint.getX() + hSpacing, firstPoint.getY());
        controlPoints[2].setLocation(firstPoint.getX() + hSpacing, firstPoint.getY() + vSpacing);        
        controlPoints[3].setLocation(firstPoint.getX(), firstPoint.getY() + vSpacing);
        break;
      case MiniRound2:
        hSpacing = vSpacing = (int) MINI_ROUND_SPACING.convertToPixels();
        controlPoints[1].setLocation(firstPoint.getX() + hSpacing, firstPoint.getY());
        controlPoints[2].setLocation(firstPoint.getX(), firstPoint.getY() + vSpacing);
        controlPoints[3].setLocation(firstPoint.getX() + hSpacing, firstPoint.getY() + vSpacing); 
        break;
      case InLine:
        vSpacing = (int) INLINE_SPACING.convertToPixels();
        controlPoints[1].setLocation(firstPoint.getX(), firstPoint.getY() + vSpacing);
        controlPoints[2].setLocation(firstPoint.getX(), firstPoint.getY() + 2 * vSpacing);
        controlPoints[3].setLocation(firstPoint.getX(), firstPoint.getY() + 3 * vSpacing);
        break;
      case SquareBR3:
        hSpacing = vSpacing = (int) BR3_SPACING.convertToPixels();        
        controlPoints[1].setLocation(firstPoint.getX() + hSpacing, firstPoint.getY());
        controlPoints[2].setLocation(firstPoint.getX(), firstPoint.getY() + vSpacing);
        controlPoints[3].setLocation(firstPoint.getX() + hSpacing, firstPoint.getY() + vSpacing);
        break;
    }

    if (orientation != Orientation.DEFAULT) {
      double theta = 0;
      switch (orientation) {
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
      AffineTransform rotation = AffineTransform.getRotateInstance(theta, firstPoint.getX(), firstPoint.getY());

      for (int i = 1; i < controlPoints.length; i++) {
        rotation.transform(controlPoints[i], controlPoints[i]);
      }      
    }
  }

  @SuppressWarnings("incomplete-switch")
  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      
      double centerX = (controlPoints[0].getX() + controlPoints[1].getX() + controlPoints[2].getX() + controlPoints[3].getX()) / 4;
      double centerY = (controlPoints[0].getY() + controlPoints[1].getY() + controlPoints[2].getY() + controlPoints[3].getY()) / 4;
      
      int width = 0;
      int length = 0;
      
      switch (rectifierType) {
        case MiniDIP1:
        case MiniDIP2:
          length = (int) MINI_LENGTH.convertToPixels();
          width = (int) MINI_WIDTH.convertToPixels();
          break;
        case MiniRound1:
        case MiniRound2:
          length = width = (int) MINI_ROUND_DIAMETER.convertToPixels();
          break;
        case InLine:
          length = (int) INLINE_WIDTH.convertToPixels();
          width = (int) INLINE_LENGTH.convertToPixels();
          break;
        case SquareBR3:
          length = width = (int) BR3_LENGTH.convertToPixels();
          break;
      }
      
      if (orientation == Orientation._90 || orientation == Orientation._270) {
        int p = length;
        length = width;
        width = p;
      }
      
      switch (rectifierType) {
        case MiniDIP1:
        case MiniDIP2:
        case InLine:
          body[0] = new Area(new RoundRectangle2D.Double(centerX - length / 2, centerY - width / 2, length, width, EDGE_RADIUS, EDGE_RADIUS));
          break;
        case MiniRound1:
        case MiniRound2:
          body[0] = new Area(new Ellipse2D.Double(centerX - length / 2, centerY - width / 2, length, width));
          break;
        case SquareBR3:
          double margin = (BR3_LENGTH.convertToPixels() - BR3_SPACING.convertToPixels()) / 2;
          double holeSize = BR3_HOLE_SIZE.convertToPixels();
//          
//          Path2D path = new Path2D.Double();
//          path.moveTo(centerX - width / 2 + margin, centerY - width / 2);
//          path.lineTo(centerX + width / 2, centerY - width / 2);
//          path.lineTo(centerX + width / 2, centerY + width / 2);
//          path.lineTo(centerX - width / 2, centerY + width / 2);
//          path.lineTo(centerX - width / 2, centerY - width / 2 + margin);
//          path.closePath();
          
          RoundedPolygon poly = new RoundedPolygon(new Point2D[] {
              new Point2D.Double(centerX, centerY - width / 2),
              new Point2D.Double(centerX + width / 2, centerY - width / 2),
              new Point2D.Double(centerX + width / 2, centerY + width / 2),
              new Point2D.Double(centerX - width / 2, centerY + width / 2),
              new Point2D.Double(centerX - width / 2, (int) (centerY - width / 2 + margin)),
              new Point2D.Double((int) (centerX - width / 2 + margin), centerY - width / 2),
              
          }, new double[] { EDGE_RADIUS, EDGE_RADIUS, EDGE_RADIUS, EDGE_RADIUS / 2 , EDGE_RADIUS / 2});
          
          body[0] = new Area(poly);
          body[0].subtract(new Area(new Ellipse2D.Double(centerX - holeSize / 2, centerY - holeSize / 2, holeSize, holeSize)));
          
          if (orientation != Orientation.DEFAULT) {
            double theta = 0;
            switch (orientation) {
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
            AffineTransform rotation = AffineTransform.getRotateInstance(theta, centerX, centerY);

            body[0].transform(rotation);
          }
          
          break;
      }
    }
    return body;
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Area mainArea = getBody()[0];
    
    PinShape pinShape = rectifierType.getPinShape();
    int pinSize = (int) (pinShape == PinShape.Round ? ROUND_PIN_SIZE.convertToPixels() : SQUARE_PIN_SIZE.convertToPixels()) / 2 * 2;
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    if (!outlineMode) {      
      for (Point2D point : controlPoints) {
        g2d.setColor(PIN_COLOR);
        Shape shape;
        if (pinShape == PinShape.Round)
          shape = new Ellipse2D.Double(point.getX() - pinSize / 2, point.getY() - pinSize / 2, pinSize, pinSize);
        else
          shape = new Rectangle2D.Double(point.getX() - pinSize / 2, point.getY() - pinSize / 2, pinSize, pinSize);
        g2d.fill(shape);
        g2d.setColor(PIN_BORDER_COLOR);
        g2d.draw(shape);
      }
    }
    
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
    g2d.fill(mainArea);
    g2d.setComposite(oldComposite);

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
              : getBorderColor();
    }
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (outlineMode) {
      Area area = new Area(mainArea);
      area.subtract(getBody()[1]);
      g2d.draw(area);
    } else {
      g2d.draw(mainArea);
//      if (getBody()[1] != null) {
//        g2d.setColor(getIndentColor());
//        g2d.fill(getBody()[1]);
//      }
    }

    drawingObserver.stopTracking();

    // Draw label.
    g2d.setFont(project.getFont());
    
    // Override font size
    if (getFontSizeOverride() != null)
      g2d.setFont(g2d.getFont().deriveFont(1f * getFontSizeOverride()));
    
    Color finalLabelColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : getLabelColor();
    }
    g2d.setColor(finalLabelColor);
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
    String[] label = null;

    if (getDisplay() == Display.NAME) {
      label = new String[] {getName()};
    } else if (getDisplay() == Display.VALUE) {
      label = new String[] {getValue().toString()};
    } else if (getDisplay() == Display.BOTH) {
      String value = getValue().toString();
      label = value.isEmpty() ? new String[] {getName()} : new String[] {getName(), value};
    }

    if (label != null) {
      for (int i = 0; i < label.length; i++) {
        String l = label[i];
        Rectangle2D rect = fontMetrics.getStringBounds(l, g2d);
        int textHeight = (int) (rect.getHeight());
        int textWidth = (int) (rect.getWidth());
        // Center text horizontally and vertically
        Rectangle bounds = mainArea.getBounds();
        double x = bounds.getX() + (bounds.width - textWidth) / 2;
        double y = bounds.getY() + (bounds.height - textHeight) / 2 + fontMetrics.getAscent();

        AffineTransform oldTransform = g2d.getTransform();

        if (getLabelOriantation() == LabelOriantation.Directional && (getOrientation() == Orientation.DEFAULT || getOrientation() == Orientation._180)) {
          double centerX = bounds.getX() + bounds.width / 2;
          double centerY = bounds.getY() + bounds.height / 2;
          g2d.rotate(-Math.PI / 2, centerX, centerY);
        }

        if (label.length == 2) {
          if (i == 0)
            g2d.translate(0, -textHeight / 2);
          else if (i == 1)
            g2d.translate(0, textHeight / 2);
        }
        
        if (rectifierType == RectifierType.SquareBR3)
          y -= BR3_HOLE_SIZE.convertToPixels();

        g2d.drawString(l, (int)x, (int)y);

        g2d.setTransform(oldTransform);
      }
    }

    // draw pin numbers    
    g2d.setFont(project.getFont().deriveFont((float) (project.getFont().getSize2D() * 0.8)));
    g2d.setColor(finalLabelColor);
    for (int i = 0; i <controlPoints.length; i++) {
      Point2D point = controlPoints[i];            
      
      int dx = 0;
      int dy = 0;
      
      // determine label offset
      switch (rectifierType) {
        case MiniDIP1:
          dx = pinSize * MINI1_LABEL_SPACING_X[i];
          break;
        case MiniDIP2:
          dx = pinSize * MINI2_LABEL_SPACING_X[i];
          break;  
        case MiniRound1:
        case MiniRound2:
          dy = pinSize * MINI_ROUND_LABEL_SPACING_Y[i];
          break;
        case InLine:
          dy = pinSize * INLINE_LABEL_SPACING_Y[i];
          break;
        case SquareBR3:
          dy = pinSize * BR3_LABEL_SPACING_Y[i];
          break;
      }
      
      dx = (int) (1.5 * dx);
      dy = (int) (1.5 * dy);
      
      // now apply the correct rotation
      switch (rectifierType) {
        case MiniDIP1:         
        case MiniDIP2:
          switch (orientation) {
            case _90:        
              int p1 = dx;
              dx = dy;
              dy = p1;
              break;
            case _180:
              dx = - dx;
              dy = - dy;
              break;
            case _270:
              int p2 = dx;
              dx = -dy;
              dy = -p2;
              break;
          }
          break;  
        case MiniRound1:
        case MiniRound2:
        case SquareBR3:
          switch (orientation) {
            case _90:        
              int p1 = dx;
              dx = -dy;
              dy = -p1;
              break;
            case _180:
              dx = - dx;
              dy = - dy;
              break;
            case _270:
              int p2 = dx;
              dx = dy;
              dy = p2;
              break;
          }
          break;
        case InLine:
          switch (orientation) {
            case _90:        
              int p1 = dx;
              dx = -dy;
              dy = -p1;
              break;
            case _180:
              dx = -dx;
              dy = -dy;
              break;
            case _270:
              int p2 = dx;
              dx = dy;
              dy = p2;
              break;
          }
          break;
      }         
      
      StringUtils.drawCenteredText(g2d, pointLabels[i], point.getX() + dx, point.getY() + dy, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = (int) (2f * width / 32);
    Area area = new Area(new Ellipse2D.Double(margin / 2, margin, width - 2 * margin, width - 2 * margin));
    g2d.setColor(BODY_COLOR);
    g2d.fill(area);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(area);
    g2d.setColor(PIN_COLOR);
    int pinSize = 2 * width / 32;    
    g2d.fillOval(width * 2 / 8, height * 2 / 8, pinSize, pinSize);
    g2d.fillOval(width * 6 / 8 - pinSize, height * 2 / 8, pinSize, pinSize);
    g2d.fillOval(width * 6 / 8 - pinSize, height * 6 / 8 - pinSize, pinSize, pinSize);
    g2d.fillOval(width * 2 / 8, height * 6 / 8 - pinSize, pinSize, pinSize);
    g2d.setColor(LABEL_COLOR);
    g2d.setFont(LABEL_FONT.deriveFont(8f * width / 32));
    StringUtils.drawCenteredText(g2d, "+", width * 2 / 8 + 1, height * 2 / 8 + 4 * width / 32, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(g2d, "-", width * 2 / 8 + 1, height * 6 / 8 - 5 * width / 32, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(g2d, "~", width * 6 / 8 - 2 * width / 32, height * 2 / 8 + 5 * width / 32, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(g2d, "~", width * 6 / 8 - 2 * width / 32, height * 6 / 8 - 5 * width / 32, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    if (bodyColor == null) {
      bodyColor = BODY_COLOR;
    }
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    if (borderColor == null) {
      borderColor = BORDER_COLOR;
    }
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    if (labelColor == null) {
      labelColor = LABEL_COLOR;
    }
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty(name = "Type")
  public RectifierType getRectifierType() {
    return rectifierType;
  }
  
  public void setRectifierType(RectifierType rectifierType) {
    this.rectifierType = rectifierType;
    updateControlPoints();
    body = null;
  }
  
  @EditableProperty
  public Current getCurrent() {
    return current;
  }
  
  public void setCurrent(Current current) {
    this.current = current;
  }
  
  @EditableProperty
  public Voltage getVoltage() {
    return voltage;
  }
  
  public void setVoltage(Voltage voltage) {
    this.voltage = voltage;
  }
  
  @EditableProperty(name = "Label Orientation")
  public LabelOriantation getLabelOriantation() {
    if (labelOriantation == null)
      labelOriantation = LabelOriantation.Directional;
    return labelOriantation;
  }
  
  public void setLabelOriantation(LabelOriantation labelOriantation) {
    this.labelOriantation = labelOriantation;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {    
    int margin = 20;    
    Rectangle2D bounds = getBody()[0].getBounds2D();
    return new Rectangle2D.Double(bounds.getX() - margin, bounds.getY() - margin, bounds.getWidth() + 2 * margin, bounds.getHeight() + 2 * margin);
  }

  public static enum RectifierType {
    
    MiniDIP1("DFM A", PinShape.Square), 
    MiniDIP2("DFM B", PinShape.Square), 
    MiniRound1("Round WOG A", PinShape.Round), 
    MiniRound2("Round WOG B", PinShape.Round), 
    InLine("In-Line D-44", PinShape.Round), 
    SquareBR3("Square BR-3", PinShape.Round);
    
    private String label;
    private PinShape pinShape;
    
    private RectifierType(String label, PinShape pinShape) {
      this.label = label;
      this.pinShape = pinShape;
    }
    
    @Override
    public String toString() {
      return label;
    }

    public PinShape getPinShape() {
      return pinShape;
    }
  }
  
  private static enum PinShape {
    Square, Round
  }
}
