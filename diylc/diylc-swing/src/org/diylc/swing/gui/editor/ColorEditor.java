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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.diylc.common.PropertyWrapper;

public class ColorEditor extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final String title = "Click to edit";

  private JLabel colorLabel;
  private JTextField colorField;
  private PropertyWrapper property;

  public ColorEditor(final PropertyWrapper property) {
    super();
    this.property = property;

    setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.weighty = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    add(getColorLabel(), gbc);

    gbc.gridx++;
    gbc.weightx = 0;
    gbc.insets = new Insets(0, 2, 0, 0);
    add(new JLabel("#"), gbc);

    gbc.gridx++;
    gbc.insets = new Insets(0, 0, 0, 0);
    add(getColorField(), gbc);
    
    if (property.isReadOnly()) {
      getColorField().setEnabled(false);
      getColorLabel().setEnabled(false);
    }
  }

  public JLabel getColorLabel() {
    if (colorLabel == null) {
      colorLabel = new JLabel(property.isUnique() ? title : ("(multi value) " + title)) {

        private static final long serialVersionUID = 1L;

        @Override
        public void setBackground(Color bg) {
          if (bg.getRed() < 127 || bg.getBlue() < 127 || bg.getGreen() < 127) {
            setForeground(Color.white);
          } else {
            setForeground(Color.black);
          }
          super.setBackground(bg);
        }
      };
      colorLabel.setOpaque(true);
      colorLabel.setHorizontalAlignment(SwingConstants.CENTER);
      colorLabel.setBorder(BorderFactory.createEtchedBorder());
      colorLabel.setBackground((Color) property.getValue());
      colorLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      colorLabel.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          Color newColor = JColorChooser.showDialog(ColorEditor.this, "Choose Color", getBackground());
          if (newColor != null) {
            property.setChanged(true);
            property.setValue(newColor);
            getColorLabel().setBackground(newColor);
            getColorField().setText(Integer.toHexString(newColor.getRGB()).substring(2).toUpperCase());
          }
        }
      });
    }
    return colorLabel;
  }

  public JTextField getColorField() {
    if (colorField == null) {
      Color color = (Color) property.getValue();
      colorField =
          new JTextField(property.isUnique() ? Integer.toHexString(color.getRGB()).substring(2).toUpperCase() : "");
      colorField.setColumns(6);
      colorField.getDocument().addDocumentListener(new DocumentListener() {

        @Override
        public void removeUpdate(DocumentEvent e) {
          updateColor();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
          updateColor();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
          updateColor();
        }
      });
    }
    return colorField;
  }

  private void updateColor() {
    if (getColorField().getText().length() == 6) {
      Color newColor = Color.decode("#" + getColorField().getText());
      if (newColor != null && getColorLabel().getBackground() != newColor) {
        getColorLabel().setBackground(newColor);
        getColorLabel().setText(title);
        property.setChanged(true);
        property.setValue(newColor);
      }
    }
  }
}
