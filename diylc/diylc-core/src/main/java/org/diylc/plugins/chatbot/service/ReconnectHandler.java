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
import org.diylc.netlist.Node;
import org.diylc.plugins.chatbot.model.AiEditOperation;

/**
 * Moves one end of an existing wire to another terminal, keeping its routing, properties and
 * place in the z-order, which a remove followed by an add would lose.
 */
class ReconnectHandler implements IAiEditHandler {

  @Override
  public void apply(AiEditOperation op, AiEditContext context) {
    IDIYComponent<?> wire = context.findExactComponent(op.getComponentName());
    if (wire == null) {
      context.warn("Cannot reconnect '" + op.getComponentName() + "' (not found).");
      return;
    }
    int index = AiEditContext.parseIndex(op.getTerminal());
    // the exported terminals of a curve are its first and last control points, but "1" is also
    // taken to mean the far end, which on a curve is never a terminal of its own
    if (index == 1 && wire instanceof AbstractCurvedComponent) {
      index = wire.getControlPointCount() - 1;
    }
    if (index < 0 || !AiProjectBuilder.getWireEnds(context.getProject())
        .contains(new Node(wire, index))) {
      context.warn("Cannot reconnect '" + op.getComponentName() + "' (terminal '"
          + op.getTerminal() + "' is not one of its wire ends).");
      return;
    }
    Point2D target = context.resolveTerminal(op.getToTerminal());
    if (target == null) {
      context.warn("Cannot reconnect '" + op.getComponentName()
          + "' (could not resolve terminal reference " + op.getToTerminal() + ").");
      return;
    }
    WireGeometry.moveWireEnd(wire, index, target);
    context.select(wire);
  }
}
