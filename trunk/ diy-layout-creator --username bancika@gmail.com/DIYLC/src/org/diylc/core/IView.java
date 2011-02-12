package org.diylc.core;


/**
 * Base interface for the main GUI component.
 * 
 * @author Branislav Stojkovic
 */
public interface IView {

	public static final int ERROR_MESSAGE = 0;
	public static final int INFORMATION_MESSAGE = 1;
	public static final int WARNING_MESSAGE = 2;
	public static final int QUESTION_MESSAGE = 3;
	public static final int PLAIN_MESSAGE = -1;
	public static final int DEFAULT_OPTION = -1;
	public static final int YES_NO_OPTION = 0;
	public static final int YES_NO_CANCEL_OPTION = 1;
	public static final int OK_CANCEL_OPTION = 2;
	public static final int YES_OPTION = 0;
	public static final int NO_OPTION = 1;
	public static final int CANCEL_OPTION = 2;
	public static final int OK_OPTION = 0;
	
	public static final String CHECK_BOX_MENU_ITEM = "org.diylc.checkBoxMenuItem";
	public static final String RADIO_BUTTON_GROUP_KEY = "org.diylc.radioButtonGroup";

	void showMessage(String message, String title, int messageType);

	int showConfirmDialog(String message, String title, int optionType, int messageType);
}
