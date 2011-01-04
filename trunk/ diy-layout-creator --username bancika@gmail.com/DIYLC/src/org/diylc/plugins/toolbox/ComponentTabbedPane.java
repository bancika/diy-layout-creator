package org.diylc.plugins.toolbox;

import java.awt.Container;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTabbedPane;

import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;


class ComponentTabbedPane extends JTabbedPane {

	private static final long serialVersionUID = 1L;
	private final IPlugInPort plugInPort;

	public ComponentTabbedPane(IPlugInPort plugInPort) {
		super();
		this.plugInPort = plugInPort;
		Map<String, List<ComponentType>> componentTypes = plugInPort.getComponentTypes();
		for (Map.Entry<String, List<ComponentType>> entry : componentTypes.entrySet()) {
			addTab(entry.getKey(), createComponentPanel(entry.getValue()));
		}
	}

	private Container createComponentPanel(List<ComponentType> componentTypes) {
		Container componentPanel = new Container();
		componentPanel.setLayout(new BoxLayout(componentPanel, BoxLayout.X_AXIS));
		for (ComponentType componentType : componentTypes) {
			JButton button = new ComponentButton(plugInPort, componentType);
			componentPanel.add(button);
		}
		return componentPanel;
	}
}
