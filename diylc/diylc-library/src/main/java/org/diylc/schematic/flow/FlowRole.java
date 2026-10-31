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
package org.diylc.schematic.flow;

/**
 * Direction in which signal travels through one control point of a component. Used to orient the
 * generated schematic so that it reads from input on the left to output on the right.
 *
 * @author Branislav Stojkovic
 */
public enum FlowRole {

  /** Signal enters the component here: a base, a gate, a control grid. */
  INPUT,

  /** Signal leaves the component here: a collector, a drain, a plate, a pickup terminal. */
  OUTPUT,

  /**
   * The point carries no direction of its own. Emitters, cathodes, heaters, supply and ground legs
   * and both ends of a passive part all fall here.
   */
  NEUTRAL
}
