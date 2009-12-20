package com.diyfever.diylc.model.measures;

public enum ResistanceUnit implements Unit {

	R(1, "\u03a9"), K(1e3, "K"), M(1e6, "M");

	double factor;
	String display;

	private ResistanceUnit(double factor, String display) {
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
