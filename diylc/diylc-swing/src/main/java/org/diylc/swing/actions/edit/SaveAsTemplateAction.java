package org.diylc.swing.actions.edit;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.utils.IconLoader;

public class SaveAsTemplateAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;

  public SaveAsTemplateAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Save as Variant");
    putValue(AbstractAction.SMALL_ICON, IconLoader.BriefcaseAdd.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Save as template triggered");
    String templateName = JOptionPane.showInputDialog(null, "Variant name:", "Save as Variant",
        JOptionPane.PLAIN_MESSAGE);
    if (templateName != null && !templateName.trim().isEmpty()) {
      plugInPort.saveSelectedComponentAsVariant(templateName);
    }
  }
}