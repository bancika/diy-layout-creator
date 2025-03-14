/*

O    DIY Layout Creator (DIYLC).
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
package org.diylc.swing.gui.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.JTextField;

import org.diylc.swingframework.fonts.FontChooserComboBox;

import org.diylc.common.PropertyWrapper;
import org.diylc.utils.Constants;

public class FontEditor extends FontChooserComboBox {

  private static final long serialVersionUID = 1L;

  private Color oldBg = getBackground();

  private final PropertyWrapper property;

  public FontEditor(final PropertyWrapper property) {
    this.property = property;
    if (property.isReadOnly())
      setEnabled(false);
    ((JTextField) getEditor().getEditorComponent()).setBorder(BorderFactory.createCompoundBorder(
        ((JTextField) getEditor().getEditorComponent()).getBorder(), BorderFactory.createEmptyBorder(0, 2, 0, 0)));
    setSelectedItem(((Font) property.getValue()).getName());
    addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          property.setChanged(true);
          setBackground(oldBg);
          Font oldFont = (Font) FontEditor.this.property.getValue();
          Font newFont = new Font(getSelectedItem().toString(), oldFont.getStyle(), oldFont.getSize());
          FontEditor.this.property.setValue(newFont);
        }
      }
    });
    if (!property.isUnique()) {
      setBackground(Constants.MULTI_VALUE_COLOR);
    }
  }
}
