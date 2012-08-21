package org.diylc.components.passive;

import org.diylc.core.measures.Resistance;

public class ResistorValue {

	private Resistance resistance;
	private Power power;

	public ResistorValue(Resistance resistance, Power power) {
		super();
		this.resistance = resistance;
		this.power = power;
	}

	public Resistance getResistance() {
		return resistance;
	}

	public Power getPower() {
		return power;
	}
	
	@Override
	public String toString() {
		return resistance.toString() + power.toString();
	}
}
