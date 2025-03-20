package org.diylc.swing.actions.edit;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.utils.IconLoader;

public class BringToFrontAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;

  public BringToFrontAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Bring Forward");
    putValue(AbstractAction.SMALL_ICON, IconLoader.Front.getIcon());
    putValue(AbstractAction.ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Bring to Front triggered");
    plugInPort.bringSelectionToFront();
  }
}