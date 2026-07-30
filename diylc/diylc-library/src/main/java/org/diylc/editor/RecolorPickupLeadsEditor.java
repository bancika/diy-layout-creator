/*
 *
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 *
 * This file is part of DIYLC.
 *
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.editor;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import org.diylc.common.IProjectEditor;
import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.guitar.AbstractGuitarPickup;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.presenter.DrawingManager;

/**
 * Recolors any {@link HookupWire} already attached to a pickup's terminals to match the lead
 * colour the pickup's <em>currently applied</em> {@code PickupDefinition} specifies for that
 * terminal - the explicit, on-demand counterpart to "Add Flexible Leads" only colouring
 * <em>newly created</em> leads (see {@code FlexibleLeadsEditor}): selecting a different pickup
 * definition never automatically recolours existing wires (per the task), so this is how a user
 * asks for that recolouring to happen after switching to a different manufacturer/model.
 *
 * <p>"Attached" is determined the same proximity-based way every other connection in DIYLC is
 * (there is no separate connection graph): a wire counts as attached to a pickup terminal if one
 * of its own sticky control points sits within {@link DrawingManager#CONTROL_POINT_SIZE} of that
 * terminal's control point, mirroring {@code PickupReplacementService}'s connectivity check.
 *
 * <p>A terminal with no colour in the current definition (or a pickup with no definition applied
 * at all) leaves any wire touching it completely untouched, matching "Add Flexible Leads"'s own
 * fallback rule of never guessing a colour that is not actually specified.
 */
public class RecolorPickupLeadsEditor implements IProjectEditor {

  private final AbstractGuitarPickup pickup;

  public RecolorPickupLeadsEditor(AbstractGuitarPickup pickup) {
    this.pickup = pickup;
  }

  @Override
  public Set<IDIYComponent<?>> edit(Project project, Set<IDIYComponent<?>> selection) {
    Set<IDIYComponent<?>> recoloredWires = new HashSet<>();

    for (int i = 0; i < pickup.getControlPointCount(); i++) {
      if (!pickup.isControlPointSticky(i)) {
        continue;
      }
      Color leadColor = pickup.getDefaultLeadColor(i);
      if (leadColor == null) {
        continue;
      }
      Point2D terminal = pickup.getControlPoint(i);
      for (IDIYComponent<?> component : project.getComponents()) {
        if (!(component instanceof HookupWire wire)) {
          continue;
        }
        if (isAttachedTo(wire, terminal)) {
          wire.setLeadColor(leadColor);
          recoloredWires.add(wire);
        }
      }
    }

    Set<IDIYComponent<?>> newSelection = new HashSet<>();
    newSelection.add(pickup);
    newSelection.addAll(recoloredWires);
    return newSelection;
  }

  private static boolean isAttachedTo(HookupWire wire, Point2D terminal) {
    for (int j = 0; j < wire.getControlPointCount(); j++) {
      if (wire.isControlPointSticky(j) && wire.getControlPoint(j).distance(terminal) < DrawingManager.CONTROL_POINT_SIZE) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String getEditAction() {
    return "Recolor Wires to Pickup Colors";
  }
}
