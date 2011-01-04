package org.diylc.plugins.clipboard;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.diylc.common.ComponentSelection;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.images.IconLoader;
import org.diylc.model.IComponentInstance;
import org.diylc.model.Project;

import com.diyfever.gui.undo.IUndoListener;
import com.diyfever.gui.undo.UndoHandler;
import com.rits.cloning.Cloner;

public class ClipboardManager implements IPlugIn, ClipboardOwner {

	private static final Logger LOG = Logger.getLogger(ClipboardManager.class);

	private static final String EDIT_TITLE = "Edit";
	private IPlugInPort plugInPort;

	private Clipboard clipboard;
	private Cloner cloner;

	private CutAction cutAction;
	private CopyAction copyAction;
	private PasteAction pasteAction;

	private UndoHandler<Project> undoHandler;

	public ClipboardManager() {
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		cloner = new Cloner();
		undoHandler = new UndoHandler<Project>(new IUndoListener<Project>() {

			@Override
			public void actionPerformed(Project currentState) {
				plugInPort.loadProject(currentState, false);
			}
		});
		clipboard.addFlavorListener(new FlavorListener() {

			@Override
			public void flavorsChanged(FlavorEvent e) {
				refreshActions();
			}
		});
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;
		plugInPort.injectMenuAction(undoHandler.getUndoAction(), EDIT_TITLE);
		plugInPort.injectMenuAction(undoHandler.getRedoAction(), EDIT_TITLE);
		plugInPort.injectMenuAction(null, EDIT_TITLE);
		cutAction = new CutAction();
		plugInPort.injectMenuAction(cutAction, EDIT_TITLE);
		copyAction = new CopyAction();
		plugInPort.injectMenuAction(copyAction, EDIT_TITLE);
		pasteAction = new PasteAction();
		plugInPort.injectMenuAction(pasteAction, EDIT_TITLE);
		plugInPort.injectMenuAction(null, EDIT_TITLE);
		plugInPort.injectMenuAction(new SelectAllAction(), EDIT_TITLE);

		refreshActions();
	}

	@Override
	public EnumSet<EventType> getSubscribedEventTypes() {
		return EnumSet.of(EventType.SELECTION_CHANGED, EventType.PROJECT_MODIFIED);
	}

	@Override
	public void processMessage(EventType eventType, Object... params) {
		switch (eventType) {
		case SELECTION_CHANGED:
			refreshActions();
			break;
		case PROJECT_MODIFIED:
			undoHandler.stateChanged((Project) params[0], (Project) params[1], (String) params[2]);
			break;
		}
	}

	private void refreshActions() {
		boolean enabled = !plugInPort.getSelectedComponents().isEmpty();
		cutAction.setEnabled(enabled);
		copyAction.setEnabled(enabled);
		try {
			pasteAction.setEnabled(clipboard.isDataFlavorAvailable(ComponentSelection.listFlavor));
		} catch (Exception e) {
			pasteAction.setEnabled(false);
		}
	}

	class CutAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public CutAction() {
			super();
			putValue(AbstractAction.NAME, "Cut");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X,
					ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Cut.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Cut triggered");
		}
	}

	class CopyAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public CopyAction() {
			super();
			putValue(AbstractAction.NAME, "Copy");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C,
					ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Copy.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Copy triggered");
			clipboard.setContents(cloner.deepClone(plugInPort.getSelectedComponents()),
					ClipboardManager.this);
		}
	}

	class PasteAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public PasteAction() {
			super();
			putValue(AbstractAction.NAME, "Paste");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V,
					ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Paste.getIcon());
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Paste triggered");
			try {
				List<IComponentInstance> components = (List<IComponentInstance>) clipboard
						.getData(ComponentSelection.listFlavor);
				plugInPort.addComponents(cloner.deepClone(components), null);
			} catch (Exception ex) {
				LOG.error("Coule not paste.", ex);
			}
		}
	}

	class SelectAllAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SelectAllAction() {
			super();
			putValue(AbstractAction.NAME, "Select All");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A,
					ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Selection.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Select All triggered");
		}
	}

	// ClipboardOwner

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		refreshActions();
	}
}
