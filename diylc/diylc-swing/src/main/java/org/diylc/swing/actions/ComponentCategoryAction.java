/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
package org.diylc.swing.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;

import org.diylc.common.IPlugInPort;
import org.diylc.core.IView;

public class ComponentCategoryAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private final IPlugInPort plugInPort;
  private final String category;

  public ComponentCategoryAction(IPlugInPort plugInPort, String category) {
    super();
    this.plugInPort = plugInPort;
    this.category = category;
    putValue(AbstractAction.NAME, category);
    putValue(IView.CHECK_BOX_MENU_ITEM, true);
    putValue(AbstractAction.SELECTED_KEY, plugInPort.isCategoryEnabled(category));

    ConfigurationManager.getInstance().addConfigListener(
        IPlugInPort.DISABLED_CATEGORIES_KEY, new IConfigListener() {

          @Override
          public void valueChanged(String key, Object value) {
            putValue(AbstractAction.SELECTED_KEY, plugInPort.isCategoryEnabled(category));
          }
        });
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    boolean selected = Boolean.TRUE.equals(getValue(AbstractAction.SELECTED_KEY));
    plugInPort.setCategoryEnabled(category, selected);
  }
}
