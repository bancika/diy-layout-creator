/*
 *
 * DIY Layout Creator (DIYLC).
 * Copyright (c) 2009-2025 held jointly by the individual authors.
 *
 * This file is part of DIYLC.
 *
 * DIYLC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DIYLC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.diylc.netlist;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.diylc.testcomponents.OnePointTestComponent;
import org.diylc.testcomponents.SwitchTestComponent;

public class NetlistSortingTest {

  @Test
  public void namesDifferingOnlyInCaseSortTheSameEitherWay() {
    Node lower = new Node(new OnePointTestComponent("1k 2w", "2", new Point2D.Double(0, 0)), 0);
    Node upper = new Node(new OnePointTestComponent("1k 2W", "2", new Point2D.Double(0, 0)), 0);

    Assert.assertEquals(sorted(lower, upper), sorted(upper, lower));
  }

  @Test
  public void sameNamedPartsSortByLocation() {
    Node top = new Node(new OnePointTestComponent("R1", "1", new Point2D.Double(50, 0)), 0);
    Node bottom = new Node(new OnePointTestComponent("R1", "1", new Point2D.Double(0, 10)), 0);

    Assert.assertEquals(Arrays.asList(top, bottom), sorted(bottom, top));
    Assert.assertEquals(Arrays.asList(top, bottom), sorted(top, bottom));
  }

  @Test
  public void sameNamedSwitchesKeepTheirPlaceInASetup() {
    SwitchTestComponent upper = switchAt(0);
    SwitchTestComponent lower = switchAt(10);

    SwitchSetup upperOn = setup(new Position(lower, 1), new Position(upper, 0));
    SwitchSetup lowerOn = setup(new Position(upper, 1), new Position(lower, 0));

    Assert.assertEquals("SW1.ON:SW1.OFF", upperOn.toString());
    Assert.assertEquals("SW1.OFF:SW1.ON", lowerOn.toString());
    Assert.assertEquals(-Integer.signum(lowerOn.compareTo(upperOn)),
        Integer.signum(upperOn.compareTo(lowerOn)));
    Assert.assertNotEquals(0, upperOn.compareTo(lowerOn));
  }

  private static List<Node> sorted(Node... nodes) {
    List<Node> list = new ArrayList<Node>(Arrays.asList(nodes));
    Collections.sort(list);
    return list;
  }

  private static SwitchTestComponent switchAt(double y) {
    SwitchTestComponent component = new SwitchTestComponent("SW1");
    component.setControlPoint(new Point2D.Double(0, y), 0);
    component.setControlPoint(new Point2D.Double(10, y), 1);
    return component;
  }

  private static SwitchSetup setup(Position... positions) {
    return new SwitchSetup(new ArrayList<Position>(Arrays.asList(positions)));
  }
}
