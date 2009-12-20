package com.diyfever.diylc.plugins.toolbox;

import java.awt.Container;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTabbedPane;

import com.diyfever.diylc.common.IPlugInPort;
import com.diyfever.diylc.model.IComponentType;

class ComponentTabbedPane extends JTabbedPane {

	private static final long serialVersionUID = 1L;
	private final IPlugInPort plugInPort;

	public ComponentTabbedPane(IPlugInPort plugInPort) {
		super();
		this.plugInPort = plugInPort;
		Map<String, List<IComponentType>> componentTypes = plugInPort
				.getComponentTypes();
		for (Map.Entry<String, List<IComponentType>> entry : componentTypes
				.entrySet()) {
			addTab(entry.getKey(), createComponentPanel(entry.getValue()));
		}
	}

	private Container createComponentPanel(List<IComponentType> componentTypes) {
		Container componentPanel = new Container();
		componentPanel
				.setLayout(new BoxLayout(componentPanel, BoxLayout.X_AXIS));
		for (IComponentType componentType : componentTypes) {
			JButton button = new ComponentButton(plugInPort, componentType);
			componentPanel.add(button);
		}
		return componentPanel;
	}
}
