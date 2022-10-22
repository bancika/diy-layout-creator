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
package org.diylc.components.tube;

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
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractAngledComponent;
import org.diylc.components.transform.AngledComponentTransformer;
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

@ComponentDescriptor(name = "Tube Socket", author = "Branislav Stojkovic", category = "Tubes",
    instanceNamePrefix = "V", description = "Various types of tube/valve sockets",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE, enableCache = true,
    transformer = AngledComponentTransformer.class)
public class TubeSocket extends AbstractAngledComponent<String> {

  private static final Size B9A_PIN_SPACING_CHASSIS = new Size(12.5d, SizeUnit.mm);
  private static final Size B9A_PIN_SPACING_PCB = new Size(21d, SizeUnit.mm);
  private static final Size OCTAL_PIN_SPACING = new Size(17.5d, SizeUnit.mm);
  private static final Size B7G_PIN_SPACING = new Size(12d, SizeUnit.mm);
  private static final Size B7G_CUTOUT_DIAMETER = new Size(4d, SizeUnit.mm);
  private static final Size B9A_CUTOUT_DIAMETER = new Size(5.5d, SizeUnit.mm);
  private static final Size B12C_HOLE_SIZE = new Size(10d, SizeUnit.mm);
  private static final Size B12C_CUTOUT_DIAMETER = new Size(5.5d, SizeUnit.mm);
  private static final Size B12C_PIN_SPACING_PCB = new Size(25d, SizeUnit.mm);
  private static final Size B12C_PIN_SPACING_CHASSIS = new Size(22d, SizeUnit.mm);

  private static final Size OCTAL_DIAMETER = new Size(25d, SizeUnit.mm);
  private static final Size B9A_DIAMETER = new Size(3 / 4d, SizeUnit.in);
  private static final Size B7G_DIAMETER = new Size(17d, SizeUnit.mm);
  private static final Size B12C_DIAMETER = new Size(30d, SizeUnit.mm);

  private static final long serialVersionUID = 1L;

  private static Color BODY_COLOR = Color.decode("#F7F7EF");
  private static Color LABEL_COLOR = BODY_COLOR.darker();
  public static Color PIN_COLOR = METAL_COLOR;
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Size PIN_DIAMETER = new Size(1d, SizeUnit.mm);
  private static Size PIN_WIDTH = new Size(0.08d, SizeUnit.in);
  private static Size PIN_THICKNESS = new Size(0.02d, SizeUnit.in);
  public static Size HOLE_SIZE = new Size(5d, SizeUnit.mm);
  public static Size OCTAL_TICK_SIZE = new Size(2d, SizeUnit.mm);

  private Base base = Base.B9A;
  private String type = "";
  private Color color = BODY_COLOR;
  private String electrodeLabels = null; 
  private Mount mount = Mount.CHASSIS;
  private Color labelColor = LABEL_COLOR;

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};

  public TubeSocket() {
    super();
    updateControlPoints();
  }

  @EditableProperty
  public Base getBase() {
    return base;
  }

  public void setBase(Base base) {
    this.base = base;
    updateControlPoints();
    // Reset body shape
    body = null;
  }

  @EditableProperty
  public Color getColor() {
    if (color == null) {
      color = BODY_COLOR;
    }
    return color;
  }
  
  public void setColor(Color color) {
    this.color = color;
  }

  // @EditableProperty
  // public Mount getMount() {
  // return mount;
  // }
  //
  // public void setMount(Mount mount) {
  // this.mount = mount;
  // }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    int pinCount;
    int pinSpacing;
    boolean hasEmptySpace;
    switch (base) {
      case B7G:
        pinCount = 7;
        pinSpacing = getClosestOdd(B7G_PIN_SPACING.convertToPixels());
        hasEmptySpace = true;
        break;
      case OCTAL:
        pinCount = 8;
        pinSpacing = getClosestOdd(OCTAL_PIN_SPACING.convertToPixels());
        hasEmptySpace = false;
        break;
      case B9A:
        pinCount = 9;
        pinSpacing = getClosestOdd(getMount() == Mount.PCB ? B9A_PIN_SPACING_PCB.convertToPixels() : B9A_PIN_SPACING_CHASSIS.convertToPixels());
        hasEmptySpace = true;
        break;
      case B12C:
        pinCount = 12;
        pinSpacing = getClosestOdd(getMount() == Mount.PCB ? B12C_PIN_SPACING_PCB.convertToPixels() : B12C_PIN_SPACING_CHASSIS.convertToPixels());
        hasEmptySpace = true;
        break;
      default:
        throw new RuntimeException("Unexpected base: " + base);
    }
    double angleIncrement = Math.PI * 2 / (hasEmptySpace ? (pinCount + 1) : pinCount);
    double initialAngleOffset = hasEmptySpace ? angleIncrement : (angleIncrement / 2);

    controlPoints = new Point2D[pinCount + 1];
    double theta = initialAngleOffset + getAngle().getValueRad();
    controlPoints[0] = firstPoint;
    for (int i = 0; i < pinCount; i++) {
      controlPoints[i + 1] =
          new Point2D.Double(firstPoint.getX() + Math.cos(theta) * pinSpacing / 2,
              firstPoint.getY() + Math.sin(theta) * pinSpacing / 2);
      theta += angleIncrement;
    }
  }

  public Shape[] getBody() {
    if (body == null) {
      int bodyDiameter;
      switch (base) {
        case B7G:
          bodyDiameter = getClosestOdd(B7G_DIAMETER.convertToPixels());
          break;
        case B9A:
          bodyDiameter = getClosestOdd(B9A_DIAMETER.convertToPixels());
          break;
        case OCTAL:
          bodyDiameter = getClosestOdd(OCTAL_DIAMETER.convertToPixels());
          break;
        case B12C:
          bodyDiameter = getClosestOdd(B12C_DIAMETER.convertToPixels());
          break;
        default:
          throw new RuntimeException("Unexpected base: " + base);
      }
      
      Ellipse2D baseShape =
          new Ellipse2D.Double(controlPoints[0].getX() - bodyDiameter / 2, controlPoints[0].getY() - bodyDiameter / 2,
              bodyDiameter, bodyDiameter);
      Area bodyArea = new Area(baseShape);
      int holeSize = getClosestOdd(base == Base.B12C ? B12C_HOLE_SIZE.convertToPixels() : HOLE_SIZE.convertToPixels());
      bodyArea.subtract(new Area(new Ellipse2D.Double(controlPoints[0].getX() - holeSize / 2, controlPoints[0].getY() - holeSize
          / 2, holeSize, holeSize)));
      
      if (base == Base.OCTAL) {
        int tickSize = getClosestOdd(OCTAL_TICK_SIZE.convertToPixels());
        double theta = getAngle().getValueRad();
        int centerX = (int) (controlPoints[0].getX() + Math.cos(theta) * holeSize / 2);
        int centerY = (int) (controlPoints[0].getY() + Math.sin(theta) * holeSize / 2);
        bodyArea.subtract(new Area(new Ellipse2D.Double(centerX - tickSize / 2, centerY - tickSize / 2, tickSize,
            tickSize)));        
      } else if (base == Base.B9A && getMount() == Mount.CHASSIS) {
        double cutoutDiameter = getClosestOdd(B9A_CUTOUT_DIAMETER.convertToPixels());
        bodyArea.subtract(new Area(new Ellipse2D.Double(controlPoints[0].getX() - cutoutDiameter / 2, controlPoints[0].getY() - bodyDiameter / 2 - cutoutDiameter * 3 / 4, cutoutDiameter, cutoutDiameter)));
        bodyArea.subtract(new Area(new Ellipse2D.Double(controlPoints[0].getX() - cutoutDiameter / 2, controlPoints[0].getY() + bodyDiameter / 2 - cutoutDiameter / 4, cutoutDiameter, cutoutDiameter)));
      } else if (base == Base.B7G && getMount() == Mount.CHASSIS) {
        double cutoutDiameter = getClosestOdd(B7G_CUTOUT_DIAMETER.convertToPixels());
        bodyArea.subtract(new Area(new Ellipse2D.Double(controlPoints[0].getX() - cutoutDiameter / 2, controlPoints[0].getY() - bodyDiameter / 2 - cutoutDiameter * 3 / 4, cutoutDiameter, cutoutDiameter)));
        bodyArea.subtract(new Area(new Ellipse2D.Double(controlPoints[0].getX() - cutoutDiameter / 2, controlPoints[0].getY() + bodyDiameter / 2 - cutoutDiameter / 4, cutoutDiameter, cutoutDiameter)));
      } else if (base == Base.B12C && getMount() == Mount.CHASSIS) {
        double cutoutDiameter = getClosestOdd(B12C_CUTOUT_DIAMETER.convertToPixels());
        bodyArea.subtract(new Area(new Ellipse2D.Double(controlPoints[0].getX() - cutoutDiameter / 2, controlPoints[0].getY() - bodyDiameter / 2 - cutoutDiameter * 3 / 4, cutoutDiameter, cutoutDiameter)));
        bodyArea.subtract(new Area(new Ellipse2D.Double(controlPoints[0].getX() - cutoutDiameter / 2, controlPoints[0].getY() + bodyDiameter / 2 - cutoutDiameter / 4, cutoutDiameter, cutoutDiameter)));
      }    
      
      body = new Shape[] { bodyArea };
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    // Draw body
    Shape[] body = getBody();
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    if (componentState != ComponentState.DRAGGING) {
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getColor());
      g2d.fill(body[0]);
    }
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
              : getColor().darker();
    }
    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);
    
    drawingObserver.stopTracking();
    
    // Draw pins

    for (int i = 1; i < controlPoints.length; i++) {
      Shape pinShape;
      if (getMount() == Mount.PCB) {
        int pinSize = getClosestOdd(PIN_DIAMETER.convertToPixels());
        pinShape = new Ellipse2D.Double(controlPoints[i].getX() - pinSize / 2,
            controlPoints[i].getY() - pinSize / 2, pinSize, pinSize);
      } else {
        int pinWidth = getClosestOdd(PIN_WIDTH.convertToPixels());
        int pinThickness = getClosestOdd(PIN_THICKNESS.convertToPixels());
        pinShape = new Rectangle2D.Double(controlPoints[i].getX() - pinWidth / 2,
            controlPoints[i].getY() - pinThickness / 2, pinWidth, pinThickness);
        double theta = Math.atan2(controlPoints[i].getY() - controlPoints[0].getY(),
            controlPoints[i].getX() - controlPoints[0].getX()) + Math.PI / 2;
        Area rotatedPin = new Area(pinShape);
        rotatedPin.transform(AffineTransform.getRotateInstance(theta, controlPoints[i].getX(),
            controlPoints[i].getY()));
        pinShape = rotatedPin;
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : PIN_COLOR);
      if (getMount() == Mount.CHASSIS)
        drawingObserver.startTrackingContinuityArea(true);
      g2d.fill(pinShape);
      drawingObserver.stopTrackingContinuityArea();
      if (!outlineMode) {
        g2d.setColor(PIN_BORDER_COLOR);
        g2d.draw(pinShape);
      }
    }
    
    // draw electrode labels
    if (getElectrodeLabels() != null) {
      g2d.setColor(getLabelColor());
      g2d.setFont(project.getFont().deriveFont((float) (project.getFont().getSize2D() * 0.8)));
      String[] labels = getElectrodeLabels().split(",");
      double electrodeLabelOffset = project.getFont().getSize2D() * (getBase() == Base.B9A && getMount() == Mount.PCB ? 1.5 : 1);
      for (int i = 0; i < labels.length; i++) {
        if (i < controlPoints.length - 1) {
          String label = labels[i];
          double theta = Math.atan2(controlPoints[i + 1].getY() - controlPoints[0].getY(), controlPoints[i + 1].getX() - controlPoints[0].getX());
          double x = controlPoints[i + 1].getX() - Math.cos(theta) * electrodeLabelOffset;
          double y = controlPoints[i + 1].getY() - Math.sin(theta) * electrodeLabelOffset;
          StringUtils.drawCenteredText(g2d, label, (int)x, (int)y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        }
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    Area area = new Area(new Ellipse2D.Double(1, 1, width - 2, width - 2));
    int center = width / 2;
    area.subtract(new Area(new Ellipse2D.Double(center - 2, center - 2, 5, 5)));
    g2d.setColor(BODY_COLOR);
    g2d.fill(area);
    g2d.setColor(BODY_COLOR.darker());
    g2d.draw(area);

    int radius = width / 2 - 6;
    for (int i = 0; i < 8; i++) {
      int x = (int) (center + Math.cos(i * Math.PI / 4) * radius);
      int y = (int) (center + Math.sin(i * Math.PI / 4) * radius);
      g2d.setColor(PIN_COLOR);
      g2d.fillOval(x - 1, y - 1, 3, 3);
      g2d.setColor(PIN_BORDER_COLOR);
      g2d.drawOval(x - 1, y - 1, 3, 3);
    }
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
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
  @EditableProperty(name = "Designation")
  public String getValue() {
    return type;
  }

  @Override
  public void setValue(String value) {
    this.type = value;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index > 0;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    
    body = null;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
  
  @EditableProperty(name = "Electrode Labels")
  public String getElectrodeLabels() {
    if (electrodeLabels == null)
      electrodeLabels = "1,2,3,4,5,6,7,8,9,10,11,12";
    return electrodeLabels;
  }
  
  public void setElectrodeLabels(String electrodeLabels) {
    this.electrodeLabels = electrodeLabels;
  }
  
  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    if (labelColor == null)
      labelColor = LABEL_COLOR;
    return labelColor;
  }
  
  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }
  
  @EditableProperty(name = "Mount")
  public Mount getMount() {
    if (mount == null)
      mount = Mount.CHASSIS;
    return mount;
  }
  
  public void setMount(Mount mount) {
    this.mount = mount;
    
    updateControlPoints();
    // Reset body shape
    body = null;
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    String[] labels = getElectrodeLabels().split(",");
    return index > 0 && index <= labels.length ? labels[index - 1] : null;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    Rectangle2D rect = getBody()[0].getBounds2D();
    int margin = 40; // to catch the pins that are outside the body, if needed
    return new Rectangle2D.Double(rect.getX() - margin, rect.getY() - margin, rect.getWidth() + 2 * margin, rect.getHeight() + 2 * margin);
  }

  public static enum Base {
    B9A("Noval B9A"), OCTAL("Octal"), B7G("Small-button B7G"), B12C("Duodecar B12C");

    String name;

    private Base(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public static enum Mount {
    CHASSIS("Chassis"), PCB("PCB");

    String name;

    private Mount(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
