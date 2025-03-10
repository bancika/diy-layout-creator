package org.diylc.netlist;

import org.diylc.components.guitar.LPSwitch;
import org.junit.Test;

public class LPSwitchTests extends AbstractSwitchTests {
  
  @Test
  public void testLPSwitch() {
    LPSwitch lpSwitch = new LPSwitch();
    String[] validCombinations = new String[] {"0,1,2", "1,1,2", "1,2,3", "1,1,3", "2,2,3"};
    testSwitch(lpSwitch, validCombinations);
  }  
}
