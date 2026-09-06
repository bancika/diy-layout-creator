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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IContinuity;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

/**
 * Auto-routed Manhattan wire that renders a single connection in the schematic view. The endpoints
 * stick to the pins of the schematic symbols they connect; the intermediate way-points are
 * calculated by the router and are not meant to be edited by the user.
 *
 * @author Branislav Stojkovic
 */
@ComponentDescriptor(name = "Schematic Wire", author = "DIYLC", category = "Schematic Symbols",
    instanceNamePrefix = "W", description = "Auto-routed Manhattan wire for schematic connections",
    zOrder = IDIYComponent.WIRING, bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false)
public class SchematicWire extends AbstractComponent<Void> implements IContinuity {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.black;

  private UUID sourceComponentId;
  private int sourcePinIndex;
  private UUID targetComponentId;
  private int targetPinIndex;

  private List<Point2D> routePoints;
  private Color color = COLOR;
  private int thickness = 1;

  public SchematicWire() {
    super();
    routePoints = new ArrayList<Point2D>();
    routePoints.add(new Point2D.Double(0, 0));
    routePoints.add(new Point2D.Double(0, 0));
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip()))
      return;
    List<Point2D> points = getRoutePoints();
    if (points.size() < 2)
      return;
    // The schematic renders wires on a locked layer (so they cannot be selected or detached), but
    // locked components are painted at reduced alpha. A wire must stay solid, so force an opaque
    // composite for its own drawing and restore it afterwards.
    Composite oldComposite = g2d.getComposite();
    g2d.setComposite(AlphaComposite.SrcOver);
    g2d.setColor(
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
            : color);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(thickness));
    GeneralPath path = new GeneralPath();
    path.moveTo(points.get(0).getX(), points.get(0).getY());
    for (int i = 1; i < points.size(); i++) {
      path.lineTo(points.get(i).getX(), points.get(i).getY());
    }
    g2d.draw(path);
    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.drawLine(2, height / 3, width / 2, height / 3);
    g2d.drawLine(width / 2, height / 3, width / 2, height * 2 / 3);
    g2d.drawLine(width / 2, height * 2 / 3, width - 2, height * 2 / 3);
  }

  public List<Point2D> getRoutePoints() {
    if (routePoints == null)
      routePoints = new ArrayList<Point2D>();
    return routePoints;
  }

  public void setRoutePoints(List<Point2D> routePoints) {
    this.routePoints = routePoints;
  }

  @Override
  public int getControlPointCount() {
    return getRoutePoints().size();
  }

  @Override
  public Point2D getControlPoint(int index) {
    return getRoutePoints().get(index);
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    getRoutePoints().get(index).setLocation(point);
  }

  @Override
  public boolean isControlPointSticky(int index) {
    // only the two endpoints attach to symbol pins
    return index == 0 || index == getRoutePoints().size() - 1;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public String getControlPointNodeName(int index) {
    // wires are continuity, not graph nodes
    return null;
  }

  @Override
  public boolean arePointsConnected(int index1, int index2) {
    return true;
  }

  public UUID getSourceComponentId() {
    return sourceComponentId;
  }

  public void setSourceComponentId(UUID sourceComponentId) {
    this.sourceComponentId = sourceComponentId;
  }

  public int getSourcePinIndex() {
    return sourcePinIndex;
  }

  public void setSourcePinIndex(int sourcePinIndex) {
    this.sourcePinIndex = sourcePinIndex;
  }

  public UUID getTargetComponentId() {
    return targetComponentId;
  }

  public void setTargetComponentId(UUID targetComponentId) {
    this.targetComponentId = targetComponentId;
  }

  public int getTargetPinIndex() {
    return targetPinIndex;
  }

  public void setTargetPinIndex(int targetPinIndex) {
    this.targetPinIndex = targetPinIndex;
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty
  public int getThickness() {
    return thickness;
  }

  public void setThickness(int thickness) {
    this.thickness = thickness;
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  @Override
  public IDIYComponent<Void> clone() throws CloneNotSupportedException {
    SchematicWire clone = (SchematicWire) super.clone();
    List<Point2D> pointsCopy = new ArrayList<Point2D>();
    for (Point2D p : getRoutePoints()) {
      pointsCopy.add(new Point2D.Double(p.getX(), p.getY()));
    }
    clone.routePoints = pointsCopy;
    return clone;
  }
}
