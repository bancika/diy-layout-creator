package com.diyfever.diylc.plugins.toolbox;

import java.util.EnumSet;

import javax.swing.SwingConstants;

import com.diyfever.diylc.common.BadPositionException;
import com.diyfever.diylc.common.EventType;
import com.diyfever.diylc.common.IPlugIn;
import com.diyfever.diylc.common.IPlugInPort;

public class ToolBox implements IPlugIn {

	@Override
	public void connect(IPlugInPort plugInPort) {
		try {
			plugInPort.injectGUIComponent(new ComponentTabbedPane(plugInPort), SwingConstants.TOP);
		} catch (BadPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public EnumSet<EventType> getSubscribedEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processMessage(EventType eventType, Object... params) {
		// TODO Auto-generated method stub

	}
}
