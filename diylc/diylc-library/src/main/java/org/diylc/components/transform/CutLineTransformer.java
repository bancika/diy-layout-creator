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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.diylc.common.IComponentTransformer;
import org.diylc.common.OrientationHV;
import org.diylc.components.connectivity.CutLine;
import org.diylc.core.IDIYComponent;

public class CutLineTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component.getClass().equals(CutLine.class);
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return component.getClass().equals(CutLine.class);
  }

  @Override
  public boolean mirroringChangesCircuit() {
    return false;
  }

  @Override
  public void rotate(IDIYComponent<?> component, Point2D center, int direction) {
    CutLine line = (CutLine) component;
    AffineTransform rotate =
        AffineTransform.getRotateInstance(Math.PI / 2 * direction, center.getX(), center.getY());
    Point2D start = rotate.transform(line.getControlPoint(0), null);
    Point2D end = rotate.transform(getEnd(line), null);

    if (line.getOrientation() == OrientationHV.HORIZONTAL) {
      line.setOrientation(OrientationHV.VERTICAL);
    } else {
      line.setOrientation(OrientationHV.HORIZONTAL);
    }
    setEndpoints(line, start, end);
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point2D center, int direction) {
    CutLine line = (CutLine) component;
    Point2D start = mirror(line.getControlPoint(0), center, direction);
    Point2D end = mirror(getEnd(line), center, direction);
    setEndpoints(line, start, end);
  }

  // the line always extends right or down from its control point, so after a transform the
  // control point has to move to whichever end is now top-left
  private void setEndpoints(CutLine line, Point2D start, Point2D end) {
    line.setControlPoint(new Point2D.Double(Math.min(start.getX(), end.getX()),
        Math.min(start.getY(), end.getY())), 0);
  }

  private Point2D getEnd(CutLine line) {
    Point2D start = line.getControlPoint(0);
    double length = line.getLength().convertToPixels();
    if (line.getOrientation() == OrientationHV.HORIZONTAL) {
      return new Point2D.Double(start.getX() + length, start.getY());
    }
    return new Point2D.Double(start.getX(), start.getY() + length);
  }

  private Point2D mirror(Point2D p, Point2D center, int direction) {
    if (direction == IComponentTransformer.HORIZONTAL) {
      return new Point2D.Double(2 * center.getX() - p.getX(), p.getY());
    }
    return new Point2D.Double(p.getX(), 2 * center.getY() - p.getY());
  }
}
