package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
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

@ComponentDescriptor(name = "Transformer Coil Symbol", author = "Branislav Stojkovic", category = "Schematics",
    instanceNamePrefix = "L", description = "Transformer coil symbol. Use multiple instances together with \"Transformer Core Symbol\"<br>to draw transformer schematics.", stretchable = false,
    zOrder = IDIYComponent.COMPONENT, rotatable = true, keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Schematic")
public class TransformerCoil extends AbstractComponent<org.diylc.core.measures.Voltage> {

  private static final long serialVersionUID = 1L;

  public static Size TAP_SPACING = new Size(0.2d, SizeUnit.in);
  public static Size LEAD_SPACING = new Size(0.1d, SizeUnit.in);
  public static Color COLOR = Color.blue;
  public static Color LEAD_COLOR = Color.black;

  private int tapCount = 0;
  private Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0),
      new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0)};
  private Orientation orientation = Orientation.DEFAULT;
  private org.diylc.core.measures.Voltage voltage = null;
  protected Color color = COLOR;

  private Shape[] body = null;

  public TransformerCoil() {
    super();
    updateControlPoints();
  }

  private void updateControlPoints() {
    double spacing = TAP_SPACING.convertToPixels();
    int fx = 1;
    int fy = 1;
    switch (this.orientation) {
      case DEFAULT:
        fx = 0;
        break;
      case _90:
        fx = -1;
        fy = 0;
        break;
      case _180:
        fx = 0;
        fy = -1;
        break;
      case _270:
        fy = 0;
        break;
      default:
        break;
    }

    if (this.tapCount == 0) {
      fx *= 2;
      fy *= 2;
    }

    Point refPoint = this.controlPoints[0];
    for (int i = 1; i < controlPoints.length; i++) {
      this.controlPoints[i].setLocation(refPoint.x + i * fx * spacing, refPoint.y + i * fy * spacing);
    }
  }

  @EditableProperty
  @Override
  public org.diylc.core.measures.Voltage getValue() {
    return voltage;
  }

  @Override
  public void setValue(org.diylc.core.measures.Voltage value) {
    this.voltage = value;
  }

  @EditableProperty(name = "Number of Taps", defaultable = true)
  public int getTapCount() {
    return tapCount;
  }

  public void setTapCount(int tapCount) {
    this.tapCount = tapCount;
    updateControlPoints();
    this.body = null;
  }

  @EditableProperty(defaultable = true)
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    this.body = null;
  }

  @Override
  public int getControlPointCount() {
    return this.tapCount + 2;
  }

  @Override
  public Point getControlPoint(int index) {
    return this.controlPoints[index];
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.controlPoints[index].setLocation(point);
    this.body = null;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
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

    Shape[] body = getBody();

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

    g2d.setColor(LEAD_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(body[0]);

    g2d.setColor(finalColor);
//    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    g2d.draw(body[1]);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR);
    g2d.drawLine(width / 8, height / 2, width / 8, height * 6 / 8);
    g2d.drawLine(width * 7 / 8, height / 2, width * 7 / 8, height * 6 / 8);
    g2d.setColor(COLOR);

    GeneralPath polyline = new GeneralPath();
    polyline.moveTo(width / 8, height / 2);
    polyline.curveTo(width / 8, height / 4, width * 3 / 8, height / 4, width * 3 / 8, height / 2);
    polyline.curveTo(width * 3 / 8, height / 4, width * 5 / 8, height / 4, width * 5 / 8, height / 2);
    polyline.curveTo(width * 5 / 8, height / 4, width * 7 / 8, height / 4, width * 7 / 8, height / 2);

    g2d.draw(polyline);
  }

  public Shape[] getBody() {
    if (body == null) {
      double spacing = LEAD_SPACING.convertToPixels();
      int fx = 1;
      int fy = 1;
      switch (this.orientation) {
        case DEFAULT:
          fy = 0;
          break;
        case _90:
          fx = 0;
          break;
        case _180:
          fx = -1;
          fy = 0;
          break;
        case _270:
          fx = 0;
          fy = -1;
          break;
        default:
          break;
      }

      GeneralPath leads = new GeneralPath();
      GeneralPath coil = new GeneralPath();

      int coilsPerTap = 3;

      for (int i = 0; i < tapCount + 2; i++) {
        leads.moveTo(this.controlPoints[i].x, this.controlPoints[i].y);
        leads.lineTo(this.controlPoints[i].x + fx * spacing, this.controlPoints[i].y + fy * spacing);

        int dx = this.controlPoints[i + 1].x - this.controlPoints[i].x;
        int dy = this.controlPoints[i + 1].y - this.controlPoints[i].y;
        // skip the last tap
        if (i < tapCount + 1) {
          double x1 = this.controlPoints[i].x + fx * spacing * 2;
          double y1 = this.controlPoints[i].y + fy * spacing * 2;
          double x2 = this.controlPoints[i + 1].x + fx * spacing * 2;
          double y2 = this.controlPoints[i + 1].y + fy * spacing * 2;
          double dxa = x2 - x1;
          double dya = y2 - y1;

          coil.moveTo(this.controlPoints[i].x + fx * spacing, this.controlPoints[i].y + fy * spacing);

          for (int j = 0; j < coilsPerTap; j++) {
            coil.curveTo(x1 + dxa / coilsPerTap * j, y1 + dya / coilsPerTap * j, x1 + dxa / coilsPerTap * (j + 1), y1
                + dya / coilsPerTap * (j + 1), this.controlPoints[i].x + fx * spacing + dx / coilsPerTap * (j + 1),
                this.controlPoints[i].y + fy * spacing + dy / coilsPerTap * (j + 1));
          }
        }
      }
      body = new Shape[2];

      body[0] = leads;
      body[1] = coil;

    }
    return body;
  }
}
