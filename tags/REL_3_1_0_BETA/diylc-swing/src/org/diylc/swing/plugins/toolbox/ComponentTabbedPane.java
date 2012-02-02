package org.diylc.swing.plugins.toolbox;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.presenter.ComparatorFactory;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.Presenter;

/**
 * Tabbed pane that shows all available components categorized into tabs.
 * 
 * @author Branislav Stojkovic
 */
class ComponentTabbedPane extends JTabbedPane {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(ComponentTabbedPane.class);

	public static int SCROLL_STEP = Presenter.ICON_SIZE + ComponentButton.MARGIN * 2 + 2;

	private final IPlugInPort plugInPort;

	public ComponentTabbedPane(IPlugInPort plugInPort) {
		super();
		this.plugInPort = plugInPort;
		addTab("Recently Used", createRecentComponentsPanel());
		Map<String, List<ComponentType>> componentTypes = plugInPort.getComponentTypes();
		List<String> categories = new ArrayList<String>(componentTypes.keySet());
		Collections.sort(categories);
		for (String category : categories) {
			JPanel panel = createTab((componentTypes.get(category)));
			addTab(category, panel);
		}
		addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				ComponentTabbedPane.this.plugInPort.setNewComponentTypeSlot(null);
			}
		});
	}

	private JPanel createTab(List<ComponentType> componentTypes) {
		JPanel panel = new JPanel(new BorderLayout());
		// final JScrollPane scrollPane =
		// createComponentScrollBar(componentTypes);
		panel.setOpaque(false);
		panel.add(createComponentPanel(componentTypes), BorderLayout.CENTER);
		// JButton leftButton = new JButton("<");
		// leftButton.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// Rectangle rect = scrollPane.getVisibleRect();
		// if (rect.x > SCROLL_STEP) {
		// rect.translate(-SCROLL_STEP, 0);
		// } else {
		// rect.translate(-rect.x, 0);
		// }
		// }
		// });
		// leftButton.setMargin(new Insets(0, 2, 0, 2));
		// JButton rightButton = new JButton(">");
		// rightButton.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// Rectangle rect = scrollPane.getVisibleRect();
		// if (rect.x + rect.width < scrollPane.getViewport().getSize().width -
		// SCROLL_STEP) {
		// rect.translate(SCROLL_STEP, 0);
		// } else {
		// rect.translate(scrollPane.getViewport().getSize().width - rect.x -
		// rect.width,
		// 0);
		// }
		// }
		// });
		// rightButton.setMargin(new Insets(0, 2, 0, 2));
		// panel.add(leftButton, BorderLayout.WEST);
		// panel.add(scrollPane);
		// panel.add(rightButton, BorderLayout.EAST);
		return panel;
	}

	// private JScrollPane createComponentScrollBar(List<ComponentType>
	// componentTypes) {
	// JScrollPane scrollPane = new
	// JScrollPane(createComponentPanel(componentTypes));
	// scrollPane.setOpaque(false);
	// scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	// scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	// return scrollPane;
	// }

	private Component createComponentPanel(List<ComponentType> componentTypes) {
		Container toolbar = new Container();
		Collections.sort(componentTypes, ComparatorFactory.getInstance()
				.getComponentTypeComparator());
		toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
		for (ComponentType componentType : componentTypes) {
			JButton button = new ComponentButton(plugInPort, componentType);
			toolbar.add(button);
		}

		return toolbar;
	}

	private Component createRecentComponentsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		final Container toolbar = new Container();
		toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
		refreshRecentComponentsToolbar(toolbar, (List<String>) ConfigurationManager.getInstance()
				.readObject(IPlugInPort.RECENT_COMPONENTS_KEY, new ArrayList<String>()));
		ConfigurationManager.getInstance().addConfigListener(IPlugInPort.RECENT_COMPONENTS_KEY,
				new IConfigListener() {

					@Override
					public void valueChanged(String key, Object value) {
						refreshRecentComponentsToolbar(toolbar, (List<String>) value);
						toolbar.invalidate();
					}
				});

		panel.add(toolbar, BorderLayout.CENTER);

		return panel;
	}

	private void refreshRecentComponentsToolbar(Container toolbar,
			List<String> recentComponentClassList) {
		toolbar.removeAll();
		for (String componentClassName : recentComponentClassList) {
			ComponentType componentType;
			try {
				componentType = ComponentProcessor.getInstance().extractComponentTypeFrom(
						(Class<? extends IDIYComponent<?>>) Class.forName(componentClassName));
				JButton button = new ComponentButton(plugInPort, componentType);
				toolbar.add(button);
			} catch (ClassNotFoundException e) {
				LOG.error("Could not create recent component button for " + componentClassName, e);
			}
		}
	}
}
