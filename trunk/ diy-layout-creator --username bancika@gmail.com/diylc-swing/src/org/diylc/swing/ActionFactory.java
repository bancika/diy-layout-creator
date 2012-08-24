package org.diylc.swing;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.ExpansionMode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.core.Theme;
import org.diylc.images.IconLoader;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.editor.PropertyEditorDialog;
import org.diylc.swing.plugins.edit.ComponentTransferable;
import org.diylc.swing.plugins.file.BomDialog;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.ButtonDialog;
import org.diylc.swingframework.IDrawingProvider;
import org.diylc.swingframework.export.DrawingExporter;
import org.diylc.utils.BomEntry;

public class ActionFactory {

	private static final Logger LOG = Logger.getLogger(ActionFactory.class);

	private static ActionFactory instance;

	public static ActionFactory getInstance() {
		if (instance == null) {
			instance = new ActionFactory();
		}
		return instance;
	}

	private ActionFactory() {
	}

	// File menu actions.

	public NewAction createNewAction(IPlugInPort plugInPort) {
		return new NewAction(plugInPort);
	}

	public OpenAction createOpenAction(IPlugInPort plugInPort, ISwingUI swingUI) {
		return new OpenAction(plugInPort, swingUI);
	}

	public SaveAction createSaveAction(IPlugInPort plugInPort, ISwingUI swingUI) {
		return new SaveAction(plugInPort, swingUI);
	}

	public SaveAsAction createSaveAsAction(IPlugInPort plugInPort,
			ISwingUI swingUI) {
		return new SaveAsAction(plugInPort, swingUI);
	}

	public CreateBomAction createBomAction(IPlugInPort plugInPort) {
		return new CreateBomAction(plugInPort);
	}

	public ExportPDFAction createExportPDFAction(
			IDrawingProvider drawingProvider, ISwingUI swingUI) {
		return new ExportPDFAction(drawingProvider, swingUI);
	}

	public ExportPNGAction createExportPNGAction(
			IDrawingProvider drawingProvider, ISwingUI swingUI) {
		return new ExportPNGAction(drawingProvider, swingUI);
	}

	public PrintAction createPrintAction(IDrawingProvider drawingProvider) {
		return new PrintAction(drawingProvider);
	}

	public ExitAction createExitAction(IPlugInPort plugInPort) {
		return new ExitAction(plugInPort);
	}

	// Edit menu actions.

	public CutAction createCutAction(IPlugInPort plugInPort,
			Clipboard clipboard, ClipboardOwner clipboardOwner) {
		return new CutAction(plugInPort, clipboard, clipboardOwner);
	}

	public CopyAction createCopyAction(IPlugInPort plugInPort,
			Clipboard clipboard, ClipboardOwner clipboardOwner) {
		return new CopyAction(plugInPort, clipboard, clipboardOwner);
	}

	public PasteAction createPasteAction(IPlugInPort plugInPort,
			Clipboard clipboard) {
		return new PasteAction(plugInPort, clipboard);
	}

	public SelectAllAction createSelectAllAction(IPlugInPort plugInPort) {
		return new SelectAllAction(plugInPort);
	}

	public GroupAction createGroupAction(IPlugInPort plugInPort) {
		return new GroupAction(plugInPort);
	}

	public UngroupAction createUngroupAction(IPlugInPort plugInPort) {
		return new UngroupAction(plugInPort);
	}

	public EditProjectAction createEditProjectAction(IPlugInPort plugInPort) {
		return new EditProjectAction(plugInPort);
	}

	public EditSelectionAction createEditSelectionAction(IPlugInPort plugInPort) {
		return new EditSelectionAction(plugInPort);
	}

	public DeleteSelectionAction createDeleteSelectionAction(
			IPlugInPort plugInPort) {
		return new DeleteSelectionAction(plugInPort);
	}

	public ExpandSelectionAction createExpandSelectionAction(
			IPlugInPort plugInPort, ExpansionMode expansionMode) {
		return new ExpandSelectionAction(plugInPort, expansionMode);
	}

	public SendToBackAction createSendToBackAction(IPlugInPort plugInPort) {
		return new SendToBackAction(plugInPort);
	}

	public BringToFrontAction createBringToFrontAction(IPlugInPort plugInPort) {
		return new BringToFrontAction(plugInPort);
	}

	// Config actions.

	public ConfigAction createConfigAction(IPlugInPort plugInPort,
			String title, String configKey, boolean defaultValue) {
		return new ConfigAction(plugInPort, title, configKey, defaultValue);
	}

	public ThemeAction createThemeAction(IPlugInPort plugInPort, Theme theme) {
		return new ThemeAction(plugInPort, theme);
	}

	public RenumberAction createRenumberAction(IPlugInPort plugInPort,
			boolean xAxisFirst) {
		return new RenumberAction(plugInPort, xAxisFirst);
	}

	// File menu actions.

	public static class NewAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;

		public NewAction(IPlugInPort plugInPort) {
			super();
			this.plugInPort = plugInPort;
			putValue(AbstractAction.NAME, "New");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_N, ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.DocumentPlainYellow
					.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("NewAction triggered");
			if (!plugInPort.allowFileAction()) {
				return;
			}
			plugInPort.createNewProject();
			List<PropertyWrapper> properties = plugInPort
					.getProjectProperties();
			PropertyEditorDialog editor = DialogFactory.getInstance()
					.createPropertyEditorDialog(properties, "Edit Project");
			editor.setVisible(true);
			if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
				plugInPort.applyPropertiesToProject(properties);
			}
			// Save default values.
			for (PropertyWrapper property : editor.getDefaultedProperties()) {
				if (property.getValue() != null) {
					plugInPort.setProjectDefaultPropertyValue(property
							.getName(), property.getValue());
				}
			}
		}
	}

	public static class OpenAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;
		private ISwingUI swingUI;

		public OpenAction(IPlugInPort plugInPort, ISwingUI swingUI) {
			super();
			this.plugInPort = plugInPort;
			this.swingUI = swingUI;
			putValue(AbstractAction.NAME, "Open");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_O, ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.FolderOut.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("OpenAction triggered");
			if (!plugInPort.allowFileAction()) {
				return;
			}
			final File file = DialogFactory.getInstance().showOpenDialog(
					FileFilterEnum.DIY.getFilter(), null,
					FileFilterEnum.DIY.getExtensions()[0], null);
			if (file != null) {
				swingUI.executeBackgroundTask(new ITask<Void>() {

					@Override
					public Void doInBackground() throws Exception {
						LOG.debug("Opening from " + file.getAbsolutePath());
						plugInPort.loadProjectFromFile(file.getAbsolutePath());
						return null;
					}

					@Override
					public void complete(Void result) {
					}

					@Override
					public void failed(Exception e) {
						swingUI.showMessage("Could not open file. "
								+ e.getMessage(), "Error",
								ISwingUI.ERROR_MESSAGE);
					}
				});
			}
		}
	}

	public static class SaveAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;
		private ISwingUI swingUI;

		public SaveAction(IPlugInPort plugInPort, ISwingUI swingUI) {
			super();
			this.plugInPort = plugInPort;
			this.swingUI = swingUI;
			putValue(AbstractAction.NAME, "Save");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.DiskBlue.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("SaveAction triggered");
			if (plugInPort.getCurrentFileName() == null) {
				final File file = DialogFactory.getInstance().showSaveDialog(
						FileFilterEnum.DIY.getFilter(), null,
						FileFilterEnum.DIY.getExtensions()[0], null);
				if (file != null) {
					swingUI.executeBackgroundTask(new ITask<Void>() {

						@Override
						public Void doInBackground() throws Exception {
							LOG.debug("Saving to " + file.getAbsolutePath());
							plugInPort.saveProjectToFile(
									file.getAbsolutePath(), false);
							return null;
						}

						@Override
						public void complete(Void result) {
						}

						@Override
						public void failed(Exception e) {
							swingUI.showMessage("Could not save to file. "
									+ e.getMessage(), "Error",
									ISwingUI.ERROR_MESSAGE);
						}
					});
				}
			} else {
				swingUI.executeBackgroundTask(new ITask<Void>() {

					@Override
					public Void doInBackground() throws Exception {
						LOG.debug("Saving to "
								+ plugInPort.getCurrentFileName());
						plugInPort.saveProjectToFile(plugInPort
								.getCurrentFileName(), false);
						return null;
					}

					@Override
					public void complete(Void result) {
					}

					@Override
					public void failed(Exception e) {
						swingUI.showMessage("Could not save to file. "
								+ e.getMessage(), "Error",
								ISwingUI.ERROR_MESSAGE);
					}
				});
			}
		}
	}

	public static class SaveAsAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;
		private ISwingUI swingUI;

		public SaveAsAction(IPlugInPort plugInPort, ISwingUI swingUI) {
			super();
			this.plugInPort = plugInPort;
			this.swingUI = swingUI;
			putValue(AbstractAction.NAME, "Save As");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_S, ActionEvent.CTRL_MASK
							| ActionEvent.SHIFT_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.DiskBlue.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("SaveAsAction triggered");
			final File file = DialogFactory.getInstance().showSaveDialog(
					FileFilterEnum.DIY.getFilter(), null,
					FileFilterEnum.DIY.getExtensions()[0], null);
			if (file != null) {
				swingUI.executeBackgroundTask(new ITask<Void>() {

					@Override
					public Void doInBackground() throws Exception {
						LOG.debug("Saving to " + file.getAbsolutePath());
						plugInPort.saveProjectToFile(file.getAbsolutePath(),
								false);
						return null;
					}

					@Override
					public void complete(Void result) {
					}

					@Override
					public void failed(Exception e) {
						swingUI.showMessage("Could not save to file. "
								+ e.getMessage(), "Error",
								ISwingUI.ERROR_MESSAGE);
					}
				});
			}
		}
	}

	public static class CreateBomAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;

		public CreateBomAction(IPlugInPort plugInPort) {
			super();
			this.plugInPort = plugInPort;
			putValue(AbstractAction.NAME, "Create B.O.M.");
			putValue(AbstractAction.SMALL_ICON, IconLoader.BOM.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("CreateBomAction triggered");
			List<BomEntry> bom = org.diylc.utils.BomMaker.getInstance()
					.createBom(plugInPort.getCurrentProject().getComponents());
			BomDialog dialog = DialogFactory.getInstance().createBomDialog(bom);
			dialog.setVisible(true);
		}
	}

	public static class ExportPDFAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IDrawingProvider drawingProvider;
		private ISwingUI swingUI;

		public ExportPDFAction(IDrawingProvider drawingProvider,
				ISwingUI swingUI) {
			super();
			this.drawingProvider = drawingProvider;
			this.swingUI = swingUI;
			putValue(AbstractAction.NAME, "Export to PDF");
			putValue(AbstractAction.SMALL_ICON, IconLoader.PDF.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("ExportPDFAction triggered");
			final File file = DialogFactory.getInstance().showSaveDialog(
					FileFilterEnum.PDF.getFilter(), null,
					FileFilterEnum.PDF.getExtensions()[0], null);
			if (file != null) {
				swingUI.executeBackgroundTask(new ITask<Void>() {

					@Override
					public Void doInBackground() throws Exception {
						LOG.debug("Exporting to " + file.getAbsolutePath());
						DrawingExporter.getInstance().exportPDF(
								ExportPDFAction.this.drawingProvider, file);
						return null;
					}

					@Override
					public void complete(Void result) {
					}

					@Override
					public void failed(Exception e) {
						swingUI.showMessage("Could not export to PDF. "
								+ e.getMessage(), "Error",
								ISwingUI.ERROR_MESSAGE);
					}
				});
			}
		}
	}

	public static class ExportPNGAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IDrawingProvider drawingProvider;
		private ISwingUI swingUI;

		public ExportPNGAction(IDrawingProvider drawingProvider,
				ISwingUI swingUI) {
			super();
			this.drawingProvider = drawingProvider;
			this.swingUI = swingUI;
			putValue(AbstractAction.NAME, "Export to PNG");
			putValue(AbstractAction.SMALL_ICON, IconLoader.Image.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("ExportPNGAction triggered");
			final File file = DialogFactory.getInstance().showSaveDialog(
					FileFilterEnum.PNG.getFilter(), null,
					FileFilterEnum.PNG.getExtensions()[0], null);
			if (file != null) {
				swingUI.executeBackgroundTask(new ITask<Void>() {

					@Override
					public Void doInBackground() throws Exception {
						LOG.debug("Exporting to " + file.getAbsolutePath());
						DrawingExporter.getInstance().exportPNG(
								ExportPNGAction.this.drawingProvider, file);
						return null;
					}

					@Override
					public void complete(Void result) {
					}

					@Override
					public void failed(Exception e) {
						swingUI.showMessage("Could not export to PNG. "
								+ e.getMessage(), "Error",
								ISwingUI.ERROR_MESSAGE);
					}
				});
			}
		}
	}

	public static class PrintAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IDrawingProvider drawingProvider;

		public PrintAction(IDrawingProvider drawingProvider) {
			super();
			this.drawingProvider = drawingProvider;
			putValue(AbstractAction.NAME, "Print...");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_P, ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Print.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("PrintAction triggered");
			try {
				DrawingExporter.getInstance().print(this.drawingProvider);
			} catch (PrinterException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public static class ExitAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;

		public ExitAction(IPlugInPort plugInPort) {
			super();
			this.plugInPort = plugInPort;
			putValue(AbstractAction.NAME, "Exit");
			putValue(AbstractAction.SMALL_ICON, IconLoader.Exit.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("ExitAction triggered");
			if (plugInPort.allowFileAction()) {
				System.exit(0);
			}
		}
	}

	// Edit menu actions.

	public static class CutAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;
		private Clipboard clipboard;
		private ClipboardOwner clipboardOwner;

		public CutAction(IPlugInPort plugInPort, Clipboard clipboard,
				ClipboardOwner clipboardOwner) {
			super();
			this.plugInPort = plugInPort;
			this.clipboard = clipboard;
			this.clipboardOwner = clipboardOwner;
			putValue(AbstractAction.NAME, "Cut");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_X, ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Cut.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Cut triggered");
			clipboard.setContents(new ComponentTransferable(
					cloneComponents(plugInPort.getSelectedComponents())),
					clipboardOwner);
			plugInPort.deleteSelectedComponents();
		}
	}

	public static class CopyAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;
		private Clipboard clipboard;
		private ClipboardOwner clipboardOwner;

		public CopyAction(IPlugInPort plugInPort, Clipboard clipboard,
				ClipboardOwner clipboardOwner) {
			super();
			this.plugInPort = plugInPort;
			this.clipboard = clipboard;
			this.clipboardOwner = clipboardOwner;
			putValue(AbstractAction.NAME, "Copy");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_C, ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Copy.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Copy triggered");
			clipboard.setContents(new ComponentTransferable(
					cloneComponents(plugInPort.getSelectedComponents())),
					clipboardOwner);
		}
	}

	public static class PasteAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;
		private Clipboard clipboard;

		public PasteAction(IPlugInPort plugInPort, Clipboard clipboard) {
			super();
			this.plugInPort = plugInPort;
			this.clipboard = clipboard;
			putValue(AbstractAction.NAME, "Paste");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_V, ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Paste.getIcon());
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Paste triggered");
			try {
				List<IDIYComponent<?>> components = (List<IDIYComponent<?>>) clipboard
						.getData(ComponentTransferable.listFlavor);
				plugInPort.pasteComponents(cloneComponents(components));
			} catch (Exception ex) {
				LOG.error("Coule not paste.", ex);
			}
		}
	}

	private static List<IDIYComponent<?>> cloneComponents(
			List<IDIYComponent<?>> components) {
		List<IDIYComponent<?>> result = new ArrayList<IDIYComponent<?>>(
				components.size());
		for (IDIYComponent<?> component : components) {
			try {
				result.add(component.clone());
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	public static class SelectAllAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;

		public SelectAllAction(IPlugInPort plugInPort) {
			super();
			this.plugInPort = plugInPort;
			putValue(AbstractAction.NAME, "Select All");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_A, ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Selection.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Select All triggered");
			plugInPort.selectAll();
		}
	}

	public static class GroupAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;

		public GroupAction(IPlugInPort plugInPort) {
			super();
			this.plugInPort = plugInPort;
			putValue(AbstractAction.NAME, "Group Selection");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_G, ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Group.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Group Selection triggered");
			plugInPort.groupSelectedComponents();
		}
	}

	public static class UngroupAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;

		public UngroupAction(IPlugInPort plugInPort) {
			super();
			this.plugInPort = plugInPort;
			putValue(AbstractAction.NAME, "Ungroup Selection");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_U, ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Ungroup.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Ungroup Selection triggered");
			plugInPort.ungroupSelectedComponents();
		}
	}

	public static class EditProjectAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;

		public EditProjectAction(IPlugInPort plugInPort) {
			super();
			this.plugInPort = plugInPort;
			putValue(AbstractAction.NAME, "Edit Project");
			putValue(AbstractAction.SMALL_ICON, IconLoader.DocumentEdit
					.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Edit Project triggered");
			List<PropertyWrapper> properties = plugInPort
					.getProjectProperties();
			PropertyEditorDialog editor = DialogFactory.getInstance()
					.createPropertyEditorDialog(properties, "Edit Project");
			editor.setVisible(true);
			if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
				plugInPort.applyPropertiesToProject(properties);
			}
			// Save default values.
			for (PropertyWrapper property : editor.getDefaultedProperties()) {
				if (property.getValue() != null) {
					plugInPort.setProjectDefaultPropertyValue(property
							.getName(), property.getValue());
				}
			}
		}
	}

	public static class EditSelectionAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;

		public EditSelectionAction(IPlugInPort plugInPort) {
			super();
			this.plugInPort = plugInPort;
			putValue(AbstractAction.NAME, "Edit Selection");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_E, ActionEvent.CTRL_MASK));
			putValue(AbstractAction.SMALL_ICON, IconLoader.EditComponent
					.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Edit Selection triggered");
			List<PropertyWrapper> properties = plugInPort
					.getMutualSelectionProperties();
			if (properties == null || properties.isEmpty()) {
				LOG.info("Nothing to edit");
				return;
			}
			plugInPort.editSelection();
		}
	}

	public static class DeleteSelectionAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;

		public DeleteSelectionAction(IPlugInPort plugInPort) {
			super();
			this.plugInPort = plugInPort;
			putValue(AbstractAction.NAME, "Delete Selection");
			putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_DELETE, 0));
			putValue(AbstractAction.SMALL_ICON, IconLoader.Delete.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Delete Selection triggered");
			plugInPort.deleteSelectedComponents();
		}
	}

	public static class ExpandSelectionAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;
		private ExpansionMode expansionMode;

		public ExpandSelectionAction(IPlugInPort plugInPort,
				ExpansionMode expansionMode) {
			super();
			this.plugInPort = plugInPort;
			this.expansionMode = expansionMode;
			switch (expansionMode) {
			case ALL:
				putValue(AbstractAction.NAME, "All Connected");
				break;
			case IMMEDIATE:
				putValue(AbstractAction.NAME, "Immediate Only");
				break;
			case SAME_TYPE:
				putValue(AbstractAction.NAME, "Same Type Only");
				break;

			default:
				break;
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Expand Selection triggered: " + expansionMode);
			plugInPort.expandSelection(expansionMode);
		}
	}

	public static class SendToBackAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;

		public SendToBackAction(IPlugInPort plugInPort) {
			super();
			this.plugInPort = plugInPort;
			putValue(AbstractAction.NAME, "Send Backward");
			putValue(AbstractAction.SMALL_ICON, IconLoader.Back.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Send to Back triggered");
			plugInPort.sendSelectionToBack();
		}
	}

	public static class BringToFrontAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;

		public BringToFrontAction(IPlugInPort plugInPort) {
			super();
			this.plugInPort = plugInPort;
			putValue(AbstractAction.NAME, "Bring Forward");
			putValue(AbstractAction.SMALL_ICON, IconLoader.Front.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("Bring to Front triggered");
			plugInPort.bringSelectionToFront();
		}
	}

	public static class ConfigAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;
		private String configKey;

		public ConfigAction(IPlugInPort plugInPort, String title,
				String configKey, boolean defaultValue) {
			super();
			this.plugInPort = plugInPort;
			this.configKey = configKey;
			putValue(AbstractAction.NAME, title);
			putValue(IView.CHECK_BOX_MENU_ITEM, true);
			putValue(AbstractAction.SELECTED_KEY, ConfigurationManager
					.getInstance().readBoolean(configKey, defaultValue));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info(getValue(AbstractAction.NAME) + " triggered");
			ConfigurationManager.getInstance().writeValue(configKey,
					getValue(AbstractAction.SELECTED_KEY));
			plugInPort.refresh();
		}
	}

	public static class ThemeAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;
		private Theme theme;

		public ThemeAction(IPlugInPort plugInPort, Theme theme) {
			super();
			this.plugInPort = plugInPort;
			this.theme = theme;
			putValue(AbstractAction.NAME, theme.getName());
			putValue(IView.RADIO_BUTTON_GROUP_KEY, "theme");
			putValue(AbstractAction.SELECTED_KEY, plugInPort.getSelectedTheme()
					.getName().equals(theme.getName()));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info(getValue(AbstractAction.NAME) + " triggered");
			plugInPort.setSelectedTheme(theme);
		}
	}

	public static class RenumberAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IPlugInPort plugInPort;
		private boolean xAxisFirst;

		public RenumberAction(IPlugInPort plugInPort, boolean xAxisFirst) {
			super();
			this.plugInPort = plugInPort;
			this.xAxisFirst = xAxisFirst;
			putValue(AbstractAction.NAME, xAxisFirst ? "X Axis First"
					: "Y Axis First");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info(getValue(AbstractAction.NAME) + " triggered");
			plugInPort.renumberSelectedComponents(xAxisFirst);
		}
	}
}
