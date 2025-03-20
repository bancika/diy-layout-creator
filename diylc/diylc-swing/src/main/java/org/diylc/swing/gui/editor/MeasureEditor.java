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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.swing.JComboBox;
import org.apache.log4j.Logger;
import org.diylc.swingframework.DoubleTextField;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.measures.AbstractMeasure;
import org.diylc.core.measures.Unit;
import org.diylc.utils.Constants;

public class MeasureEditor extends Container {

  private static final long serialVersionUID = 1L;

  private final static Logger LOG = Logger.getLogger(MeasureEditor.class);

  private Color oldBg;
  private DoubleTextField valueField;
  private JComboBox<Object> unitBox;

  @SuppressWarnings("unchecked")
  public MeasureEditor(final PropertyWrapper property) {
    setLayout(new BorderLayout());
    final AbstractMeasure<?> measure = ((AbstractMeasure<?>) property.getValue());    
    valueField = new DoubleTextField(measure == null ? null : measure.getValue());
    oldBg = valueField.getBackground();
    valueField.addPropertyChangeListener(DoubleTextField.VALUE_PROPERTY, new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        try {
          Constructor<?> ctor = property.getType().getConstructors()[0];
          AbstractMeasure<?> newMeasure =
              (AbstractMeasure<?>) ctor.newInstance((Double) evt.getNewValue(), unitBox.getSelectedItem());
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
      Type type = ((ParameterizedType) property.getType().getGenericSuperclass()).getActualTypeArguments()[0];
      Method method = ((Class<?>) type).getMethod("values");
      unitBox = new JComboBox<Object>((Object[]) method.invoke(null));
      unitBox.setSelectedItem(measure == null ? null : measure.getUnit());
      unitBox.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent evt) {
          try {
            Constructor<?> ctor = property.getType().getConstructors()[0];
            Double newValue = valueField.getValue();
            if (newValue != null && property.isReadOnly()) {
              double oldFactor = ((Unit)((AbstractMeasure<?>)property.getValue()).getUnit()).getFactor();
              double newFactor = ((Unit)unitBox.getSelectedItem()).getFactor();
              newValue = newValue * oldFactor / newFactor;
              valueField.setValue(newValue);
            }
            AbstractMeasure<?> newMeasure =
                (AbstractMeasure<?>) ctor.newInstance(newValue,
                    (Enum<? extends Unit>) unitBox.getSelectedItem());
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
