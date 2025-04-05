/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.plugins.cloud.service;

import java.io.*;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import java.lang.management.ManagementFactory;

import com.diyfever.httpproxy.PhpFlatProxy;
import com.diyfever.httpproxy.ProxyFactory;

import org.diylc.appframework.miscutils.Utils;
import org.diylc.common.EventType;
import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.plugins.cloud.model.CommentEntity;
import org.diylc.plugins.cloud.model.IServiceAPI;
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.plugins.cloud.model.UserEntity;
import org.diylc.presenter.ComparatorFactory;
import org.diylc.presenter.ComponentProcessor;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Contains all the back-end logic for using the cloud and manipulating projects on the cloud.
 * 
 * @author Branislav Stojkovic
 */
public class CloudService {
  
  private static final Logger LOG = Logger.getLogger(CloudService.class);
  private static final String MACHINE_ID_FILE = "machine.id";
  private static final String MACHINE_ID_KEY = "cloud.machineId";
  private static final String USERNAME_KEY = "cloud.Username";
  private static final String TOKEN_KEY = "cloud.token";
  private static final String ERROR = "Error";
  private static final Object SUCCESS = "Success";
  private static final ExecutorService executor = Executors.newSingleThreadExecutor();

  private final IPlugInPort plugInPort;

  private IServiceAPI service;
  private String serviceUrl;
  private UserEntity currentUser;
  private String currentToken;
  private String machineId;
  private String[] categories;

  private boolean loggedIn = false;
  
  public CloudService(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    
    serviceUrl = ConfigurationManager.getInstance().readString(IServiceAPI.URL_KEY,
        "http://www.diy-fever.com/diylc/api/v1");
    ProxyFactory factory = new ProxyFactory(new PhpFlatProxy());
    service = factory.createProxy(IServiceAPI.class, serviceUrl);

    // Delay a test of logged in state to give it time to complete
    Timer timer = new Timer(1000, e -> {
      try {
        tryLogInWithToken();
      } catch (Exception ex) {
        LOG.error("Background login attempt failed", ex);
      }
    });
    timer.setRepeats(false); // Make it one-shot
    timer.start();
  }

  private IServiceAPI getService() {
    if (service == null) {
      serviceUrl =
          ConfigurationManager.getInstance().readString(IServiceAPI.URL_KEY, "http://www.diy-fever.com/diylc/api/v1");
      ProxyFactory factory = new ProxyFactory(new PhpFlatProxy());
      service = factory.createProxy(IServiceAPI.class, serviceUrl);
    }
    return service;
  }

  public boolean logIn(String username, String password) throws CloudException {
    LOG.info("Trying to login to cloud as " + username);

    String res;
    try {
      res = getService().login(username, password, getMachineId());
    } catch (Exception e) {
      throw new CloudException(e);
    }

    if (res == null || res.equals(ERROR)) {
      LOG.info("Login failed");
      return false;
    } else {
      LOG.info("Login success");
      ConfigurationManager.getInstance().writeValue(USERNAME_KEY, username);
      ConfigurationManager.getInstance().writeValue(TOKEN_KEY, res);
      this.loggedIn = true;
      this.plugInPort.getMessageDispatcher().dispatchMessage(EventType.CLOUD_LOGGED_IN);
      return true;
    }
  }

  private void tryLogInWithToken() throws CloudException {
    String username = ConfigurationManager.getInstance().readString(USERNAME_KEY, null);
    String token = ConfigurationManager.getInstance().readString(TOKEN_KEY, null);

    if (username != null && token != null) {
      LOG.info("Trying to login to cloud using a token as " + username);
      String res;
      try {
        res = getService().loginWithToken(username, token, getMachineId());
      } catch (Exception e) {
        throw new CloudException(e);
      }
      if (res == null || res.equals(ERROR)) {
        LOG.info("Login failed");
      } else {
        LOG.info("Login success");
        this.loggedIn = true;
        this.plugInPort.getMessageDispatcher().dispatchMessage(EventType.CLOUD_LOGGED_IN);
      }
    }
  }

  public void logOut() {
    LOG.info("Logged out");
    ConfigurationManager.getInstance().writeValue(TOKEN_KEY, null);
    this.loggedIn = false;
    this.plugInPort.getMessageDispatcher().dispatchMessage(EventType.CLOUD_LOGGED_OUT);
  }

  public boolean isLoggedIn() {
    return loggedIn;
  }

  public String[] getCategories() throws CloudException {
    if (categories == null) {
      Object res;
      LOG.info("Fetching categories");
      try {
        res = getService().getCategories();
        if (res != null && res.equals(ERROR))
          throw new CloudException("Could not fetch categories from the server.");
        if (res instanceof List<?>) {
          @SuppressWarnings("unchecked")
          List<String> cats = (List<String>) res;
          cats.add(0, "");
          categories = cats.toArray(new String[0]);
        } else {
          throw new CloudException("Unexpected server response received for category list: " + res.getClass().getName());
        }
      } catch (Exception e) {
        throw new CloudException(e);
      }
    }
    return categories;
  }

  public void uploadProject(String projectName, String category, String description, String keywords,
      String diylcVersion, File thumbnail, File project, Integer projectId) throws IOException, CloudException {
    String username = ConfigurationManager.getInstance().readString(USERNAME_KEY, null);
    String token = ConfigurationManager.getInstance().readString(TOKEN_KEY, null);

    if (username == null || token == null)
      throw new CloudException("Login failed. Please try to login again.");

    if (projectId == null)
      LOG.info("Uploading a new project: " + projectName);
    else
      LOG.info("Updating project: " + projectId);
    
    try {
      String res =
          getService().uploadProject(username, token, getMachineId(), projectName, category, description, diylcVersion,
              keywords, thumbnail, project, projectId);
      if (!res.equals(SUCCESS))
        throw new CloudException(res);
    } catch (Exception e) {
      throw new CloudException(e);
    }
  }
  
  public void replaceProjectFile(String diylcVersion, File thumbnail, File project, Integer projectId) throws IOException, CloudException {
    String username = ConfigurationManager.getInstance().readString(USERNAME_KEY, null);
    String token = ConfigurationManager.getInstance().readString(TOKEN_KEY, null);

    if (username == null || token == null)
      throw new CloudException("Login failed. Please try to login again.");

    LOG.info("Replacing project files for: " + projectId);
    try {
      String res =
          getService().replaceProjectFiles(username, token, getMachineId(), diylcVersion, thumbnail, project, projectId);
      if (!res.equals(SUCCESS))
        throw new CloudException(res);
    } catch (Exception e) {
      throw new CloudException(e);
    }
  }

  public void updateProjectDetails(ProjectEntity project, String diylcVersion) throws IOException, CloudException {
    String username = ConfigurationManager.getInstance().readString(USERNAME_KEY, null);
    String token = ConfigurationManager.getInstance().readString(TOKEN_KEY, null);

    if (username == null || token == null)
      throw new CloudException("Login failed. Please try to login again.");
    LOG.info("Updating project with projectId: " + project.getId());
    try {
      String res =
          getService().uploadProject(username, token, getMachineId(), project.getName(),
              project.getCategoryForDisplay(), project.getDescription(), diylcVersion, project.getKeywords(), null,
              null, project.getId());
      if (!res.equals(SUCCESS))
        throw new CloudException(res);
    } catch (Exception e) {
      throw new CloudException(e);
    }
  }

  public void deleteProject(int projectId) throws CloudException {
    String username = ConfigurationManager.getInstance().readString(USERNAME_KEY, null);
    String token = ConfigurationManager.getInstance().readString(TOKEN_KEY, null);

    if (username == null || token == null)
      throw new CloudException("Login failed. Please try to login again.");

    LOG.info("Deleting project with projectId: " + projectId);
    try {
      String res = getService().deleteProject(username, token, getMachineId(), projectId);
      if (!res.equals(SUCCESS))
        throw new CloudException(res);
    } catch (Exception e) {
      throw new CloudException(e);
    }
  }

  public void postComment(int projectId, String comment) throws CloudException {
    String username = ConfigurationManager.getInstance().readString(USERNAME_KEY, null);
    String token = ConfigurationManager.getInstance().readString(TOKEN_KEY, null);

    if (username == null || token == null)
      throw new CloudException("Login failed. Please try to login again.");

    LOG.info("Posting a new comment to projectId: " + projectId);

    try {
      String res = getService().postComment(username, token, getMachineId(), projectId, comment);
      if (!res.equals(SUCCESS))
        throw new CloudException(res);
    } catch (Exception e) {
      throw new CloudException(e);
    }
  }

  public void createUserAccount(String username, String password, String email, String website, String bio)
      throws CloudException {
    LOG.info("Creating a new user: " + username);
    try {
      String res = getService().createUser(username, password, email, website, bio);
      if (res == null)
        throw new CloudException("Could not create user account.");
      if (!SUCCESS.equals(res))
        throw new CloudException(res);
    } catch (Exception e) {
      throw new CloudException(e);
    }
  }

  public UserEntity getUserDetails() throws CloudException {
    String username = ConfigurationManager.getInstance().readString(USERNAME_KEY, null);
    String token = ConfigurationManager.getInstance().readString(TOKEN_KEY, null);
    LOG.info("Retreiving user details for: " + username);
    Object res;
    try {
      res = getService().getUserDetails(username, token, getMachineId());
    } catch (Exception e) {
      throw new CloudException(e);
    }
    if (res instanceof String)
      throw new CloudException(res.toString());
    if (res instanceof UserEntity)
      return (UserEntity) res;
    throw new CloudException("Unexpected server response received for category list: " + res.getClass().getName());
  }

  public String getCurrentUsername() {
    return ConfigurationManager.getInstance().readString(USERNAME_KEY, null);
  }

  public String getCurrentToken() {
    return ConfigurationManager.getInstance().readString(TOKEN_KEY, null);
  }

  public void updatePassword(String oldPassword, String newPassword) throws CloudException {
    String username = ConfigurationManager.getInstance().readString(USERNAME_KEY, null);

    LOG.info("Updating password for: " + username);
    String res;
    try {
      res = getService().updatePassword(username, oldPassword, newPassword);
    } catch (Exception e) {
      throw new CloudException(e);
    }
    if (!res.equals(SUCCESS))
      throw new CloudException(res);
  }

  public void updateUserDetails(String email, String website, String bio) throws CloudException {
    String username = ConfigurationManager.getInstance().readString(USERNAME_KEY, null);
    String token = ConfigurationManager.getInstance().readString(TOKEN_KEY, null);

    LOG.info("Updating user details for: " + username);
    String res;
    try {
      res = getService().updateUserDetails(username, token, getMachineId(), email, website, bio);
    } catch (Exception e) {
      throw new CloudException(e);
    }
    if (!res.equals(SUCCESS))
      throw new CloudException(res);
  }

  public List<ProjectEntity> search(String criteria, String category, String sortOrder, int pageNumber, int itemsPerPage)
      throws CloudException {
    LOG.info(String.format("search(%1$s,%2$s,%3$s,%4$d,%5$d)", criteria, category, sortOrder, pageNumber, itemsPerPage));
    try {
      Object res = getService().search(criteria, category, pageNumber, itemsPerPage, sortOrder, null, null);
      return processResults(res);
    } catch (CloudException ce) {
      throw ce;
    } catch (Exception e) {
      throw new CloudException(e);
    }
  }

  public List<ProjectEntity> fetchUserUploads(Integer projectId) throws CloudException {
    UserEntity user = getUserDetails();
    LOG.info("Fetching all user uploads for: " + user.getUsername());
    try {
      Object res = getService().search("", "", 1, Integer.MAX_VALUE, getSortings()[0], user.getUsername(), projectId);
      return processResults(res);
    } catch (CloudException ce) {
      throw ce;
    } catch (Exception e) {
      throw new CloudException(e);
    }
  }

  private List<ProjectEntity> processResults(Object res) throws CloudException, IOException {
    if (res == null)
      throw new CloudException("Failed to retreive search results.");
    if (res instanceof String)
      throw new CloudException(res.toString());
    if (res instanceof List<?>) {
      @SuppressWarnings("unchecked")
      List<ProjectEntity> projects = (List<ProjectEntity>) res;
      LOG.info("Received " + projects.size() + " results. Downloading thumbnails...");
      // Download thumbnails and replace urls with local paths to speed up loading in the main
      // thread
      for (ProjectEntity project : projects) {
        String url = project.getThumbnailUrl();
        URL website = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        File temp = File.createTempFile("thumbnail", ".png");
        FileOutputStream fos = new FileOutputStream(temp);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        project.setThumbnailUrl(temp.getAbsolutePath());
        project.setCategories(getCategories());
        fos.close();
      }
      LOG.info("Finished downloading thumbnails.");
      return projects;
    }
    throw new CloudException("Unexpected server response received for search results: " + res.getClass().getName());
  }

  public String[] getSortings() throws CloudException {
    LOG.info("Fetching sortings");
    try {
      return getService().getSortings().toArray(new String[0]);
    } catch (Exception e) {
      throw new CloudException(e);
    }
  }

  public List<CommentEntity> getComments(int projectId) throws CloudException {
    LOG.info("Fetching comments for projectId: " + projectId);
    try {
      Object res = getService().getComments(projectId);
      if (res == null)
        throw new CloudException("Failed to retreive search results.");
      if (res instanceof String)
        throw new CloudException(res.toString());
      if (res instanceof List<?>) {
        @SuppressWarnings("unchecked")
        List<CommentEntity> comments = (List<CommentEntity>) res;
        return comments;
      }
      throw new CloudException("Unexpected server response received for comments: " + res.getClass().getName());
    } catch (Exception e) {
      throw new CloudException(e);
    }
  }

  public List<PropertyWrapper> getProjectProperties(ProjectEntity project) {
    List<PropertyWrapper> properties = ComponentProcessor.getInstance().extractProperties(ProjectEntity.class);
    try {
      for (PropertyWrapper property : properties) {
        property.readFrom(project);
      }
    } catch (Exception e) {
      LOG.error("Could not get project entity properties", e);
      return null;
    }
    Collections.sort(properties, ComparatorFactory.getInstance().getDefaultPropertyComparator());
    return properties;
  }

  public String getMachineId() {
    if (machineId == null) {
      if (Utils.isWindows()) {
        machineId = System.getenv("COMPUTERNAME");
      } else {
        try {
          Runtime r = Runtime.getRuntime();
          Process p = r.exec("uname -a");
          BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
          machineId = reader.readLine();
        } catch (Exception e) {
          LOG.error("Error getting machineId from uname", e);
        }
      }
      if (machineId == null) {
        try {
          // Generate a unique identifier based on hardware information
          List<String> identifiers = new ArrayList<>();

          // Get MAC addresses
          Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
          while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            byte[] mac = networkInterface.getHardwareAddress();
            if (mac != null) {
              StringBuilder sb = new StringBuilder();
              for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X", mac[i]));
              }
              identifiers.add(sb.toString());
            }
          }

          // Get hostname
          String hostname = InetAddress.getLocalHost().getHostName();
          identifiers.add(hostname);

          // Sort to ensure consistent order
          Collections.sort(identifiers);

          // Create a single string from all identifiers
          StringBuilder combined = new StringBuilder();
          for (String id : identifiers) {
            combined.append(id);
          }

          // Create a hash of the combined string
          MessageDigest md = MessageDigest.getInstance("SHA-256");
          byte[] hash = md.digest(combined.toString().getBytes());

          // Convert to hex string
          StringBuilder hexString = new StringBuilder();
          for (byte b : hash) {
            hexString.append(String.format("%02x", b));
          }

          machineId = hexString.toString();

        } catch (Exception e) {
          LOG.error("Failed to generate machine ID", e);
        }
      }
      if (machineId == null) {
        machineId = "Generic";
      }
    }
    return machineId;
  }
}
