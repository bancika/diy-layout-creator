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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.diylc.lang.LangUtil;

public class ActionToolbar extends JPanel {
  
  private static final long serialVersionUID = 1L;

  public ActionToolbar() {
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder());
  }

  public void add(final Action action) {
    final JLabel l = new JLabel();
    l.setIcon((Icon) action.getValue(AbstractAction.SMALL_ICON));
    l.setToolTipText(LangUtil.translate((String) action.getValue(AbstractAction.NAME)));
    l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    l.addMouseListener(new MouseAdapter() {
      
      @Override
      public void mouseClicked(MouseEvent e) {
        action.actionPerformed(null);
      }      
    });
    add(l);  
    setEnabled(false);
  }
  
  @Override
  public void setEnabled(boolean enabled) {
    for (Component c : getComponents())
      c.setEnabled(enabled);
  }
  
  public void addSpacer() {
    JSeparator l = new JSeparator();
//    l.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.black));
    add(l);
  }
}
