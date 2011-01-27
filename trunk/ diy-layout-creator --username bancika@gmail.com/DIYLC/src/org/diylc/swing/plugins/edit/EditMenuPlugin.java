package org.diylc.swing.plugins.edit;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.util.EnumSet;

import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.Project;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;

import com.diyfever.gui.undo.IUndoListener;
import com.diyfever.gui.undo.UndoHandler;

public class EditMenuPlugin implements IPlugIn, ClipboardOwner {

	private static final String EDIT_TITLE = "Edit";
	
	private IPlugInPort plugInPort;
	private ISwingUI swingUI;

	private Clipboard clipboard;

	private ActionFactory.CutAction cutAction;
	private ActionFactory.CopyAction copyAction;
	private ActionFactory.PasteAction pasteAction;
	private ActionFactory.EditSelectionAction editSelectionAction;
	private ActionFactory.DeleteSelectionAction deleteSelectionAction;
	private ActionFactory.GroupAction groupAction;
	private ActionFactory.UngroupAction ungroupAction;

	private UndoHandler<Project> undoHandler;

	public EditMenuPlugin(ISwingUI swingUI) {
		this.swingUI = swingUI;
		// SecurityManager securityManager = System.getSecurityManager();
		// if (securityManager != null) {
		// try {
		// securityManager.checkSystemClipboardAccess();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
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

	public ActionFactory.CutAction getCutAction() {
		if (cutAction == null) {
			cutAction = ActionFactory.getInstance().createCutAction(plugInPort, clipboard, this);
		}
		return cutAction;
	}

	public ActionFactory.CopyAction getCopyAction() {
		if (copyAction == null) {
			copyAction = ActionFactory.getInstance().createCopyAction(plugInPort, clipboard, this);
		}
		return copyAction;
	}

	public ActionFactory.PasteAction getPasteAction() {
		if (pasteAction == null) {
			pasteAction = ActionFactory.getInstance().createPasteAction(plugInPort, clipboard);
		}
		return pasteAction;
	}

	public ActionFactory.EditSelectionAction getEditSelectionAction() {
		if (editSelectionAction == null) {
			editSelectionAction = ActionFactory.getInstance().createEditSelectionAction(plugInPort);
		}
		return editSelectionAction;
	}

	public ActionFactory.DeleteSelectionAction getDeleteSelectionAction() {
		if (deleteSelectionAction == null) {
			deleteSelectionAction = ActionFactory.getInstance().createDeleteSelectionAction(
					plugInPort);
		}
		return deleteSelectionAction;
	}

	public ActionFactory.GroupAction getGroupAction() {
		if (groupAction == null) {
			groupAction = ActionFactory.getInstance().createGroupAction(plugInPort);
		}
		return groupAction;
	}

	public ActionFactory.UngroupAction getUngroupAction() {
		if (ungroupAction == null) {
			ungroupAction = ActionFactory.getInstance().createUngroupAction(plugInPort);
		}
		return ungroupAction;
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;
		
		ActionFactory actionFactory = ActionFactory.getInstance();

		swingUI.injectMenuAction(undoHandler.getUndoAction(), EDIT_TITLE);
		swingUI.injectMenuAction(undoHandler.getRedoAction(), EDIT_TITLE);
		swingUI.injectMenuAction(null, EDIT_TITLE);
		swingUI.injectMenuAction(getCutAction(), EDIT_TITLE);
		swingUI.injectMenuAction(getCopyAction(), EDIT_TITLE);
		swingUI.injectMenuAction(getPasteAction(), EDIT_TITLE);
		swingUI.injectMenuAction(null, EDIT_TITLE);
		swingUI.injectMenuAction(actionFactory.createSelectAllAction(plugInPort), EDIT_TITLE);
		swingUI.injectMenuAction(getEditSelectionAction(), EDIT_TITLE);
		swingUI.injectMenuAction(getDeleteSelectionAction(), EDIT_TITLE);
		swingUI.injectMenuAction(getGroupAction(), EDIT_TITLE);
		swingUI.injectMenuAction(getUngroupAction(), EDIT_TITLE);
		swingUI.injectMenuAction(null, EDIT_TITLE);
		swingUI.injectMenuAction(actionFactory.createEditProjectAction(plugInPort), EDIT_TITLE);
		
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
		getCutAction().setEnabled(enabled);
		getCopyAction().setEnabled(enabled);
		try {
			getPasteAction().setEnabled(
					clipboard.isDataFlavorAvailable(ComponentTransferable.listFlavor));
		} catch (Exception e) {
			getPasteAction().setEnabled(false);
		}
		getEditSelectionAction().setEnabled(enabled);
		getDeleteSelectionAction().setEnabled(enabled);
		getGroupAction().setEnabled(enabled);
		getUngroupAction().setEnabled(enabled);
	}

	// ClipboardOwner

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		refreshActions();
	}
}
