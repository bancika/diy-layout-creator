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
package org.diylc.core.gerber;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.EnumSet;

import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.testcomponents.MockDIYComponent;
import org.junit.Test;

public class GerberExporterTest {

  @Test
  public void testSimpleComponentGoesToSilkscreenTopByDefault() {
    GerberG2DWrapper g2d = draw(new SimpleGerberComponent(null));

    assertEquals(EnumSet.of(GerberLayer.SilkscreenTop), EnumSet.copyOf(g2d.getLayerMap().keySet()));
  }

  @Test
  public void testSimpleComponentGoesToItsOwnLayer() {
    GerberG2DWrapper g2d = draw(new SimpleGerberComponent(GerberLayer.CopperBot));

    assertEquals(EnumSet.of(GerberLayer.CopperBot), EnumSet.copyOf(g2d.getLayerMap().keySet()));
  }

  private static GerberG2DWrapper draw(SimpleGerberComponent component) {
    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = img.createGraphics();
    GerberG2DWrapper g2d =
        new GerberG2DWrapper(graphics, "test", new Rectangle2D.Double(0, 0, 100, 100));
    GerberExporter.drawComponent(new Project(), g2d, component, false);
    graphics.dispose();
    return g2d;
  }

  public static class SimpleGerberComponent extends MockDIYComponent
      implements IGerberComponentSimple {

    private static final long serialVersionUID = 1L;

    // null uses the interface default
    private GerberLayer layer;

    public SimpleGerberComponent(GerberLayer layer) {
      this.layer = layer;
    }

    @Override
    public GerberLayer getGerberLayer() {
      return layer == null ? IGerberComponentSimple.super.getGerberLayer() : layer;
    }

    @Override
    public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
        Project project, IDrawingObserver drawingObserver) {
      g2d.setColor(Color.black);
      g2d.fill(new Rectangle2D.Double(10, 10, 20, 20));
    }
  }
}
