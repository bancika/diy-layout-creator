package org.diylc.components.passive;

import java.awt.Point;

import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.ResistanceUnit;

public abstract class AbstractPotentiometer extends
		AbstractTransparentComponent<Resistance> {

	private static final long serialVersionUID = 1L;

	protected Point[] controlPoints;

	protected Resistance resistance = new Resistance(100d, ResistanceUnit.K);
	protected Orientation orientation = Orientation.DEFAULT;
	protected Taper taper = Taper.LIN;

	@Override
	public int getControlPointCount() {
		return controlPoints.length;
	}

	@Override
	public Point getControlPoint(int index) {
		return controlPoints[index];
	}

	@Override
	public void setControlPoint(Point point, int index) {
		controlPoints[index].setLocation(point);
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.NEVER;
	}

	@Override
	public boolean isControlPointSticky(int index) {
		return true;
	}
	
	@Override
	public String getValueForDisplay() {
		return resistance.toString() + " " + taper.toString();
	}

	@EditableProperty
	@Override
	public Resistance getValue() {
		return resistance;
	}

	@Override
	public void setValue(Resistance value) {
		this.resistance = value;
	}

	@EditableProperty
	public Taper getTaper() {
		return taper;
	}

	public void setTaper(Taper taper) {
		this.taper = taper;
	}

	@EditableProperty
	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}
}
