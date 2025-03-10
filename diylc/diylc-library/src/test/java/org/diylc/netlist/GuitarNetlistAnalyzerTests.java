/*
 *
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 *
 * This file is part of DIYLC.
 *
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package org.diylc.netlist;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;

import org.junit.Test;

import org.diylc.components.electromechanical.OpenJack1_4;
import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.passive.PotentiometerPanel;
import org.diylc.components.passive.RadialCeramicDiskCapacitor;
import org.diylc.netlist.Group;
import org.diylc.netlist.GuitarDiagramAnalyzer;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Tree;
import org.diylc.netlist.TreeException;

public class GuitarNetlistAnalyzerTests {

  @Test
  public void testOneSinglePickup() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    SingleCoilPickup pickup = new SingleCoilPickup();
    pickup.setName("Pickup");
    Netlist netlist = new Netlist(Arrays.asList(jack, pickup));
    Group tipGroup = new Group().connect(jack, 0).connect(pickup, 1);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup, 2);
    netlist.add(tipGroup).add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup.North<-))", s);
  }

  @Test
  public void testOneSinglePickupVolPot() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    SingleCoilPickup pickup = new SingleCoilPickup();
    pickup.setName("Pickup");
    PotentiometerPanel vol = new PotentiometerPanel();
    vol.setName("Volume");
    Netlist netlist = new Netlist(Arrays.asList(jack, pickup, vol));
    Group hotGroup = new Group().connect(pickup, 1).connect(vol, 0);
    Group tipGroup = new Group().connect(jack, 0).connect(vol, 1);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup, 2).connect(vol, 2);
    netlist.add(tipGroup).add(tipGroup).add(sleeveGroup).add(hotGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Volume.1-2 + Pickup.North<-) || (Volume.2-3))", s);
  }

  @Test
  public void testOneSinglePickupVolTonePot() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    SingleCoilPickup pickup = new SingleCoilPickup();
    pickup.setName("Pickup");
    PotentiometerPanel vol = new PotentiometerPanel();
    vol.setName("Volume");
    PotentiometerPanel tone = new PotentiometerPanel();
    tone.setName("Tone");
    RadialCeramicDiskCapacitor cap = new RadialCeramicDiskCapacitor();
    cap.setName("Cap");
    Netlist netlist = new Netlist(Arrays.asList(jack, pickup, vol, tone, cap));
    Group hotGroup = new Group().connect(pickup, 1).connect(vol, 0).connect(tone, 2);
    Group tipGroup = new Group().connect(jack, 0).connect(vol, 1);
    Group sleeveGroup =
        new Group().connect(jack, 1).connect(pickup, 2).connect(vol, 2).connect(cap, 0);
    Group toneGroup = new Group().connect(tone, 1).connect(cap, 1);
    netlist.add(tipGroup).add(tipGroup).add(sleeveGroup).add(hotGroup).add(toneGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("(((Volume.1-2) + ((Pickup.North<-) || (Tone.2-3 + Cap))) || (Volume.2-3))", s);
  }

  @Test
  public void testTwoSinglePickupsMasterVolSeparateTone() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    SingleCoilPickup pickup1 = new SingleCoilPickup();
    pickup1.setName("Pickup1");
    SingleCoilPickup pickup2 = new SingleCoilPickup();
    pickup2.setName("Pickup2");
    PotentiometerPanel vol = new PotentiometerPanel();
    vol.setName("Volume");
    PotentiometerPanel tone1 = new PotentiometerPanel();
    tone1.setName("Tone1");
    PotentiometerPanel tone2 = new PotentiometerPanel();
    tone2.setName("Tone2");
    RadialCeramicDiskCapacitor cap1 = new RadialCeramicDiskCapacitor();
    cap1.setName("Cap1");
    RadialCeramicDiskCapacitor cap2 = new RadialCeramicDiskCapacitor();
    cap2.setName("Cap2");
    Netlist netlist =
        new Netlist(Arrays.asList(jack, pickup1, pickup2, vol, tone1, tone2, cap1, cap2));
    Group hotGroup = new Group().connect(pickup1, 1).connect(vol, 0).connect(tone1, 2)
        .connect(pickup2, 1).connect(tone2, 2);
    Group tipGroup = new Group().connect(jack, 0).connect(vol, 1);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup1, 2).connect(pickup2, 2)
        .connect(vol, 2).connect(cap1, 0).connect(cap2, 0);
    Group tone1Group = new Group().connect(tone1, 1).connect(cap1, 1);
    Group tone2Group = new Group().connect(tone2, 1).connect(cap2, 1);
    netlist.add(tipGroup).add(tipGroup).add(sleeveGroup).add(hotGroup).add(tone1Group)
        .add(tone2Group);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals(
        "(((Volume.1-2) + ((Pickup1.North<-) || (Pickup2.North<-) || (Tone1.2-3 + Cap1) || (Tone2.2-3 + Cap2))) || (Volume.2-3))",
        s);
  }

  @Test
  public void testOneSinglePickupReverse() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    SingleCoilPickup pickup = new SingleCoilPickup();
    pickup.setName("Pickup");
    Netlist netlist = new Netlist(Arrays.asList(jack, pickup));
    Group tipGroup = new Group().connect(jack, 0).connect(pickup, 2);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup, 1);
    netlist.add(tipGroup).add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup.North->))", s);
  }

  @Test
  public void testOneHumbuckerSeries() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setName("Pickup");
    Netlist netlist = new Netlist(Arrays.asList(jack, pickup));
    Group tipGroup = new Group().connect(jack, 0).connect(pickup, 0);
    Group coilTapGroup = new Group().connect(pickup, 1).connect(pickup, 2);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup, 3);
    netlist.add(tipGroup).add(coilTapGroup).add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup.North<- + Pickup.South<-))", s);
  }

  @Test
  public void testOneHumbuckerSeriesReverse() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setName("Pickup");
    Netlist netlist = new Netlist(Arrays.asList(jack, pickup));
    Group tipGroup = new Group().connect(jack, 0).connect(pickup, 3);
    Group coilTapGroup = new Group().connect(pickup, 1).connect(pickup, 2);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup, 0);
    netlist.add(tipGroup).add(coilTapGroup).add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup.South-> + Pickup.North->))", s);
  }

  @Test
  public void testOneHumbuckerParallel() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setName("Pickup");
    Netlist netlist = new Netlist(Arrays.asList(jack, pickup));
    Group tipGroup = new Group().connect(jack, 0).connect(pickup, 0).connect(pickup, 2);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup, 1).connect(pickup, 3);
    netlist.add(tipGroup).add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup.North<-) || (Pickup.South<-))", s);
  }

  @Test
  public void testOneHumbuckerParallelOutOfPhase() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setName("Pickup");
    Netlist netlist = new Netlist(Arrays.asList(jack, pickup));
    Group tipGroup = new Group().connect(jack, 0).connect(pickup, 0).connect(pickup, 3);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup, 1).connect(pickup, 2);
    netlist.add(tipGroup).add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup.North<-) || (Pickup.South->))", s);
  }

  @Test
  public void testTwoHumbuckersSeries() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    HumbuckerPickup pickup1 = new HumbuckerPickup();
    pickup1.setName("Pickup1");
    HumbuckerPickup pickup2 = new HumbuckerPickup();
    pickup2.setName("Pickup2");
    Netlist netlist = new Netlist(Arrays.asList(jack, pickup1, pickup2));
    Group tipGroup = new Group().connect(jack, 0).connect(pickup1, 0);
    Group coilTapGroup1 = new Group().connect(pickup1, 1).connect(pickup1, 2);
    Group coilTapGroup2 = new Group().connect(pickup2, 1).connect(pickup2, 2);
    Group joinGroup = new Group().connect(pickup1, 3).connect(pickup2, 0);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup2, 3);
    netlist.add(tipGroup).add(coilTapGroup1).add(coilTapGroup2).add(joinGroup).add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup1.North<- + Pickup1.South<- + Pickup2.North<- + Pickup2.South<-))", s);
  }

  @Test
  public void testTwoHumbuckersParallel() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    HumbuckerPickup pickup1 = new HumbuckerPickup();
    pickup1.setName("Pickup1");
    HumbuckerPickup pickup2 = new HumbuckerPickup();
    pickup2.setName("Pickup2");
    Netlist netlist = new Netlist(Arrays.asList(jack, pickup1, pickup2));
    Group tipGroup = new Group().connect(jack, 0).connect(pickup1, 0).connect(pickup2, 0);
    Group coilTapGroup1 = new Group().connect(pickup1, 1).connect(pickup1, 2);
    Group coilTapGroup2 = new Group().connect(pickup2, 1).connect(pickup2, 2);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup1, 3).connect(pickup2, 3);
    netlist.add(tipGroup).add(coilTapGroup1).add(coilTapGroup2).add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup1.North<- + Pickup1.South<-) || (Pickup2.North<- + Pickup2.South<-))", s);
  }

  @Test
  public void testTwoHumbuckersParallelOutOfPhase() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    HumbuckerPickup pickup1 = new HumbuckerPickup();
    pickup1.setName("Pickup1");
    HumbuckerPickup pickup2 = new HumbuckerPickup();
    pickup2.setName("Pickup2");
    Netlist netlist = new Netlist(Arrays.asList(jack, pickup1, pickup2));
    Group tipGroup = new Group().connect(jack, 0).connect(pickup1, 0).connect(pickup2, 3);
    Group coilTapGroup1 = new Group().connect(pickup1, 1).connect(pickup1, 2);
    Group coilTapGroup2 = new Group().connect(pickup2, 1).connect(pickup2, 2);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup1, 3).connect(pickup2, 0);
    netlist.add(tipGroup).add(coilTapGroup1).add(coilTapGroup2).add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup1.North<- + Pickup1.South<-) || (Pickup2.South-> + Pickup2.North->))", s);
  }

  @Test
  public void testTwoHumbuckersMasterVolSeparateTone() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    HumbuckerPickup pickup1 = new HumbuckerPickup();
    pickup1.setName("Pickup1");
    HumbuckerPickup pickup2 = new HumbuckerPickup();
    pickup2.setName("Pickup2");
    PotentiometerPanel vol = new PotentiometerPanel();
    vol.setName("Volume");
    PotentiometerPanel tone1 = new PotentiometerPanel();
    tone1.setName("Tone1");
    PotentiometerPanel tone2 = new PotentiometerPanel();
    tone2.setName("Tone2");
    RadialCeramicDiskCapacitor cap1 = new RadialCeramicDiskCapacitor();
    cap1.setName("Cap1");
    RadialCeramicDiskCapacitor cap2 = new RadialCeramicDiskCapacitor();
    cap2.setName("Cap2");
    Netlist netlist =
        new Netlist(Arrays.asList(jack, pickup1, pickup2, vol, tone1, tone2, cap1, cap2));
    Group hotGroup = new Group().connect(pickup1, 0).connect(vol, 0).connect(tone1, 2)
        .connect(pickup2, 0).connect(tone2, 2);
    Group tipGroup = new Group().connect(jack, 0).connect(vol, 1);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup1, 3).connect(pickup2, 3)
        .connect(vol, 2).connect(cap1, 0).connect(cap2, 0);
    Group tone1Group = new Group().connect(tone1, 1).connect(cap1, 1);
    Group tone2Group = new Group().connect(tone2, 1).connect(cap2, 1);
    Group coilTap1 = new Group().connect(pickup1, 1).connect(pickup1, 2);
    Group coilTap2 = new Group().connect(pickup2, 1).connect(pickup2, 2);
    netlist.add(tipGroup).add(tipGroup).add(sleeveGroup).add(hotGroup).add(tone1Group)
        .add(tone2Group).add(coilTap1).add(coilTap2);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals(
        "(((Volume.1-2) + ((Pickup1.North<- + Pickup1.South<-) || (Pickup2.North<- + Pickup2.South<-) || (Tone1.2-3 + Cap1) || (Tone2.2-3 + Cap2))) || (Volume.2-3))",
        s);
  }
}
