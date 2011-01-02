package com.diyfever.diylc.utils.dnd;

/* Copyright 2006 Timothy Wall All Rights Reserved. */
import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Provide a ghosted drag image which will appear on any instances of
 * {@link RootPaneContainer} in the current VM. Its location in screen
 * coordinates may be set via {@link #move}.
 * <p>
 * When the image is no longer needed, invoke {@link #dispose}, which hides the
 * graphic immediately, or {@link #returnToOrigin}, which moves the image to its
 * original location prior to invoking {@link #dispose}.
 */
public class GhostedDragImage extends AbstractComponentDecorator {

	private Icon icon;
	// drag origin, relative to drag source
	private Point origin;
	// current drag location, relative to drag source
	private Point location;
	// offset of the image from the cursor
	private Point imageOffset;
	private List ghosts = new ArrayList();
	private Component dragSource;
	private float ghostAlpha = 0.5f;

	/**
	 * Create a ghosted drag image, using the given icon.
	 * 
	 * @param dragSource
	 *            source of the drag
	 * @param screenLocation
	 *            screen location where the drag started
	 * @param icon
	 *            image to be drawn
	 * @param imageOffset
	 *            offset of the image from the cursor
	 */
	public GhostedDragImage(JComponent dragSource, Point screenLocation, Icon icon,
			Point imageOffset) {
		this(dragSource, dragSource.getRootPane().getLayeredPane(), screenLocation, icon,
				imageOffset, true);
	}

	/**
	 * Create a ghosted drag image.
	 * 
	 * @param dragSource
	 *            source of the drag
	 * @param root
	 *            layered pane on which ghosted image is drawn
	 * @param screenLocation
	 *            initial location of image, in screen coordinates
	 * @param icon
	 *            icon to use for the ghost image
	 * @param imageOffset
	 *            offset of the image within the drag source
	 * @param trackFrames
	 *            if true, creates additional ghosts for all extant frames which
	 *            contain a {@link JLayeredPane} where the image can be painted.
	 */
	protected GhostedDragImage(JComponent dragSource, JLayeredPane root, Point screenLocation,
			Icon icon, Point imageOffset, boolean trackFrames) {
		super(root);
		this.dragSource = dragSource;
		this.icon = icon;
		this.imageOffset = imageOffset;
		Point loc = root.getLocationOnScreen();
		setLocation(screenLocation.x - loc.x + imageOffset.x, screenLocation.y - loc.y
				+ imageOffset.y);
		if (trackFrames) {
			Window w = SwingUtilities.getWindowAncestor(root);
			Frame[] frames = Frame.getFrames();
			// Log.debug("track " + frames.length + " other frames");
			for (int i = 0; i < frames.length; i++) {
				Frame frame = frames[i];
				if (frame instanceof RootPaneContainer && frame.isShowing() && frame != w) {
					// Log.debug("Track " + frame);
					JLayeredPane p = ((RootPaneContainer) frame).getLayeredPane();
					GhostedDragImage slave = new GhostedDragImage(dragSource, p, screenLocation,
							icon, imageOffset, false);
					ghosts.add(slave);
					slave.move(screenLocation);
				}
			}
		}
	}

	/** Set the transparency of the ghosted image. */
	public void setAlpha(float alpha) {
		ghostAlpha = alpha;
	}

	/**
	 * Ensure the decorator cursor matches the drag cursor, or we get cursor
	 * flicker when autoscrolling.
	 */
	public void setCursor(Cursor cursor) {
		super.setCursor(cursor);
		for (Iterator i = ghosts.iterator(); i.hasNext();) {
			GhostedDragImage slave = (GhostedDragImage) i.next();
			slave.setCursor(cursor);
		}
	}

	/** Make all ghosted images go away. */
	public void dispose() {
		location = origin;
		super.dispose();
		for (Iterator i = ghosts.iterator(); i.hasNext();) {
			GhostedDragImage slave = (GhostedDragImage) i.next();
			slave.dispose();
		}
	}

	/**
	 * Sets the location of the decoration within the layered pane. Keeps track
	 * of the location relative to the drag source to facilitate moving back
	 * with {@link #returnToOrigin}.
	 */
	private void setLocation(int x, int y) {
		location = SwingUtilities.convertPoint(getComponent(), x, y, dragSource);
		if (origin == null)
			origin = location;
		setDecorationBounds(x, y, icon.getIconWidth(), icon.getIconHeight());
	}

	// Setting this non-zero provides some buffer around the drag image
	// which helps avoid cursor flicker as the cursor moves (especially
	// repomgr, probably due to slow repaint times).
	private static final int CURSOR_SIZE = 32;

	/**
	 * Adjust the bounds of the painting component to allow some buffer to avoid
	 * cursor flicker when moving.
	 */
	protected Rectangle clipDecorationBounds(Rectangle decorated) {
		decorated.x -= CURSOR_SIZE;
		decorated.y -= CURSOR_SIZE;
		decorated.width += CURSOR_SIZE * 2;
		decorated.height += CURSOR_SIZE * 2;
		return super.clipDecorationBounds(decorated);
	}

	/**
	 * Move the ghosted image to the requested location.
	 * 
	 * @param screen
	 *            Where to draw the image, in screen coordinates
	 */
	public void move(Point screen) {
		// Log.debug("Move to " + screen);
		Point loc = getComponent().getLocationOnScreen();
		setLocation(screen.x - loc.x + imageOffset.x, screen.y - loc.y + imageOffset.y);
		for (Iterator i = ghosts.iterator(); i.hasNext();) {
			GhostedDragImage g = (GhostedDragImage) i.next();
			// Log.debug("Move on other frame");
			g.move(screen);
		}
	}

	/** Paint the supplied image with transparency. */
	public void paint(Graphics graphics) {
		Rectangle r = getDecorationBounds();
		Graphics2D g = (Graphics2D) graphics.create();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ghostAlpha));
		g.translate(CURSOR_SIZE, CURSOR_SIZE);
		icon.paintIcon(getPainter(), g, r.x, r.y);
		g.dispose();
	}

	private static final int SLIDE_INTERVAL = 1000 / 24;

	/** Animate the ghosted image returning to its origin. */
	public void returnToOrigin() {
		setCursor(null);
		final Timer timer = new Timer(SLIDE_INTERVAL, null);
		timer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int dx = (origin.x - location.x) / 2;
				int dy = (origin.y - location.y) / 2;
				if (dx != 0 || dy != 0) {
					Point loc = dragSource.getLocationOnScreen();
					Point where = new Point(loc.x + location.x + dx - imageOffset.x, loc.y
							+ location.y + dy - imageOffset.y);
					move(where);
				} else {
					timer.stop();
					dispose();
				}
			}
		});
		timer.start();
	}
}