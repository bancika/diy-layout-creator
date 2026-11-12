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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.Set;

import com.bancika.gerberwriter.DrillFormat;

import org.diylc.common.PCBLayer;
import org.diylc.components.electromechanical.DrillHole;
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
    Set<GerberLayer> layers = layersOf(drawGerber(via));
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

  @Test
  public void testPadWithHoleIsComponentPad() {
    SolderPad pad = new SolderPad();
    pad.setHoleType(SolderPad.HoleType.NTPH);
    String gerber = drawGerber(pad).getLayerMap().get(GerberLayer.CopperBot).dumpGerberToString();
    assertTrue(gerber.contains("TA.AperFunction,ComponentPad*"));
    assertFalse(gerber.contains("ConnectorPad"));
  }

  @Test
  public void testPadWithoutHoleIsSmdPad() {
    SolderPad pad = new SolderPad();
    pad.setHoleType(SolderPad.HoleType.NONE);
    String gerber = drawGerber(pad).getLayerMap().get(GerberLayer.CopperBot).dumpGerberToString();
    assertTrue(gerber.contains("TA.AperFunction,SMDPad,CuDef*"));
  }

  @Test
  public void testViaIsDrilledOnceAsViaDrill() {
    String excellon = drawGerber(new Via()).getDrillLayerMap().get(GerberLayer.DrillPlated)
        .dumpToString(DrillFormat.EXCELLON);
    assertTrue(excellon.contains("; #@! TA.AperFunction,Plated,PTH,ViaDrill\nT1C0.4\n"));
    assertEquals(1, countHoles(excellon));
  }

  @Test
  public void testDrillHoleMarkerIsDrilledOnce() {
    // the marker's border and crosshair are drawn too, but they are not holes
    String excellon = drawGerber(new DrillHole()).getDrillLayerMap()
        .get(GerberLayer.DrillNonPlated).dumpToString(DrillFormat.EXCELLON);
    assertTrue(excellon.contains("T1C3.0\n"));
    assertEquals(1, countHoles(excellon));
  }

  private static long countHoles(String excellon) {
    return excellon.lines().filter(line -> line.startsWith("X")).count();
  }

  // a cut-out is drawn with clear polarity, %LPC*%
  private static void assertNoCutOut(GerberG2DWrapper g2d) {
    for (GerberLayer copperLayer : new GerberLayer[] {GerberLayer.CopperTop,
        GerberLayer.CopperBot}) {
      String gerber = g2d.getLayerMap().get(copperLayer).dumpGerberToString();
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
    return layersOf(drawGerber(pad));
  }

  private static Set<GerberLayer> layersOf(GerberG2DWrapper g2d) {
    Set<GerberLayer> layers = EnumSet.noneOf(GerberLayer.class);
    layers.addAll(g2d.getLayerMap().keySet());
    layers.addAll(g2d.getDrillLayerMap().keySet());
    return layers;
  }

  private static GerberG2DWrapper drawGerber(IGerberComponentCustom component) {
    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = img.createGraphics();
    GerberG2DWrapper g2d =
        new GerberG2DWrapper(graphics, "test", new Rectangle2D.Double(-50, -50, 100, 100));
    g2d.startedDrawingComponent();
    component.draw(g2d, ComponentState.NORMAL, false, new Project(), g2d, g2d);
    graphics.dispose();
    return g2d;
  }
}
