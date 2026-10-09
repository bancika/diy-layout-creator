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
package org.diylc.components;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Template;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.serialization.ProjectFileManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Checks every factory variant shipped in {@code import-defaults/variants.xml} against the class it
 * configures. Nothing else does, and the failure is silent: a template names its properties as
 * plain strings and its enum constants through an XStream package alias, so a renamed property, a
 * renamed constant or a mistyped alias gives a variant that loads and then sets nothing. The user
 * picks it from the palette and gets a default component.
 *
 * <p>This goes through {@link ComponentProcessor#extractProperties} and {@link PropertyWrapper},
 * which is exactly what {@code InstantiationManager} does when it applies a template, so a variant
 * that passes here is one the application can really apply. That matters more than it sounds:
 * matching properties by hand gets autoboxing and inherited accessors subtly wrong, and would
 * report drift that is not there.
 */
public class FactoryVariantsTest {

  @BeforeClass
  public static void setUp() {
    try {
      ConfigurationManager.getInstance().initialize("diylc");
    } catch (Exception ignored) {
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, List<Template>> loadVariants() {
    InputStream in = FactoryVariantsTest.class.getClassLoader()
        .getResourceAsStream("import-defaults/variants.xml");
    Assert.assertNotNull("variants.xml is not on the classpath", in);
    return (Map<String, List<Template>>) ProjectFileManager.xStreamSerializer.fromXML(in);
  }

  /**
   * Problems are collected rather than asserted one at a time, so a run reports every stale
   * template at once instead of stopping at the first.
   */
  @Test
  public void everyVariantNamesLivePropertiesAndApplies() throws Exception {
    List<String> problems = new ArrayList<String>();
    int checked = 0;

    for (Map.Entry<String, List<Template>> entry : loadVariants().entrySet()) {
      Class<?> type;
      try {
        type = Class.forName(entry.getKey());
      } catch (ClassNotFoundException e) {
        problems.add(entry.getKey() + ": no such class");
        continue;
      }

      Map<String, PropertyWrapper> properties = new HashMap<String, PropertyWrapper>();
      for (PropertyWrapper property : ComponentProcessor.getInstance().extractProperties(type)) {
        properties.put(property.getName(), property);
      }

      for (Template variant : entry.getValue()) {
        String where = type.getSimpleName() + " / " + variant.getName();
        if (variant.getValues() == null || variant.getValues().isEmpty()) {
          problems.add(where + ": sets nothing");
          continue;
        }
        checked++;

        for (Map.Entry<String, Object> value : variant.getValues().entrySet()) {
          PropertyWrapper property = properties.get(value.getKey());
          if (property == null) {
            problems.add(where + ": no property named \"" + value.getKey() + "\"");
            continue;
          }
          if (value.getValue() == null) {
            continue;
          }

          IDIYComponent<?> component =
              (IDIYComponent<?>) type.getDeclaredConstructor().newInstance();
          PropertyWrapper applied = (PropertyWrapper) property.clone();
          applied.setValue(value.getValue());
          try {
            applied.writeTo(component);
          } catch (Exception e) {
            problems.add(where + ": \"" + value.getKey() + "\" will not apply -- "
                + e.getClass().getSimpleName() + " writing a "
                + value.getValue().getClass().getSimpleName() + " to a "
                + property.getType().getSimpleName());
            continue;
          }
          applied.readFrom(component);
          if (!value.getValue().equals(applied.getValue())) {
            problems.add(where + ": \"" + value.getKey() + "\" set to " + value.getValue()
                + " but reads back " + applied.getValue());
          }
        }
      }
    }

    Assert.assertTrue("no variants were found at all", checked > 0);
    Assert.assertEquals(problems.size() + " stale factory variant value(s):\n  "
        + String.join("\n  ", problems) + "\n", 0, problems.size());
  }
}
