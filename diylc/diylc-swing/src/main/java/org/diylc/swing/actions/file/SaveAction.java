package org.diylc.swing.actions.file;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.utils.IconLoader;
import org.diylc.swing.plugins.file.FileFilterEnum;

public class SaveAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;

  public SaveAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    putValue(AbstractAction.NAME, "Save");
    putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    putValue(AbstractAction.SMALL_ICON, IconLoader.DiskBlue.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("SaveAction triggered");
    if (plugInPort.getCurrentFileName() == null) {
      final File file = DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(),
          FileFilterEnum.DIY.getFilter(), null, FileFilterEnum.DIY.getExtensions()[0], null);
      if (file != null) {
        swingUI.executeBackgroundTask(new ITask<Void>() {

          @Override
          public Void doInBackground() throws Exception {
            ActionFactory.LOG.debug("Saving to " + file.getAbsolutePath());
            plugInPort.saveProjectToFile(file.getAbsolutePath(), false);
            return null;
          }

          @Override
          public void complete(Void result) {}

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Could not save to file. " + e.getMessage(), "Error",
                ISwingUI.ERROR_MESSAGE);
          }
        }, true);
      }
    } else {
      swingUI.executeBackgroundTask(new ITask<Void>() {

        @Override
        public Void doInBackground() throws Exception {
          ActionFactory.LOG.debug("Saving to " + plugInPort.getCurrentFileName());
          plugInPort.saveProjectToFile(plugInPort.getCurrentFileName(), false);
          return null;
        }

        @Override
        public void complete(Void result) {}

        @Override
        public void failed(Exception e) {
          swingUI.showMessage("Could not save to file. " + e.getMessage(), "Error",
              ISwingUI.ERROR_MESSAGE);
        }
      }, true);
    }
  }
}