package org.diylc.swing.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.diylc.common.PropertyWrapper;
import org.diylc.swingframework.DoubleTextField;
import org.diylc.utils.Constants;

public class IntEditor extends DoubleTextField {

	private static final long serialVersionUID = 1L;

	private Color oldBg = getBackground();

	public IntEditor(final PropertyWrapper property) {
		setLayout(new BorderLayout());
		setValue((double) (Integer) property.getValue());
		addPropertyChangeListener(DoubleTextField.VALUE_PROPERTY, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				property.setChanged(true);
				setBackground(oldBg);
				property.setValue(((Double) evt.getNewValue()).intValue());
			}
		});
		if (!property.isUnique()) {
			setBackground(Constants.MULTI_VALUE_COLOR);
		}
	}
}
