package org.diylc.netlist;

import org.diylc.components.guitar.LeverSwitch;
import org.diylc.components.guitar.LeverSwitch.LeverSwitchType;
import org.junit.Test;

public class LeverSwitchTests extends AbstractSwitchTests {

  @Test
  public void testLeverSwitchDP5T() {
    LeverSwitch leverSwitch = new LeverSwitch();

    leverSwitch.setValue(LeverSwitchType.DP5T);
    String[] validCombinations = new String[] {"0,0,1", "0,6,11", "1,0,2", "1,7,11", "2,0,3",
        "2,8,11", "3,0,4", "3,9,11", "4,0,5", "4,10,11"};

    testSwitch(leverSwitch, validCombinations);
  }

  @Test
  public void testLeverSwitch4P5T() {
    LeverSwitch leverSwitch = new LeverSwitch();

    String[] validCombinations = new String[] {"0,0,1", "0,6,11", "0,12,13", "0,18,23", "1,0,2",
        "1,7,11", "1,12,14", "1,19,23", "2,0,3", "2,8,11", "2,12,15", "2,20,23", "3,0,4", "3,9,11",
        "3,12,16", "3,21,23", "4,0,5", "4,10,11", "4,12,17", "4,22,23"};
    leverSwitch.setValue(LeverSwitchType._4P5T);
    testSwitch(leverSwitch, validCombinations);
  }
  
  @Test
  public void testLeverSwitchImport() {
    LeverSwitch leverSwitch = new LeverSwitch();

    String[] validCombinations = new String[] {"0,0,3", "0,4,5", "1,0,3", "1,4,5", "1,1,3",
        "1,4,6", "2,1,3", "2,4,6", "3,1,3", "3,2,3", "3,4,6", "3,4,7", "4,2,3", "4,4,7"};
    
    leverSwitch.setValue(LeverSwitchType.DP3T_5pos_Import);
    testSwitch(leverSwitch, validCombinations);
  }
  
  @Test
  public void testLeverSwitch6Way() {
    LeverSwitch leverSwitch = new LeverSwitch();

    String[] validCombinations = new String[] {"0,0,8", "0,1,3", "0,1,5", "1,0,8", "1,2,8", "1,1,5", "2,2,8", "2,1,5", "2,1,7",
        "3,2,8", "3,4,8", "3,1,7", "4,4,8", "4,1,7", "4,1,9", "5,4,8", "5,6,8", "5,1,9"};
    
    leverSwitch.setValue(LeverSwitchType._6_WAY_OG);
    testSwitch(leverSwitch, validCombinations);
  }
}
