package org.diylc.core.measures;

import org.diylc.utils.Constants;

public class Size extends AbstractMeasure<SizeUnit> {

	private static final long serialVersionUID = 1L;

	public Size() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Size(Double value, SizeUnit unit) {
		super(value, unit);
		// TODO Auto-generated constructor stub
	}

	public int convertToPixels() {
		double factor = getUnit().getFactor() / SizeUnit.in.getFactor();
		return (int) Math.round(factor * Constants.PIXELS_PER_INCH * getValue());
	}

	@Override
	public Size clone() throws CloneNotSupportedException {
		return new Size(value, unit);
	}
}
