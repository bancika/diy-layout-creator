package org.diylc.components;

import java.awt.Point;

import org.diylc.core.IDIYComponent;

public abstract class AbstractSinglePointComponent implements IDIYComponent<Void> {

	private static final long serialVersionUID = 1L;

	protected Point point = new Point(0, 0);

	@Override
	public int getControlPointCount() {
		return 1;
	}

	@Override
	public Point getControlPoint(int index) {
		return point;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		this.point.setLocation(point);
	}
	
	@Override
	public String getName() {
		return "Pad";
	}
	
	@Override
	public void setName(String name) {
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
}
