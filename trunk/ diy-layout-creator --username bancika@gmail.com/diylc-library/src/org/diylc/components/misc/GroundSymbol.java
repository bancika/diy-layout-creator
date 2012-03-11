package org.diylc.components.misc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;

import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Ground Symbol", author = "Branislav Stojkovic", category = "Misc", instanceNamePrefix = "GND", description = "Ground schematic symbol", stretchable = false, zOrder = IDIYComponent.COMPONENT)
public class GroundSymbol extends AbstractComponent<Void> {

	private static final long serialVersionUID = 1L;

	public static Color COLOR = Color.black;
	public static Size SIZE = new Size(0.1d, SizeUnit.in);

	private Point point = new Point(0, 0);
	private Color color = COLOR;
	private Size size = SIZE;
	private GroundSymbolType type = GroundSymbolType.DEFAULT;

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
			Project project, IDrawingObserver drawingObserver) {
		int sizePx = (int) size.convertToPixels();
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.setColor(color);
		int x = point.x;
		int y = point.y;
		g2d.drawLine(x, y, x, y + sizePx / 6);
		if (type == GroundSymbolType.DEFAULT) {
			int delta = sizePx / 7;
			for (int i = 0; i < 5; i++) {
				g2d.drawLine(x - sizePx / 2 + delta * i, y + sizePx / 6 * (i + 1), x + sizePx / 2
						- delta * i, y + sizePx / 6 * (i + 1));
			}
		} else {
			Polygon poly = new Polygon(new int[] { x - sizePx / 2, x + sizePx / 2, x }, new int[] {
					y + sizePx / 6, y + sizePx / 6, y + sizePx }, 3);
			g2d.draw(poly);
		}
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int margin = 3 * width / 32;
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.setColor(COLOR);
		g2d.drawLine(width / 2, margin, width / 2, margin * 3 + height / 5);
		for (int i = 0; i < 5; i++) {
			g2d.drawLine(margin * (i + 1), margin * (3 + i) + height / 5, width - margin * (i + 1),
					margin * (3 + i) + height / 5);
		}
	}

	@EditableProperty(name = "Style")
	public GroundSymbolType getType() {
		return type;
	}

	public void setType(GroundSymbolType type) {
		this.type = type;
	}

	@Override
	public Point getControlPoint(int index) {
		return point;
	}

	@EditableProperty
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@EditableProperty
	public Size getSize() {
		return size;
	}

	public void setSize(Size size) {
		this.size = size;
	}

	@Override
	public int getControlPointCount() {
		return 1;
	}

	@Override
	public boolean isControlPointSticky(int index) {
		return true;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		this.point = point;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.WHEN_SELECTED;
	}

	@Deprecated
	@Override
	public Void getValue() {
		return null;
	}

	@Deprecated
	@Override
	public void setValue(Void value) {
	}

	public static enum GroundSymbolType {
		DEFAULT("Default"), TRIANGLE("Triangle");

		private String title;

		private GroundSymbolType(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}

		@Override
		public String toString() {
			return title;
		}
	}
}
