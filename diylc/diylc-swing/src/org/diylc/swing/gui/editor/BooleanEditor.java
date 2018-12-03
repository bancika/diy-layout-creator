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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.diylc.common.PropertyWrapper;
import org.diylc.utils.Constants;

public class BooleanEditor extends JCheckBox {

  private static final long serialVersionUID = 1L;

  private Color oldBg = getBackground();

  public BooleanEditor(final PropertyWrapper property) {
    super();
    setSelected((Boolean) property.getValue());
    addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        property.setChanged(true);
        setBackground(oldBg);
        property.setValue(isSelected());
      }
    });
    if (!property.isUnique()) {
      setBackground(Constants.MULTI_VALUE_COLOR);
    }
  }
}
