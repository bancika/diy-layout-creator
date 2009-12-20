package com.diyfever.diylc.plugins.statusbar;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.diyfever.diylc.common.BadPositionException;
import com.diyfever.diylc.common.EventType;
import com.diyfever.diylc.common.IPlugIn;
import com.diyfever.diylc.common.IPlugInPort;
import com.diyfever.diylc.model.IComponentType;
import com.diyfever.gui.MemoryBar;
import com.diyfever.gui.miscutils.PercentageListCellRenderer;
import com.diyfever.gui.update.UpdateLabel;

public class StatusBar extends JPanel implements IPlugIn {

	private static final long serialVersionUID = 1L;

	private JComboBox zoomBox;
	private UpdateLabel updateLabel;
	private MemoryBar memoryPanel;
	private JLabel statusLabel;

	private IPlugInPort plugInPort;

	public StatusBar() {
		super();

		// setLayout(new FlowLayout(FlowLayout.TRAILING));
		setLayout(new GridBagLayout());
	}

	private JComboBox getZoomBox() {
		if (zoomBox == null) {
			zoomBox = new JComboBox(new Double[] { 0.25d, 0.5d, 0.75d, 1d,
					1.5d, 2d });
			zoomBox.setSelectedItem(1d);
			zoomBox.setFocusable(false);
			zoomBox.setRenderer(new PercentageListCellRenderer());
			zoomBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					plugInPort.setZoomLevel((Double) zoomBox.getSelectedItem());
				}
			});
		}
		return zoomBox;
	}

	private UpdateLabel getUpdateLabel() {
		if (updateLabel == null) {
			updateLabel = new UpdateLabel(plugInPort.getCurrentVersionNumber(),
					"http://www.diy-fever.com/update.xml");
			// updateLabel.setBorder(BorderFactory.createCompoundBorder(
			// BorderFactory.createEtchedBorder(), BorderFactory
			// .createEmptyBorder(2, 4, 2, 4)));
			updateLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		}
		return updateLabel;
	}

	private MemoryBar getMemoryPanel() {
		if (memoryPanel == null) {
			memoryPanel = new MemoryBar();
		}
		return memoryPanel;
	}

	private JLabel getStatusLabel() {
		if (statusLabel == null) {
			statusLabel = new JLabel();
			statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		}
		return statusLabel;
	}

	private void layoutComponents() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		add(getStatusLabel(), gbc);

		JPanel zoomPanel = new JPanel(new BorderLayout());
		zoomPanel.add(new JLabel("Zoom: "), BorderLayout.WEST);
		zoomPanel.add(getZoomBox(), BorderLayout.CENTER);
		// zoomPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
		// .createEtchedBorder(), BorderFactory.createEmptyBorder(2, 4, 2,
		// 4)));
		zoomPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0;
		add(zoomPanel, gbc);

		gbc.gridx = 2;
		add(getUpdateLabel(), gbc);

		gbc.gridx = 3;
		gbc.fill = GridBagConstraints.NONE;
		add(getMemoryPanel(), gbc);

		gbc.gridx = 4;
		add(new JPanel(), gbc);
	}

	// IPlugIn

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;

		layoutComponents();

		try {
			plugInPort.injectGUIComponent(this, SwingUtilities.BOTTOM);
		} catch (BadPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public EnumSet<EventType> getSubscribedEventTypes() {
		return EnumSet.of(EventType.ZOOM_CHANGED, EventType.SLOT_CHANGED);
	}

	@Override
	public void processMessage(EventType eventType, Object... params) {
		switch (eventType) {
		case ZOOM_CHANGED:
			if (!params[0].equals(getZoomBox().getSelectedItem())) {
				getZoomBox().setSelectedItem(params[0]);
			}
			break;
		case SLOT_CHANGED:
			IComponentType componentType = (IComponentType) params[0];
			if (componentType == null) {
				getStatusLabel().setText("");
			} else {
				getStatusLabel().setText(
						"Click on the canvas to create a new "
								+ componentType.getName()
								+ " or press Esc to cancel");
			}
			break;
		}
	}
}
