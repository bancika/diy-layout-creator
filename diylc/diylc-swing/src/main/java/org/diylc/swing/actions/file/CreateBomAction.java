package org.diylc.swing.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import javax.swing.AbstractAction;

import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.utils.IconLoader;
import org.diylc.swing.plugins.file.BomDialog;
import org.diylc.utils.BomEntry;

public class CreateBomAction extends AbstractAction {

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
    ActionFactory.LOG.info("CreateBomAction triggered");
    List<BomEntry> bom = org.diylc.utils.BomMaker.getInstance()
        .createBom(plugInPort.getCurrentProject().getComponents());

    String initialFileName = null;
    String currentFile = plugInPort.getCurrentFileName();
    if (currentFile != null) {
      File cFile = new File(currentFile);
      initialFileName = cFile.getName().replaceAll("(?i)\\.diy", "") + " BOM";
    }

    BomDialog dialog = DialogFactory.getInstance().createBomDialog(bom, initialFileName);
    dialog.setVisible(true);
  }
}