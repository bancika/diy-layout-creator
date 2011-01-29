package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Electrolytic Capacitor", author = "Branislav Stojkovic", category = "Passive", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "C", description = "Vertical mounted electrolytic capacitor, polarized or bipolar", zOrder = IDIYComponent.COMPONENT)
public class ElectrolyticRadial extends AbstractLeadedComponent<Capacitance> {

	private static final long serialVersionUID = 1L;

	public static Size DEFAULT_SIZE = new Size(1d / 2, SizeUnit.in);
	public static Color BODY_COLOR = Color.decode("#27408B");
	public static Color BORDER_COLOR = BODY_COLOR.darker();
	public static Color COVER_COLOR = Color.decode("#C3E4ED");

	private Capacitance value = new Capacitance(1d, CapacitanceUnit.uF);

	private Color coverColor = COVER_COLOR;

	public ElectrolyticRadial() {
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

	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setColor(BODY_COLOR);
		g2d.fillOval(6, 6, width - 12, width - 12);
		g2d.setColor(BORDER_COLOR);
		g2d.drawOval(6, 6, width - 12, width - 12);
	}

	@Override
	protected void decorateComponentBody(Graphics2D g2d) {
		int totalDiameter = getClosestOdd(getLength().convertToPixels());
		int coverDiameter = getClosestOdd(totalDiameter * 3 / 4);
		g2d.setColor(coverColor);
		int position = (totalDiameter - coverDiameter) / 2;
		g2d.fillOval(position, position, coverDiameter, coverDiameter);
		g2d.setColor(coverColor.darker());
		g2d.drawLine(position + coverDiameter / 5, position + coverDiameter / 5, position + 4
				* coverDiameter / 5, position + 4 * coverDiameter / 5);
		g2d.drawLine(position + coverDiameter / 5, position + 4 * coverDiameter / 5, position + 4
				* coverDiameter / 5, position + coverDiameter / 5);
	}

	@Override
	protected Size getDefaultWidth() {
		return null;
	}

	@Deprecated
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

	@EditableProperty(name = "Cover")
	public Color getCoverColor() {
		return coverColor;
	}

	public void setCoverColor(Color coverColor) {
		this.coverColor = coverColor;
	}

	@Override
	protected Shape getBodyShape() {
		return new Ellipse2D.Double(0f, 0f, getClosestOdd(getLength().convertToPixels()),
				getClosestOdd(getLength().convertToPixels()));
	}
}
