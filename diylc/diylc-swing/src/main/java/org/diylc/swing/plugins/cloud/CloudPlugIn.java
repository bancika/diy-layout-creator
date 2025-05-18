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
package org.diylc.swing.plugins.cloud;

import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.plugins.cloud.service.CloudService;

import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ISwingUI;
import org.diylc.common.DummyView;
import org.diylc.swing.plugins.cloud.view.browser.CloudBrowserFrame;
import org.diylc.swing.plugins.cloud.view.browser.UploadManagerFrame;
import org.diylc.swing.plugins.cloud.actions.*;

public class CloudPlugIn implements IPlugIn {

  private static final String ONLINE_TITLE = "Cloud";

  private final static Logger LOG = Logger.getLogger(CloudPlugIn.class);

  private ISwingUI swingUI;
  private IPlugInPort plugInPort;
  private IPlugInPort thumbnailPresenter;
  private ThumbnailGenerator thumbnailGenerator;
  private CloudService cloudService;

  private LibraryAction libraryAction;
  private LoginAction loginAction;
  private LogOutAction logOutAction;
  private CreateAccountAction createAccountAction;
  private ManageAccountAction manageAccountAction;
  private UploadAction uploadAction;
  private ChangePasswordAction changePasswordAction;
  private ManageProjectsAction manageProjectsAction;
  private ConnectPatreonAction connectPatreonAction;

  private CloudBrowserFrame cloudBrowser;

  public CloudPlugIn(ISwingUI swingUI) {
    super();

    this.swingUI = swingUI;
    this.thumbnailPresenter = new Presenter(new DummyView(), InMemoryConfigurationManager.getInstance());
    this.thumbnailGenerator = new ThumbnailGenerator(thumbnailPresenter);
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    this.cloudService = plugInPort.getCloudService();

    // Initialize actions
    this.libraryAction = new LibraryAction(this);
    this.loginAction = new LoginAction(this);
    this.logOutAction = new LogOutAction(this);
    this.createAccountAction = new CreateAccountAction(this);
    this.manageAccountAction = new ManageAccountAction(this);
    this.uploadAction = new UploadAction(this);
    this.changePasswordAction = new ChangePasswordAction(this);
    this.manageProjectsAction = new ManageProjectsAction(this);
    this.connectPatreonAction = new ConnectPatreonAction(plugInPort, swingUI);

    swingUI.injectMenuAction(getLibraryAction(), ONLINE_TITLE);
    swingUI.injectMenuAction(null, ONLINE_TITLE);
    swingUI.injectMenuAction(getLoginAction(), ONLINE_TITLE);
    swingUI.injectMenuAction(getCreateAccountAction(), ONLINE_TITLE);
    swingUI.injectMenuAction(null, ONLINE_TITLE);
    swingUI.injectMenuAction(getUploadAction(), ONLINE_TITLE);
    swingUI.injectMenuAction(getManageProjectsAction(), ONLINE_TITLE);
    swingUI.injectMenuAction(null, ONLINE_TITLE);
    swingUI.injectMenuAction(getManageAccountAction(), ONLINE_TITLE);
    swingUI.injectMenuAction(getChangePasswordAction(), ONLINE_TITLE);
    swingUI.injectMenuAction(getConnectPatreonAction(), ONLINE_TITLE);
    swingUI.injectMenuAction(getLogOutAction(), ONLINE_TITLE);

    // default state
    getUploadAction().setEnabled(false);
    getManageProjectsAction().setEnabled(false);
    getManageAccountAction().setEnabled(false);
    getChangePasswordAction().setEnabled(false);
    getLogOutAction().setEnabled(false);
  }

  public CloudBrowserFrame getCloudBrowser() {
    if (cloudBrowser == null) {
      cloudBrowser = new CloudBrowserFrame(swingUI, plugInPort);
    }
    return cloudBrowser;
  }

  public UploadManagerFrame createUploadManagerFrame() {
    return new UploadManagerFrame(swingUI, plugInPort);
  }

  public LibraryAction getLibraryAction() {
    return libraryAction;
  }

  public LoginAction getLoginAction() {
    return loginAction;
  }

  public LogOutAction getLogOutAction() {
    return logOutAction;
  }

  public CreateAccountAction getCreateAccountAction() {
    return createAccountAction;
  }

  public ManageAccountAction getManageAccountAction() {
    return manageAccountAction;
  }

  public ConnectPatreonAction getConnectPatreonAction() {
    return connectPatreonAction;
  }

  public UploadAction getUploadAction() {
    return uploadAction;
  }

  public ChangePasswordAction getChangePasswordAction() {
    return changePasswordAction;
  }

  public ManageProjectsAction getManageProjectsAction() {
    return manageProjectsAction;
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.CLOUD_LOGGED_IN, EventType.CLOUD_LOGGED_OUT);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    switch (eventType) {
      case CLOUD_LOGGED_IN:
        loggedIn();
        break;
      case CLOUD_LOGGED_OUT:
        loggedOut();
        break;
    }
  }

  public void loggedIn() {
    getLoginAction().setEnabled(false);
    getCreateAccountAction().setEnabled(false);

    getLogOutAction().setEnabled(true);
    getManageAccountAction().setEnabled(true);
    getUploadAction().setEnabled(true);
    getManageProjectsAction().setEnabled(true);
    getChangePasswordAction().setEnabled(true);
  }

  public void loggedOut() {
    getLoginAction().setEnabled(true);
    getCreateAccountAction().setEnabled(true);

    getLogOutAction().setEnabled(false);
    getManageAccountAction().setEnabled(false);
    getUploadAction().setEnabled(false);
    getManageProjectsAction().setEnabled(false);
    getChangePasswordAction().setEnabled(false);
  }

  // Getters for action classes
  public ISwingUI getSwingUI() {
    return swingUI;
  }

  public IPlugInPort getPlugInPort() {
    return plugInPort;
  }

  public IPlugInPort getThumbnailPresenter() {
    return thumbnailPresenter;
  }

  public ThumbnailGenerator getThumbnailGenerator() {
    return thumbnailGenerator;
  }

  public CloudService getCloudService() {
    return cloudService;
  }
}
