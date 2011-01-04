package org.diylc.core.measures;

public class Inductance extends AbstractMeasure<InductanceUnit> {

	private static final long serialVersionUID = 1L;

	public Inductance() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Inductance(Double value, InductanceUnit unit) {
		super(value, unit);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Inductance clone() throws CloneNotSupportedException {
		return new Inductance(value, unit);
	}
}
