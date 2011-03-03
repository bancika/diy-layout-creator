package org.diylc.components.passive;

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
import org.diylc.core.Theme;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Electrolytic Capacitor", author = "Branislav Stojkovic", category = "Passive", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "C", description = "Vertical mounted electrolytic capacitor, polarized or bipolar", zOrder = IDIYComponent.COMPONENT)
public class RadialElectrolytic extends AbstractLeadedComponent<Capacitance> {

	private static final long serialVersionUID = 1L;

	public static Size DEFAULT_SIZE = new Size(1d / 2, SizeUnit.in);
	public static Color BODY_COLOR = Color.decode("#EAADEA");
	public static Color BORDER_COLOR = BODY_COLOR.darker();
	public static Color MARKER_COLOR = Color.gray;
	public static Color TICK_COLOR = Color.white;

	private Capacitance value = new Capacitance(1d, CapacitanceUnit.uF);
	private Voltage voltage = Voltage._63V;

	private Color markerColor = MARKER_COLOR;
	private Color tickColor = TICK_COLOR;
	private boolean polarized = true;

	public RadialElectrolytic() {
		super();
		this.bodyColor = BODY_COLOR;
		this.borderColor = BORDER_COLOR;
	}

	@EditableProperty
	public Capacitance getValue() {
		return value;
	}

	public void setValue(Capacitance value) {
		this.value = value;
	}

	@EditableProperty
	public Voltage getVoltage() {
		return voltage;
	}

	public void setVoltage(Voltage voltage) {
		this.voltage = voltage;
	}

	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setColor(BODY_COLOR);
		int margin = 3;
		Ellipse2D body = new Ellipse2D.Double(margin, margin, getClosestOdd(width - 2 * margin),
				getClosestOdd(width - 2 * margin));
		g2d.fill(body);
		Area marker = new Area(body);
		marker.subtract(new Area(new Rectangle2D.Double(margin, margin, width - 4 * margin,
				getClosestOdd(width - 2 * margin))));
		g2d.setColor(MARKER_COLOR);
		g2d.fill(marker);
		g2d.setColor(TICK_COLOR);
		g2d.drawLine(width - 2 * margin, height / 2 - 2, width - 2 * margin, height / 2 + 2);
		g2d.setColor(BORDER_COLOR);
		g2d.draw(body);
		// g2d.setColor(COVER_COLOR.darker());
		// g2d.drawLine(innerMargin + 2, innerMargin + 2, width - innerMargin -
		// 2, width - innerMargin
		// - 2);
		// g2d.drawLine(innerMargin + 2, width - innerMargin - 2, width -
		// innerMargin - 2,
		// innerMargin + 2);
	}

	@Override
	protected void decorateComponentBody(Graphics2D g2d) {
		if (polarized) {
			int totalDiameter = getClosestOdd(getLength().convertToPixels());
			Area area = new Area(getBodyShape());
			area
					.subtract(new Area(new Rectangle2D.Double(0, 0, totalDiameter * 0.8,
							totalDiameter)));
			g2d.setColor(markerColor);
			g2d.fill(area);
			g2d.setColor(tickColor);
			g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
			g2d.drawLine((int) (totalDiameter * 0.9), totalDiameter / 2
					- (int) (totalDiameter * 0.05), (int) (totalDiameter * 0.9), totalDiameter / 2
					+ (int) (totalDiameter * 0.05));
		}
		// int coverDiameter = getClosestOdd(totalDiameter * 3 / 4);
		// g2d.setColor(coverColor);
		// int position = (totalDiameter - coverDiameter) / 2;
		// g2d.fillOval(position, position, coverDiameter, coverDiameter);
		// g2d.setColor(coverColor.darker());
		// g2d.drawLine(position + coverDiameter / 5, position + coverDiameter /
		// 5, position + 4
		// * coverDiameter / 5, position + 4 * coverDiameter / 5);
		// g2d.drawLine(position + coverDiameter / 5, position + 4 *
		// coverDiameter / 5, position + 4
		// * coverDiameter / 5, position + coverDiameter / 5);
	}

	@Override
	protected Size getDefaultWidth() {
		return null;
	}

	@Override
	public Size getWidth() {
		return super.getWidth();
	}

	@Override
	protected Size getDefaultLength() {
		// We'll reuse width property to set the diameter.
		return DEFAULT_SIZE;
	}

	@EditableProperty(name = "Diameter")
	@Override
	public Size getLength() {
		return super.getLength();
	}

	@EditableProperty(name = "Marker")
	public Color getMarkerColor() {
		return markerColor;
	}

	public void setMarkerColor(Color coverColor) {
		this.markerColor = coverColor;
	}

	@EditableProperty(name = "Tick")
	public Color getTickColor() {
		return tickColor;
	}

	public void setTickColor(Color tickColor) {
		this.tickColor = tickColor;
	}

	@EditableProperty(name = "Polarized")
	public boolean getPolarized() {
		return polarized;
	}

	public void setPolarized(boolean polarized) {
		this.polarized = polarized;
	}

	@Override
	protected Shape getBodyShape() {
		return new Ellipse2D.Double(0f, 0f, getClosestOdd(getLength().convertToPixels()),
				getClosestOdd(getLength().convertToPixels()));
	}
}
