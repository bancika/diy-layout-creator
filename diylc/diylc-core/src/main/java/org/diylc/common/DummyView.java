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
package org.diylc.common;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.diylc.core.IView;

public class DummyView implements IView {

  @Override
  public int showConfirmDialog(String message, String title, int optionType, int messageType) {
    return 0;
  }

  @Override
  public void showMessage(String message, String title, int messageType) {}

  @Override
  public File promptFileSave() {
    return null;
  }

  @Override
  public boolean editProperties(List<PropertyWrapper> properties, Set<PropertyWrapper> defaultedProperties, String title) {
    return false;
  }

  @Override
  public String showInputDialog(String message, String title) {
    return null;
  }
}
