package org.diylc.swing.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;

public class RenumberAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private boolean xAxisFirst;

  public RenumberAction(IPlugInPort plugInPort, boolean xAxisFirst) {
    super();
    this.plugInPort = plugInPort;
    this.xAxisFirst = xAxisFirst;
    putValue(AbstractAction.NAME, xAxisFirst ? "Top-to-Bottom" : "Left-to-Right");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info(getValue(AbstractAction.NAME) + " triggered");
    plugInPort.renumberSelectedComponents(xAxisFirst);
  }
}