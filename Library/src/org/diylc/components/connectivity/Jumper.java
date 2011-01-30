package org.diylc.components.connectivity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;

@ComponentDescriptor(name = "Jumper", author = "Branislav Stojkovic", category = "Connectivity", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "J", description = "", zOrder = IDIYComponent.COMPONENT)
public class Jumper extends AbstractLeadedComponent<Void> {

	private static final long serialVersionUID = 1L;

	public static Color COLOR = Color.blue;

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.setColor(COLOR);
		g2d.drawLine(1, height - 2, width - 2, 1);
	}

	@Override
	@EditableProperty(name = "Color")
	public Color getLeadColor(ComponentState componentState) {
		return componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : COLOR;
	}

	@Deprecated
	public Color getBodyColor() {
		return super.getBodyColor();
	}

	@Deprecated
	@Override
	public Color getBorderColor() {
		return super.getBorderColor();
	}

	@Deprecated
	@Override
	public Byte getAlpha() {
		return super.getAlpha();
	}

	@Deprecated
	@Override
	public Size getLength() {
		return super.getLength();
	}

	@Deprecated
	@Override
	public Size getWidth() {
		return super.getWidth();
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
