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
package org.diylc.components.schematic;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.diylc.awt.StringUtils;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

/**
 * Generic rectangular IC-style schematic symbol with configurable terminals on each of the four
 * sides, similar to KiCad's generic symbol. Used as the default fallback representation for physical
 * components that do not declare a dedicated schematic factory.
 *
 * @author Branislav Stojkovic
 */
@ComponentDescriptor(name = "Box", author = "DIYLC", category = "Schematic Symbols",
    instanceNamePrefix = "U", description = "Generic schematic box with configurable terminals",
    zOrder = IDIYComponent.COMPONENT, bomPolicy = BomPolicy.SHOW_ALL_NAMES,
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "Schematic")
public class SchematicBox extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
  public static Color BODY_COLOR = Color.white;
  public static Color BORDER_COLOR = Color.black;
  public static Color PIN_COLOR = Color.black;

  private String leftNodes = "1,2";
  private String rightNodes = "3,4";
  private String topNodes = "";
  private String bottomNodes = "";

  private String value = "";
  private Display display = Display.BOTH;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};

  transient private List<String> nodeLabels;

  public SchematicBox() {
    super();
    updateControlPoints();
  }

  private static List<String> parseNodes(String csv) {
    List<String> result = new ArrayList<String>();
    if (csv == null)
      return result;
    for (String token : csv.split(",")) {
      String trimmed = token.trim();
      if (!trimmed.isEmpty())
        result.add(trimmed);
    }
    return result;
  }

  private int leftCount() {
    return parseNodes(leftNodes).size();
  }

  private int rightCount() {
    return parseNodes(rightNodes).size();
  }

  private int topCount() {
    return parseNodes(topNodes).size();
  }

  private int bottomCount() {
    return parseNodes(bottomNodes).size();
  }

  private double pinSpacing() {
    return PIN_SPACING.convertToPixels();
  }

  private double bodyWidth() {
    double spacing = pinSpacing();
    int horizontal = Math.max(topCount(), bottomCount());
    return Math.max((horizontal + 1) * spacing, 4 * spacing);
  }

  private double bodyHeight() {
    double spacing = pinSpacing();
    int vertical = Math.max(leftCount(), rightCount());
    return Math.max((vertical + 1) * spacing, 2 * spacing);
  }

  /**
   * Recomputes the pin geometry from the anchor (control point 0) and the configured node counts.
   * Ordering: left side (top to bottom), right side (top to bottom), top side (left to right),
   * bottom side (left to right).
   *
   * <p>
   * This is called on construction and whenever the node lists change — <b>not</b> from
   * {@link #setControlPoint}. During a drag the presenter translates every control point by the
   * same delta (because {@link #canPointMoveFreely} is {@code false}), which already keeps the pins
   * on the box edge, so recomputing there would double-apply the delta. When the pin count is
   * unchanged the existing {@link Point2D} objects are mutated in place so references held by the
   * drawing/selection code stay valid.
   * </p>
   */
  private void updateControlPoints() {
    nodeLabels = null;
    double spacing = pinSpacing();
    double leadLength = spacing;
    double firstPinX = controlPoints[0].getX();
    double firstPinY = controlPoints[0].getY();

    double bodyX = firstPinX + leadLength;
    double bodyY = firstPinY - spacing;
    double bodyW = bodyWidth();
    double bodyH = bodyHeight();

    List<Point2D> points = new ArrayList<Point2D>();
    // left
    for (int i = 0; i < leftCount(); i++) {
      points.add(new Point2D.Double(firstPinX, bodyY + spacing * (i + 1)));
    }
    // right
    for (int i = 0; i < rightCount(); i++) {
      points.add(new Point2D.Double(bodyX + bodyW + leadLength, bodyY + spacing * (i + 1)));
    }
    // top
    for (int i = 0; i < topCount(); i++) {
      points.add(new Point2D.Double(bodyX + spacing * (i + 1), bodyY - leadLength));
    }
    // bottom
    for (int i = 0; i < bottomCount(); i++) {
      points.add(new Point2D.Double(bodyX + spacing * (i + 1), bodyY + bodyH + leadLength));
    }
    if (points.isEmpty()) {
      points.add(new Point2D.Double(firstPinX, firstPinY));
    }
    // keep index 0 exactly where it was
    points.set(0, new Point2D.Double(firstPinX, firstPinY));

    if (controlPoints.length == points.size()) {
      for (int i = 0; i < points.size(); i++) {
        controlPoints[i].setLocation(points.get(i));
      }
    } else {
      controlPoints = points.toArray(new Point2D[0]);
    }
  }

  private List<String> getNodeLabels() {
    if (nodeLabels == null) {
      nodeLabels = new ArrayList<String>();
      nodeLabels.addAll(parseNodes(leftNodes));
      nodeLabels.addAll(parseNodes(rightNodes));
      nodeLabels.addAll(parseNodes(topNodes));
      nodeLabels.addAll(parseNodes(bottomNodes));
    }
    return nodeLabels;
  }

  private Rectangle2D getBodyRectangle() {
    double spacing = pinSpacing();
    double leadLength = spacing;
    double bodyX = controlPoints[0].getX() + leadLength;
    double bodyY = controlPoints[0].getY() - spacing;
    return new Rectangle2D.Double(bodyX, bodyY, bodyWidth(), bodyHeight());
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip()))
      return;

    Composite oldComposite = applyAlpha(g2d, componentState);
    Rectangle2D body = getBodyRectangle();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(body);
    g2d.setComposite(oldComposite);

    Color finalBorder =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
            : borderColor;
    g2d.setColor(finalBorder);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(body);

    // pins
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
        ? SELECTION_COLOR
        : PIN_COLOR);
    for (int i = 0; i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      Point2D edge = nearestEdgePoint(p, body);
      g2d.drawLine((int) Math.round(p.getX()), (int) Math.round(p.getY()), (int) Math.round(edge.getX()),
          (int) Math.round(edge.getY()));
    }

    // pin labels
    Font smallFont = project.getFont().deriveFont((float) (project.getFont().getSize() * 0.8));
    g2d.setFont(smallFont);
    List<String> labels = getNodeLabels();
    for (int i = 0; i < controlPoints.length && i < labels.size(); i++) {
      Point2D edge = nearestEdgePoint(controlPoints[i], body);
      double lx = edge.getX();
      double ly = edge.getY();
      // nudge label inward
      if (Math.abs(edge.getX() - body.getMinX()) < 1)
        lx += smallFont.getSize();
      else if (Math.abs(edge.getX() - body.getMaxX()) < 1)
        lx -= smallFont.getSize();
      if (Math.abs(edge.getY() - body.getMinY()) < 1)
        ly += smallFont.getSize() * 0.7;
      else if (Math.abs(edge.getY() - body.getMaxY()) < 1)
        ly -= smallFont.getSize() * 0.7;
      StringUtils.drawCenteredText(g2d, labels.get(i), lx, ly, HorizontalAlignment.CENTER,
          VerticalAlignment.CENTER);
    }

    // body label
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
        ? LABEL_COLOR_SELECTED
        : LABEL_COLOR);
    g2d.setFont(project.getFont());
    String label = getLabelForDisplay();
    if (label != null && !label.isEmpty()) {
      StringUtils.drawCenteredText(g2d, label, body.getCenterX(), body.getCenterY(),
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
  }

  private String getLabelForDisplay() {
    switch (display) {
      case NAME:
        return getName();
      case VALUE:
        return value;
      case NONE:
        return "";
      case BOTH:
      default:
        return (getName() == null ? "" : getName()) + (value == null || value.isEmpty() ? "" : " " + value);
    }
  }

  private static Point2D nearestEdgePoint(Point2D p, Rectangle2D body) {
    double x = p.getX();
    double y = p.getY();
    if (x <= body.getMinX())
      return new Point2D.Double(body.getMinX(), clamp(y, body.getMinY(), body.getMaxY()));
    if (x >= body.getMaxX())
      return new Point2D.Double(body.getMaxX(), clamp(y, body.getMinY(), body.getMaxY()));
    if (y <= body.getMinY())
      return new Point2D.Double(clamp(x, body.getMinX(), body.getMaxX()), body.getMinY());
    return new Point2D.Double(clamp(x, body.getMinX(), body.getMaxX()), body.getMaxY());
  }

  private static double clamp(double v, double min, double max) {
    return Math.max(min, Math.min(max, v));
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 4 * width / 32;
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(margin * 2, margin, width - margin * 4, height - margin * 2);
    g2d.setColor(BORDER_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.drawRect(margin * 2, margin, width - margin * 4, height - margin * 2);
    g2d.drawLine(0, height / 3, margin * 2, height / 3);
    g2d.drawLine(0, height * 2 / 3, margin * 2, height * 2 / 3);
    g2d.drawLine(width - margin * 2, height / 2, width, height / 2);
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
    // Move only the requested point. The presenter drags every control point of the box together
    // (canPointMoveFreely == false), so the pins stay on the edge without a recompute here.
    controlPoints[index].setLocation(point);
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public String getControlPointNodeName(int index) {
    List<String> labels = getNodeLabels();
    return index < labels.size() ? labels.get(index) : Integer.toString(index + 1);
  }

  @EditableProperty(name = "Left Nodes")
  public String getLeftNodes() {
    return leftNodes;
  }

  public void setLeftNodes(String leftNodes) {
    this.leftNodes = leftNodes;
    updateControlPoints();
  }

  @EditableProperty(name = "Right Nodes")
  public String getRightNodes() {
    return rightNodes;
  }

  public void setRightNodes(String rightNodes) {
    this.rightNodes = rightNodes;
    updateControlPoints();
  }

  @EditableProperty(name = "Top Nodes")
  public String getTopNodes() {
    return topNodes;
  }

  public void setTopNodes(String topNodes) {
    this.topNodes = topNodes;
    updateControlPoints();
  }

  @EditableProperty(name = "Bottom Nodes")
  public String getBottomNodes() {
    return bottomNodes;
  }

  public void setBottomNodes(String bottomNodes) {
    this.bottomNodes = bottomNodes;
    updateControlPoints();
  }

  @EditableProperty
  public Display getDisplay() {
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
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

  @EditableProperty
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }
}
