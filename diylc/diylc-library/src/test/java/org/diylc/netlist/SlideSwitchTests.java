package org.diylc.netlist;

import org.junit.Test;

import org.diylc.components.electromechanical.SlideSwitch;
import org.diylc.components.electromechanical.SlideSwitchType;

public class SlideSwitchTests extends AbstractSwitchTests {

  @Test
  public void testSlideSwitchSPDT() {
    SlideSwitch slideSwitch = new SlideSwitch();
    slideSwitch.setValue(SlideSwitchType.SPDT);
    String[] validCombinations = new String[] {"0,0,1", "1,1,2"};
    testSwitch(slideSwitch, validCombinations);
  }
  
  @Test
  public void testSlideSwitchDPDT() {
    SlideSwitch slideSwitch = new SlideSwitch();
    slideSwitch.setValue(SlideSwitchType.DPDT);
    String[] validCombinations = new String[] {
      "0,0,1", "0,3,4",  // Position 1 connections
      "1,1,2", "1,4,5"   // Position 2 connections
    };
    testSwitch(slideSwitch, validCombinations);
  }
  
  @Test
  public void testSlideSwitchDP3T() {
    SlideSwitch slideSwitch = new SlideSwitch();
    slideSwitch.setValue(SlideSwitchType.DP3T);
    String[] validCombinations = new String[] {
      "0,0,1", "0,4,5",  // Position 1 connections
      "1,1,2", "1,5,6",  // Position 2 connections
      "2,2,3", "2,6,7"   // Position 3 connections
    };
    testSwitch(slideSwitch, validCombinations);
  }
}
