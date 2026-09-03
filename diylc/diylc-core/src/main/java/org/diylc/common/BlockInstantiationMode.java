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
package org.diylc.common;

/**
 * The two ways a saved {@link BuildingBlock} can be placed on the canvas.
 *
 * @author Branislav Stojkovic
 */
public enum BlockInstantiationMode {

  /**
   * The block's components are cloned as individual, independently selectable components and
   * grouped together (the original, and only, behavior before composite mode was introduced).
   */
  GROUP,

  /**
   * The block's components are cloned and wrapped in a single
   * {@code org.diylc.components.composite.CompositeComponent} that behaves as one rigid
   * component with its own name, aggregating the control points of all its children.
   */
  COMPOSITE
}
