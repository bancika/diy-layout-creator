package org.diylc.swing.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
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
import org.diylc.swingframework.DoubleTextField;
import org.diylc.utils.Constants;

public class MeasureEditor extends Container {

	private static final long serialVersionUID = 1L;

	private Color oldBg;
	private DoubleTextField valueField;
	private JComboBox unitBox;

	@SuppressWarnings("unchecked")
	public MeasureEditor(final PropertyWrapper property) {
		setLayout(new BorderLayout());
		AbstractMeasure measure = ((AbstractMeasure) property.getValue());
		valueField = new DoubleTextField(measure.getValue());
		oldBg = valueField.getBackground();
		valueField.addPropertyChangeListener(DoubleTextField.VALUE_PROPERTY,
				new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						try {
							AbstractMeasure<?> newMeasure = (AbstractMeasure<?>) ((AbstractMeasure) property
									.getValue()).clone();
							newMeasure.setValue((Double) evt.getNewValue());
							property.setValue(newMeasure);
							property.setChanged(true);
							valueField.setBackground(oldBg);
							unitBox.setBackground(oldBg);
						} catch (CloneNotSupportedException ex) {
						}
					}
				});
		add(valueField, BorderLayout.CENTER);
		try {
			Method method = measure.getUnit().getClass().getMethod("values");
			unitBox = new JComboBox((Object[]) method.invoke(measure.getUnit()));
			unitBox.setSelectedItem(measure.getUnit());
			unitBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						AbstractMeasure<Enum<? extends Unit>> newMeasure = (AbstractMeasure<Enum<? extends Unit>>) ((AbstractMeasure) property
								.getValue()).clone();
						newMeasure.setUnit((Enum<? extends Unit>) unitBox.getSelectedItem());
						property.setValue(newMeasure);
						property.setChanged(true);
						valueField.setBackground(oldBg);
						unitBox.setBackground(oldBg);
					} catch (CloneNotSupportedException ex) {
					}
				}
			});
			add(unitBox, BorderLayout.EAST);

			if (!property.isUnique()) {
				valueField.setBackground(Constants.MULTI_VALUE_COLOR);
				unitBox.setBackground(Constants.MULTI_VALUE_COLOR);
			}
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
