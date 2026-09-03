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

import java.awt.geom.Point2D;

import org.diylc.common.ComponentType;
import org.diylc.common.IComponentTransformer;
import org.diylc.core.IDIYComponent;
import org.diylc.presenter.ComponentProcessor;

/**
 * Rotates and mirrors a {@link CompositeComponent} by delegating to each child's own
 * {@link IComponentTransformer}, around the same centre for every child so the assembly stays
 * geometrically consistent. If any child's type cannot rotate (or mirror), the whole composite
 * reports that it cannot either - silently leaving one child un-rotated would be worse (see
 * design decision D9 in {@code docs/plans/composite-building-blocks.md}).
 *
 * @author Branislav Stojkovic
 */
public class CompositeComponentTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    CompositeComponent composite = (CompositeComponent) component;
    if (composite.getComponents().isEmpty())
      return false;
    for (IDIYComponent<?> child : composite.getComponents()) {
      if (!childTransformer(child).canRotate(child))
        return false;
    }
    return true;
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    CompositeComponent composite = (CompositeComponent) component;
    if (composite.getComponents().isEmpty())
      return false;
    for (IDIYComponent<?> child : composite.getComponents()) {
      if (!childTransformer(child).canMirror(child))
        return false;
    }
    return true;
  }

  @Override
  public boolean mirroringChangesCircuit() {
    // This transformer instance is shared by every CompositeComponent (ComponentProcessor caches
    // one instance per class), so it has no access to a specific composite's children here.
    // Conservatively assume mirroring can change the circuit, so the user is always warned - a
    // spurious confirmation is far cheaper than silently flipping a board's pinout.
    return true;
  }

  @Override
  public void rotate(IDIYComponent<?> component, Point2D center, int direction) {
    CompositeComponent composite = (CompositeComponent) component;
    for (IDIYComponent<?> child : composite.getComponents()) {
      childTransformer(child).rotate(child, center, direction);
    }
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point2D center, int direction) {
    CompositeComponent composite = (CompositeComponent) component;
    for (IDIYComponent<?> child : composite.getComponents()) {
      childTransformer(child).mirror(child, center, direction);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private IComponentTransformer childTransformer(IDIYComponent<?> child) {
    ComponentType type =
        ComponentProcessor.getInstance().extractComponentTypeFrom((Class) child.getClass());
    return type.getTransformer();
  }
}
