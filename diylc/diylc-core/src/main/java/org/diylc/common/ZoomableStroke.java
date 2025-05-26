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
package org.diylc.common;

import java.awt.BasicStroke;

/***
 * {@link BasicStroke} that will always be zoomed in when rendered.
 * 
 * @author Branislav Stojkovic
 */
public class ZoomableStroke extends BasicStroke {

  public ZoomableStroke() {
    // TODO Auto-generated constructor stub
  }

  public ZoomableStroke(float width) {
    super(width);
    // TODO Auto-generated constructor stub
  }

  public ZoomableStroke(float width, int cap, int join) {
    super(width, cap, join);
    // TODO Auto-generated constructor stub
  }

  public ZoomableStroke(float width, int cap, int join, float miterlimit) {
    super(width, cap, join, miterlimit);
    // TODO Auto-generated constructor stub
  }

  public ZoomableStroke(float width, int cap, int join, float miterlimit, float[] dash, float dash_phase) {
    super(width, cap, join, miterlimit, dash, dash_phase);
    // TODO Auto-generated constructor stub
  }

}
