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
package org.diylc.core.annotations;

/**
 * Controls whether an {@link EditableProperty} is treated as something that changes the set of
 * terminals a component exposes, when the AI component catalog is generated.
 * <p>
 * The catalog generator discovers this on its own by setting each property to every value in its
 * domain and watching the control points, so {@link #AUTO} is almost always right. The other two
 * values exist for the cases where that probe draws the wrong conclusion.
 *
 * @author Branislav Stojkovic
 */
public enum PinoutDriver {

  /**
   * Let the generator decide by observation. This is the default and should stay so unless there
   * is a concrete reason otherwise.
   */
  AUTO,

  /**
   * Always treat the property as a driver, even when changing it in isolation leaves the terminals
   * alone. Use this when the property only matters in combination with another one, which the
   * one-at-a-time probe cannot see.
   */
  FORCE,

  /**
   * Never treat the property as a driver. Use this when the property does change the control
   * points but not in a way worth describing, so that the catalog does not grow a variant table
   * nobody needs.
   */
  EXCLUDE
}
