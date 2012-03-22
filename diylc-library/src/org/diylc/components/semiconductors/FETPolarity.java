package org.diylc.components.semiconductors;

public enum FETPolarity {

	NEGATIVE, POSITIVE;

	@Override
	public String toString() {
		return name().substring(0, 1) + name().substring(1).toLowerCase();
	}
}
