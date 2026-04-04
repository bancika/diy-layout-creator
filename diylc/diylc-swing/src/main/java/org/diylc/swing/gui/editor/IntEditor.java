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
package org.diylc.swing.gui.editor;

import java.awt.Color;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.diylc.common.PropertyWrapper;
import org.diylc.utils.Constants;

public class IntEditor extends JSpinner {

  private static final long serialVersionUID = 1L;

  private Color oldBg;

  public IntEditor(final PropertyWrapper property) {
    super(new SpinnerNumberModel());
    JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) getEditor();
    oldBg = editor.getTextField().getBackground();
    
    if (property.isReadOnly())
      setEnabled(false);
    
    if (property.getValue() != null)
      setValue((Integer) property.getValue());
      
    addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        property.setChanged(true);
        editor.getTextField().setBackground(oldBg);
        property.setValue((Integer) getValue());
      }
    });
    
    if (!property.isUnique()) {
      editor.getTextField().setBackground(Constants.MULTI_VALUE_COLOR);
    }
  }
}

