package org.diylc.components.connectivity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;

import org.diylc.components.AbstractCurvedComponent;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Hookup Wire", author = "Branislav Stojkovic", category = "Connectivity", instanceNamePrefix = "W", description = "Flexible wire with two control points", zOrder = IDIYComponent.ABOVE_COMPONENT)
public class HookupWire extends AbstractCurvedComponent<AWG> {

	private static final long serialVersionUID = 1L;

	public static Color COLOR = Color.green;
	public static double INSULATION_THICKNESS_PCT = 0.3;

	protected AWG value = AWG._22;

	@Override
	protected Color getDefaultColor() {
		return COLOR;
	}

	@Override
	protected void drawCurve(CubicCurve2D curve, Graphics2D g2d) {
		int thickness = (int) (Math.pow(Math.E, -1.12436 - 0.11594 * value.getValue())
				* Constants.GRID * Constants.GRIDS_PER_INCH * (1 + 2 * INSULATION_THICKNESS_PCT));
		g2d.setColor(color.darker());
		g2d.setStroke(new BasicStroke(thickness));
		g2d.draw(curve);
		g2d.setColor(color);
		g2d.setStroke(new BasicStroke(thickness - 2));
		g2d.draw(curve);
	}

	@EditableProperty(name = "AWG")
	@Override
	public AWG getValue() {
		return value;
	}

	@Override
	public void setValue(AWG value) {
		this.value = value;
	}
}
