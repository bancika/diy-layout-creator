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

import org.diylc.common.ComponentType;
import org.diylc.components.semiconductors.BJTSymbol;
import org.diylc.core.IDIYComponent;

/**
 * Derives the direction of a drawn transistor symbol from its fixed geometry.
 *
 * <p>
 * A {@code BJTSymbol} names its pins {@code Q1.0}, {@code Q1.1} and so on, so there is nothing to
 * read. Its three legs are however always built in the same order: control point 0 is the lone pin on
 * the left, which is the base, and of the two on the right the upper one is the collector.
 * </p>
 *
 * <p>
 * This rule is deliberately restricted to the transistor symbol rather than to every three legged
 * symbol, because a potentiometer shares that geometry while being entirely passive and would be
 * given a direction it does not have.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public class BjtSymbolFlowRule implements ISignalFlowRule {

  @Override
  public FlowHints evaluate(IDIYComponent<?> component, ComponentType type) {
    if (!(component instanceof BJTSymbol) || component.getControlPointCount() < 3) {
      return null;
    }
    return FlowHints.builder().input(0).output(1).neutral(2).build();
  }
}
