package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Electrolytic Capacitor (axial)", author = "Branislav Stojkovic", category = "Passive", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "C", description = "Axial electrolytic capacitor, similar to Sprague Atom, F&T, etc", zOrder = IDIYComponent.COMPONENT)
public class AxialElectrolyticCapacitor extends AbstractLeadedComponent<Capacitance> {

	private static final long serialVersionUID = 1L;

	public static Size DEFAULT_WIDTH = new Size(1d / 2, SizeUnit.in);
	public static Size DEFAULT_HEIGHT = new Size(1d / 8, SizeUnit.in);
	public static Color BODY_COLOR = Color.decode("#EAADEA");
	public static Color BORDER_COLOR = BODY_COLOR.darker();
	public static Color MARKER_COLOR = Color.gray;
	public static Color TICK_COLOR = Color.white;

	private Capacitance value = new Capacitance(1d, CapacitanceUnit.uF);
	private Voltage voltage = Voltage._63V;

	private Color markerColor = MARKER_COLOR;
	private Color tickColor = TICK_COLOR;
	private boolean polarized = true;

	public AxialElectrolyticCapacitor() {
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
		g2d.rotate(-Math.PI / 4, width / 2, height / 2);
		g2d.setColor(LEAD_COLOR);
		g2d.drawLine(0, height / 2, width, height / 2);
		g2d.setColor(BODY_COLOR);
		g2d.fillRect(4, height / 2 - 3, width - 8, 6);
		g2d.setColor(MARKER_COLOR);
		g2d.fillRect(width - 9, height / 2 - 3, 5, 6);
		g2d.setColor(TICK_COLOR);
		g2d.drawLine(width - 6, height / 2 - 1, width - 6, height / 2 + 1);
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
		return new Rectangle2D.Double(0f, 0f, getLength().convertToPixels(),
				getClosestOdd(getWidth().convertToPixels()));
	}

	@Override
	protected void decorateComponentBody(Graphics2D g2d) {
		if (polarized) {
			int markerLength = (int) (getLength().convertToPixels() * 0.2);
			g2d.setColor(markerColor);
			int width = getClosestOdd(getWidth().convertToPixels());
			g2d.fillRect(getLength().convertToPixels() - markerLength, 0, markerLength, width);
			g2d.setColor(tickColor);
			g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
			g2d.drawLine(getLength().convertToPixels() - markerLength / 2,
					(int) (width / 2 - width * 0.15), getLength().convertToPixels() - markerLength
							/ 2, (int) (width / 2 + width * 0.15));
		}
	}
}
