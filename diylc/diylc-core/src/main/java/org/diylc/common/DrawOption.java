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

import java.awt.Graphics;

/**
 * Enumerates all options that can be used when drawing a project.
 * 
 * @see IPlugInPort#draw(java.awt.Graphics2D, java.util.Set, IComponentFiler)
 * 
 * @author Branislav Stojkovic
 */
public enum DrawOption {

  /**
   * Selection rectangle will be drawn when needed and selected components may be rendered
   * differently.
   */
  SELECTION,
  /**
   * Selected zoom level will be applied to scale the {@link Graphics} before drawing.
   */
  ZOOM,
  /**
   * Grid lines are drawn.
   */
  GRID,
  /**
   * Control points are drawn.
   */
  CONTROL_POINTS,
  /**
   * Anti-aliasing is used when drawing.
   */
  ANTIALIASING,
  /**
   * Draw components in outline mode.
   */
  OUTLINE_MODE,
  /**
   * Include extra space around the layout.
   */
  EXTRA_SPACE,
  /**
   * Whether components should be cached or not.
   */
  ENABLE_CACHING, 
  /**
   * Whether locked components should be rendered as transparent or not.
   */
  LOCKED_ALPHA;
}
