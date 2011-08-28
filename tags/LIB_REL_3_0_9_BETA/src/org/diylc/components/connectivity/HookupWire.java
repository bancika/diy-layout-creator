package org.diylc.components.connectivity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;

import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractCurvedComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Hookup Wire", author = "Branislav Stojkovic", category = "Connectivity", instanceNamePrefix = "W", description = "Flexible wire with two control points", zOrder = IDIYComponent.COMPONENT)
public class HookupWire extends AbstractCurvedComponent<Void> {

	private static final long serialVersionUID = 1L;

	public static Color COLOR = Color.green;
	public static double INSULATION_THICKNESS_PCT = 0.3;

	protected AWG gauge = AWG._22;

	@Override
	protected Color getDefaultColor() {
		return COLOR;
	}

	@Override
	protected void drawCurve(CubicCurve2D curve, Graphics2D g2d, ComponentState componentState) {
		int thickness = (int) (Math.pow(Math.E, -1.12436 - 0.11594 * gauge.getValue())
				* Constants.PIXELS_PER_INCH * (1 + 2 * INSULATION_THICKNESS_PCT));
		Color curveColor = componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : color.darker();
		g2d.setColor(curveColor);
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(thickness));
		g2d.draw(curve);
		if (componentState == ComponentState.NORMAL) {
			g2d.setColor(color);
			g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(thickness - 2));
			g2d.draw(curve);
		}
	}

	@EditableProperty(name = "AWG")
	public AWG getGauge() {
		return gauge;
	}

	public void setGauge(AWG gauge) {
		this.gauge = gauge;
	}

	@Override
	public Void getValue() {
		return null;
	}

	@Override
	public void setValue(Void value) {
	}
}
