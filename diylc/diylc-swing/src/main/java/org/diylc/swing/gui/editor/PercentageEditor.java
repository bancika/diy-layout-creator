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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.diylc.swingframework.DoubleTextField;

import org.diylc.common.Percentage;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.annotations.PercentEditor;
import org.diylc.utils.Constants;

public class PercentageEditor extends JPanel {

  private static final long serialVersionUID = 1L;

  private Color oldBg = getBackground();

  private int minValue = 0;
  private int maxValue = 100;
  private boolean isUpdating = false;

  public PercentageEditor(final PropertyWrapper property) {
    super();

    setLayout(new FlowLayout());

    try {
      PercentEditor percentEditor = property.getGetter().getAnnotation(PercentEditor.class);
      if (percentEditor != null) {
        minValue = percentEditor.minValue();
        maxValue = percentEditor.maxValue();
      }
    } catch (Exception e) {
    }

    final JSlider slider = new JSlider();
    final DoubleTextField valueField = new DoubleTextField();
    
    if (property.isReadOnly()) {
      slider.setEnabled(false);
      valueField.setEnabled(false);
    }

    slider.setMinimum(minValue * 100);
    slider.setMaximum(maxValue * 100);
    slider.setMinorTickSpacing(100);

    // Initialize values BEFORE adding the listener to avoid overwriting the property with integer
    Object val = property.getValue();
    if (val != null) {
        BigDecimal bdVal = ((Percentage) val).getValue();
        if (bdVal == null) bdVal = BigDecimal.ZERO;
        slider.setValue((int) Math.round(bdVal.doubleValue() * 100));
        valueField.setText(bdVal.stripTrailingZeros().toPlainString());
    } else {
        slider.setValue(minValue * 100);
        valueField.setText(Integer.toString(minValue));
    }
    
    slider.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
            slider.setSnapToTicks(true);
        }
        @Override
        public void mouseReleased(java.awt.event.MouseEvent e) {
            slider.setSnapToTicks(false);
        }
    });

    slider.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        if (isUpdating) return;
        isUpdating = true;
        try {
          property.setChanged(true);
          setBackground(oldBg);
          slider.setBackground(oldBg);
          BigDecimal bd = BigDecimal.valueOf(slider.getValue()).divide(BigDecimal.valueOf(100)).stripTrailingZeros();
          property.setValue(new Percentage(bd));
          valueField.setText(bd.toPlainString());
        } finally {
          isUpdating = false;
        }
      }
    });

    valueField.setColumns(4);
    valueField.addKeyListener(new KeyAdapter() {

      @Override
      public void keyReleased(KeyEvent e) {
        try {
          double newPosition = Double.parseDouble(valueField.getText());
          if (newPosition >= minValue && newPosition <= maxValue) {
              isUpdating = true;
              try {
                  slider.setValue((int) Math.round(newPosition * 100));
                  property.setChanged(true);
                  setBackground(oldBg);
                  slider.setBackground(oldBg);
                  // Use string parsing directly to avoid double floating point inaccuracy
                  property.setValue(new Percentage(new BigDecimal(valueField.getText().trim())));
              } finally {
                  isUpdating = false;
              }
          }
        } catch (Exception ex) {
        }
      }
    });

    if (!property.isUnique()) {
      setBackground(Constants.MULTI_VALUE_COLOR);
      slider.setBackground(Constants.MULTI_VALUE_COLOR);
    }

    add(slider);
    add(valueField);
    add(new JLabel("%"));
  }
}
