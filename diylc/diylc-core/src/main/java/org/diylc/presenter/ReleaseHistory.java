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

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import org.diylc.appframework.update.Version;
import org.diylc.appframework.update.VersionNumber;

/**
 * Reads the release history bundled as <code>update.xml</code>, which is also where the running
 * version number comes from. Needs no {@link Presenter}, so build-time tools can use it too.
 *
 * @author Branislav Stojkovic
 */
public class ReleaseHistory {

  private static final Logger LOG = Logger.getLogger(ReleaseHistory.class);

  private static final int RECENT_VERSION_COUNT = 10;

  private static List<Version> recentVersions = null;

  private ReleaseHistory() {}

  /**
   * @return up to ten most recent versions, newest first.
   */
  public static synchronized List<Version> getRecentVersions() {
    if (recentVersions == null) {
      try (BufferedInputStream in =
          new BufferedInputStream(ReleaseHistory.class.getResourceAsStream("/update.xml"))) {
        XStream xStream = new XStream(new DomDriver());
        xStream.addPermission(AnyTypePermission.ANY);
        @SuppressWarnings("unchecked")
        List<Version> allVersions = (List<Version>) xStream.fromXML(in);
        recentVersions = allVersions.stream()
            .sorted(Comparator.comparing(Version::getVersionNumber).reversed())
            .limit(RECENT_VERSION_COUNT).collect(Collectors.toList());
      } catch (Exception e) {
        LOG.error("Could not load recent versions", e);
        recentVersions = new ArrayList<>();
      }
    }
    return recentVersions;
  }

  /**
   * @return the newest version in the release history, or 5.0.0 if it cannot be read.
   */
  public static VersionNumber getCurrentVersion() {
    return getRecentVersions().stream().findFirst().map(Version::getVersionNumber)
        .orElse(new VersionNumber(5, 0, 0));
  }
}
