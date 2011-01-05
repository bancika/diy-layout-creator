package org.diylc.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ControlPoint;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;

/**
 * Base class for all leaded components such as resistors or capacitors. Has two
 * control points and draws leads between them. Also, it positions and draws the
 * shape of the component as specified by a child class.
 * 
 * @author Branislav Stojkovic
 */
public abstract class AbstractLeadedDIYComponent<T> implements IDIYComponent<T> {

	private static final long serialVersionUID = 1L;

	public static Color LEAD_COLOR = Color.black;
	public static Color LABEL_COLOR = Color.black;
	public static Color LABEL_COLOR_SELECTED = Color.red;

	protected Size width;
	protected Size height;
	protected Point point1 = new Point(0, 0);
	protected Point point2 = new Point((int) (Constants.GRID * 10), 0);
	protected String name = "New Component";

	protected AbstractLeadedDIYComponent() {
		super();
		try {
			this.width = getDefaultWidth().clone();
			this.height = getDefaultHeight().clone();
		} catch (CloneNotSupportedException e) {
			// This should never happen because Size supports cloning.
		}
	}

	/**
	 * @return default component width.
	 */
	protected abstract Size getDefaultWidth();

	/**
	 * Returns default component height.
	 * 
	 * @return
	 */
	protected abstract Size getDefaultHeight();

	/**
	 * @return shape that represents component body. Shape should not be
	 *         transformed and should be referenced to (0, 0).
	 */
	protected abstract Shape getComponentShape();

	/**
	 * @return component body color.
	 */
	protected abstract Color getBodyColor();

	/**
	 * @return component border color.
	 */
	protected abstract Color getBorderColor();

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project) {
		g2d.setColor(LEAD_COLOR);
		g2d.setStroke(new BasicStroke(1));
		double distance = point1.distance(point2);
		Shape shape = getComponentShape();
		Rectangle shapeRect = shape.getBounds();
		double leadLenght = (distance - shapeRect.width) / 2;
		Double theta;
		theta = Math.atan2(point2.y - point1.y, point2.x - point1.x);
		// Draw leads.
		g2d.drawLine(point1.x, point1.y, (int) (point1.x + leadLenght * Math.cos(theta)),
				(int) (point1.y + leadLenght * Math.sin(theta)));
		g2d.drawLine(point2.x, point2.y, (int) (point2.x - leadLenght * Math.cos(theta)),
				(int) (point2.y - leadLenght * Math.sin(theta)));
		// Transform graphics to draw the body in the right place and at the
		// right angle.
		g2d.translate((point1.x + point2.x - shapeRect.width) / 2,
				(point1.y + point2.y - shapeRect.height) / 2);
		g2d.rotate(theta, shapeRect.width / 2, shapeRect.height / 2);
		// Draw body.
		if (componentState != ComponentState.DRAGGING) {
			g2d.setColor(getBodyColor());
			g2d.fill(shape);
		}
		g2d.setColor(getBorderColor());
		g2d.draw(shape);
		// Draw label.
		g2d.setFont(Constants.LABEL_FONT);
		g2d
				.setColor(componentState == ComponentState.SELECTED ? LABEL_COLOR_SELECTED
						: LABEL_COLOR);
		FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
		java.awt.geom.Rectangle2D textRect = fontMetrics.getStringBounds(getName(), g2d);
		g2d.drawString(getName(), (int) (shapeRect.width - textRect.getWidth()) / 2,
				(int) (shapeRect.height - textRect.getHeight()) / 2 + fontMetrics.getAscent());
	}

	@EditableProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ControlPoint(visibilityPolicy = VisibilityPolicy.WHEN_SELECTED)
	public Point getPoint1() {
		return point1;
	}

	public void setPoint1(Point point1) {
		this.point1 = point1;
	}

	@ControlPoint(visibilityPolicy = VisibilityPolicy.WHEN_SELECTED)
	public Point getPoint2() {
		return point2;
	}

	public void setPoint2(Point point2) {
		this.point2 = point2;
	}

	@EditableProperty(defaultable = true)
	public Size getWidth() {
		return width;
	}

	public void setWidth(Size width) {
		this.width = width;
	}

	@EditableProperty(defaultable = true)
	public Size getHeight() {
		return height;
	}

	public void setHeight(Size height) {
		this.height = height;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractLeadedDIYComponent other = (AbstractLeadedDIYComponent) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
