package org.diylc.core.measures;

public class Resistance extends AbstractMeasure<ResistanceUnit> {

	private static final long serialVersionUID = 1L;

	public Resistance() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Resistance(Double value, ResistanceUnit multiplier) {
		super(value, multiplier);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Resistance clone() throws CloneNotSupportedException {
		return new Resistance(value, unit);
	}

	public static Resistance parseResistance(String value) {
		for (ResistanceUnit unit : ResistanceUnit.values()) {
			if (value.toLowerCase().endsWith(unit.toString().toLowerCase())) {
				value = value.substring(0, value.length() - unit.toString().length() - 1).trim();
				return new Resistance(Double.parseDouble(value), unit);
			}
		}
		throw new IllegalArgumentException("Could not parse resistance: " + value);
	}
}
