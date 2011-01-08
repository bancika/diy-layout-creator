package org.diylc.plugins.toolbox;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTabbedPane;

import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;

/**
 * Tabbed pane that shows all available components categorized into
 * tabs.
 * 
 * @author Branislav Stojkovic
 */
class ComponentTabbedPane extends JTabbedPane {

	private static final long serialVersionUID = 1L;
	private final IPlugInPort plugInPort;

	public ComponentTabbedPane(IPlugInPort plugInPort) {
		super();
		this.plugInPort = plugInPort;
		Map<String, List<ComponentType>> componentTypes = plugInPort.getComponentTypes();
		List<String> categories = new ArrayList<String>(componentTypes.keySet());
		Collections.sort(categories);
		for (String category : categories) {
			addTab(category, createComponentPanel(componentTypes.get(category)));
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
