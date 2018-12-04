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
package org.diylc.swing.plugins.file;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.EnumSet;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.DrawOption;
import org.diylc.common.IPlugInPort;
import org.diylc.swingframework.IDrawingProvider;

/**
 * {@link IDrawingProvider} implementation that uses {@link IPlugInPort} to draw a project onto the
 * canvas.
 * 
 * @author Branislav Stojkovic
 */
public class ProjectDrawingProvider implements IDrawingProvider {

  private IPlugInPort plugInPort;
  private boolean useZoom;
  private boolean showGridWhenNeeded;

  public ProjectDrawingProvider(IPlugInPort plugInPort, boolean useZoom, boolean showGridWhenNeeded) {
    super();
    this.plugInPort = plugInPort;
    this.useZoom = useZoom;
    this.showGridWhenNeeded = showGridWhenNeeded;
  }

  @Override
  public Dimension getSize() {
    return plugInPort.getCanvasDimensions(useZoom);
  }

  @Override
  public void draw(int page, Graphics g) {
    EnumSet<DrawOption> drawOptions = EnumSet.of(DrawOption.ANTIALIASING);
    if (useZoom) {
      drawOptions.add(DrawOption.ZOOM);
    }
    if (showGridWhenNeeded && ConfigurationManager.getInstance().readBoolean(IPlugInPort.EXPORT_GRID_KEY, false)) {
      drawOptions.add(DrawOption.GRID);
    }
    if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.OUTLINE_KEY, false)) {
      drawOptions.add(DrawOption.OUTLINE_MODE);
    }
    plugInPort.draw((Graphics2D) g, drawOptions, null);
  }

  @Override
  public int getPageCount() {
    return 1;
  }
}
