package org.diylc.model.measures;

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
}
