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
package org.diylc.swing.gui.components;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class OverwritePromptFileChooser extends JFileChooser {

  private static final long serialVersionUID = 1L;
  private String defaultExtension;

  public void approveSelection() {
    File lSelectedFile;
    if (getSelectedFile().getAbsolutePath().contains(".")) {
      lSelectedFile = getSelectedFile();
    } else {
      lSelectedFile = new File(getSelectedFile().getAbsoluteFile() + "." + defaultExtension);
    }

    if (lSelectedFile != null && lSelectedFile.exists()) {
      int lResponse =
          JOptionPane.showConfirmDialog(this, lSelectedFile.getAbsolutePath() + " already exists.\nDo you "
              + "want to replace it?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

      if (lResponse != JOptionPane.YES_OPTION) {
        return;
      }
    }
    super.approveSelection();
  }

  public void setFileFilter(FileFilter filter, String defaultExtension) {
    this.defaultExtension = defaultExtension;
    super.setFileFilter(filter);
  }
}
