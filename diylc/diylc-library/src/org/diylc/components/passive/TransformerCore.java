package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
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

@ComponentDescriptor(
    name = "Transformer Core (symbol)",
    author = "Branislav Stojkovic",
    category = "Schematics",
    instanceNamePrefix = "T",
    description = "Transformer core symbol. Use multiple instances together with \"Transformer Coil Symbol\"<br>to draw transformer schematics.",
    stretchable = true, zOrder = IDIYComponent.COMPONENT, rotatable = true, keywordPolicy = KeywordPolicy.SHOW_TAG,
    creationMethod = CreationMethod.POINT_BY_POINT, keywordTag = "Schematic")
public class TransformerCore extends AbstractComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Size SPACING = new Size(0.025d, SizeUnit.in);
  public static Color COLOR = Color.blue;

  private Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, 0)};

  protected Color color = COLOR;

  public TransformerCore() {
    super();
  }

  @Override
  public int getControlPointCount() {
    return 2;
  }

  @Override
  public Point getControlPoint(int index) {
    return this.controlPoints[index];
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.controlPoints[index].setLocation(point);
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

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

    double theta =
        Math.atan2(this.controlPoints[1].y - this.controlPoints[0].y, this.controlPoints[1].x - this.controlPoints[0].x) + Math.PI / 2;
    double spacing = SPACING.convertToPixels();
    // System.out.println(theta);

    g2d.setColor(finalColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    g2d.translate(spacing * Math.cos(theta) / 2, spacing * Math.sin(theta) / 2);
    g2d.drawLine(this.controlPoints[0].x, this.controlPoints[0].y, this.controlPoints[1].x, this.controlPoints[1].y);
    g2d.translate(-spacing * Math.cos(theta), -spacing * Math.sin(theta));
    g2d.drawLine(this.controlPoints[0].x, this.controlPoints[0].y, this.controlPoints[1].x, this.controlPoints[1].y);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(COLOR);

    GeneralPath polyline = new GeneralPath();
    polyline.moveTo(0, height * 7 / 16);
    polyline.lineTo(width, height * 7 / 16);
    polyline.moveTo(0, height * 9 / 16);
    polyline.lineTo(width, height * 9 / 16);

    g2d.draw(polyline);
  }

  @Override
  public Void getValue() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public void setValue(Void value) {
    // TODO Auto-generated method stub

  }
}
