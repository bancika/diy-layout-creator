package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.diylc.components.AbstractRadialComponent;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Ceramic Capacitor (radial)", author = "Branislav Stojkovic", category = "Passive", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "C", description = "Standard radial ceramic capacitor", zOrder = IDIYComponent.COMPONENT)
public class RadialCeramicDiskCapacitor extends AbstractRadialComponent<Capacitance> {

	private static final long serialVersionUID = 1L;

	public static Size DEFAULT_WIDTH = new Size(1d / 4, SizeUnit.in);
	public static Size DEFAULT_HEIGHT = new Size(1d / 8, SizeUnit.in);
	public static Color BODY_COLOR = Color.decode("#F0E68C");
	public static Color BORDER_COLOR = BODY_COLOR.darker();

	private Capacitance value = null;
	@Deprecated
	private Voltage voltage = Voltage._63V;
	private org.diylc.core.measures.Voltage voltageNew = null;

	public RadialCeramicDiskCapacitor() {
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
	
	@Override
	public String getValueForDisplay() {
		return getValue().toString() + " " + getVoltageNew().toString();
	}
	
	@EditableProperty(name = "Voltage")
	public org.diylc.core.measures.Voltage getVoltageNew() {
		return voltageNew;
	}

	public void setVoltageNew(org.diylc.core.measures.Voltage voltageNew) {
		this.voltageNew = voltageNew;
	}
	
	@EditableProperty
	public Voltage getVoltage() {
		return voltage;
	}
	
	public void setVoltage(Voltage voltage) {
		this.voltage = voltage;
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
	protected Size getDefaultWidth() {
		return DEFAULT_HEIGHT;
	}

	@Override
	protected Size getDefaultLength() {
		return DEFAULT_WIDTH;
	}

	@Override
	protected Shape getBodyShape() {
		return new Ellipse2D.Double(0f, 0f, getLength().convertToPixels(), getClosestOdd(getWidth()
				.convertToPixels()));
	}
}
