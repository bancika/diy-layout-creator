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
package org.diylc.components.chassis;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDatasheetSupport;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.AiNote;
import org.diylc.core.annotations.AiNote.Scope;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@AiNote(value = "One Enclosure draws a whole box, so use one per box rather than one per side. "
    + "Also select Drill Hole for the round holes of its jacks, pots, switches and LEDs, and the "
    + "Rectangular, Elliptical or Polygonal Cutout for any other openings", scope = Scope.SELECTION)
@AiNote("Draw each hole and cutout on the panel it goes through, inside that panel's outline")
@AiNote("The two control points are opposite corners of the enclosure's top panel. With 'Height' above zero the sides are unfolded around it as an exploded view, each as tall as 'Height' and set 'Spacing' apart from it: the front against the top panel's upper edge, the back against its lower edge and, when 'Sides' is All, the left and right sides against its left and right edges. The bottom is never drawn. With 'Sides' set to None, or 'Height' at zero, it is a single flat panel of any size, such as one side of a chassis drawn on its own")
@AiNote("The enclosure is not conductive in the netlist: jacks, pots and lugs drawn on it are not joined through it, so chassis ground points must be connected explicitly")
@AiNote("A datasheet model is a Hammond enclosure part number such as 1590BB, 1550B or 1444-32, or a generic size such as 125B, and is stored as 'Value'; it sets the top panel to the enclosure's length and width, upright for pedal-sized die-cast boxes up to 125 mm long and horizontal for bigger boxes and amplifier chassis and 'Height' to its wall height. 125B is a generic size sold by several makers; its Hammond equivalent is 1590N1")
@ComponentDescriptor(name = "Enclosure", author = "Branislav Stojkovic", category = "Hardware",
    instanceNamePrefix = "ENC",
    description = "Top panel of an enclosure or chassis, optionally with its sides unfolded",
    zOrder = IDIYComponent.CHASSIS, bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, autoEdit = false,
    transformer = SimpleComponentTransformer.class, enableDatasheet = true,
    datasheetCreationStepCount = 2, datasheetModelColumn = 1)
public class ChassisPanel extends AbstractTransparentComponent<String>
    implements IDatasheetSupport {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = LIGHT_METAL_COLOR;
  public static Color BORDER_COLOR = LIGHT_METAL_COLOR.darker();
  // a new enclosure is a 125B, the most common pedal enclosure, upright as in its datasheet row
  public static String DEFAULT_MODEL = "125B";
  public static Size DEFAULT_TOP_WIDTH = new Size(66d, SizeUnit.mm);
  public static Size DEFAULT_TOP_LENGTH = new Size(122d, SizeUnit.mm);
  public static Size DEFAULT_WALL_HEIGHT = new Size(36d, SizeUnit.mm);
  public static Size DEFAULT_RADIUS = new Size(0d, SizeUnit.mm);
  public static Size DEFAULT_SPACING = new Size(0d, SizeUnit.mm);

  // the fields below up to borderThickness carry the names they had when this class extended
  // AbstractShape, because old files store them under those names
  private String value = DEFAULT_MODEL;
  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(
      DEFAULT_TOP_WIDTH.convertToPixels(), DEFAULT_TOP_LENGTH.convertToPixels())};
  private Point2D.Double firstPoint = new Point2D.Double();
  private Point2D.Double secondPoint = new Point2D.Double();
  private Color color = COLOR;
  private Color borderColor = BORDER_COLOR;
  // no longer used; kept so that old files that store it still load
  @Deprecated
  private Size borderThickness;
  protected Size edgeRadius = DEFAULT_RADIUS;
  private Size height = DEFAULT_WALL_HEIGHT;
  private Sides sides = Sides.ALL;
  private Size spacing = DEFAULT_SPACING;

  public ChassisPanel() {
    updateCorners();
  }

  public ChassisPanel(String[] model) {
    this();

    applyModel(model);
  }

  // columns: group, model, top panel size along x and along y, wall height. The wall height leaves
  // out the lid, which is the bottom left out of the view. Pedal-sized boxes are listed upright,
  // as pedals use them, and the bigger ones horizontal
  @Override
  public void applyModel(String[] model) {
    setValue(model[1]);
    setHeight(Size.parseSize(model[4]));
    // the first control point stays put and the top panel keeps the direction it is drawn in
    Point2D first = controlPoints[0];
    Point2D second = controlPoints[1];
    double x = Size.parseSize(model[2]).convertToPixels();
    double y = Size.parseSize(model[3]).convertToPixels();
    setControlPoint(new Point2D.Double(first.getX() + (second.getX() < first.getX() ? -x : x),
        first.getY() + (second.getY() < first.getY() ? -y : y)), 1);
  }

  private void updateCorners() {
    firstPoint.setLocation(Math.min(controlPoints[0].getX(), controlPoints[1].getX()),
        Math.min(controlPoints[0].getY(), controlPoints[1].getY()));
    secondPoint.setLocation(Math.max(controlPoints[0].getX(), controlPoints[1].getX()),
        Math.max(controlPoints[0].getY(), controlPoints[1].getY()));
  }

  /**
   * @return the side panels of the exploded view, folded out from the edges of the top panel:
   *         front, back and then, when all sides are shown, left and right. Empty when no sides are
   *         shown or the enclosure has no height.
   */
  Rectangle2D[] getSidePanels() {
    double h = getHeight() == null ? 0 : getHeight().convertToPixels();
    if (h <= 0 || getSides() == Sides.NONE) {
      return new Rectangle2D[0];
    }
    double s = getSpacing() == null ? 0 : getSpacing().convertToPixels();
    double x = firstPoint.getX();
    double y = firstPoint.getY();
    double w = secondPoint.getX() - x;
    double d = secondPoint.getY() - y;
    Rectangle2D front = new Rectangle2D.Double(x, y - s - h, w, h);
    Rectangle2D back = new Rectangle2D.Double(x, y + d + s, w, h);
    if (getSides() == Sides.FRONT_AND_BACK) {
      return new Rectangle2D[] {front, back};
    }
    return new Rectangle2D[] {front, back, new Rectangle2D.Double(x - s - h, y, h, d),
        new Rectangle2D.Double(x + w + s, y, h, d)};
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    int x = (int) Math.round(firstPoint.getX());
    int y = (int) Math.round(firstPoint.getY());
    int width = (int) (secondPoint.getX() - firstPoint.getX());
    int length = (int) (secondPoint.getY() - firstPoint.getY());
    int radius = (int) getEdgeRadius().convertToPixels();
    Rectangle2D[] sidePanels = getSidePanels();
    if (componentState != ComponentState.DRAGGING) {
      Composite oldComposite = applyAlpha(g2d, componentState);
      g2d.setColor(color);
      if (radius == 0) {
        g2d.fillRect(x, y, width, length);
      } else {
        g2d.fillRoundRect(x, y, width, length, radius, radius);
      }
      for (Rectangle2D panel : sidePanels) {
        g2d.fill(panel);
      }
      g2d.setComposite(oldComposite);
      // the panels drawn so far are the whole area of the enclosure
      drawingObserver.stopTracking();
    }
    g2d.setColor(componentState == ComponentState.SELECTED
        || componentState == ComponentState.DRAGGING ? SELECTION_COLOR : borderColor);
    if (radius == 0) {
      g2d.drawRect(x, y, width, length);
    } else {
      g2d.drawRoundRect(x, y, width, length, radius, radius);
    }
    for (Rectangle2D panel : sidePanels) {
      g2d.draw(panel);
    }
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
    updateCorners();
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  @EditableProperty
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty(name = "Color")
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty(name = "Radius")
  public Size getEdgeRadius() {
    if (edgeRadius == null) {
      edgeRadius = DEFAULT_RADIUS;
    }
    return edgeRadius;
  }

  public void setEdgeRadius(Size edgeRadius) {
    this.edgeRadius = edgeRadius;
  }

  @EditableProperty
  public Size getHeight() {
    return height;
  }

  public void setHeight(Size height) {
    this.height = height;
  }

  @EditableProperty
  public Sides getSides() {
    if (sides == null) {
      sides = Sides.ALL;
    }
    return sides;
  }

  public void setSides(Sides sides) {
    this.sides = sides;
  }

  @EditableProperty
  public Size getSpacing() {
    if (spacing == null) {
      spacing = DEFAULT_SPACING;
    }
    return spacing;
  }

  public void setSpacing(Size spacing) {
    this.spacing = spacing;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    int flap = 6 / factor;
    int left = 2 / factor + flap;
    int top = 2 / factor + flap;
    int topWidth = width - 2 * left;
    int topHeight = height - 2 * top;
    int[][] panels = {{left, top, topWidth, topHeight}, {left, top - flap, topWidth, flap},
        {left, top + topHeight, topWidth, flap}, {left - flap, top, flap, topHeight},
        {left + topWidth, top, flap, topHeight}};
    for (int[] panel : panels) {
      g2d.setColor(LIGHT_METAL_COLOR);
      g2d.fillRect(panel[0], panel[1], panel[2], panel[3]);
      g2d.setColor(LIGHT_METAL_COLOR.darker());
      g2d.drawRect(panel[0], panel[1], panel[2], panel[3]);
    }
  }

  public enum Sides {
    ALL("All"), FRONT_AND_BACK("Front and Back"), NONE("None");

    private final String label;

    Sides(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
