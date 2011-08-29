package org.diylc.core.measures;

import org.diylc.utils.Constants;

public class Size extends AbstractMeasure<SizeUnit> implements Comparable<Size> {

	private static final long serialVersionUID = 1L;

	public Size() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Size(Double value, SizeUnit unit) {
		super(value, unit);
		// TODO Auto-generated constructor stub
	}

	public double convertToPixels() {
//		double factor = getUnit().getFactor() / SizeUnit.in.getFactor();
//		int grids = (int) (factor * getValue() * Constants.GRIDS_PER_INCH);
//		double remainder = (factor * getValue() * Constants.GRIDS_PER_INCH) - grids;
//		return (int) Math.round(Constants.PIXELS_PER_INCH / Constants.GRIDS_PER_INCH
//				* (grids + remainder));
		return getValue() * getUnit().getFactor() / SizeUnit.in.getFactor() * Constants.PIXELS_PER_INCH;
	}

	@Override
	public Size clone() throws CloneNotSupportedException {
		return new Size(value, unit);
	}
	
	public static Size parseSize(String value) {
		for (SizeUnit unit : SizeUnit.values()) {
			if (value.toLowerCase().endsWith(unit.toString().toLowerCase())) {
				value = value.substring(0, value.length() - unit.toString().length() - 1).trim();
				return new Size(Double.parseDouble(value), unit);
			}
		}
		throw new IllegalArgumentException("Could not parse size: " + value);
	}

	@Override
	public int compareTo(Size o) {
		return new Double(value * unit.getFactor()).compareTo(o.getValue() * o.getUnit().getFactor());
	}
}
