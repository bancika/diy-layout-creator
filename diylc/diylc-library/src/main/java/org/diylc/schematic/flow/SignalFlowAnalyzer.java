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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.diylc.common.ComponentType;
import org.diylc.core.IDIYComponent;
import org.diylc.presenter.ComponentProcessor;

/**
 * Works out which way signal runs through a component by asking each {@link ISignalFlowRule} in turn
 * and taking the first answer that carries a direction.
 *
 * <p>
 * The rules are held in an explicit list rather than discovered by scanning, because their order is
 * their priority: the ones that read real per instance data come before the ones that infer from
 * geometry, and the rule that merely reads pin names comes last. Adding a rule means writing the
 * class and placing it in this list at the specificity it deserves.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public class SignalFlowAnalyzer {

  private static final List<ISignalFlowRule> RULES = Collections.unmodifiableList(Arrays.asList(
      new TransistorPinoutFlowRule(),
      new TubeElectrodeFlowRule(),
      new TubeSymbolFlowRule(),
      new BjtSymbolFlowRule(),
      new PickupFlowRule(),
      new NamedPinFlowRule()));

  /**
   * @param component the physical layout component
   * @return the first directional answer any rule had about the component, or null when none of them
   *         recognised it, which is the normal outcome for passive parts
   */
  @SuppressWarnings("unchecked")
  public FlowHints analyze(IDIYComponent<?> component) {
    if (component == null) {
      return null;
    }
    ComponentType type = ComponentProcessor.getInstance()
        .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass());
    for (ISignalFlowRule rule : RULES) {
      FlowHints hints = rule.evaluate(component, type);
      if (hints != null && hints.hasDirection()) {
        return hints;
      }
    }
    return null;
  }

  /** @return the rules in priority order. */
  public static List<ISignalFlowRule> getRules() {
    return RULES;
  }
}
