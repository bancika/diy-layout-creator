package com.diyfever.diylc.model.measures;

public enum SizeUnit implements Unit {

	mm(1d), cm(10d), m(1e4d), in(25.4d), ft(25.4d * 12), yd(9144d);

	double factor;

	private SizeUnit(double factor) {
		this.factor = factor;
	}

	@Override
	public double getFactor() {
		return factor;
	}
}
