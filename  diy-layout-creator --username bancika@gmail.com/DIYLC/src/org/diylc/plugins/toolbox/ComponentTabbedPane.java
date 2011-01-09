package org.diylc.plugins.toolbox;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;

import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.presenter.ComparatorFactory;
import org.diylc.presenter.Presenter;

/**
 * Tabbed pane that shows all available components categorized into tabs.
 * 
 * @author Branislav Stojkovic
 */
class ComponentTabbedPane extends JTabbedPane {

	public static int SCROLL_STEP = Presenter.ICON_SIZE + ComponentButton.MARGIN * 2 + 2;

	private static final long serialVersionUID = 1L;
	private final IPlugInPort plugInPort;

	public ComponentTabbedPane(IPlugInPort plugInPort) {
		super();
		this.plugInPort = plugInPort;
		Map<String, List<ComponentType>> componentTypes = plugInPort.getComponentTypes();
		List<String> categories = new ArrayList<String>(componentTypes.keySet());
		Collections.sort(categories);
		for (String category : categories) {
			JPanel panel = createTab((componentTypes.get(category)));
			addTab(category, panel);
		}
		setPreferredSize(new Dimension(-1, Presenter.ICON_SIZE + ComponentButton.MARGIN * 2 + 38));
	}

	private JPanel createTab(List<ComponentType> componentTypes) {
		JPanel panel = new JPanel(new BorderLayout());
		final JScrollPane scrollPane = createComponentScrollBar(componentTypes);
		panel.setOpaque(false);
		JButton leftButton = new JButton("<");
		leftButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Rectangle rect = scrollPane.getVisibleRect();
				if (rect.x > SCROLL_STEP) {
					rect.translate(-SCROLL_STEP, 0);
				} else {
					rect.translate(-rect.x, 0);
				}
			}
		});
		leftButton.setMargin(new Insets(0, 2, 0, 2));
		JButton rightButton = new JButton(">");
		rightButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Rectangle rect = scrollPane.getVisibleRect();
				if (rect.x + rect.width < scrollPane.getViewport().getSize().width - SCROLL_STEP) {
					rect.translate(SCROLL_STEP, 0);
				} else {
					rect.translate(scrollPane.getViewport().getSize().width - rect.x - rect.width,
							0);
				}
			}
		});
		rightButton.setMargin(new Insets(0, 2, 0, 2));
		panel.add(leftButton, BorderLayout.WEST);
		panel.add(scrollPane);
		panel.add(rightButton, BorderLayout.EAST);
		return panel;
	}

	private JScrollPane createComponentScrollBar(List<ComponentType> componentTypes) {
		JScrollPane scrollPane = new JScrollPane(createComponentPanel(componentTypes));
		scrollPane.setOpaque(false);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		return scrollPane;
	}

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
}
