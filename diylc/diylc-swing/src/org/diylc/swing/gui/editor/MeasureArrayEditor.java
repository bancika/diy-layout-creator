/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.swing.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.swing.JComboBox;

import org.apache.log4j.Logger;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.measures.AbstractMeasure;
import org.diylc.swingframework.DoubleArrayTextField;
import org.diylc.utils.Constants;

public class MeasureArrayEditor extends Container {

	private static final long serialVersionUID = 1L;

	private final static Logger LOG = Logger
			.getLogger(MeasureArrayEditor.class);

	private Color oldBg;
	private DoubleArrayTextField valueField;
	private JComboBox unitBox;
	
	public MeasureArrayEditor(final PropertyWrapper property) {
		setLayout(new BorderLayout());
		final AbstractMeasure<?>[] measure = ((AbstractMeasure<?>[]) property
				.getValue());
		Double[] values = new Double[measure.length];
		for (int i = 0; i < measure.length; i++) {
			values[i] = measure[i] == null ? null : measure[i].getValue();
		}
		valueField = new DoubleArrayTextField(measure == null ? null : values);
		if (property.isReadOnly())
	      valueField.setEnabled(false);
		oldBg = valueField.getBackground();
		valueField.addPropertyChangeListener(
				DoubleArrayTextField.VALUE_PROPERTY,
				new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						try {
							Constructor<?> ctor = property.getType().getComponentType()
									.getConstructors()[0];
							Double[] newValues = (Double[]) evt.getNewValue();
							AbstractMeasure<?>[] newMeasure;
							if (newValues == null)
								newMeasure = null;
							else {
								newMeasure = (AbstractMeasure<?>[]) Array.newInstance(property.getType().getComponentType(), newValues.length);
								for (int i = 0; i < newValues.length; i++) {
									newMeasure[i] = (AbstractMeasure<?>) ctor
											.newInstance(newValues[i],
													unitBox.getSelectedItem());
								}
							}

							property.setValue(newMeasure);
							property.setChanged(true);
							valueField.setBackground(oldBg);
							unitBox.setBackground(oldBg);
						} catch (Exception e) {
							LOG.error("Error while updating property value", e);
						}
					}
				});
		add(valueField, BorderLayout.CENTER);
		try {
			Type type = ((ParameterizedType) property.getType().getComponentType()
					.getGenericSuperclass()).getActualTypeArguments()[0];
			Method method = ((Class<?>) type).getMethod("values");
			unitBox = new JComboBox((Object[]) method.invoke(null));
			unitBox.setSelectedItem(measure == null || measure.length == 0 || measure[0] == null ? null
					: measure[0].getUnit());
			unitBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent evt) {
					try {
						Constructor<?> ctor = property.getType().getComponentType()
								.getConstructors()[0];
						
						AbstractMeasure<?>[] newMeasure;
						Double[] newValues = valueField.getValue();
						if (newValues == null)
							newMeasure = null;
						else {
							newMeasure = (AbstractMeasure<?>[]) Array.newInstance(property.getType().getComponentType(), newValues.length);
							for (int i = 0; i < newValues.length; i++) {
								newMeasure[i] = (AbstractMeasure<?>) ctor
										.newInstance(newValues[i],
												unitBox.getSelectedItem());
							}
						}
						
						property.setValue(newMeasure);
						property.setChanged(true);
						valueField.setBackground(oldBg);
						unitBox.setBackground(oldBg);
					} catch (Exception e) {
						LOG.error("Error while updating property units", e);
					}
				}
			});
			add(unitBox, BorderLayout.EAST);

			if (!property.isUnique()) {
				valueField.setBackground(Constants.MULTI_VALUE_COLOR);
				unitBox.setBackground(Constants.MULTI_VALUE_COLOR);
			}
		} catch (Exception e) {
			LOG.error("Error while creating the editor", e);
		}
	}

	@Override
	public void requestFocus() {
		this.valueField.requestFocus();
	}

	@Override
	public boolean requestFocusInWindow() {
		return this.valueField.requestFocusInWindow();
	}

	@Override
	public synchronized void addKeyListener(KeyListener l) {
		this.valueField.addKeyListener(l);
		this.unitBox.addKeyListener(l);
	}
}
