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
package org.diylc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Map;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;

import org.diylc.serialization.ProjectFileManager;

/**
 * Gives a test a blank configuration: both the shared {@link ConfigurationManager} and the shared
 * {@link InMemoryConfigurationManager} start empty on every call, with no listeners, so nothing a
 * previous test or a previous run saved or registered can influence the next one. The
 * configuration lives in a temporary directory, never in the user's own {@code ~/diylc}:
 * initializing against the real {@code config.xml} copied it to a new
 * {@code config.xml.<timestamp>} on every run that could not read it, and left that file exposed
 * to whatever a test might write.
 *
 * @author Branislav Stojkovic
 */
public final class TestConfiguration {

  private static final String APP_NAME = "diylc";

  private static File directory;

  private TestConfiguration() {}

  public static synchronized void initialize() {
    ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    if (directory == null) {
      ProjectFileManager.configure(configurationManager.getSerializer());
      try {
        directory = Files.createTempDirectory("diylc-test").toFile();
      } catch (IOException e) {
        throw new IllegalStateException("Could not create a temporary configuration directory", e);
      }
    }
    // initialize() reads back whatever an earlier test saved, and starts empty only without a file
    new File(directory, APP_NAME + File.separator + "config.xml").delete();
    // the configuration directory is resolved from user.home once, inside initialize()
    String userHome = System.getProperty("user.home");
    try {
      System.setProperty("user.home", directory.getAbsolutePath());
      configurationManager.initialize(APP_NAME);
    } finally {
      System.setProperty("user.home", userHome);
    }
    clearListeners(configurationManager);
    resetInMemoryConfiguration();
  }

  // initialize() replaces the values but keeps the listeners, so objects from earlier tests, such
  // as a CustomTreeModel nothing displays any more, would still react to this test's writes
  private static void clearListeners(ConfigurationManager configurationManager) {
    try {
      Field listeners = ConfigurationManager.class.getDeclaredField("listeners");
      listeners.setAccessible(true);
      ((Map<?, ?>) listeners.get(configurationManager)).clear();
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Could not clear configuration listeners", e);
    }
  }

  // InMemoryConfigurationManager offers no way to clear its singleton, which every presenter that
  // tests create shares, including the one inside CloudSyncJob; getInstance() makes a new, empty
  // one once the field is cleared
  private static void resetInMemoryConfiguration() {
    try {
      Field instance = InMemoryConfigurationManager.class.getDeclaredField("instance");
      instance.setAccessible(true);
      instance.set(null, null);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Could not reset the in-memory configuration", e);
    }
  }
}
