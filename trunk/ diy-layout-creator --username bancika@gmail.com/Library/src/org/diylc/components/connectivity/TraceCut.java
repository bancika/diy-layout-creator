package org.diylc.components.connectivity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Trace Cut", category = "Connectivity", author = "Branislav Stojkovic", description = "test", instanceNamePrefix = "Cut", stretchable = false, zOrder = IDIYComponent.ABOVE_BOARD)
public class TraceCut extends AbstractComponent<Void> {

	private static final long serialVersionUID = 1L;

	public static Size SIZE = new Size(0.07d, SizeUnit.in);
	public static Color FILL_COLOR = Color.white;
	public static Color BORDER_COLOR = Color.red;
	public static Color SELECTION_COLOR = Color.blue;

	private Size size = SIZE;
	private Color fillColor = FILL_COLOR;
	private Color borderColor = BORDER_COLOR;
	
	protected Point point = new Point(0, 0);

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project,
			IDrawingObserver drawingObserver) {
		int size = getClosestOdd(this.size.convertToPixels());
		int dotDiameter = size - 6;
		g2d.setColor(fillColor);
		g2d.fillRect(point.x - size / 2, point.y - size / 2, size, size);
		g2d.setColor(componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : borderColor);
		g2d.setStroke(Constants.BASIC_STROKE);
		g2d.drawRect(point.x - size / 2, point.y - size / 2, size, size);
		g2d
				.fillOval(point.x - dotDiameter / 2, point.y - dotDiameter / 2, dotDiameter,
						dotDiameter);
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int size = 16;
		int dotDiameter = size / 4 * 2;
		g2d.setColor(FILL_COLOR);
		g2d.fillRect((width - size) / 2, (height - size) / 2, size, size);
		g2d.setColor(BORDER_COLOR);
		g2d.setStroke(Constants.BASIC_STROKE);
		g2d.drawRect((width - size) / 2, (height - size) / 2, size, size);
		g2d.fillOval((width - dotDiameter) / 2, (height - dotDiameter) / 2, dotDiameter,
				dotDiameter);
	}

	@Override
	public int getControlPointCount() {
		return 1;
	}

	@Override
	public Point getControlPoint(int index) {
		return point;
	}
	
	@Override
	public boolean isControlPointSticky(int index) {
		return false;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.NEVER;
	}


	@Override
	public void setControlPoint(Point point, int index) {
		this.point.setLocation(point);
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
	
	@EditableProperty
	public Size getSize() {
		return size;
	}

	public void setSize(Size size) {
		this.size = size;
	}

	@EditableProperty(name = "Fill")
	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	@EditableProperty(name = "Border")
	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	@Deprecated
	@Override
	public String getName() {
		return super.getName();
	}
}
