package org.diylc.swing.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.images.IconLoader;
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
        KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Find triggered");
    FindDialog dialog = DialogFactory.getInstance().createFindDialog();
    dialog.setVisible(true);

    if (dialog.getSelectedButtonCaption() == FindDialog.OK) {
      String criteria = dialog.getCriteria();
      plugInPort.selectMatching(criteria);
      if (plugInPort.getSelectedComponents().size() == 0) {
        swingUI.showMessage("No matching components found.", "Find",
            ISwingUI.INFORMATION_MESSAGE);
      }
    }
  }
}