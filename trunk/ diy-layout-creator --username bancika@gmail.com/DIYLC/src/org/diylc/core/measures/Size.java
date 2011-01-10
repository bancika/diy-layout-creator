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
		int grids = (int) (factor * getValue() * Constants.GRIDS_PER_INCH);
		double remainder = (factor * getValue() * Constants.GRIDS_PER_INCH) - grids;
		return (int) Math.round(Constants.GRID * (grids + remainder));
	}

	@Override
	public Size clone() throws CloneNotSupportedException {
		return new Size(value, unit);
	}
}
