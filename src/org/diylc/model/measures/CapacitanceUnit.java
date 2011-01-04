package org.diylc.model.measures;

public enum CapacitanceUnit implements Unit {

	pF(1, "pF"), nF(1e3, "nF"), uF(1e6, "\u03bcF"), mF(1e9, "mF"), F(1e12, "F");

	double factor;
	String display;

	private CapacitanceUnit(double factor, String display) {
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
