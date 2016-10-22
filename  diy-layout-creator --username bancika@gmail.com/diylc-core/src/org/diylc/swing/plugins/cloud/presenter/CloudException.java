package org.diylc.swing.plugins.cloud.presenter;

public class CloudException extends Exception {

	private static final long serialVersionUID = 1L;

	public CloudException(Exception e) {
		super(e);
	}

	public CloudException(String string) {
		super(string);
	}

}
