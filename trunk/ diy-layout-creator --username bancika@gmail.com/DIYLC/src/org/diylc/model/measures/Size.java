package org.diylc.model.measures;

import java.awt.Toolkit;

public class Size extends AbstractMeasure<SizeUnit> {

	private static final long serialVersionUID = 1L;

	public static final int PIXELS_PER_INCH = Toolkit.getDefaultToolkit().getScreenResolution();

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
		return (int) (factor * PIXELS_PER_INCH * getValue());
	}

	@Override
	public Size clone() throws CloneNotSupportedException {
		return new Size(value, unit);
	}
}
