package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.ResistorColorCode;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.ResistanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Resistor", author = "Branislav Stojkovic", category = "Passive", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "R", description = "test", zOrder = IDIYComponent.COMPONENT)
public class Resistor extends AbstractLeadedComponent<Resistance> {

	private static final long serialVersionUID = 1L;

	public static Size DEFAULT_WIDTH = new Size(1d / 2, SizeUnit.in);
	public static Size DEFAULT_HEIGHT = new Size(1d / 8, SizeUnit.in);
	public static Color BODY_COLOR = Color.decode("#82CFFD");
	public static Color BORDER_COLOR = BODY_COLOR.darker();
	public static int BAND_SPACING = 5;
	public static int FIRST_BAND = 4;

	private Resistance value = new Resistance(123d, ResistanceUnit.K);
	private Power power = Power.HALF;
	private ResistorColorCode colorCode = ResistorColorCode._5_BAND;

	public Resistor() {
		super();
		this.bodyColor = BODY_COLOR;
		this.borderColor = BORDER_COLOR;
	}

	@Override
	protected boolean supportsStandingMode() {
		return true;
	}

	@EditableProperty(validatorClass = PositiveMeasureValidator.class)
	public Resistance getValue() {
		return value;
	}

	public void setValue(Resistance value) {
		this.value = value;
	}

	@EditableProperty
	public Power getPower() {
		return power;
	}

	public void setPower(Power power) {
		this.power = power;
	}

	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.rotate(-Math.PI / 4, width / 2, height / 2);
		g2d.setColor(LEAD_COLOR);
		g2d.drawLine(0, height / 2, width, height / 2);
		g2d.setColor(BODY_COLOR);
		g2d.fillRect(4, height / 2 - 3, width - 8, 6);
		g2d.setColor(Color.red);
		g2d.drawLine(7, height / 2 - 3, 7, height / 2 + 3);
		g2d.setColor(Color.orange);
		g2d.drawLine(11, height / 2 - 3, 11, height / 2 + 3);
		g2d.setColor(Color.black);
		g2d.drawLine(15, height / 2 - 3, 15, height / 2 + 3);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRect(4, height / 2 - 3, width - 8, 6);
	}

	@Override
	protected Size getDefaultWidth() {
		return DEFAULT_HEIGHT;
	}

	@Override
	protected Size getDefaultLength() {
		return DEFAULT_WIDTH;
	}

	@EditableProperty(name = "Color code")
	public ResistorColorCode getColorCode() {
		return colorCode;
	}

	public void setColorCode(ResistorColorCode colorCode) {
		this.colorCode = colorCode;
	}

	@Override
	protected Shape getBodyShape() {
		return new Rectangle2D.Double(0f, 0f, getLength().convertToPixels(),
				getClosestOdd(getWidth().convertToPixels()));
	}

	@Override
	protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
		// int width = getWidth().convertToPixels();
		if (colorCode == ResistorColorCode.NONE || outlineMode) {
			return;
		}
		int height = getClosestOdd(getWidth().convertToPixels());
		Color[] bands = value.getColorCode(colorCode);
		int x = FIRST_BAND;
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
		for (int i = 0; i < bands.length; i++) {
			g2d.setColor(bands[i]);
			g2d.drawLine(x, 1, x, height - 1);
			x += BAND_SPACING;
		}
	}

	@Override
	protected int getLabelOffset(int bodyWidth, int labelWidth) {
		Color[] bands = value.getColorCode(colorCode);
		int bandArea = FIRST_BAND + BAND_SPACING * (bands.length - 1);
		// Only offset the label if overlaping with the band area.
		if (labelWidth > bodyWidth - 2 * bandArea)
			return bandArea / 2;
		return 0;
	}
}
