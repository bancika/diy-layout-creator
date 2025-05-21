/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.swing.gui;

import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.diylc.lang.LangUtil;

public class TranslatedPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  @Override
  public Component add(Component comp) {
    if (comp instanceof JButton) {
      JButton button = (JButton)comp;
      String name = button.getText();
      if (name != null)
        button.setText(LangUtil.translate(name));
    }
    if (comp instanceof JLabel) {
      JLabel button = (JLabel)comp;
      String name = button.getText();
      
      if (name != null) {        
        boolean containsColon = false;
        if (name.contains(": ")) {
          containsColon = true;
          name = name.replace(": ", "");
        }
        
        String translated = LangUtil.translate(name);
        if (containsColon)
          translated += ": ";
        
        button.setText(translated);
      }
    }
    return super.add(comp);
  }
}
