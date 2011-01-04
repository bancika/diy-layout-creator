package org.diylc.gui.editor;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.diylc.common.PropertyWrapper;


public class StringEditor extends JTextField {

	private static final long serialVersionUID = 1L;
	private final PropertyWrapper property;

	public StringEditor(PropertyWrapper property) {
		super(property.getValue() == null ? "" : (String) property.getValue());
		this.property = property;
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

	private void textChanged() {
		property.setValue(getText());
	}

}
