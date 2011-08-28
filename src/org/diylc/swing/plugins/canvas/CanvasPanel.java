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
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.diylc.common.DrawOption;
import org.diylc.common.IPlugInPort;

import com.diyfever.gui.miscutils.ConfigurationManager;

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

//	static final EnumSet<DrawOption> DRAW_OPTIONS = EnumSet.of(DrawOption.GRID,
//			DrawOption.SELECTION, DrawOption.ZOOM, DrawOption.CONTROL_POINTS);
//	static final EnumSet<DrawOption> DRAW_OPTIONS_ANTI_ALIASING = EnumSet.of(DrawOption.GRID,
//			DrawOption.SELECTION, DrawOption.ZOOM, DrawOption.ANTIALIASING,
//			DrawOption.CONTROL_POINTS);

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
				DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK,
				new CanvasGestureListener(plugInPort));
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
	}

	@Override
	public void paint(Graphics g) {
		if (plugInPort == null) {
			return;
		}
		if (bufferImage == null) {
			createBufferImage();
		}
		Graphics2D g2d = (Graphics2D) bufferImage.getGraphics();
		g2d.setClip(getVisibleRect());
		Set<DrawOption> drawOptions = EnumSet.of(DrawOption.GRID, DrawOption.SELECTION,
				DrawOption.ZOOM, DrawOption.CONTROL_POINTS);
		if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.ANTI_ALIASING_KEY, true)) {
			drawOptions.add(DrawOption.ANTIALIASING);
		}
		if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.OUTLINE_KEY, false)) {
			drawOptions.add(DrawOption.OUTLINE_MODE);
		}
		plugInPort.draw(g2d, drawOptions, null);
		if (USE_HARDWARE_ACCELLERATION) {
			VolatileImage volatileImage = (VolatileImage) bufferImage;
			do {
				try {
					if (volatileImage.contentsLost()) {
						createBufferImage();
					}
					// int validation =
					// volatileImage.validate(screenGraphicsConfiguration);
					// if (validation == VolatileImage.IMAGE_INCOMPATIBLE) {
					// createBufferImage();
					// }
					g.drawImage(bufferImage, 0, 0, this);
				} catch (NullPointerException e) {
					createBufferImage();
				}
			} while (volatileImage == null || volatileImage.contentsLost());
		} else {
			g.drawImage(bufferImage, 0, 0, this);
			// bufferImage.flush();
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
		// addKeyListener(new KeyAdapter() {
		//
		// @Override
		// public void keyPressed(KeyEvent e) {
		// if (e.getKeyCode() == KeyEvent.VK_DELETE) {
		// plugInPort.deleteSelectedComponents();
		// }
		// // plugInPort.mouseMoved(getMousePosition(), e.isControlDown(),
		// // e.isShiftDown(), e
		// // .isAltDown());
		// }
		// });
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
					plugInPort.mouseClicked(e.getPoint(), e.isControlDown(), e.isShiftDown(), e
							.isAltDown(), e.getClickCount());
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
