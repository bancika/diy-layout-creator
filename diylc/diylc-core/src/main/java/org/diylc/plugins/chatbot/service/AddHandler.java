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
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.plugins.chatbot.model.AiEditOperation;

/**
 * Adds a component placed by its position on the grid. Wires, which are placed by the terminals
 * they connect, are added by {@link AddWireHandler}.
 */
class AddHandler implements IAiEditHandler {

  @Override
  public void apply(AiEditOperation op, AiEditContext context) throws Exception {
    ComponentType ct = context.findComponentType(op.getComponentType());
    if (ct == null) {
      context.warn("Cannot add '" + op.getComponentName() + "' (unknown type: "
          + op.getComponentType() + " - consider updating DIYLC).");
      return;
    }

    Project project = context.getProject();
    IDIYComponent<?> comp = context.instantiate(ct, op.getComponentName());

    // Apply properties first so geometry (e.g. pin count, orientation) is computed correctly
    context.getPropertyApplier().apply(comp, op.getProperties());
    context.getPropertyApplier().applyDatasheetModel(comp, op.getDatasheetModel());

    if (ct.getCreationMethod() == CreationMethod.SINGLE_CLICK) {
      if (op.getPosition() != null) {
        WireGeometry.moveTo(comp, op.getPosition().toPixels(project.getGridSpacing()));
      } else {
        context.warn("Component '" + op.getComponentName()
            + "' is missing 'position' for SINGLE_CLICK placement.");
      }
    } else { // POINT_BY_POINT
      if (op.getFromPos() != null && op.getToPos() != null) {
        Point2D from = op.getFromPos().toPixels(project.getGridSpacing());
        Point2D to = op.getToPos().toPixels(project.getGridSpacing());
        comp.stretchBetween(from, to);
      } else {
        context.warn("Component '" + op.getComponentName()
            + "' is missing 'fromPos'/'toPos' for POINT_BY_POINT placement.");
      }
    }

    context.addComponent(comp);
  }
}
