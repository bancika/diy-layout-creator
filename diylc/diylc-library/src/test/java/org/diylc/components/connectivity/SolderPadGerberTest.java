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
package org.diylc.components.connectivity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;

import com.bancika.gerberwriter.DataLayer;

import org.diylc.common.PCBLayer;
import org.diylc.core.ComponentState;
import org.diylc.core.Project;
import org.diylc.core.gerber.GerberG2DWrapper;
import org.diylc.core.gerber.GerberLayer;
import org.diylc.core.gerber.IGerberComponentCustom;
import org.junit.Test;

public class SolderPadGerberTest {

  @Test
  public void testTopPadOpensTopMask() {
    Set<GerberLayer> layers = drawGerber(PCBLayer._2);
    assertTrue(layers.contains(GerberLayer.CopperTop));
    assertTrue(layers.contains(GerberLayer.SolderMaskTop));
    assertTrue(!layers.contains(GerberLayer.SolderMaskBot));
  }

  @Test
  public void testBottomPadOpensBottomMask() {
    Set<GerberLayer> layers = drawGerber(PCBLayer._1);
    assertTrue(layers.contains(GerberLayer.CopperBot));
    assertTrue(layers.contains(GerberLayer.SolderMaskBot));
    assertTrue(!layers.contains(GerberLayer.SolderMaskTop));
  }

  @Test
  public void testPlatedPadIsOnBothLayers() {
    Set<GerberLayer> layers = drawGerber(PCBLayer._1, SolderPad.HoleType.PTH);
    assertTrue(layers.contains(GerberLayer.CopperTop));
    assertTrue(layers.contains(GerberLayer.CopperBot));
    assertTrue(layers.contains(GerberLayer.SolderMaskTop));
    assertTrue(layers.contains(GerberLayer.SolderMaskBot));
    assertTrue(layers.contains(GerberLayer.DrillPlated));
  }

  @Test
  public void testNonPlatedPadIsOnItsLayer() {
    Set<GerberLayer> layers = drawGerber(PCBLayer._1, SolderPad.HoleType.NTPH);
    assertTrue(layers.contains(GerberLayer.CopperBot));
    assertTrue(!layers.contains(GerberLayer.CopperTop));
    assertTrue(layers.contains(GerberLayer.DrillNonPlated));
  }

  @Test
  public void testViaIsOnBothLayersAndTented() {
    Via via = new Via();
    Set<GerberLayer> layers = drawGerber(via).keySet();
    assertTrue(layers.contains(GerberLayer.CopperTop));
    assertTrue(layers.contains(GerberLayer.CopperBot));
    assertTrue(layers.contains(GerberLayer.DrillPlated));
    assertTrue(!layers.contains(GerberLayer.SolderMaskTop));
    assertTrue(!layers.contains(GerberLayer.SolderMaskBot));
  }

  @Test
  public void testHolesAreNotCutOutOfCopper() {
    SolderPad pad = new SolderPad();
    pad.setHoleType(SolderPad.HoleType.PTH);
    assertNoCutOut(drawGerber(pad));
    assertNoCutOut(drawGerber(new Via()));
  }

  // a cut-out is drawn with clear polarity, %LPC*%
  private static void assertNoCutOut(Map<GerberLayer, DataLayer> layers) {
    for (GerberLayer copperLayer : new GerberLayer[] {GerberLayer.CopperTop,
        GerberLayer.CopperBot}) {
      String gerber = layers.get(copperLayer).dumpGerberToString();
      assertTrue(gerber.contains("D03*"));
      assertFalse(gerber.contains("%LPC*%"));
    }
  }

  private static Set<GerberLayer> drawGerber(PCBLayer layer) {
    return drawGerber(layer, SolderPad.HoleType.NTPH);
  }

  private static Set<GerberLayer> drawGerber(PCBLayer layer, SolderPad.HoleType holeType) {
    SolderPad pad = new SolderPad();
    pad.setLayer(layer);
    pad.setHoleType(holeType);
    return drawGerber(pad).keySet();
  }

  private static Map<GerberLayer, DataLayer> drawGerber(IGerberComponentCustom component) {
    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = img.createGraphics();
    GerberG2DWrapper g2d =
        new GerberG2DWrapper(graphics, "test", new Rectangle2D.Double(-50, -50, 100, 100));
    g2d.startedDrawingComponent();
    component.draw(g2d, ComponentState.NORMAL, false, new Project(), g2d, g2d);
    graphics.dispose();
    return g2d.getLayerMap();
  }
}
