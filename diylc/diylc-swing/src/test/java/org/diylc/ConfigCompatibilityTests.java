/*
 *
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 *
 * This file is part of DIYLC.
 *
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.thoughtworks.xstream.XStream;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.ComponentType;
import org.diylc.common.Favorite;
import org.diylc.common.Favorite.FavoriteType;
import org.diylc.common.IBlockProcessor;
import org.diylc.common.IPlugInPort;
import org.diylc.common.IVariantProcessor;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Template;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.Presenter;
import org.diylc.serialization.ProjectFileManager;

/**
 * Verifies that configuration files written by older DIYLC releases still load with the current
 * code base. Each file in <code>src/test/resources/configs</code> is a real
 * <code>config.xml</code> produced by the version its name refers to, with personal entries
 * removed.
 * <p>
 * The configuration is an XStream graph that embeds building blocks, variants and component
 * defaults, so it is bound to the component classes just like a <code>.diy</code> file is. XStream
 * is strict about unknown elements here, which means a single renamed or removed field anywhere in
 * a component class makes the whole configuration unreadable. In the running application that
 * failure is silent: {@link ConfigurationManager#initialize(String)} logs the error, backs the file
 * up and starts with an empty configuration, so the user loses every building block, variant and
 * preference at once.
 *
 * @author Branislav Stojkovic
 */
@RunWith(Parameterized.class)
public class ConfigCompatibilityTests {

  private static final String CONFIG_RESOURCE_DIR = "/configs";
  private static final String TEST_APP_NAME = "diylc-config-test";
  private static final String CONFIG_FILE_NAME = "config.xml";

  private static Map<String, ComponentType> typeMap;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private final String fileName;
  private final File configFile;

  public ConfigCompatibilityTests(String fileName, File configFile) {
    this.fileName = fileName;
    this.configFile = configFile;
  }

  @Parameters(name = "{0}")
  public static Collection<Object[]> configFiles() throws URISyntaxException {
    File dir =
        new File(ConfigCompatibilityTests.class.getResource(CONFIG_RESOURCE_DIR).toURI());
    File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".xml"));
    Assert.assertNotNull("No configuration files found in " + CONFIG_RESOURCE_DIR, files);
    Arrays.sort(files);
    List<Object[]> params = new ArrayList<Object[]>();
    for (File file : files)
      params.add(new Object[] {file.getName(), file});
    return params;
  }

  @BeforeClass
  public static void buildTypeMap() {
    // mirrors the lookup that VariantManager builds when it resolves stored variants
    typeMap = new TreeMap<String, ComponentType>(String.CASE_INSENSITIVE_ORDER);
    for (Map.Entry<String, List<ComponentType>> entry : ComponentProcessor.getInstance()
        .getComponentTypes().entrySet())
      for (ComponentType type : entry.getValue()) {
        typeMap.put(type.getInstanceClass().getCanonicalName(), type);
        typeMap.put(type.getCategory() + "." + type.getName(), type);
        if (type.getCategory().contains("Electro-Mechanical"))
          typeMap.put(type.getCategory().replace("Electro-Mechanical", "Electromechanical") + "."
              + type.getName(), type);
      }
  }

  @Test
  public void testDeserializesCompletely() throws IOException {
    Map<String, Object> config = deserialize();

    Assert.assertFalse("Configuration is empty", config.isEmpty());

    Map<String, List<IDIYComponent<?>>> blocks = readMap(config, IBlockProcessor.BLOCKS_KEY);
    if (blocks != null)
      for (Map.Entry<String, List<IDIYComponent<?>>> entry : blocks.entrySet()) {
        Assert.assertNotNull("Building block " + entry.getKey() + " is null", entry.getValue());
        Assert.assertFalse("Building block " + entry.getKey() + " is empty",
            entry.getValue().isEmpty());
        for (IDIYComponent<?> component : entry.getValue()) {
          Assert.assertNotNull("Null component in building block " + entry.getKey(), component);
          for (int i = 0; i < component.getControlPointCount(); i++)
            Assert.assertNotNull("Control point " + i + " of " + component.getClass().getName()
                + " in building block " + entry.getKey() + " did not deserialize",
                component.getControlPoint(i));
        }
      }

    Map<String, List<Template>> variants = readMap(config, IVariantProcessor.TEMPLATES_KEY);
    if (variants != null)
      for (Map.Entry<String, List<Template>> entry : variants.entrySet())
        for (Template variant : entry.getValue()) {
          Assert.assertNotNull("Null variant for " + entry.getKey(), variant);
          Assert.assertNotNull("Variant of " + entry.getKey() + " has no name", variant.getName());
          Assert.assertNotNull("Variant " + variant.getName() + " of " + entry.getKey()
              + " has no values", variant.getValues());
        }
  }

  /**
   * Loads the file the way the application does, through
   * {@link ConfigurationManager#initialize(String)}, which reads
   * <code>&lt;user.home&gt;/&lt;appName&gt;/config.xml</code>.
   */
  @Test
  public void testConfigurationManagerReadsWithoutErrors() throws IOException {
    File configDir = temporaryFolder.newFolder(TEST_APP_NAME);
    Files.copy(configFile.toPath(), new File(configDir, CONFIG_FILE_NAME).toPath(),
        StandardCopyOption.REPLACE_EXISTING);

    String userHome = System.getProperty("user.home");
    System.setProperty("user.home", temporaryFolder.getRoot().getAbsolutePath());
    try {
      // a private instance so that the singleton other tests share is left alone
      ConfigurationManager configurationManager = new ConfigurationManager();
      ProjectFileManager.configure(configurationManager.getSerializer());
      configurationManager.initialize(TEST_APP_NAME);

      Assert.assertFalse(
          "ConfigurationManager discarded " + fileName + ", see the logged error for the cause",
          configurationManager.isFileWithErrors());
      // initialize() also ends up with an empty configuration when it finds no file at all, so
      // make sure it really read this one
      Assert.assertNotNull("ConfigurationManager did not read " + fileName,
          configurationManager.readObject(IBlockProcessor.BLOCKS_KEY, null));
    } finally {
      System.setProperty("user.home", userHome);
    }
  }

  /**
   * Component classes are referenced by name from variants, favorites, recent components and
   * per-component defaults. Those names are plain strings that XStream cannot validate, so a
   * renamed or moved component class silently orphans everything the user stored for it.
   */
  @Test
  public void testComponentReferencesResolve() throws IOException {
    Map<String, Object> config = deserialize();
    List<String> unresolved = new ArrayList<String>();

    Map<String, List<Template>> variants = readMap(config, IVariantProcessor.TEMPLATES_KEY);
    if (variants != null)
      for (String key : variants.keySet())
        if (!typeMap.containsKey(key))
          unresolved.add("variants of " + key);

    Map<String, String> defaultVariants = readMap(config, IVariantProcessor.DEFAULT_TEMPLATES_KEY);
    if (defaultVariants != null)
      for (String key : defaultVariants.keySet())
        if (!typeMap.containsKey(key))
          unresolved.add("default variant of " + key);

    @SuppressWarnings("unchecked")
    List<Favorite> favorites = (List<Favorite>) config.get(IPlugInPort.FAVORITES_KEY);
    if (favorites != null)
      for (Favorite favorite : favorites)
        if (favorite.getType() == FavoriteType.Component
            && !typeMap.containsKey(favorite.getName()))
          unresolved.add("favorite " + favorite.getName());

    @SuppressWarnings("unchecked")
    List<String> recentComponents = (List<String>) config.get(IPlugInPort.RECENT_COMPONENTS_KEY);
    if (recentComponents != null)
      for (String recent : recentComponents)
        if (!recent.startsWith(IBlockProcessor.BLOCK_PREFIX) && !typeMap.containsKey(recent))
          unresolved.add("recent component " + recent);

    for (String key : config.keySet())
      if (key != null && key.startsWith(Presenter.DEFAULTS_KEY_PREFIX)) {
        String className =
            key.substring(Presenter.DEFAULTS_KEY_PREFIX.length(), key.lastIndexOf(':'));
        try {
          Class.forName(className);
        } catch (ClassNotFoundException e) {
          unresolved.add("default value for " + className);
        }
      }

    Assert.assertTrue(fileName + " references classes that no longer exist: " + unresolved,
        unresolved.isEmpty());
  }

  /**
   * Mirrors the serializer setup of {@link DIYLCStarter#runDIYLC(String[])}.
   */
  @SuppressWarnings("unchecked")
  private Map<String, Object> deserialize() throws IOException {
    XStream xStream = new ConfigurationManager().getSerializer();
    ProjectFileManager.configure(xStream);
    try (Reader reader = new InputStreamReader(new FileInputStream(configFile), "UTF-8")) {
      return (Map<String, Object>) xStream.fromXML(reader);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> Map<String, T> readMap(Map<String, Object> config, String key) {
    return (Map<String, T>) config.get(key);
  }
}
