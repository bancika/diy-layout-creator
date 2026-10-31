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
import org.diylc.components.tube.Jumbo4PinTubeSocket;
import org.diylc.components.tube.TubeSocket;
import org.diylc.core.IDIYComponent;

/**
 * Reads the direction off the electrode labels of a tube socket.
 *
 * <p>
 * The labels are the user's own text and default to plain pin numbers on {@link TubeSocket}, so this
 * rule contributes only when someone has actually named the electrodes. When it finds nothing it
 * returns null and lets a later rule try, rather than guessing at a pin-out it cannot see.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public class TubeElectrodeFlowRule implements ISignalFlowRule {

  @Override
  public FlowHints evaluate(IDIYComponent<?> component, ComponentType type) {
    if (!(component instanceof TubeSocket) && !(component instanceof Jumbo4PinTubeSocket)) {
      return null;
    }
    FlowHints.Builder builder = FlowHints.builder();
    for (int i = 0; i < component.getControlPointCount(); i++) {
      builder.role(i, roleOf(component.getControlPointNodeName(i)));
    }
    FlowHints hints = builder.build();
    return hints.hasDirection() ? hints : null;
  }

  /**
   * Only the control grid steers the tube, so G2 and G3 - the screen and suppressor, which sit on a
   * supply rail or at the cathode - are deliberately left neutral.
   */
  private static FlowRole roleOf(String electrode) {
    if (electrode == null) {
      return FlowRole.NEUTRAL;
    }
    String label = electrode.trim().toUpperCase();
    if (label.equals("G") || label.equals("G1") || label.equals("GRID")) {
      return FlowRole.INPUT;
    }
    if (label.equals("P") || label.equals("A") || label.equals("PLATE") || label.equals("ANODE")) {
      return FlowRole.OUTPUT;
    }
    return FlowRole.NEUTRAL;
  }
}
