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
package org.diylc.swing.gui.editor;

import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.diylc.common.PropertyWrapper;
import org.diylc.utils.Constants;

public class StringEditor extends JTextField {

  private static final long serialVersionUID = 1L;

  private Color oldBg = getBackground();

  private final PropertyWrapper property;

  public StringEditor(PropertyWrapper property) {
    super(property.getValue() == null ? "" : (String) property.getValue());
    this.property = property;
    if (property.isReadOnly())
      setEnabled(false);
    this.setColumns(32);
    getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void changedUpdate(DocumentEvent e) {
        textChanged();
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        textChanged();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        textChanged();
      }
    });
    if (!property.isUnique()) {
      setBackground(Constants.MULTI_VALUE_COLOR);
    }
  }

  private void textChanged() {
    property.setChanged(true);
    setBackground(oldBg);
    property.setValue(getText());
  }
  
  @Override
  public boolean requestFocusInWindow() {
    selectAll();
    return super.requestFocusInWindow();    
  }
}
