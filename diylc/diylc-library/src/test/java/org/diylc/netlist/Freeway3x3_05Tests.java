package org.diylc.netlist;

import org.diylc.components.guitar.Freeway3x3_05;
import org.junit.Test;

public class Freeway3x3_05Tests extends AbstractSwitchTests {

  @Test
  public void testFreeway3x3_05() {
    Freeway3x3_05 sw = new Freeway3x3_05();

    String[] validCombinations = new String[]{
            "0,0,6", "0,6,11", "0,0,11", "0,17,21", "0,21,22", "0,17,22",
            "1,1,6", "1,6,12", "1,1,12", "1,18,21", "1,21,23", "1,18,23",
            "2,2,6", "2,6,13", "2,2,13", "2,19,21", "2,21,24", "2,19,24",
            "3,5,6", "3,6,10", "3,5,10", "3,16,21", "3,21,27", "3,16,27",
            "4,4,6", "4,6,9", "4,4,9", "4,15,21", "4,21,26", "4,15,26",
            "5,3,6", "5,6,8", "5,3,8", "5,14,21", "5,21,25", "5,14,25",
    };

    testSwitch(sw, validCombinations);
  }
}
