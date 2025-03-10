package org.diylc.swing.actions.edit;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.utils.IconLoader;

public class SaveAsBlockAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;

  public SaveAsBlockAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Save as Building Block");
    putValue(AbstractAction.SMALL_ICON, IconLoader.ComponentAdd.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Save as building block triggered");
    String templateName = JOptionPane.showInputDialog(null, "Block name:",
        "Save as Building Block", JOptionPane.PLAIN_MESSAGE);
    if (templateName != null && !templateName.trim().isEmpty()) {
      plugInPort.saveSelectionAsBlock(templateName);
    }
  }
}