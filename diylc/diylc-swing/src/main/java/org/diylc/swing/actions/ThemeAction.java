package org.diylc.swing.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.diylc.common.IPlugInPort;
import org.diylc.core.IView;
import org.diylc.core.Theme;
import org.diylc.swing.ActionFactory;

public class ThemeAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private Theme theme;

  public ThemeAction(IPlugInPort plugInPort, Theme theme) {
    super();
    this.plugInPort = plugInPort;
    this.theme = theme;
    putValue(AbstractAction.NAME, theme.getName());
    putValue(IView.RADIO_BUTTON_GROUP_KEY, "theme");
    putValue(AbstractAction.SELECTED_KEY,
        plugInPort.getSelectedTheme().getName().equals(theme.getName()));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info(getValue(AbstractAction.NAME) + " triggered");
    plugInPort.setSelectedTheme(theme);
  }
}