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
package org.diylc.components.guitar.pickup;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.diylc.components.guitar.AbstractGuitarPickup;
import org.diylc.components.guitar.AbstractGuitarPickup.Polarity;
import org.diylc.components.guitar.AbstractSingleOrHumbuckerPickup;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.presenter.DrawingManager;

/**
 * Replaces one pickup component with another of a different component class ("cross-type"), as
 * described by the task's "different component type" rules:
 * <ul>
 * <li>preserve name, anchor/location, rotation/orientation, list position (layer/z-order), label
 * styling, alpha, group membership and selection state;</li>
 * <li>preserve connections only through matching <em>semantic</em> terminal identity, never raw
 * control-point index - here, the same per-terminal names
 * ({@code IDIYComponent#getControlPointNodeName}) the existing guitar netlist analyser already
 * uses (e.g. "North Start"), which are available whether or not either pickup has a library
 * definition applied;</li>
 * <li>callers must use {@link #plan} first and, if it reports unmatched connections, obtain
 * explicit user confirmation before calling {@link #replace} - this class performs no
 * confirmation itself, it only computes what would happen and then does it.</li>
 * </ul>
 *
 * <p>Connections in DIYLC are derived purely from control points sharing (almost) the same
 * location - there is no separate connection graph stored anywhere - so "preserving" a
 * connection means placing the new component's corresponding control point at the exact spot the
 * old one occupied, and "detaching" an unmatched one simply means not doing that (the component
 * that was attached there, e.g. a wire, is left completely alone and ends up unconnected, exactly
 * as if the user had deleted the pickup and placed an unrelated one by hand).
 */
public class PickupReplacementService {

  /** One sticky, named control point on the old component that has nothing to match to on the
   * new component and is currently touching another component (i.e. it is connected to
   * something). */
  public record UnmatchedConnection(int controlPointIndex, String terminalName) {
  }

  /**
   * Read-only outcome of analysing a prospective replacement, safe to compute at any time since
   * it never mutates the project.
   */
  public record ReplacementPlan(Class<? extends AbstractGuitarPickup> newComponentClass, boolean sameType,
      List<UnmatchedConnection> unmatchedConnections) {

    /** @return {@code true} if the caller must obtain explicit user confirmation before replacing. */
    public boolean requiresConfirmation() {
      return !sameType && !unmatchedConnections.isEmpty();
    }
  }

  /**
   * Analyses what would happen if {@code newDefinition} were applied to {@code oldComponent},
   * without changing anything. Same-type applications (handled elsewhere, in place) are reported
   * with an empty {@code unmatchedConnections} list and never require confirmation.
   */
  public ReplacementPlan plan(Project project, AbstractGuitarPickup oldComponent, PickupDefinition newDefinition) {
    Class<? extends AbstractGuitarPickup> targetClass = requireComponentClass(newDefinition);

    if (PickupComponentFactory.isSameType(oldComponent, newDefinition.format())) {
      return new ReplacementPlan(targetClass, true, List.of());
    }

    AbstractGuitarPickup preview = instantiate(targetClass);
    seedPolarity(oldComponent, preview);

    List<UnmatchedConnection> unmatched = new ArrayList<>();
    for (int i = 0; i < oldComponent.getControlPointCount(); i++) {
      if (!oldComponent.isControlPointSticky(i)) {
        continue;
      }
      String name = oldComponent.getControlPointNodeName(i);
      if (name == null) {
        continue;
      }
      if (findMatchingControlPointIndex(preview, name) < 0 && isConnectedToSomethingElse(project, oldComponent, i)) {
        unmatched.add(new UnmatchedConnection(i, name));
      }
    }
    return new ReplacementPlan(targetClass, false, unmatched);
  }

  /**
   * Performs the replacement: builds a new component of {@code newDefinition}'s format, gives it
   * the same raw {@link org.diylc.common.Orientation} value the old component had (the same
   * "Orientation" property a user would see and set on any DIYLC component; this is also what its
   * body rendering rotates by, identically for every pickup class), then positions its control
   * points as follows. A terminal that only matches the old component <em>by semantic name</em>
   * (see the class docs) - nothing actually wired to it - keeps its natural position for the new
   * component's own class, translated as one rigid unit so the whole thing sits where it should;
   * this is what makes an unconnected replacement look like the new class's own natural shape
   * rather than inheriting the old class's, and also keeps a terminal with no match at all safely
   * away from an unrelated wire. A terminal that <em>is</em> actually connected to something else,
   * however, is copied to the old component's exact coordinate instead, which is what actually
   * preserves that connection - the two component classes do not necessarily lay their control
   * points out in the same direction (see {@code AbstractGuitarPickup.getControlPointDirection()}),
   * so a single shared translation cannot guarantee every connected terminal lands exactly right
   * at once. Finally swaps the new component into the project at the old component's exact list
   * position.
   *
   * <p>Callers are responsible for having already obtained user confirmation if
   * {@link #plan}{@code .requiresConfirmation()} was {@code true} - this method does not ask.
   *
   * @return the new component, so callers can update the current selection to point to it.
   */
  public AbstractGuitarPickup replace(Project project, AbstractGuitarPickup oldComponent,
      PickupDefinition newDefinition) {
    Class<? extends AbstractGuitarPickup> targetClass = requireComponentClass(newDefinition);
    AbstractGuitarPickup newComponent = instantiate(targetClass);

    seedPolarity(oldComponent, newComponent);

    // Orientation first, on the freshly-instantiated component (control point 0 still at its own
    // class default, e.g. (0, 0) - see AbstractGuitarPickup's controlPoint field), so it is laid
    // out exactly as it would be if placed directly at that orientation.
    //
    // This is a plain copy of the raw value - the same "Orientation" property value a user would
    // see and set via the normal rotate action, on any component type, everywhere else in DIYLC
    // (the body/shape rendering for every pickup class rotates by exactly this many degrees,
    // regardless of class - see each class's getBody()). Earlier this method instead tried to
    // preserve the old component's exact control-point axis via a per-class "native direction"
    // adjustment, but that made the resulting Orientation *property* diverge from what a freshly
    // placed component of the new type would show (e.g. replacing a default-orientation humbucker
    // produced a single coil rotated 90 degrees from a sidebar-placed one) - a real, reported
    // regression. The control-point axis issue that motivated that attempt was actually caused by
    // the anchoring logic below (independently remapping each matched terminal, rather than
    // translating the new component as one rigid unit), which is what genuinely needed fixing, and
    // is fixed below without touching orientation at all.
    newComponent.setOrientation(oldComponent.getOrientation());

    // Because orientation is a raw copy (see above), the old and new components' own control
    // point layouts do not necessarily run in the same screen direction when their classes have
    // different native axes (SingleCoilPickup's is horizontal; every other pickup class's is
    // vertical - see getControlPointDirection()) - e.g. a default-orientation humbucker's leads
    // run vertically while a default-orientation single coil's run horizontally.
    //
    // A terminal that is only matched *by name* (nothing actually wired to it) gets its natural
    // position for the new component's own class: computed from the new component's own fresh,
    // as-if-placed-directly layout, translated by a single fixed (dx, dy) anchored on one
    // reference match, exactly as if the whole component had been placed directly and then
    // shifted into position. This is what makes an unwired replacement look like the new class's
    // own natural shape (e.g. a single coil's two leads end up horizontal, not inheriting the old
    // humbucker's vertical arrangement) - and, just as importantly, keeps any terminal that has no
    // match at all away from an unrelated wire several spacings off in the old component's own
    // (different) direction, a real regression this method once had.
    //
    // A terminal that *is* actually wired to something else, however, must end up at the old
    // component's exact coordinate regardless of direction - the single shared translation above
    // cannot guarantee that for every wired terminal simultaneously when the two components' own
    // directions differ, so each such terminal's coordinate is copied directly instead. This can
    // only ever affect a terminal that is really connected to something, so it never fights the
    // natural-appearance goal for a freshly tried, not-yet-wired replacement - only for terminals
    // where actually preserving the connection has to take priority over pure appearance.
    Point2D[] freshPoints = new Point2D[newComponent.getControlPointCount()];
    for (int i = 0; i < freshPoints.length; i++) {
      freshPoints[i] = (Point2D) newComponent.getControlPoint(i).clone();
    }

    int referenceOldIndex = -1;
    int referenceNewIndex = -1;
    for (int i = 0; i < oldComponent.getControlPointCount(); i++) {
      if (!oldComponent.isControlPointSticky(i) || oldComponent.getControlPointNodeName(i) == null) {
        continue;
      }
      int newIndex = findMatchingControlPointIndex(newComponent, oldComponent.getControlPointNodeName(i));
      if (newIndex >= 0) {
        referenceOldIndex = i;
        referenceNewIndex = newIndex;
        break;
      }
    }
    // No terminal names matched at all (a fully incompatible replacement the caller proceeded
    // with anyway after confirmation): fall back to anchoring via the raw control point 0, same
    // as previous behaviour for that edge case.
    Point2D referenceOldPoint = oldComponent.getControlPoint(referenceOldIndex >= 0 ? referenceOldIndex : 0);
    Point2D referenceNewPoint = freshPoints[referenceNewIndex >= 0 ? referenceNewIndex : 0];
    double dx = referenceOldPoint.getX() - referenceNewPoint.getX();
    double dy = referenceOldPoint.getY() - referenceNewPoint.getY();
    for (int i = 0; i < newComponent.getControlPointCount(); i++) {
      newComponent.setControlPoint(new Point2D.Double(freshPoints[i].getX() + dx, freshPoints[i].getY() + dy), i);
    }

    for (int i = 0; i < oldComponent.getControlPointCount(); i++) {
      if (!oldComponent.isControlPointSticky(i)) {
        continue;
      }
      String name = oldComponent.getControlPointNodeName(i);
      if (name == null) {
        continue;
      }
      int newIndex = findMatchingControlPointIndex(newComponent, name);
      if (newIndex >= 0 && isConnectedToSomethingElse(project, oldComponent, i)) {
        newComponent.setControlPoint((Point2D) oldComponent.getControlPoint(i).clone(), newIndex);
      }
    }

    // name, label styling, alpha and group membership (via the same component id - groups
    // reference members by id, not by object reference).
    newComponent.setId(oldComponent.getId());
    newComponent.setName(oldComponent.getName());
    newComponent.setLabelColor(oldComponent.getLabelColor());
    newComponent.setAlpha(oldComponent.getAlpha());
    newComponent.setFontSizeOverride(oldComponent.getFontSizeOverride());

    // definition id/snapshot/label/pole-piece/polarity, same as a same-type update.
    PickupDefinitionApplier.applyFields(newComponent, newDefinition);

    // list position (layer/z-order) - swap in place rather than remove+add.
    int index = project.getComponents().indexOf(oldComponent);
    project.getComponents().set(index, newComponent);

    // lockedComponents is a Set<IDIYComponent<?>> keyed by identity, not id, so it needs an
    // explicit swap (unlike groupsEx, which is id-based and needed no change above).
    if (project.getLockedComponents().remove(oldComponent)) {
      project.getLockedComponents().add(newComponent);
    }

    return newComponent;
  }

  private static Class<? extends AbstractGuitarPickup> requireComponentClass(PickupDefinition definition) {
    Class<? extends AbstractGuitarPickup> targetClass = PickupComponentFactory.getComponentClass(definition.format());
    if (targetClass == null) {
      throw new IllegalArgumentException("No pickup component class is known for format " + definition.format());
    }
    return targetClass;
  }

  /**
   * When both the old and new components are 2-terminal (non-humbucking) pickups, seed the new
   * one with the old one's polarity before computing/using control-point names - this is what
   * makes same-side single-coil-family swaps (e.g. Single Coil &lt;-&gt; P90, both North) match
   * seamlessly instead of needlessly flagging their leads as unmatched.
   */
  private static void seedPolarity(AbstractGuitarPickup oldComponent, AbstractGuitarPickup newComponent) {
    if (oldComponent instanceof AbstractSingleOrHumbuckerPickup oldTwoOrFour
        && newComponent instanceof AbstractSingleOrHumbuckerPickup newTwoOrFour
        && oldTwoOrFour.getPolarity() != Polarity.Humbucking) {
      newTwoOrFour.setPolarity(oldTwoOrFour.getPolarity());
    }
  }

  private static int findMatchingControlPointIndex(AbstractGuitarPickup component, String name) {
    for (int i = 0; i < component.getControlPointCount(); i++) {
      if (component.isControlPointSticky(i) && name.equals(component.getControlPointNodeName(i))) {
        return i;
      }
    }
    return -1;
  }

  private static boolean isConnectedToSomethingElse(Project project, AbstractGuitarPickup component, int index) {
    Point2D point = component.getControlPoint(index);
    for (IDIYComponent<?> other : project.getComponents()) {
      if (other == component) {
        continue;
      }
      for (int j = 0; j < other.getControlPointCount(); j++) {
        if (other.isControlPointSticky(j) && point.distance(other.getControlPoint(j)) < DrawingManager.CONTROL_POINT_SIZE) {
          return true;
        }
      }
    }
    return false;
  }

  private static AbstractGuitarPickup instantiate(Class<? extends AbstractGuitarPickup> componentClass) {
    try {
      return componentClass.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Could not instantiate pickup component class " + componentClass, e);
    }
  }
}
