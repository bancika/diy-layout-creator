package org.diylc.components.connectivity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;

import org.diylc.components.AbstractCurvedComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Curved Trace", author = "Branislav Stojkovic", category = "Connectivity", instanceNamePrefix = "Trace", description = "Curved copper trace with two control points", zOrder = IDIYComponent.ABOVE_BOARD)
public class CurvedTrace extends AbstractCurvedComponent<Size> {

	private static final long serialVersionUID = 1L;

	public static Color COLOR = Color.black;
	public static Size SIZE = new Size(1d, SizeUnit.mm);

	protected Size size = SIZE;

	@Override
	protected Color getDefaultColor() {
		return COLOR;
	}

	@Override
	protected void drawCurve(CubicCurve2D curve, Graphics2D g2d, ComponentState componentState) {
		int thickness = size.convertToPixels();
		g2d.setStroke(new BasicStroke(thickness));
		g2d.setColor(componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : color);
		g2d.draw(curve);
	}

	@EditableProperty(name = "Width")
	@Override
	public Size getValue() {
		return size;
	}

	@Override
	public void setValue(Size value) {
		this.size = value;
	}
}
