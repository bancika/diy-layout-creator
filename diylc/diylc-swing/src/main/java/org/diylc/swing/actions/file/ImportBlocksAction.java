package org.diylc.swing.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.lang.LangUtil;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.file.FileFilterEnum;

public class ImportBlocksAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private ISwingUI swingUI;
  private IPlugInPort plugInPort;

  public ImportBlocksAction(ISwingUI swingUI, IPlugInPort plugInPort) {
    super();
    this.swingUI = swingUI;
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Import Building Blocks");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("ImportBlocksAction triggered");


    final File file = DialogFactory.getInstance().showOpenDialog(FileFilterEnum.XML.getFilter(),
        null, FileFilterEnum.XML.getExtensions()[0], null, swingUI.getOwnerFrame());

    if (file != null) {
      swingUI.executeBackgroundTask(new ITask<Integer>() {

        @Override
        public Integer doInBackground() throws Exception {
          return plugInPort.importBlocks(file.getAbsolutePath());
        }

        @Override
        public void complete(Integer result) {
          swingUI.showMessage(
              String.format(LangUtil.translate("%s building block(s) imported from \"%s\"."),
                  result, file.getName()),
              LangUtil.translate("Success"), ISwingUI.INFORMATION_MESSAGE);
        }

        @Override
        public void failed(Exception e) {
          swingUI.showMessage(
              String.format(LangUtil.translate("Could not import building blocks: %s"),
                  e.getMessage()),
              LangUtil.translate("Error"),
              ISwingUI.ERROR_MESSAGE);
        }
      }, true);
    }
  }
}