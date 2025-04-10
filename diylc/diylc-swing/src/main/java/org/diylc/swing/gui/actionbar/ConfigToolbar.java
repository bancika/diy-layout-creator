/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.common.IPlugInPort;

public class ConfigToolbar extends JPanel {
  
  private static final long serialVersionUID = 1L;
  
  private final String CHECK = "\u2611";
  private final String UNCHECK = "\u2610";

  public ConfigToolbar() {
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder());
  }

  public void add(String title, String configKey, Icon icon, boolean defaultValue) {
    final JLabel label = new JLabel();    
    boolean checked = ConfigurationManager.getInstance().readBoolean(configKey, defaultValue);
    label.setText(checked ? CHECK : UNCHECK);
    label.setIcon(icon);
    label.setToolTipText(title);
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.addMouseListener(new MouseAdapter() {
      
      @Override
      public void mouseClicked(MouseEvent e) {
        boolean checked = ConfigurationManager.getInstance().readBoolean(configKey, defaultValue);
        checked = !checked;
        ConfigurationManager.getInstance().writeValue(configKey, checked);
      }      
    });
    add(label);
    
    ConfigurationManager.getInstance().addConfigListener(configKey, new IConfigListener() {
      
      @Override
      public void valueChanged(String key, Object value) {
        boolean checked = (boolean) value;
        label.setText(checked ? CHECK : UNCHECK);
      }
    });
  }

  public Consumer<Boolean> add(String title, Icon icon, boolean defaultState, Runnable toggleAction) {
    final JLabel label = new JLabel();
    label.setText(defaultState ? CHECK : UNCHECK);
    label.setIcon(icon);
    label.setToolTipText(title);
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        toggleAction.run();
      }
    });
    add(label);

//    ConfigurationManager.getInstance().addConfigListener(configKey, new IConfigListener() {
//
//      @Override
//      public void valueChanged(String key, Object value) {
//        boolean checked = (boolean) value;
//        label.setText(checked ? CHECK : UNCHECK);
//      }
//    });
    return (checked) -> label.setText(checked ? CHECK : UNCHECK);
  }

  public void addToggleLabel(String text, String configKey, String defaultValue, List<ToggleItem> items) {
    ToggleConfigLabel label = new ToggleConfigLabel(text, configKey, defaultValue, items);
    add(label);
  }
  
  @Override
  public void setEnabled(boolean enabled) {
    for (Component c : getComponents())
      c.setEnabled(enabled);
  }
  
  public void addSpacer() {
    JSeparator l = new JSeparator();
    add(l);
  }
}
