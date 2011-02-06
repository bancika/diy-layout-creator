package org.diylc.swing.plugins.config;

import java.util.EnumSet;

import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;

/**
 * Controls configuration menu.
 * 
 * @author Branislav Stojkovic
 */
public class ConfigPlugin implements IPlugIn {

	private static final String CONFIG_MENU = "Config";

	private ISwingUI swingUI;

	public ConfigPlugin(ISwingUI swingUI) {
		super();
		this.swingUI = swingUI;
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		swingUI.injectMenuAction(ActionFactory.getInstance().createConfigAction(plugInPort,
				"Anti-Aliasing", IPlugInPort.ANTI_ALIASING_KEY, true), CONFIG_MENU);
		swingUI.injectMenuAction(ActionFactory.getInstance().createConfigAction(plugInPort,
				"Hi-Quality Rendering", IPlugInPort.HI_QUALITY_RENDER_KEY, false), CONFIG_MENU);
	}

	@Override
	public EnumSet<EventType> getSubscribedEventTypes() {
		return EnumSet.noneOf(EventType.class);
	}

	@Override
	public void processMessage(EventType eventType, Object... params) {
	}
}
