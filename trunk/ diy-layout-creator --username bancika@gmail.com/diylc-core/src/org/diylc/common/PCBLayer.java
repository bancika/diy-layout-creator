package org.diylc.common;

public enum PCBLayer {

	_1, _2, _3, _4, _5;
	
	public String toString() {
		return name().substring(1);
	};
}
