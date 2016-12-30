package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
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

@ComponentDescriptor(name = "Open 1/4\" Jack", category = "Electromechanical", author = "Branislav Stojkovic",
    description = "Switchcraft-style open panel mount 1/4\" phono jack, stereo and mono", stretchable = false,
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "J")
public class OpenJack1_4 extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = Color.lightGray;
  private static Color WAFER_COLOR = Color.decode("#CD8500");

  private static Size OUTER_DIAMETER = new Size(0.75d, SizeUnit.in);
  private static Size INNER_DIAMETER = new Size(0.25d, SizeUnit.in);
  private static Size RING_DIAMETER = new Size(0.33d, SizeUnit.in);
  private static Size SPRING_LENGTH = new Size(0.563d, SizeUnit.in);
  private static Size SPRING_WIDTH = new Size(0.12d, SizeUnit.in);
  private static Size HOLE_DIAMETER = new Size(0.05d, SizeUnit.in);
  private static Size HOLE_TO_EDGE = new Size(0.063d, SizeUnit.in);

  private String value = "";
  private Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0)};
  transient Shape[] body;
  private Orientation orientation = Orientation.DEFAULT;
  private JackType type = JackType.MONO;
  private boolean showLabels = true;

  public OpenJack1_4() {
    super();
    updateControlPoints();
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (componentState != ComponentState.DRAGGING) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : WAFER_COLOR);
      g2d.fill(body[0]);

      g2d.setComposite(oldComposite);
    }

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
              : WAFER_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);

    if (componentState != ComponentState.DRAGGING) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
      g2d.fill(body[1]);
      g2d.fill(body[2]);
      if (body[3] != null)
        g2d.fill(body[3]);

      g2d.setComposite(oldComposite);
    }

    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : BASE_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[1]);
    g2d.draw(body[2]);
    if (body[3] != null)
      g2d.draw(body[3]);

    // draw labels
    if (showLabels) {
      g2d.setColor(BASE_COLOR.darker());
      g2d.setFont(LABEL_FONT.deriveFont(LABEL_FONT.getSize2D() * 0.8f));
      int springLength = (int) SPRING_LENGTH.convertToPixels();
      int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();
      int centerY = controlPoints[0].y + springLength - holeToEdge;
      Point tipLabel = new Point(controlPoints[0].x, (int) (controlPoints[0].y + holeToEdge * 1.25));
      AffineTransform ringTransform = AffineTransform.getRotateInstance(Math.PI * 0.795, controlPoints[0].x, centerY);
      AffineTransform sleeveTransform = AffineTransform.getRotateInstance(Math.PI * 0.295, controlPoints[0].x, centerY);
      Point ringLabel = new Point(0, 0);
      Point sleeveLabel = new Point(0, 0);
      ringTransform.transform(tipLabel, ringLabel);
      sleeveTransform.transform(tipLabel, sleeveLabel);
      double theta = 0;
      // Rotate if needed
      if (orientation != Orientation.DEFAULT) {
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
      }

      if (theta != 0) {
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, controlPoints[0].x, controlPoints[0].y);
        rotation.transform(tipLabel, tipLabel);
        rotation.transform(ringLabel, ringLabel);
        rotation.transform(sleeveLabel, sleeveLabel);
      }
      drawCenteredText(g2d, "T", tipLabel.x, tipLabel.y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      drawCenteredText(g2d, "S", sleeveLabel.x, sleeveLabel.y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      if (getType() == JackType.STEREO)
        drawCenteredText(g2d, "R", ringLabel.x, ringLabel.y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
  }

  @SuppressWarnings("incomplete-switch")
  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[4];

      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int outerDiameter = getClosestOdd(OUTER_DIAMETER.convertToPixels());
      int innerDiameter = getClosestOdd(INNER_DIAMETER.convertToPixels());
      int ringDiameter = getClosestOdd(RING_DIAMETER.convertToPixels());
      int springLength = (int) SPRING_LENGTH.convertToPixels();
      int springWidth = (int) SPRING_WIDTH.convertToPixels();
      int holeDiameter = getClosestOdd(HOLE_DIAMETER.convertToPixels());
      int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();

      int centerY = y + springLength - holeToEdge;

      Area wafer =
          new Area(new Ellipse2D.Double(x - outerDiameter / 2, centerY - outerDiameter / 2, outerDiameter,
              outerDiameter));
      wafer.subtract(new Area(new Ellipse2D.Double(x - ringDiameter / 2, centerY - ringDiameter / 2, ringDiameter,
          ringDiameter)));

      body[0] = wafer;

      Area tip =
          new Area(new RoundRectangle2D.Double(x - springWidth / 2, y - holeToEdge, springWidth, springLength
              - ringDiameter / 2, springWidth, springWidth));
      tip.subtract(new Area(
          new Ellipse2D.Double(x - holeDiameter / 2, y - holeDiameter / 2, holeDiameter, holeDiameter)));
      tip.subtract(wafer);

      body[1] = tip;

      Area sleeve =
          new Area(new RoundRectangle2D.Double(x - springWidth / 2, y - holeToEdge, springWidth, springLength,
              springWidth, springWidth));
      sleeve.subtract(new Area(new Ellipse2D.Double(x - holeDiameter / 2, y - holeDiameter / 2, holeDiameter,
          holeDiameter)));
      sleeve.transform(AffineTransform.getRotateInstance(Math.PI * 0.295, x, centerY));
      sleeve.add(new Area(new Ellipse2D.Double(x - ringDiameter / 2, centerY - ringDiameter / 2, ringDiameter,
          ringDiameter)));
      sleeve.subtract(new Area(new Ellipse2D.Double(x - innerDiameter / 2, centerY - innerDiameter / 2, innerDiameter,
          innerDiameter)));

      body[2] = sleeve;

      if (getType() == JackType.STEREO) {
        Area ring =
            new Area(new RoundRectangle2D.Double(x - springWidth / 2, y - holeToEdge, springWidth, springLength,
                springWidth, springWidth));
        ring.subtract(new Area(new Ellipse2D.Double(x - holeDiameter / 2, y - holeDiameter / 2, holeDiameter,
            holeDiameter)));
        ring.transform(AffineTransform.getRotateInstance(Math.PI * 0.795, x, centerY));
        ring.subtract(new Area(new Ellipse2D.Double(x - outerDiameter / 2, centerY - outerDiameter / 2, outerDiameter,
            outerDiameter)));

        body[3] = ring;
      }

      double theta = 0;
      // Rotate if needed
      if (orientation != Orientation.DEFAULT) {
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
      }

      // Rotate if needed
      if (theta != 0) {
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
        // Skip the last one because it's already rotated
        for (int i = 0; i < body.length; i++) {
          Shape shape = body[i];
          Area area = (Area) shape;
          if (area != null) {
            area.transform(rotation);
          }
        }
      }
    }

    return body;
  }

  @SuppressWarnings("incomplete-switch")
  private void updateControlPoints() {
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;

    int springLength = (int) SPRING_LENGTH.convertToPixels();
    int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();

    int centerY = y + springLength - holeToEdge;

    AffineTransform.getRotateInstance(Math.PI * 0.295, x, centerY).transform(controlPoints[0], controlPoints[1]);
    AffineTransform.getRotateInstance(Math.PI * 0.795, x, centerY).transform(controlPoints[0], controlPoints[2]);

    // Rotate if needed
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
      AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
      for (Point point : controlPoints) {
        rotation.transform(point, point);
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int waferDiameter = 15 * width / 32;
    int sleeveDiameter = 9 * width / 32;

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(6f * width / 32));
    g2d.setColor(BASE_COLOR);
    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 4);

    g2d.rotate(Math.PI * 0.795, width / 2, height / 2);

    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 4);

    g2d.setColor(WAFER_COLOR);
    g2d.draw(new Ellipse2D.Double(width / 2 - waferDiameter / 2, height / 2 - waferDiameter / 2, waferDiameter,
        waferDiameter));

    g2d.setColor(BASE_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f * width / 32));
    g2d.draw(new Ellipse2D.Double(width / 2 - sleeveDiameter / 2, height / 2 - sleeveDiameter / 2, sleeveDiameter,
        sleeveDiameter));

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(6f * width / 32));
    g2d.rotate(-Math.PI / 2, width / 2, height / 2);

    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 3);
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
  public boolean isControlPointSticky(int index) {
    return index < 3 || getType() == JackType.STEREO;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.controlPoints[index].setLocation(point);
    // Invalidate the body
    body = null;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
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
    // Invalidate the body
    body = null;
  }

  @EditableProperty
  public JackType getType() {
    return type;
  }

  public void setType(JackType type) {
    this.type = type;
    updateControlPoints();
    // Invalidate the body
    body = null;
  }

  @EditableProperty(name = "Labels")
  public boolean getShowLabels() {
    return showLabels;
  }

  public void setShowLabels(boolean showLabels) {
    this.showLabels = showLabels;
  }
}
