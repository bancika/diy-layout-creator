package org.diylc.swing.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;

import org.diylc.core.IView;
import org.diylc.swing.ActionFactory;

public class ToggleAction extends AbstractAction implements IConfigListener {

  private static final long serialVersionUID = 1L;

  private String configKey;

  private String title;

  public ToggleAction(String title, String configKey, String groupName, String defaultValue,
      Icon icon) {
    super();
    this.title = title;
    this.configKey = configKey;
    putValue(AbstractAction.NAME, title);
    putValue(IView.RADIO_BUTTON_GROUP_KEY, groupName);
    putValue(AbstractAction.SELECTED_KEY, ConfigurationManager.getInstance()
        .readString(configKey, defaultValue).equalsIgnoreCase(title));
    if (icon != null)
      putValue(AbstractAction.SMALL_ICON, icon);
    // keep track of config changes and update the state accordingly
    ConfigurationManager.getInstance().addConfigListener(configKey, this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info(getValue(AbstractAction.NAME) + " toggle triggered");
    ConfigurationManager.getInstance().writeValue(configKey, title);
  }

  @Override
  public void valueChanged(String key, Object value) {
    putValue(AbstractAction.SELECTED_KEY, title.equalsIgnoreCase(value.toString()));
  }
}