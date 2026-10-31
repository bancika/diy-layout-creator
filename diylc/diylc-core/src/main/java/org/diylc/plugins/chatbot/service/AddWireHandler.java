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

import org.diylc.common.ComponentType;
import org.diylc.core.IDIYComponent;
import org.diylc.plugins.chatbot.model.AiEditOperation;

/**
 * Adds a wire or another connector between two terminals, resolved to wherever the components
 * they belong to ended up after the rest of the script.
 */
class AddWireHandler implements IAiEditHandler {

  @Override
  public void apply(AiEditOperation op, AiEditContext context) throws Exception {
    ComponentType ct = context.findComponentType(op.getComponentType());
    if (ct == null) {
      context.warn("Cannot add wire '" + op.getComponentName() + "' (unknown type: "
          + op.getComponentType() + ").");
      return;
    }

    Point2D startPoint = context.resolveTerminal(op.getFromTerminal());
    Point2D endPoint = context.resolveTerminal(op.getToTerminal());
    if (startPoint == null || endPoint == null) {
      context.warn("Cannot add wire '" + op.getComponentName()
          + "' (could not resolve terminal references).");
      return;
    }

    IDIYComponent<?> wire = context.instantiate(ct, op.getComponentName());
    context.getPropertyApplier().apply(wire, op.getProperties());
    // shapes the wire the way it comes out when drawn by hand: a curve, a staircase or a line
    wire.stretchBetween(startPoint, endPoint);
    context.addComponent(wire);
  }
}
