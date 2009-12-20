package com.diyfever.diylc.common;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class ActivePoint extends Point {

	private static final long serialVersionUID = 1L;

	private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

	public ActivePoint() {
		super();
	}

	public ActivePoint(int x, int y) {
		super(x, y);
	}

	public ActivePoint(Point p) {
		super(p);
	}

	public void addPropertyChangeListener(PropertyChangeListener element) {
		listeners.add(element);
	}

	public boolean removePropertyChangeListener(Object o) {
		return listeners.remove(o);
	}

	@Override
	public void setLocation(double x, double y) {
		this.setLocation(new Point2D.Double(x, y));
	}

	@Override
	public void setLocation(int x, int y) {
		this.setLocation(new Point(x, y));
	}

	@Override
	public void setLocation(Point p) {
		for (PropertyChangeListener listener : listeners) {
			listener.propertyChange(new PropertyChangeEvent(this, "value",
					this, p));
		}
		super.setLocation(p);
	}

	public void setLocationSilent(double x, double y) {
		super.setLocation(x, y);
	}

	public void setLocationSilent(int x, int y) {
		super.setLocation(x, y);
	}

	public void setLocationSilent(Point p) {
		super.setLocation(p);
	}
}
