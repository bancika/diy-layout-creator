/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.core;

/**
 * Enumerates control point visibility policies.
 * 
 * @author Branislav Stojkovic
 */
public enum VisibilityPolicy {

  /**
   * Control point should be rendered all the time.
   */
  ALWAYS,
  /**
   * Control point should be rendered only when the component is selected.
   */
  WHEN_SELECTED,
  /**
   * Control point should not be rendered regardless of component selection.
   */
  NEVER;
}
