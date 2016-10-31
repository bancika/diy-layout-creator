package org.diylc.swing.gui.editor;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.diylc.common.PropertyWrapper;
import org.diylc.swing.gui.components.HTMLTextArea;
import org.diylc.utils.Constants;

public class MultiLineStringEditor extends JScrollPane {

  private static final long serialVersionUID = 1L;

  private Color oldBg;

  private JTextArea textArea;

  private final PropertyWrapper property;

  public MultiLineStringEditor(PropertyWrapper property) {
    super();
    this.property = property;
    setViewportView(getTextArea());
    setPreferredSize(new Dimension(192, 64));
  }

  public JTextArea getTextArea() {
    if (textArea == null) {
      textArea = new HTMLTextArea(property.getValue() == null ? "" : (String) property.getValue());
      textArea.setFont(getFont());
      oldBg = textArea.getBackground();
      textArea.getDocument().addDocumentListener(new DocumentListener() {

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
        textArea.setBackground(Constants.MULTI_VALUE_COLOR);
      }
    }
    return textArea;
  }

  private void textChanged() {
    property.setChanged(true);
    getTextArea().setBackground(oldBg);
    property.setValue(getTextArea().getText());
  }
}
