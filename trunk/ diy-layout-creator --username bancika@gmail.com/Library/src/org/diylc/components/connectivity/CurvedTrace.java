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
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Curved Trace", author = "Branislav Stojkovic", category = "Connectivity", instanceNamePrefix = "Trace", description = "Curved copper trace with two control points", zOrder = IDIYComponent.ABOVE_BOARD)
public class CurvedTrace extends AbstractCurvedComponent<Void> {

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
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(thickness));
		g2d.setColor(componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : color);
		g2d.draw(curve);
	}
	
	@EditableProperty(name = "Width")
	public Size getSize() {
		return size;
	}

	public void setSize(Size size) {
		this.size = size;
	}

	@Deprecated
	@Override
	public Void getValue() {
		return null;
	}
	
	@Override
	public void setValue(Void value) {
	}

	@Deprecated
	@Override
	public Byte getAlpha() {
		return super.getAlpha();
	}
	
	@Override
	public void setAlpha(Byte alpha) {
		super.setAlpha(alpha);
	}
}
