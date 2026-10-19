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

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.diylc.components.AbstractLeadedComponent.LabelOrientation;
import org.diylc.components.passive.Resistor;
import org.diylc.components.semiconductors.BridgeRectifier;
import org.junit.Assert;
import org.junit.Test;

/**
 * Files saved before {@code LabelOriantation} was renamed must keep loading. The field is migrated
 * and saved under its new name; the enum keeps its shipped class name on disk, because older builds
 * discard a whole config file that names a class they cannot resolve.
 */
public class ComponentAliasProviderTest {

  private static final String OLD_ENUM_NAME = "diylc.AbstractLeadedComponent_-LabelOriantation";
  private static final String OLD_FULL_ENUM_NAME =
      "org.diylc.components.AbstractLeadedComponent_-LabelOriantation";

  // a fresh instance has not seen any class yet, which is the state it is in while reading
  // config.xml at startup
  private static XStream createXStream() {
    XStream xStream = new XStream(new DomDriver("UTF-8"));
    ProjectFileManager.configure(xStream);
    return xStream;
  }

  // files saved before the rename carry only the misspelled field
  private static String toOldFieldName(String xml) {
    Assert.assertTrue(xml.contains("labelOrientation"));
    return xml.replace("labelOrientation", "labelOriantation");
  }

  @Test
  public void leadedComponentMigratesOldFieldName() {
    Resistor resistor = new Resistor();
    resistor.setLabelOrientation(LabelOrientation.Horizontal);
    String oldXml = toOldFieldName(createXStream().toXML(resistor));

    Resistor loaded = (Resistor) createXStream().fromXML(oldXml);
    Assert.assertEquals(LabelOrientation.Horizontal, loaded.getLabelOrientation());

    String savedXml = createXStream().toXML(loaded);
    Assert.assertTrue(savedXml.contains("<labelOrientation>Horizontal</labelOrientation>"));
    Assert.assertFalse(savedXml.contains("labelOriantation"));
  }

  @Test
  public void bridgeRectifierMigratesOldFieldName() {
    BridgeRectifier rectifier = new BridgeRectifier();
    rectifier.setLabelOrientation(LabelOrientation.Horizontal);
    String oldXml = toOldFieldName(createXStream().toXML(rectifier));

    BridgeRectifier loaded = (BridgeRectifier) createXStream().fromXML(oldXml);
    Assert.assertEquals(LabelOrientation.Horizontal, loaded.getLabelOrientation());

    String savedXml = createXStream().toXML(loaded);
    Assert.assertTrue(savedXml.contains("<labelOrientation>Horizontal</labelOrientation>"));
    Assert.assertFalse(savedXml.contains("labelOriantation"));
  }

  @Test
  public void enumValueHeldAsObjectReadsOldClassName() {
    String xml = "<map><entry><string>Label Orientation</string><" + OLD_ENUM_NAME
        + ">Horizontal</" + OLD_ENUM_NAME + "></entry></map>";

    Map<?, ?> loaded = (Map<?, ?>) createXStream().fromXML(xml);
    Assert.assertEquals(LabelOrientation.Horizontal, loaded.get("Label Orientation"));
  }

  @Test
  public void enumValueHeldAsObjectReadsOldFullClassName() {
    String xml = "<map><entry><string>Label Orientation</string><" + OLD_FULL_ENUM_NAME
        + ">Horizontal</" + OLD_FULL_ENUM_NAME + "></entry></map>";

    Map<?, ?> loaded = (Map<?, ?>) createXStream().fromXML(xml);
    Assert.assertEquals(LabelOrientation.Horizontal, loaded.get("Label Orientation"));
  }

  @Test
  public void enumValueHeldAsObjectWritesOldClassName() {
    Map<String, Object> values = new HashMap<String, Object>();
    values.put("Label Orientation", LabelOrientation.Horizontal);

    String xml = createXStream().toXML(values);
    Assert.assertTrue(xml.contains("<" + OLD_ENUM_NAME + ">Horizontal</" + OLD_ENUM_NAME + ">"));
  }
}
