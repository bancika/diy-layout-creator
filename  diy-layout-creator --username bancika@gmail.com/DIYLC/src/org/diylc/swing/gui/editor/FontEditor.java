package org.diylc.swing.gui.editor;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.diylc.common.PropertyWrapper;

import com.diyfever.gui.FontChooserComboBox;

public class FontEditor extends FontChooserComboBox {

	private static final long serialVersionUID = 1L;

	private final PropertyWrapper property;

	public FontEditor(PropertyWrapper property) {
		this.property = property;
		setSelectedItem(((Font) property.getValue()).getName());
		addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Font oldFont = (Font) FontEditor.this.property.getValue();
					Font newFont = new Font(getSelectedItem().toString(), oldFont.getStyle(),
							oldFont.getSize());
					FontEditor.this.property.setValue(newFont);
				}
			}
		});
	}
}
