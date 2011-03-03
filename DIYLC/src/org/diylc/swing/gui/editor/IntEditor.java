package org.diylc.swing.gui.editor;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.diylc.common.PropertyWrapper;

import com.diyfever.gui.DoubleTextField;

public class IntEditor extends DoubleTextField {

	private static final long serialVersionUID = 1L;

	public IntEditor(final PropertyWrapper property) {
		setLayout(new BorderLayout());
		setValue((double) (Integer) property.getValue());
		addPropertyChangeListener(DoubleTextField.VALUE_PROPERTY, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				property.setValue(((Double) evt.getNewValue()).intValue());
			}
		});
	}
}
