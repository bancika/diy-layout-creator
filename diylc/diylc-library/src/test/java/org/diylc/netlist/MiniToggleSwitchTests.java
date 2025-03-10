package org.diylc.netlist;

import org.junit.Test;

import org.diylc.components.electromechanical.MiniToggleSwitch;
import org.diylc.components.electromechanical.ToggleSwitchType;

public class MiniToggleSwitchTests extends AbstractSwitchTests {
  
  @Test
  public void testMiniToggleSwitchDPDTononon1() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType.DPDT_ononon_1);
    String[] validCombinations = new String[] {"0,0,1", "0,3,4", "1,0,1", "1,4,5", "2,1,2", "2,4,5"};
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitchDPDTononon2() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType.DPDT_ononon_2);
    String[] validCombinations = new String[] {"0,0,1", "0,3,4", "1,1,2", "1,3,4", "2,1,2", "2,4,5"};
    testSwitch(toggleSwitch, validCombinations);
  }
}
