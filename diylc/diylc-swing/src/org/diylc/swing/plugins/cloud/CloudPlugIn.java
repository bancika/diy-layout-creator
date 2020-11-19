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
package org.diylc.swing.plugins.cloud;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.swing.images.IconLoader;
import org.diylc.plugins.cloud.presenter.CloudException;
import org.diylc.plugins.cloud.presenter.CloudPresenter;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.DummyView;
import org.diylc.swing.plugins.cloud.view.ChangePasswordDialog;
import org.diylc.swing.plugins.cloud.view.LoginDialog;
import org.diylc.swing.plugins.cloud.view.UploadDialog;
import org.diylc.swing.plugins.cloud.view.UserEditDialog;
import org.diylc.swing.plugins.cloud.view.browser.CloudBrowserFrame;
import org.diylc.swing.plugins.cloud.view.browser.UploadManagerFrame;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.ButtonDialog;

public class CloudPlugIn implements IPlugIn {

  private static final String ONLINE_TITLE = "Project Cloud";

  private final static Logger LOG = Logger.getLogger(CloudPlugIn.class);

  private ISwingUI swingUI;
  private IPlugInPort plugInPort;
  private IPlugInPort thumbnailPresenter;

  private LibraryAction libraryAction;

  private LoginAction loginAction;
  private LogOutAction logOutAction;
  private CreateAccountAction createAccountAction;
  private ManageAccountAction manageAccountAction;

  private UploadAction uploadAction;
  private ChangePasswordAction changePasswordAction;
  private ManageProjectsAction manageProjectsAction;

  private CloudBrowserFrame cloudBrowser;

  public CloudPlugIn(ISwingUI swingUI) {
    super();

    this.swingUI = swingUI;
    this.thumbnailPresenter = new Presenter(new DummyView(), InMemoryConfigurationManager.getInstance());

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
    swingUI.injectMenuAction(getLogOutAction(), ONLINE_TITLE);

    // default state
    getUploadAction().setEnabled(false);
    getManageProjectsAction().setEnabled(false);
    getLogOutAction().setEnabled(false);
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;

    initialize();
  }

  private void initialize() {
    this.swingUI.executeBackgroundTask(new ITask<Boolean>() {

      @Override
      public Boolean doInBackground() throws Exception {
        return CloudPresenter.Instance.tryLogInWithToken();
      }

      @Override
      public void failed(Exception e) {
        LOG.error("Error while trying to login using token");
      }

      @Override
      public void complete(Boolean result) {
        try {
          if (result)
            loggedIn();
        } catch (Exception e) {
          LOG.error("Error while trying to login with token", e);
        }
      }
    }, false);
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
    if (libraryAction == null) {
      libraryAction = new LibraryAction();
    }
    return libraryAction;
  }

  public LoginAction getLoginAction() {
    if (loginAction == null) {
      loginAction = new LoginAction();
    }
    return loginAction;
  }

  public LogOutAction getLogOutAction() {
    if (logOutAction == null) {
      logOutAction = new LogOutAction();
    }
    return logOutAction;
  }

  public CreateAccountAction getCreateAccountAction() {
    if (createAccountAction == null) {
      createAccountAction = new CreateAccountAction();
    }
    return createAccountAction;
  }

  public ManageAccountAction getManageAccountAction() {
    if (manageAccountAction == null) {
      manageAccountAction = new ManageAccountAction();
    }
    return manageAccountAction;
  }

  public UploadAction getUploadAction() {
    if (uploadAction == null) {
      uploadAction = new UploadAction();
    }
    return uploadAction;
  }

  public ChangePasswordAction getChangePasswordAction() {
    if (changePasswordAction == null) {
      changePasswordAction = new ChangePasswordAction();
    }
    return changePasswordAction;
  }

  public ManageProjectsAction getManageProjectsAction() {
    if (manageProjectsAction == null) {
      manageProjectsAction = new ManageProjectsAction();
    }
    return manageProjectsAction;
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return null;
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {}

  class LibraryAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public LibraryAction() {
      super();
      putValue(AbstractAction.NAME, "Search The Cloud");
      putValue(AbstractAction.SMALL_ICON, IconLoader.Cloud.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      getCloudBrowser().setVisible(true);
      getCloudBrowser().requestFocus();
    }
  }

  class LoginAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public LoginAction() {
      super();
      putValue(AbstractAction.NAME, "Log In");
      putValue(AbstractAction.SMALL_ICON, IconLoader.IdCard.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LoginDialog dialog = DialogFactory.getInstance().createLoginDialog();
      do {
        dialog.setVisible(true);
        if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
          try {
            if (CloudPresenter.Instance.logIn(dialog.getUserName(), dialog.getPassword())) {
              swingUI
                  .showMessage(
                      "You have successfully logged into the system. You will remain logged in from this machine until logged out.",
                      "Login Successful", IView.INFORMATION_MESSAGE);
              loggedIn();
              break;
            } else {
              swingUI.showMessage(
                  "Could not login. Possible reasons are wrong credentials or lack of internet connection.",
                  "Login Error", IView.ERROR_MESSAGE);
            }
          } catch (CloudException e1) {
            swingUI.showMessage("Could not login. Error: " + e1.getMessage(), "Login Error", IView.ERROR_MESSAGE);
          }
        } else
          break;
      } while (true);
    }
  }

  class LogOutAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public LogOutAction() {
      super();
      putValue(AbstractAction.NAME, "Log Out");
      putValue(AbstractAction.SMALL_ICON, IconLoader.IdCard.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      CloudPresenter.Instance.logOut();
      loggedOut();
    }
  }

  class CreateAccountAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public CreateAccountAction() {
      super();
      putValue(AbstractAction.NAME, "Create New Account");
      putValue(AbstractAction.SMALL_ICON, IconLoader.IdCardAdd.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final UserEditDialog dialog = DialogFactory.getInstance().createUserEditDialog(null);
      dialog.setVisible(true);
      if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
        swingUI.executeBackgroundTask(new ITask<Void>() {

          @Override
          public Void doInBackground() throws Exception {
            CloudPresenter.Instance.createUserAccount(dialog.getUserName(), dialog.getPassword(), dialog.getEmail(),
                dialog.getWebsite(), dialog.getBio());
            return null;
          }

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Failed to create the account. Error: " + e.getMessage(), "Cloud Error",
                IView.ERROR_MESSAGE);
          }

          @Override
          public void complete(Void result) {
            swingUI.showMessage("Cloud account created successfully.", "Cloud", IView.INFORMATION_MESSAGE);
          }
        }, true);
      }
    }
  }

  class ManageAccountAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public ManageAccountAction() {
      super();
      putValue(AbstractAction.NAME, "Manage Account");
      putValue(AbstractAction.SMALL_ICON, IconLoader.IdCardEdit.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        final UserEditDialog dialog =
            DialogFactory.getInstance().createUserEditDialog(CloudPresenter.Instance.getUserDetails());
        dialog.setVisible(true);
        if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
          swingUI.executeBackgroundTask(new ITask<Void>() {

            @Override
            public Void doInBackground() throws Exception {
              CloudPresenter.Instance.updateUserDetails(dialog.getEmail(), dialog.getWebsite(), dialog.getBio());
              return null;
            }

            @Override
            public void failed(Exception e) {
              swingUI.showMessage("Failed to update the account. Error: " + e.getMessage(), "Cloud Error",
                  IView.ERROR_MESSAGE);
            }

            @Override
            public void complete(Void result) {
              swingUI.showMessage("Cloud account updated successfully.", "Cloud", IView.INFORMATION_MESSAGE);
            }
          }, true);
        }
      } catch (CloudException e1) {
        swingUI.showMessage("Failed to retreive user details from the server. Error: " + e1.getMessage(),
            "Cloud Error", IView.ERROR_MESSAGE);
      }
    }
  }

  class ChangePasswordAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public ChangePasswordAction() {
      super();
      putValue(AbstractAction.NAME, "Change Password");
      putValue(AbstractAction.SMALL_ICON, IconLoader.KeyEdit.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final ChangePasswordDialog dialog = DialogFactory.getInstance().createChangePasswordDialog();
      dialog.setVisible(true);
      if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
        swingUI.executeBackgroundTask(new ITask<Void>() {

          @Override
          public Void doInBackground() throws Exception {
            CloudPresenter.Instance.updatePassword(dialog.getOldPassword(), dialog.getNewPassword());
            return null;
          }

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Failed to update the password. Error: " + e.getMessage(), "Cloud Error",
                IView.ERROR_MESSAGE);
          }

          @Override
          public void complete(Void result) {
            swingUI.showMessage("Password updated.", "Cloud", IView.INFORMATION_MESSAGE);
          }
        }, true);
      }
    }
  }

  class UploadAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public UploadAction() {
      super();
      putValue(AbstractAction.NAME, "Upload A Project");
      putValue(AbstractAction.SMALL_ICON, IconLoader.CloudUp.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info("UploadAction triggered");

      final File[] files =
          DialogFactory.getInstance().showOpenMultiDialog(FileFilterEnum.DIY.getFilter(), null,
              FileFilterEnum.DIY.getExtensions()[0], null, swingUI.getOwnerFrame());
      if (files != null && files.length > 0) {
        List<ITask<String[]>> tasks = new ArrayList<ITask<String[]>>();
        final ListIterator<ITask<String[]>> taskIterator = tasks.listIterator();

        for (final File file : files) {
          taskIterator.add(new ITask<String[]>() {

            @Override
            public String[] doInBackground() throws Exception {
              LOG.debug("Uploading from " + file.getAbsolutePath());
              thumbnailPresenter.loadProjectFromFile(file.getAbsolutePath());
              return CloudPresenter.Instance.getCategories();
            }

            @Override
            public void complete(final String[] result) {
              final UploadDialog dialog =
                  DialogFactory.getInstance().createUploadDialog(swingUI.getOwnerFrame(), thumbnailPresenter, result,
                      false);
              dialog.setVisible(true);
              if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
                try {
                  final File thumbnailFile = File.createTempFile("upload-thumbnail", ".png");
                  if (ImageIO.write(dialog.getThumbnail(), "png", thumbnailFile)) {
                    swingUI.executeBackgroundTask(new ITask<Void>() {

                      @Override
                      public Void doInBackground() throws Exception {
                        CloudPresenter.Instance.uploadProject(dialog.getName(), dialog.getCategory(), dialog
                            .getDescription(), dialog.getKeywords(), plugInPort.getCurrentVersionNumber().toString(),
                            thumbnailFile, file, null);
                        return null;
                      }

                      @Override
                      public void failed(Exception e) {
                        swingUI.showMessage(e.getMessage(), "Upload Error", IView.ERROR_MESSAGE);
                      }

                      @Override
                      public void complete(Void result) {
                        swingUI
                            .showMessage(
                                "The project has been uploaded to the cloud successfully. Thank you for your contribution!",
                                "Upload Success", IView.INFORMATION_MESSAGE);

                        synchronized (taskIterator) {
                          if (taskIterator.hasPrevious())
                            swingUI.executeBackgroundTask(taskIterator.previous(), true);
                        }
                      }
                    }, true);
                  } else {
                    swingUI.showMessage("Could not prepare temporary files to be uploaded to the cloud.",
                        "Upload Error", IView.ERROR_MESSAGE);
                  }
                } catch (Exception e) {
                  swingUI.showMessage(e.getMessage(), "Upload Error", IView.ERROR_MESSAGE);
                }
              }
            }

            @Override
            public void failed(Exception e) {
              swingUI.showMessage("Could not open file. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
            }
          });
        }        

        synchronized (taskIterator) {
          if (taskIterator.hasPrevious())
            swingUI.executeBackgroundTask(taskIterator.previous(), true);
        }
      }
    }
  }

  class ManageProjectsAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public ManageProjectsAction() {
      super();
      putValue(AbstractAction.NAME, "Manage My Uploads");
      putValue(AbstractAction.SMALL_ICON, IconLoader.CloudGear.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      UploadManagerFrame frame = createUploadManagerFrame();
      frame.setVisible(true);
    }
  }

  public void loggedIn() {
    getLoginAction().setEnabled(false);
    getCreateAccountAction().setEnabled(false);

    getLogOutAction().setEnabled(true);
    getManageAccountAction().setEnabled(true);
    getUploadAction().setEnabled(true);
    getManageProjectsAction().setEnabled(true);
  }

  public void loggedOut() {
    getLoginAction().setEnabled(true);
    getCreateAccountAction().setEnabled(true);

    getLogOutAction().setEnabled(false);
    getManageAccountAction().setEnabled(false);
    getUploadAction().setEnabled(false);
    getManageProjectsAction().setEnabled(false);
  }
}
