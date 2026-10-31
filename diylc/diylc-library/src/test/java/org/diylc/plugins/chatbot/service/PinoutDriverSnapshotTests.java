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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.diylc.common.ComponentType;
import org.diylc.plugins.chatbot.model.pinout.AiPinout;
import org.diylc.presenter.ComponentProcessor;

/**
 * Pins down which editable property of which component the AI catalog considers capable of changing
 * that component's terminals.
 * <p>
 * The catalog works this out by observation, so nobody has to remember to declare it. The flip side
 * is that a change to a component can silently add or drop a variant table without anyone noticing,
 * which is what this snapshot is here to prevent. When it fails, look at what changed: if the new
 * behaviour is intended, copy the regenerated file the failure points at over
 * {@code src/test/resources/pinout-drivers.txt} and regenerate the catalog.
 *
 * @author Branislav Stojkovic
 */
public class PinoutDriverSnapshotTests {

  private static final String SNAPSHOT = "pinout-drivers.txt";

  @Test
  public void driverSetsMatchTheSnapshot() throws Exception {
    String actual = render();
    String expected = readSnapshot();

    if (!expected.equals(actual)) {
      File regenerated = new File("target", SNAPSHOT);
      Files.write(regenerated.toPath(), actual.getBytes(StandardCharsets.UTF_8));
      fail("Pinout drivers changed:\n" + difference(expected, actual)
          + "\nIf that is intended, copy " + regenerated.getAbsolutePath()
          + " over src/test/resources/" + SNAPSHOT + " and regenerate the component catalog.");
    }
  }

  /** One line per component type, naming what drives its terminals, sorted for a stable diff. */
  private static String render() {
    ComponentProcessor processor = ComponentProcessor.getInstance();
    List<String> lines = new ArrayList<String>();
    for (List<ComponentType> types : processor.getComponentTypes().values()) {
      for (ComponentType type : types) {
        String name =
            type.getInstanceClass().getCanonicalName().replace("org.diylc.components.", "");
        lines.add(name + " = " + state(PinoutAnalyzer.analyze(type, processor)));
      }
    }
    Collections.sort(lines);
    return String.join("\n", lines) + "\n";
  }

  private static String state(AiPinout pinout) {
    if (pinout == null) {
      return "none";
    }
    return pinout.drivers() == null ? "fixed" : String.join(", ", pinout.drivers());
  }

  private static String readSnapshot() throws Exception {
    try (InputStream stream =
        PinoutDriverSnapshotTests.class.getClassLoader().getResourceAsStream(SNAPSHOT)) {
      if (stream == null) {
        return "";
      }
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static String difference(String expected, String actual) {
    List<String> before = List.of(expected.split("\n"));
    List<String> after = List.of(actual.split("\n"));
    StringBuilder report = new StringBuilder();
    for (String line : after) {
      if (!before.contains(line)) {
        report.append("  now: ").append(line).append('\n');
      }
    }
    for (String line : before) {
      if (!after.contains(line)) {
        report.append("  was: ").append(line).append('\n');
      }
    }
    return report.toString();
  }
}
