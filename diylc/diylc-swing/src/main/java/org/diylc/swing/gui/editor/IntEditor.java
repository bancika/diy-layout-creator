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
import java.text.ParseException;

import javax.swing.AbstractSpinnerModel;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

import org.diylc.common.PropertyWrapper;
import org.diylc.utils.Constants;

public class IntEditor extends JSpinner {

  private static final long serialVersionUID = 1L;

  private Color oldBg;

  public IntEditor(final PropertyWrapper property) {
    super(new NullableIntModel((Integer) property.getValue()));

    JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) getEditor();
    JFormattedTextField textField = editor.getTextField();

    // Install a formatter that maps empty text to null and vice-versa
    DefaultFormatter formatter = new DefaultFormatter() {
      private static final long serialVersionUID = 1L;

      @Override
      public Object stringToValue(String text) throws ParseException {
        if (text == null || text.trim().isEmpty())
          return null;
        try {
          return Integer.valueOf(text.trim());
        } catch (NumberFormatException e) {
          throw new ParseException(text, 0);
        }
      }

      @Override
      public String valueToString(Object value) throws ParseException {
        return value == null ? "" : value.toString();
      }
    };
    formatter.setOverwriteMode(false);
    textField.setFormatterFactory(new DefaultFormatterFactory(formatter));

    oldBg = textField.getBackground();

    if (property.isReadOnly())
      setEnabled(false);

    addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        property.setChanged(true);
        textField.setBackground(oldBg);
        property.setValue(getValue());
      }
    });

    if (!property.isUnique()) {
      textField.setBackground(Constants.MULTI_VALUE_COLOR);
    }
  }

  /**
   * A spinner model that holds a nullable Integer value.
   */
  private static class NullableIntModel extends AbstractSpinnerModel {

    private Integer value;
    private boolean firing = false;

    NullableIntModel(Integer initialValue) {
      this.value = initialValue;
    }

    @Override
    public Object getValue() {
      return value;
    }

    @Override
    public void setValue(Object val) {
      if (firing)
        return;

      Integer newValue;
      if (val == null) {
        newValue = null;
      } else if (val instanceof Integer) {
        newValue = (Integer) val;
      } else if (val instanceof Number) {
        newValue = ((Number) val).intValue();
      } else if (val instanceof String) {
        String s = ((String) val).trim();
        newValue = s.isEmpty() ? null : Integer.valueOf(s);
      } else {
        return;
      }

      if (java.util.Objects.equals(this.value, newValue))
        return;

      this.value = newValue;
      firing = true;
      try {
        fireStateChanged();
      } finally {
        firing = false;
      }
    }

    @Override
    public Object getNextValue() {
      return value == null ? 1 : value + 1;
    }

    @Override
    public Object getPreviousValue() {
      return value == null ? -1 : value - 1;
    }
  }
}
