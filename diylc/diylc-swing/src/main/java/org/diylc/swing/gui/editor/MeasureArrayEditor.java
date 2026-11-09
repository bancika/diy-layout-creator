/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.diylc.swingframework.DoubleArrayTextField;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.measures.AbstractMeasure;
import org.diylc.core.measures.Unit;
import org.diylc.core.measures.ValueNotation;
import org.diylc.core.measures.ValueNotation.ParsedMeasure;
import org.diylc.utils.Constants;

public class MeasureArrayEditor extends Container {

	private static final long serialVersionUID = 1L;

	private final static Logger LOG = Logger
			.getLogger(MeasureArrayEditor.class);

	private Color oldBg;
	private JTextField valueField;
	private JComboBox<Object> unitBox;
	private boolean ignoreUnitChanges = false;
	
	@SuppressWarnings("unchecked")
	public MeasureArrayEditor(final PropertyWrapper property) {
		setLayout(new BorderLayout());
		final AbstractMeasure<?>[] measure = ((AbstractMeasure<?>[]) property
				.getValue());
		final Function<String, Unit> unitResolver = MeasureEditor
				.getUnitResolver(property.getType().getComponentType());
		if (unitResolver == null) {
			valueField = createDoubleField(property, measure);
		} else {
			final MeasureTextField<List<ParsedMeasure<Unit>>> measureField = new MeasureTextField<List<ParsedMeasure<Unit>>>(
					formatForField(measure), text -> parseSections(text, unitResolver));
			measureField.addPropertyChangeListener(MeasureTextField.VALUE_PROPERTY,
					new PropertyChangeListener() {

						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							List<ParsedMeasure<Unit>> parsed = (List<ParsedMeasure<Unit>>) evt
									.getNewValue();
							// the unit box shows the unit of the first section
							if (parsed != null && !parsed.isEmpty() && parsed.get(0) != null
									&& parsed.get(0).unit() != null
									&& parsed.get(0).unit() != unitBox.getSelectedItem()) {
								ignoreUnitChanges = true;
								try {
									unitBox.setSelectedItem(parsed.get(0).unit());
								} finally {
									ignoreUnitChanges = false;
								}
							}
							applySections(property, parsed, false);
						}
					});
			measureField.addFocusListener(new FocusAdapter() {

				@Override
				public void focusLost(FocusEvent e) {
					if (measureField.getValue() != null) {
						measureField.setDisplayText(
								formatForField((AbstractMeasure<?>[]) property.getValue()));
					}
				}
			});
			valueField = measureField;
		}
		if (property.isReadOnly()) {
			valueField.setEnabled(false);
		}
		oldBg = valueField.getBackground();
		add(valueField, BorderLayout.CENTER);
		try {
			Type type = ((ParameterizedType) property.getType().getComponentType()
					.getGenericSuperclass()).getActualTypeArguments()[0];
			Method method = ((Class<?>) type).getMethod("values");
			unitBox = new JComboBox<Object>((Object[]) method.invoke(null));
			unitBox.setSelectedItem(measure == null || measure.length == 0 || measure[0] == null ? null
					: measure[0].getUnit());
			unitBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent evt) {
					if (ignoreUnitChanges) {
						return;
					}
					if (valueField instanceof DoubleArrayTextField doubleField) {
						applyValues(property, doubleField.getValue());
					} else {
						MeasureTextField<List<ParsedMeasure<Unit>>> measureField = (MeasureTextField<List<ParsedMeasure<Unit>>>) valueField;
						if (measureField.getValue() != null) {
							applySections(property, measureField.getValue(), true);
							measureField.setDisplayText(
									formatForField((AbstractMeasure<?>[]) property.getValue()));
						}
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

	private DoubleArrayTextField createDoubleField(final PropertyWrapper property,
			AbstractMeasure<?>[] measure) {
		Double[] values = new Double[measure.length];
		for (int i = 0; i < measure.length; i++) {
			values[i] = measure[i] == null ? null : measure[i].getValue();
		}
		DoubleArrayTextField valueField = new DoubleArrayTextField(measure == null ? null : values);
		valueField.addPropertyChangeListener(
				DoubleArrayTextField.VALUE_PROPERTY,
				new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						applyValues(property, (Double[]) evt.getNewValue());
					}
				});
		return valueField;
	}

	private void applyValues(PropertyWrapper property, Double[] newValues) {
		try {
			Constructor<?> ctor = property.getType().getComponentType()
					.getConstructors()[0];
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

	/**
	 * Sections typed with their own unit keep it, the others take the unit selected in the box.
	 * When the box itself changed, its unit is applied to every section.
	 */
	private void applySections(PropertyWrapper property, List<ParsedMeasure<Unit>> sections,
			boolean forceBoxUnit) {
		try {
			Constructor<?> ctor = property.getType().getComponentType()
					.getConstructors()[0];
			AbstractMeasure<?>[] newMeasure = null;
			if (sections != null) {
				newMeasure = (AbstractMeasure<?>[]) Array.newInstance(property.getType().getComponentType(), sections.size());
				for (int i = 0; i < sections.size(); i++) {
					ParsedMeasure<Unit> section = sections.get(i);
					Object unit = section == null || section.unit() == null || forceBoxUnit
							? unitBox.getSelectedItem() : section.unit();
					newMeasure[i] = (AbstractMeasure<?>) ctor
							.newInstance(section == null ? null : section.value(), unit);
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

	private static List<ParsedMeasure<Unit>> parseSections(String text,
			Function<String, Unit> unitResolver) {
		List<ParsedMeasure<Unit>> sections = new ArrayList<ParsedMeasure<Unit>>();
		for (String part : text.split("/", -1)) {
			sections.add(part.trim().isEmpty() ? null : ValueNotation.parse(part, unitResolver));
		}
		return sections;
	}

	private static String formatForField(AbstractMeasure<?>[] measure) {
		if (measure == null) {
			return "";
		}
		Object boxUnit = measure.length == 0 || measure[0] == null ? null : measure[0].getUnit();
		List<String> parts = new ArrayList<String>();
		for (AbstractMeasure<?> section : measure) {
			parts.add(MeasureEditor.formatForField(section,
					section == null || section.getUnit() == boxUnit));
		}
		return String.join(" / ", parts);
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
