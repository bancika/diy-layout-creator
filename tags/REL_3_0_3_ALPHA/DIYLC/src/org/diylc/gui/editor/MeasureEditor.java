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
import org.diylc.core.measures.AbstractMeasure;
import org.diylc.core.measures.Unit;

import com.diyfever.gui.DoubleTextField;

public class MeasureEditor extends Container {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public MeasureEditor(final PropertyWrapper property) {
		setLayout(new BorderLayout());
		AbstractMeasure measure = ((AbstractMeasure) property.getValue());
		DoubleTextField valueField = new DoubleTextField(measure.getValue());
		valueField.addPropertyChangeListener(DoubleTextField.VALUE_PROPERTY,
				new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						try {
							AbstractMeasure<?> newMeasure = (AbstractMeasure<?>) ((AbstractMeasure) property
									.getValue()).clone();
							newMeasure.setValue((Double) evt.getNewValue());
							property.setValue(newMeasure);
						} catch (CloneNotSupportedException ex) {
						}
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
					try {
						AbstractMeasure<Enum<? extends Unit>> newMeasure = (AbstractMeasure<Enum<? extends Unit>>) ((AbstractMeasure) property
								.getValue()).clone();
						newMeasure.setUnit((Enum<? extends Unit>) unitBox.getSelectedItem());
						property.setValue(newMeasure);
					} catch (CloneNotSupportedException ex) {
					}
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
