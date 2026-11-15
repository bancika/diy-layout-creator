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

import org.diylc.common.PCBLayer;
import org.diylc.components.connectivity.CopperTrace;
import org.junit.Assert;
import org.junit.Test;

/**
 * The inner PCB layers are deprecated but must keep loading from files that use them, with their
 * layer and continuity id unchanged, because XStream resolves enum constants by name.
 */
public class InnerLayerCompatibilityTest {

  private static XStream createXStream() {
    XStream xStream = new XStream(new DomDriver("UTF-8"));
    ProjectFileManager.configure(xStream);
    return xStream;
  }

  @Test
  @SuppressWarnings("deprecation")
  public void innerLayerTraceLoadsUnchanged() {
    CopperTrace trace = new CopperTrace();
    trace.setLayer(PCBLayer._2);
    String xml = createXStream().toXML(trace);
    Assert.assertTrue(xml.contains("<layer>_2</layer>"));

    CopperTrace loaded = (CopperTrace) createXStream().fromXML(xml.replace("<layer>_2</layer>", "<layer>_5</layer>"));
    Assert.assertEquals(PCBLayer._5, loaded.getLayer());
    Assert.assertEquals(5, loaded.getLayerId());
    Assert.assertTrue(createXStream().toXML(loaded).contains("<layer>_5</layer>"));
  }
}
