package org.diylc.swing.actions.edit;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.images.IconLoader;

public class DuplicateAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;

  public DuplicateAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Duplicate");
    putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    putValue(AbstractAction.SMALL_ICON, IconLoader.DocumentsGear.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Duplicate triggered");
    try {
      plugInPort.duplicateSelection();
    } catch (Exception ex) {
      ActionFactory.LOG.error("Coule not duplicate.", ex);
    }
  }
}