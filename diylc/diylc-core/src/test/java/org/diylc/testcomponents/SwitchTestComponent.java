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
package org.diylc.testcomponents;

import java.awt.geom.Point2D;

import org.diylc.core.ISwitch;

/**
 * Smallest possible {@link ISwitch} implementation, used to verify that a building block
 * containing a switch can still be instantiated as a {@code CompositeComponent} (design decision
 * D6 in {@code docs/plans/composite-building-blocks.md} - the composite itself is not
 * {@code ISwitch}, so the switch places and draws but contributes no switching behavior).
 * <p>
 * Deliberately a public top-level class, not a nested one: {@link
 * org.diylc.components.AbstractComponent#clone()} instantiates the no-arg constructor via plain
 * reflection (no {@code setAccessible}), which fails across packages unless both the class and
 * the constructor are public.
 */
public class SwitchTestComponent extends TwoPointTestComponent implements ISwitch {

  private static final long serialVersionUID = 1L;

  public SwitchTestComponent() {
    this("SW");
  }

  public SwitchTestComponent(String name) {
    super(name, new Point2D.Double(0, 0), new Point2D.Double(10, 0));
  }

  @Override
  public int getPositionCount() {
    return 2;
  }

  @Override
  public String getPositionName(int position) {
    return position == 0 ? "ON" : "OFF";
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    return position == 0;
  }
}
