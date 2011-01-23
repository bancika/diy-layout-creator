package org.diylc.core;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;

import org.diylc.common.BadPositionException;
import org.diylc.common.IPlugInPort;

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

	/**
	 * Adds a component to the view.
	 * 
	 * @param component
	 *            component to insert
	 * @param position
	 *            target position
	 * @throws BadPositionException
	 * 
	 * @see IPlugInPort#injectGUIComponent
	 */
	void addComponent(JComponent component, int position) throws BadPositionException;

	/**
	 * Adds a menu action.
	 * 
	 * @param action
	 *            action to insert
	 * @param menuName
	 *            target menu name
	 * 
	 * @see IPlugInPort#injectMenuAction
	 */
	void addMenuAction(Action action, String menuName);

	/**
	 * Adds a submenu to an existing menu.
	 * 
	 * @param name
	 * @param icon
	 * @param parentMenuName
	 */
	void addSubmenu(String name, Icon icon, String parentMenuName);

	void showMessage(String message, String title, int messageType);

	int showConfirmDialog(String message, String title, int optionType, int messageType);
}
