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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.diylc.common.PropertyWrapper;
import org.diylc.components.misc.LoadlineEntity;
import org.diylc.swingframework.ButtonDialog;

public class LoadlineEditor extends JButton {

  private static final long serialVersionUID = 1L;

  private static final String title = "Click to edit";
  
  private PropertyWrapper property;

  public LoadlineEditor(final PropertyWrapper property) {
    super();
    this.property = property;

    LoadlineEntity loadline = (LoadlineEntity) property.getValue();
    
    String name = (loadline == null ? "<empty>" : loadline.getName()) + " ";
    
    setText(property.isUnique() ? (name + title ): ("(multi value) " + title));
    setOpaque(true);
    setHorizontalAlignment(SwingConstants.CENTER);
    setBorder(BorderFactory.createEtchedBorder());
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        LoadlineEditorDialog dialog = new LoadlineEditorDialog(null, "Edit Loadline", loadline);
        dialog.setVisible(true);
        if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption()))
        {          
          property.setChanged(true);
//          property.setValue(newColor);            
        }
      }
    });
    
    if (property.isReadOnly()) {
     setEnabled(false);
    }
  }
}
