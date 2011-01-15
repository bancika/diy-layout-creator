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
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.gui.DialogFactory;
import org.diylc.gui.editor.PropertyEditorDialog;
import org.diylc.images.IconLoader;

import com.diyfever.gui.ButtonDialog;
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
		plugInPort.injectMenuAction(null, EDIT_TITLE);
		plugInPort.injectMenuAction(new EditSelectionAction(), EDIT_TITLE);
		plugInPort.injectMenuAction(new EditProjectAction(), EDIT_TITLE);
		plugInPort.injectMenuAction(null, EDIT_TITLE);
		plugInPort.injectMenuAction(new GroupAction(), EDIT_TITLE);
		plugInPort.injectMenuAction(new UngroupAction(), EDIT_TITLE);

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
				List<IDIYComponent<?>> components = (List<IDIYComponent<?>>) clipboard
						.getData(ComponentSelection.listFlavor);
				plugInPort.pasteComponents(cloner.deepClone(components));
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
			plugInPort.selectAll();
		}
	}

	class GroupAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public GroupAction() {
			super();
			putValue(AbstractAction.NAME, "Group Selection");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G,
					ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Group.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Group Selection triggered");
			plugInPort.groupSelectedComponents();
		}
	}

	class UngroupAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public UngroupAction() {
			super();
			putValue(AbstractAction.NAME, "Ungroup Selection");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U,
					ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Ungroup.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Ungroup Selection triggered");
			plugInPort.ungroupSelectedComponents();
		}
	}

	class EditProjectAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public EditProjectAction() {
			super();
			putValue(AbstractAction.NAME, "Edit Project");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E,
					ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.DocumentEdit.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Edit Project triggered");
			List<PropertyWrapper> properties = plugInPort.getProjectProperties();
			PropertyEditorDialog editor = DialogFactory.getInstance().createPropertyEditorDialog(
					properties, "Edit Project");
			editor.setVisible(true);
			if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
				plugInPort.applyPropertiesToProject(properties);
			}
			// Save default values.
			for (PropertyWrapper property : editor.getDefaultedProperties()) {
				if (property.getValue() != null) {
					plugInPort.setProjectDefaultPropertyValue(property.getName(), property.getValue());
				}
			}
		}
	}

	class EditSelectionAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public EditSelectionAction() {
			super();
			putValue(AbstractAction.NAME, "Edit Selection");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E,
					ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.EditComponent.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Edit Selection triggered");
			List<PropertyWrapper> properties = plugInPort.getMutualSelectionProperties();
			if (properties == null || properties.isEmpty()) {
				LOG.info("Nothing to edit");
				return;
			}
			PropertyEditorDialog editor = DialogFactory.getInstance().createPropertyEditorDialog(
					properties, "Edit Selection");
			editor.setVisible(true);
			if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
				plugInPort.applyPropertiesToSelection(properties);
			}
			// Save default values.
			for (PropertyWrapper property : editor.getDefaultedProperties()) {
				if (property.getValue() != null) {
					plugInPort.setSelectionDefaultPropertyValue(property.getName(), property.getValue());
				}
			}
		}
	}

	// ClipboardOwner

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		refreshActions();
	}
}
