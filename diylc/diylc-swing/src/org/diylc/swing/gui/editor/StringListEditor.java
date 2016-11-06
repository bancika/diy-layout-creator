package org.diylc.swing.gui.editor;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.annotations.DynamicList;
import org.diylc.utils.Constants;

public class StringListEditor extends JComboBox {

  private static final long serialVersionUID = 1L;

  private Color oldBg = getBackground();

  private final PropertyWrapper property;

  public StringListEditor(final PropertyWrapper property) throws Exception {
    this.property = property;
    String listFunctionName = property.getGetter().getAnnotation(DynamicList.class).availableValueFunction();
    Method function = property.getOwnerObject().getClass().getMethod(listFunctionName);
    Object[] values = (Object[]) function.invoke(property.getOwnerObject());
    setModel(new DefaultComboBoxModel(values));
    setSelectedItem(property.getValue());
    addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          property.setChanged(true);
          setBackground(oldBg);
          StringListEditor.this.property.setValue(e.getItem());
        }
      }
    });
    if (!property.isUnique()) {
      setBackground(Constants.MULTI_VALUE_COLOR);
    }
  }
}
