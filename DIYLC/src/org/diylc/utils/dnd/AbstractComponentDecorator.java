package org.diylc.utils.dnd;

/* Copyright (c) 2003-2006 Timothy Wall, All Rights Reserved */
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

/**
 * Provide a method for consistently augmenting the appearance of a given
 * component by painting something on it <i>after</i> the component itself gets
 * painted. If not explicitly removed via {@link #dispose}, an instance of this
 * object will live as long as its target component.
 * <p>
 * By default, the decorator matches the location and size of the decorated
 * component, but the bounds can be adjusted by overriding
 * {@link #getDecorationBounds()}. The {@link #synch()} method should be called
 * whenever the bounds returned by {@link #getDecorationBounds()} would change.
 * <p>
 * The decoration is clipped to the bounds set on the decoration, which does not
 * necessarily need to be the same as the decorated component bounds. The
 * decoration may extend beyond the decorated component bounds, or it may be
 * reduced to a smaller region.
 */
// TODO: should probably do some locking on Component.getTreeLock()
// TODO: synch underlying cursor when decorator covers more than
// one component; the cursor should change if the decoration exceeds the
// component's bounds
public abstract class AbstractComponentDecorator {
	public static final Rectangle DEFAULT_BOUNDS = null;
	public static final int TOP = 0;
	// Disabled for now, since it doesn't work properly
	private static final int BOTTOM = -1;
	/**
	 * Account for the difference between the decorator actual origin and the
	 * logical origin we want to pass to the {@link #paint} method.
	 */
	private Point originOffset = new Point(0, 0);

	private Painter painter;
	private JComponent component;
	private Container parent;
	private Listener listener;
	private int layerOffset;
	private int position;
	private Rectangle bounds;

	/** Create a decorator for the given component. */
	public AbstractComponentDecorator(JComponent c) {
		this(c, 1);
	}

	/**
	 * Create a decorator for the given component, indicating the layer offset
	 * from the target component. Negative values mean the decoration is painted
	 * <em>before</em> the target component is painted.
	 */
	public AbstractComponentDecorator(JComponent c, int layerOffset) {
		this(c, layerOffset, TOP);
	}

	/**
	 * Create a decorator with the given position within its layer. Use
	 * {@link #TOP} to cover other decorations, or {@link #BOTTOM} to be covered
	 * by other decorations.
	 * <p>
	 * WARNING: BOTTOM doesn't currently work, probably a JLayeredPane bug in
	 * either the code or documentation.
	 * 
	 * @see JLayeredPane
	 */
	public AbstractComponentDecorator(JComponent c, int layerOffset, int position) {
		component = c;
		this.layerOffset = layerOffset;
		this.position = position;
		this.bounds = DEFAULT_BOUNDS;
		parent = c.getParent();
		painter = new Painter();
		listener = new Listener();
		component.addHierarchyListener(listener);
		component.addComponentListener(listener);
		component.addPropertyChangeListener(listener);
		attach();
	}

	/**
	 * Set the text to be displayed when the mouse is over the decoration.
	 * 
	 * @see JComponent#setToolTipText(String)
	 */
	public void setToolTipText(String text) {
		painter.setToolTipText(text);
	}

	/**
	 * Return the currently set default tooltip text.
	 * 
	 * @see JComponent#setToolTipText
	 */
	public String getToolTipText() {
		return painter.getToolTipText();
	}

	/**
	 * Provide for different tool tips depending on the actual location over the
	 * decoration. Note that if you <em>only</em> override this method, you must
	 * also invoke {@link #setToolTipText(String)} with a non-<span
	 * class="javakeyword">null</span> argument.
	 * 
	 * @see JComponent#getToolTipText(MouseEvent)
	 */
	public String getToolTipText(MouseEvent e) {
		return getToolTipText();
	}

	/**
	 * Indicate whether any of the decoration is visible. The decoration may be
	 * clipped by ancestor scroll panes or by being moved outside if the visible
	 * region of its parent window.
	 */
	public boolean isVisible() {
		return painter.isVisible();
	}

	/** Use this to change the visibility of the decoration. */
	public void setVisible(boolean visible) {
		painter.setVisible(visible);
	}

	protected void attach() {
		Window w = SwingUtilities.getWindowAncestor(component);
		if (w instanceof RootPaneContainer) {
			JLayeredPane lp = ((RootPaneContainer) w).getLayeredPane();
			Component layeredChild = component;
			int layer = JLayeredPane.DRAG_LAYER.intValue();
			if (this instanceof BackgroundPainter) {
				layer = ((BackgroundPainter) this).layer;
				painter.setDecoratedLayer(layer);
			} else if (layeredChild == lp) {
				// Is this the best layer to use?
				painter.setDecoratedLayer(layer);
			} else {
				while (layeredChild.getParent() != lp) {
					layeredChild = layeredChild.getParent();
				}
				int base = lp.getLayer(layeredChild);
				// NOTE: JLayeredPane doesn't properly repaint an overlapping
				// child when an obscured child calls repaint() if the two
				// are in the same layer, so we use the next-higher layer
				// instead of simply using a different position within the
				// layer.
				layer = base + layerOffset;
				if (layerOffset < 0) {
					BackgroundPainter bp = (BackgroundPainter) lp
							.getClientProperty(BackgroundPainter.key(base));
					if (bp == null) {
						bp = new BackgroundPainter(lp, base);
					}
				}
				painter.setDecoratedLayer(base);
			}
			lp.add(painter, new Integer(layer), position);
		} else {
			// Always detach when the target component's window is null
			// or is not a suitable container,
			// otherwise we might prevent GC of the component
			Container parent = painter.getParent();
			if (parent != null) {
				parent.remove(painter);
			}
		}
		// Track size changes in any viewport parent
		if (parent != null) {
			parent.removeComponentListener(listener);
		}
		parent = component.getParent();
		if (parent != null) {
			parent.addComponentListener(listener);
		}
		synch();
	}

	/**
	 * Ensure the size of the decorator matches the current decoration bounds
	 * with appropriate clipping to viewports.
	 */
	protected void synch() {
		Container painterParent = painter.getParent();
		if (painterParent != null) {
			Rectangle decorated = getDecorationBounds();
			Rectangle clipRect = clipDecorationBounds(decorated);

			Point pt = SwingUtilities
					.convertPoint(component, clipRect.x, clipRect.y, painterParent);
			if (clipRect.width <= 0 || clipRect.height <= 0) {
				setPainterBounds(-1, -1, 0, 0);
				setVisible(false);
			} else {
				setPainterBounds(pt.x, pt.y, clipRect.width, clipRect.height);
				setVisible(true);
			}
		}
		painter.repaint();
	}

	/**
	 * Adjust the painting offsets and size of the decoration to account for
	 * ancestor clipping. This might be due to scroll panes or having the
	 * decoration lie outside the parent layered pane.
	 */
	protected Rectangle clipDecorationBounds(Rectangle decorated) {
		// Amount we have to translate the Graphics context
		originOffset.x = decorated.x;
		originOffset.y = decorated.y;
		// If the the component is obscured (by a viewport or some
		// other means), use the painter bounds to clip to the visible
		// bounds. Doing may change the actual origin, so adjust our
		// origin offset accordingly
		Rectangle visible = getClippingRect(component, decorated);
		Rectangle clipRect = decorated.intersection(visible);
		if (decorated.x < visible.x)
			originOffset.x += visible.x - decorated.x;
		if (decorated.y < visible.y)
			originOffset.y += visible.y - decorated.y;
		return clipRect;
	}

	/**
	 * Return any clipping rectangle detected above the given component, in the
	 * coordinate space of the given component. The given rectangle is desired
	 * to be visible.
	 */
	private Rectangle getClippingRect(Container component, Rectangle desired) {
		Rectangle clip = component instanceof JComponent ? ((JComponent) component)
				.getVisibleRect()
				: new Rectangle(0, 0, component.getWidth(), component.getHeight());
		if (desired.x >= clip.x && desired.y >= clip.y
				&& desired.x + desired.width <= clip.x + clip.width
				&& desired.y + desired.height <= clip.y + clip.height) {
			// desired rect is within the current clip rect
		} else if (component.getParent() != null) {
			// Only apply the clip if it is actually smaller than the
			// component's visible area
			if (component != painter.getParent() && clip.x > 0 && clip.y > 0
					&& clip.width < component.getWidth() && clip.height < component.getHeight()) {
				// Don't alter the original rectangle
				desired = new Rectangle(desired);
				// Check for clipping further up the hierarchy
				desired.x += component.getX();
				desired.y += component.getY();
				clip = getClippingRect(component.getParent(), desired);
				clip.x -= component.getX();
				clip.y -= component.getY();
			}
		}
		return clip;
	}

	/**
	 * Return the bounds, relative to the decorated component, of the
	 * decoration. The default covers the entire component. Note that this
	 * method will be called from the constructor, so be careful when overriding
	 * and referencing derived class state.
	 */
	protected Rectangle getDecorationBounds() {
		return bounds != DEFAULT_BOUNDS ? bounds : new Rectangle(0, 0, component.getWidth(),
				component.getHeight());
	}

	/**
	 * Change the bounds of the decoration, relative to the decorated component.
	 * The special value {@link #DEFAULT_BOUNDS} means the bounds will track the
	 * component bounds.
	 */
	public void setDecorationBounds(Rectangle bounds) {
		if (bounds == DEFAULT_BOUNDS) {
			this.bounds = bounds;
		} else {
			this.bounds = new Rectangle(bounds);
		}
		synch();
	}

	/**
	 * Change the bounds of the decoration, relative to the decorated component.
	 */
	public void setDecorationBounds(int x, int y, int w, int h) {
		setDecorationBounds(new Rectangle(x, y, w, h));
	}

	protected void setPainterBounds(int x, int y, int w, int h) {
		painter.setLocation(x, y);
		painter.setSize(w, h);
		repaint();
	}

	protected JComponent getComponent() {
		return component;
	}

	protected JComponent getPainter() {
		return painter;
	}

	/** Set the cursor to appear anywhere over the decoration bounds. */
	public void setCursor(Cursor cursor) {
		painter.setCursor(cursor);
	}

	/** Force a refresh of the underlying component and its decoration. */
	public void repaint() {
		component.repaint();
		painter.repaint();
	}

	/** Stop decorating. */
	public void dispose() {
		component.removeHierarchyListener(listener);
		component.removeComponentListener(listener);
		component.removePropertyChangeListener(listener);
		if (parent != null) {
			parent.removeComponentListener(listener);
			parent = null;
		}
		Container painterParent = painter.getParent();
		if (painterParent != null) {
			Rectangle bounds = painter.getBounds();
			painterParent.remove(painter);
			painterParent.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
		}
		component.repaint();
		component = null;
	}

	/**
	 * Define the decoration's appearance. The point (0,0) represents the upper
	 * left corner of the decorated component. The default clip mask will be the
	 * extents of the decoration bounds, as indicated by
	 * {@link #getDecorationBounds()}, which defaults to the decorated component
	 * bounds.
	 */
	public abstract void paint(Graphics g);

	public String toString() {
		return super.toString() + " on " + getComponent();
	}

	private static Field nComponents;
	static {
		try {
			nComponents = Container.class.getDeclaredField("ncomponents");
			nComponents.setAccessible(true);
		} catch (Exception e) {
			nComponents = null;
		}
	}

	private static boolean useSimpleBackground() {
		return nComponents == null;
	}

	/** Used to hook into the Swing painting architecture. */
	private class Painter extends JComponent {
		private int base;
		private Cursor cursor;

		public JComponent getComponent() {
			return AbstractComponentDecorator.this.getComponent();
		}

		public void setDecoratedLayer(int base) {
			this.base = base;
		}

		public int getDecoratedLayer() {
			return base;
		}

		public boolean isBackgroundDecoration() {
			return layerOffset < 0;
		}

		/** Set the cursor to something else. */
		public void setCursor(Cursor cursor) {
			Cursor oldCursor = getCursor();
			// Make sure the cursor actually changed, otherwise
			// we get cursor flicker (notably on w32 title bars)
			if (oldCursor == null && cursor != null || oldCursor != null
					&& !oldCursor.equals(cursor)) {
				this.cursor = cursor;
				super.setCursor(cursor);
			}
		}

		/**
		 * Returns the cursor of the decorated component, or the last cursor set
		 * by {@link #setCursor}.
		 */
		public Cursor getCursor() {
			return cursor != null ? cursor : component.getCursor();
		}

		/**
		 * Delegate to the containing decorator to perform the paint.
		 */
		public void paint(Graphics g) {
			if (!component.isShowing())
				return;
			Graphics g2 = g.create();
			g2.translate(-originOffset.x, -originOffset.y);
			AbstractComponentDecorator.this.paint(g2);
			g2.dispose();
			/*
			 * g = g.create(); g.setClip(null); g.setColor(Color.red);
			 * g.drawRect(0, 0, getWidth()-1, getHeight()-1);
			 */
		}

		/**
		 * Provide a decorator-specific tooltip, shown when within the
		 * decorator's bounds.
		 */
		public String getToolTipText(MouseEvent e) {
			return AbstractComponentDecorator.this.getToolTipText(e);
		}

		public String toString() {
			return "Painter for " + AbstractComponentDecorator.this;
		}
	}

	/**
	 * Provides a shared background painting mechanism for multiple decorations.
	 * This ensures that the background is only painted once if more than one
	 * background decorator is applied.
	 */
	private static class BackgroundPainter extends AbstractComponentDecorator {
		private static String key(int layer) {
			return "backgroundPainter for layer " + layer;
		}

		private String key;
		private int layer;

		public BackgroundPainter(JLayeredPane p, int layer) {
			super(p, 0, TOP);
			this.layer = layer;
			key = key(layer);
			p.putClientProperty(key, this);
		}

		private int hideChildren(Container c) {
			if (c == null)
				return 0;
			int value = c.getComponentCount();
			try {
				nComponents.set(c, new Integer(0));
			} catch (Exception e) {
				return c.getComponentCount();
			}
			return value;
		}

		private void restoreChildren(Container c, int count) {
			if (c != null) {
				try {
					nComponents.set(c, new Integer(count));
				} catch (Exception e) {
				}
			}
		}

		private void paintBackground(Graphics g, Component parent, JComponent jc) {
			int x = jc.getX();
			int y = jc.getY();
			int w = jc.getWidth();
			int h = jc.getHeight();
			paintBackground(g.create(x, y, w, h), jc);
		}

		private void paintBackground(Graphics g, JComponent jc) {
			if (jc.isOpaque()) {
				if (useSimpleBackground()) {
					g.setColor(jc.getBackground());
					g.fillRect(0, 0, jc.getWidth(), jc.getHeight());
				} else {
					int count = hideChildren(jc);
					boolean db = jc.isDoubleBuffered();
					if (db)
						jc.setDoubleBuffered(false);
					jc.paint(g);
					if (db)
						jc.setDoubleBuffered(true);
					restoreChildren(jc, count);
				}
			}
			Component[] kids = jc.getComponents();
			for (int i = 0; i < kids.length; i++) {
				if (kids[i] instanceof JComponent) {
					paintBackground(g, jc, (JComponent) kids[i]);
				}
			}
		}

		private List findOpaque(Component root) {
			List list = new ArrayList();
			if (root.isOpaque() && root instanceof JComponent) {
				list.add(root);
				((JComponent) root).setOpaque(false);
			}
			if (root instanceof Container) {
				Component[] kids = ((Container) root).getComponents();
				for (int i = 0; i < kids.length; i++) {
					list.addAll(findOpaque(kids[i]));
				}
			}
			return list;
		}

		private List findDoubleBuffered(Component root) {
			List list = new ArrayList();
			if (root.isDoubleBuffered() && root instanceof JComponent) {
				list.add(root);
				((JComponent) root).setDoubleBuffered(false);
			}
			if (root instanceof Container) {
				Component[] kids = ((Container) root).getComponents();
				for (int i = 0; i < kids.length; i++) {
					list.addAll(findDoubleBuffered(kids[i]));
				}
			}
			return list;
		}

		private void paintForeground(Graphics g, JComponent jc) {
			List opaque = findOpaque(jc);
			List db = findDoubleBuffered(jc);
			jc.paint(g);
			for (Iterator i = opaque.iterator(); i.hasNext();) {
				((JComponent) i.next()).setOpaque(true);
			}
			for (Iterator i = db.iterator(); i.hasNext();) {
				((JComponent) i.next()).setDoubleBuffered(true);
			}
		}

		/** Walk the list of "background" decorators and paint them. */
		public void paint(Graphics g) {

			JLayeredPane lp = (JLayeredPane) getComponent();
			Component[] kids = lp.getComponents();
			// Construct an area of the intersection of all decorators
			Area area = new Area();
			List painters = new ArrayList();
			List components = new ArrayList();
			for (int i = kids.length - 1; i >= 0; i--) {
				if (kids[i] instanceof Painter) {
					Painter p = (Painter) kids[i];
					if (p.isBackgroundDecoration() && p.getDecoratedLayer() == layer
							&& p.isShowing()) {
						painters.add(p);
						area.add(new Area(p.getBounds()));
					}
				} else if (lp.getLayer(kids[i]) == layer && kids[i] instanceof JComponent) {
					components.add(kids[i]);
				}
			}
			if (painters.size() == 0) {
				dispose();
				return;
			}
			g.setClip(area);

			// Paint background for that area
			for (Iterator i = components.iterator(); i.hasNext();) {
				JComponent c = (JComponent) i.next();
				paintBackground(g, lp, c);
			}

			// Paint the bg decorators
			for (Iterator i = painters.iterator(); i.hasNext();) {
				Painter p = (Painter) i.next();
				p.paint(g.create(p.getX(), p.getY(), p.getWidth(), p.getHeight()));
			}
			// Paint foreground for the area
			for (Iterator i = components.iterator(); i.hasNext();) {
				JComponent c = (JComponent) i.next();
				paintForeground(g.create(c.getX(), c.getY(), c.getWidth(), c.getHeight()), c);
			}
		}

		public void dispose() {
			getComponent().putClientProperty(key, null);
			super.dispose();
		}

		public String toString() {
			return key + " on " + getComponent();
		}
	}

	/** Tracks changes to component configuration. */
	private final class Listener extends ComponentAdapter implements HierarchyListener,
			PropertyChangeListener {
		public void hierarchyChanged(HierarchyEvent e) {
			if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
				attach();
			}
		}

		public void propertyChange(PropertyChangeEvent e) {
			if (JLayeredPane.LAYER_PROPERTY.equals(e.getPropertyName())) {
				attach();
			}
		}

		public void componentMoved(ComponentEvent e) {
			// FIXME figure out why attach works and synch doesn't
			// when painting a selection marquee over a decorated background
			attach();
		}

		public void componentResized(ComponentEvent e) {
			// FIXME figure out why attach works and synch doesn't
			// when painting a selection marquee over a decorated background
			attach();
		}

		public void componentHidden(ComponentEvent e) {
			setVisible(false);
		}

		public void componentShown(ComponentEvent e) {
			setVisible(true);
		}
	}
}
