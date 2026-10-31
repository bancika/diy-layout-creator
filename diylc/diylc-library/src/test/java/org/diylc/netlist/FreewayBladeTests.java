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
        "0,1,3",
        "1,3,6", "1,1,3", "1,1,6", "1,0,4",
        "2,3,6", "2,0,4",
        "3,3,5", "3,1,5", "3,1,3", "3,0,6",
        "4,3,5", "4,2,3", "4,2,5", "4,1,6",
        "5,3,6", "5,2,3", "5,2,6", "5,1,4",
    };

    testSwitch(sw, validCombinations);
  }

  @Test
  public void testBlade5B5_01() {
    FreewayBlade sw = new FreewayBlade();

    sw.setType(FreewayBladeType.B5_01);
    String[] validCombinations = new String[] {
        "0,0,7", "0,1,6",
        "1,3,7", "1,0,7", "1,0,3", "1,1,6",
        "2,3,7",
        "3,7,8", "3,3,8", "3,3,7", "3,6,9",
        "4,7,8", "4,6,9",
        "5,4,9", "5,2,5", "5,0,7",
        "6,6,9", "6,1,9", "6,1,6", "6,7,8", "6,3,8", "6,0,8", "6,3,7", "6,0,7", "6,0,3",
        "7,0,9", "7,7,8", "7,1,6",
        "8,6,9", "8,1,9", "8,1,6", "8,7,8", "8,0,8", "8,0,7",
        "9,3,9", "9,7,8", "9,1,6",
    };

    testSwitch(sw, validCombinations);
  }

  @Test
  public void testBlade5B5_02() {
    FreewayBlade sw = new FreewayBlade();

    sw.setType(FreewayBladeType.B5_02);
    String[] validCombinations = new String[] {
        "0,0,6",
        "1,1,6", "1,0,6", "1,0,1",
        "2,6,10", "2,2,10", "2,0,10", "2,2,6", "2,0,6", "2,0,2", "2,8,9", "2,4,9", "2,4,8",
        "3,6,10", "3,3,10", "3,3,6",
        "4,6,10",
        "5,5,9", "5,0,6",
        "6,5,9", "6,6,7", "6,0,7", "6,0,6",
        "7,6,7",
        "8,7,10", "8,6,10", "8,6,7", "8,5,8",
        "9,6,10", "9,5,8",
    };

    testSwitch(sw, validCombinations);
  }
}
