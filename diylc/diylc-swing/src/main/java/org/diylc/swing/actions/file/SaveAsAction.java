package org.diylc.swing.actions.file;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.utils.IconLoader;
import org.diylc.swing.plugins.file.FileFilterEnum;

public class SaveAsAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;

  public SaveAsAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    putValue(AbstractAction.NAME, "Save As");
    putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | ActionEvent.SHIFT_MASK));
    putValue(AbstractAction.SMALL_ICON, IconLoader.DiskBlue.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("SaveAsAction triggered");
    String currentFileName = plugInPort.getCurrentFileName();
    
    final File file = DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(),
        FileFilterEnum.DIY.getFilter(), 
        Optional.ofNullable(currentFileName).map(File::new).orElse(null), 
        FileFilterEnum.DIY.getExtensions()[0], null);
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
  }
}
