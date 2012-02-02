package org.diylc.components.passive;

public enum Power {

	EIGHT("1/8W"), QUARTER("1/4W"), HALF("1/2W"), ONE("1W"), TWO("2W"), FIVE("5W"), TEN("10W"), FIFTY(
			"50W"), HUNDRED("100W");

	String label;

	Power(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}
}
