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
package org.diylc.netlist;

import static org.junit.Assert.assertEquals;

import org.diylc.components.electromechanical.OpenJack1_4;
import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.junit.Test;

public class NetlistAnalyzerTests {

  @Test
  public void testOneSinglePickup() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    SingleCoilPickup pickup = new SingleCoilPickup();
    pickup.setName("Pickup");
    Netlist netlist = new Netlist();
    Group tipGroup = new Group().connect(jack, 0).connect(pickup, 1);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup, 2);
    netlist.getGroups().add(tipGroup);
    netlist.getGroups().add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup.North<-))", s);
  }
  
  @Test
  public void testOneSinglePickupReverse() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    SingleCoilPickup pickup = new SingleCoilPickup();
    pickup.setName("Pickup");
    Netlist netlist = new Netlist();
    Group tipGroup = new Group().connect(jack, 0).connect(pickup, 2);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup, 1);
    netlist.getGroups().add(tipGroup);
    netlist.getGroups().add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup.North->))", s);
  }
  
  @Test
  public void testOneHumbuckerSeries() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setName("Pickup");
    Netlist netlist = new Netlist();
    Group tipGroup = new Group().connect(jack, 0).connect(pickup, 0);
    Group coilTapGroup = new Group().connect(pickup, 1).connect(pickup, 2);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup, 3);
    netlist.getGroups().add(tipGroup);
    netlist.getGroups().add(coilTapGroup);
    netlist.getGroups().add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup.North<- + Pickup.South<-))", s);
  }
  
  @Test
  public void testOneHumbuckerSeriesReverse() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setName("Pickup");
    Netlist netlist = new Netlist();
    Group tipGroup = new Group().connect(jack, 0).connect(pickup, 3);
    Group coilTapGroup = new Group().connect(pickup, 1).connect(pickup, 2);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup, 0);
    netlist.getGroups().add(tipGroup);
    netlist.getGroups().add(coilTapGroup);
    netlist.getGroups().add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup.South-> + Pickup.North->))", s);
  }
  
  @Test
  public void testOneHumbuckerParallel() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setName("Pickup");
    Netlist netlist = new Netlist();
    Group tipGroup = new Group().connect(jack, 0).connect(pickup, 0).connect(pickup, 2);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup, 1).connect(pickup, 3);
    netlist.getGroups().add(tipGroup);    
    netlist.getGroups().add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup.South<-) || (Pickup.North<-))", s);
  }
  
  @Test
  public void testOneHumbuckerParallelOutOfPhase() throws TreeException {
    OpenJack1_4 jack = new OpenJack1_4();
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setName("Pickup");
    Netlist netlist = new Netlist();
    Group tipGroup = new Group().connect(jack, 0).connect(pickup, 0).connect(pickup, 3);
    Group sleeveGroup = new Group().connect(jack, 1).connect(pickup, 1).connect(pickup, 2);
    netlist.getGroups().add(tipGroup);    
    netlist.getGroups().add(sleeveGroup);
    Tree tree = new GuitarDiagramAnalyzer().constructTree(netlist);
    String s = tree.toString();
    assertEquals("((Pickup.North<-) || (Pickup.South->))", s);
  }
}
