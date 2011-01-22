package org.diylc.plugins.layers;

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

public class LayersMenuPlugin implements IPlugIn {

	private static final String LOCK_LAYERS_TITLE = "Lock Layers";

	private IPlugInPort plugInPort;
	private Map<Layer, Action> lockActionMap;

	public LayersMenuPlugin() {
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;

		lockActionMap = new HashMap<Layer, Action>();
		for (Layer layer : Layer.values()) {
			final int zOrder = layer.getZOrder();
			AbstractAction action = new AbstractAction(layer.getTitle()) {

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					LayersMenuPlugin.this.plugInPort.setLayerLocked(zOrder, true);
				}
			};
			action.putValue(IPlugInPort.CHECK_BOX_MENU_ITEM, true);
			lockActionMap.put(layer, action);
			plugInPort.injectMenuAction(action, LOCK_LAYERS_TITLE);
		}
	}

	@Override
	public EnumSet<EventType> getSubscribedEventTypes() {
		return EnumSet.of(EventType.LAYER_STATE_CHANGED);
	}

	@Override
	public void processMessage(EventType eventType, Object... params) {
		if (eventType == EventType.LAYER_STATE_CHANGED) {
			Set<Integer> lockedLayers = (Set<Integer>) params[0];
			for (Layer layer : Layer.values()) {
				lockActionMap.get(layer).putValue(Action.SELECTED_KEY,
						lockedLayers.contains(layer.getZOrder()));
			}
		}
	}

	static enum Layer {
		BELOW_CHASSIS("Below Chassis", IDIYComponent.BELOW_CHASSIS), CHASSIS("Chassis",
				IDIYComponent.CHASSIS), ABOVE_CHASSIS("Above Chassis", IDIYComponent.ABOVE_CHASSIS), BELOW_BOARD(
				"Below Board", IDIYComponent.BELOW_BOARD), BOARD("Board", IDIYComponent.BOARD), ABOVE_BOARD(
				"Above Board", IDIYComponent.ABOVE_BOARD), COMPONENT("Component",
				IDIYComponent.COMPONENT), ABOVE_COMPONENT("Above Component",
				IDIYComponent.ABOVE_COMPONENT);

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
