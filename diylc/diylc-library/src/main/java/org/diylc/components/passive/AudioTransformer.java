/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
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
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.gerber.IGerberComponentSimple;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Mini Signal Transformer", author = "Branislav Stojkovic", category = "Passive",
    instanceNamePrefix = "TR", description = "Miniature PCB-mount signal transformer with EI core",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE)
public class AudioTransformer extends AbstractMultiPartComponent<String> implements IGerberComponentSimple {

  private static final long serialVersionUID = 1L;

  public static Color CORE_COLOR = METAL_COLOR;
  public static Color CORE_BORDER_COLOR = CORE_COLOR.darker();
  public static Color COIL_COLOR = Color.decode("#DDDDDD");
  public static Color COIL_BORDER_COLOR = COIL_COLOR.darker();
  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color LABEL_COLOR = Color.white;
  public static int EDGE_RADIUS = 6;
  public static Size PIN_SIZE = new Size(0.03d, SizeUnit.in);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  private Size leadSpacing = new Size(0.1d, SizeUnit.in);
  private Size windingSpacing = new Size(0.5d, SizeUnit.in);
  private Size coreThickness = new Size(0.15d, SizeUnit.in);
  private Size coreWidth = new Size(0.6, SizeUnit.in);
  private Size coilWidth = new Size(0.5, SizeUnit.in);
  private Size coilLength = new Size(0.6, SizeUnit.in);
  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  protected Display display = Display.BOTH;
  private Color coreColor = CORE_COLOR;
  private Color coreBorderColor = CORE_BORDER_COLOR;
  private Color coilColor = COIL_COLOR;
  private Color coilBorderColor = COIL_BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  private boolean primaryCT = true;
  private boolean secondaryCT = true;

  transient private Area[] body;

  public AudioTransformer() {
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

  @EditableProperty(name = "Lead Spacing", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getLeadSpacing() {
    return leadSpacing;
  }

  public void setLeadSpacing(Size leadSpacing) {
    this.leadSpacing = leadSpacing;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty(name = "Winding Spacing", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getWindingSpacing() {
    return windingSpacing;
  }

  public void setWindingSpacing(Size rowSpacing) {
    this.windingSpacing = rowSpacing;
    updateControlPoints();
    // Reset body shape;
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

  private void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    int pinCount = 4 + (primaryCT ? 1 : 0) + (secondaryCT ? 1 : 0);
    controlPoints = new Point2D[pinCount];
    controlPoints[0] = firstPoint;
    double leadSpacing = this.leadSpacing.convertToPixels();
    double windingSpacing = this.windingSpacing.convertToPixels();
    
    // Update control points.
    for (int i = 1; i < 2 + (primaryCT ? 1 : 0); i++)
      controlPoints[i] = new Point2D.Double(firstPoint.getX(), firstPoint.getY() + i * leadSpacing * (primaryCT ? 1 : 2));    
    for (int i = 0; i < 2 + (secondaryCT ? 1 : 0); i++)
      controlPoints[2 + (primaryCT ? 1 : 0) + i] = new Point2D.Double(firstPoint.getX() + windingSpacing, firstPoint.getY() + i * leadSpacing * (secondaryCT ? 1 : 2));    
        
    AffineTransform tx = getTx();

    if (tx != null) {
      for (int i = 1; i < controlPoints.length; i++) {
        tx.transform(controlPoints[i], controlPoints[i]);
      }      
    }    
  }

  @Override
  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      double leadSpacing = this.leadSpacing.convertToPixels();
      double windingSpacing = this.windingSpacing.convertToPixels();
      double centerX = (int) (controlPoints[0].getX() + windingSpacing / 2);
      double centerY = (int) (controlPoints[0].getY() + leadSpacing);
      int coreWidth = getClosestOdd(this.coreWidth.convertToPixels());
      int coreThickness = getClosestOdd(this.coreThickness.convertToPixels());
      int coilWidth = getClosestOdd(this.coilWidth.convertToPixels());
      int coilLength = getClosestOdd(this.coilLength.convertToPixels());
      
      body[0] = new Area(new Rectangle2D.Double(centerX - coreThickness / 2, centerY - coreWidth / 2, coreThickness, coreWidth));
      body[1] = new Area(new RoundRectangle2D.Double(centerX - coilLength / 2, centerY - coilWidth / 2, coilLength, coilWidth, coilWidth / 3, coilWidth / 3));
      body[1].subtract(body[0]);
      
      AffineTransform tx = getTx();
      if (tx != null)
        for (Area b : body) {
          if (b != null)
            b.transform(tx);
        }
    }
    return body;
  }
  
  @SuppressWarnings("incomplete-switch")
  private AffineTransform getTx() {
    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();
    if (orientation == Orientation.DEFAULT)
      return null;
    
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
    AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
    
    return rotation;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Area[] body = getBody();
    Area coreArea = body[0];
    Area coilArea = body[1];
        
    if (!outlineMode) {
      int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
      for (Point2D point : controlPoints) {
        g2d.setColor(PIN_COLOR);
        g2d.fillOval((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
        g2d.setColor(PIN_BORDER_COLOR);
        g2d.drawOval((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
      }
    }
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    
    // render coil
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getCoilColor());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.fill(coilArea);
    Color finalBorderColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor = theme.getOutlineColor();
    } else {
      finalBorderColor = getCoilBorderColor();
    }
    g2d.setColor(finalBorderColor);
    g2d.draw(coilArea);
    
    // render core
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getCoreColor());
    g2d.fill(coreArea);
    g2d.setComposite(oldComposite);

    if (!outlineMode)
      finalBorderColor = getCoreBorderColor();
    
    g2d.setColor(finalBorderColor);   
    g2d.draw(coreArea);
    
    drawingObserver.stopTracking();   

    g2d.setFont(project.getFont());
    
    // Override font size
    if (getFontSizeOverride() != null)
      g2d.setFont(g2d.getFont().deriveFont(1f * getFontSizeOverride()));
    
    // Draw winding designations
    Point2D wPoint = new Point2D.Double((int) (controlPoints[0].getX() + project.getFontSize()), (int) (controlPoints[0].getY() + leadSpacing.convertToPixels()));
    AffineTransform tx = getTx();
    if (tx != null)
      tx.transform(wPoint, wPoint);
    StringUtils.drawCenteredText(g2d, "P", wPoint.getX(), wPoint.getY(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    wPoint = new Point2D.Double((int) (controlPoints[0].getX() + windingSpacing.convertToPixels() - project.getFontSize()), (int) (controlPoints[0].getY() + leadSpacing.convertToPixels()));
    if (tx != null)
      tx.transform(wPoint, wPoint);
    StringUtils.drawCenteredText(g2d, "S", wPoint.getX(), wPoint.getY(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

    // Draw label.
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
        Rectangle bounds = coreArea.getBounds();
        double x = bounds.getX() + (bounds.width - textWidth) / 2;
        double y = bounds.getY() + (bounds.height - textHeight) / 2 + fontMetrics.getAscent();

        AffineTransform oldTransform = g2d.getTransform();

        if (getOrientation() == Orientation.DEFAULT || getOrientation() == Orientation._180) {
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

        g2d.drawString(l, (int)x, (int)y);

        g2d.setTransform(oldTransform);
      }
    }

    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int radius = (int) (12f * width / 32);
    g2d.setColor(COIL_COLOR);
    g2d.fillRoundRect(1, (int) (height / 8f), width - 2, (int) (height * 6 / 8f), radius, radius);
    g2d.setColor(COIL_BORDER_COLOR);
    g2d.drawRoundRect(1, (int) (height / 8f), width - 2, (int) (height * 6 / 8f), radius, radius);
    
    g2d.setColor(CORE_COLOR);
    g2d.fillRect(width * 3 / 8, 1, width / 4, height - 2);
    g2d.setColor(CORE_BORDER_COLOR);
    g2d.drawRect(width * 3 / 8, 1, width / 4, height - 2);
    
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
    
    int pinSize = (int) (2f * width / 32);
    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < 3; i++) {
      g2d.fillOval(width / 5 - pinSize + 1, (height / 6) * (i + 2), pinSize, pinSize);
      g2d.fillOval(4 * width / 5 + 1, (height / 6) * (i + 2), pinSize, pinSize);
    }
  }

  @EditableProperty(name = "Core")
  public Color getCoreColor() {
    if (coreColor == null) {
      coreColor = CORE_COLOR;
    }
    return coreColor;
  }

  public void setCoreColor(Color coreColor) {
    this.coreColor = coreColor;
  }

  @EditableProperty(name = "Core Border")
  public Color getCoreBorderColor() {
    if (coreBorderColor == null) {
      coreBorderColor = CORE_BORDER_COLOR;
    }
    return coreBorderColor;
  }

  public void setCoreBorderColor(Color coreBorderColor) {
    this.coreBorderColor = coreBorderColor;
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
    
  @EditableProperty(name = "Core Thickness")
  public Size getCoreThickness() {
    return coreThickness;
  }

  public void setCoreThickness(Size coreThickness) {
    this.coreThickness = coreThickness;
    body = null;
  }

  @EditableProperty(name = "Core Width")
  public Size getCoreWidth() {
    return coreWidth;
  }

  public void setCoreWidth(Size coreWidth) {
    this.coreWidth = coreWidth;
    body = null;
  }

  @EditableProperty(name = "Coil Width")
  public Size getCoilWidth() {
    return coilWidth;
  }

  public void setCoilWidth(Size coilWidth) {
    this.coilWidth = coilWidth;
    body = null;
  }

  @EditableProperty(name = "Coil Length")
  public Size getCoilLength() {
    return coilLength;
  }

  public void setCoilLength(Size coilLength) {
    this.coilLength = coilLength;
    body = null;
  }
  
  @EditableProperty(name = "Coil")
  public Color getCoilColor() {
    return coilColor;
  }

  public void setCoilColor(Color coilColor) {
    this.coilColor = coilColor;
  }

  @EditableProperty(name = "Coil Border")
  public Color getCoilBorderColor() {
    return coilBorderColor;
  }

  public void setCoilBorderColor(Color coilBorderColor) {
    this.coilBorderColor = coilBorderColor;
  }
  
  @EditableProperty(name = "Primary CT")
  public boolean getPrimaryCT() {
    return primaryCT;
  }

  public void setPrimaryCT(boolean primaryCT) {
    this.primaryCT = primaryCT;
    updateControlPoints();
  }

  @EditableProperty(name = "Secondary CT")
  public boolean getSecondaryCT() {
    return secondaryCT;
  }

  public void setSecondaryCT(boolean secondaryCT) {
    this.secondaryCT = secondaryCT;
    updateControlPoints();
  }  
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
