package org.diylc.components.electromechanical;

public enum DCPolarity {

	NONE("None"), CENTER_POSITIVE("Center Positive"), CENTER_NEGATIVE("Center Negative");

	private String title;

	private DCPolarity(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return title;
	}
}
