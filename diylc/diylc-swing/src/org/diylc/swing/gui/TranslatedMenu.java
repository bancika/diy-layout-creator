/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.swing.gui;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.diylc.lang.LangUtil;

public class TranslatedMenu extends JMenu {

  private static final long serialVersionUID = 1L;
  
  public TranslatedMenu(String name) {
    super(LangUtil.translate(name));
  }

  @Override
  public JMenuItem add(Action action) {
    String name = (String) action.getValue(Action.NAME);
    if (name != null)
      action.putValue(Action.NAME, LangUtil.translate(name));
    return super.add(action);
  }
}
