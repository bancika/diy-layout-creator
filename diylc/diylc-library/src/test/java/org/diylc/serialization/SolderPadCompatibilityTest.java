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
package org.diylc.serialization;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.diylc.components.connectivity.SolderPad;
import org.junit.Assert;
import org.junit.Test;

/**
 * New solder pads are plated, but pads saved before the hole type existed had non-plated holes
 * and must keep them, so that old projects export and net as before.
 */
public class SolderPadCompatibilityTest {

  private static XStream createXStream() {
    XStream xStream = new XStream(new DomDriver("UTF-8"));
    ProjectFileManager.configure(xStream);
    return xStream;
  }

  @Test
  public void newPadIsPlated() {
    Assert.assertEquals(SolderPad.HoleType.PTH, new SolderPad().getHoleType());
  }

  @Test
  public void padSavedWithoutHoleTypeLoadsNonPlated() {
    String xml = createXStream().toXML(new SolderPad());
    Assert.assertTrue(xml.contains("<holeType>PTH</holeType>"));

    SolderPad loaded =
        (SolderPad) createXStream().fromXML(xml.replaceAll("\\s*<holeType>PTH</holeType>", ""));
    Assert.assertEquals(SolderPad.HoleType.NTPH, loaded.getHoleType());
  }

  @Test
  public void savedNonPlatedPadStaysNonPlated() {
    SolderPad pad = new SolderPad();
    pad.setHoleType(SolderPad.HoleType.NTPH);
    SolderPad loaded = (SolderPad) createXStream().fromXML(createXStream().toXML(pad));
    Assert.assertEquals(SolderPad.HoleType.NTPH, loaded.getHoleType());
  }
}
