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

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.diylc.swingframework.DoubleTextField;

import org.diylc.common.PropertyWrapper;
import org.diylc.utils.Constants;

public class IntEditor extends DoubleTextField {

  private static final long serialVersionUID = 1L;

  private Color oldBg = getBackground();

  public IntEditor(final PropertyWrapper property) {
    setLayout(new BorderLayout());
    if (property.isReadOnly())
      setEnabled(false);
    if (property.getValue() != null)
      setValue((double) (Integer) property.getValue());
    addPropertyChangeListener(DoubleTextField.VALUE_PROPERTY, new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        property.setChanged(true);
        setBackground(oldBg);
        property.setValue(evt.getNewValue() == null ? null : ((Double) evt.getNewValue()).intValue());
      }
    });
    if (!property.isUnique()) {
      setBackground(Constants.MULTI_VALUE_COLOR);
    }
  }
}
