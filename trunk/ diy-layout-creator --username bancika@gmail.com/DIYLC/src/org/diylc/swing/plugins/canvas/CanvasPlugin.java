package org.diylc.swing.plugins.canvas;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.EnumSet;

import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.diylc.common.BadPositionException;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.plugins.file.ProjectDrawingProvider;

import com.diyfever.gui.miscutils.ConfigurationManager;
import com.diyfever.gui.ruler.IRulerListener;
import com.diyfever.gui.ruler.RulerScrollPane;

public class CanvasPlugin implements IPlugIn {

	private static final Logger LOG = Logger.getLogger(CanvasPlugin.class);

	private static final String WHEEL_ZOOM = "wheelZoom";

	private RulerScrollPane scrollPane;
	private CanvasPanel canvasPanel;
	private IPlugInPort plugInPort;

	private ISwingUI swingUI;

	public CanvasPlugin(ISwingUI swingUI) {
		this.swingUI = swingUI;
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;
		try {
			swingUI.injectGUIComponent(getScrollPane(), SwingConstants.CENTER);
		} catch (BadPositionException e) {
			LOG.error("Could not install canvas plugin", e);
		}
	}

	public CanvasPanel getCanvasPanel() {
		if (canvasPanel == null) {
			canvasPanel = new CanvasPanel(plugInPort);
		}
		return canvasPanel;
	}

	private RulerScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new RulerScrollPane(getCanvasPanel(), new ProjectDrawingProvider(
					plugInPort, true), new Size(1d, SizeUnit.cm).convertToPixels(), new Size(1d,
					SizeUnit.in).convertToPixels());
			boolean metric = ConfigurationManager.getInstance().readBoolean(Presenter.METRIC_KEY,
					true);
			boolean wheelZoom = ConfigurationManager.getInstance().readBoolean(WHEEL_ZOOM, false);
			scrollPane.setMetric(metric);
			scrollPane.setWheelScrollingEnabled(!wheelZoom);
			scrollPane.addUnitListener(new IRulerListener() {

				@Override
				public void unitsChanged(boolean isMetric) {
					plugInPort.setMetric(isMetric);
				}
			});
			if (wheelZoom) {
				scrollPane.addMouseWheelListener(new MouseWheelListener() {

					@Override
					public void mouseWheelMoved(MouseWheelEvent e) {
						double d = plugInPort.getZoomLevel();
						Double[] availableZoomLevels = plugInPort.getAvailableZoomLevels();
						if (e.getWheelRotation() > 0) {
							int i = availableZoomLevels.length - 1;
							while (i > 0 && availableZoomLevels[i] >= d) {
								i--;
							}
							plugInPort.setZoomLevel(availableZoomLevels[i]);
						} else {
							int i = 0;
							while (i < availableZoomLevels.length - 1
									&& availableZoomLevels[i] <= d) {
								i++;
							}
							plugInPort.setZoomLevel(availableZoomLevels[i]);
						}
					}
				});
			}
		}
		return scrollPane;
	}

	@Override
	public EnumSet<EventType> getSubscribedEventTypes() {
		return EnumSet.of(EventType.PROJECT_LOADED, EventType.ZOOM_CHANGED, EventType.REPAINT);
	}

	@Override
	public void processMessage(final EventType eventType, Object... params) {
		// SwingUtilities.invokeLater(new Runnable() {
		//
		// @Override
		// public void run() {
		// LOG.debug("event: " + eventType);
		switch (eventType) {
		case PROJECT_LOADED:
			refreshSize();
			if ((Boolean) params[1]) {
				// Scroll to the center.
				Rectangle visibleRect = canvasPanel.getVisibleRect();
				visibleRect.setLocation((canvasPanel.getWidth() - visibleRect.width) / 2,
						(canvasPanel.getHeight() - visibleRect.height) / 2);
				canvasPanel.scrollRectToVisible(visibleRect);
				canvasPanel.revalidate();
			}
			// canvasPanel.validate();
			// canvasPanel.repaint();
			break;
		case ZOOM_CHANGED:
			refreshSize();
			break;
		case REPAINT:
			canvasPanel.repaint();
			break;
		}
		// }
		// });
	}

	private void refreshSize() {
		Dimension d = plugInPort.getCanvasDimensions(true);
		canvasPanel.setSize(d);
		canvasPanel.setPreferredSize(d);
		getScrollPane().setZoomLevel(plugInPort.getZoomLevel());
	}

	/**
	 * Causes ruller scroll pane to refresh by sending a fake mouse moved
	 * message to the canvasPanel.
	 */
	public void refresh() {
		MouseEvent event = new MouseEvent(canvasPanel, MouseEvent.MOUSE_MOVED, System
				.currentTimeMillis(), 0, 1, 1,// canvasPanel.getWidth() / 2,
				// canvasPanel.getHeight() / 2,
				0, false);
		canvasPanel.dispatchEvent(event);
	}
}
