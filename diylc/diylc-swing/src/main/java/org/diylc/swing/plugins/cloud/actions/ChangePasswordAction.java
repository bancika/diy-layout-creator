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
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.cloud.CloudPlugIn;
import org.diylc.swing.plugins.cloud.view.ChangePasswordDialog;
import org.diylc.swingframework.ButtonDialog;
import org.diylc.utils.IconLoader;

public class ChangePasswordAction extends AbstractAction {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(ChangePasswordAction.class);
  private final CloudPlugIn cloudPlugIn;

  public ChangePasswordAction(CloudPlugIn cloudPlugIn) {
    super();
    this.cloudPlugIn = cloudPlugIn;
    putValue(AbstractAction.NAME, "Change Password");
    putValue(AbstractAction.SMALL_ICON, IconLoader.KeyEdit.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final ChangePasswordDialog dialog = DialogFactory.getInstance().createChangePasswordDialog();
    dialog.setVisible(true);
    if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
      cloudPlugIn.getSwingUI().executeBackgroundTask(new ITask<Void>() {
        @Override
        public Void doInBackground() throws Exception {
          cloudPlugIn.getCloudService().updatePassword(dialog.getOldPassword(), dialog.getNewPassword());
          return null;
        }

        @Override
        public void failed(Exception e) {
          cloudPlugIn.getSwingUI().showMessage("Failed to update the password. Error: " + e.getMessage(), "Cloud Error",
              IView.ERROR_MESSAGE);
        }

        @Override
        public void complete(Void result) {
          cloudPlugIn.getSwingUI().showMessage("Password updated.", "Cloud", IView.INFORMATION_MESSAGE);
        }
      }, true);
    }
  }
} 
