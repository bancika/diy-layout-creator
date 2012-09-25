package org.diylc.core.measures;

public class Current extends AbstractMeasure<CurrentUnit> {

	private static final long serialVersionUID = 1L;

	public Current(Double value, CurrentUnit unit) {
		super(value, unit);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Current clone() throws CloneNotSupportedException {
		return new Current(value, unit);
	}
}
