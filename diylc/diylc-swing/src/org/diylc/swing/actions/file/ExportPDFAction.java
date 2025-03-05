package org.diylc.swing.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.images.IconLoader;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.IDrawingProvider;
import org.diylc.swingframework.export.DrawingExporter;

public class ExportPDFAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IDrawingProvider drawingProvider;
  private ISwingUI swingUI;
  private IPlugInPort plugInPort;
  private String defaultSuffix;

  public ExportPDFAction(IPlugInPort plugInPort, IDrawingProvider drawingProvider,
      ISwingUI swingUI, String defaultSuffix) {
    super();
    this.plugInPort = plugInPort;
    this.drawingProvider = drawingProvider;
    this.swingUI = swingUI;
    this.defaultSuffix = defaultSuffix;
    putValue(AbstractAction.NAME, "Export to PDF");
    putValue(AbstractAction.SMALL_ICON, IconLoader.PDF.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("ExportPDFAction triggered");

    File initialFile = null;
    String currentFile = plugInPort.getCurrentFileName();
    if (currentFile != null) {
      File cFile = new File(currentFile);
      initialFile =
          new File(cFile.getName().replaceAll("(?i)\\.diy", "") + defaultSuffix + ".pdf");
    }

    final File file = DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(),
        FileFilterEnum.PDF.getFilter(), initialFile, FileFilterEnum.PDF.getExtensions()[0], null);
    if (file != null) {
      swingUI.executeBackgroundTask(new ITask<Void>() {

        @Override
        public Void doInBackground() throws Exception {
          ActionFactory.LOG.debug("Exporting to " + file.getAbsolutePath());
          DrawingExporter.getInstance().exportPDF(ExportPDFAction.this.drawingProvider, file);
          return null;
        }

        @Override
        public void complete(Void result) {}

        @Override
        public void failed(Exception e) {
          swingUI.showMessage("Could not export to PDF. " + e.getMessage(), "Error",
              ISwingUI.ERROR_MESSAGE);
        }
      }, true);
    }
  }
}