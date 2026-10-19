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
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.diyfever.httpproxy.PhpFlatProxy;
import com.diyfever.httpproxy.ProxyFactory;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.common.DummyView;
import org.diylc.core.IView;
import org.diylc.plugins.cloud.model.IServiceAPI;
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.plugins.cloud.service.CloudException;
import org.diylc.presenter.Presenter;
import org.diylc.swing.plugins.cloud.ThumbnailGenerator;
import org.diylc.swing.plugins.cloud.sync.CloudSyncConfig.PageConfig;
import org.diylc.swing.plugins.cloud.sync.CloudSyncConfig.UserConfig;
import org.diylc.swing.plugins.cloud.sync.SyncState.SyncedFile;

/**
 * Command-line job that mirrors .diy files published on users' own web pages into the DIYLC
 * cloud. Each new file is uploaded as a project and each changed file replaces the files of the
 * project it was uploaded as, through the same API calls the Upload action makes on the user's
 * behalf. The description of each project cites where the file came from, and is updated when the
 * configured source changes. Meant to run headless from cron:
 *
 * <pre>
 * java -cp diylc.jar org.diylc.swing.plugins.cloud.sync.CloudSyncJob cloud-sync.json [--dry-run]
 * </pre>
 *
 * With {@code --dry-run} files are downloaded and thumbnails rendered into local storage, but
 * nothing is uploaded and the sync state is left untouched.
 *
 * @author Branislav Stojkovic
 */
public class CloudSyncJob {

  private static final Logger LOG = Logger.getLogger(CloudSyncJob.class);

  private static final String DRY_RUN = "--dry-run";
  private static final String SUCCESS = "Success";
  private static final String STATE_FILE = "sync-state.json";
  private static final String CONFIGURATION_NAME = "diylc-cloud-sync";
  private static final String SORT_BY_AGE = "Age";
  // column sizes of diylc_project on the server
  private static final int MAX_NAME_LENGTH = 50;
  private static final int MAX_DESCRIPTION_LENGTH = 1024;
  private static final Pattern SOURCE_LINE = Pattern.compile("(?:\\A|\\s+)Source: \\S*\\s*\\z");

  private final CloudSyncConfig config;
  private final File storageDir;
  private final boolean dryRun;
  private final IServiceAPI service;
  private final PageScraper scraper;
  private final RecordingView view = new RecordingView();
  private final Presenter presenter;
  private final ThumbnailGenerator thumbnailGenerator;
  private final String diylcVersion;
  private List<String> categories;
  // projects the current user already has in the cloud, fetched only once a file is found that
  // the sync state does not know
  private List<ProjectEntity> uploads;

  public CloudSyncJob(CloudSyncConfig config, File storageDir, boolean dryRun) {
    this(config, storageDir, dryRun, new ProxyFactory(new PhpFlatProxy())
        .createProxy(IServiceAPI.class, config.serviceUrlOrDefault()), new PageScraper());
  }

  CloudSyncJob(CloudSyncConfig config, File storageDir, boolean dryRun, IServiceAPI service,
      PageScraper scraper) {
    this.config = config;
    this.storageDir = storageDir;
    this.dryRun = dryRun;
    this.service = service;
    this.scraper = scraper;
    this.presenter = new Presenter(view, InMemoryConfigurationManager.getInstance());
    this.thumbnailGenerator = new ThumbnailGenerator(presenter);
    this.diylcVersion = presenter.getCurrentVersionNumber().toString();
  }

  public static void main(String[] args) {
    System.setProperty("java.awt.headless", "true");
    // replaces the application's log4j.properties, which log4j picks up from the classpath
    LogManager.resetConfiguration();
    BasicConfigurator.configure();
    Logger.getRootLogger().setLevel(Level.WARN);
    LOG.setLevel(Level.INFO);

    List<String> arguments = new ArrayList<>(Arrays.asList(args));
    boolean dryRun = arguments.remove(DRY_RUN);
    if (arguments.size() != 1) {
      System.err.println("Usage: CloudSyncJob <config.json> [" + DRY_RUN + "]");
      System.exit(2);
    }

    // components read drawing options from the global configuration, so it needs initializing;
    // a name of its own keeps the job on default settings and away from the user's config
    ConfigurationManager.getInstance().initialize(CONFIGURATION_NAME);

    int failures;
    try {
      File configFile = new File(arguments.get(0));
      CloudSyncConfig config = CloudSyncConfig.load(configFile);
      failures = new CloudSyncJob(config, config.resolveStorageDir(configFile), dryRun).run();
    } catch (Exception e) {
      LOG.error("Cloud sync failed", e);
      failures = 1;
    }
    System.exit(failures == 0 ? 0 : 1);
  }

  /**
   * @return the number of pages and files that could not be synced
   */
  public int run() throws CloudException {
    categories = service.getCategories();
    if (categories == null || categories.isEmpty()) {
      throw new CloudException("Could not fetch categories from the server.");
    }
    int failures = 0;
    for (UserConfig user : config.users()) {
      failures += syncUser(user);
    }
    return failures;
  }

  private int syncUser(UserConfig user) {
    File userDir = new File(storageDir, Integer.toString(user.userId()));
    Map<Outcome, Integer> outcomes = new EnumMap<>(Outcome.class);
    try {
      validate(user);
      SyncState state = SyncState.load(new File(userDir, STATE_FILE));
      uploads = null;
      Set<URI> seen = new HashSet<>();
      for (PageConfig page : user.pages()) {
        List<URI> links;
        try {
          links = scraper.findDiyLinks(URI.create(page.url()));
        } catch (Exception e) {
          LOG.error("Could not scrape " + page.url(), e);
          outcomes.merge(Outcome.FAILED, 1, Integer::sum);
          continue;
        }
        for (URI link : links) {
          if (seen.add(link)) {
            Outcome outcome;
            try {
              outcome = syncFile(user, page, link, userDir, state);
            } catch (Exception e) {
              LOG.error("Could not sync " + link, e);
              outcome = Outcome.FAILED;
            }
            outcomes.merge(outcome, 1, Integer::sum);
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Could not sync user " + user.userId(), e);
      outcomes.merge(Outcome.FAILED, 1, Integer::sum);
    }
    LOG.info("User " + user.userId() + (dryRun ? " (dry run): " : ": ") + outcomes);
    return outcomes.getOrDefault(Outcome.FAILED, 0);
  }

  private void validate(UserConfig user) throws CloudException {
    if (!dryRun && (isBlank(user.username()) || isBlank(user.token())
        || isBlank(user.machineId()))) {
      throw new CloudException("username, token and machineId are required");
    }
    if (user.pages() == null || user.pages().isEmpty()) {
      throw new CloudException("No pages configured");
    }
    for (PageConfig page : user.pages()) {
      resolveCategory(user.categoryFor(page));
    }
  }

  private Outcome syncFile(UserConfig user, PageConfig page, URI uri, File userDir,
      SyncState state) throws Exception {
    String url = uri.toString();
    byte[] content = scraper.download(uri);
    String sha256 = sha256(content);
    SyncedFile synced = state.get(url);
    String source = user.sourceFor(uri);
    if (synced != null && synced.sha256().equals(sha256)) {
      if (source.equals(synced.source())) {
        return Outcome.UNCHANGED;
      }
      if (dryRun) {
        LOG.info("Would cite " + source + " in project " + synced.projectId());
      } else {
        citeSource(user, synced.projectId(), source);
        state.put(new SyncedFile(url, synced.projectId(), sha256, source,
            Instant.now().toString()));
      }
      return Outcome.SOURCE_UPDATED;
    }

    File projectFile = localFile(userDir, uri);
    projectFile.getParentFile().mkdirs();
    Files.write(projectFile.toPath(), content);
    File thumbnailFile = new File(projectFile.getPath() + ".png");
    String projectDescription = renderThumbnail(projectFile, thumbnailFile);
    String name = projectName(uri);

    // a project already in the cloud under the same name, uploaded by hand or by a run whose
    // state was lost, is taken over rather than duplicated
    Integer projectId = synced != null ? Integer.valueOf(synced.projectId())
        : findUnclaimed(getUploads(user), name, state, Comparator.naturalOrder()).orElse(null);
    Outcome outcome = projectId == null ? Outcome.UPLOADED : Outcome.UPDATED;
    if (dryRun) {
      LOG.info((projectId == null ? "Would upload " : "Would update project " + projectId
          + " from ") + url);
      return outcome;
    }

    if (projectId == null) {
      checkResponse(service.uploadProject(user.username(), user.token(), user.machineId(), name,
          resolveCategory(user.categoryFor(page)), description(projectDescription, source),
          diylcVersion, user.keywordsFor(page), thumbnailFile, projectFile, null));
      // the API does not return the ID of the new project, so the newest unclaimed one with the
      // same name is taken to be it
      uploads = fetchUploads(user);
      projectId = findUnclaimed(uploads, name, state, Comparator.reverseOrder())
          .orElseThrow(() -> new CloudException(
              "Uploaded " + url + " but could not find it among the user's projects"));
      LOG.info("Uploaded " + url + " as project " + projectId);
    } else {
      checkResponse(service.replaceProjectFiles(user.username(), user.token(),
          user.machineId(), diylcVersion, thumbnailFile, projectFile, projectId));
      LOG.info("Updated project " + projectId + " from " + url);
      // replacing the files leaves the description as it was, including the source it cites
      if (synced == null || !source.equals(synced.source())) {
        citeSource(user, projectId, source);
      }
    }
    state.put(new SyncedFile(url, projectId, sha256, source, Instant.now().toString()));
    return outcome;
  }

  /**
   * Loads the project into the off-screen presenter and writes its thumbnail.
   *
   * @return the project description stored in the file
   */
  private String renderThumbnail(File projectFile, File thumbnailFile)
      throws IOException, CloudException {
    view.takeError();
    presenter.loadProjectFromFile(projectFile.getAbsolutePath());
    String error = view.takeError();
    if (error != null) {
      throw new CloudException(error);
    }
    if (!ImageIO.write(thumbnailGenerator.getThumbnail(), "png", thumbnailFile)) {
      throw new IOException("Could not write " + thumbnailFile);
    }
    return presenter.getCurrentProject().getDescription();
  }

  /**
   * Points the source line of the project's description at the specified source, keeping the rest
   * of the project details as they are in the cloud.
   */
  private void citeSource(UserConfig user, int projectId, String source) throws CloudException {
    ProjectEntity project = getUploads(user).stream()
        .filter(p -> p.getId() == projectId)
        .findFirst()
        .orElseThrow(() -> new CloudException(
            "Project " + projectId + " is not among the user's projects"));
    String description = withSource(project.getDescription(), source);
    if (description.equals(project.getDescription())) {
      return;
    }
    checkResponse(service.uploadProject(user.username(), user.token(), user.machineId(),
        project.getName(), project.getCategoryForDisplay(), description, diylcVersion,
        project.getKeywords(), null, null, projectId));
    LOG.info("Cited " + source + " in project " + projectId);
  }

  private List<ProjectEntity> getUploads(UserConfig user) throws CloudException {
    if (uploads == null) {
      uploads = isBlank(user.username()) ? List.of() : fetchUploads(user);
    }
    return uploads;
  }

  private List<ProjectEntity> fetchUploads(UserConfig user) throws CloudException {
    Object res =
        service.search("", "", 1, Integer.MAX_VALUE, SORT_BY_AGE, user.username(), null);
    if (res instanceof List<?> list) {
      return list.stream().map(ProjectEntity.class::cast).toList();
    }
    // a single result is not wrapped in a list, and no results come back as an empty string
    if (res instanceof ProjectEntity project) {
      return List.of(project);
    }
    if (res == null || res instanceof String s && s.isBlank()) {
      return List.of();
    }
    throw new CloudException("Unexpected response while fetching user projects: " + res);
  }

  private static Optional<Integer> findUnclaimed(List<ProjectEntity> uploads, String name,
      SyncState state, Comparator<Integer> preference) {
    return uploads.stream()
        .filter(p -> name.equals(p.getName()) && !state.isClaimed(p.getId()))
        .map(ProjectEntity::getId)
        .min(preference);
  }

  private String resolveCategory(String configured) throws CloudException {
    if (configured != null) {
      String wanted = stripSubcategoryPrefix(configured);
      for (String category : categories) {
        if (stripSubcategoryPrefix(category).equalsIgnoreCase(wanted)) {
          return category;
        }
      }
    }
    throw new CloudException("Unknown category '" + configured + "', expected one of "
        + categories);
  }

  // subcategories are listed as "- Name" and the server matches the category by that full string
  private static String stripSubcategoryPrefix(String category) {
    String trimmed = category.trim();
    return trimmed.startsWith("- ") ? trimmed.substring(2).trim() : trimmed;
  }

  private static void checkResponse(String res) throws CloudException {
    if (!SUCCESS.equals(res)) {
      throw new CloudException(res);
    }
  }

  static String projectName(URI uri) {
    String path = uri.getPath();
    String name = path.substring(path.lastIndexOf('/') + 1)
        .replaceFirst("(?i)\\.diy$", "")
        .replace('_', ' ')
        .replaceAll("\\s+", " ")
        .trim();
    return name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH).trim() : name;
  }

  static String description(String projectDescription, String source) {
    String sourceLine = "Source: " + source;
    if (isBlank(projectDescription)) {
      return sourceLine;
    }
    String text = projectDescription.trim();
    int room = MAX_DESCRIPTION_LENGTH - sourceLine.length() - 2;
    if (text.length() > room) {
      text = text.substring(0, room).trim();
    }
    return text + "\n\n" + sourceLine;
  }

  // replaces the source line that description() ends with, or adds one when there is none, and
  // keeps the rest of the description, which may have been edited in the cloud
  static String withSource(String description, String source) {
    String text =
        description == null ? null : SOURCE_LINE.matcher(description).replaceFirst("");
    return description(text, source);
  }

  static File localFile(File userDir, URI uri) throws IOException {
    File root = userDir.getCanonicalFile();
    File file = new File(new File(root, uri.getHost()), uri.getPath()).getCanonicalFile();
    if (!file.toPath().startsWith(root.toPath())) {
      throw new IOException("Refusing to store " + uri + " outside " + root);
    }
    return file;
  }

  private static String sha256(byte[] content) throws NoSuchAlgorithmException {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  private enum Outcome {
    UNCHANGED, UPLOADED, UPDATED, SOURCE_UPDATED, FAILED
  }

  /**
   * {@link Presenter#loadProjectFromFile(String)} reports a failed load only through the view, so
   * the error is captured here to keep a stale project from being uploaded in its place.
   */
  private static class RecordingView extends DummyView {

    private String error;

    @Override
    public void showMessage(String message, String title, int messageType) {
      if (messageType == IView.ERROR_MESSAGE) {
        error = message;
      }
    }

    String takeError() {
      String result = error;
      error = null;
      return result;
    }
  }
}
