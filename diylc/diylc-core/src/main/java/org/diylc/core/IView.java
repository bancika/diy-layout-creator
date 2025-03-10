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
package org.diylc.core;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.diylc.common.PropertyWrapper;


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

  boolean editProperties(List<PropertyWrapper> properties, Set<PropertyWrapper> defaultedProperties, String title);
  
  String showInputDialog(String message, String title);

  File promptFileSave();
}
