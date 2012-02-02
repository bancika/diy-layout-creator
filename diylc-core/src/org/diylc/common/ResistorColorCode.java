package org.diylc.common;

public enum ResistorColorCode {

	NONE, _4_BAND, _5_BAND;

	@Override
	public String toString() {
		if (name().startsWith("_")) {
			return name().substring(1).replace("_", " ").toLowerCase();
		}
		return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
	}
}
