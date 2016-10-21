package org.diylc.swing.plugins.cloud;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.EnumSet;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.images.IconLoader;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.DummyView;
import org.diylc.swing.plugins.cloud.presenter.CloudListener;
import org.diylc.swing.plugins.cloud.presenter.CloudPresenter;
import org.diylc.swing.plugins.cloud.view.LoginDialog;
import org.diylc.swing.plugins.cloud.view.NewUserDialog;
import org.diylc.swing.plugins.cloud.view.UploadDialog;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.ButtonDialog;
import org.diylc.swingframework.ProgressDialog;

public class OnlineManager implements IPlugIn, CloudListener {

	private static final String ONLINE_TITLE = "DIY Cloud";

	private final static Logger LOG = Logger.getLogger(OnlineManager.class);

	private ISwingUI swingUI;
	private IPlugInPort plugInPort;
	private IPlugInPort thumbnailPresenter;
	private CloudPresenter onlinePresenter;

	private LibraryAction libraryAction;

	private LoginAction loginAction;
	private LogOutAction logOutAction;
	private CreateAccountAction createAccountAction;
	private ManageAccountAction manageAccountAction;

	private UploadAction uploadAction;
	private ManageProjectsAction manageProjectsAction;

	public OnlineManager(ISwingUI swingUI) {
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
		swingUI.injectMenuAction(getLogOutAction(), ONLINE_TITLE);		

		// default state
		getUploadAction().setEnabled(false);
		getManageProjectsAction().setEnabled(false);
		getLogOutAction().setEnabled(false);
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;
		this.onlinePresenter = new CloudPresenter(this);

		initialize();
	}

	private void initialize() {
		this.swingUI.executeBackgroundTask(new ITask<Boolean>() {

			@Override
			public Boolean doInBackground() throws Exception {
				return onlinePresenter.tryLogInWithToken();
			}

			@Override
			public void failed(Exception e) {
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
			uploadAction = new UploadAction(this.thumbnailPresenter,
					this.swingUI, this.onlinePresenter);
		}
		return uploadAction;
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
					if (onlinePresenter.logIn(dialog.getUserName(),
							dialog.getPassword())) {
						swingUI.showMessage(
								"You have successfully logged into the system. You will remain logged in on this machine until logged out.",
								"Login Successful", IView.INFORMATION_MESSAGE);
						break;
					} else {
						swingUI.showMessage(
								"Could not login. Possible reasons are wrong credentials or lack of internet connection.",
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
			onlinePresenter.logOut();
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
			NewUserDialog dialog = DialogFactory.getInstance()
					.createNewUserDialog(null);
			dialog.setVisible(true);
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
			NewUserDialog dialog = DialogFactory.getInstance()
					.createNewUserDialog(null);
			dialog.setVisible(true);
		}
	}

	class UploadAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;
		private ISwingUI swingUI;
		private CloudPresenter onlinePresenter;

		public UploadAction(IPlugInPort plugInPort, ISwingUI swingUI, CloudPresenter onlinePresenter) {
			super();
			this.plugInPort = plugInPort;
			this.swingUI = swingUI;
			this.onlinePresenter = onlinePresenter;
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
				swingUI.executeBackgroundTask(new ITask<Void>() {

					@Override
					public Void doInBackground() throws Exception {
						LOG.debug("Uploading from " + file.getAbsolutePath());
						plugInPort.loadProjectFromFile(file.getAbsolutePath());
						return null;
					}

					@Override
					public void complete(Void result) {
						UploadDialog dialog = DialogFactory.getInstance()
								.createUploadDialog(UploadAction.this.plugInPort, UploadAction.this.onlinePresenter);
						dialog.setVisible(true);
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
