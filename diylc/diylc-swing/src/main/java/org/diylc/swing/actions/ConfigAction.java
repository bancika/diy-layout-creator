package org.diylc.swing.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;

import org.diylc.common.IPlugInPort;
import org.diylc.core.IView;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.gui.DialogFactory;

public class ConfigAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private String configKey;
  private String tipKey;

  public ConfigAction(IPlugInPort plugInPort, String title, String configKey,
      boolean defaultValue, String tipKey) {
    super();
    this.plugInPort = plugInPort;
    this.configKey = configKey;
    this.tipKey = tipKey;
    putValue(AbstractAction.NAME, title);
    putValue(IView.CHECK_BOX_MENU_ITEM, true);
    putValue(AbstractAction.SELECTED_KEY,
        ConfigurationManager.getInstance().readBoolean(configKey, defaultValue));

    ActionFactory.LOG.info("Initializing " + configKey + " to " + getValue(AbstractAction.SELECTED_KEY));

    ConfigurationManager.getInstance().addConfigListener(configKey, new IConfigListener() {

      @Override
      public void valueChanged(String key, Object value) {
        putValue(AbstractAction.SELECTED_KEY, Boolean.TRUE.equals(value));
      }
    });
  }

  public ConfigAction(IPlugInPort plugInPort, String title, String configKey,
      boolean defaultValue) {
    this(plugInPort, title, configKey, defaultValue, null);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info(configKey + " config set to " + getValue(AbstractAction.SELECTED_KEY));
    ConfigurationManager.getInstance().writeValue(configKey,
        getValue(AbstractAction.SELECTED_KEY));
    if ((Boolean) getValue(AbstractAction.SELECTED_KEY) && tipKey != null
        && !ConfigurationManager.getInstance().readBoolean(tipKey + ".dismissed", false)) {
      DialogFactory.getInstance().createInfoDialog(tipKey).setVisible(true);
    }
    plugInPort.refresh();
  }
}