package org.diylc.swing.plugins.canvas;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.common.BadPositionException;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.ExpansionMode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.images.IconLoader;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.plugins.edit.ComponentTransferable;
import org.diylc.swing.plugins.file.ProjectDrawingProvider;
import org.diylc.swingframework.ruler.IRulerListener;
import org.diylc.swingframework.ruler.RulerScrollPane;

public class CanvasPlugin implements IPlugIn, ClipboardOwner {

	private static final Logger LOG = Logger.getLogger(CanvasPlugin.class);

	private RulerScrollPane scrollPane;
	private CanvasPanel canvasPanel;
	private JPopupMenu popupMenu;
	private JMenu selectionMenu;
	private JMenu expandMenu;

	private ActionFactory.CutAction cutAction;
	private ActionFactory.CopyAction copyAction;
	private ActionFactory.PasteAction pasteAction;
	private ActionFactory.EditSelectionAction editSelectionAction;
	private ActionFactory.DeleteSelectionAction deleteSelectionAction;
	private ActionFactory.SaveAsTemplateAction saveAsTemplateAction;
	private ActionFactory.GroupAction groupAction;
	private ActionFactory.UngroupAction ungroupAction;
	private ActionFactory.SendToBackAction sendToBackAction;
	private ActionFactory.BringToFrontAction bringToFrontAction;
	private ActionFactory.ExpandSelectionAction expandSelectionAllAction;
	private ActionFactory.ExpandSelectionAction expandSelectionImmediateAction;
	private ActionFactory.ExpandSelectionAction expandSelectionSameTypeAction;
	private ActionFactory.RotateSelectionAction rotateClockwiseAction;
	private ActionFactory.RotateSelectionAction rotateCounterclockwiseAction;

	private IPlugInPort plugInPort;
	private ISwingUI swingUI;

	private Clipboard clipboard;

	private double zoomLevel = 1;

	public CanvasPlugin(ISwingUI swingUI) {
		this.swingUI = swingUI;
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
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
			canvasPanel.addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					canvasPanel.requestFocus();
					mouseReleased(e);
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (plugInPort.getNewComponentTypeSlot() == null
							&& e.isPopupTrigger()) {
						// Enable actions.
						boolean enabled = !plugInPort.getSelectedComponents()
								.isEmpty();
						getCutAction().setEnabled(enabled);
						getCopyAction().setEnabled(enabled);
						try {
							getPasteAction()
									.setEnabled(
											clipboard
													.isDataFlavorAvailable(ComponentTransferable.listFlavor));
						} catch (Exception ex) {
							getPasteAction().setEnabled(false);
						}
						getEditSelectionAction().setEnabled(enabled);
						getDeleteSelectionAction().setEnabled(enabled);
						getExpandSelectionAllAction().setEnabled(enabled);
						getExpandSelectionImmediateAction().setEnabled(enabled);
						getExpandSelectionSameTypeAction().setEnabled(enabled);
						getGroupAction().setEnabled(enabled);
						getUngroupAction().setEnabled(enabled);
						getSendToBackAction().setEnabled(enabled);
						getBringToFrontAction().setEnabled(enabled);

						getSaveAsTemplateAction().setEnabled(
								plugInPort.getSelectedComponents().size() == 1);

						showPopupAt(e.getX(), e.getY());
					}
				}
			});

			canvasPanel.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {
					if (plugInPort.keyPressed(e.getKeyCode(),
							e.isControlDown(), e.isShiftDown(), e.isAltDown())) {
						e.consume();
					}
				}
			});

			canvasPanel.addMouseMotionListener(new MouseAdapter() {

				@Override
				public void mouseMoved(MouseEvent e) {
					canvasPanel.setCursor(plugInPort.getCursorAt(e.getPoint()));
					plugInPort.mouseMoved(e.getPoint(), e.isControlDown(), e
							.isShiftDown(), e.isAltDown());
				}
			});
			
			canvasPanel.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					plugInPort.mouseClicked(e.getPoint(), e.getButton(), e
							.isControlDown(), e.isShiftDown(), e.isAltDown(), e
							.getClickCount());
				}
			});
		}
		return canvasPanel;
	}

	private RulerScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new RulerScrollPane(getCanvasPanel(),
					new ProjectDrawingProvider(plugInPort, true, false),
					new Size(1d, SizeUnit.cm).convertToPixels(), new Size(1d,
							SizeUnit.in).convertToPixels());
			boolean metric = ConfigurationManager.getInstance().readBoolean(
					Presenter.METRIC_KEY, true);
			boolean wheelZoom = ConfigurationManager.getInstance().readBoolean(
					IPlugInPort.WHEEL_ZOOM_KEY, false);
			ConfigurationManager.getInstance().addConfigListener(
					IPlugInPort.WHEEL_ZOOM_KEY, new IConfigListener() {

						@Override
						public void valueChanged(String key, Object value) {
							scrollPane
									.setWheelScrollingEnabled(!(Boolean) value);
						}
					});
			scrollPane.setMetric(metric);
			scrollPane.setWheelScrollingEnabled(!wheelZoom);
			scrollPane.addUnitListener(new IRulerListener() {

				@Override
				public void unitsChanged(boolean isMetric) {
					plugInPort.setMetric(isMetric);
				}
			});
			scrollPane.addMouseWheelListener(new MouseWheelListener() {

				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					boolean wheelZoom = ConfigurationManager.getInstance()
							.readBoolean(IPlugInPort.WHEEL_ZOOM_KEY, false);
					if (!wheelZoom) {
						return;
					}
					double d = plugInPort.getZoomLevel();
					Double[] availableZoomLevels = plugInPort
							.getAvailableZoomLevels();
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
		return scrollPane;
	}

	private void showPopupAt(int x, int y) {
		updateSelectionMenu(x, y);
		getPopupMenu().show(canvasPanel, x, y);
	}

	public JPopupMenu getPopupMenu() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();
			popupMenu.add(getSelectionMenu());
			popupMenu.addSeparator();
			popupMenu.add(getCutAction());
			popupMenu.add(getCopyAction());
			popupMenu.add(getPasteAction());
			popupMenu.addSeparator();
			popupMenu.add(getEditSelectionAction());
			popupMenu.add(getDeleteSelectionAction());
			popupMenu.add(getRotateClockwiseAction());
			popupMenu.add(getRotateCounterclockwiseAction());
			popupMenu.add(getGroupAction());
			popupMenu.add(getUngroupAction());
			popupMenu.add(getSendToBackAction());
			popupMenu.add(getBringToFrontAction());
			popupMenu.add(getSaveAsTemplateAction());
			popupMenu.add(getExpandMenu());
			popupMenu.addSeparator();
			popupMenu.add(ActionFactory.getInstance().createEditProjectAction(
					plugInPort));
		}
		return popupMenu;
	}

	public JMenu getSelectionMenu() {
		if (selectionMenu == null) {
			selectionMenu = new JMenu("Select");
			selectionMenu.setIcon(IconLoader.ElementsSelection.getIcon());
		}
		return selectionMenu;
	}

	public JMenu getExpandMenu() {
		if (expandMenu == null) {
			expandMenu = new JMenu("Expand Selection");
			expandMenu.setIcon(IconLoader.BranchAdd.getIcon());
			expandMenu.add(getExpandSelectionAllAction());
			expandMenu.add(getExpandSelectionImmediateAction());
			expandMenu.add(getExpandSelectionSameTypeAction());
		}
		return expandMenu;
	}

	private void updateSelectionMenu(int x, int y) {
		getSelectionMenu().removeAll();
		for (IDIYComponent<?> component : plugInPort
				.findComponentsAt(new Point(x, y))) {
			JMenuItem item = new JMenuItem(component.getName());
			final IDIYComponent<?> finalComponent = component;
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
					newSelection.add(finalComponent);
					plugInPort.updateSelection(newSelection);
					plugInPort.refresh();
				}
			});
			getSelectionMenu().add(item);
		}
	}

	public ActionFactory.CutAction getCutAction() {
		if (cutAction == null) {
			cutAction = ActionFactory.getInstance().createCutAction(plugInPort,
					clipboard, this);
		}
		return cutAction;
	}

	public ActionFactory.CopyAction getCopyAction() {
		if (copyAction == null) {
			copyAction = ActionFactory.getInstance().createCopyAction(
					plugInPort, clipboard, this);
		}
		return copyAction;
	}

	public ActionFactory.PasteAction getPasteAction() {
		if (pasteAction == null) {
			pasteAction = ActionFactory.getInstance().createPasteAction(
					plugInPort, clipboard);
		}
		return pasteAction;
	}

	public ActionFactory.EditSelectionAction getEditSelectionAction() {
		if (editSelectionAction == null) {
			editSelectionAction = ActionFactory.getInstance()
					.createEditSelectionAction(plugInPort);
		}
		return editSelectionAction;
	}

	public ActionFactory.DeleteSelectionAction getDeleteSelectionAction() {
		if (deleteSelectionAction == null) {
			deleteSelectionAction = ActionFactory.getInstance()
					.createDeleteSelectionAction(plugInPort);
		}
		return deleteSelectionAction;
	}
	
	public ActionFactory.RotateSelectionAction getRotateClockwiseAction() {
		if (rotateClockwiseAction == null) {
			rotateClockwiseAction = ActionFactory.getInstance().createRotateSelectionAction(plugInPort, 1);
		}
		return rotateClockwiseAction;
	}
	
	public ActionFactory.RotateSelectionAction getRotateCounterclockwiseAction() {
		if (rotateCounterclockwiseAction == null) {
			rotateCounterclockwiseAction = ActionFactory.getInstance().createRotateSelectionAction(plugInPort, -1);
		}
		return rotateCounterclockwiseAction;
	}

	public ActionFactory.SaveAsTemplateAction getSaveAsTemplateAction() {
		if (saveAsTemplateAction == null) {
			saveAsTemplateAction = ActionFactory.getInstance()
					.createSaveAsTemplateAction(plugInPort);
		}
		return saveAsTemplateAction;
	}

	public ActionFactory.GroupAction getGroupAction() {
		if (groupAction == null) {
			groupAction = ActionFactory.getInstance().createGroupAction(
					plugInPort);
		}
		return groupAction;
	}

	public ActionFactory.UngroupAction getUngroupAction() {
		if (ungroupAction == null) {
			ungroupAction = ActionFactory.getInstance().createUngroupAction(
					plugInPort);
		}
		return ungroupAction;
	}

	public ActionFactory.SendToBackAction getSendToBackAction() {
		if (sendToBackAction == null) {
			sendToBackAction = ActionFactory.getInstance()
					.createSendToBackAction(plugInPort);
		}
		return sendToBackAction;
	}

	public ActionFactory.BringToFrontAction getBringToFrontAction() {
		if (bringToFrontAction == null) {
			bringToFrontAction = ActionFactory.getInstance()
					.createBringToFrontAction(plugInPort);
		}
		return bringToFrontAction;
	}

	public ActionFactory.ExpandSelectionAction getExpandSelectionAllAction() {
		if (expandSelectionAllAction == null) {
			expandSelectionAllAction = ActionFactory.getInstance()
					.createExpandSelectionAction(plugInPort, ExpansionMode.ALL);
		}
		return expandSelectionAllAction;
	}

	public ActionFactory.ExpandSelectionAction getExpandSelectionImmediateAction() {
		if (expandSelectionImmediateAction == null) {
			expandSelectionImmediateAction = ActionFactory.getInstance()
					.createExpandSelectionAction(plugInPort,
							ExpansionMode.IMMEDIATE);
		}
		return expandSelectionImmediateAction;
	}

	public ActionFactory.ExpandSelectionAction getExpandSelectionSameTypeAction() {
		if (expandSelectionSameTypeAction == null) {
			expandSelectionSameTypeAction = ActionFactory.getInstance()
					.createExpandSelectionAction(plugInPort,
							ExpansionMode.SAME_TYPE);
		}
		return expandSelectionSameTypeAction;
	}

	@Override
	public EnumSet<EventType> getSubscribedEventTypes() {
		return EnumSet.of(EventType.PROJECT_LOADED, EventType.ZOOM_CHANGED,
				EventType.REPAINT);
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
				visibleRect.setLocation(
						(canvasPanel.getWidth() - visibleRect.width) / 2,
						(canvasPanel.getHeight() - visibleRect.height) / 2);
				canvasPanel.scrollRectToVisible(visibleRect);
				canvasPanel.revalidate();
			}
			break;
		case ZOOM_CHANGED:
			Rectangle visibleRect = canvasPanel.getVisibleRect();
			refreshSize();
			// Try to set the visible area to be centered with the previous
			// one.
			double zoomFactor = (Double) params[0] / zoomLevel;
			visibleRect.setBounds((int) (visibleRect.x * zoomFactor),
					(int) (visibleRect.y * zoomFactor), visibleRect.width,
					visibleRect.height);
			canvasPanel.scrollRectToVisible(visibleRect);
			canvasPanel.revalidate();

			zoomLevel = (Double) params[0];
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
	 * Causes ruler scroll pane to refresh by sending a fake mouse moved message
	 * to the canvasPanel.
	 */
	public void refresh() {
		MouseEvent event = new MouseEvent(canvasPanel, MouseEvent.MOUSE_MOVED,
				System.currentTimeMillis(), 0, 1, 1,// canvasPanel.getWidth() /
				// 2,
				// canvasPanel.getHeight() / 2,
				0, false);
		canvasPanel.dispatchEvent(event);
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// TODO Auto-generated method stub

	}
}
