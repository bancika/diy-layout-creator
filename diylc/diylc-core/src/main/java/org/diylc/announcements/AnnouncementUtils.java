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
package org.diylc.announcements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for detecting and extracting URLs from announcements and HTML.
 */
public class AnnouncementUtils {

  private static final Pattern HREF_PATTERN = Pattern.compile(
      "href=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

  /**
   * Extracts the first URL found in the text or HTML.
   *
   * @param html text or HTML content
   * @return first URL found, or null if none
   */
  public static String extractFirstUrl(String html) {
    List<String> urls = extractUrls(html);
    return urls.isEmpty() ? null : urls.get(0);
  }

  /**
   * Extracts all unique URLs found in the HTML href attributes in order of appearance.
   *
   * @param html text or HTML content
   * @return list of unique URLs
   */
  public static List<String> extractUrls(String html) {
    List<String> urls = new ArrayList<String>();
    if (html == null || html.isEmpty()) {
      return urls;
    }

    Matcher hrefMatcher = HREF_PATTERN.matcher(html);
    while (hrefMatcher.find()) {
      String url = hrefMatcher.group(1).trim();
      url = normalizeUrl(url);
      if (!urls.contains(url)) {
        urls.add(url);
      }
    }

    return urls;
  }

  /**
   * Checks whether the HTML contains any link.
   *
   * @param html text or HTML content
   * @return true if at least one URL is detected, false otherwise
   */
  public static boolean containsLink(String html) {
    return extractFirstUrl(html) != null;
  }

  /**
   * Normalizes a URL to ensure it has a valid protocol if starting with www.
   */
  public static String normalizeUrl(String url) {
    if (url == null) {
      return null;
    }
    String trimmed = url.trim();
    if (trimmed.startsWith("www.")) {
      return "http://" + trimmed;
    }
    return trimmed;
  }
}
