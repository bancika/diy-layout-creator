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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.common.DummyView;
import org.diylc.plugins.cloud.model.IServiceAPI;
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.presenter.Presenter;
import org.diylc.swing.plugins.cloud.sync.CloudSyncConfig.PageConfig;
import org.diylc.swing.plugins.cloud.sync.CloudSyncConfig.UserConfig;

public class CloudSyncJobTests {

  private static final URI PAGE = URI.create("https://example.com/files/Page/Page.htm");
  private static final URI FILE = URI.create("https://example.com/files/Page/My_Amp.diy");

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void uploadsNewFilesOnceAndReplacesChangedOnes() throws Exception {
    FakeCloud cloud = new FakeCloud();
    FakeScraper scraper = new FakeScraper(emptyProject());

    assertEquals(0, newJob(cloud, scraper).run());
    assertEquals(List.of("search", "uploadProject My Amp", "search"), cloud.writesAndLookups);
    assertEquals("Source: " + FILE, cloud.lastDescription);

    cloud.writesAndLookups.clear();
    assertEquals(0, newJob(cloud, scraper).run());
    assertEquals(List.of(), cloud.writesAndLookups);

    scraper.content = new String(scraper.content, StandardCharsets.UTF_8)
        .replace("New Project", "Changed Project").getBytes(StandardCharsets.UTF_8);
    assertEquals(0, newJob(cloud, scraper).run());
    assertEquals(List.of("replaceProjectFiles 1"), cloud.writesAndLookups);
    assertEquals(1, cloud.projects.size());
  }

  @Test
  public void takesOverProjectAlreadyUploadedUnderSameName() throws Exception {
    FakeCloud cloud = new FakeCloud();
    cloud.add("My Amp");
    cloud.add("Something Else");

    assertEquals(0, newJob(cloud, new FakeScraper(emptyProject())).run());
    assertEquals(List.of("search", "replaceProjectFiles 1"), cloud.writesAndLookups);
  }

  @Test
  public void unreadableFileIsReportedAndNotUploaded() throws Exception {
    FakeCloud cloud = new FakeCloud();
    FakeScraper scraper = new FakeScraper("not a project".getBytes(StandardCharsets.UTF_8));

    assertEquals(1, newJob(cloud, scraper).run());
    assertEquals(List.of(), cloud.writesAndLookups);
  }

  @Test
  public void extractsDistinctDiyLinksInPageOrder() {
    String html = "<a href=\"https://example.com/files/Page/A_B.diy\">A_B.diy</a>"
        + "<a href='Readme.htm'>readme</a>"
        + "<a HREF=\"Sub/Bandmaster 5E7.DIY\">x</a>"
        + "<a href=\"https://example.com/files/Page/A_B.diy\">again</a>";
    List<URI> links = PageScraper.extractDiyLinks(PAGE, html);
    assertEquals(List.of(URI.create("https://example.com/files/Page/A_B.diy"),
        URI.create("https://example.com/files/Page/Sub/Bandmaster%205E7.DIY")), links);
  }

  @Test
  public void derivesProjectNameFromFileName() {
    assertEquals("Fender Bandmaster 5E7", CloudSyncJob.projectName(
        URI.create("https://example.com/Fender_Bandmaster%205E7.diy")));
    String longName = CloudSyncJob.projectName(URI.create("https://example.com/"
        + "AA1164_Princeton_Reverb_Layout_Reverb_and_Tremolo_Deleted_Hoffman_Board.diy"));
    assertEquals("AA1164 Princeton Reverb Layout Reverb and Tremolo", longName);
  }

  @Test
  public void descriptionAlwaysCitesSource() {
    URI source = URI.create("https://example.com/a.diy");
    assertEquals("Source: https://example.com/a.diy", CloudSyncJob.description(null, source));
    String description = CloudSyncJob.description("x".repeat(2000), source);
    assertEquals(1024, description.length());
    assertTrue(description.endsWith("\n\nSource: https://example.com/a.diy"));
  }

  @Test(expected = IOException.class)
  public void refusesToStoreOutsideUserDirectory() throws IOException {
    CloudSyncJob.localFile(new File("target/sync-test"),
        URI.create("https://example.com/a/../../../../escape.diy"));
  }

  @Test
  public void readsPagesAsBareUrlsOrObjects() throws IOException {
    String json = "{\"users\":[{\"userId\":1,\"category\":\"Other\",\"pages\":["
        + "\"https://example.com/a.htm\","
        + "{\"url\":\"https://example.com/b.htm\",\"category\":\"Guitar Amplifiers\"}]}]}";
    CloudSyncConfig config = new ObjectMapper().readValue(json, CloudSyncConfig.class);
    CloudSyncConfig.UserConfig user = config.users().get(0);
    assertEquals("Other", user.categoryFor(user.pages().get(0)));
    assertEquals("Guitar Amplifiers", user.categoryFor(user.pages().get(1)));
    assertEquals("", user.keywordsFor(user.pages().get(1)));
    assertEquals(CloudSyncConfig.DEFAULT_SERVICE_URL, config.serviceUrlOrDefault());
  }

  private CloudSyncJob newJob(FakeCloud cloud, FakeScraper scraper) throws IOException {
    UserConfig user = new UserConfig(7, "someone", "cron-job", "token", "Guitar Amplifiers", null,
        List.of(PageConfig.of(PAGE.toString())));
    CloudSyncConfig config = new CloudSyncConfig(null, null, List.of(user));
    return new CloudSyncJob(config, new File(folder.getRoot(), "storage"), false, cloud.api(),
        scraper);
  }

  private byte[] emptyProject() throws IOException {
    ConfigurationManager.getInstance().initialize("diylc-test");
    File file = folder.newFile("empty.diy");
    new Presenter(new DummyView(), InMemoryConfigurationManager.getInstance())
        .saveProjectToFile(file.getAbsolutePath(), false);
    return Files.readAllBytes(file.toPath());
  }

  private static class FakeScraper extends PageScraper {

    private byte[] content;

    FakeScraper(byte[] content) {
      this.content = content;
    }

    @Override
    public List<URI> findDiyLinks(URI page) {
      return List.of(FILE);
    }

    @Override
    public byte[] download(URI uri) {
      return content;
    }
  }

  private static class FakeCloud {

    private final List<ProjectEntity> projects = new ArrayList<>();
    private final List<String> writesAndLookups = new ArrayList<>();
    private String lastDescription;

    void add(String name) {
      ProjectEntity project = new ProjectEntity();
      project.setId(projects.size() + 1);
      project.setName(name);
      projects.add(project);
    }

    IServiceAPI api() {
      return (IServiceAPI) Proxy.newProxyInstance(getClass().getClassLoader(),
          new Class<?>[] {IServiceAPI.class}, (proxy, method, args) -> {
            switch (method.getName()) {
              case "getCategories":
                return new ArrayList<>(List.of("Other", "- Guitar Amplifiers"));
              case "search":
                writesAndLookups.add("search");
                return new ArrayList<>(projects);
              case "uploadProject":
                assertEquals("- Guitar Amplifiers", args[4]);
                lastDescription = (String) args[5];
                add((String) args[3]);
                writesAndLookups.add("uploadProject " + args[3]);
                return "Success";
              case "replaceProjectFiles":
                writesAndLookups.add("replaceProjectFiles " + args[6]);
                return "Success";
              default:
                throw new UnsupportedOperationException(method.getName());
            }
          });
    }
  }
}
