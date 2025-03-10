package org.diylc.swing.actions;

import java.awt.Graphics2D;
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

public class ExportGerberAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private ISwingUI swingUI;
  private IPlugInPort plugInPort;

  public ExportGerberAction(IPlugInPort plugInPort,
      ISwingUI swingUI) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    putValue(AbstractAction.NAME, "Export to Gerber");
    putValue(AbstractAction.SMALL_ICON, IconLoader.DocumentX2.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("ExportGerberAction triggered");

    File initialFile = null;
    String currentFile = plugInPort.getCurrentFileName();
    if (currentFile != null) {
      File cFile = new File(currentFile);
      initialFile =
          new File(cFile.getName().replaceAll("(?i)\\.diy", ""));
    }

    final File file = DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(),
        FileFilterEnum.ALL_FILES.getFilter(), initialFile, "", null);
    if (file != null) {
      swingUI.executeBackgroundTask(new ITask<Void>() {

        @Override
        public Void doInBackground() throws Exception {
          ActionFactory.LOG.debug("Exporting to " + file.getAbsolutePath());
          plugInPort.exportToGerber(file.getAbsolutePath(), (Graphics2D) swingUI.getOwnerFrame().getGraphics());
          return null;
        }

        @Override
        public void complete(Void result) {}

        @Override
        public void failed(Exception e) {
          swingUI.showMessage("Could not export to Gerber file. " + e.getMessage(), "Error",
              ISwingUI.ERROR_MESSAGE);
        }
      }, true);
    }
  }
}