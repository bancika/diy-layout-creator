package org.diylc.swing;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.JPopupMenu.Separator;

import org.diylc.common.BadPositionException;

/**
 * Interface for plugin access to the swing front end.
 * 
 * @author Branislav Stojkovic
 */
public interface ISwingUI {

	/**
	 * Injects a custom GUI panels provided by the plug-in and desired position
	 * in the window. Application will layout plug-in panels accordingly. <br>
	 * Valid positions are:
	 * <ul>
	 * <li> {@link SwingConstants#TOP}</li>
	 * <li> {@link SwingConstants#BOTTOM}</li>
	 * <li> {@link SwingConstants#LEFT}</li>
	 * <li> {@link SwingConstants#RIGHT}</li>
	 * </ul>
	 * 
	 * Center position is reserved for the main canvas panel and cannot be used.
	 * 
	 * @param component
	 * @param position
	 * @throws BadPositionException
	 *             in case invalid position is specified
	 */
	void injectGUIComponent(JComponent component, int position) throws BadPositionException;

	/**
	 * Injects a custom menu action into application's main menu. If
	 * <code>action</code> is set to null {@link Separator} will be added. If
	 * the specified menu does not exist it will be automatically created.
	 * 
	 * @param action
	 *            {@link Action} to inser
	 * @param menuName
	 *            name of the menu to insert into
	 */
	void injectMenuAction(Action action, String menuName);

	/**
	 * Injects a custom submenu into application's main menu. If
	 * <code>action</code> is set to null {@link Separator} will be added. If
	 * the specified menu does not exist it will be automatically created.
	 * 
	 * @param name
	 * @param icon
	 * @param parentMenuName
	 */
	void injectSubmenu(String name, Icon icon, String parentMenuName);
}
