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

import org.diylc.components.guitar.AbstractGuitarPickup;
import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.JazzBassPickup;
import org.diylc.components.guitar.PBassPickup;
import org.diylc.components.guitar.P90Pickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.guitar.StingrayBassPickup;

/**
 * Single, centralised place that maps an intrinsic {@link PickupFormat} to the DIYLC component
 * class that represents it, and vice-versa. Per the task's architecture, this is the only place
 * that should know about this mapping - other code (the library dialog, replacement service,
 * etc.) should go through here rather than scattering {@code instanceof}/{@code switch} logic
 * over component classes.
 *
 * <p>Visual sub-variants (Strat/Tele, PAF/Mini/Filtertron, rail/stacked single coils, etc.) are
 * intentionally not part of this mapping - they are represented by each component's existing
 * "type"/pole-piece properties, not by additional component classes or format values.
 */
public final class PickupComponentFactory {

  private PickupComponentFactory() {}

  /**
   * @param component a placed pickup component.
   * @return the intrinsic format that best represents this component's class, or {@code null} if
   *         it does not match any known pickup component class.
   */
  public static PickupFormat getFormat(AbstractGuitarPickup component) {
    if (component == null) {
      return null;
    }
    return getFormat(component.getClass());
  }

  /**
   * @param componentClass a pickup component class.
   * @return the intrinsic format that best represents this class, or {@code null} if it does not
   *         match any known pickup component class.
   */
  public static PickupFormat getFormat(Class<? extends AbstractGuitarPickup> componentClass) {
    if (componentClass == null) {
      return null;
    }
    // Order matters only in that each of these classes is mutually exclusive; a plain equality
    // check (rather than isAssignableFrom) is intentional so that any future subclass of one of
    // these concrete pickup classes doesn't silently inherit an unrelated format.
    if (componentClass == HumbuckerPickup.class) {
      return PickupFormat.HUMBUCKER;
    }
    if (componentClass == SingleCoilPickup.class) {
      return PickupFormat.SINGLE_COIL;
    }
    if (componentClass == P90Pickup.class) {
      return PickupFormat.P90;
    }
    if (componentClass == JazzBassPickup.class) {
      return PickupFormat.JAZZ_BASS;
    }
    if (componentClass == PBassPickup.class) {
      return PickupFormat.PRECISION_BASS;
    }
    if (componentClass == StingrayBassPickup.class) {
      return PickupFormat.STINGRAY_BASS;
    }
    return null;
  }

  /**
   * @param format an intrinsic pickup format.
   * @return the DIYLC component class that represents it, or {@code null} for an unknown/null
   *         format.
   */
  public static Class<? extends AbstractGuitarPickup> getComponentClass(PickupFormat format) {
    if (format == null) {
      return null;
    }
    switch (format) {
      case HUMBUCKER:
        return HumbuckerPickup.class;
      case SINGLE_COIL:
        return SingleCoilPickup.class;
      case P90:
        return P90Pickup.class;
      case JAZZ_BASS:
        return JazzBassPickup.class;
      case PRECISION_BASS:
        return PBassPickup.class;
      case STINGRAY_BASS:
        return StingrayBassPickup.class;
      default:
        return null;
    }
  }

  /**
   * @param component a placed pickup component.
   * @param format an intrinsic pickup format.
   * @return {@code true} if the component's class is the one {@link #getComponentClass(PickupFormat)}
   *         maps the given format to (i.e. applying a definition of this format to this component
   *         would be a same-type, in-place update rather than a component replacement).
   */
  public static boolean isSameType(AbstractGuitarPickup component, PickupFormat format) {
    Class<? extends AbstractGuitarPickup> targetClass = getComponentClass(format);
    return targetClass != null && component != null && targetClass.equals(component.getClass());
  }
}
