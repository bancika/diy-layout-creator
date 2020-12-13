package org.diylc.swing.gui.actionbar;

import javax.swing.Icon;

public class ToggleItem {
  private String name;
  private String configValue;
  private Icon icon;
  
  public ToggleItem(String name, String configValue, Icon icon) {
    super();
    this.name = name;
    this.configValue = configValue;
    this.icon = icon;
  }

  public String getName() {
    return name;
  }

  public String getConfigValue() {
    return configValue;
  }

  public Icon getIcon() {
    return icon;
  }
}
