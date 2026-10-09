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
import org.diylc.components.tube.AbstractTubeSymbol;
import org.diylc.core.IDIYComponent;

/**
 * Derives the direction of a tube symbol from its fixed geometry.
 *
 * <p>
 * Tube symbols name their pins by number, so there is nothing to read; the electrode order is however
 * fixed by the drawing. Both {@code TriodeSymbol} and {@code PentodeSymbol} build their control
 * points with the control grid first - it is the left end of the dashed grid electrode - and the
 * plate second. Everything after that is a cathode, a screen or suppressor grid, or a heater, none of
 * which carries signal direction.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public class TubeSymbolFlowRule implements ISignalFlowRule {

  @Override
  public FlowHints evaluate(IDIYComponent<?> component, ComponentType type) {
    if (!(component instanceof AbstractTubeSymbol) || component.getControlPointCount() < 2) {
      return null;
    }
    return FlowHints.builder().input(0).output(1).build();
  }
}
