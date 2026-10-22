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
package org.diylc.plugins.chatbot.service;

import java.awt.geom.Point2D;

import org.diylc.components.AbstractCurvedComponent;
import org.diylc.core.IDIYComponent;
import org.diylc.presenter.CalcUtils;

/**
 * Moves components and wire ends for the edit script operations.
 *
 * @author Branislav Stojkovic
 */
class WireGeometry {

  /**
   * Moves a component as a whole, so that its first control point lands on the target.
   */
  static void moveTo(IDIYComponent<?> comp, Point2D target) {
    Point2D anchor = comp.getControlPoint(0);
    double dx = target.getX() - anchor.getX();
    double dy = target.getY() - anchor.getY();
    for (int i = 0; i < comp.getControlPointCount(); i++) {
      Point2D p = comp.getControlPoint(i);
      comp.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
    }
  }

  /**
   * Moves one end of a wire and leaves the rest of it alone. On a curve, the handle next to that
   * end moves along with it, so the curve leaves the terminal at the same angle as before.
   */
  static void moveWireEnd(IDIYComponent<?> wire, int index, Point2D target) {
    Point2D end = wire.getControlPoint(index);
    double dx = target.getX() - end.getX();
    double dy = target.getY() - end.getY();
    if (wire instanceof AbstractCurvedComponent) {
      int last = wire.getControlPointCount() - 1;
      int handle = index == 0 ? 1 : last - 1;
      // a three point curve shares its only handle between both ends, so neither end owns it
      if (handle != last - handle && !wire.isControlPointSticky(handle)) {
        Point2D p = wire.getControlPoint(handle);
        wire.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), handle);
      }
    }
    // the end is set last: a smooth curve realigns its other handles around the point that was
    // set last, unless that point is an end
    wire.setControlPoint(new Point2D.Double(target.getX(), target.getY()), index);
  }

  /**
   * @return the terminal of the component that the given control point is stuck to, or -1
   */
  static int findStuckPin(IDIYComponent<?> c, int index, IDIYComponent<?> component) {
    for (int pin = 0; pin < component.getControlPointCount(); pin++) {
      if (CalcUtils.areControlPointsStuck(c, index, component, pin)) {
        return pin;
      }
    }
    return -1;
  }
}
