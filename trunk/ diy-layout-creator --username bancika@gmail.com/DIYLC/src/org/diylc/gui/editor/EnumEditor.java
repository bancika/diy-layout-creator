package org.diylc.gui.editor;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.diylc.common.PropertyWrapper;

public class EnumEditor extends JComboBox {

	private static final long serialVersionUID = 1L;

	private final PropertyWrapper property;

	public EnumEditor(PropertyWrapper property) {
		this.property = property;
		Object[] values = property.getType().getEnumConstants();
		setModel(new DefaultComboBoxModel(values));
		setSelectedItem(property.getValue());
		addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					EnumEditor.this.property.setValue(e.getItem());
				}
			}
		});
	}
}
