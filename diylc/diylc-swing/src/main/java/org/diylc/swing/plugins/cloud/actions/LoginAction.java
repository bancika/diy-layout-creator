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
import org.apache.log4j.Logger;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.plugins.cloud.service.CloudException;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.cloud.CloudPlugIn;
import org.diylc.swing.plugins.cloud.view.LoginDialog;
import org.diylc.swingframework.ButtonDialog;
import org.diylc.utils.IconLoader;

public class LoginAction extends AbstractAction {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(LoginAction.class);
  private final CloudPlugIn cloudPlugIn;

  public LoginAction(CloudPlugIn cloudPlugIn) {
    super();
    this.cloudPlugIn = cloudPlugIn;
    putValue(AbstractAction.NAME, "Log In");
    putValue(AbstractAction.SMALL_ICON, IconLoader.IdCard.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    LoginDialog dialog = DialogFactory.getInstance().createLoginDialog();
    do {
      dialog.setVisible(true);
      if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
        try {
          if (cloudPlugIn.getPlugInPort().getCloudService().logIn(dialog.getUserName(), dialog.getPassword())) {
            cloudPlugIn.getSwingUI().showMessage(
                "You have successfully logged into the system. You will remain logged in from this machine until logged out.",
                "Login Successful", IView.INFORMATION_MESSAGE);
            cloudPlugIn.loggedIn();
            break;
          } else {
            cloudPlugIn.getSwingUI().showMessage(
                "Could not login. Possible reasons are wrong credentials or lack of internet connection.",
                "Login Error", IView.ERROR_MESSAGE);
          }
        } catch (CloudException e1) {
          cloudPlugIn.getSwingUI().showMessage("Could not login. Error: " + e1.getMessage(), "Login Error", IView.ERROR_MESSAGE);
        }
      } else
        break;
    } while (true);
  }
} 
