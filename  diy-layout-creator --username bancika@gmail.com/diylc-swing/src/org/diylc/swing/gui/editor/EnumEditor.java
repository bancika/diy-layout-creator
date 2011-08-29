package org.diylc.swing.gui.editor;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.diylc.common.PropertyWrapper;
import org.diylc.utils.Constants;

public class EnumEditor extends JComboBox {

	private static final long serialVersionUID = 1L;

	private Color oldBg = getBackground();

	private final PropertyWrapper property;

	public EnumEditor(final PropertyWrapper property) {
		this.property = property;
		Object[] values = property.getType().getEnumConstants();
		setModel(new DefaultComboBoxModel(values));
		setSelectedItem(property.getValue());
		addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					property.setChanged(true);
					setBackground(oldBg);
					EnumEditor.this.property.setValue(e.getItem());
				}
			}
		});
		if (!property.isUnique()) {
			setBackground(Constants.MULTI_VALUE_COLOR);
		}
	}
}
