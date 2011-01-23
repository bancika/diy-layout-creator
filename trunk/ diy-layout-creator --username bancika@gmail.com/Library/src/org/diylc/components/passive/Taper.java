package org.diylc.components.passive;

public enum Taper {

	LIN, LOG, REV_LOG, W, S;

	@Override
	public String toString() {
		return name().substring(0, 1).toUpperCase()
				+ name().substring(1).toLowerCase().replace("_", "");
	}
}
