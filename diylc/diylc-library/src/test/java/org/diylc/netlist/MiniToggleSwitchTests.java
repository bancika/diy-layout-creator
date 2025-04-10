package org.diylc.netlist;

import org.junit.Test;

import org.diylc.components.electromechanical.MiniToggleSwitch;
import org.diylc.components.electromechanical.ToggleSwitchType;

public class MiniToggleSwitchTests extends AbstractSwitchTests {
  
  @Test
  public void testMiniToggleSwitchSPST() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType.SPST);
    String[] validCombinations = new String[] {"0,0,1"};
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitchSPDT() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType.SPDT);
    String[] validCombinations = new String[] {
      "0,0,1", 
      "1,1,2"
    };
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitchSPDT_off() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType.SPDT_off);
    String[] validCombinations = new String[] {
      "0,0,1",
      "2,1,2"
    };
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitchDPDT() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType.DPDT);
    String[] validCombinations = new String[] {
      "0,0,1", "0,3,4",
      "1,1,2", "1,4,5"
    };
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitchDPDT_off() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType.DPDT_off);
    String[] validCombinations = new String[] {
      "0,0,1", "0,3,4",
      "2,1,2", "2,4,5"
    };
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitchDPDT_ononon1() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType.DPDT_ononon_1);
    String[] validCombinations = new String[] {
      "0,0,1", "0,3,4",
      "1,0,1", "1,4,5",
      "2,1,2", "2,4,5"
    };
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitchDPDT_ononon2() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType.DPDT_ononon_2);
    String[] validCombinations = new String[] {
      "0,0,1", "0,3,4",
      "1,1,2", "1,3,4",
      "2,1,2", "2,4,5"
    };
    testSwitch(toggleSwitch, validCombinations);
  }

  @Test
  public void testMiniToggleSwitch_3PDT() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType._3PDT);
    String[] validCombinations = new String[] {
      "0,0,1", "0,3,4", "0,6,7",
      "1,1,2", "1,4,5", "1,7,8"
    };
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitch_3PDT_off() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType._3PDT_off);
    String[] validCombinations = new String[] {
      "0,0,1", "0,3,4", "0,6,7",
      "2,1,2", "2,4,5", "2,7,8"
    };
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitch_4PDT() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType._4PDT);
    String[] validCombinations = new String[] {
      "0,0,1", "0,3,4", "0,6,7", "0,9,10",
      "1,1,2", "1,4,5", "1,7,8", "1,10,11"
    };
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitch_4PDT_off() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType._4PDT_off);
    String[] validCombinations = new String[] {
      "0,0,1", "0,3,4", "0,6,7", "0,9,10",
      "2,1,2", "2,4,5", "2,7,8", "2,10,11"
    };
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitch_4PDT_ononon1() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType._4PDT_ononon_1);
    String[] validCombinations = new String[] {
      "0,0,1", "0,3,4", "0,6,7", "0,9,10",
      "1,0,1", "1,4,5", "1,6,7", "1,10,11",
      "2,1,2", "2,4,5", "2,7,8", "2,10,11"
    };
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitch_4PDT_ononon2() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType._4PDT_ononon_2);
    String[] validCombinations = new String[] {
      "0,0,1", "0,3,4", "0,6,7", "0,9,10",
      "1,1,2", "1,3,4", "1,7,8", "1,9,10",
      "2,1,2", "2,4,5", "2,7,8", "2,10,11"
    };
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitch_5PDT() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType._5PDT);
    String[] validCombinations = new String[] {
      "0,0,1", "0,3,4", "0,6,7", "0,9,10", "0,12,13",
      "1,1,2", "1,4,5", "1,7,8", "1,10,11", "1,13,14"
    };
    testSwitch(toggleSwitch, validCombinations);
  }
  
  @Test
  public void testMiniToggleSwitch_5PDT_off() {
    MiniToggleSwitch toggleSwitch = new MiniToggleSwitch();
    toggleSwitch.setValue(ToggleSwitchType._5PDT_off);
    String[] validCombinations = new String[] {
      "0,0,1", "0,3,4", "0,6,7", "0,9,10", "0,12,13",
      "2,1,2", "2,4,5", "2,7,8", "2,10,11", "2,13,14"
    };
    testSwitch(toggleSwitch, validCombinations);
  }
}
