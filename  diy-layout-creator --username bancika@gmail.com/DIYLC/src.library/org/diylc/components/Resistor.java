package org.diylc.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import org.diylc.core.ComponentState;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.ResistanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Resistor", author = "bancika", category = "Passive", instanceNamePrefix = "R", desciption = "test")
public class Resistor extends AbstractLeadedDIYComponent {

	private static final long serialVersionUID = 1L;

	private Resistance r = new Resistance(123d, ResistanceUnit.K);
	private Color color = Color.green;

	public Resistor() {
		super();
	}

	@EditableProperty
	public Resistance getR() {
		return r;
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState) {
		g2d.setColor(componentState.equals(ComponentState.SELECTED) ? color : color.darker());
		g2d.setStroke(new BasicStroke(1));
		if (componentState == ComponentState.DRAGGING) {
			g2d.setStroke(new BasicStroke(1));
		}
		g2d.drawLine(point1.x, point1.y, point2.x, point2.y);
		g2d.rotate(Math.PI / 4, (point1.x + point2.x) / 2, (point1.y + point2.y) / 2);
		g2d.fillRect((point1.x + point2.x) / 2 - 10, (point1.y + point2.y) / 2 - 10, 20, 20);
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.drawString("R", 10, 10);
	}

	@Override
	protected Size getDefaultHeight() {
		return new Size(3d, SizeUnit.mm);
	}

	@Override
	protected Size getDefaultWidth() {
		return new Size(10d, SizeUnit.mm);
	}
}
