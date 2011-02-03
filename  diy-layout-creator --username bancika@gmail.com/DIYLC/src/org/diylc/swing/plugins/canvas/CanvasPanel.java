package org.diylc.swing.plugins.canvas;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.VolatileImage;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.diylc.common.DrawOption;
import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.editor.PropertyEditorDialog;

import com.diyfever.gui.ButtonDialog;

/**
 * GUI class used to draw onto.
 * 
 * @author Branislav Stojkovic
 */
class CanvasPanel extends JComponent implements Autoscroll {

	private static final long serialVersionUID = 1L;

	private IPlugInPort plugInPort;

	private Image bufferImage;
	private GraphicsConfiguration screenGraphicsConfiguration;

	private static boolean USE_HARDWARE_ACCELLERATION = false;

	static final EnumSet<DrawOption> DRAW_OPTIONS = EnumSet.of(DrawOption.GRID,
			DrawOption.SELECTION, DrawOption.ZOOM, DrawOption.ANTIALIASING,
			DrawOption.CONTROL_POINTS);

	public CanvasPanel(IPlugInPort plugInPort) {
		super();
		this.plugInPort = plugInPort;
		setFocusable(true);
		initializeListeners();
		initializeDnD();
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = graphicsEnvironment.getScreenDevices();
		screenGraphicsConfiguration = devices[0].getDefaultConfiguration();

		initializeActions();
	}

	public void invalidateCache() {
		bufferImage = null;
	}

	private void initializeDnD() {
		// Initialize drag source recognizer.
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK, new CanvasGestureListener(plugInPort));
		// Initialize drop target.
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE,
				new CanvasTargetListener(plugInPort), true);
	}

	private void initializeActions() {
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSlot");
		getActionMap().put("clearSlot", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				CanvasPanel.this.plugInPort.setNewComponentTypeSlot(null);
			}
		});
	}

	protected void createBufferImage() {
		if (USE_HARDWARE_ACCELLERATION) {
			bufferImage = screenGraphicsConfiguration.createCompatibleVolatileImage(getWidth(),
					getHeight());
			((VolatileImage) bufferImage).validate(screenGraphicsConfiguration);
		} else {
			bufferImage = createImage(getWidth(), getHeight());
		}
		Graphics2D g2d = (Graphics2D) bufferImage.getGraphics();
		plugInPort.draw(g2d, DRAW_OPTIONS, null);
	}

	@Override
	public void paint(Graphics g) {
		if (plugInPort == null) {
			return;
		}
		if (bufferImage == null) {
			createBufferImage();
		} else {
			Graphics2D g2d = (Graphics2D) bufferImage.getGraphics();
			plugInPort.draw(g2d, DRAW_OPTIONS, null);
		}
		if (USE_HARDWARE_ACCELLERATION) {
			VolatileImage volatileImage = (VolatileImage) bufferImage;
			do {
				try {
					if (volatileImage.contentsLost()) {
						createBufferImage();
					}
//					int validation = volatileImage.validate(screenGraphicsConfiguration);
//					if (validation == VolatileImage.IMAGE_INCOMPATIBLE) {
//						createBufferImage();
//					}
					g.drawImage(bufferImage, 0, 0, this);
				} catch (NullPointerException e) {
					createBufferImage();
				}
			} while (volatileImage == null || volatileImage.contentsLost());
		} else {
			g.drawImage(bufferImage, 0, 0, this);
//			bufferImage.flush();
		}
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	private void initializeListeners() {
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				invalidateCache();
				invalidate();
			}
		});
//		addKeyListener(new KeyAdapter() {
//
//			@Override
//			public void keyPressed(KeyEvent e) {
//				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
//					plugInPort.deleteSelectedComponents();
//				}
//				// plugInPort.mouseMoved(getMousePosition(), e.isControlDown(),
//				// e.isShiftDown(), e
//				// .isAltDown());
//			}
//		});
		addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				setCursor(plugInPort.getCursorAt(e.getPoint()));
				plugInPort.mouseMoved(e.getPoint(), e.isControlDown(), e.isShiftDown(), e
						.isAltDown());
			}
		});
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (e.getClickCount() == 2) {
						List<PropertyWrapper> properties = plugInPort
								.getMutualSelectionProperties();
						if (properties == null || properties.isEmpty()) {
							// properties = plugInPort.getProjectProperties();
							// PropertyEditorDialog editor =
							// DialogFactory.getInstance()
							// .createPropertyEditorDialog(properties,
							// "Edit Project");
							// editor.setVisible(true);
							// if
							// (ButtonDialog.OK.equals(editor.getSelectedButtonCaption()))
							// {
							// plugInPort.applyPropertiesToProject(properties);
							// // Save default values.
							// for (PropertyWrapper property :
							// editor.getDefaultedProperties()) {
							// if (property.getValue() != null) {
							// plugInPort.setProjectDefaultPropertyValue(property.getName(),
							// property.getValue());
							// }
							// }
							// }
						} else {
							PropertyEditorDialog editor = DialogFactory.getInstance()
									.createPropertyEditorDialog(properties, "Edit Selection");
							editor.setVisible(true);
							if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
								try {
									plugInPort.applyPropertiesToSelection(properties);
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								// Save default values.
								for (PropertyWrapper property : editor.getDefaultedProperties()) {
									if (property.getValue() != null) {
										plugInPort.setSelectionDefaultPropertyValue(property.getName(),
												property.getValue());
									}
								}
							}
						}
					} else {
						plugInPort.mouseClicked(e.getPoint(), e.isControlDown(), e.isShiftDown(), e
								.isAltDown());
					}
				}
			}
		});
	}

	// Autoscroll

	@Override
	public void autoscroll(Point cursorLocn) {
		scrollRectToVisible(new Rectangle(cursorLocn.x - 15, cursorLocn.y - 15, 30, 30));
	}

	@Override
	public Insets getAutoscrollInsets() {
		Rectangle rect = getVisibleRect();
		return new Insets(rect.y - 15, rect.x - 15, rect.y + rect.height + 15, rect.x + rect.width
				+ 15);
	}
}
