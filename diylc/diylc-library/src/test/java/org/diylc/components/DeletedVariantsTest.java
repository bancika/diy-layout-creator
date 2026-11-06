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
package org.diylc.components;

import java.awt.geom.Point2D;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.TestConfiguration;
import org.diylc.common.IPlugInPort;
import org.diylc.core.Template;
import org.diylc.presenter.VariantManager;
import org.diylc.serialization.ProjectFileManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Checks that a factory variant marked {@code deletedOn} in {@code import-defaults/variants.xml} is
 * taken out of the user's configuration only when the last import predates the deletion, and is
 * never imported, and that the recorded import date comes from the file rather than the clock.
 */
public class DeletedVariantsTest {

  private static final String RESISTOR = "org.diylc.components.passive.Resistor";

  @BeforeClass
  public static void setUp() {
    TestConfiguration.initialize();
  }

  @SuppressWarnings("unchecked")
  private static Map<String, List<Template>> loadVariants() {
    InputStream in = DeletedVariantsTest.class.getClassLoader()
        .getResourceAsStream("import-defaults/variants.xml");
    Assert.assertNotNull("variants.xml is not on the classpath", in);
    return (Map<String, List<Template>>) ProjectFileManager.xStreamSerializer.fromXML(in);
  }

  private static Template deletedResistor() {
    return loadVariants().get(RESISTOR).stream().filter(t -> t.getDeletedOn() != null)
        .findFirst().get();
  }

  private static InMemoryConfigurationManager configWith(List<Template> userResistors,
      LocalDateTime importDate) {
    Map<String, List<Template>> variantMap = new HashMap<String, List<Template>>();
    variantMap.put(RESISTOR, userResistors);
    InMemoryConfigurationManager config = new InMemoryConfigurationManager();
    config.writeValue(IPlugInPort.TEMPLATES_KEY, variantMap);
    config.writeValue(IPlugInPort.DEFAULT_TEMPLATES_IMPORT_DATE_KEY, importDate);
    return config;
  }

  @SuppressWarnings("unchecked")
  private static List<Template> importInto(InMemoryConfigurationManager config) {
    new VariantManager(config, null).importDefaultVariants();
    return ((Map<String, List<Template>>) config.readObject(IPlugInPort.TEMPLATES_KEY, null))
        .get(RESISTOR);
  }

  private static Template userVariant(String name) {
    List<Point2D> points = new ArrayList<Point2D>();
    points.add(new Point2D.Double(0, 0));
    return new Template(name, new HashMap<String, Object>(), points);
  }

  @Test
  public void deletedVariantsCarryOnlyTheirName() {
    for (Map.Entry<String, List<Template>> entry : loadVariants().entrySet()) {
      for (Template variant : entry.getValue()) {
        if (variant.getDeletedOn() != null) {
          String where = entry.getKey() + " / " + variant.getName();
          Assert.assertNotNull(where, variant.getName());
          Assert.assertNull(where, variant.getValues());
          Assert.assertNull(where, variant.getPoints());
        }
      }
    }
  }

  @Test
  public void deletedWhenTheLastImportWasEarlier() {
    Template deleted = deletedResistor();
    Template own = userVariant("My Resistor");
    List<Template> userResistors = new ArrayList<Template>();
    userResistors.add(userVariant(deleted.getName()));
    userResistors.add(own);

    List<Template> remaining =
        importInto(configWith(userResistors, deleted.getDeletedOn().minusDays(1)));

    Assert.assertEquals(1, remaining.size());
    Assert.assertSame(own, remaining.get(0));
  }

  @Test
  public void recreatedByTheUserAfterTheDeletionIsKept() {
    Template deleted = deletedResistor();
    List<Template> userResistors = new ArrayList<Template>();
    userResistors.add(userVariant(deleted.getName()));

    List<Template> remaining =
        importInto(configWith(userResistors, deleted.getDeletedOn().plusDays(1)));

    Assert.assertEquals(1, remaining.size());
    Assert.assertEquals(deleted.getName(), remaining.get(0).getName());
  }

  @Test
  public void neverImported() {
    List<Template> remaining =
        importInto(configWith(new ArrayList<Template>(), LocalDateTime.MIN));

    Assert.assertTrue(remaining.stream().noneMatch(t -> t.getDeletedOn() != null));
  }

  @Test
  public void importDateIsTheLatestDateInTheFile() {
    LocalDateTime latest = LocalDateTime.MIN;
    for (List<Template> variants : loadVariants().values()) {
      for (Template variant : variants) {
        if (variant.getCreatedOn() != null && variant.getCreatedOn().isAfter(latest)) {
          latest = variant.getCreatedOn();
        }
        if (variant.getDeletedOn() != null && variant.getDeletedOn().isAfter(latest)) {
          latest = variant.getDeletedOn();
        }
      }
    }
    InMemoryConfigurationManager config =
        configWith(new ArrayList<Template>(), LocalDateTime.MIN);

    importInto(config);

    Assert.assertEquals(latest,
        config.readObject(IPlugInPort.DEFAULT_TEMPLATES_IMPORT_DATE_KEY, null));
  }

  @Test
  public void importDateNeverMovesBackwards() {
    LocalDateTime later = LocalDateTime.of(2999, 1, 1, 0, 0);
    InMemoryConfigurationManager config = configWith(new ArrayList<Template>(), later);

    importInto(config);

    Assert.assertEquals(later,
        config.readObject(IPlugInPort.DEFAULT_TEMPLATES_IMPORT_DATE_KEY, null));
  }

  private static class InMemoryConfigurationManager implements IConfigurationManager<Object> {

    private final Map<String, Object> values = new HashMap<String, Object>();

    @Override
    public void addConfigListener(String key, IConfigListener listener) {}

    @Override
    public boolean readBoolean(String key, boolean defaultValue) {
      return (Boolean) values.getOrDefault(key, defaultValue);
    }

    @Override
    public String readString(String key, String defaultValue) {
      return (String) values.getOrDefault(key, defaultValue);
    }

    @Override
    public int readInt(String key, int defaultValue) {
      return (Integer) values.getOrDefault(key, defaultValue);
    }

    @Override
    public float readFloat(String key, float defaultValue) {
      return (Float) values.getOrDefault(key, defaultValue);
    }

    @Override
    public double readDouble(String key, double defaultValue) {
      return (Double) values.getOrDefault(key, defaultValue);
    }

    @Override
    public Object readObject(String key, Object defaultValue) {
      return values.getOrDefault(key, defaultValue);
    }

    @Override
    public void writeValue(String key, Object value) {
      values.put(key, value);
    }

    @Override
    public Object getSerializer() {
      return null;
    }

    @Override
    public void initialize(String appName) {}
  }
}
