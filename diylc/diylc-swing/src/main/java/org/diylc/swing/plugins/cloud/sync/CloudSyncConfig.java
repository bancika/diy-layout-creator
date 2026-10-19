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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configuration of {@link CloudSyncJob}. It carries login tokens, so the file it is read from is
 * kept out of version control.
 *
 * @param serviceUrl optional override of the cloud API base URL
 * @param storageDir where downloaded files, thumbnails and sync state are kept, resolved against
 *        the directory of the configuration file when relative
 * @param users accounts whose pages are synced
 */
public record CloudSyncConfig(String serviceUrl, String storageDir, List<UserConfig> users) {

  public static final String DEFAULT_SERVICE_URL = "http://www.diy-fever.com/diylc/api/v1";
  public static final String DEFAULT_STORAGE_DIR = "storage";

  public static CloudSyncConfig load(File file) throws IOException {
    return new ObjectMapper().readValue(file, CloudSyncConfig.class);
  }

  public String serviceUrlOrDefault() {
    return serviceUrl == null || serviceUrl.isBlank() ? DEFAULT_SERVICE_URL : serviceUrl;
  }

  public File resolveStorageDir(File configFile) {
    File dir =
        new File(storageDir == null || storageDir.isBlank() ? DEFAULT_STORAGE_DIR : storageDir);
    return dir.isAbsolute() ? dir
        : new File(configFile.getAbsoluteFile().getParentFile(), dir.getPath());
  }

  /**
   * The cloud API authenticates by username (or email), token and machine ID; {@code userId} only
   * names the user's storage folder. {@code sourceUrl}, when set, is the page cited as the source
   * in project descriptions instead of the link to the file itself.
   */
  public record UserConfig(int userId, String username, String machineId, String token,
      String category, String keywords, String sourceUrl, List<PageConfig> pages) {

    public String sourceFor(URI file) {
      return sourceUrl == null || sourceUrl.isBlank() ? file.toString() : sourceUrl;
    }

    public String categoryFor(PageConfig page) {
      return page.category() == null ? category : page.category();
    }

    public String keywordsFor(PageConfig page) {
      String result = page.keywords() == null ? keywords : page.keywords();
      return result == null ? "" : result;
    }
  }

  /**
   * A page to scrape for links to .diy files. A bare URL string in the JSON is accepted as a page
   * that inherits category and keywords from its user.
   */
  public record PageConfig(String url, String category, String keywords) {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PageConfig {}

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PageConfig of(String url) {
      return new PageConfig(url, null, null);
    }
  }
}
