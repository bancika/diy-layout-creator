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
package org.diylc.presenter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.core.CreationMethod;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.testcomponents.TwoPointTestComponent;
import org.junit.Before;
import org.junit.Test;

/**
 * Covers the single default slot that variants and datasheet models share, see
 * {@code docs/plans/default-datasheet-model.md}.
 */
public class VariantManagerTest {

  private static final String[] NPN = new String[] {"BJT (NPN)", "2N3904", "BJT_EBC"};
  private static final String[] PNP = new String[] {"BJT (PNP)", "2N3906", "BJT_EBC"};

  private MapConfigurationManager configManager;
  private ComponentType type;
  private VariantManager manager;

  @Before
  public void setUp() {
    configManager = new MapConfigurationManager();
    type = createType(new ArrayList<String[]>(Arrays.asList(NPN, PNP)));
    manager = new VariantManager(configManager, null);
  }

  @Test
  public void pinningAModelMakesItTheDefault() {
    manager.setDefaultModel(type, NPN);

    assertTrue(manager.isDefaultModel(type, NPN));
    assertFalse(manager.isDefaultModel(type, PNP));
    assertArrayEquals(NPN, manager.getDefaultModel(type));
    assertEquals("datasheet:BJT (NPN)|2N3904", manager.getDefaultVariant(type));
  }

  @Test
  public void pinningTheDefaultModelAgainClearsIt() {
    manager.setDefaultModel(type, NPN);
    manager.setDefaultModel(type, NPN);

    assertFalse(manager.isDefaultModel(type, NPN));
    assertNull(manager.getDefaultModel(type));
    assertNull(manager.getDefaultVariant(type));
  }

  @Test
  public void pinningAnotherModelReplacesTheDefault() {
    manager.setDefaultModel(type, NPN);
    manager.setDefaultModel(type, PNP);

    assertFalse(manager.isDefaultModel(type, NPN));
    assertArrayEquals(PNP, manager.getDefaultModel(type));
  }

  @Test
  public void modelAndVariantReplaceEachOther() {
    manager.setDefaultVariant(type, "My variant");
    manager.setDefaultModel(type, NPN);

    assertArrayEquals(NPN, manager.getDefaultModel(type));

    manager.setDefaultVariant(type, "My variant");

    assertEquals("My variant", manager.getDefaultVariant(type));
    assertNull(manager.getDefaultModel(type));
  }

  @Test
  public void modelMissingFromTheDatasheetIsNoDefault() {
    manager.setDefaultModel(type, NPN);
    ComponentType reduced = createType(new ArrayList<String[]>(Arrays.<String[]>asList(PNP)));

    assertNull(manager.getDefaultModel(reduced));
  }

  @Test
  public void legacyKeyIsReadAndReplacedOnWrite() {
    String legacyKey = type.getCategory() + "." + type.getName();
    Map<String, String> defaults = new HashMap<String, String>();
    defaults.put(legacyKey, "Old variant");
    configManager.writeValue(IPlugInPort.DEFAULT_TEMPLATES_KEY, defaults);

    assertEquals("Old variant", manager.getDefaultVariant(type));

    manager.setDefaultModel(type, PNP);

    @SuppressWarnings("unchecked")
    Map<String, String> written = (Map<String, String>) configManager
        .readObject(IPlugInPort.DEFAULT_TEMPLATES_KEY, null);
    assertFalse(written.containsKey(legacyKey));
    assertArrayEquals(PNP, manager.getDefaultModel(type));
  }

  private static ComponentType createType(List<String[]> datasheet) {
    return new ComponentType("Test Transistor", "", CreationMethod.SINGLE_CLICK, "Test", "Q", "",
        null, TwoPointTestComponent.class, 0, false, BomPolicy.SHOW_ALL_NAMES, false, null,
        KeywordPolicy.NEVER_SHOW, null, false, datasheet, 2);
  }

  private static class MapConfigurationManager implements IConfigurationManager<Object> {

    private final Map<String, Object> values = new HashMap<String, Object>();

    @Override
    public void addConfigListener(String key, IConfigListener listener) {}

    @Override
    public boolean readBoolean(String key, boolean defaultValue) {
      return (Boolean) readObject(key, defaultValue);
    }

    @Override
    public String readString(String key, String defaultValue) {
      return (String) readObject(key, defaultValue);
    }

    @Override
    public int readInt(String key, int defaultValue) {
      return (Integer) readObject(key, defaultValue);
    }

    @Override
    public float readFloat(String key, float defaultValue) {
      return (Float) readObject(key, defaultValue);
    }

    @Override
    public double readDouble(String key, double defaultValue) {
      return (Double) readObject(key, defaultValue);
    }

    @Override
    public Object readObject(String key, Object defaultValue) {
      return values.containsKey(key) ? values.get(key) : defaultValue;
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
