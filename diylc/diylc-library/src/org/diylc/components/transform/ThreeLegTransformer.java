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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.diylc.common.IComponentTransformer;
import org.diylc.common.Orientation;
import org.diylc.components.Abstract3LegSymbol;
import org.diylc.components.semiconductors.SymbolFlipping;
import org.diylc.core.IDIYComponent;

public class ThreeLegTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return Abstract3LegSymbol.class.isAssignableFrom(component.getClass());
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return Abstract3LegSymbol.class.isAssignableFrom(component.getClass());
  }
  
  @Override
  public boolean mirroringChangesCircuit() {   
    return true;
  }

  @Override
  public void rotate(IDIYComponent<?> component, Point2D center, int direction) {
    AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2 * direction, center.getX(), center.getY());
    for (int index = 0; index < component.getControlPointCount(); index++) {
      Point2D p = new Point2D.Double();
      rotate.transform(component.getControlPoint(index), p);
      component.setControlPoint(p, index);
    }

    Abstract3LegSymbol a3l = (Abstract3LegSymbol) component;
    Orientation o = a3l.getOrientation();
    int oValue = o.ordinal();
    oValue += direction;
    if (oValue < 0)
      oValue = Orientation.values().length - 1;
    if (oValue >= Orientation.values().length)
      oValue = 0;
    o = Orientation.values()[oValue];
    a3l.setOrientation(o);
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point2D center, int direction) {
    Abstract3LegSymbol a3l = (Abstract3LegSymbol) component;

    Orientation o = a3l.getOrientation();
    SymbolFlipping flip = a3l.getFlip();
    
    if (direction == IComponentTransformer.HORIZONTAL) {
     if (o == Orientation.DEFAULT || o == Orientation._180) {
       if (flip == SymbolFlipping.NONE)
         a3l.setFlip(SymbolFlipping.X);
       else if (flip == SymbolFlipping.X)
         a3l.setFlip(SymbolFlipping.NONE);
     } else if (o == Orientation._90 || o == Orientation._270) {
       if (flip == SymbolFlipping.NONE)
         a3l.setFlip(SymbolFlipping.Y);
       else if (flip == SymbolFlipping.Y)
         a3l.setFlip(SymbolFlipping.NONE);
     }
    } else {
      if (o == Orientation.DEFAULT || o == Orientation._180) {
        if (flip == SymbolFlipping.NONE)
          a3l.setFlip(SymbolFlipping.Y);
        else if (flip == SymbolFlipping.Y)
          a3l.setFlip(SymbolFlipping.NONE);
      } else if (o == Orientation._90 || o == Orientation._270) {
        if (flip == SymbolFlipping.NONE)
          a3l.setFlip(SymbolFlipping.X);
        else if (flip == SymbolFlipping.X)
          a3l.setFlip(SymbolFlipping.NONE);
      }
    }
  }
}
