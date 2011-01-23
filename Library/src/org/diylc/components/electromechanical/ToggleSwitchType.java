package org.diylc.components.electromechanical;

public enum ToggleSwitchType {

	SPST, SPDT, DPDT, _3PDT, _4PDT, _5PDT;

	@Override
	public String toString() {
		return name().replace("_", "");
	}
}
