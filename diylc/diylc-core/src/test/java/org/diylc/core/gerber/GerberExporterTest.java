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
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.diylc.common.PCBLayer;
import org.diylc.core.ComponentState;
import org.diylc.core.IBoardSideComponent;
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

  @Test
  public void testTopSideComponentKeepsItsLayer() {
    GerberG2DWrapper g2d = draw(new SidedGerberComponent(PCBLayer._2));

    assertEquals(EnumSet.of(GerberLayer.SilkscreenTop), EnumSet.copyOf(g2d.getLayerMap().keySet()));
  }

  @Test
  public void testBottomSideComponentGoesToTheOtherSide() {
    GerberG2DWrapper g2d = draw(new SidedGerberComponent(PCBLayer._1));

    assertEquals(EnumSet.of(GerberLayer.SilkscreenBot), EnumSet.copyOf(g2d.getLayerMap().keySet()));
  }

  @Test
  public void testComponentWithoutSideStaysOnTop() {
    GerberG2DWrapper g2d = draw(new SidedGerberComponent(null));

    assertEquals(EnumSet.of(GerberLayer.SilkscreenTop), EnumSet.copyOf(g2d.getLayerMap().keySet()));
  }

  @Test
  public void testTopSideTextReadsLeftToRight() {
    List<Rectangle2D> glyphs = drawText(PCBLayer._2);

    assertTrue(glyphs.get(0).getMinX() < glyphs.get(1).getMinX());
  }

  @Test
  public void testBottomSideTextIsMirrored() {
    List<Rectangle2D> top = drawText(PCBLayer._2);
    List<Rectangle2D> bottom = drawText(PCBLayer._1);

    // seen from the top, the first letter of a bottom-side label is on the right
    assertTrue(bottom.get(0).getMinX() > bottom.get(1).getMinX());
    // mirrored about the centre of its layout box, so the text stays where it was; the glyphs'
    // uneven side bearings move their drawn bounds by a pixel or so
    assertEquals(union(top).getCenterX(), union(bottom).getCenterX(), 2);
  }

  private static List<Rectangle2D> drawText(PCBLayer side) {
    List<Rectangle2D> glyphs = new ArrayList<Rectangle2D>();
    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = img.createGraphics();
    GerberG2DWrapper g2d =
        new GerberG2DWrapper(graphics, "test", new Rectangle2D.Double(0, 0, 100, 100)) {
          @Override
          public void fillShape(Shape s) {
            glyphs.add(s.getBounds2D());
          }
        };
    GerberExporter.drawComponent(new Project(), g2d, new TextGerberComponent(side), false);
    graphics.dispose();
    return glyphs;
  }

  private static Rectangle2D union(List<Rectangle2D> rects) {
    Rectangle2D union = (Rectangle2D) rects.get(0).clone();
    for (Rectangle2D rect : rects) {
      union.add(rect);
    }
    return union;
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

  public static class SidedGerberComponent extends SimpleGerberComponent
      implements IBoardSideComponent {

    private static final long serialVersionUID = 1L;

    private PCBLayer side;

    public SidedGerberComponent(PCBLayer side) {
      super(null);
      this.side = side;
    }

    @Override
    public PCBLayer getBoardSide() {
      return side;
    }
  }

  public static class TextGerberComponent extends SidedGerberComponent {

    private static final long serialVersionUID = 1L;

    public TextGerberComponent(PCBLayer side) {
      super(side);
    }

    @Override
    public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
        Project project, IDrawingObserver drawingObserver) {
      g2d.setColor(Color.black);
      g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
      g2d.drawString("AB", 10, 50);
    }
  }
}
