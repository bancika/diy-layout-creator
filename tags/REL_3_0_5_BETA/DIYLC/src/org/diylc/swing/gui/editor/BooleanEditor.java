package org.diylc.swing.gui.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.diylc.common.PropertyWrapper;


public class BooleanEditor extends JCheckBox {

	private static final long serialVersionUID = 1L;
	
	public BooleanEditor(final PropertyWrapper property) {
		super();
		setSelected((Boolean) property.getValue());
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				property.setValue(isSelected());
			}
		});
	}
}
