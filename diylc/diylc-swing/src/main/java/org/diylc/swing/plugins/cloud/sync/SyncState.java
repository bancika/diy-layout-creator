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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Remembers, per source URL, which cloud project a file was uploaded as and the hash of the content
 * that was uploaded, so that later runs only upload what is new or changed.
 */
public class SyncState {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  private final File file;
  private final Map<String, SyncedFile> filesByUrl = new LinkedHashMap<>();

  private SyncState(File file) {
    this.file = file;
  }

  public static SyncState load(File file) throws IOException {
    SyncState state = new SyncState(file);
    if (file.exists()) {
      Snapshot snapshot = MAPPER.readValue(file, Snapshot.class);
      for (SyncedFile synced : snapshot.files()) {
        state.filesByUrl.put(synced.url(), synced);
      }
    }
    return state;
  }

  public SyncedFile get(String url) {
    return filesByUrl.get(url);
  }

  public boolean isClaimed(int projectId) {
    return filesByUrl.values().stream().anyMatch(f -> f.projectId() == projectId);
  }

  /** Records the file and writes the state out immediately, so an interrupted run loses nothing. */
  public void put(SyncedFile synced) throws IOException {
    filesByUrl.put(synced.url(), synced);
    file.getParentFile().mkdirs();
    File temp = new File(file.getPath() + ".tmp");
    MAPPER.writeValue(temp, new Snapshot(new ArrayList<>(filesByUrl.values())));
    Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING,
        StandardCopyOption.ATOMIC_MOVE);
  }

  public record SyncedFile(String url, int projectId, String sha256, String syncedOn) {}

  private record Snapshot(List<SyncedFile> files) {}
}
