package org.diylc.common;

public enum Orientation {

	DEFAULT, _90, _180, _270;

	@Override
	public String toString() {
		if (this == DEFAULT) {
			return "Default";
		} else {
			return name().replace("_", "") + " degrees clockwise";
		}
	}
}
