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
package org.diylc.swing.plugins.cloud.sync;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds links to .diy files on a web page and downloads them.
 */
public class PageScraper {

  private static final Pattern DIY_LINK =
      Pattern.compile("href\\s*=\\s*[\"']([^\"']+\\.diy)[\"']", Pattern.CASE_INSENSITIVE);
  private static final Duration TIMEOUT = Duration.ofSeconds(60);
  private static final String USER_AGENT = "DIYLC-CloudSync";

  private final HttpClient client = HttpClient.newBuilder()
      .followRedirects(HttpClient.Redirect.NORMAL)
      .connectTimeout(TIMEOUT)
      .build();

  public List<URI> findDiyLinks(URI page) throws IOException, InterruptedException {
    return extractDiyLinks(page, new String(download(page), StandardCharsets.UTF_8));
  }

  public byte[] download(URI uri) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder(uri)
        .timeout(TIMEOUT)
        .header("User-Agent", USER_AGENT)
        .GET()
        .build();
    HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    if (response.statusCode() != 200) {
      throw new IOException("HTTP " + response.statusCode() + " while downloading " + uri);
    }
    return response.body();
  }

  /**
   * Returns absolute URIs of every distinct .diy link in the page, in page order. Links are taken
   * as written in the markup, so entity-escaped ampersands are decoded and the raw spaces some
   * pages use in file names are percent-encoded.
   */
  static List<URI> extractDiyLinks(URI base, String html) {
    Set<URI> links = new LinkedHashSet<>();
    Matcher matcher = DIY_LINK.matcher(html);
    while (matcher.find()) {
      String href = matcher.group(1).trim().replace("&amp;", "&").replace(" ", "%20");
      try {
        links.add(base.resolve(href).normalize());
      } catch (IllegalArgumentException e) {
        // not a well-formed URI even after encoding spaces; the link is unusable
      }
    }
    return new ArrayList<>(links);
  }
}
