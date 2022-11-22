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
package org.diylc.components.passive;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.transform.PotentiometerTransformer;
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

@ComponentDescriptor(name = "Potentiometer", author = "Branislav Stojkovic", category = "Passive",
    creationMethod = CreationMethod.SINGLE_CLICK, instanceNamePrefix = "VR",
    description = "Panel mount potentiometer with solder lugs", zOrder = IDIYComponent.COMPONENT,
    transformer = PotentiometerTransformer.class, enableCache = true)
public class PotentiometerPanel extends AbstractPotentiometer {

  private static final long serialVersionUID = 1L;

  protected static Size BODY_DIAMETER = new Size(1d, SizeUnit.in);
  protected static Size SPACING = new Size(0.3d, SizeUnit.in);
  protected static Size LUG_DIAMETER = new Size(0.15d, SizeUnit.in);
  protected static Size PIN_SIZE = new Size(0.05d, SizeUnit.in);
  protected static Size NUT_SIZE = new Size(0.4d, SizeUnit.in);
  protected static Size SHAFT_SIZE = new Size(1 / 4d, SizeUnit.in);
  protected static Color BODY_COLOR = Color.lightGray;
  protected static Color WAFER_COLOR = Color.decode("#CD8500");
  protected static Color BORDER_COLOR = Color.gray;
  protected static Color NUT_COLOR = Color.decode("#CBD5DB");


  protected Size bodyDiameter = BODY_DIAMETER;
  protected Size spacing = SPACING;
  protected Size lugDiameter = LUG_DIAMETER;
  protected Color bodyColor = BODY_COLOR;
  protected Color borderColor = BORDER_COLOR;
  protected Color nutColor = NUT_COLOR;
  protected Color waferColor = WAFER_COLOR;
  protected Type type = Type.ThroughHole;
  @Deprecated
  protected boolean showShaft = false;
  protected View view;
  // Array of 7 elements: 3 lug connectors, 1 pot body and 3 lugs
  transient protected Area[] body = null;

  public PotentiometerPanel() {
    controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
    updateControlPoints();
  }

  protected void updateControlPoints() {
    int spacing = (int) this.spacing.convertToPixels();
    switch (orientation) {
      case DEFAULT:
        controlPoints[1].setLocation(controlPoints[0].getX() + spacing, controlPoints[0].getY());
        controlPoints[2].setLocation(controlPoints[0].getX() + 2 * spacing, controlPoints[0].getY());
        break;
      case _90:
        controlPoints[1].setLocation(controlPoints[0].getX(), controlPoints[0].getY() + spacing);
        controlPoints[2].setLocation(controlPoints[0].getX(), controlPoints[0].getY() + 2 * spacing);
        break;
      case _180:
        controlPoints[1].setLocation(controlPoints[0].getX() - spacing, controlPoints[0].getY());
        controlPoints[2].setLocation(controlPoints[0].getX() - 2 * spacing, controlPoints[0].getY());
        break;
      case _270:
        controlPoints[1].setLocation(controlPoints[0].getX(), controlPoints[0].getY() - spacing);
        controlPoints[2].setLocation(controlPoints[0].getX(), controlPoints[0].getY() - 2 * spacing);
        break;
      default:
        break;
    }
  }

  public Area[] getBody() {
    int spacing = (int) this.spacing.convertToPixels();
    int diameter = getClosestOdd(bodyDiameter.convertToPixels());
    if (body == null) {
      // mandatory: 0, 1, 2 pins, 3 body, 9 wafer
      // optional: 4, 5, 6 lugs, 7 nut, 8 shaft
      body = new Area[10];

      // Add lugs.
      int pinWidth = (int) PIN_SIZE.convertToPixels();

      double centerX = 0;
      double centerY = 0;

      double waferWidth = 2.5 * spacing;
      double waferHeight = spacing;

      switch (orientation) {
        case DEFAULT:
          centerX = controlPoints[0].getX() + spacing;
          centerY = controlPoints[0].getY() - spacing / 2 - diameter / 2;

          body[9] =
              new Area(new RoundRectangle2D.Double(centerX - waferWidth / 2, controlPoints[0].getY() - waferHeight - spacing
                  / 2, waferWidth, waferHeight, spacing / 4, spacing / 4));

          for (int i = 0; i < 3; i++) {
            body[i] =
                new Area(new Rectangle2D.Double(controlPoints[i].getX() - pinWidth / 2, controlPoints[i].getY()
                    - (spacing + diameter) / 2, pinWidth, (spacing + diameter) / 2));
          }
          break;
        case _90:
          centerX = controlPoints[0].getX() + spacing / 2 + diameter / 2;
          centerY = controlPoints[0].getY() + spacing;

          body[9] =
              new Area(new RoundRectangle2D.Double(controlPoints[0].getX() + spacing / 2, centerY - waferWidth / 2,
                  waferHeight, waferWidth, spacing / 4, spacing / 4));

          for (int i = 0; i < 3; i++) {
            body[i] =
                new Area(new Rectangle2D.Double(controlPoints[i].getX(), controlPoints[i].getY() - pinWidth / 2,
                    (spacing + diameter) / 2, pinWidth));
          }
          break;
        case _180:
          centerX = controlPoints[0].getX() - spacing;
          centerY = controlPoints[0].getY() + spacing / 2 + diameter / 2;

          body[9] =
              new Area(new RoundRectangle2D.Double(centerX - waferWidth / 2, controlPoints[0].getY() + spacing / 2,
                  waferWidth, waferHeight, spacing / 4, spacing / 4));

          for (int i = 0; i < 3; i++) {
            body[i] =
                new Area(new Rectangle2D.Double(controlPoints[i].getX() - pinWidth / 2, controlPoints[i].getY(), pinWidth,
                    (spacing + diameter) / 2));
          }
          break;
        case _270:
          centerX = controlPoints[0].getX() - spacing / 2 - diameter / 2;
          centerY = controlPoints[0].getY() - spacing;

          body[9] =
              new Area(new RoundRectangle2D.Double(controlPoints[0].getX() - waferHeight - spacing / 2, centerY - waferWidth
                  / 2, waferHeight, waferWidth, spacing / 4, spacing / 4));

          for (int i = 0; i < 3; i++) {
            body[i] =
                new Area(new Rectangle2D.Double(controlPoints[i].getX() - (spacing + diameter) / 2, controlPoints[i].getY()
                    - pinWidth / 2, (spacing + diameter) / 2, pinWidth));
          }
          break;
        default:
          break;
      }

      body[3] = new Area(new Ellipse2D.Double(centerX - diameter / 2, centerY - diameter / 2, diameter, diameter));

      body[9].subtract(body[3]);

      for (int i = 0; i < 3; i++) {
        body[i].subtract(body[3]);
      }

      if (getType() == Type.ThroughHole) {
        int lugDiameter = getClosestOdd(this.lugDiameter.convertToPixels());
        int holeDiameter = getClosestOdd(this.lugDiameter.convertToPixels() / 2);

        for (int i = 0; i < 3; i++) {
          Area area =
              new Area(new Ellipse2D.Double(controlPoints[i].getX() - lugDiameter / 2, controlPoints[i].getY() - lugDiameter / 2,
                  lugDiameter, lugDiameter));
          body[4 + i] = area;
        }

        for (int i = 0; i < 3; i++) {
          for (int j = 0; j < 3; j++) {
            body[i].subtract(body[4 + j]);
          }
        }
        // Make holes in the lugs.
        for (int i = 0; i < 3; i++) {
          body[4 + i].subtract(new Area(new Ellipse2D.Double(controlPoints[i].getX() - holeDiameter / 2, controlPoints[i].getY()
              - holeDiameter / 2, holeDiameter, holeDiameter)));
        }
      }
      
      if (getView() == View.ShaftUp) {
        int nutSize = (int) NUT_SIZE.convertToPixels();
        int shaftSize = (int) SHAFT_SIZE.convertToPixels();
        int[] xPoints = new int[6];
        int[] yPoints = new int[6];
        for (int i = 0; i < 6; i++) {
          double alpha = Math.toRadians(60 * i);
          xPoints[i] = (int) (centerX + Math.cos(alpha) * nutSize / 2);
          yPoints[i] = (int) (centerY + Math.sin(alpha) * nutSize / 2);
        }
        body[7] = new Area(new Polygon(xPoints, yPoints, 6));
        body[8] =
            new Area(new Ellipse2D.Double(centerX - shaftSize / 2, centerY - shaftSize / 2, shaftSize, shaftSize));
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
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    Area[] body = getBody();
    for (int i = 0; i < body.length; i++) {
      Area shape = body[i];      
      // determine color
      if (shape != null) {
        switch (i) {
          case 7:
            g2d.setColor(getNutColor());
            break;
          case 9:
            g2d.setColor(getWaferColor());
            break;
          default:
            g2d.setColor(getBodyColor());
        }

        Composite oldComposite = g2d.getComposite();
        if (alpha < MAX_ALPHA) {
          g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
        }
        if (!outlineMode) {
//          if (i == 3)
//            drawingObserver.startTrackingContinuityArea(true);
          g2d.fill(shape);
//          if (i == 3)
//            drawingObserver.stopTrackingContinuityArea();
        }
        g2d.setComposite(oldComposite);
        Color finalBorderColor;
        if (outlineMode) {
          finalBorderColor =
              componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
                  : theme.getOutlineColor();
        } else {
          finalBorderColor =
              componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
                  : getBorderColor();
        }
        g2d.setColor(finalBorderColor);
        g2d.draw(shape);
      }
    }
    // Draw caption.
    g2d.setFont(project.getFont());
    
    // Override font size
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
    g2d.setColor(finalLabelColor);
    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D bodyRect = getBody()[3].getBounds2D();
    Rectangle2D rect = fontMetrics.getStringBounds(getName(), g2d);

    int textHeight = (int) rect.getHeight();
    int textWidth = (int) rect.getWidth();
    int panelHeight = (int) bodyRect.getHeight();
    int panelWidth = (int) bodyRect.getWidth();

    int x = (panelWidth - textWidth) / 2;
    int y = panelHeight / 2 - textHeight + fontMetrics.getAscent();

    g2d.drawString(getName(), (int) (bodyRect.getX() + x), (int) (bodyRect.getY() + y));

    // Draw value.
    rect = fontMetrics.getStringBounds(getValueForDisplay(), g2d);

    textHeight = (int) rect.getHeight();
    textWidth = (int) rect.getWidth();

    x = (panelWidth - textWidth) / 2;
    y = panelHeight / 2 + fontMetrics.getAscent();

    g2d.drawString(getValueForDisplay(), (int) (bodyRect.getX() + x), (int) (bodyRect.getY() + y));
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 4 * width / 32;
    int waferMargin = 5 * width / 32;
    int spacing = width / 3 - 1;
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1 * width / 32));
    g2d.setColor(WAFER_COLOR);
    g2d.fillRect(waferMargin, height / 2, width - 2 * waferMargin, height / 3);    
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(waferMargin, height / 2, width - 2 * waferMargin, height / 3, 2 * width / 32, 2 * width / 32);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2 * width / 32));
    g2d.drawLine(width / 2 - spacing, height / 2, width / 2 - spacing, height - margin);
    g2d.drawLine(width / 2 + spacing, height / 2, width / 2 + spacing, height - margin);
    g2d.drawLine(width / 2, height / 2, width / 2, height - margin);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(4 * width / 32));
    g2d.drawLine(width / 2 - spacing, height - margin, width / 2 - spacing, height - margin);
    g2d.drawLine(width / 2 + spacing, height - margin, width / 2 + spacing, height - margin);
    g2d.drawLine(width / 2, height - margin, width / 2, height - margin);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(BODY_COLOR);
    g2d.fillOval(margin, margin / 2, width - 2 * margin, height - 2 * margin);
    g2d.setColor(BORDER_COLOR);
    g2d.drawOval(margin, margin / 2, width - 2 * margin, height - 2 * margin);
  }

  @EditableProperty
  public Size getSpacing() {
    return spacing;
  }

  public void setSpacing(Size spacing) {
    this.spacing = spacing;
    updateControlPoints();
    body = null;
  }

  @EditableProperty
  public Type getType() {
    if (type == null)
      type = Type.ThroughHole;
    return type;
  }

  public void setType(Type type) {
    this.type = type;
    body = null;
  }

  @EditableProperty(name = "Nut")
  public Color getNutColor() {
    if (nutColor == null)
      nutColor = NUT_COLOR;
    return nutColor;
  }

  public void setNutColor(Color nutColor) {
    this.nutColor = nutColor;
  }

  @EditableProperty(name = "Diameter")
  public Size getBodyDiameter() {
    return bodyDiameter;
  }

  public void setBodyDiameter(Size bodyDiameter) {
    this.bodyDiameter = bodyDiameter;
    body = null;
  }

  @EditableProperty(name = "Lug Size")
  public Size getLugDiameter() {
    return lugDiameter;
  }

  public void setLugDiameter(Size lugDiameter) {
    this.lugDiameter = lugDiameter;
    body = null;
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

  @EditableProperty(name = "View")
  public View getView() {
    if (view == null)
      view = showShaft ? View.ShaftUp : View.ShaftDown;
    return view;
  }
  
  public void setView(View view) {
    this.view = view;
    body = null;
  }

  @EditableProperty(name = "Wafer")
  public Color getWaferColor() {
    if (waferColor == null)
      waferColor = WAFER_COLOR;
    return waferColor;
  }

  public void setWaferColor(Color waferColor) {
    this.waferColor = waferColor;
  }
  
  @Override
  public String getInternalLinkName(int index1, int index2) {
    if (index1 > index2)
      return getInternalLinkName(index2, index1);
    
    if (index2 - index1 == 1)
      return (index1 + 1) + "-" + (index2 + 1);
    
    return null;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    Shape[] body = getBody();
    int margin = 20;
    double minX = 0;
    double minY = 0;
    double maxX = 0;
    double maxY = 0;
    for (Shape a : body) {
      if (a != null) {
        Rectangle2D bounds2d = a.getBounds2D();
        if (bounds2d.getMinX() < minX)
          minX = bounds2d.getMinX();
        if (bounds2d.getMinY() < minY)
          minY = bounds2d.getMinY();
        if (bounds2d.getMaxX() > maxX)
          maxX = bounds2d.getMaxX();
        if (bounds2d.getMaxY() > maxY)
          maxY = bounds2d.getMaxY();
      }
    }
    return new Rectangle2D.Double(minX - margin, minY - margin, maxX - minX + 2 * margin, maxY - minY + 2 * margin);
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  public enum Type {
    ThroughHole("Through Hole"), PCB("PCB");

    private String value;

    private Type(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }
  
  public enum View {
    ShaftDown("Shaft Down"), ShaftUp("Shaft Up");//, TerminalsDown, TerminalsUp;
    
    private String value;

    private View(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }
}
