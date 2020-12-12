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
package org.diylc.components.transform;

import java.awt.geom.Point2D;

import org.diylc.common.OrientationHV;
import org.diylc.components.boards.AbstractVeroBoard;
import org.diylc.core.IDIYComponent;

public class VeroBoardTransformer extends SimpleComponentTransformer {

  @Override
  public void rotate(IDIYComponent<?> component, Point2D center, int direction) {
    super.rotate(component, center, direction);
    
    AbstractVeroBoard board = (AbstractVeroBoard)component;
    OrientationHV orientation = board.getOrientation();
    if (orientation == OrientationHV.HORIZONTAL)
      board.setOrientation(OrientationHV.VERTICAL);
    else
      board.setOrientation(OrientationHV.HORIZONTAL);
  }
}
