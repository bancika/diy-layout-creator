package org.diylc.core.measures;

public class Capacitance extends AbstractMeasure<CapacitanceUnit> {

	private static final long serialVersionUID = 1L;

//	public Capacitance() {
//		super();
//		// TODO Auto-generated constructor stub
//	}

	public Capacitance(Double value, CapacitanceUnit unit) {
		super(value, unit);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Capacitance clone() throws CloneNotSupportedException {
		return new Capacitance(value, unit);
	}
	
	public static Capacitance parseCapacitance(String value) {
		for (CapacitanceUnit unit : CapacitanceUnit.values()) {
			if (value.toLowerCase().endsWith(unit.toString().toLowerCase())) {
				value = value.substring(0, value.length() - unit.toString().length() - 1).trim();
				return new Capacitance(Double.parseDouble(value), unit);
			}
		}
		throw new IllegalArgumentException("Could not parse capacitance: " + value);
	}
}
