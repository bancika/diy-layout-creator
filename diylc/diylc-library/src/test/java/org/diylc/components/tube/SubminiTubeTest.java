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

import org.diylc.common.Orientation;
import org.diylc.components.tube.SubminiTube.PinArrangement;
import org.diylc.components.tube.SubminiTube.PinCount;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class SubminiTubeTest {

  /** The centre of a standing circular tube is not a lead; lead 1 is the first on the circle. */
  @Test
  public void circularCentreIsNotALead() {
    SubminiTube tube = new SubminiTube();
    tube.setPinArrangement(PinArrangement.Circular);
    tube.setPinCount(PinCount._8);
    Assert.assertNull(tube.getControlPointNodeName(0));
    Assert.assertEquals("1", tube.getControlPointNodeName(1));
    Assert.assertEquals("8", tube.getControlPointNodeName(8));
  }

  /** Standing upright, the top lead of a circular tube rises from the centre. */
  @Test
  public void circularTopLeadIsTheCentre() {
    SubminiTube tube = new SubminiTube();
    tube.setPinArrangement(PinArrangement.Circular);
    tube.setPinCount(PinCount._5);
    tube.setTopLead(true);
    tube.setPinNames("A,B,C,D,E,TOP");
    Assert.assertEquals("TOP", tube.getControlPointNodeName(0));
    Assert.assertEquals("A", tube.getControlPointNodeName(1));
  }

  @Test
  public void inlineLeadsStartAtTheFirstPointAndEndWithTheTopLead() {
    SubminiTube tube = new SubminiTube();
    tube.setPinArrangement(PinArrangement.Inline);
    tube.setPinCount(PinCount._3);
    tube.setTopLead(true);
    tube.setPinNames("K,G,,P");
    Assert.assertEquals(4, tube.getControlPointCount());
    Assert.assertEquals("K", tube.getControlPointNodeName(0));
    // a blank name falls back to the lead number
    Assert.assertEquals("3", tube.getControlPointNodeName(2));
    Assert.assertEquals("P", tube.getControlPointNodeName(3));
  }

  @Test
  public void modelSetsTheTubeAndLeavesTheMounting() {
    SubminiTube tube = new SubminiTube();
    tube.setOrientation(Orientation._90);
    tube.setFolded(true);
    tube.applyModel(new String[] {"Dual Triode", "TEST", "Inline", "yes", "10 mm", "7 mm", "40 mm",
        "1P", "1G", "1K", "F4", "F5", "2K", "2G", "2P", "IS"});

    Assert.assertEquals("TEST", tube.getValue());
    Assert.assertEquals(PinArrangement.Inline, tube.getPinArrangement());
    Assert.assertTrue(tube.getTopLead());
    Assert.assertEquals(PinCount._8, tube.getPinCount());
    Assert.assertEquals(new Size(10d, SizeUnit.mm), tube.getDiameter());
    Assert.assertEquals(new Size(7d, SizeUnit.mm), tube.getThickness());
    Assert.assertEquals(new Size(40d, SizeUnit.mm), tube.getLength());
    Assert.assertEquals(Orientation._90, tube.getOrientation());
    Assert.assertTrue(tube.getFolded());
    Assert.assertEquals("IS", tube.getControlPointNodeName(8));
  }

  /** Files saved before the property existed draw a round bulb, as they always did. */
  @Test
  public void bulbWithoutThicknessIsRound() {
    SubminiTube tube = new SubminiTube();
    Assert.assertEquals(tube.getDiameter(), tube.getThickness());
  }

  /**
   * Seen from above, leads run counter-clockwise from the gap, which faces right by default: lead 1
   * is just above the gap and the last lead just below it.
   */
  @Test
  public void circularLeadsRunCounterClockwiseFromTheGap() {
    SubminiTube tube = new SubminiTube();
    tube.setPinArrangement(PinArrangement.Circular);
    tube.setPinCount(PinCount._8);
    double centreY = tube.getControlPoint(0).getY();
    double centreX = tube.getControlPoint(0).getX();
    Assert.assertTrue(tube.getControlPoint(1).getY() < centreY);
    Assert.assertTrue(tube.getControlPoint(1).getX() > centreX);
    Assert.assertTrue(tube.getControlPoint(8).getY() > centreY);
    Assert.assertTrue(tube.getControlPoint(8).getX() > centreX);
  }
}
