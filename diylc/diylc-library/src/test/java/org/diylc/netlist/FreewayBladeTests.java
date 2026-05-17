package org.diylc.netlist;

import org.junit.Test;

import org.diylc.components.guitar.FreewayBlade;
import org.diylc.components.guitar.FreewayBlade.FreewayBladeType;

public class FreewayBladeTests extends AbstractSwitchTests {

  @Test
  public void testBlade3B3_01() {
    FreewayBlade sw = new FreewayBlade();

    sw.setType(FreewayBladeType.B3_01);
    String[] validCombinations = new String[] {
        "0,3,5",
        "1,0,3", "1,3,5", "1,0,5", "1,2,6",
        "2,0,3", "2,2,6",
        "3,1,3", "3,1,5", "3,3,5", "3,0,6",
        "4,1,3", "4,3,4", "4,1,4", "4,0,5",
        "5,0,3", "5,3,4", "5,0,4", "5,2,5",
    };

    testSwitch(sw, validCombinations);
  }

  @Test
  public void testBlade5B5_01() {
    FreewayBlade sw = new FreewayBlade();

    sw.setType(FreewayBladeType.B5_01);
    String[] validCombinations = new String[] {
        "0,2,9", "0,3,8",
        "1,2,6", "1,2,9", "1,6,9", "1,3,8",
        "2,2,6",
        "3,1,2", "3,1,6", "3,2,6", "3,0,3",
        "4,1,2", "4,0,3",
        "5,0,5", "5,4,7", "5,2,9",
        "6,0,3", "6,0,8", "6,3,8", "6,1,2", "6,1,6", "6,1,9", "6,2,6", "6,2,9", "6,6,9",
        "7,0,9", "7,1,2", "7,3,8",
        "8,0,3", "8,0,8", "8,3,8", "8,1,2", "8,1,9", "8,2,9",
        "9,0,6", "9,1,2", "9,3,8",
    };

    testSwitch(sw, validCombinations);
  }

  @Test
  public void testBlade5B5_02() {
    FreewayBlade sw = new FreewayBlade();

    sw.setType(FreewayBladeType.B5_02);
    String[] validCombinations = new String[] {
        "0,4,10",
        "1,4,9", "1,4,10", "1,9,10",
        "2,0,4", "2,0,8", "2,0,10", "2,4,8", "2,4,10", "2,8,10", "2,1,2", "2,1,6", "2,2,6",
        "3,0,4", "3,0,7", "3,4,7",
        "4,0,4",
        "5,1,5", "5,4,10",
        "6,1,5", "6,3,4", "6,3,10", "6,4,10",
        "7,3,4",
        "8,0,3", "8,0,4", "8,3,4", "8,2,5",
        "9,0,4", "9,2,5",
    };

    testSwitch(sw, validCombinations);
  }
}
