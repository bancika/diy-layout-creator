package org.diylc.core.measures;

public enum VoltageUnit implements Unit {

	MV(1e-1, "mV"), V(1, "V"), KV(1e3, "KV");

	double factor;
	String display;

	private VoltageUnit(double factor, String display) {
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
