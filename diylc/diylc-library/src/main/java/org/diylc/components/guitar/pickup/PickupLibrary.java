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
package org.diylc.components.guitar.pickup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.Utils;

/**
 * Loads, validates, indexes, filters and resolves {@link PickupDefinition}s from the built-in
 * classpath library and any user-supplied JSON files. See task requirements: load once and
 * cache; a broken file must never prevent DIYLC from starting; duplicate ids are rejected (first
 * one wins) rather than silently overridden.
 */
public class PickupLibrary {

  private static final Logger LOG = Logger.getLogger(PickupLibrary.class);

  public static final String BUILTIN_RESOURCE = "/pickups/pickups.json";
  public static final String USER_SUBDIR = "pickups";

  private static volatile PickupLibrary instance;

  private final List<PickupDefinition> definitions;
  private final List<PickupLoadIssue> issues;

  public PickupLibrary(List<PickupDefinition> definitions, List<PickupLoadIssue> issues) {
    this.definitions = List.copyOf(definitions);
    this.issues = List.copyOf(issues);
  }

  /** @return the shared, lazily-loaded instance. Never {@code null}, even if loading failed. */
  public static PickupLibrary getInstance() {
    PickupLibrary result = instance;
    if (result == null) {
      synchronized (PickupLibrary.class) {
        result = instance;
        if (result == null) {
          instance = result = loadDefault();
        }
      }
    }
    return result;
  }

  /** Forces a fresh load from the built-in resource and user directory. */
  public static synchronized PickupLibrary reload() {
    instance = loadDefault();
    return instance;
  }

  /**
   * Loads the built-in classpath library and any user JSON files, merging them (built-in first,
   * then user files in name order) while rejecting duplicate ids. Never throws - any failure is
   * logged and simply results in fewer (or zero) available definitions.
   */
  public static PickupLibrary loadDefault() {
    PickupLibraryLoader loader = new PickupLibraryLoader();
    List<PickupLibraryLoader.ParsedFile> parsedFiles = new ArrayList<>();

    try {
      loadBuiltIn(loader, parsedFiles);
    } catch (Exception e) {
      LOG.error("Could not load built-in pickup library", e);
      parsedFiles.add(new PickupLibraryLoader.ParsedFile(null, null, List.of(), List.of(
          new PickupLoadIssue(BUILTIN_RESOURCE, null, null,
              "unexpected error loading built-in library: " + e.getMessage()))));
    }

    try {
      loadUserFiles(loader, parsedFiles);
    } catch (Exception e) {
      LOG.error("Could not load user pickup library files", e);
      parsedFiles.add(new PickupLibraryLoader.ParsedFile(null, null, List.of(), List.of(
          new PickupLoadIssue(USER_SUBDIR, null, null,
              "unexpected error loading user library files: " + e.getMessage()))));
    }

    return merge(parsedFiles);
  }

  /**
   * Merges any number of already-parsed library files, in the given order, into a single
   * {@link PickupLibrary}. Earlier files take precedence: if two files (or two entries within a
   * file) declare the same pickup id, the one encountered first wins and the later one is
   * rejected with a logged {@link PickupLoadIssue} rather than silently overriding it. Exposed as
   * a separate method (rather than being inlined into {@link #loadDefault()}) so it can be
   * exercised directly in tests without touching the classpath or filesystem.
   */
  public static PickupLibrary merge(List<PickupLibraryLoader.ParsedFile> parsedFiles) {
    Map<String, PickupDefinition> byId = new LinkedHashMap<>();
    List<PickupLoadIssue> allIssues = new ArrayList<>();
    for (PickupLibraryLoader.ParsedFile parsed : parsedFiles) {
      mergeParsedFile(parsed, byId, allIssues);
    }
    for (PickupLoadIssue issue : allIssues) {
      LOG.warn("Pickup library issue: " + issue);
    }
    return new PickupLibrary(new ArrayList<>(byId.values()), allIssues);
  }

  private static void loadBuiltIn(PickupLibraryLoader loader, List<PickupLibraryLoader.ParsedFile> parsedFiles) {
    try (InputStream in = PickupLibrary.class.getResourceAsStream(BUILTIN_RESOURCE)) {
      if (in == null) {
        parsedFiles.add(new PickupLibraryLoader.ParsedFile(null, null, List.of(), List.of(
            new PickupLoadIssue(BUILTIN_RESOURCE, null, null, "resource not found on classpath"))));
        return;
      }
      String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      parsedFiles.add(loader.parse(json, "classpath:" + BUILTIN_RESOURCE));
    } catch (IOException e) {
      parsedFiles.add(new PickupLibraryLoader.ParsedFile(null, null, List.of(), List.of(
          new PickupLoadIssue(BUILTIN_RESOURCE, null, null, "could not read resource: " + e.getMessage()))));
    }
  }

  private static void loadUserFiles(PickupLibraryLoader loader, List<PickupLibraryLoader.ParsedFile> parsedFiles) {
    String userDirPath;
    try {
      userDirPath = Utils.getUserDataDirectory("diylc") + USER_SUBDIR;
    } catch (Exception e) {
      parsedFiles.add(new PickupLibraryLoader.ParsedFile(null, null, List.of(), List.of(
          new PickupLoadIssue(USER_SUBDIR, null, null, "could not resolve user data directory: " + e.getMessage()))));
      return;
    }
    File userDir = new File(userDirPath);
    if (!userDir.isDirectory()) {
      // no user library present - not an error.
      return;
    }
    File[] files = userDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
    if (files == null) {
      return;
    }
    List<File> sorted = Arrays.stream(files).sorted().collect(Collectors.toList());
    for (File file : sorted) {
      try {
        String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        parsedFiles.add(loader.parse(json, file.getAbsolutePath()));
      } catch (Exception e) {
        parsedFiles.add(new PickupLibraryLoader.ParsedFile(null, null, List.of(), List.of(
            new PickupLoadIssue(file.getAbsolutePath(), null, null, "could not read file: " + e.getMessage()))));
      }
    }
  }

  private static void mergeParsedFile(PickupLibraryLoader.ParsedFile parsed,
      Map<String, PickupDefinition> byId, List<PickupLoadIssue> allIssues) {
    allIssues.addAll(parsed.issues());
    for (PickupDefinition definition : parsed.pickups()) {
      if (byId.containsKey(definition.id())) {
        allIssues.add(new PickupLoadIssue(null, definition.id(), "id",
            "duplicate pickup id '" + definition.id() + "'; keeping the first definition already loaded"
                + " and ignoring this one"));
        continue;
      }
      byId.put(definition.id(), definition);
    }
  }

  /** @return all successfully loaded, valid definitions, built-in first. */
  public List<PickupDefinition> getAll() {
    return definitions;
  }

  /** @return every issue (from any source file) encountered while loading. */
  public List<PickupLoadIssue> getIssues() {
    return issues;
  }

  /** @return the definition with the given id, or {@code null} if not found. */
  public PickupDefinition findById(String id) {
    if (id == null) {
      return null;
    }
    for (PickupDefinition definition : definitions) {
      if (id.equals(definition.id())) {
        return definition;
      }
    }
    return null;
  }

  /**
   * Searches definitions by an optional free-text query (matched against manufacturer, model and
   * variant, case-insensitively) and optional format/manufacturer filters. Any {@code null} or
   * blank filter is not applied.
   */
  public List<PickupDefinition> search(String text, PickupFormat format, String manufacturer) {
    String needle = (text == null || text.isBlank()) ? null : text.trim().toLowerCase();
    String manufacturerNeedle = (manufacturer == null || manufacturer.isBlank()) ? null : manufacturer.trim()
        .toLowerCase();
    List<PickupDefinition> result = new ArrayList<>();
    for (PickupDefinition d : definitions) {
      if (format != null && d.format() != format) {
        continue;
      }
      if (manufacturerNeedle != null
          && (d.manufacturer() == null || !d.manufacturer().toLowerCase().contains(manufacturerNeedle))) {
        continue;
      }
      if (needle != null && !matchesText(d, needle)) {
        continue;
      }
      result.add(d);
    }
    return Collections.unmodifiableList(result);
  }

  private boolean matchesText(PickupDefinition d, String needle) {
    return containsIgnoreCase(d.manufacturer(), needle) || containsIgnoreCase(d.model(), needle)
        || containsIgnoreCase(d.variant(), needle);
  }

  private boolean containsIgnoreCase(String haystack, String needle) {
    return haystack != null && haystack.toLowerCase().contains(needle);
  }

  /** @return the distinct set of manufacturers present in this library, sorted alphabetically. */
  public List<String> getManufacturers() {
    return definitions.stream().map(PickupDefinition::manufacturer).filter(m -> m != null && !m.isBlank())
        .distinct().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
  }
}
