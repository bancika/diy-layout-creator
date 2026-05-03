package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
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
import org.diylc.components.transform.MiniaturePotentiometerTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Potentiometer (Miniature)", author = "Branislav Stojkovic", category = "Passive",
    creationMethod = CreationMethod.SINGLE_CLICK, instanceNamePrefix = "VR",
    description = "9mm Alpha style miniature potentiometers", zOrder = IDIYComponent.COMPONENT,
    transformer = MiniaturePotentiometerTransformer.class)
public class MiniaturePotentiometer extends AbstractPotentiometer {

  private static final long serialVersionUID = 1L;

  protected static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
  protected static Size BODY_WIDTH = new Size(9.5d, SizeUnit.mm);
  
  // Right-Angled dimensions
  protected static Size BODY_DEPTH_RIGHT_ANGLED = new Size(7.5d, SizeUnit.mm);
  protected static Size FRONT_TO_PINS_RIGHT_ANGLED = new Size(5.25d, SizeUnit.mm);
  protected static Size SHAFT_LENGTH = new Size(15d, SizeUnit.mm);
  
  // Vertical dimensions
  protected static Size BODY_HEIGHT_VERTICAL = new Size(11.35d, SizeUnit.mm);
  protected static Size SHAFT_OFFSET_VERTICAL = new Size(6.5d, SizeUnit.mm);
  protected static Size TABS_WIDTH_VERTICAL = new Size(12.0d, SizeUnit.mm);

  protected static Size SHAFT_DIAMETER = new Size(6.0d, SizeUnit.mm); 
  protected static Size THREAD_LENGTH = new Size(5d, SizeUnit.mm);
  protected static Size THREAD_DIAMETER = new Size(7d, SizeUnit.mm);
  
  private static Color BODY_COLOR = Color.decode("#42ac6a");
  private static Color BORDER_COLOR = BODY_COLOR.darker();
  private static Color SHAFT_COLOR = Color.lightGray;
  private static Color SHAFT_BORDER_COLOR = SHAFT_COLOR.darker();
  public static Color PIN_COLOR = METAL_COLOR;
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Size PIN_SIZE = new Size(0.03d, SizeUnit.in);
  protected static Display DISPLAY = Display.NAME;

  protected Color bodyColor = BODY_COLOR;
  protected Color borderColor = BORDER_COLOR;
  protected Display display = DISPLAY;
  
  transient protected Shape[] body = null;

  protected MiniatureType type = MiniatureType.RIGHT_ANGLED;

  public MiniaturePotentiometer() {
    controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
    updateControlPoints();
  }

  protected void updateControlPoints() {
    int spacing = getClosestOdd(PIN_SPACING.convertToPixels());
    int dx1 = 0;
    int dy1 = 0;
    int dx2 = 0;
    int dy2 = 0;
    switch (getOrientation()) {
      case DEFAULT:
        dx1 = spacing;
        dy1 = 0;
        dx2 = 2 * spacing;
        dy2 = 0;
        break;
      case _90:
        dx1 = 0;
        dy1 = spacing;
        dx2 = 0;
        dy2 = 2 * spacing;
        break;
      case _180:
        dx1 = -spacing;
        dy1 = 0;
        dx2 = -2 * spacing;
        dy2 = 0;
        break;
      case _270:
        dx1 = 0;
        dy1 = -spacing;
        dx2 = 0;
        dy2 = -2 * spacing;
        break;
    }
    controlPoints[1].setLocation(controlPoints[0].getX() + dx1, controlPoints[0].getY() + dy1);
    controlPoints[2].setLocation(controlPoints[0].getX() + dx2, controlPoints[0].getY() + dy2);
  }

  public Shape[] getBody() {
    if (body == null) {
      double centerX = controlPoints[1].getX();
      double centerY = controlPoints[1].getY();

      int width = getClosestOdd(BODY_WIDTH.convertToPixels());
      int shaftDiam = getClosestOdd(SHAFT_DIAMETER.convertToPixels());
      int threadDiam = getClosestOdd(THREAD_DIAMETER.convertToPixels());
      
      Area housingArea = null;
      Area shaftArea = null;
      Area threadArea = null;

      if (type == MiniatureType.RIGHT_ANGLED) {
        body = new Shape[4];
        int depth = getClosestOdd(BODY_DEPTH_RIGHT_ANGLED.convertToPixels());
        int frontToPins = getClosestOdd(FRONT_TO_PINS_RIGHT_ANGLED.convertToPixels());
        int shaftLength = getClosestOdd(SHAFT_LENGTH.convertToPixels());
        int threadLength = getClosestOdd(THREAD_LENGTH.convertToPixels());

        double housingY = centerY - frontToPins;
        double housingX = centerX - width / 2;
        
        double shaftY = housingY - shaftLength;
        double shaftX = centerX - shaftDiam / 2;
        
        double threadY = housingY - threadLength;
        double threadX = centerX - threadDiam / 2;
        
        housingArea = new Area(new RoundRectangle2D.Double(housingX, housingY, width, depth, 2, 2));
        shaftArea = new Area(new Rectangle2D.Double(shaftX, shaftY, shaftDiam, shaftLength - threadLength));
        threadArea = new Area(new Rectangle2D.Double(threadX, threadY, threadDiam, threadLength));
        
        java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
        int step = 4;
        for (double i = threadY + step; i <= threadY + threadLength; i += step) {
          path.moveTo(threadX + 1, i);
          path.lineTo(threadX + threadDiam - 1, i - step);
        }
        
        body[0] = housingArea;
        body[1] = shaftArea;
        body[2] = threadArea;
        body[3] = path;
      } else {
        body = new Shape[6];
        int height = getClosestOdd(BODY_HEIGHT_VERTICAL.convertToPixels());
        int shaftOffset = getClosestOdd(SHAFT_OFFSET_VERTICAL.convertToPixels());
        int tabsWidth = getClosestOdd(TABS_WIDTH_VERTICAL.convertToPixels());
        
        double housingY = centerY - height;
        double housingX = centerX - width / 2;
        double shaftCenterY = centerY - shaftOffset;
        
        housingArea = new Area(new RoundRectangle2D.Double(housingX, housingY, width, height, 2, 2));
        
        double tabWidth = (tabsWidth - width) / 2;
        double tabHeight = getClosestOdd(new Size(1.0, SizeUnit.mm).convertToPixels());
        double tabY = shaftCenterY - tabHeight / 2;
        Area leftTab = new Area(new Rectangle2D.Double(centerX - tabsWidth / 2, tabY, tabWidth, tabHeight));
        Area rightTab = new Area(new Rectangle2D.Double(centerX + width / 2, tabY, tabWidth, tabHeight));
        Area bracketArea = new Area();
        bracketArea.add(leftTab);
        bracketArea.add(rightTab);
        
        double holeRadius = getClosestOdd(new Size(0.6, SizeUnit.mm).convertToPixels());
        double offset = getClosestOdd(new Size(1.5, SizeUnit.mm).convertToPixels());
        
        // Top-Right rivet
        double hole1X = centerX + width / 2 - offset;
        double hole1Y = housingY + offset;
        Area hole1 = new Area(new Ellipse2D.Double(hole1X - holeRadius, hole1Y - holeRadius, holeRadius * 2, holeRadius * 2));
        
        // Bottom-Left rivet
        double hole2X = centerX - width / 2 + offset;
        double hole2Y = centerY - offset * 1.8;
        Area hole2 = new Area(new Ellipse2D.Double(hole2X - holeRadius, hole2Y - holeRadius, holeRadius * 2, holeRadius * 2));

        threadArea = new Area(new Ellipse2D.Double(centerX - threadDiam / 2, shaftCenterY - threadDiam / 2, threadDiam, threadDiam));
        shaftArea = new Area(new Ellipse2D.Double(centerX - shaftDiam / 2, shaftCenterY - shaftDiam / 2, shaftDiam, shaftDiam));

        body[0] = housingArea;
        body[1] = shaftArea; // Will be drawn as inner ring
        body[2] = threadArea; // Will be drawn as outer ring
        body[3] = hole1; // Rivet 1
        body[4] = hole2; // Rivet 2
        body[5] = bracketArea; // Side tabs
      }

      double angle = 0;
      switch (getOrientation()) {
        case _90:
          angle = Math.PI / 2;
          break;
        case _180:
          angle = Math.PI;
          break;
        case _270:
          angle = 3 * Math.PI / 2;
          break;
        default:
          break;
      }

      if (angle != 0) {
        AffineTransform rotation = AffineTransform.getRotateInstance(angle, centerX, centerY);
        for (int i = 0; i < body.length; i++) {
          if (body[i] != null) {
            if (body[i] instanceof Area) {
              body[i] = new Area(body[i]).createTransformedArea(rotation);
            } else if (body[i] instanceof java.awt.geom.GeneralPath) {
              ((java.awt.geom.GeneralPath) body[i]).transform(rotation);
            } else {
              body[i] = new Area(body[i]).createTransformedArea(rotation);
            }
          }
        }
      }
    }
    return body;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    super.setControlPoint(point, index);
    body = null;
  }

  @Override
  public void setOrientation(Orientation orientation) {
    super.setOrientation(orientation);
    updateControlPoints();
    body = null;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    Shape housing = getBody()[0];
    Shape shaft = getBody()[1];
    Shape thread = getBody()[2];
    
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    
    Composite oldComposite = applyAlpha(g2d, componentState);
    
    if (housing != null) {
      if (type == MiniatureType.RIGHT_ANGLED) {
        Shape threadPath = body.length > 3 ? body[3] : null;
        if (!outlineMode) {
          if (shaft != null) {
            g2d.setColor(SHAFT_COLOR);
            g2d.fill(shaft);
            g2d.setColor(SHAFT_BORDER_COLOR);
            g2d.draw(shaft);
          }
          if (thread != null) {
            g2d.setColor(SHAFT_COLOR);
            g2d.fill(thread);
            g2d.setColor(SHAFT_BORDER_COLOR);
            g2d.draw(thread);
            if (threadPath != null) {
              g2d.setColor(SHAFT_BORDER_COLOR);
              g2d.draw(threadPath);
            }
          }
        } else {
          if (shaft != null) {
            g2d.setColor(theme.getOutlineColor());
            g2d.draw(shaft);
          }
          if (thread != null) {
            g2d.setColor(theme.getOutlineColor());
            g2d.draw(thread);
          }
        }
      }
      
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
      g2d.fill(housing);
      drawingObserver.stopTracking();

      Color finalBorderColor;
      if (outlineMode) {
        finalBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
                : theme.getOutlineColor();
      } else {
        finalBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
                : borderColor;
      }
      g2d.setColor(finalBorderColor);
      g2d.draw(housing);
      
      if (type == MiniatureType.VERTICAL) {
        Shape rivet1 = body.length > 3 ? body[3] : null;
        Shape rivet2 = body.length > 4 ? body[4] : null;
        Shape bracket = body.length > 5 ? body[5] : null;
        
        if (!outlineMode) {
          if (bracket != null) {
            g2d.setColor(PIN_COLOR);
            g2d.fill(bracket);
            g2d.setColor(PIN_BORDER_COLOR);
            g2d.draw(bracket);
          }
          if (rivet1 != null) {
            g2d.setColor(SHAFT_COLOR);
            g2d.fill(rivet1);
            g2d.setColor(SHAFT_BORDER_COLOR);
            g2d.draw(rivet1);
          }
          if (rivet2 != null) {
            g2d.setColor(SHAFT_COLOR);
            g2d.fill(rivet2);
            g2d.setColor(SHAFT_BORDER_COLOR);
            g2d.draw(rivet2);
          }
          if (thread != null) {
            g2d.setColor(SHAFT_COLOR.darker());
            g2d.fill(thread);
            g2d.setColor(SHAFT_BORDER_COLOR);
            g2d.draw(thread);
          }
          if (shaft != null) {
            g2d.setColor(SHAFT_COLOR);
            g2d.fill(shaft);
            g2d.setColor(SHAFT_BORDER_COLOR);
            g2d.draw(shaft);
          }
        } else {
          if (bracket != null) {
            g2d.setColor(theme.getOutlineColor());
            g2d.draw(bracket);
          }
          if (rivet1 != null) {
            g2d.setColor(theme.getOutlineColor());
            g2d.draw(rivet1);
          }
          if (rivet2 != null) {
            g2d.setColor(theme.getOutlineColor());
            g2d.draw(rivet2);
          }
          if (thread != null) {
            g2d.setColor(theme.getOutlineColor());
            g2d.draw(thread);
          }
          if (shaft != null) {
            g2d.setColor(theme.getOutlineColor());
            g2d.draw(shaft);
          }
        }
      }
    }
    
    g2d.setComposite(oldComposite);

    // Draw pins
    int pinSize = getClosestOdd(PIN_SIZE.convertToPixels());
    for (Point2D point : controlPoints) {
      Rectangle2D rect = new Rectangle2D.Double(point.getX() - pinSize / 2.0, point.getY() - pinSize / 2.0, pinSize, pinSize);
      if (!outlineMode) {
        g2d.setColor(PIN_COLOR);
        g2d.fill(rect);
      }
      g2d.setColor(outlineMode ? theme.getOutlineColor() : PIN_BORDER_COLOR);
      g2d.draw(rect);
    }

    // Draw label
    g2d.setFont(project.getFont());
    if (getFontSizeOverride() != null)
      g2d.setFont(g2d.getFont().deriveFont(1f * getFontSizeOverride()));
    
    Color finalLabelColor;
    if (outlineMode) {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : LABEL_COLOR;
    }

    String label = "";
    label = (getDisplay() == Display.NAME) ? getName() : (getValue() == null ? "" : getValue().toString());
    if (getDisplay() == Display.NONE) {
      label = "";
    }
    if (getDisplay() == Display.BOTH) {
      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
    }

    g2d.setColor(finalLabelColor);
    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D bodyRect = housing.getBounds2D();
    Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);

    int textHeight = (int) rect.getHeight();
    int textWidth = (int) rect.getWidth();
    int panelHeight = (int) bodyRect.getHeight();
    int panelWidth = (int) bodyRect.getWidth();

    int x = (panelWidth - textWidth) / 2;
    int y = (panelHeight - textHeight) / 2 + fontMetrics.getAscent();

    g2d.drawString(label, (int) (bodyRect.getX() + x), (int) (bodyRect.getY() + y));
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int bodyWidth = 16;
    int bodyHeight = 12;
    int bodyX = width / 2 - bodyWidth / 2;
    int bodyY = height - 2 - bodyHeight;
    
    int threadWidth = 10;
    int threadLength = 4;
    int threadY = bodyY - threadLength;
    
    int shaftWidth = 6;
    int shaftLength = 10;
    int shaftY = threadY - shaftLength;
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    // Shaft
    g2d.setColor(SHAFT_COLOR);
    g2d.fillRect(width / 2 - shaftWidth / 2, shaftY, shaftWidth, shaftLength);
    g2d.setColor(SHAFT_BORDER_COLOR);
    g2d.drawRect(width / 2 - shaftWidth / 2, shaftY, shaftWidth, shaftLength);

    // Thread
    g2d.setColor(SHAFT_COLOR);
    g2d.fillRect(width / 2 - threadWidth / 2, threadY, threadWidth, threadLength);
    g2d.setColor(SHAFT_BORDER_COLOR);
    g2d.drawRect(width / 2 - threadWidth / 2, threadY, threadWidth, threadLength);
    g2d.drawLine(width / 2 - threadWidth / 2 + 1, threadY + 1, width / 2 + threadWidth / 2 - 1, threadY + 2);
    g2d.drawLine(width / 2 - threadWidth / 2 + 1, threadY + 3, width / 2 + threadWidth / 2 - 1, threadY + 4);

    // Housing
    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, 2, 2);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, 2, 2);
    
    // Pins
    int pinSize = 3;
    int pinY = bodyY + bodyHeight - pinSize / 2;
    g2d.setColor(PIN_COLOR);
    g2d.fillRect(bodyX + 2 - pinSize / 2, pinY, pinSize, pinSize);
    g2d.setColor(PIN_BORDER_COLOR);
    g2d.drawRect(bodyX + 2 - pinSize / 2, pinY, pinSize, pinSize);

    g2d.setColor(PIN_COLOR);
    g2d.fillRect(width / 2 - pinSize / 2, pinY, pinSize, pinSize);
    g2d.setColor(PIN_BORDER_COLOR);
    g2d.drawRect(width / 2 - pinSize / 2, pinY, pinSize, pinSize);

    g2d.setColor(PIN_COLOR);
    g2d.fillRect(bodyX + bodyWidth - 2 - pinSize / 2, pinY, pinSize, pinSize);
    g2d.setColor(PIN_BORDER_COLOR);
    g2d.drawRect(bodyX + bodyWidth - 2 - pinSize / 2, pinY, pinSize, pinSize);
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty
  public Display getDisplay() {
    if (display == null) {
      display = DISPLAY;
    }
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty(name = "Type")
  public MiniatureType getType() {
    return type;
  }

  public void setType(MiniatureType type) {
    this.type = type;
    updateControlPoints();
    body = null;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  public static enum MiniatureType {
    RIGHT_ANGLED("Right-Angled"), VERTICAL("Vertical");

    String label;

    private MiniatureType(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
