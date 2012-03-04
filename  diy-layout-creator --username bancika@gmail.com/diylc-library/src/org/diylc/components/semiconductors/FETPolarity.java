package org.diylc.components.semiconductors;

public enum FETPolarity {

	NEGATIVE("Negative"), POSITIVE("Positive");

	private String title;

	private FETPolarity(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return title;
	}
}
