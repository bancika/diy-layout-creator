package org.diylc.components;

import java.awt.Point;

import org.diylc.core.IComponentInstance;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomName;
import org.diylc.core.annotations.ControlPoint;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.utils.Constants;


/**
 * Base class for all leaded components such as resistors or capacitors. Has two
 * control points for leads and one control point for the label.
 * 
 * @author Branislav Stojkovic
 */
public abstract class AbstractLeadedComponentInstance implements IComponentInstance {

	private static final long serialVersionUID = 1L;

	protected Size width;
	protected Size height;
	protected Point point1 = new Point(0, 0);
	protected Point point2 = new Point((int) (Constants.GRID * 10), 0);
	protected Point labelPoint = new Point((int) (Constants.GRID * 5), -(int) Constants.GRID);
	protected String name = "New Component";

	protected AbstractLeadedComponentInstance() {
		super();
		this.width = getDefaultWidth();
		this.height = getDefaultHeight();
	}

	/**
	 * Returns the default component width.
	 * 
	 * @return
	 */
	protected abstract Size getDefaultWidth();

	/**
	 * Returns the default component height.
	 * 
	 * @return
	 */
	protected abstract Size getDefaultHeight();

	@BomName
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

	@ControlPoint(visibilityPolicy = VisibilityPolicy.WHEN_SELECTED)
	public Point getLabelPoint() {
		return labelPoint;
	}

	public void setLabelPoint(Point labelPoint) {
		this.labelPoint = labelPoint;
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
		AbstractLeadedComponentInstance other = (AbstractLeadedComponentInstance) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
