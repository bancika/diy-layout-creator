package org.diylc.common;

public enum VerticalAlignment {
	TOP, CENTER, BOTTOM;
	
	public String toString() {
		return name().substring(0, 1) + name().substring(1).toLowerCase();
	}
}
