package org.diylc.components.connectivity;

import java.awt.Color;
import java.awt.Graphics2D;

import org.diylc.components.AbstractSinglePointComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Solder Pad", category = "Connectivity", author = "Branislav Stojkovic", description = "test", instanceNamePrefix = "", stretchable = false, zOrder = IDIYComponent.ABOVE_BOARD + 0.1)
public class SolderPad extends AbstractSinglePointComponent {

	private static final long serialVersionUID = 1L;

	public static Size SIZE = new Size(0.09d, SizeUnit.in);
	public static Size HOLE_SIZE = new Size(0.8d, SizeUnit.mm);
	public static Color COLOR = Color.black;

	private Size size = SIZE;
	private Color color = COLOR;

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project) {
		int diameter = size.convertToPixels();
		int holeDiameter = HOLE_SIZE.convertToPixels();
		g2d.setColor(color);
		g2d.fillOval(point.x - diameter / 2, point.y - diameter / 2, diameter, diameter);
		g2d.setColor(Constants.CANVAS_COLOR);
		g2d.fillOval(point.x - holeDiameter / 2, point.y - holeDiameter / 2, holeDiameter,
				holeDiameter);
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int diameter = width / 2;
		int holeDiameter = 4;
		g2d.setColor(COLOR);
		g2d.fillOval((width - diameter) / 2, (height - diameter) / 2, diameter, diameter);
		g2d.setColor(Constants.CANVAS_COLOR);
		g2d.fillOval((width - holeDiameter) / 2, (height - holeDiameter) / 2, holeDiameter,
				holeDiameter);
	}

	@EditableProperty
	public Size getSize() {
		return size;
	}

	public void setSize(Size size) {
		this.size = size;
	}

	@EditableProperty
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
