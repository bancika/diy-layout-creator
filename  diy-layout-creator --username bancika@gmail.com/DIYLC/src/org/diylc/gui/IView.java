package org.diylc.gui;

import javax.swing.Action;
import javax.swing.JComponent;

import org.diylc.common.BadPositionException;
import org.diylc.common.IPlugInPort;


/**
 * Base interface for the main GUI component.
 * 
 * @author Branislav Stojkovic
 */
public interface IView {

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
}
