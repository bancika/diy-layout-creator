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

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.diylc.common.ComponentType;
import org.diylc.common.IProjectEditor;
import org.diylc.components.guitar.AbstractGuitarPickup;
import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.guitar.pickup.PickupComponentFactory;
import org.diylc.components.guitar.pickup.PickupDefinition;
import org.diylc.components.guitar.pickup.PickupDefinitionApplier;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.InstantiationManager;

/**
 * Places a brand new pickup component of a {@link PickupDefinition}'s format directly at a given
 * location - the "right-click the empty canvas (base layer)" counterpart to
 * {@link ApplyPickupDefinitionEditor}, which only ever operates on an already-placed, selected
 * pickup. Run through {@code IPlugInPort#applyEditor(IProjectEditor)} exactly like every other
 * pickup editor, so it participates in undo/redo the same way.
 *
 * <p>The new component is always given the plain, uncovered/open appearance a real, undressed
 * pickup would have on a workbench, regardless of whatever a user last configured manually for
 * that component type (DIYLC otherwise remembers per-type "last used" property values for new
 * placements - deliberately bypassed here so the requested style is guaranteed):
 * <ul>
 * <li>{@link HumbuckerPickup} - {@link HumbuckerPickup.HumbuckerType#PAF}, no cover
 * ("Uncovered PAF");</li>
 * <li>{@link SingleCoilPickup} - {@link SingleCoilPickup.SingleCoilType#Stratocaster} with plain
 * round pole pieces (a regular, uncovered single coil).</li>
 * </ul>
 * Every other pickup format (P90, bass pickups) is left at its own component class's ordinary
 * defaults, since the task only specifies an appearance for these two.
 */
public class PlacePickupFromLibraryEditor implements IProjectEditor {

  private final PickupDefinition definition;
  private final Point2D location;

  public PlacePickupFromLibraryEditor(PickupDefinition definition, Point2D location) {
    this.definition = definition;
    this.location = location;
  }

  @Override
  public Set<IDIYComponent<?>> edit(Project project, Set<IDIYComponent<?>> selection) {
    Class<? extends AbstractGuitarPickup> componentClass = PickupComponentFactory.getComponentClass(definition.format());
    if (componentClass == null) {
      return Collections.emptySet();
    }

    AbstractGuitarPickup pickup;
    try {
      pickup = componentClass.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Could not instantiate pickup component class " + componentClass, e);
    }
    pickup.createdIn(project);

    // Move the whole (freshly-constructed, still at its own class default anchor) component to
    // the requested location as one rigid unit, same technique used by PickupReplacementService.
    Point2D origin = (Point2D) pickup.getControlPoint(0).clone();
    double dx = location.getX() - origin.getX();
    double dy = location.getY() - origin.getY();
    for (int i = 0; i < pickup.getControlPointCount(); i++) {
      Point2D p = pickup.getControlPoint(i);
      pickup.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
    }

    applyUncoveredStyle(pickup);
    PickupDefinitionApplier.applyFields(pickup, definition);

    ComponentType componentType = ComponentProcessor.getInstance().extractComponentTypeFrom(componentClass);
    pickup.setName(new InstantiationManager().createUniqueName(componentType, project.getComponents()));

    project.getComponents().add(pickup);

    Set<IDIYComponent<?>> newSelection = new HashSet<>();
    newSelection.add(pickup);
    return newSelection;
  }

  private static void applyUncoveredStyle(AbstractGuitarPickup pickup) {
    if (pickup instanceof HumbuckerPickup humbucker) {
      humbucker.setType(HumbuckerPickup.HumbuckerType.PAF);
      humbucker.setCover(false);
    } else if (pickup instanceof SingleCoilPickup singleCoil) {
      singleCoil.setType(SingleCoilPickup.SingleCoilType.Stratocaster);
      singleCoil.setPolePieceType(SingleCoilPickup.PolePieceType.Rods);
    }
  }

  @Override
  public String getEditAction() {
    return "Select Pickup from Library";
  }
}
