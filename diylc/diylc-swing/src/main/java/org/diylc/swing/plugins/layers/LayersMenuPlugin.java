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
package org.diylc.swing.plugins.layers;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.core.LayerIds;
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
      final int layerId = layer.getLayerId();
      AbstractAction lockAction = new AbstractAction("Lock") {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          LayersMenuPlugin.this.plugInPort.setLayerLocked(layerId, (Boolean) getValue(Action.SELECTED_KEY));
//          selectAllActionMap.get(zOrder).setEnabled(!(Boolean) getValue(Action.SELECTED_KEY));
        }
      };
      lockAction.putValue(IView.CHECK_BOX_MENU_ITEM, true);
      lockActionMap.put(layer, lockAction);
      
      AbstractAction visibleAction = new AbstractAction("Visible") {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          LayersMenuPlugin.this.plugInPort.setLayerVisibility(layerId, getValue(Action.SELECTED_KEY) == null ? true : (Boolean) getValue(Action.SELECTED_KEY));
//          selectAllActionMap.get(zOrder).setEnabled(!(Boolean) getValue(Action.SELECTED_KEY));
        }
      };
      visibleAction.putValue(IView.CHECK_BOX_MENU_ITEM, true);
      // the shortcuts follow the menu order, Ctrl+1 for the first layer
      visibleAction.putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_1 + layer.ordinal(),
              Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
      visibleActionMap.put(layer, visibleAction);

      AbstractAction selectAllAction = new AbstractAction("Select All") {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          LayersMenuPlugin.this.plugInPort.selectAll(layerId);
        }
      };
      selectAllActionMap.put(layerId, selectAllAction);

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
        lockActionMap.get(layer).putValue(Action.SELECTED_KEY, lockedLayers.contains(layer.getLayerId()));
      }
    } else if (eventType == EventType.LAYER_VISIBILITY_CHANGED) {
      Set<Integer> hiddenLayers = (Set<Integer>) params[0];
      for (Layer layer : Layer.values()) {
        visibleActionMap.get(layer).putValue(Action.SELECTED_KEY, !hiddenLayers.contains(layer.getLayerId()));
      }
    }    
  }

  // in menu order, which also gives the shortcuts
  static enum Layer {
    CHASSIS("Chassis", IDIYComponent.CHASSIS), BOARD("Board", IDIYComponent.BOARD),
    BOTTOM_TRACES("Bottom Traces", LayerIds.BOTTOM_TRACES),
    TOP_TRACES("Top Traces", LayerIds.TOP_TRACES),
    COMPONENT("Component", IDIYComponent.COMPONENT), WIRING("Wiring", IDIYComponent.WIRING),
    TEXT("Text", IDIYComponent.TEXT);

    String title;
    int layerId;

    private Layer(String title, int layerId) {
      this.title = title;
      this.layerId = layerId;
    }

    public String getTitle() {
      return title;
    }

    public int getLayerId() {
      return layerId;
    }
  }
}
