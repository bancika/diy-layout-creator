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
package org.diylc.components;

import java.awt.Rectangle;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

/**
 * Base class for radial components. The same as {@link AbstractLeadedComponent} but with added pin
 * spacing.
 * 
 * @author bancika
 * 
 * @param <T>
 */
public abstract class AbstractRadialComponent<T> extends AbstractLeadedComponent<T> {

  private static final long serialVersionUID = 1L;

  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);

  private Size pinSpacing = PIN_SPACING;

  @Override
  protected int calculatePinSpacing(Rectangle shapeRect) {
    return (int) getPinSpacing().convertToPixels();
  }

  @EditableProperty(name = "Lead Spacing")
  public Size getPinSpacing() {
    if (pinSpacing == null) {
      pinSpacing = PIN_SPACING;
    }
    return pinSpacing;
  }

  public void setPinSpacing(Size pinSpacing) {
    this.pinSpacing = pinSpacing;
  }
}
