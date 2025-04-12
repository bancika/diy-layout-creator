package org.diylc.swing.actions.analyze;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.common.IPlugInPort;
import org.diylc.common.OperationMode;
import org.diylc.core.IView;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.utils.IconLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class HighlightConnectedAreasAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private final IPlugInPort plugInPort;

  public HighlightConnectedAreasAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;

    putValue(AbstractAction.NAME, "Highlight Connected Areas");
    putValue(IView.CHECK_BOX_MENU_ITEM, true);
    putValue(AbstractAction.SELECTED_KEY,
        plugInPort.getOperationMode() == OperationMode.HIGHLIGHT_CONNECTED_AREAS);
    putValue(AbstractAction.SMALL_ICON, IconLoader.LaserPointer.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (Boolean.TRUE.equals(getValue(AbstractAction.SELECTED_KEY))) {
      plugInPort.setOperationMode(OperationMode.HIGHLIGHT_CONNECTED_AREAS);
    } else {
      plugInPort.setOperationMode(OperationMode.EDIT);
    }
  }

  public void refreshState() {
    putValue(AbstractAction.SELECTED_KEY,
        plugInPort.getOperationMode() == OperationMode.HIGHLIGHT_CONNECTED_AREAS);
  }
}
