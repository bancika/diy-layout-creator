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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.diylc.components.electromechanical.FuseHolderPanel;
import org.diylc.components.passive.Resistor;
import org.diylc.core.Project;

public class FuseHolderPanelTests {

  @Test
  public void testFuseJoinsTheLugs() throws NetlistException {
    Netlist netlist = buildNetlist(true);

    assertEquals(1, netlist.getGroups().size());
    assertEquals(4, netlist.getGroups().iterator().next().getNodes().size());
  }

  @Test
  public void testEmptyHolderKeepsTheLugsApart() throws NetlistException {
    Netlist netlist = buildNetlist(false);

    assertEquals(2, netlist.getGroups().size());
    for (Group group : netlist.getGroups()) {
      assertTrue(group.getNodes().stream().anyMatch(n -> n.getComponent() instanceof FuseHolderPanel));
    }
  }

  private Netlist buildNetlist(boolean hasFuse) throws NetlistException {
    FuseHolderPanel holder = new FuseHolderPanel();
    holder.setName("FH1");
    holder.setHasFuse(hasFuse);
    Resistor r1 = new Resistor();
    r1.setName("R1");
    r1.setControlPoint(holder.getControlPoint(0), 0);
    r1.setControlPoint(new Point2D.Double(1000, 1000), 1);
    Resistor r2 = new Resistor();
    r2.setName("R2");
    r2.setControlPoint(holder.getControlPoint(1), 0);
    r2.setControlPoint(new Point2D.Double(2000, 2000), 1);

    Project project = new Project();
    project.getComponents().add(holder);
    project.getComponents().add(r1);
    project.getComponents().add(r2);

    List<Netlist> netlists = NetlistBuilder.extractNetlists(false, project, new ArrayList<>());
    assertEquals(1, netlists.size());
    return netlists.get(0);
  }
}
