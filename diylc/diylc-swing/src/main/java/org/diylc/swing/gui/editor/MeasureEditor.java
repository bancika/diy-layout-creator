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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.function.Function;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.ResistanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.ValueNotation;
import org.diylc.core.measures.ValueNotation.ParsedMeasure;
import org.diylc.swingframework.DoubleTextField;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.measures.AbstractMeasure;
import org.diylc.core.measures.Unit;
import org.diylc.utils.Constants;

public class MeasureEditor extends Container {

  private static final long serialVersionUID = 1L;

  private final static Logger LOG = Logger.getLogger(MeasureEditor.class);

  private static final Format DECIMAL_FORMAT = new DecimalFormat("0.#####");

  private Color oldBg;
  private JTextField valueField;
  private JComboBox<Object> unitBox;
  private boolean ignoreUnitChanges = false;

  @SuppressWarnings("unchecked")
  public MeasureEditor(final PropertyWrapper property) {
    setLayout(new BorderLayout());
    AbstractMeasure<?> measure = ((AbstractMeasure<?>) property.getValue());
    measure = convertUnitsIfNeeded(measure);
    final Function<String, Unit> unitResolver = getUnitResolver(property.getType());
    if (unitResolver == null) {
      final DoubleTextField doubleField =
          new DoubleTextField(measure == null ? null : measure.getValue());
      doubleField.addPropertyChangeListener(DoubleTextField.VALUE_PROPERTY,
          new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
              applyValue(property, (Double) evt.getNewValue(), unitBox.getSelectedItem());
            }
          });
      valueField = doubleField;
    } else {
      final MeasureTextField<ParsedMeasure<Unit>> measureField =
          new MeasureTextField<ParsedMeasure<Unit>>(formatForField(measure, true),
              text -> ValueNotation.parse(text, unitResolver));
      measureField.addPropertyChangeListener(MeasureTextField.VALUE_PROPERTY,
          new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
              ParsedMeasure<Unit> parsed = (ParsedMeasure<Unit>) evt.getNewValue();
              if (parsed == null) {
                applyValue(property, null, unitBox.getSelectedItem());
                return;
              }
              // a unit typed together with the value, e.g. 4K7, also switches the unit box
              if (parsed.unit() != null && parsed.unit() != unitBox.getSelectedItem()) {
                ignoreUnitChanges = true;
                try {
                  unitBox.setSelectedItem(parsed.unit());
                } finally {
                  ignoreUnitChanges = false;
                }
              }
              applyValue(property, parsed.value(), unitBox.getSelectedItem());
            }
          });
      measureField.addFocusListener(new FocusAdapter() {

        @Override
        public void focusLost(FocusEvent e) {
          if (measureField.getValue() != null) {
            measureField.setDisplayText(
                formatForField((AbstractMeasure<?>) property.getValue(), true));
          }
        }
      });
      valueField = measureField;
    }
    oldBg = valueField.getBackground();
    add(valueField, BorderLayout.CENTER);
    try {
      Type type = ((ParameterizedType) property.getType().getGenericSuperclass()).getActualTypeArguments()[0];
      Method method = ((Class<?>) type).getMethod("values");
      unitBox = new JComboBox<Object>((Object[]) method.invoke(null));
      unitBox.setSelectedItem(measure == null ? null : measure.getUnit());
      unitBox.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent evt) {
          if (ignoreUnitChanges) {
            return;
          }
          try {
            Double newValue = getFieldValue();
            if (newValue != null && property.isReadOnly()) {
              double oldFactor = ((Unit)((AbstractMeasure<?>)property.getValue()).getUnit()).getFactor();
              double newFactor = ((Unit)unitBox.getSelectedItem()).getFactor();
              newValue = newValue * oldFactor / newFactor;
            }
            applyValue(property, newValue, unitBox.getSelectedItem());
            if (newValue != null && (property.isReadOnly() || unitResolver != null)) {
              setFieldValue((AbstractMeasure<?>) property.getValue());
            }
          } catch (Exception e) {
            LOG.error("Error while updating property units", e);
          }
        }
      });
      add(unitBox, BorderLayout.EAST);

      if (property.isReadOnly()) {
        valueField.setEnabled(false);
        unitBox.setEnabled(true);
      }

      if (!property.isUnique()) {
        valueField.setBackground(Constants.MULTI_VALUE_COLOR);
        unitBox.setBackground(Constants.MULTI_VALUE_COLOR);
      }
    } catch (Exception e) {
      LOG.error("Error while creating the editor", e);
    }
  }

  /**
   * Returns the function that resolves unit symbols typed together with the value, or null when
   * the measure only accepts a plain number.
   */
  static Function<String, Unit> getUnitResolver(Class<?> measureType) {
    if (Resistance.class.equals(measureType)) {
      return ResistanceUnit::fromSymbol;
    }
    if (Capacitance.class.equals(measureType)) {
      return CapacitanceUnit::fromSymbol;
    }
    return null;
  }

  /**
   * Text shown in the value field. It is always decimal, whatever the selected value notation,
   * because the unit box beside it already shows the unit; the unit is written out only when it
   * differs from the one in the box.
   */
  static String formatForField(AbstractMeasure<?> measure, boolean unitShownInBox) {
    if (measure == null || measure.getValue() == null) {
      return "";
    }
    String number;
    synchronized (DECIMAL_FORMAT) {
      number = DECIMAL_FORMAT.format(measure.getValue());
    }
    return unitShownInBox || measure.getUnit() == null ? number : number + measure.getUnit();
  }

  private void applyValue(PropertyWrapper property, Double newValue, Object unit) {
    try {
      if (newValue == null) {
        property.setValue(null);
      } else {
        Constructor<?> ctor = property.getType().getConstructors()[0];
        property.setValue((AbstractMeasure<?>) ctor.newInstance(newValue, unit));
      }
      property.setChanged(true);
      valueField.setBackground(oldBg);
      unitBox.setBackground(oldBg);
    } catch (Exception e) {
      LOG.error("Error while updating property value", e);
    }
  }

  @SuppressWarnings("unchecked")
  private Double getFieldValue() {
    if (valueField instanceof DoubleTextField doubleField) {
      return doubleField.getValue();
    }
    ParsedMeasure<Unit> parsed = ((MeasureTextField<ParsedMeasure<Unit>>) valueField).getValue();
    return parsed == null ? null : parsed.value();
  }

  @SuppressWarnings("unchecked")
  private void setFieldValue(AbstractMeasure<?> measure) {
    if (valueField instanceof DoubleTextField doubleField) {
      doubleField.setValue(measure == null ? null : measure.getValue());
    } else {
      ((MeasureTextField<ParsedMeasure<Unit>>) valueField).setDisplayText(formatForField(measure, true));
    }
  }

  private AbstractMeasure<?> convertUnitsIfNeeded(AbstractMeasure<?> value) {
    if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.AUTO_UNIT_CONVERSION_KEY, true)
        && value instanceof Size sizeValue) {
      SizeUnit preferredUnit;
      if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.METRIC_KEY, true)) {
        preferredUnit = SizeUnit.mm;
      }
      else {
        preferredUnit = SizeUnit.in;
      }
      if (preferredUnit.getType() != sizeValue.getUnit().getType()) {
        SizeUnit targetUnit = SizeUnit.ConversionMapping.get(sizeValue.getUnit());
        if (targetUnit != null) {
          value = sizeValue.convertToUnits(targetUnit);
        }
      }
    }
    return value;
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
