package org.diylc.swing.actions.edit;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.images.IconLoader;

public class UngroupAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;

  public UngroupAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Ungroup Selection");
    putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    putValue(AbstractAction.SMALL_ICON, IconLoader.Ungroup.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Ungroup Selection triggered");
    plugInPort.ungroupSelectedComponents();
  }
}