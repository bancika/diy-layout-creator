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
package org.diylc.common;

import org.diylc.core.gerber.GerberLayer;

public enum PCBLayer {

  _1("Bottom"), _2("Top"), _3("Inner 1"), _4("Inner 2"), _5("Inner 3"), _6("Inner 4"), _7("Inner 5"), _8("Inner 6");
  
  private String label;
  
  private PCBLayer(String label) {
    this.label = label;
  }

  public String toString() {
    return label;
  };
  
  public int getId() {
    return this.ordinal() + 1;
  }
  
  public GerberLayer toGerberCopperLayer() {
    switch (this) {
      case _1: return GerberLayer.CopperBot;
      case _2: return GerberLayer.CopperTop;
      default:
        throw new RuntimeException("Unsupported PCB layer: " + this);
    }
  }
}
