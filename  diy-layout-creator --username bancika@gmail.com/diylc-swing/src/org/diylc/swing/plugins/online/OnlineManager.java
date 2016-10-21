package org.diylc.swing.plugins.online;

import java.awt.event.ActionEvent;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.SwingWorker;

import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.images.IconLoader;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.online.presenter.OnlinePresenter;
import org.diylc.swing.plugins.online.view.LoginDialog;
import org.diylc.swing.plugins.online.view.NewUserDialog;
import org.diylc.swing.plugins.online.view.UploadDialog;
import org.diylc.swingframework.ProgressDialog;

public class OnlineManager implements IPlugIn {

	private static final String ONLINE_TITLE = "Project Cloud";

	private IPlugInPort plugInPort;
	private OnlinePresenter onlinePresenter;	

	public OnlineManager(ISwingUI swingUI) {
		super();
		
//		plugInPort.injectMenuAction(new LibraryAction(), ONLINE_TITLE);
//		plugInPort.injectMenuAction(null, ONLINE_TITLE);
		swingUI.injectMenuAction(new LoginAction(), ONLINE_TITLE);
		swingUI.injectMenuAction(new CreateAccountAction(), ONLINE_TITLE);
		swingUI.injectMenuAction(null, ONLINE_TITLE);
		swingUI.injectMenuAction(new UploadAction(), ONLINE_TITLE);
		swingUI.injectMenuAction(new ManageProjectsAction(), ONLINE_TITLE);
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;
		this.onlinePresenter = new OnlinePresenter();

		initialize();
	}

	private void initialize() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				//libraryPresenter.connectDb();
				return null;
			}

			@Override
			protected void done() {
				// if (libraryPresenter.isLoggedIn()) {
				//
				// }
			}
		};
		worker.execute();
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
			putValue(AbstractAction.NAME, "Browse Library");
			putValue(AbstractAction.SMALL_ICON, IconLoader.Chest.getIcon());
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
			LoginDialog dialog = DialogFactory.getInstance().createLoginDialog();
			dialog.setVisible(true);
			onlinePresenter.login(dialog.getName(), dialog.getPassword());
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
			NewUserDialog dialog = DialogFactory.getInstance().createNewUserDialog(null);
			dialog.setVisible(true);
		}
	}

	class UploadAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public UploadAction() {
			super();
			putValue(AbstractAction.NAME, "Upload Project");
			putValue(AbstractAction.SMALL_ICON, IconLoader.Upload.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			UploadDialog dialog = DialogFactory.getInstance().createUploadDialog();
			dialog.setVisible(true);
		}
	}

	class ManageProjectsAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ManageProjectsAction() {
			super();
			putValue(AbstractAction.NAME, "Manage Projects");
			putValue(AbstractAction.SMALL_ICON, IconLoader.Wrench.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			ProgressDialog dialog = DialogFactory.getInstance().createProgressDialog("Test",
					new String[] {}, "Some text", false);
			dialog.setVisible(true);
		}
	}
}
