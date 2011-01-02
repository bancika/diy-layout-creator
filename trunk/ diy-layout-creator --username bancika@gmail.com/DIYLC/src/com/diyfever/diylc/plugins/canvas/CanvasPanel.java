package com.diyfever.diylc.plugins.canvas;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
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

import com.diyfever.diylc.common.DrawOption;
import com.diyfever.diylc.common.IPlugInPort;
import com.diyfever.diylc.common.PropertyWrapper;
import com.diyfever.diylc.gui.DialogFactory;
import com.diyfever.diylc.gui.editor.PropertyEditorDialog;
import com.diyfever.gui.ButtonDialog;

/**
 * GUI class used to draw onto.
 * 
 * @author Branislav Stojkovic
 */
class CanvasPanel extends JComponent implements Autoscroll {

	private static final long serialVersionUID = 1L;

	private IPlugInPort plugInPort;

	private VolatileImage bufferImage;
	private GraphicsConfiguration screenGraphicsConfiguration;

	private static final EnumSet<DrawOption> drawOptions = EnumSet.of(DrawOption.GRID,
			DrawOption.SELECTION, DrawOption.ZOOM, DrawOption.ANTIALIASING,
			DrawOption.CONTROL_POINTS);

	public CanvasPanel(IPlugInPort plugInPort) {
		super();
		this.plugInPort = plugInPort;
		setBackground(Color.white);
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
				DnDConstants.ACTION_MOVE, new CanvasGestureListener(plugInPort));
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
				CanvasPanel.this.plugInPort.setNewComponentSlot(null);
			}
		});
	}

	protected void createBufferImage() {
		bufferImage = screenGraphicsConfiguration.createCompatibleVolatileImage(getWidth(),
				getHeight());
		bufferImage.validate(screenGraphicsConfiguration);
		Graphics2D g2d = (Graphics2D) bufferImage.getGraphics();
		plugInPort.draw(g2d, drawOptions);
	}

	@Override
	public void paint(Graphics g) {
		if (plugInPort == null) {
			return;
		}
		if (bufferImage == null) {
			createBufferImage();
		}
		do {
			if (bufferImage.contentsLost()) {
				createBufferImage();
			}
			int validation = bufferImage.validate(screenGraphicsConfiguration);
			if (validation == VolatileImage.IMAGE_INCOMPATIBLE) {
				createBufferImage();
			}
			g.drawImage(bufferImage, 0, 0, this);
		} while (bufferImage.contentsLost());
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
		addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				setCursor(plugInPort.getCursorAt(e.getPoint()));
				plugInPort.mouseMoved(e.getPoint());
			}
		});
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (e.getClickCount() == 2) {
						List<PropertyWrapper> properties = plugInPort
								.getMutualSelectionProperties();
						if (properties != null) {
							if (properties.isEmpty()) {

							} else {
								PropertyEditorDialog editor = DialogFactory.getInstance()
										.createPropertyEditorDialog(properties);
								editor.setVisible(true);
								if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
									try {
										plugInPort.applyPropertiesToSelection(properties);
									} catch (Exception e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									for (PropertyWrapper property : editor.getDefaultedProperties()) {
										if (property.getValue() != null) {
											plugInPort.setDefaultPropertyValue(property.getName(),
													property.getValue());
										}
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
