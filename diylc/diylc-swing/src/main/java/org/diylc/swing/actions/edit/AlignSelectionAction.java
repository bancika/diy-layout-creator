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
package org.diylc.swing.actions.edit;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.diylc.common.AlignmentMode;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.utils.IconLoader;

public class AlignSelectionAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private AlignmentMode mode;

  public AlignSelectionAction(IPlugInPort plugInPort, AlignmentMode mode) {
    super();
    this.plugInPort = plugInPort;
    this.mode = mode;
    putValue(AbstractAction.NAME, mode.toString());
    putValue(AbstractAction.SMALL_ICON, getIcon(mode).getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Align Selection triggered: " + mode);
    plugInPort.alignSelection(mode);
  }

  private static IconLoader getIcon(AlignmentMode mode) {
    switch (mode) {
      case LEFT:
        return IconLoader.AlignLeft;
      case RIGHT:
        return IconLoader.AlignRight;
      case TOP:
        return IconLoader.AlignTop;
      case BOTTOM:
        return IconLoader.AlignBottom;
      case HORIZONTAL_CENTER:
        return IconLoader.AlignCenterHorizontal;
      default:
        return IconLoader.AlignCenterVertical;
    }
  }
}
