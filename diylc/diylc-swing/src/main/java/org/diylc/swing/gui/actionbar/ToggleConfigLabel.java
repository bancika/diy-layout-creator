/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.swing.gui.actionbar;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;

import org.diylc.lang.LangUtil;

public class ToggleConfigLabel extends JLabel implements IConfigListener {

  private static final long serialVersionUID = 1L;
  
  private List<ToggleItem> items;
  private String configItem;
  private String title;
  
  private int itemIndex = 0;  

  public ToggleConfigLabel(String title, String configKey, String defaultValue, List<ToggleItem> items) {
    super();
    this.title = title;
    this.configItem = configKey;
    this.items = items;
    setHorizontalTextPosition(SwingConstants.LEFT);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    
    valueChanged(configKey, ConfigurationManager.getInstance().readString(configKey, defaultValue));
    ConfigurationManager.getInstance().addConfigListener(configKey, this);
    
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        itemIndex++;
        if (itemIndex >= items.size())
          itemIndex = 0;        
        ConfigurationManager.getInstance().writeValue(configKey, items.get(itemIndex).getConfigValue());
      }
    });
  }
  
  @Override
  public void valueChanged(String key, Object value) {
    if (!this.configItem.equalsIgnoreCase(key) || value == null)
      return;
    
    this.itemIndex = 0;
    for (int i = 0; i < items.size(); i++) {
      ToggleItem item = items.get(i);
      if (item.getConfigValue().equalsIgnoreCase(value.toString()))
      {
        this.itemIndex = i;
        setIcon(item.getIcon());
        setToolTipText(title + " " + item.getConfigValue() + ". " + LangUtil.translate("Click to toggle to the next option"));
        break;
      }
    }
  }
}
