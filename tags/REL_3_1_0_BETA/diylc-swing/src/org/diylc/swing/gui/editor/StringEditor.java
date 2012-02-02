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
}
