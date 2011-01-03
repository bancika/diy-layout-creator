package com.diyfever.diylc.components;

import java.awt.Point;

import com.diyfever.diylc.model.IComponentInstance;
import com.diyfever.diylc.model.VisibilityPolicy;
import com.diyfever.diylc.model.annotations.BomName;
import com.diyfever.diylc.model.annotations.ControlPoint;
import com.diyfever.diylc.model.annotations.EditableProperty;
import com.diyfever.diylc.model.measures.Size;
import com.diyfever.diylc.utils.Constants;

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
	protected Point point2 = new Point(0, (int) (Constants.GRID * 10));
	protected Point labelPoint = new Point((int) Constants.GRID, (int) (Constants.GRID * 5));
	protected String name = "New Component";

	protected AbstractLeadedComponentInstance() {
		super();
		this.width = getDefaultWidth();
		this.height = getDefaultHeight();
	}

	protected abstract Size getDefaultWidth();

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
}
