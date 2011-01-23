package org.diylc.components.passive;

import org.diylc.core.measures.Resistance;

public class PotentiometerValue {

	private Resistance resistance;
	private Taper taper;

	public PotentiometerValue(Resistance resistance, Taper taper) {
		super();
		this.resistance = resistance;
		this.taper = taper;
	}

	public Resistance getResistance() {
		return resistance;
	}

	public Taper getTaper() {
		return taper;
	}

	@Override
	public String toString() {
		return resistance.toString() + taper.toString();
	}
}
