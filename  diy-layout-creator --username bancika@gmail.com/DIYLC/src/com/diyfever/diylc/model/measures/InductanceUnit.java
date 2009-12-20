package com.diyfever.diylc.model.measures;

public enum InductanceUnit implements Unit {

	pH(1, "pH"), nH(1e3, "nH"), uH(1e6, "\u03bcH"), mH(1e9, "mH"), H(1e12, "H");

	double factor;
	String display;

	private InductanceUnit(double factor, String display) {
		this.factor = factor;
		this.display = display;
	}

	@Override
	public double getFactor() {
		return factor;
	}

	@Override
	public String toString() {
		return display;
	}
}
