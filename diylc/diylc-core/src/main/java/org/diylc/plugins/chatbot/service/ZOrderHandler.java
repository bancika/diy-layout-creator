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

import java.util.List;

import org.diylc.core.IDIYComponent;
import org.diylc.plugins.chatbot.model.AiEditOperation;

/**
 * Moves a component directly behind or directly in front of a reference component.
 */
class ZOrderHandler implements IAiEditHandler {

  private final boolean inFront;

  ZOrderHandler(boolean inFront) {
    this.inFront = inFront;
  }

  @Override
  public void apply(AiEditOperation op, AiEditContext context) {
    IDIYComponent<?> target = context.findComponentByName(op.getComponentName());
    IDIYComponent<?> ref = context.findComponentByName(op.getReferenceComponent());

    if (target == null) {
      context.warn("Cannot change Z-order: component '" + op.getComponentName()
          + "' not found.");
      return;
    }
    if (ref == null) {
      context.warn("Cannot change Z-order: reference component '"
          + op.getReferenceComponent() + "' not found.");
      return;
    }
    if (target.equals(ref)) {
      return;
    }

    List<IDIYComponent<?>> comps = context.getProject().getComponents();
    if (!comps.contains(target) || !comps.contains(ref)) {
      return;
    }

    comps.remove(target);
    int refIndex = comps.indexOf(ref);
    comps.add(inFront ? refIndex + 1 : refIndex, target);
    context.select(target);
  }
}
