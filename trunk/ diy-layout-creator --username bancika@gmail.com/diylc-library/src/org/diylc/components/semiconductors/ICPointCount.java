package org.diylc.components.semiconductors;

public enum ICPointCount {
	_3("3", 3), _5("5", 5);

	private String title;
	private int value;

	private ICPointCount(String title, int value) {
		this.title = title;
		this.value = value;
	}

	@Override
	public String toString() {
		return title;
	}
	
	public int getValue() {
		return value;
	}
}
