package org.diylc.components.passive;

import java.awt.Point;

import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.ResistanceUnit;

public abstract class AbstractPotentiometer extends AbstractComponent<PotentiometerValue> {

	private static final long serialVersionUID = 1L;

	protected Point[] controlPoints;

	protected Resistance resistance = new Resistance(100d, ResistanceUnit.K);
	protected Orientation orientation = Orientation.DEFAULT;
	protected Taper taper;

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

	@EditableProperty
	public Resistance getResistance() {
		return resistance;
	}

	public void setResistance(Resistance resistance) {
		this.resistance = resistance;
	}

	@Override
	public PotentiometerValue getValue() {
		return new PotentiometerValue(resistance, taper);
	}

	@Override
	public void setValue(PotentiometerValue value) {
		this.resistance = value.getResistance();
		this.taper = value.getTaper();
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
