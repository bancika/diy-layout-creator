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

import org.diylc.common.DistributionMode;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.utils.IconLoader;

public class DistributeSelectionAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private DistributionMode mode;

  public DistributeSelectionAction(IPlugInPort plugInPort, DistributionMode mode) {
    super();
    this.plugInPort = plugInPort;
    this.mode = mode;
    putValue(AbstractAction.NAME, mode.toString());
    putValue(AbstractAction.SMALL_ICON, getIcon(mode).getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Distribute Selection triggered: " + mode);
    plugInPort.distributeSelection(mode);
  }

  private static IconLoader getIcon(DistributionMode mode) {
    switch (mode) {
      case HORIZONTAL_CENTERS:
        return IconLoader.DistributeCentersHorizontal;
      case HORIZONTAL_SPACING:
        return IconLoader.DistributeSpacingHorizontal;
      case VERTICAL_CENTERS:
        return IconLoader.DistributeCentersVertical;
      default:
        return IconLoader.DistributeSpacingVertical;
    }
  }
}
