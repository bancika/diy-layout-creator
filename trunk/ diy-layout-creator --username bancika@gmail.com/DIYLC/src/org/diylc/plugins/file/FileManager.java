package org.diylc.plugins.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractAction;

import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.gui.DialogFactory;
import org.diylc.images.IconLoader;

import com.diyfever.gui.IDrawingProvider;
import com.diyfever.gui.export.DrawingExporter;

/**
 * Entry point class for File management utilities.
 * 
 * @author Branislav Stojkovic
 */
public class FileManager implements IPlugIn {

	private static final String FILE_TITLE = "File";
	private static final String TRACE_MASK_TITLE = "Trace Mask";

	private IPlugInPort plugInPort;
	private ProjectDrawingProvider drawingProvider;
	private TraceMaskDrawingProvider traceMaskDrawingProvider;

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;
		this.drawingProvider = new ProjectDrawingProvider(plugInPort);
		this.traceMaskDrawingProvider = new TraceMaskDrawingProvider(plugInPort);

		plugInPort.injectMenuAction(new NewAction(), FILE_TITLE);
		plugInPort.injectMenuAction(new OpenAction(), FILE_TITLE);
		plugInPort.injectMenuAction(new SaveAction(), FILE_TITLE);
		plugInPort.injectMenuAction(new SaveAsAction(), FILE_TITLE);
		plugInPort.injectMenuAction(null, FILE_TITLE);
		plugInPort.injectMenuAction(new ExportPDFAction(drawingProvider), FILE_TITLE);
		plugInPort.injectMenuAction(new ExportPNGAction(drawingProvider), FILE_TITLE);
		plugInPort.injectMenuAction(new PrintAction(drawingProvider), FILE_TITLE);
		plugInPort.injectSubmenu(TRACE_MASK_TITLE, IconLoader.TraceMask.getIcon(), FILE_TITLE);
		plugInPort
				.injectMenuAction(new ExportPDFAction(traceMaskDrawingProvider), TRACE_MASK_TITLE);
		plugInPort
				.injectMenuAction(new ExportPNGAction(traceMaskDrawingProvider), TRACE_MASK_TITLE);
		plugInPort.injectMenuAction(new PrintAction(traceMaskDrawingProvider), TRACE_MASK_TITLE);
		plugInPort.injectMenuAction(new CreateBomAction(), FILE_TITLE);
		plugInPort.injectMenuAction(null, FILE_TITLE);
		plugInPort.injectMenuAction(new ExitAction(), FILE_TITLE);
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
			if (!plugInPort.allowFileAction()) {
				return;
			}
			plugInPort.createNewProject();
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
			if (!plugInPort.allowFileAction()) {
				return;
			}
			File file = DialogFactory.getInstance().showOpenDialog(FileFilterEnum.DIY.getFilter(),
					null, FileFilterEnum.DIY.getExtensions()[0], new ProjectPreview());
			if (file != null) {
				plugInPort.loadProjectFromFile(file.getAbsolutePath());
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
			if (plugInPort.getCurrentFileName() == null) {
				File file = DialogFactory.getInstance().showSaveDialog(
						FileFilterEnum.DIY.getFilter(), null,
						FileFilterEnum.DIY.getExtensions()[0], new ProjectPreview());
				if (file != null) {
					plugInPort.saveProjectToFile(file.getAbsolutePath());
				}
			} else {
				plugInPort.saveProjectToFile(plugInPort.getCurrentFileName());
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
			File file = DialogFactory.getInstance().showSaveDialog(FileFilterEnum.DIY.getFilter(),
					null, FileFilterEnum.DIY.getExtensions()[0], new ProjectPreview());
			if (file != null) {
				plugInPort.saveProjectToFile(file.getAbsolutePath());
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
			File file = DialogFactory.getInstance().showSaveDialog(FileFilterEnum.PDF.getFilter(),
					null, FileFilterEnum.PDF.getExtensions()[0], null);
			if (file != null) {
				DrawingExporter.getInstance().exportPDF(this.drawingProvider, file);
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
			File file = DialogFactory.getInstance().showSaveDialog(FileFilterEnum.PNG.getFilter(),
					null, FileFilterEnum.PNG.getExtensions()[0], null);
			if (file != null) {
				DrawingExporter.getInstance().exportPNG(this.drawingProvider, file);
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
			DrawingExporter.getInstance().print(this.drawingProvider);
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
