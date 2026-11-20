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

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;

import org.junit.Test;

public class ReleaseHistoryTest {

  // Packages are named after the Maven version while the running application reports the newest
  // version in update.xml, so a release must not go out with the two disagreeing
  @Test
  public void testPomVersionMatchesReleaseHistory() {
    // set by Surefire from the root POM; absent when the test is run from an IDE
    String projectVersion = System.getProperty("diylc.projectVersion");
    assumeNotNull(projectVersion);
    assertEquals("Set <revision> in diylc/pom.xml to the newest version in update.xml",
        ReleaseHistory.getCurrentVersion().toString(), projectVersion);
  }
}
