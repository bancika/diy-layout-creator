package org.diylc.components.semiconductors;

public enum BJTPolarity {

	NPN("NPN"), PNP("PNP");

	private String title;

	private BJTPolarity(String title) {
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
