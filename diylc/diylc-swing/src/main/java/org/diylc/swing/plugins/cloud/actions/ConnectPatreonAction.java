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

import org.apache.log4j.Logger;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.plugins.cloud.service.CloudException;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.cloud.CloudPlugIn;
import org.diylc.swing.plugins.cloud.view.UserEditDialog;
import org.diylc.swingframework.ButtonDialog;
import org.diylc.utils.IconLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ConnectPatreonAction extends AbstractAction {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(ConnectPatreonAction.class);
  private final IPlugInPort plugInPort;
  private final ISwingUI swingUI;

  public ConnectPatreonAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    putValue(AbstractAction.NAME, "Connect to Patreon");
    putValue(AbstractAction.SMALL_ICON, IconLoader.Patreon.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!plugInPort.getCloudService().isLoggedIn()) {
      swingUI.showMessage("You must be logged in to DIYLC Cloud account in order to connect with Patreon account.", "Error",
          ISwingUI.ERROR_MESSAGE);
      return;
    }
    String username = plugInPort.getCloudService().getCurrentUsername();
    String url = "http://diy-fever.com/diylc/api/v1/ai/patreon_login.php?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
    try {
      java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
    } catch (Exception ex) {
      LOG.error("Failed to open Patreon connection page", ex);
      swingUI.showMessage("Failed to open Patreon connection page. Please visit " + url + " manually.", "Error",
          ISwingUI.ERROR_MESSAGE);
    }
  }
} 
