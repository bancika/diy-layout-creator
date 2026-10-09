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
import org.diylc.core.IDIYComponent;

/**
 * Works out which control points of a component signal enters and leaves by, so that the schematic
 * generator can lay the circuit out in the direction it actually runs.
 *
 * <p>
 * A rule speaks only about components it recognises and returns {@code null} for everything else,
 * which lets the next rule try. {@link SignalFlowAnalyzer} holds the rules in priority order, most
 * specific first, and takes the first answer that carries a direction.
 * </p>
 *
 * <p>
 * Implementations must be stateless; one instance is created and reused for the life of the
 * application.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public interface ISignalFlowRule {

  /**
   * @param component the physical layout component
   * @param type      the component's type, or null when it could not be resolved
   * @return roles of the component's control points, or null if this rule has nothing to say about
   *         this component
   */
  FlowHints evaluate(IDIYComponent<?> component, ComponentType type);
}
