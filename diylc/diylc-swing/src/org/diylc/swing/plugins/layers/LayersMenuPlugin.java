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
package org.diylc.swing.plugins.layers;

import java.awt.event.ActionEvent;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.swing.ISwingUI;

public class LayersMenuPlugin implements IPlugIn {

  private static final String LOCK_LAYERS_TITLE = "Layers";

  private IPlugInPort plugInPort;
  private Map<Layer, Action> lockActionMap;
  private Map<Layer, Action> visibleActionMap;
  private Map<Integer, Action> selectAllActionMap;

  public LayersMenuPlugin(ISwingUI swingUI) {
    lockActionMap = new HashMap<Layer, Action>();
    visibleActionMap = new HashMap<Layer, Action>();
    selectAllActionMap = new HashMap<Integer, Action>();
    for (Layer layer : Layer.values()) {
      final int zOrder = layer.getZOrder();
      AbstractAction lockAction = new AbstractAction("Lock") {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          LayersMenuPlugin.this.plugInPort.setLayerLocked(zOrder, (Boolean) getValue(Action.SELECTED_KEY));
//          selectAllActionMap.get(zOrder).setEnabled(!(Boolean) getValue(Action.SELECTED_KEY));
        }
      };
      lockAction.putValue(IView.CHECK_BOX_MENU_ITEM, true);
      lockActionMap.put(layer, lockAction);
      
      AbstractAction visibleAction = new AbstractAction("Visible") {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          LayersMenuPlugin.this.plugInPort.setLayerVisibility(zOrder, getValue(Action.SELECTED_KEY) == null ? true : (Boolean) getValue(Action.SELECTED_KEY));
//          selectAllActionMap.get(zOrder).setEnabled(!(Boolean) getValue(Action.SELECTED_KEY));
        }
      };
      visibleAction.putValue(IView.CHECK_BOX_MENU_ITEM, true);
      visibleActionMap.put(layer, visibleAction);

      AbstractAction selectAllAction = new AbstractAction("Select All") {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          LayersMenuPlugin.this.plugInPort.selectAll(zOrder);
        }
      };
      selectAllActionMap.put(zOrder, selectAllAction);

      swingUI.injectSubmenu(layer.title, null, LOCK_LAYERS_TITLE);
      swingUI.injectMenuAction(visibleAction, layer.title);
      swingUI.injectMenuAction(lockAction, layer.title);
      swingUI.injectMenuAction(selectAllAction, layer.title);
    }
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.LAYER_STATE_CHANGED, EventType.LAYER_VISIBILITY_CHANGED);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void processMessage(EventType eventType, Object... params) {
    if (eventType == EventType.LAYER_STATE_CHANGED) {
      Set<Integer> lockedLayers = (Set<Integer>) params[0];
      for (Layer layer : Layer.values()) {
        lockActionMap.get(layer).putValue(Action.SELECTED_KEY, lockedLayers.contains(layer.getZOrder()));
      }
    } else if (eventType == EventType.LAYER_VISIBILITY_CHANGED) {
      Set<Integer> hiddenLayers = (Set<Integer>) params[0];
      for (Layer layer : Layer.values()) {
        visibleActionMap.get(layer).putValue(Action.SELECTED_KEY, !hiddenLayers.contains(layer.getZOrder()));
      }
    }    
  }

  static enum Layer {
    CHASSIS("Chassis", IDIYComponent.CHASSIS), BOARD("Board", IDIYComponent.BOARD), TRACE("Trace", IDIYComponent.TRACE), COMPONENT(
        "Component", IDIYComponent.COMPONENT), WIRING("Wiring", IDIYComponent.WIRING), TEXT("Text", IDIYComponent.TEXT);

    String title;
    int zOrder;

    private Layer(String title, int order) {
      this.title = title;
      zOrder = order;
    }

    public String getTitle() {
      return title;
    }

    public int getZOrder() {
      return zOrder;
    }
  }
}
