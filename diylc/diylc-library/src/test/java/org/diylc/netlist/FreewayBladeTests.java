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
        "0,3,1",
        "1,6,3", "1,3,1", "1,6,1", "1,4,0",
        "2,6,3", "2,4,0",
        "3,5,3", "3,1,5", "3,3,1", "3,0,6",
        "4,5,3", "4,3,2", "4,5,2", "4,6,1",
        "5,6,3", "5,3,2", "5,6,2", "5,4,1",
    };

    testSwitch(sw, validCombinations);
  }

  @Test
  public void testBlade5B5_01() {
    FreewayBlade sw = new FreewayBlade();

    sw.setType(FreewayBladeType.B5_01);
    String[] validCombinations = new String[] {
        "0,7,0", "0,6,1",
        "1,7,3", "1,7,0", "1,3,0", "1,6,1",
        "2,7,3",
        "3,8,7", "3,8,3", "3,7,3", "3,9,6",
        "4,8,7", "4,9,6",
        "5,9,4", "5,5,2", "5,7,0",
        "6,9,6", "6,9,1", "6,6,1", "6,8,7", "6,8,3", "6,8,0", "6,7,3", "6,7,0", "6,3,0",
        "7,0,9", "7,8,7", "7,6,1",
        "8,9,6", "8,9,1", "8,6,1", "8,8,7", "8,8,0", "8,7,0",
        "9,9,3", "9,8,7", "9,6,1",
    };

    testSwitch(sw, validCombinations);
  }

  @Test
  public void testBlade5B5_02() {
    FreewayBlade sw = new FreewayBlade();

    sw.setType(FreewayBladeType.B5_02);
    String[] validCombinations = new String[] {
        "0,6,0",
        "1,6,1", "1,6,0", "1,1,0",
        "2,10,6", "2,10,2", "2,10,0", "2,6,2", "2,6,0", "2,2,0", "2,9,8", "2,9,4", "2,8,4",
        "3,10,6", "3,10,3", "3,6,3",
        "4,10,6",
        "5,9,5", "5,6,0",
        "6,9,5", "6,7,6", "6,7,0", "6,6,0",
        "7,7,6",
        "8,10,7", "8,10,6", "8,7,6", "8,8,5",
        "9,10,6", "9,8,5",
    };

    testSwitch(sw, validCombinations);
  }
}
