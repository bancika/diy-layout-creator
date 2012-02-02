package org.diylc.components.passive;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import org.diylc.components.AbstractSchematicLeadedSymbol;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Capacitor (schematic symbol)", author = "Branislav Stojkovic", category = "Passive", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "C", description = "Capacitor schematic symbol with an optional polarity sign", zOrder = IDIYComponent.COMPONENT)
public class CapacitorSymbol extends AbstractSchematicLeadedSymbol<Capacitance> {

	private static final long serialVersionUID = 1L;

	public static Size DEFAULT_LENGTH = new Size(0.05, SizeUnit.in);
	public static Size DEFAULT_WIDTH = new Size(0.15, SizeUnit.in);

	private Capacitance value = new Capacitance(22d, CapacitanceUnit.nF);
	private Voltage voltage = Voltage._63V;
	private boolean polarized = false;

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

	@EditableProperty
	public boolean getPolarized() {
		return polarized;
	}

	public void setPolarized(boolean polarized) {
		this.polarized = polarized;
	}

	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.rotate(-Math.PI / 4, width / 2, height / 2);
		g2d.setColor(LEAD_COLOR);
		g2d.drawLine(0, height / 2, 13, height / 2);
		g2d.drawLine(width - 13, height / 2, width, height / 2);
		g2d.setColor(COLOR);
		g2d.drawLine(14, height / 2 - 6, 14, height / 2 + 6);
		g2d.drawLine(width - 14, height / 2 - 6, width - 14, height / 2 + 6);
	}

	@Override
	protected Size getDefaultWidth() {
		return DEFAULT_WIDTH;
	}

	@Override
	protected Size getDefaultLength() {
		return DEFAULT_LENGTH;
	}

	@Override
	protected Shape getBodyShape() {
		GeneralPath polyline = new GeneralPath();
		double length = getLength().convertToPixels();
		double width = getWidth().convertToPixels();
		polyline.moveTo(0, 0);
		polyline.lineTo(0, width);
		polyline.moveTo(length, 0);
		polyline.lineTo(length, width);
		return polyline;
	}

	@Override
	protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
		if (polarized) {
			// Draw + sign.
			g2d.setColor(getBorderColor());
			int plusSize = getClosestOdd(getWidth().convertToPixels() / 4);
			int x = -plusSize;
			int y = plusSize;
			g2d.drawLine(x - plusSize / 2, y, x + plusSize / 2, y);
			g2d.drawLine(x, y - plusSize / 2, x, y + plusSize / 2);
		}
	}
}
