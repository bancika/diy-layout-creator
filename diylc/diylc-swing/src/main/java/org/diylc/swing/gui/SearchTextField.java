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
package org.diylc.swing.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;
import javax.swing.Icon;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.diylc.utils.IconLoader;

public class SearchTextField extends JTextField {

  private static final long serialVersionUID = 1L;
  
  private boolean supportsQuickJump;
  private char shortcutKey;

  public SearchTextField(boolean supportsQuickJump, char shortcutKey, Consumer<String> searchAction) {
    super();
    this.supportsQuickJump = supportsQuickJump;
    this.shortcutKey = shortcutKey;
    
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        repaint();
      }

      @Override
      public void focusLost(FocusEvent e) {
        repaint();
      }
    });

    getDocument().addDocumentListener(new DocumentListener() {

      public void changedUpdate(DocumentEvent e) {
        process();
      }

      public void removeUpdate(DocumentEvent e) {
        process();
      }

      public void insertUpdate(DocumentEvent e) {
        process();
      }

      public void process() {
        String text = getText();
        searchAction.accept(text);
      }
    });
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);

    Graphics2D g2d = (Graphics2D) g;
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
    Icon icon = IconLoader.SearchBox.getIcon();
    icon.paintIcon(this, g2d, this.getWidth() - 18, 3);

    if (this.getText().trim().length() == 0 && !this.hasFocus()) {
      g2d.setColor(Color.gray);
      g2d.setFont(this.getFont());
      g2d.drawString(supportsQuickJump ? "Search (press " + shortcutKey + " to jump here)" : "Search", 4, 3 + this.getFont().getSize());
    }
  }   
}
