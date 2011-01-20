package org.diylc.plugins.statusbar;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.diylc.common.BadPositionException;
import org.diylc.common.ComponentSelection;
import org.diylc.common.ComponentType;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.presenter.ComparatorFactory;
import org.diylc.presenter.Presenter;

import com.diyfever.gui.MemoryBar;
import com.diyfever.gui.miscutils.ConfigurationManager;
import com.diyfever.gui.miscutils.PercentageListCellRenderer;
import com.diyfever.gui.miscutils.Utils;
import com.diyfever.gui.update.UpdateLabel;

public class StatusBar extends JPanel implements IPlugIn {

	private static final long serialVersionUID = 1L;

	public static String UPDATE_URL = "http://www.diy-fever.com/update.xml";
	private static final Format sizeFormat = new DecimalFormat("0.00");

	private JComboBox zoomBox;
	private UpdateLabel updateLabel;
	private MemoryBar memoryPanel;
	private JLabel statusLabel;
	private JLabel sizeLabel;

	private IPlugInPort plugInPort;

	// State variables
	private ComponentType componentSlot;
	private Point controlPointSlot;
	private List<IDIYComponent<?>> componentsUnderCursor;
	private List<String> selectedComponentNames;
	private List<String> stuckComponentNames;

	public StatusBar() {
		super();

		// setLayout(new FlowLayout(FlowLayout.TRAILING));
		setLayout(new GridBagLayout());
	}

	private JComboBox getZoomBox() {
		if (zoomBox == null) {
			zoomBox = new JComboBox(plugInPort.getAvailableZoomLevels());
			zoomBox.setSelectedItem(plugInPort.getZoomLevel());
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
			updateLabel = new UpdateLabel(plugInPort.getCurrentVersionNumber(), UPDATE_URL);
			// updateLabel.setBorder(BorderFactory.createCompoundBorder(
			// BorderFactory.createEtchedBorder(), BorderFactory
			// .createEmptyBorder(2, 4, 2, 4)));
			updateLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		}
		return updateLabel;
	}

	private MemoryBar getMemoryPanel() {
		if (memoryPanel == null) {
			memoryPanel = new MemoryBar(true);
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

	public JLabel getSizeLabel() {
		if (sizeLabel == null) {
			sizeLabel = new JLabel("N/A");
			sizeLabel.setToolTipText("Selection Size");
			sizeLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(
					0, 1, 0, 1, UIManager.getColor("Separator.shadow")), BorderFactory
					.createEmptyBorder(0, 4, 0, 4)));
		}
		return sizeLabel;
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
		add(getSizeLabel(), gbc);

		gbc.gridx = 2;
		add(zoomPanel, gbc);

		gbc.gridx = 3;
		add(getUpdateLabel(), gbc);

		gbc.gridx = 4;
		gbc.fill = GridBagConstraints.NONE;
		add(getMemoryPanel(), gbc);

		gbc.gridx = 5;
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
		return EnumSet.of(EventType.ZOOM_CHANGED, EventType.SLOT_CHANGED,
				EventType.AVAILABLE_CTRL_POINTS_CHANGED, EventType.SELECTION_SIZE_CHANGED,
				EventType.SELECTION_CHANGED);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processMessage(EventType eventType, Object... params) {
		switch (eventType) {
		case ZOOM_CHANGED:
			if (!params[0].equals(getZoomBox().getSelectedItem())) {
				final Double zoom = (Double) params[0];
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						getZoomBox().setSelectedItem(zoom);
					}
				});
			}
			break;
		case SELECTION_CHANGED:
			ComponentSelection selection = (ComponentSelection) params[0];
			Collection<IDIYComponent<?>> stuckComponents = (Collection<IDIYComponent<?>>) params[1];
			Collection<String> componentNames = new HashSet<String>();
			for (IDIYComponent<?> component : selection) {
				componentNames.add("<font color='blue'>" + component.getName() + "</font>");
			}
			this.selectedComponentNames = new ArrayList<String>(componentNames);
			Collections.sort(this.selectedComponentNames);
			this.stuckComponentNames = new ArrayList<String>();
			for (IDIYComponent<?> component : stuckComponents) {
				this.stuckComponentNames.add("<font color='blue'>" + component.getName()
						+ "</font>");
			}
			this.stuckComponentNames.removeAll(this.selectedComponentNames);
			Collections.sort(this.stuckComponentNames);
			refreshStatusText();
			break;
		case SELECTION_SIZE_CHANGED:
			Point2D size = (Point2D) params[0];
			boolean metric = ConfigurationManager.getInstance().readBoolean(Presenter.METRIC_KEY,
					true);
			if (size == null) {
				getSizeLabel().setText("N/A");
			} else {
				getSizeLabel().setText(
						sizeFormat.format(size.getX()) + " x " + sizeFormat.format(size.getY())
								+ (metric ? " cm" : " in"));
			}
			break;
		case SLOT_CHANGED:
			componentSlot = (ComponentType) params[0];
			controlPointSlot = (Point) params[1];
			refreshStatusText();
			break;
		case AVAILABLE_CTRL_POINTS_CHANGED:
			componentsUnderCursor = new ArrayList<IDIYComponent<?>>(
					((Map<IDIYComponent<?>, Integer>) params[0]).keySet());
			Collections.sort(componentsUnderCursor, ComparatorFactory.getInstance()
					.getComponentNameComparator());
			refreshStatusText();
			break;
		}
	}

	private void refreshStatusText() {
		String statusText = "";
		if (componentSlot == null) {
			if (componentsUnderCursor != null && !componentsUnderCursor.isEmpty()) {
				String formattedNames = Utils.toCommaString(componentsUnderCursor);
				statusText = "Drag control point(s) of " + formattedNames;
			} else if (selectedComponentNames != null && !selectedComponentNames.isEmpty()) {
				StringBuilder builder = new StringBuilder();
				builder.append(Utils.toCommaString(selectedComponentNames));
				if (!stuckComponentNames.isEmpty()) {
					builder.append(" (hold <b>Ctrl</b> to unstuck from ");
					builder.append(Utils.toCommaString(stuckComponentNames));
					builder.append(")");
				}
				statusText = "<html>Selection: " + builder.toString() + "</html>";
			} else {
				statusText = "";
			}
		} else {
			switch (componentSlot.getCreationMethod()) {
			case POINT_BY_POINT:
				String count;
				if (controlPointSlot == null) {
					count = "first";
				} else {
					count = "second";
				}
				statusText = "Click on the canvas to set the " + count + " control point of a new "
						+ componentSlot.getName() + " or press Esc to cancel";
				break;
			case SINGLE_CLICK:
				statusText = "Click on the canvas to create a new " + componentSlot.getName()
						+ " or press Esc to cancel";
				break;
			}
		}
		final String finalStatus = statusText;
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				getStatusLabel().setText(finalStatus);
			}
		});
	}
}
