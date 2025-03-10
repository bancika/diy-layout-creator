package org.diylc.netlist;

import org.junit.Test;

import org.diylc.components.guitar.S1Switch;

public class S1SwitchTests extends AbstractSwitchTests {
  
  @Test
  public void testS1Switch() {
    S1Switch s1Switch = new S1Switch();

    String[] validCombinations = new String[] {"0,1,2", "0,4,5", "0,7,8", "0,10,11", "1,2,3", "1,5,6", "1,8,9", "1,11,12"};
    
    testSwitch(s1Switch, validCombinations);
  }
}
