package org.diylc.swing.actions.edit;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.diylc.common.IPlugInPort;
import org.diylc.core.ExpansionMode;
import org.diylc.swing.ActionFactory;

public class ExpandSelectionAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private ExpansionMode expansionMode;

  public ExpandSelectionAction(IPlugInPort plugInPort, ExpansionMode expansionMode) {
    super();
    this.plugInPort = plugInPort;
    this.expansionMode = expansionMode;
    switch (expansionMode) {
      case ALL:
        putValue(AbstractAction.NAME, "All Connected");
        break;
      case IMMEDIATE:
        putValue(AbstractAction.NAME, "Immediate Only");
        break;
      case SAME_TYPE:
        putValue(AbstractAction.NAME, "Same Type Only");
        break;

      default:
        break;
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Expand Selection triggered: " + expansionMode);
    plugInPort.expandSelection(expansionMode);
  }
}