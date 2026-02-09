package org.diylc.swing.actions;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.utils.IconLoader;
import org.diylc.swing.plugins.edit.FindDialog;

public class FindAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;

  public FindAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    putValue(AbstractAction.NAME, "Find");
    putValue(AbstractAction.SMALL_ICON, IconLoader.SearchBox.getIcon());
    putValue(AbstractAction.ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Find triggered");
    FindDialog dialog = DialogFactory.getInstance().createFindDialog();
    dialog.setVisible(true);

    if (Objects.equals(dialog.getSelectedButtonCaption(), FindDialog.OK)) {
      String criteria = dialog.getCriteria();
      plugInPort.selectMatching(criteria);
      if (plugInPort.getSelectedComponents().isEmpty()) {
        swingUI.showMessage("No matching components found.", "Find",
            ISwingUI.INFORMATION_MESSAGE);
      }
    }
  }
}
