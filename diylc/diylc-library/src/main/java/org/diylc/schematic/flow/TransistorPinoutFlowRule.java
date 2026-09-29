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
import org.diylc.components.semiconductors.AbstractTransistorPackage;
import org.diylc.components.semiconductors.TransistorPinout;
import org.diylc.core.IDIYComponent;

/**
 * Reads the direction straight off a transistor's declared pin-out, which already says which leg is
 * which for every package and every leg ordering the user can pick.
 *
 * @author Branislav Stojkovic
 */
public class TransistorPinoutFlowRule implements ISignalFlowRule {

  private static final String REGULATOR = "REGULATOR";

  @Override
  public FlowHints evaluate(IDIYComponent<?> component, ComponentType type) {
    if (!(component instanceof AbstractTransistorPackage)) {
      return null;
    }
    TransistorPinout pinout = ((AbstractTransistorPackage) component).getPinout();
    if (pinout == null) {
      return null;
    }
    // the letters are in control point order, so the n-th letter describes the n-th leg
    String electrodes = pinout.toPinout();
    boolean regulator = pinout.name().startsWith(REGULATOR);
    FlowHints.Builder builder = FlowHints.builder();
    for (int i = 0; i < electrodes.length(); i++) {
      builder.role(i, roleOf(electrodes.charAt(i), regulator));
    }
    FlowHints hints = builder.build();
    return hints.hasDirection() ? hints : null;
  }

  /**
   * Base, gate and a regulator's input are where signal arrives; collector, drain and a regulator's
   * output are where it leaves. Emitters and sources sit on a rail and carry no direction.
   *
   * <p>
   * The {@code regulator} flag is not cosmetic: <code>G</code> is the gate of a JFET or MOSFET but
   * the ground leg of a regulator, so reading it the same way in both cases would turn every
   * regulator around.
   * </p>
   */
  private static FlowRole roleOf(char electrode, boolean regulator) {
    if (regulator) {
      switch (electrode) {
        case 'I':
          return FlowRole.INPUT;
        case 'O':
          return FlowRole.OUTPUT;
        default:
          return FlowRole.NEUTRAL;
      }
    }
    switch (electrode) {
      case 'B':
      case 'G':
        return FlowRole.INPUT;
      case 'C':
      case 'D':
        return FlowRole.OUTPUT;
      default:
        return FlowRole.NEUTRAL;
    }
  }
}
