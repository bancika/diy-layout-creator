package org.diylc.swing.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;

import org.diylc.swingframework.IDrawingProvider;
import org.diylc.swingframework.export.DrawingExporter;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.images.IconLoader;
import org.diylc.swing.plugins.file.FileFilterEnum;

public class ExportPNGAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IDrawingProvider drawingProvider;
  private ISwingUI swingUI;
  private IPlugInPort plugInPort;
  private String defaultSuffix;

  public ExportPNGAction(IPlugInPort plugInPort, IDrawingProvider drawingProvider,
      ISwingUI swingUI, String defaultSuffix) {
    super();
    this.plugInPort = plugInPort;
    this.drawingProvider = drawingProvider;
    this.swingUI = swingUI;
    this.defaultSuffix = defaultSuffix;
    putValue(AbstractAction.NAME, "Export to PNG");
    putValue(AbstractAction.SMALL_ICON, IconLoader.Image.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("ExportPNGAction triggered");

    File initialFile = null;
    String currentFile = plugInPort.getCurrentFileName();
    if (currentFile != null) {
      File cFile = new File(currentFile);
      initialFile =
          new File(cFile.getName().replaceAll("(?i)\\.diy", "") + defaultSuffix + ".png");
    }

    final File file = DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(),
        FileFilterEnum.PNG.getFilter(), initialFile, FileFilterEnum.PNG.getExtensions()[0], null);
    if (file != null) {
      swingUI.executeBackgroundTask(new ITask<Void>() {

        @Override
        public Void doInBackground() throws Exception {
          ActionFactory.LOG.debug("Exporting to " + file.getAbsolutePath());
          DrawingExporter.getInstance().exportPNG(ExportPNGAction.this.drawingProvider, file);
          return null;
        }

        @Override
        public void complete(Void result) {}

        @Override
        public void failed(Exception e) {
          swingUI.showMessage("Could not export to PNG. " + e.getMessage(), "Error",
              ISwingUI.ERROR_MESSAGE);
        }
      }, true);
    }
  }
}