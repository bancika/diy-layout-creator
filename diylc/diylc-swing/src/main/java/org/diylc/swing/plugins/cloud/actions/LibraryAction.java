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
package org.diylc.swing.plugins.cloud.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.diylc.swing.plugins.cloud.CloudPlugIn;
import org.diylc.utils.IconLoader;

public class LibraryAction extends AbstractAction {
  private static final long serialVersionUID = 1L;
  private final CloudPlugIn cloudPlugIn;

  public LibraryAction(CloudPlugIn cloudPlugIn) {
    super();
    this.cloudPlugIn = cloudPlugIn;
    putValue(AbstractAction.NAME, "Search The Cloud");
    putValue(AbstractAction.SMALL_ICON, IconLoader.Cloud.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    cloudPlugIn.getCloudBrowser().setVisible(true);
    cloudPlugIn.getCloudBrowser().requestFocus();
  }
} 
