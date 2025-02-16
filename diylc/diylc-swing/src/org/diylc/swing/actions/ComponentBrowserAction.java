package org.diylc.swing.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.core.IView;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.plugins.config.ConfigPlugin;

public class ComponentBrowserAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private String browserType;

  public ComponentBrowserAction(String browserType) {
    super();
    this.browserType = browserType;
    putValue(AbstractAction.NAME, browserType);
    putValue(IView.RADIO_BUTTON_GROUP_KEY, "componentBrowser");

    putValue(AbstractAction.SELECTED_KEY, browserType.equals(ConfigurationManager.getInstance()
        .readString(ConfigPlugin.COMPONENT_BROWSER, ConfigPlugin.SEARCHABLE_TREE)));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info(getValue(AbstractAction.NAME) + " triggered");
    ConfigurationManager.getInstance().writeValue(ConfigPlugin.COMPONENT_BROWSER, browserType);
  }
}