package org.diylc.netlist;

import org.diylc.components.electromechanical.SlideSwitch;
import org.diylc.components.electromechanical.SlideSwitchType;
import org.junit.Test;

public abstract class SlideSwitchTests extends AbstractSwitchTests {

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
    String[] validCombinations = new String[] {"0,0,2", "0,1,3", "1,2,4", "1,3,5"};
    testSwitch(slideSwitch, validCombinations);
  }
  
  @Test
  public void testSlideSwitchDP3T() {
    SlideSwitch slideSwitch = new SlideSwitch();
    slideSwitch.setValue(SlideSwitchType.DP3T);
    String[] validCombinations = new String[] {"0,0,2", "0,1,3", "1,2,4", "1,3,5", "2,4,6", "2,5,7"};
    testSwitch(slideSwitch, validCombinations);
  }
}
