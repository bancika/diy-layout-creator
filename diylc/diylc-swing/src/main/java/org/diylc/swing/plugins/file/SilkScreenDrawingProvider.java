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
package org.diylc.swing.plugins.file;

import org.diylc.common.DrawOption;
import org.diylc.common.IPlugInPort;
import org.diylc.filter.PCBLayerFilter;
import org.diylc.filter.SilkScreenFilter;
import org.diylc.swingframework.IDrawingProvider;

import java.awt.*;
import java.util.*;

/**
 * {@link IDrawingProvider} implementation that uses {@link IPlugInPort} to draw a project onto the
 * canvas.
 * 
 * @author Branislav Stojkovic
 */
public class SilkScreenDrawingProvider implements IDrawingProvider {

  private IPlugInPort plugInPort;

  public SilkScreenDrawingProvider(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
  }

  @Override
  public Dimension getSize() {
    return plugInPort.getCanvasDimensions(false, false);
  }

  @Override
  public void draw(int page, Graphics g, double zoomFactor) {
    plugInPort.draw((Graphics2D) g, EnumSet.of(DrawOption.ANTIALIASING, DrawOption.OUTLINE_MODE),
        new SilkScreenFilter(), zoomFactor, null, null);
  }

  @Override
  public int getPageCount() {
    return 1;
  }
}
