package org.diylc.gui.editor;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.diylc.common.PropertyWrapper;

public class ByteEditor extends JSlider {

	private static final long serialVersionUID = 1L;

	public ByteEditor(final PropertyWrapper property) {
		super();
		setMinimum(0);
		setMaximum(127);
		setValue((Byte) property.getValue());
		addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				property.setValue(new Integer(getValue()).byteValue());
			}
		});
	}
}
