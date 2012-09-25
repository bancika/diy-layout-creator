package org.diylc.core.measures;

public enum CurrentUnit implements Unit {

	uA(1e-6, "uA"), mA(1e-3, "mA"), A(1, "A");

	double factor;
	String display;

	private CurrentUnit(double factor, String display) {
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
