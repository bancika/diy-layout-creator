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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.diylc.TestConfiguration;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDatasheetSupport;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.presenter.DatasheetService;
import org.diylc.utils.ReflectionUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Applies every row of every datasheet to its component, so a malformed row fails the build rather
 * than the user's Apply Model, or an AI edit that picks the model. The components are found through
 * the class index, so a new datasheet is covered without touching this test.
 */
public class DatasheetRowsTest {

  @BeforeClass
  public static void setUp() {
    TestConfiguration.initialize();
  }

  @Test
  public void everyRowApplies() throws ReflectiveOperationException {
    List<String> failures = new ArrayList<String>();
    for (Class<? extends IDIYComponent<?>> type : datasheetComponents()) {
      List<String[]> datasheet = DatasheetService.getInstance().loadDatasheet(type);
      Assert.assertNotNull(type.getSimpleName(), datasheet);
      Assert.assertFalse(type.getSimpleName(), datasheet.isEmpty());
      for (String[] row : datasheet) {
        IDatasheetSupport component =
            (IDatasheetSupport) type.getDeclaredConstructor().newInstance();
        try {
          component.applyModel(row);
        } catch (RuntimeException e) {
          failures.add(type.getSimpleName() + ": " + String.join(",", row) + " -> " + e);
        }
      }
    }
    Assert.assertTrue(String.join("\n", failures), failures.isEmpty());
  }

  /**
   * DatasheetService decodes with the platform charset, where a latin-1 byte such as the micro
   * sign turns into U+FFFD instead of failing, so units are written as uF.
   */
  @Test
  public void datasheetsAreAscii() throws IOException {
    List<String> failures = new ArrayList<String>();
    for (Class<? extends IDIYComponent<?>> type : datasheetComponents()) {
      String fileName = type.getSimpleName() + ".datasheet";
      try (InputStream in = type.getResourceAsStream("/datasheets/" + fileName)) {
        Assert.assertNotNull(fileName, in);
        int line = 1;
        int b;
        while ((b = in.read()) != -1) {
          if (b == '\n') {
            line++;
          } else if (b > 0x7f) {
            failures.add(fileName + ":" + line);
          }
        }
      }
    }
    Assert.assertTrue("non-ASCII bytes at " + failures, failures.isEmpty());
  }

  /** A datasheet is found by the class name, so a file no component claims is never loaded. */
  @Test
  public void everyDatasheetFileHasAComponent() throws URISyntaxException {
    URL folder = DatasheetRowsTest.class.getResource("/datasheets");
    Assert.assertNotNull(folder);
    Set<String> claimed = new HashSet<String>();
    for (Class<? extends IDIYComponent<?>> type : datasheetComponents()) {
      claimed.add(type.getSimpleName() + ".datasheet");
    }
    for (String fileName : new File(folder.toURI()).list()) {
      Assert.assertTrue(fileName + " belongs to no datasheet component",
          claimed.contains(fileName));
    }
  }

  @SuppressWarnings("unchecked")
  private static List<Class<? extends IDIYComponent<?>>> datasheetComponents() {
    List<Class<? extends IDIYComponent<?>>> types =
        new ArrayList<Class<? extends IDIYComponent<?>>>();
    for (Class<?> type : ReflectionUtils.getIndexedClasses(IDIYComponent.class)) {
      ComponentDescriptor descriptor = type.getAnnotation(ComponentDescriptor.class);
      if (descriptor != null && descriptor.enableDatasheet()
          && !Modifier.isAbstract(type.getModifiers())) {
        Assert.assertTrue(type.getSimpleName() + " enables a datasheet without IDatasheetSupport",
            IDatasheetSupport.class.isAssignableFrom(type));
        types.add((Class<? extends IDIYComponent<?>>) type);
      }
    }
    Assert.assertFalse("no datasheet components in the class index", types.isEmpty());
    return types;
  }
}
