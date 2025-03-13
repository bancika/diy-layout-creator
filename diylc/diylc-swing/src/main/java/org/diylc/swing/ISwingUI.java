/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.swing;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu.Separator;

import org.diylc.common.BadPositionException;
import org.diylc.common.ITask;
import org.diylc.core.IView;

import javax.swing.SwingConstants;

/**
 * Interface for plugin access to the swing front end.
 * 
 * @author Branislav Stojkovic
 */
public interface ISwingUI extends IView {

  /**
   * Injects a custom GUI panels provided by the plug-in and desired position in the window.
   * Application will layout plug-in panels accordingly. <br>
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
   * @param collapsible
   * @param visibilityConfigKey in case of a collapsible view, this config key is used to persist the state
   * @throws BadPositionException in case invalid position is specified
   */
  void injectGUIComponent(JComponent component, int position, boolean collapsible, String visibilityConfigKey) throws BadPositionException;

  /**
   * Injects a custom menu action into application's main menu. If <code>action</code> is set to
   * null {@link Separator} will be added. If the specified menu does not exist it will be
   * automatically created.
   * 
   * @param action {@link Action} to inser
   * @param menuName name of the menu to insert into
   */
  void injectMenuAction(Action action, String menuName);
  
  void injectMenuComponent(JComponent component);

  /**
   * Injects a custom submenu into application's main menu. If the specified menu does not exist it
   * will be automatically created.
   * 
   * @param name
   * @param icon
   * @param parentMenuName
   */
  void injectSubmenu(String name, Icon icon, String parentMenuName);

  /**
   * Injects a dynamic submenu into application's main menu. Items are read from the
   * <code>handler</code> and notifications are sent to the <code>handler</code> when an item is
   * clicked on.
   * 
   * @param name
   * @param icon
   * @param parentMenuName
   * @param handler
   */
  void injectDynamicSubmenu(String name, Icon icon, String parentMenuName, IDynamicSubmenuHandler handler);

  /**
   * Runs a task in background while showing busy cursor and a glass pane if needed.
   * 
   * @param task
   * @param blockUI
   */
  <T extends Object> void executeBackgroundTask(ITask<T> task, boolean blockUI);

  /**
   * @return {@link JFrame} that can be used to reference secondary dialogs and frames
   */
  JFrame getOwnerFrame();

  /**
   * 
   */
  void bringToFocus();
}
