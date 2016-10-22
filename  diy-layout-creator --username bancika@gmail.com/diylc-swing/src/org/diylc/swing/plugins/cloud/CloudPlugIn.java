package org.diylc.swing.plugins.cloud;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.EnumSet;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.images.IconLoader;
import org.diylc.plugins.cloud.model.UserEntity;
import org.diylc.plugins.cloud.presenter.CloudException;
import org.diylc.plugins.cloud.presenter.CloudListener;
import org.diylc.plugins.cloud.presenter.CloudPresenter;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.DummyView;
import org.diylc.swing.plugins.cloud.view.ChangePasswordDialog;
import org.diylc.swing.plugins.cloud.view.CloudBrowserFrame;
import org.diylc.swing.plugins.cloud.view.LoginDialog;
import org.diylc.swing.plugins.cloud.view.UserEditDialog;
import org.diylc.swing.plugins.cloud.view.UploadDialog;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.ButtonDialog;
import org.diylc.swingframework.ProgressDialog;

public class CloudPlugIn implements IPlugIn, CloudListener {

	private static final String ONLINE_TITLE = "DIY Cloud";

	private final static Logger LOG = Logger.getLogger(CloudPlugIn.class);

	private ISwingUI swingUI;
	private IPlugInPort plugInPort;
	private IPlugInPort thumbnailPresenter;
	private CloudPresenter cloudPresenter;

	private LibraryAction libraryAction;

	private LoginAction loginAction;
	private LogOutAction logOutAction;
	private CreateAccountAction createAccountAction;
	private ManageAccountAction manageAccountAction;

	private UploadAction uploadAction;
	private ChangePasswordAction changePasswordAction;
	private ManageProjectsAction manageProjectsAction;

	public CloudPlugIn(ISwingUI swingUI) {
		super();

		this.swingUI = swingUI;
		this.thumbnailPresenter = new Presenter(new DummyView());

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
		this.cloudPresenter = new CloudPresenter(this);

		initialize();
	}

	private void initialize() {
		this.swingUI.executeBackgroundTask(new ITask<Boolean>() {

			@Override
			public Boolean doInBackground() throws Exception {
				return cloudPresenter.tryLogInWithToken();
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
		});
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
	public void processMessage(EventType eventType, Object... params) {
	}

	class LibraryAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public LibraryAction() {
			super();
			putValue(AbstractAction.NAME, "Browse The Cloud");
			putValue(AbstractAction.SMALL_ICON, IconLoader.Cloud.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			CloudBrowserFrame frame = DialogFactory.getInstance()
					.createCloudBrowserFrame(plugInPort, cloudPresenter);
			frame.setVisible(true);
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
			LoginDialog dialog = DialogFactory.getInstance()
					.createLoginDialog();
			do {
				dialog.setVisible(true);
				if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
					try {
						if (cloudPresenter.logIn(dialog.getUserName(),
								dialog.getPassword())) {
							swingUI.showMessage(
									"You have successfully logged into the system. You will remain logged in from this machine until logged out.",
									"Login Successful",
									IView.INFORMATION_MESSAGE);
							break;
						} else {
							swingUI.showMessage(
									"Could not login. Possible reasons are wrong credentials or lack of internet connection.",
									"Login Error", IView.ERROR_MESSAGE);
						}
					} catch (CloudException e1) {
						swingUI.showMessage(
								"Could not login. Error: " + e1.getMessage(),
								"Login Error", IView.ERROR_MESSAGE);
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
			cloudPresenter.logOut();
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
			final UserEditDialog dialog = DialogFactory.getInstance()
					.createUserEditDialog(null);
			dialog.setVisible(true);
			if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
				swingUI.executeBackgroundTask(new ITask<Void>() {

					@Override
					public Void doInBackground() throws Exception {
						cloudPresenter.createUserAccount(dialog.getUserName(),
								dialog.getPassword(), dialog.getEmail(),
								dialog.getWebsite(), dialog.getBio());
						return null;
					}

					@Override
					public void failed(Exception e) {
						swingUI.showMessage(
								"Failed to create the account. Error: "
										+ e.getMessage(), "Cloud Error",
								IView.ERROR_MESSAGE);
					}

					@Override
					public void complete(Void result) {
						swingUI.showMessage(
								"Cloud account created successfully.", "Cloud",
								IView.ERROR_MESSAGE);
					}
				});
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
				final UserEditDialog dialog = DialogFactory.getInstance()
						.createUserEditDialog(cloudPresenter.getUserDetails());
				dialog.setVisible(true);
				if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
					swingUI.executeBackgroundTask(new ITask<Void>() {

						@Override
						public Void doInBackground() throws Exception {
							cloudPresenter.updateUserDetails(dialog.getEmail(),
									dialog.getWebsite(), dialog.getBio());
							return null;
						}

						@Override
						public void failed(Exception e) {
							swingUI.showMessage(
									"Failed to update the account. Error: "
											+ e.getMessage(), "Cloud Error",
									IView.ERROR_MESSAGE);
						}

						@Override
						public void complete(Void result) {
							swingUI.showMessage(
									"Cloud account updated successfully.",
									"Cloud", IView.INFORMATION_MESSAGE);
						}
					});
				}
			} catch (CloudException e1) {
				swingUI.showMessage(
						"Failed to retreive user details from the server. Error: "
								+ e1.getMessage(), "Cloud Error",
						IView.ERROR_MESSAGE);
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
			final ChangePasswordDialog dialog = DialogFactory.getInstance()
					.createChangePasswordDialog();
			dialog.setVisible(true);
			if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
				swingUI.executeBackgroundTask(new ITask<Void>() {

					@Override
					public Void doInBackground() throws Exception {
						cloudPresenter.updatePassword(dialog.getOldPassword(),
								dialog.getNewPassword());
						return null;
					}

					@Override
					public void failed(Exception e) {
						swingUI.showMessage(
								"Failed to update the password. Error: "
										+ e.getMessage(), "Cloud Error",
								IView.ERROR_MESSAGE);
					}

					@Override
					public void complete(Void result) {
						swingUI.showMessage("Password updated.", "Cloud",
								IView.INFORMATION_MESSAGE);
					}
				});
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

			final File file = DialogFactory.getInstance().showOpenDialog(
					FileFilterEnum.DIY.getFilter(), null,
					FileFilterEnum.DIY.getExtensions()[0], null);
			if (file != null) {
				swingUI.executeBackgroundTask(new ITask<File>() {

					@Override
					public File doInBackground() throws Exception {
						LOG.debug("Uploading from " + file.getAbsolutePath());
						thumbnailPresenter.loadProjectFromFile(file
								.getAbsolutePath());
						return file;
					}

					@Override
					public void complete(File result) {
						UploadDialog dialog = DialogFactory.getInstance()
								.createUploadDialog(thumbnailPresenter,
										cloudPresenter);
						dialog.setVisible(true);
						if (ButtonDialog.OK.equals(dialog
								.getSelectedButtonCaption())) {
							try {
								File thumbnailFile = File.createTempFile(
										"upload-thumbnail", ".png");
								if (ImageIO.write(dialog.getThumbnail(), "png",
										thumbnailFile)) {
									cloudPresenter.upload(dialog.getName(),
											dialog.getCategory(), dialog
													.getDescription(), dialog
													.getKeywords(), plugInPort
													.getCurrentVersionNumber()
													.toString(), thumbnailFile,
											result);
									swingUI.showMessage(
											"The project has been uploaded to the cloud successfully. Thank you for your contribution!",
											"Upload Success",
											IView.INFORMATION_MESSAGE);
								} else {
									swingUI.showMessage(
											"Could not prepare temporary files to be uploaded to the cloud.",
											"Upload Error", IView.ERROR_MESSAGE);
								}
							} catch (Exception e) {
								swingUI.showMessage(e.getMessage(),
										"Upload Error", IView.ERROR_MESSAGE);
							}
						}
					}

					@Override
					public void failed(Exception e) {
						swingUI.showMessage(
								"Could not open file. " + e.getMessage(),
								"Error", ISwingUI.ERROR_MESSAGE);
					}
				});
			}
		}
	}

	class ManageProjectsAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ManageProjectsAction() {
			super();
			putValue(AbstractAction.NAME, "Manage My Projects");
			putValue(AbstractAction.SMALL_ICON, IconLoader.CloudGear.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			ProgressDialog dialog = DialogFactory.getInstance()
					.createProgressDialog("Test", new String[] {}, "Some text",
							false);
			dialog.setVisible(true);
		}
	}

	@Override
	public void loggedIn() {
		getLoginAction().setEnabled(false);
		getCreateAccountAction().setEnabled(false);

		getLogOutAction().setEnabled(true);
		getManageAccountAction().setEnabled(true);
		getUploadAction().setEnabled(true);
		getManageProjectsAction().setEnabled(true);
	}

	@Override
	public void loggedOut() {
		getLoginAction().setEnabled(true);
		getCreateAccountAction().setEnabled(true);

		getLogOutAction().setEnabled(false);
		getManageAccountAction().setEnabled(false);
		getUploadAction().setEnabled(false);
		getManageProjectsAction().setEnabled(false);
	}
}
