package org.diylc.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.ComponentValue;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Ceramic Capacitor", author = "bancika", category = "Passive", instanceNamePrefix = "C", desciption = "test")
public class RadialCeramicDiskCapacitor extends AbstractLeadedDIYComponent {

	private static final long serialVersionUID = 1L;

	public static Size DEFAULT_WIDTH = new Size(1d / 2, SizeUnit.in);
	public static Size DEFAULT_HEIGHT = new Size(1d / 8, SizeUnit.in);
	public static Color BODY_COLOR = Color.decode("#F0E68C");
	public static Color BORDER_COLOR = BODY_COLOR.darker();

	private Capacitance value = new Capacitance(123d, CapacitanceUnit.nF);

	public RadialCeramicDiskCapacitor() {
		super();
	}

	@ComponentValue
	@EditableProperty
	public Capacitance getValue() {
		return value;
	}

	public void setValue(Capacitance value) {
		this.value = value;
	}

	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.drawString("C", 10, 10);
	}

	@Override
	protected Size getDefaultHeight() {
		return DEFAULT_HEIGHT;
	}

	@Override
	protected Size getDefaultWidth() {
		return DEFAULT_WIDTH;
	}

	@Override
	protected Color getBodyColor() {
		return BODY_COLOR;
	}

	@Override
	protected Color getBorderColor() {
		return BORDER_COLOR;
	}

	@Override
	protected Shape getComponentShape() {
		return new Ellipse2D.Double(0f, 0f, getWidth().convertToPixels(), getHeight()
				.convertToPixels());
	}
}
