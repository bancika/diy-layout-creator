package org.diylc.components.tube;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

public abstract class AbstractTubeSymbol extends AbstractComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
  public static Color COLOR = Color.black;

  protected String value = "";

  protected Color color = COLOR;
  protected Display display = Display.NAME;
  transient protected Shape[] body;
  protected boolean showHeaters;
  protected Orientation orientation = Orientation.DEFAULT;
  protected Point[] controlPoints;

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Color finalColor;
    if (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING) {
      finalColor = SELECTION_COLOR;
    } else if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalColor = theme.getOutlineColor();
    } else {
      finalColor = color;
    }
    g2d.setColor(finalColor);

    // Draw tube

    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    g2d.draw(body[0]);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(body[1]);

    if (body[2] != null) {
      g2d.draw(body[2]);
    }

    // Draw label
    g2d.setFont(LABEL_FONT);
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
              : LABEL_COLOR;
    }
    g2d.setColor(finalLabelColor);
    
    Rectangle rect = body[2] != null ? body[2].getBounds() : body[1].getBounds();
    
    String label = "";
    label = display == Display.VALUE ? getValue() : getName();
    if (display == Display.NONE) {
      label = "";
    }
    if (display == Display.BOTH) {
      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
    }
    drawCenteredText(g2d, label, rect.x + rect.width, rect.y + rect.height, HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM);
  }
  
  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }
  
  @Override
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    
    refreshDrawing();
  }

  @EditableProperty
  @Override
  public String getValue() {
    return this.value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty
  public Display getDisplay() {
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  @EditableProperty(name = "Heaters")
  public boolean getShowHeaters() {
    return showHeaters;
  }

  public void setShowHeaters(boolean showHeaters) {
    this.showHeaters = showHeaters;

    refreshDrawing();
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;

    refreshDrawing();
  }

  protected void refreshDrawing() {
    updateControlPoints();
    // make sure we have a new drawing
    body = null;
    getBody();

    if (this.orientation == Orientation.DEFAULT)
      return;

    Point first = this.controlPoints[0];
    double angle = Double.parseDouble(this.orientation.name().replace("_", ""));
    AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(angle), first.x, first.y);
    for (int i = 1; i < this.controlPoints.length; i++) {
      rotate.transform(this.controlPoints[i], this.controlPoints[i]);
    }
    if (this.body != null) {
      for (int i = 0; i < this.body.length; i++) {
        this.body[i] = rotate.createTransformedShape(this.body[i]);
      }
    }
  }

  /**
   * Returns transistor shape consisting of 3 parts, in this order: electrodes, connectors, bulb.
   * 
   * @return
   */
  protected abstract Shape[] getBody();

  protected abstract void updateControlPoints();
}
