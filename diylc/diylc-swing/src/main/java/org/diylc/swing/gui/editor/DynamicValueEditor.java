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

import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDynamicPropertySource;
import org.diylc.lang.LangUtil;
import org.diylc.utils.Constants;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

public class DynamicValueEditor extends JComboBox<Object> {

  private static final long serialVersionUID = 1L;

  private Color oldBg = getBackground();

  private final PropertyWrapper property;

  public DynamicValueEditor(final PropertyWrapper property) {
    this.property = property;
    IDynamicPropertySource dynamicPropertySource = property.getDynamicPropertySource();
    if (property.getOwnerObject() == null || !(property.getOwnerObject() instanceof IDIYComponent)) {
      setModel(new DefaultComboBoxModel<Object>(new Object[0]));
      setEnabled(false);
    } else {
      dynamicPropertySource.setComponent((IDIYComponent<?>) property.getOwnerObject());
      setModel(new DynamicComboBoxModel(dynamicPropertySource));
    }
    if (property.isReadOnly())
      setEnabled(false);

    setRenderer(new DefaultListCellRenderer() {

      private static final long serialVersionUID = 1L;
      
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        // TODO Auto-generated method stub
        return super.getListCellRendererComponent(list, LangUtil.translate(dynamicPropertySource.getDisplayValue(value)),
            index, isSelected, cellHasFocus);
      }
      
    });
    setSelectedItem(property.getValue());

    // Create a shared refresh action
    final Runnable refreshAction = () -> {
      if (getModel() instanceof DynamicComboBoxModel model) {
        model.refreshValues();
      }
    };

    addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        refreshAction.run();
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

      }

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {

      }
    });

    addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          property.setChanged(true);
          setBackground(oldBg);
          DynamicValueEditor.this.property.setValue(e.getItem());
        }
      }
    });
    if (!property.isUnique()) {
      setBackground(Constants.MULTI_VALUE_COLOR);
    }
  }

  class DynamicComboBoxModel implements ComboBoxModel<Object> {
    private final Vector<Object> items;
    private Object selectedItem;
    private final Vector<ListDataListener> listeners;
    private final IDynamicPropertySource dynamicPropertySource;

    public DynamicComboBoxModel(IDynamicPropertySource source) {
      this.dynamicPropertySource = source;
      this.items = new Vector<>();
      this.listeners = new Vector<>();
      refreshValues();
    }

    private void refreshValues() {
      if (dynamicPropertySource != null) {
        java.util.List<?> newValues = dynamicPropertySource.getAvailableValues();
        if (!items.equals(newValues)) {
          Object selected = selectedItem;
          items.clear();
          items.addAll(newValues);
          
          // Single update event
          ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, items.size());
          for (ListDataListener listener : listeners) {
            listener.contentsChanged(event);
          }

          // Restore selection if possible
          if (selected != null && items.contains(selected)) {
            selectedItem = selected;
          } else if (!items.isEmpty()) {
            selectedItem = items.get(0);
          } else {
            selectedItem = null;
          }
        }
      }
    }

    @Override
    public void setSelectedItem(Object anItem) {
      if ((selectedItem != null && !selectedItem.equals(anItem)) ||
          selectedItem == null && anItem != null) {
        selectedItem = anItem;
      }
    }

    @Override
    public Object getSelectedItem() {
      return selectedItem;
    }

    @Override
    public int getSize() {
      return items.size();
    }

    @Override
    public Object getElementAt(int index) {
      return items.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
      listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
      listeners.remove(l);
    }
  }
}
