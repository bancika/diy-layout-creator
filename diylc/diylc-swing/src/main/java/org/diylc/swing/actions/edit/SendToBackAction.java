package org.diylc.swing.actions.edit;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.images.IconLoader;

public class SendToBackAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;

  public SendToBackAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Send Backward");
    putValue(AbstractAction.SMALL_ICON, IconLoader.Back.getIcon());
    putValue(AbstractAction.ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Send to Back triggered");
    plugInPort.sendSelectionToBack();
  }
}