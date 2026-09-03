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
package org.diylc.components.composite;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.diylc.components.AbstractComponent;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.utils.Constants;

/**
 * Instantiation of a {@code BuildingBlock} as a single, rigid component rather than a group of
 * loose ones. Wraps an embedded list of child components and aggregates their control points,
 * so it can be placed, moved, rotated and wired up like any other component while the netlist,
 * BOM and AI project description still see its individual terminals (see
 * {@link #getControlPointNodeName(int)}).
 *
 * @author Branislav Stojkovic
 */
@ComponentDescriptor(
    name = "Building Block",
    description = "A group of components instantiated as a single rigid component",
    category = "Building Blocks",
    author = "Branislav Stojkovic",
    instanceNamePrefix = "BLK",
    zOrder = IDIYComponent.COMPONENT,
    flexibleZOrder = true,
    bomPolicy = BomPolicy.SHOW_ALL_NAMES,
    autoEdit = false,
    enableCache = false,
    transformer = CompositeComponentTransformer.class,
    hiddenInPalette = true)
public class CompositeComponent extends AbstractTransparentComponent<Void> {

  private static final long serialVersionUID = 1L;

  private List<IDIYComponent<?>> childComponents = new ArrayList<IDIYComponent<?>>();

  // Name of the building block this instance was created from. Drives its display identity in
  // the BOM, netlist report and AI project description (see ComponentProcessor.getDisplayTypeName).
  private String blockName;

  // Flat index mapping a composite control point index to (child ordinal, child point index).
  // Rebuilt lazily since children never change after construction and the arrays are not
  // serialized (transient).
  private transient int[] childOf;
  private transient int[] pointOf;

  /**
   * Required no-arg constructor, used reflectively by {@code ComponentProcessor} and XStream.
   */
  public CompositeComponent() {
    super();
  }

  private void buildIndexIfNeeded() {
    if (childOf != null) {
      return;
    }
    int total = 0;
    for (IDIYComponent<?> c : childComponents) {
      total += c.getControlPointCount();
    }
    int[] newChildOf = new int[total];
    int[] newPointOf = new int[total];
    int idx = 0;
    for (int ci = 0; ci < childComponents.size(); ci++) {
      IDIYComponent<?> c = childComponents.get(ci);
      int count = c.getControlPointCount();
      for (int pi = 0; pi < count; pi++) {
        newChildOf[idx] = ci;
        newPointOf[idx] = pi;
        idx++;
      }
    }
    childOf = newChildOf;
    pointOf = newPointOf;
  }

  /**
   * @return the embedded child components, in save order. Never {@code null}.
   */
  public List<IDIYComponent<?>> getChildComponents() {
    if (childComponents == null) {
      childComponents = new ArrayList<IDIYComponent<?>>();
    }
    return childComponents;
  }

  @EditableProperty(name = "Block", defaultable = false, sortOrder = 1)
  public String getBlockName() {
    return blockName;
  }

  /**
   * Sets the source block name. Deliberately not named {@code setBlockName} so that
   * {@code ComponentProcessor}'s getter/setter matching does not turn the "Block" property
   * editable in the property editor - it is metadata about the instance, not a value the user
   * should be able to change after placement.
   */
  public void initBlockName(String blockName) {
    this.blockName = blockName;
  }

  @Override
  public int getControlPointCount() {
    buildIndexIfNeeded();
    return childOf.length;
  }

  @Override
  public Point2D getControlPoint(int index) {
    buildIndexIfNeeded();
    return childComponents.get(childOf[index]).getControlPoint(pointOf[index]);
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    buildIndexIfNeeded();
    childComponents.get(childOf[index]).setControlPoint(point, pointOf[index]);
  }

  @Override
  public boolean isControlPointSticky(int index) {
    buildIndexIfNeeded();
    return childComponents.get(childOf[index]).isControlPointSticky(pointOf[index]);
  }

  @Override
  public boolean canControlPointOverlap(int index) {
    buildIndexIfNeeded();
    return childComponents.get(childOf[index]).canControlPointOverlap(pointOf[index]);
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    buildIndexIfNeeded();
    return childComponents.get(childOf[index]).getControlPointVisibilityPolicy(pointOf[index]);
  }

  @Override
  public String getControlPointNodeName(int index) {
    buildIndexIfNeeded();
    return childComponents.get(childOf[index]).getControlPointNodeName(pointOf[index]);
  }

  /**
   * The composite is always rigid: it moves and rotates as a unit and never stretches. See design
   * decision D4 in {@code docs/plans/composite-building-blocks.md}.
   */
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public void createdIn(Project project) {
    for (IDIYComponent<?> c : getChildComponents()) {
      c.createdIn(project);
    }
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {
    // no-op, composites have no value of their own
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Composite oldComposite = applyAlpha(g2d, componentState);
    for (IDIYComponent<?> c : getChildComponents()) {
      // Children always draw in their normal state - the composite draws its own selection
      // outline below instead of letting every child paint its own highlight.
      c.draw(g2d, ComponentState.NORMAL, outlineMode, project, drawingObserver);
    }
    g2d.setComposite(oldComposite);

    if (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING) {
      Rectangle2D bounds = getControlPointBounds();
      if (bounds != null) {
        // Keep the outline out of the hit-test area, same as guideline drawing elsewhere.
        drawingObserver.stopTracking();
        Color oldColor = g2d.getColor();
        Stroke oldStroke = g2d.getStroke();
        g2d.setColor(SELECTION_COLOR);
        g2d.setStroke(Constants.DASHED_STROKE);
        g2d.draw(bounds);
        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
        drawingObserver.startTracking();
      }
    }
  }

  private Rectangle2D getControlPointBounds() {
    int count = getControlPointCount();
    if (count == 0) {
      return null;
    }
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;
    for (int i = 0; i < count; i++) {
      Point2D p = getControlPoint(i);
      minX = Math.min(minX, p.getX());
      minY = Math.min(minY, p.getY());
      maxX = Math.max(maxX, p.getX());
      maxY = Math.max(maxY, p.getY());
    }
    double margin = 4;
    return new Rectangle2D.Double(minX - margin, minY - margin, maxX - minX + 2 * margin,
        maxY - minY + 2 * margin);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    // Must not touch `components` - ComponentProcessor draws this on a default-constructed,
    // childless instance to build the palette icon.
    g2d.setColor(LIGHT_METAL_COLOR);
    g2d.fillRect(2, 2, width - 4, height - 4);
    g2d.setColor(METAL_COLOR);
    g2d.drawRect(2, 2, width - 4, height - 4);
    int halfW = width / 2;
    int halfH = height / 2;
    g2d.drawLine(halfW, 2, halfW, height - 2);
    g2d.drawLine(2, halfH, width - 2, halfH);
  }

  /**
   * Deep-clones the composite together with all its children, so that two placed instances never
   * share child objects (that would make moving one instance move the other - see the "clone()
   * is the sharp edge" risk in {@code docs/plans/composite-building-blocks.md}). Preserves this
   * component's own id and every child's id, matching {@link AbstractComponent#clone()}'s
   * contract of copying identity by default - callers that need a genuinely new component
   * reassign ids themselves, exactly as {@code BuildingBlockManager.loadBlock} does when it
   * clones each source component individually to place a brand new instance of a block. This also
   * keeps id-sensitive reflective comparisons (like the default {@code equalsTo} most child
   * component classes inherit) working correctly between a composite and its own clone, which the
   * undo dirty-check in {@code Presenter.notifyProjectModifiedIfNeeded} relies on.
   */
  @Override
  public IDIYComponent<Void> clone() throws CloneNotSupportedException {
    CompositeComponent clone = new CompositeComponent();
    clone.setId(getId());
    clone.setName(getName());
    clone.blockName = this.blockName;
    // matches AbstractComponent.clone(), which shallow-copies the Percentage reference
    clone.setAlpha(getAlpha());
    List<IDIYComponent<?>> clonedComponents = new ArrayList<IDIYComponent<?>>(getChildComponents().size());
    for (IDIYComponent<?> c : getChildComponents()) {
      clonedComponents.add(c.clone());
    }
    clone.childComponents = clonedComponents;
    return clone;
  }

  /**
   * Overridden because {@link AbstractComponent#equalsTo(IDIYComponent)} compares the
   * {@code components} field with {@code List.equals()}, which falls back to child object
   * identity since no component class overrides {@code Object.equals()} - two structurally
   * identical composites built from different object graphs (e.g. an original and its clone)
   * would otherwise never compare equal, defeating the undo dirty-check in
   * {@code Presenter.notifyProjectModifiedIfNeeded}.
   */
  @Override
  public boolean equalsTo(IDIYComponent<?> other) {
    if (!(other instanceof CompositeComponent)) {
      return false;
    }
    CompositeComponent o = (CompositeComponent) other;
    if (!Objects.equals(getName(), o.getName()) || !Objects.equals(blockName, o.blockName)
        || !Objects.equals(getAlpha(), o.getAlpha())) {
      return false;
    }
    List<IDIYComponent<?>> otherComponents = o.getChildComponents();
    if (getChildComponents().size() != otherComponents.size()) {
      return false;
    }
    for (int i = 0; i < childComponents.size(); i++) {
      if (!childComponents.get(i).equalsTo(otherComponents.get(i))) {
        return false;
      }
    }
    return true;
  }
}
