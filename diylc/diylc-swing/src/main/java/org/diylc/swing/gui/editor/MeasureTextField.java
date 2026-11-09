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
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.diylc.utils.IconLoader;

/**
 * {@link JTextField} that parses its text on every change and fires {@link #VALUE_PROPERTY} with
 * the result, showing a warning icon while the text cannot be parsed. Unlike
 * {@code DoubleTextField} the text is never rewritten while the user types; call
 * {@link #setDisplayText(String)} to reformat it.
 *
 * @author Branislav Stojkovic
 *
 * @param <T> type of the parsed value
 */
public class MeasureTextField<T> extends JTextField {

  private static final long serialVersionUID = 1L;

  public static final String VALUE_PROPERTY = "MeasureValue";

  private final Function<String, T> parser;
  private final JLabel errorLabel;

  private T value;
  private boolean ignoreChanges = false;

  /**
   * @param text initial text
   * @param parser turns non-empty text into a value, throwing {@link IllegalArgumentException} when
   *        it cannot
   */
  public MeasureTextField(String text, Function<String, T> parser) {
    super();
    this.parser = parser;
    setLayout(new BorderLayout());
    errorLabel = new JLabel(IconLoader.Warning.getIcon());
    errorLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
    add(errorLabel, BorderLayout.EAST);
    setDisplayText(text);
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
  }

  public T getValue() {
    return value;
  }

  /**
   * Replaces the text without firing {@link #VALUE_PROPERTY}.
   */
  public void setDisplayText(String text) {
    ignoreChanges = true;
    try {
      setText(text);
      value = parseQuietly(text);
      errorLabel.setVisible(value == null);
    } finally {
      ignoreChanges = false;
    }
  }

  private T parseQuietly(String text) {
    try {
      return text == null || text.trim().isEmpty() ? null : parser.apply(text);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private void textChanged() {
    if (ignoreChanges) {
      return;
    }
    T newValue;
    try {
      newValue = getText().trim().isEmpty() ? null : parser.apply(getText());
    } catch (IllegalArgumentException e) {
      value = null;
      errorLabel.setVisible(true);
      return;
    }
    T oldValue = value;
    value = newValue;
    errorLabel.setVisible(false);
    firePropertyChange(VALUE_PROPERTY, oldValue, newValue);
  }
}
