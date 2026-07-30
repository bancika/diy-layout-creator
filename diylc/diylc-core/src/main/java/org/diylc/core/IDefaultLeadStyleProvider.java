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
package org.diylc.core;

import java.awt.Color;

/**
 * Implemented by components that can suggest a preferred colour for a lead wire generated at one
 * of their control points (e.g. by the "Add Flexible Leads" action), based on metadata the
 * component itself owns (such as an applied pickup library definition). This keeps callers like
 * the flexible-leads editor free of any component-specific knowledge: they only need to check for
 * this interface and fall back to their own default when it is absent or returns {@code null}.
 */
public interface IDefaultLeadStyleProvider {

  /**
   * @param controlPointIndex index of the control point a new lead is being generated from.
   * @return the preferred colour for that lead, or {@code null} if the component has no
   *         preference for this point (callers should keep their own current default in that
   *         case).
   */
  Color getDefaultLeadColor(int controlPointIndex);
}
