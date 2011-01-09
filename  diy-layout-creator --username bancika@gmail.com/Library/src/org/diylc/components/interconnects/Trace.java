package org.diylc.components.interconnects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

import org.diylc.components.AbstractLeadedDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Trace", author = "Branislav Stojkovic", category = "Interconnects", instanceNamePrefix = "J", desciption = "Jumper wire")
public class Trace extends AbstractLeadedDIYComponent<Void> {

	private static final long serialVersionUID = 1L;

	public static Size THICKNESS = new Size(1d, SizeUnit.mm);
	public static Color COLOR = Color.black;

	private Color leadColor = COLOR;
	private Size leadThickness = THICKNESS;

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {

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
	protected Color getBodyColor() {
		return null;
	}

	@Override
	protected Color getBorderColor() {
		return null;
	}

	@Override
	protected Shape getComponentShape() {
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
