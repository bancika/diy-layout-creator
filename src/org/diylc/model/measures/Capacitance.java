package org.diylc.model.measures;

public class Capacitance extends AbstractMeasure<CapacitanceUnit> {

	private static final long serialVersionUID = 1L;

	public Capacitance() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Capacitance(Double value, CapacitanceUnit unit) {
		super(value, unit);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Capacitance clone() throws CloneNotSupportedException {
		return new Capacitance(value, unit);
	}
}
