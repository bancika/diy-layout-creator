package com.diyfever.diylc.plugins.online;

import java.awt.event.ActionEvent;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.SwingWorker;

import com.diyfever.diylc.common.EventType;
import com.diyfever.diylc.common.IPlugIn;
import com.diyfever.diylc.common.IPlugInPort;
import com.diyfever.diylc.gui.DialogFactory;
import com.diyfever.diylc.images.IconLoader;
import com.diyfever.diylc.plugins.online.presenter.LibraryPresenter;
import com.diyfever.diylc.plugins.online.view.LoginDialog;
import com.diyfever.diylc.plugins.online.view.NewUserDialog;
import com.diyfever.diylc.plugins.online.view.UploadDialog;
import com.diyfever.gui.ProgressDialog;

public class OnlineManager implements IPlugIn {

	private static final String ONLINE_TITLE = "Online";

	private IPlugInPort plugInPort;
	private LibraryPresenter libraryPresenter;

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;
		this.libraryPresenter = new LibraryPresenter();

		initialize();

		plugInPort.injectMenuAction(new LibraryAction(), ONLINE_TITLE);
		plugInPort.injectMenuAction(null, ONLINE_TITLE);
		plugInPort.injectMenuAction(new LoginAction(), ONLINE_TITLE);
		plugInPort.injectMenuAction(new CreateAccountAction(), ONLINE_TITLE);
		plugInPort.injectMenuAction(null, ONLINE_TITLE);
		plugInPort.injectMenuAction(new UploadAction(), ONLINE_TITLE);
		plugInPort.injectMenuAction(new ManageProjectsAction(), ONLINE_TITLE);
	}

	private void initialize() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				libraryPresenter.connectDb();
				return null;
			}

			@Override
			protected void done() {
				if (libraryPresenter.isLoggedIn()) {

				}
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
			putValue(AbstractAction.NAME, "Log in");
			putValue(AbstractAction.SMALL_ICON, IconLoader.IdCard.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LoginDialog dialog = DialogFactory.getInstance()
					.createLoginDialog();
			dialog.setVisible(true);
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
					.createNewUserDialog();
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
			UploadDialog dialog = DialogFactory.getInstance()
					.createUploadDialog();
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
			ProgressDialog dialog = DialogFactory.getInstance()
					.createProgressDialog("Test", new String[] {}, "Some text",
							false);
			dialog.setVisible(true);
		}
	}
}
