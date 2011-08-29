package org.diylc.components.semiconductors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "LED", author = "Branislav Stojkovic", category = "Semiconductors", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "D", description = "Light Emitting Diode", zOrder = IDIYComponent.COMPONENT)
public class LED extends AbstractLeadedComponent<String> {

	private static final long serialVersionUID = 1L;

	public static Size DEFAULT_SIZE = new Size(5d, SizeUnit.mm);
	public static Color BODY_COLOR = Color.decode("#5DFC0A");
	public static Color BORDER_COLOR = BODY_COLOR.darker();

	private String value = "";

	public LED() {
		super();
		this.bodyColor = BODY_COLOR;
		this.borderColor = BORDER_COLOR;
	}

	@EditableProperty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.rotate(-Math.PI / 4, width / 2, height / 2);
		g2d.setColor(LEAD_COLOR);
		g2d.drawLine(0, height / 2, width, height / 2);

		int margin = 4 * width / 32;
		Area area = new Area(new Ellipse2D.Double(margin, margin, width - 2 * margin, width - 2
				* margin));
		area.intersect(new Area(new Rectangle2D.Double(margin, margin, width - 5 * margin / 2,
				width - 2 * margin)));
		g2d.setColor(BODY_COLOR);
		g2d.fill(area);
		g2d.setColor(BORDER_COLOR);
		g2d.draw(area);
		g2d
				.drawOval(margin * 2 - 1, margin * 2 - 1, width - 4 * margin + 2, width - 4
						* margin + 2);
	}

	@Override
	protected Size getDefaultWidth() {
		return DEFAULT_SIZE;
	}

	@Override
	protected Size getDefaultLength() {
		return DEFAULT_SIZE;
	}

	@Override
	protected Shape getBodyShape() {
		int size = getClosestOdd((int) (getLength().convertToPixels() * 1.2));
		Area area = new Area(new Ellipse2D.Double(0, 0, size, size));
		area.intersect(new Area(new Rectangle2D.Double(0, 0, getLength().convertToPixels() * 1.15,
				size)));
		return area;
	}

	@Override
	protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
		if (!outlineMode) {
			int size = getClosestOdd((int) (getLength().convertToPixels() * 1.2));
			int innerSize = getClosestOdd(getLength().convertToPixels());
			int x = (size - innerSize) / 2;
			Shape s = new Ellipse2D.Double(x, x, innerSize, innerSize);
			g2d.setColor(getBorderColor());
			g2d.draw(s);
		}
	}

	@EditableProperty(name = "Size")
	@Override
	public Size getLength() {
		return super.getLength();
	}

	@Override
	public Size getWidth() {
		return super.getWidth();
	}
}
