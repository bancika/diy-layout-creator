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
package org.diylc.components.passive;

import java.awt.geom.Point2D;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractLabeledComponent;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Resistance;

public abstract class AbstractPotentiometer extends AbstractLabeledComponent<Resistance> {

  private static final long serialVersionUID = 1L;

  protected Point2D[] controlPoints;

  protected Resistance resistance = null;
  protected Orientation orientation = Orientation.DEFAULT;
  protected Taper taper = Taper.LIN;

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point2D    point, int index) {
    controlPoints[index].setLocation(point);
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public String getValueForDisplay() {
    return (resistance == null ? "" : resistance.toString()) + " " + taper.toString();
  }

  @Override
  @EditableProperty(validatorClass = PositiveMeasureValidator.class)
  public Resistance getValue() {
    return resistance;
  }

  @Override
  public void setValue(Resistance value) {
    this.resistance = value;
  }

  @EditableProperty
  public Taper getTaper() {
    return taper;
  }

  public void setTaper(Taper taper) {
    this.taper = taper;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
  }
}
