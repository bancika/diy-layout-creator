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

@ComponentDescriptor(name = "Ceramic Capacitor", author = "Branislav Stojkovic", category = "Passive", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "C", description = "test", zOrder = IDIYComponent.COMPONENT)
public class RadialCeramicDiskCapacitor extends AbstractLeadedComponent<Capacitance> {

	private static final long serialVersionUID = 1L;

	public static Size DEFAULT_WIDTH = new Size(1d / 2, SizeUnit.in);
	public static Size DEFAULT_HEIGHT = new Size(1d / 8, SizeUnit.in);
	public static Color BODY_COLOR = Color.decode("#F0E68C");
	public static Color BORDER_COLOR = BODY_COLOR.darker();

	private Capacitance value = new Capacitance(123d, CapacitanceUnit.nF);

	public RadialCeramicDiskCapacitor() {
		super();
	}

	@EditableProperty
	public Capacitance getValue() {
		return value;
	}

	public void setValue(Capacitance value) {
		this.value = value;
	}

	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.rotate(-Math.PI / 4, width / 2, height / 2);
		g2d.setColor(LEAD_COLOR);
		g2d.drawLine(0, height / 2, width, height / 2);
		g2d.setColor(BODY_COLOR);
		g2d.fillOval(4, height / 2 - 3, width - 8, 6);
		g2d.setColor(BORDER_COLOR);
		g2d.drawOval(4, height / 2 - 3, width - 8, 6);
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
	protected Color getDefaultBodyColor() {
		return BODY_COLOR;
	}

	@Override
	protected Color getDefaultBorderColor() {
		return BORDER_COLOR;
	}

	@Override
	protected Shape getBodyShape() {
		return new Ellipse2D.Double(0f, 0f, getWidth().convertToPixels(), getHeight()
				.convertToPixels());
	}
}
