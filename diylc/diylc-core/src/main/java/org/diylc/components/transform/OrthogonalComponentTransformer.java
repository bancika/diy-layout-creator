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
package org.diylc.components.transform;

import java.awt.geom.Point2D;

import org.diylc.common.OrientationHV;
import org.diylc.components.AbstractOrthogonalComponent;
import org.diylc.core.IDIYComponent;

/**
 * Rotates and mirrors orthogonal connectors. Quarter-turn rotation turns every horizontal run of
 * the route into a vertical one, so the start direction has to follow the points. Mirroring leaves
 * both axes in place and needs nothing beyond moving the points.
 * 
 * @author Branislav Stojkovic
 */
public class OrthogonalComponentTransformer extends SimpleComponentTransformer {

  @Override
  public void rotate(IDIYComponent<?> component, Point2D center, int direction) {
    super.rotate(component, center, direction);
    if (component instanceof AbstractOrthogonalComponent<?> orthogonal) {
      orthogonal.setStartDirection(orthogonal.getStartDirection() == OrientationHV.HORIZONTAL
          ? OrientationHV.VERTICAL : OrientationHV.HORIZONTAL);
    }
  }
}
