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
package org.diylc.plugins.chatbot.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Lives in diylc-library rather than diylc-core on purpose. The generator finds components by
 * scanning the runtime classpath for {@code org.diylc.components}, and those classes ship in this
 * module, so running it from diylc-core produces an empty catalog and proves nothing.
 *
 * @author Branislav Stojkovic
 */
public class ComponentCatalogGeneratorTest {

  private File tempDir;

  @Before
  public void setUp() throws Exception {
    tempDir = Files.createTempDirectory("diylc_catalog_test").toFile();
  }

  @After
  public void tearDown() throws Exception {
    if (tempDir != null && tempDir.exists()) {
      File[] files = tempDir.listFiles();
      if (files != null) {
        for (File file : files) {
          file.delete();
        }
      }
      tempDir.delete();
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void generatorProducesAPopulatedCatalog() throws Exception {
    ComponentCatalogGenerator.main(new String[] {tempDir.getAbsolutePath()});

    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> full =
        mapper.readValue(new File(tempDir, "catalog_full.json"), Map.class);
    Map<String, Object> index =
        mapper.readValue(new File(tempDir, "catalog_index.json"), Map.class);

    List<Map<String, Object>> components = (List<Map<String, Object>>) full.get("components");
    assertFalse("the catalog must not come out empty", components.isEmpty());
    assertFalse(((Map<String, Object>) index.get("categories")).isEmpty());
    assertEquals(full.get("version"), index.get("version"));

    Map<String, Object> resistor = find(components, "passive.Resistor");
    Map<String, Object> pinout = (Map<String, Object>) resistor.get("pinout");
    assertEquals(2, pinout.get("pinCount"));
    assertEquals("sequential", pinout.get("labels"));

    Map<String, Object> toggle = find(components, "electromechanical.MiniToggleSwitch");
    Map<String, Object> togglePinout = (Map<String, Object>) toggle.get("pinout");
    assertEquals(List.of("Type"), togglePinout.get("drivers"));
    List<Map<String, Object>> variants =
        (List<Map<String, Object>>) togglePinout.get("variants");
    assertTrue("every switch type should be described", variants.size() > 1);
    assertTrue("a switch carries its switching table", variants.get(0).containsKey("switching"));
  }

  private static Map<String, Object> find(List<Map<String, Object>> components, String className) {
    return components.stream().filter(c -> className.equals(c.get("className"))).findFirst()
        .orElseThrow(() -> new AssertionError("no catalog entry for " + className));
  }
}
