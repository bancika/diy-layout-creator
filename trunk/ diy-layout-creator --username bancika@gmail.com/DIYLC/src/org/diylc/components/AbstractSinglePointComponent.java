package org.diylc.components;

import java.awt.Point;

public abstract class AbstractSinglePointComponent extends AbstractComponent<Void> {

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
