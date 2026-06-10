package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
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
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.awt.StringUtils;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.transform.ThonkJackTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Thonk 3.5mm Jack", author = "DIYLC", category = "Electro-Mechanical",
    creationMethod = CreationMethod.SINGLE_CLICK, instanceNamePrefix = "J",
    description = "Thonkiconn style 3.5mm jack", zOrder = IDIYComponent.COMPONENT,
    transformer = ThonkJackTransformer.class)
public class ThonkJack3_5 extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  protected static Size BODY_WIDTH_MONO = new Size(9.0d, SizeUnit.mm);
  protected static Size BODY_WIDTH_STEREO = new Size(10.0d, SizeUnit.mm);
  protected static Size BODY_HEIGHT = new Size(10.5d, SizeUnit.mm);

  protected static Size THREAD_DIAMETER = new Size(6.0d, SizeUnit.mm); 
  protected static Size HOLE_DIAMETER = new Size(3.5d, SizeUnit.mm); 
  
  private static Color DEFAULT_BODY_COLOR = Color.decode("#666666");
  private static Color BORDER_COLOR = Color.black;
  private static Color SHAFT_COLOR = Color.lightGray;
  private static Color SHAFT_BORDER_COLOR = SHAFT_COLOR.darker();
  private static Color HOLE_COLOR = Color.decode("#111111");
  public static Color PIN_COLOR = METAL_COLOR;
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Size PIN_SIZE = new Size(0.03d, SizeUnit.in);
  protected static Display DISPLAY = Display.NAME;

  protected Color bodyColor = DEFAULT_BODY_COLOR;
  protected Color borderColor = BORDER_COLOR;
  protected Color labelColor = Color.white;
  protected Display display = DISPLAY;
  
  transient protected Shape[] body = null;

  protected ThonkJackType type = ThonkJackType.MONO_SWITCHED;
  protected Orientation orientation = Orientation.DEFAULT;

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0)};

  public ThonkJack3_5() {
    super();
    updateControlPoints();
  }

  protected void updateControlPoints() {
    double dy1_mm = -11.4; // Tip (Pin 3)
    double dy2_mm = -3.1;  // Switch/Ring (Pin 2)
    
    int dy1_px = getClosestOdd(new Size(dy1_mm, SizeUnit.mm).convertToPixels());
    int dy2_px = getClosestOdd(new Size(dy2_mm, SizeUnit.mm).convertToPixels());
    
    int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
    switch (orientation) {
      case DEFAULT:
        dx1 = 0; dy1 = dy1_px;
        dx2 = 0; dy2 = dy2_px;
        break;
      case _90:
        dx1 = -dy1_px; dy1 = 0;
        dx2 = -dy2_px; dy2 = 0;
        break;
      case _180:
        dx1 = 0; dy1 = -dy1_px;
        dx2 = 0; dy2 = -dy2_px;
        break;
      case _270:
        dx1 = dy1_px; dy1 = 0;
        dx2 = dy2_px; dy2 = 0;
        break;
    }
    controlPoints[1].setLocation(controlPoints[0].getX() + dx1, controlPoints[0].getY() + dy1);
    controlPoints[2].setLocation(controlPoints[0].getX() + dx2, controlPoints[0].getY() + dy2);
  }

  public Shape[] getBody() {
    if (body == null) {
      int width = getClosestOdd((type == ThonkJackType.MONO_SWITCHED ? BODY_WIDTH_MONO : BODY_WIDTH_STEREO).convertToPixels());
      int height = getClosestOdd(BODY_HEIGHT.convertToPixels());
      int threadDiam = getClosestOdd(THREAD_DIAMETER.convertToPixels());
      int holeDiam = getClosestOdd(HOLE_DIAMETER.convertToPixels());
      
      double centerX = controlPoints[0].getX();
      double centerY = controlPoints[0].getY();

      int centerOffset_px = getClosestOdd(new Size(-7.25, SizeUnit.mm).convertToPixels());
      
      double bodyCenterX = centerX;
      double bodyCenterY = centerY;
      switch (orientation) {
        case DEFAULT: bodyCenterY += centerOffset_px; break;
        case _90: bodyCenterX -= centerOffset_px; break;
        case _180: bodyCenterY -= centerOffset_px; break;
        case _270: bodyCenterX += centerOffset_px; break;
      }

      double housingY = bodyCenterY - height / 2.0;
      double housingX = bodyCenterX - width / 2.0;
      
      double socketOffset = new Size(1.5, SizeUnit.mm).convertToPixels();
      double shaftCenterY = housingY + height - socketOffset - threadDiam / 2.0;

      Area housingArea = new Area(new RoundRectangle2D.Double(housingX, housingY, width, height, 2, 2));
      Area threadArea = new Area(new Ellipse2D.Double(bodyCenterX - threadDiam / 2.0, shaftCenterY - threadDiam / 2.0, threadDiam, threadDiam));
      Area socketHole = new Area(new Ellipse2D.Double(bodyCenterX - holeDiam / 2.0, shaftCenterY - holeDiam / 2.0, holeDiam, holeDiam));

      double pin1Y_end = bodyCenterY + new Size(7.25, SizeUnit.mm).convertToPixels();
      double bodyBottom = housingY + height;
      double flangeHeight = Math.max(1, pin1Y_end - bodyBottom);
      int pinWidth = getClosestOdd(new Size(1.0, SizeUnit.mm).convertToPixels());
      Area pin1Flange = new Area(new Rectangle2D.Double(bodyCenterX - pinWidth / 2.0, bodyBottom, pinWidth, flangeHeight));

      body = new Shape[4];
      body[0] = housingArea;
      body[1] = threadArea; 
      body[2] = socketHole;
      body[3] = pin1Flange;

      double angle = 0;
      switch (orientation) {
        case _90: angle = Math.PI / 2; break;
        case _180: angle = Math.PI; break;
        case _270: angle = 3 * Math.PI / 2; break;
        default: break;
      }

      if (angle != 0) {
        AffineTransform rotation = AffineTransform.getRotateInstance(angle, bodyCenterX, bodyCenterY);
        for (int i = 0; i < body.length; i++) {
          if (body[i] != null) {
            body[i] = new Area(body[i]).createTransformedArea(rotation);
          }
        }
      }
    }
    return body;
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
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @EditableProperty(name = "Type")
  public ThonkJackType getType() {
    return type;
  }

  public void setType(ThonkJackType type) {
    this.type = type;
    body = null;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    body = null;
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    if (type == ThonkJackType.MONO_SWITCHED) {
      switch (index) {
        case 0: return "Ground";
        case 1: return "Tip";
        case 2: return "Switch";
      }
    } else {
      switch (index) {
        case 0: return "Ground";
        case 1: return "Tip";
        case 2: return "Ring";
      }
    }
    return null;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    Shape housing = getBody()[0];
    Shape thread = getBody()[1];
    Shape socketHole = getBody()[2];
    Shape pin1Flange = getBody()[3];
    
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    
    Composite oldComposite = applyAlpha(g2d, componentState);
    
    if (housing != null) {
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
      
      if (!outlineMode) {
        g2d.setColor(PIN_COLOR);
        g2d.fill(pin1Flange);
        g2d.setColor(PIN_BORDER_COLOR);
        g2d.draw(pin1Flange);

        g2d.setColor(SHAFT_COLOR.darker());
        g2d.fill(thread);
        g2d.setColor(SHAFT_BORDER_COLOR);
        g2d.draw(thread);

        g2d.setColor(HOLE_COLOR);
        g2d.fill(socketHole);
        g2d.setColor(SHAFT_BORDER_COLOR);
        g2d.draw(socketHole);
      } else {
        g2d.setColor(theme.getOutlineColor());
        g2d.draw(pin1Flange);
        g2d.draw(thread);
        g2d.draw(socketHole);
      }
    }
    
    g2d.setComposite(oldComposite);

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

    g2d.setFont(project.getFont());
    
    Color finalLabelColor;
    if (outlineMode) {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : labelColor;
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
    Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);

    int textHeight = (int) rect.getHeight();
    int textWidth = (int) rect.getWidth();
    
    double centerX = controlPoints[0].getX();
    double centerY = controlPoints[0].getY();
    int centerOffset_px = getClosestOdd(new Size(-7.25, SizeUnit.mm).convertToPixels());
    
    double bodyCenterX = centerX;
    double bodyCenterY = centerY;
    switch (orientation) {
      case DEFAULT: bodyCenterY += centerOffset_px; break;
      case _90: bodyCenterX -= centerOffset_px; break;
      case _180: bodyCenterY -= centerOffset_px; break;
      case _270: bodyCenterX += centerOffset_px; break;
    }

    // Place text 25% above the centerline
    int textOffset_px = -getClosestOdd(new Size(BODY_HEIGHT.getValue() * 0.25, SizeUnit.mm).convertToPixels());
    Point2D textCenter = new Point2D.Double(bodyCenterX, bodyCenterY + textOffset_px);

    double angle = 0;
    switch (orientation) {
      case _90: angle = Math.PI / 2; break;
      case _180: angle = Math.PI; break;
      case _270: angle = 3 * Math.PI / 2; break;
      default: break;
    }
    if (angle != 0) {
      AffineTransform rotation = AffineTransform.getRotateInstance(angle, bodyCenterX, bodyCenterY);
      rotation.transform(textCenter, textCenter);
    }

    int drawX = (int) Math.round(textCenter.getX() - textWidth / 2);
    int drawY = (int) (textCenter.getY() - textHeight / 2 + fontMetrics.getAscent()); 

    g2d.drawString(label, drawX, drawY);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int bodyWidth = 16;
    int bodyHeight = 20;
    int bodyX = width / 2 - bodyWidth / 2;
    int bodyY = height / 2 - bodyHeight / 2 - 2; 
    
    int threadDiam = 10;
    int holeDiam = 6;
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    g2d.setColor(bodyColor);
    g2d.fillRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, 2, 2);
    g2d.setColor(borderColor);
    g2d.drawRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, 2, 2);

    int flangeHeight = 4;
    g2d.setColor(PIN_COLOR);
    g2d.fillRect(width / 2 - 1, bodyY + bodyHeight, 2, flangeHeight);
    g2d.setColor(PIN_BORDER_COLOR);
    g2d.drawRect(width / 2 - 1, bodyY + bodyHeight, 2, flangeHeight);
    
    int socketOffset = 3;
    g2d.setColor(SHAFT_COLOR.darker());
    g2d.fillOval(width / 2 - threadDiam / 2, bodyY + bodyHeight - threadDiam - socketOffset, threadDiam, threadDiam);
    g2d.setColor(SHAFT_BORDER_COLOR);
    g2d.drawOval(width / 2 - threadDiam / 2, bodyY + bodyHeight - threadDiam - socketOffset, threadDiam, threadDiam);

    g2d.setColor(HOLE_COLOR);
    g2d.fillOval(width / 2 - holeDiam / 2, bodyY + bodyHeight - threadDiam / 2 - holeDiam / 2 - socketOffset, holeDiam, holeDiam);
    g2d.setColor(SHAFT_BORDER_COLOR);
    g2d.drawOval(width / 2 - holeDiam / 2, bodyY + bodyHeight - threadDiam / 2 - holeDiam / 2 - socketOffset, holeDiam, holeDiam);

    int pinSize = 3;

    int pinY3 = bodyY + 2;
    g2d.setColor(PIN_COLOR);
    g2d.fillRect(width / 2 - pinSize / 2, pinY3, pinSize, pinSize);
    g2d.setColor(PIN_BORDER_COLOR);
    g2d.drawRect(width / 2 - pinSize / 2, pinY3, pinSize, pinSize);

    int pinY2 = bodyY + 14;
    g2d.setColor(PIN_COLOR);
    g2d.fillRect(width / 2 - pinSize / 2, pinY2, pinSize, pinSize);
    g2d.setColor(PIN_BORDER_COLOR);
    g2d.drawRect(width / 2 - pinSize / 2, pinY2, pinSize, pinSize);

    int pinY1 = bodyY + bodyHeight + flangeHeight;
    g2d.setColor(PIN_COLOR);
    g2d.fillRect(width / 2 - pinSize / 2, pinY1, pinSize, pinSize);
    g2d.setColor(PIN_BORDER_COLOR);
    g2d.drawRect(width / 2 - pinSize / 2, pinY1, pinSize, pinSize);
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Label Color")
  public Color getLabelColor() {
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
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

  @Override
  public String getValue() {
    return "";
  }

  @Override
  public void setValue(String value) {
    // nothing
  }

  public static enum ThonkJackType {
    MONO_SWITCHED("Mono Switched"), STEREO("Stereo");

    String label;

    private ThonkJackType(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
