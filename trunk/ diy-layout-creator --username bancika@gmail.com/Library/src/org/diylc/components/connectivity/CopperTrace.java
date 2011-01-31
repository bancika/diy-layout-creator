package org.diylc.components.connectivity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Copper Trace", author = "Branislav Stojkovic", category = "Connectivity", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "Trace", description = "Straight copper trace", zOrder = IDIYComponent.TRACE)
public class CopperTrace extends AbstractLeadedComponent<Void> {

	private static final long serialVersionUID = 1L;

	public static Size THICKNESS = new Size(1d, SizeUnit.mm);
	public static Color COLOR = Color.black;

	private Color leadColor = COLOR;
	private Size thickness = THICKNESS;

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3));
		g2d.setColor(COLOR);
		g2d.drawLine(1, height - 2, width - 2, 1);
	}

	@Override
	protected Color getLeadColor(ComponentState componentState) {
		return componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : leadColor;
	}
	
	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.WHEN_SELECTED;
	}

	@EditableProperty(name = "Color")
	public Color getLeadColor() {
		return leadColor;
	}

	public void setLeadColor(Color leadColor) {
		this.leadColor = leadColor;
	}

	@EditableProperty(name = "Width")
	public Size getThickness() {
		return thickness;
	}

	public void setThickness(Size thickness) {
		this.thickness = thickness;
	}
	
	@Override
	protected int getLeadThickness() {
		return getThickness().convertToPixels();
	}

	@Override
	protected boolean shouldShadeLeads() {
		return false;
	}

	public Color getBodyColor() {
		return super.getBodyColor();
	}

	@Override
	public Color getBorderColor() {
		return super.getBorderColor();
	}

	@Override
	public Byte getAlpha() {
		return super.getAlpha();
	}

	@Override
	public Size getLength() {
		return super.getLength();
	}

	@Override
	public Size getWidth() {
		return super.getWidth();
	}

	@Override
	public Void getValue() {
		return null;
	}

	@Override
	public void setValue(Void value) {
	}

	@Override
	protected Shape getBodyShape() {
		return null;
	}

	@Override
	protected Size getDefaultWidth() {
		return null;
	}

	@Override
	protected Size getDefaultLength() {
		return null;
	}
}
