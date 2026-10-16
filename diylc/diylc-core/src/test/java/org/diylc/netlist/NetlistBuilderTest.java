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
package org.diylc.netlist;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.diylc.core.IDIYComponent;
import org.diylc.presenter.Connection;
import org.diylc.testcomponents.OnePointTestComponent;

public class NetlistBuilderTest {

  @Test
  public void pointsAcrossACellBoundaryAreConnected() {
    // 522 snaps to 524 and 520 snaps to 520, yet the two are only 2px apart
    Assert.assertEquals(1, buildWire(new Point2D.Double(522, 620)).getGroups().size());
  }

  @Test
  public void pointsFurtherThanEpsAreNotConnected() {
    Assert.assertTrue(buildWire(new Point2D.Double(525, 620)).getGroups().isEmpty());
  }

  private Netlist buildWire(Point2D pinPoint) {
    OnePointTestComponent pin = new OnePointTestComponent("Pin", "1", pinPoint);
    OnePointTestComponent far = new OnePointTestComponent("Far", "1", new Point2D.Double(800, 800));
    List<IDIYComponent<?>> components = Arrays.asList(pin, far);
    List<Node> nodes = Arrays.asList(new Node(pin, 0), new Node(far, 0));
    List<Connection> connections = Arrays.asList(
        new Connection(new Point2D.Double(520, 620), new Point2D.Double(800, 800), 0));
    return NetlistBuilder.buildNetlist(components, nodes, new ArrayList<>(), connections);
  }
}
