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
package org.diylc.components.autocreate;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.components.connectivity.SolderPad;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.IAutoCreator;

public class SolderPadAutoCreator implements IAutoCreator {

  private static final Logger LOG = Logger.getLogger(SolderPadAutoCreator.class);
  
  public static final String AUTO_PADS_KEY = "autoCreatePads";

  @Override
  public List<IDIYComponent<?>> createIfNeeded(IDIYComponent<?> lastAdded) {
    if (ConfigurationManager.getInstance().readBoolean(AUTO_PADS_KEY, false)
        && !(lastAdded.getClass().equals(SolderPad.class))) {
      List<IDIYComponent<?>> res = new ArrayList<IDIYComponent<?>>();
      for (int i = 0; i < lastAdded.getControlPointCount(); i++) {
        if (lastAdded.isControlPointSticky(i)) {
          try {
            SolderPad pad = new SolderPad();
            // instantiationManager.instantiateComponent(padType, null,
            // component.getControlPoint(i), currentProject)
            // .get(0);
            pad.setControlPoint(lastAdded.getControlPoint(i), 0);
            res.add(pad);
          } catch (Exception e) {
            LOG.warn("Could not auto-create solder pad", e);
          }
        }
      }
      return res;
    }
    return null;
  }
}
