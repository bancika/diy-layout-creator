package org.diylc.components.interconnects;

public enum AWG {

	_8, _10, _12, _14, _16, _18, _20, _22, _24, _26, _28, _30;

	@Override
	public String toString() {
		return "#" + name().replace("_", "");
	}

	public int getValue() {
		return Integer.parseInt(name().replace("_", ""));
	}
}
