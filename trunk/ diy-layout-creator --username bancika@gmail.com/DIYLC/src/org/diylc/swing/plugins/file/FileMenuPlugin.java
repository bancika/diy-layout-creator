package org.diylc.swing.plugins.file;

import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.common.PropertyWrapper;
import org.diylc.images.IconLoader;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.editor.PropertyEditorDialog;

import com.diyfever.gui.ButtonDialog;
import com.diyfever.gui.IDrawingProvider;
import com.diyfever.gui.export.DrawingExporter;

/**
 * Entry point class for File management utilities.
 * 
 * @author Branislav Stojkovic
 */
public class FileMenuPlugin implements IPlugIn {

	private static final Logger LOG = Logger.getLogger(FileMenuPlugin.class);

	private static final String FILE_TITLE = "File";
	private static final String TRACE_MASK_TITLE = "Trace Mask";

	private IPlugInPort plugInPort;
	private ProjectDrawingProvider drawingProvider;
	private TraceMaskDrawingProvider traceMaskDrawingProvider;

	private ISwingUI swingUI;

	public FileMenuPlugin(ISwingUI swingUI) {
		super();
		this.swingUI = swingUI;
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;
		this.drawingProvider = new ProjectDrawingProvider(plugInPort, false);
		this.traceMaskDrawingProvider = new TraceMaskDrawingProvider(plugInPort);

		swingUI.injectMenuAction(new NewAction(), FILE_TITLE);
		swingUI.injectMenuAction(new OpenAction(), FILE_TITLE);
		swingUI.injectMenuAction(new SaveAction(), FILE_TITLE);
		swingUI.injectMenuAction(new SaveAsAction(), FILE_TITLE);
		swingUI.injectMenuAction(null, FILE_TITLE);
		swingUI.injectMenuAction(new ExportPDFAction(drawingProvider), FILE_TITLE);
		swingUI.injectMenuAction(new ExportPNGAction(drawingProvider), FILE_TITLE);
		swingUI.injectMenuAction(new PrintAction(drawingProvider), FILE_TITLE);
		swingUI.injectSubmenu(TRACE_MASK_TITLE, IconLoader.TraceMask.getIcon(), FILE_TITLE);
		swingUI.injectMenuAction(new ExportPDFAction(traceMaskDrawingProvider), TRACE_MASK_TITLE);
		swingUI.injectMenuAction(new ExportPNGAction(traceMaskDrawingProvider), TRACE_MASK_TITLE);
		swingUI.injectMenuAction(new PrintAction(traceMaskDrawingProvider), TRACE_MASK_TITLE);
		swingUI.injectMenuAction(new CreateBomAction(), FILE_TITLE);
		swingUI.injectMenuAction(null, FILE_TITLE);
		swingUI.injectMenuAction(new ExitAction(), FILE_TITLE);
	}

	class NewAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public NewAction() {
			super();
			putValue(AbstractAction.NAME, "New");
			putValue(AbstractAction.SMALL_ICON, IconLoader.DocumentPlainYellow.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("NewAction triggered");
			if (!plugInPort.allowFileAction()) {
				return;
			}
			plugInPort.createNewProject();
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
					plugInPort.setProjectDefaultPropertyValue(property.getName(), property
							.getValue());
				}
			}
		}
	}

	class OpenAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public OpenAction() {
			super();
			putValue(AbstractAction.NAME, "Open");
			putValue(AbstractAction.SMALL_ICON, IconLoader.FolderOut.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("OpenAction triggered");
			if (!plugInPort.allowFileAction()) {
				return;
			}
			final File file = DialogFactory.getInstance().showOpenDialog(
					FileFilterEnum.DIY.getFilter(), null, FileFilterEnum.DIY.getExtensions()[0],
					null);
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
						swingUI.showMessage("Could not open file. " + e.getMessage(), "Error",
								ISwingUI.ERROR_MESSAGE);
					}
				});
			}
		}
	}

	class SaveAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SaveAction() {
			super();
			putValue(AbstractAction.NAME, "Save");
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
							plugInPort.saveProjectToFile(file.getAbsolutePath());
							return null;
						}

						@Override
						public void complete(Void result) {
						}

						@Override
						public void failed(Exception e) {
							swingUI.showMessage("Could not save to file. " + e.getMessage(),
									"Error", ISwingUI.ERROR_MESSAGE);
						}
					});
				}
			} else {
				swingUI.executeBackgroundTask(new ITask<Void>() {

					@Override
					public Void doInBackground() throws Exception {
						LOG.debug("Saving to " + plugInPort.getCurrentFileName());
						plugInPort.saveProjectToFile(plugInPort.getCurrentFileName());
						return null;
					}

					@Override
					public void complete(Void result) {
					}

					@Override
					public void failed(Exception e) {
						swingUI.showMessage("Could not save to file. " + e.getMessage(), "Error",
								ISwingUI.ERROR_MESSAGE);
					}
				});
			}
		}
	}

	class SaveAsAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SaveAsAction() {
			super();
			putValue(AbstractAction.NAME, "Save As");
			putValue(AbstractAction.SMALL_ICON, IconLoader.DiskBlue.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("SaveAsAction triggered");
			final File file = DialogFactory.getInstance().showSaveDialog(
					FileFilterEnum.DIY.getFilter(), null, FileFilterEnum.DIY.getExtensions()[0],
					null);
			if (file != null) {
				swingUI.executeBackgroundTask(new ITask<Void>() {

					@Override
					public Void doInBackground() throws Exception {
						LOG.debug("Saving to " + file.getAbsolutePath());
						plugInPort.saveProjectToFile(file.getAbsolutePath());
						return null;
					}

					@Override
					public void complete(Void result) {
					}

					@Override
					public void failed(Exception e) {
						swingUI.showMessage("Could not save to file. " + e.getMessage(), "Error",
								ISwingUI.ERROR_MESSAGE);
					}
				});
			}
		}
	}

	class CreateBomAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public CreateBomAction() {
			super();
			putValue(AbstractAction.NAME, "Create B.O.M.");
			putValue(AbstractAction.SMALL_ICON, IconLoader.BOM.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("CreateBomAction triggered");
			List<BomEntry> bom = BomMaker.getInstance().createBom(
					plugInPort.getCurrentProject().getComponents());
			BomDialog dialog = DialogFactory.getInstance().createBomDialog(bom);
			dialog.setVisible(true);
		}
	}

	class ExportPDFAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IDrawingProvider drawingProvider;

		public ExportPDFAction(IDrawingProvider drawingProvider) {
			super();
			this.drawingProvider = drawingProvider;
			putValue(AbstractAction.NAME, "Export to PDF");
			putValue(AbstractAction.SMALL_ICON, IconLoader.PDF.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("ExportPDFAction triggered");
			final File file = DialogFactory.getInstance().showSaveDialog(
					FileFilterEnum.PDF.getFilter(), null, FileFilterEnum.PDF.getExtensions()[0],
					null);
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
						swingUI.showMessage("Could not export to PDF. " + e.getMessage(), "Error",
								ISwingUI.ERROR_MESSAGE);
					}
				});
			}
		}
	}

	class ExportPNGAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IDrawingProvider drawingProvider;

		public ExportPNGAction(IDrawingProvider drawingProvider) {
			super();
			this.drawingProvider = drawingProvider;
			putValue(AbstractAction.NAME, "Export to PNG");
			putValue(AbstractAction.SMALL_ICON, IconLoader.Image.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("ExportPNGAction triggered");
			final File file = DialogFactory.getInstance().showSaveDialog(
					FileFilterEnum.PNG.getFilter(), null, FileFilterEnum.PNG.getExtensions()[0],
					null);
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
						swingUI.showMessage("Could not export to PNG. " + e.getMessage(), "Error",
								ISwingUI.ERROR_MESSAGE);
					}
				});
			}
		}
	}

	class PrintAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private IDrawingProvider drawingProvider;

		public PrintAction(IDrawingProvider drawingProvider) {
			super();
			this.drawingProvider = drawingProvider;
			putValue(AbstractAction.NAME, "Print...");
			putValue(AbstractAction.SMALL_ICON, IconLoader.Print.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LOG.info("ExportPNGAction triggered");
			try {
				DrawingExporter.getInstance().print(this.drawingProvider);
			} catch (PrinterException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	class ExitAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ExitAction() {
			super();
			putValue(AbstractAction.NAME, "Exit");
			putValue(AbstractAction.SMALL_ICON, IconLoader.Exit.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (plugInPort.allowFileAction()) {
				System.exit(0);
			}
		}
	}

	@Override
	public EnumSet<EventType> getSubscribedEventTypes() {
		return null;
	}

	@Override
	public void processMessage(EventType eventType, Object... params) {
		// TODO Auto-generated method stub

	}
}
