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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.diylc.common.DrawOption;
import org.diylc.common.IPlugInPort;
import org.diylc.common.PCBLayer;
import org.diylc.core.IDIYComponent;
import org.diylc.presenter.PCBLayerFilter;
import org.diylc.swingframework.IDrawingProvider;

/**
 * {@link IDrawingProvider} implementation that uses {@link IPlugInPort} to draw a project onto the
 * canvas.
 * 
 * @author Branislav Stojkovic
 */
public class TraceMaskDrawingProvider implements IDrawingProvider {

  private IPlugInPort plugInPort;

  public TraceMaskDrawingProvider(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
  }

  @Override
  public Dimension getSize() {
    return plugInPort.getCanvasDimensions(false, false);
  }

  @Override
  public void draw(int page, Graphics g, double zoomFactor) {
    plugInPort.draw((Graphics2D) g, EnumSet.of(DrawOption.ANTIALIASING), new PCBLayerFilter(getUsedLayers()[page]), zoomFactor, null);
  }

  @Override
  public int getPageCount() {
    return getUsedLayers().length;
  }

  private PCBLayer[] getUsedLayers() {
    Set<PCBLayer> layers = EnumSet.noneOf(PCBLayer.class);
    for (IDIYComponent<?> c : plugInPort.getCurrentProject().getComponents()) {
      Class<?> clazz = c.getClass();
      try {
        Method m = clazz.getMethod("getLayer");
        PCBLayer l = (PCBLayer) m.invoke(c);
        layers.add(l);
      } catch (Exception e) {
      }
    }
    List<PCBLayer> sorted = new ArrayList<PCBLayer>(layers);
    Collections.sort(sorted);
    return sorted.toArray(new PCBLayer[] {});
  }
}
