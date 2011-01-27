package org.diylc.common;

public enum ResistorColorCode {

	_4_BAND, _5_BAND;
	
	@Override
	public String toString() {
		return name().substring(1).replace("_", " ").toLowerCase();
	}
}
