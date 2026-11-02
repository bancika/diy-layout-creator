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
package org.diylc.components.tube;

import java.lang.reflect.Field;

import org.diylc.components.semiconductors.PinLabels;
import org.diylc.components.tube.TubeSocket.Base;
import org.diylc.components.tube.TubeSocket.Mount;
import org.diylc.core.Angle;
import org.junit.Assert;
import org.junit.Test;

public class TubeSocketTest {

  @Test
  public void modelSetsBaseLabelsAndDesignation() {
    TubeSocket socket = new TubeSocket();
    socket.setMount(Mount.PCB);
    socket.setAngle(Angle.of(45));
    socket.applyModel(new String[] {"Power Pentode", "TEST", "OCTAL", "8XX", "NC", "H2", "P",
        "G2", "G1", "NC", "H7", "K"});

    Assert.assertEquals("TEST", socket.getValue());
    Assert.assertEquals(Base.OCTAL, socket.getBase());
    Assert.assertEquals("NC,H2,P,G2,G1,NC,H7,K", socket.getElectrodeLabels());
    Assert.assertEquals(Mount.PCB, socket.getMount());
    Assert.assertEquals(Angle.of(45), socket.getAngle());
    // control point 0 is the socket centre, so pin 1 is control point 1
    Assert.assertEquals("H2", socket.getControlPointNodeName(2));
    Assert.assertEquals("K", socket.getControlPointNodeName(8));
  }

  /** Files saved before the option existed always drew the names, and still do. */
  @Test
  public void socketWithoutTheOptionShowsNames() throws Exception {
    TubeSocket socket = new TubeSocket();
    Field field = TubeSocket.class.getDeclaredField("pinLabels");
    field.setAccessible(true);
    field.set(socket, null);
    Assert.assertEquals(PinLabels.NAMES, socket.getPinLabels());
  }
}
