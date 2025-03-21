package org.diylc.swing.actions.edit;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.swing.ActionFactory;
import org.diylc.utils.IconLoader;

public class EditSelectionAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;

  public EditSelectionAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Edit Selection");
    putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    putValue(AbstractAction.SMALL_ICON, IconLoader.EditComponent.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Edit Selection triggered");
    List<PropertyWrapper> properties = plugInPort.getMutualSelectionProperties();
    if (properties == null || properties.isEmpty()) {
      ActionFactory.LOG.info("Nothing to edit");
      return;
    }
    plugInPort.editSelection();
  }
}