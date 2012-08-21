package org.diylc.core.measures;

public class Inductance extends AbstractMeasure<InductanceUnit> {

	private static final long serialVersionUID = 1L;

//	public Inductance() {
//		super();
//		// TODO Auto-generated constructor stub
//	}

	public Inductance(Double value, InductanceUnit unit) {
		super(value, unit);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Inductance clone() throws CloneNotSupportedException {
		return new Inductance(value, unit);
	}

	public static Inductance parseCapacitance(String value) {
		for (InductanceUnit unit : InductanceUnit.values()) {
			if (value.toLowerCase().endsWith(unit.toString().toLowerCase())) {
				value = value.substring(0, value.length() - unit.toString().length() - 1).trim();
				return new Inductance(Double.parseDouble(value), unit);
			}
		}
		throw new IllegalArgumentException("Could not parse inductance: " + value);
	}
}
