package org.diylc.core.measures;

public class Voltage extends AbstractMeasure<VoltageUnit> {

	private static final long serialVersionUID = 1L;

//	public Voltage() {
//		super();
//		// TODO Auto-generated constructor stub
//	}

	public Voltage(Double value, VoltageUnit unit) {
		super(value, unit);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Voltage clone() throws CloneNotSupportedException {
		return new Voltage(value, unit);
	}

	public static Voltage parseCapacitance(String value) {
		for (VoltageUnit unit : VoltageUnit.values()) {
			if (value.toLowerCase().endsWith(unit.toString().toLowerCase())) {
				value = value.substring(0, value.length() - unit.toString().length() - 1).trim();
				return new Voltage(Double.parseDouble(value), unit);
			}
		}
		throw new IllegalArgumentException("Could not parse voltage: " + value);
	}
}
