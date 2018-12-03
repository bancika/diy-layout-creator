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

import org.diylc.core.annotations.EditableProperty;

public abstract class AbstractTransparentComponent<T> extends AbstractComponent<T> {

  private static final long serialVersionUID = 1L;

  public static byte MAX_ALPHA = 127;

  protected byte alpha = MAX_ALPHA;

  @EditableProperty
  public Byte getAlpha() {
    return alpha;
  }

  public void setAlpha(Byte alpha) {
    this.alpha = alpha;
  }
}
