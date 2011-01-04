package org.diylc.gui.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JComboBox;

import org.diylc.common.PropertyWrapper;
import org.diylc.model.measures.AbstractMeasure;

import com.diyfever.gui.DoubleTextField;

public class MeasureEditor extends Container {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public MeasureEditor(final PropertyWrapper property) {
		setLayout(new BorderLayout());
		final AbstractMeasure measure = ((AbstractMeasure) property.getValue());
		DoubleTextField valueField = new DoubleTextField(measure.getValue());
		valueField.addPropertyChangeListener(DoubleTextField.VALUE_PROPERTY,
				new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						measure.setValue((Double) evt.getNewValue());
					}
				});
		add(valueField, BorderLayout.CENTER);
		try {
			Method method = measure.getUnit().getClass().getMethod("values");
			final JComboBox unitBox = new JComboBox((Object[]) method.invoke(measure.getUnit()));
			unitBox.setSelectedItem(measure.getUnit());
			unitBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					measure.setUnit((Enum) unitBox.getSelectedItem());
				}
			});
			add(unitBox, BorderLayout.EAST);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
