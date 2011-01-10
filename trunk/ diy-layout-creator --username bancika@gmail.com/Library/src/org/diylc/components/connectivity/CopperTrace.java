package org.diylc.components.connectivity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Copper Trace", author = "Branislav Stojkovic", category = "Connectivity", instanceNamePrefix = "CT", description = "", zOrder = IDIYComponent.ABOVE_BOARD)
public class CopperTrace extends AbstractLeadedComponent<Void> {

	private static final long serialVersionUID = 1L;

	public static Size THICKNESS = new Size(1d, SizeUnit.mm);
	public static Color COLOR = Color.black;

	private Color leadColor = COLOR;
	private Size leadThickness = THICKNESS;

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setStroke(new BasicStroke(3));
		g2d.setColor(COLOR);
		g2d.drawLine(1, height - 2, width - 2, 1);
	}

	@Override
	@EditableProperty(name = "Color")
	public Color getLeadColor() {
		return leadColor;
	}

	public void setLeadColor(Color leadColor) {
		this.leadColor = leadColor;
	}

	@Override
	@EditableProperty(name = "Thickness")
	public Size getLeadThickness() {
		return leadThickness;
	}

	public void setLeadThickness(Size leadThickness) {
		this.leadThickness = leadThickness;
	}

	@Deprecated
	public Color getBodyColor() {
		return super.getBodyColor();
	}

	@Deprecated
	@Override
	public Color getBorderColor() {
		// TODO Auto-generated method stub
		return super.getBorderColor();
	}

	@Deprecated
	@Override
	public Byte getAlpha() {
		return super.getAlpha();
	}

	@Deprecated
	@Override
	public Size getWidth() {
		return super.getWidth();
	}

	@Deprecated
	@Override
	public Size getHeight() {
		return super.getHeight();
	}

	@Deprecated
	@Override
	public Void getValue() {
		return null;
	}

	@Deprecated
	@Override
	public void setValue(Void value) {
	}

	@Override
	protected Color getDefaultBodyColor() {
		return null;
	}

	@Override
	protected Color getDefaultBorderColor() {
		return null;
	}

	@Override
	protected Shape getBodyShape() {
		return null;
	}

	@Override
	protected Size getDefaultHeight() {
		return null;
	}

	@Override
	protected Size getDefaultWidth() {
		return null;
	}
}
