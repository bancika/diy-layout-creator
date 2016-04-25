package org.diylc.components.semiconductors;

public enum SymbolFlipping {

	NONE, Y;

	@Override
	public String toString() {
		switch (this) {
		case Y:
			return "Y-axis";
		case NONE:
			return "None";
		default:
			return name();
		}
	}
}