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

	private static final String LOCK_LAYERS_TITLE = "Lock Layers";

	private IPlugInPort plugInPort;
	private Map<Layer, Action> lockActionMap;

	public LayersMenuPlugin(ISwingUI swingUI) {
		lockActionMap = new HashMap<Layer, Action>();
		for (Layer layer : Layer.values()) {
			final int zOrder = layer.getZOrder();
			AbstractAction action = new AbstractAction(layer.getTitle()) {

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					LayersMenuPlugin.this.plugInPort.setLayerLocked(zOrder,
							(Boolean) getValue(Action.SELECTED_KEY));
				}
			};
			action.putValue(IView.CHECK_BOX_MENU_ITEM, true);
			lockActionMap.put(layer, action);
			swingUI.injectMenuAction(action, LOCK_LAYERS_TITLE);
		}
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;
	}

	@Override
	public EnumSet<EventType> getSubscribedEventTypes() {
		return EnumSet.of(EventType.LAYER_STATE_CHANGED);
	}

	@SuppressWarnings("unchecked")
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
		CHASSIS("Chassis", IDIYComponent.CHASSIS), BOARD("Board", IDIYComponent.BOARD), TRACE(
				"Trace", IDIYComponent.TRACE), COMPONENT("Component", IDIYComponent.COMPONENT), TEXT(
				"Text", IDIYComponent.TEXT);

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
