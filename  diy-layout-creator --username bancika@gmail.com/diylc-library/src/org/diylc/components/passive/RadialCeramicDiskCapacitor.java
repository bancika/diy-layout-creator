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
import org.diylc.core.measures.VoltageUnit;

@ComponentDescriptor(name = "Ceramic Capacitor (radial)", author = "Branislav Stojkovic", category = "Passive", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "C", description = "Standard radial ceramic capacitor", zOrder = IDIYComponent.COMPONENT)
public class RadialCeramicDiskCapacitor extends AbstractLeadedComponent<Capacitance> {

	private static final long serialVersionUID = 1L;

	public static Size DEFAULT_WIDTH = new Size(1d / 2, SizeUnit.in);
	public static Size DEFAULT_HEIGHT = new Size(1d / 8, SizeUnit.in);
	public static Color BODY_COLOR = Color.decode("#F0E68C");
	public static Color BORDER_COLOR = BODY_COLOR.darker();

	private Capacitance value = new Capacitance(470d, CapacitanceUnit.pF);
	@Deprecated
	private Voltage voltage = Voltage._63V;
	private org.diylc.core.measures.Voltage voltageNew = new org.diylc.core.measures.Voltage(63d,
			VoltageUnit.V);

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
		// Backward comptibility
		if (voltageNew == null) {
			voltageNew = voltage.convertToNewFormat();
			voltage = null;
			// Clear old value, don't need it anymore
		}
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
