package org.diylc.core.measures;

public enum PowerUnit implements Unit {

	MW(1e-1, "mW"), W(1, "W"), KW(1e3, "KW");

	double factor;
	String display;

	private PowerUnit(double factor, String display) {
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
