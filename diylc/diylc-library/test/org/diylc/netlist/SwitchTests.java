package org.diylc.netlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Arrays;
import org.diylc.components.electromechanical.SlideSwitch;
import org.diylc.components.electromechanical.SlideSwitchType;
import org.diylc.components.guitar.LPSwitch;
import org.diylc.components.guitar.LeverSwitch;
import org.diylc.components.guitar.LeverSwitch.LeverSwitchType;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;
import org.junit.Test;

public class SwitchTests {

  @Test
  public void testLeverSwitchDP5T() {
    LeverSwitch leverSwitch = new LeverSwitch();

    leverSwitch.setType(LeverSwitchType.DP5T);
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
    leverSwitch.setType(LeverSwitchType._4P5T);
    testSwitch(leverSwitch, validCombinations);
  }

  @Test
  public void testLPSwitch() {
    LPSwitch lpSwitch = new LPSwitch();
    String[] validCombinations = new String[] {"0,1,2", "1,1,2", "1,2,3", "1,1,3", "2,2,3"};
    testSwitch(lpSwitch, validCombinations);
  }

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

  private void testSwitch(ISwitch sw, String[] validCombinations) {
    Arrays.sort(validCombinations);
    int count = 0;
    for (int p = 0; p < sw.getPositionCount(); p++)
      for (int i = 0; i < ((IDIYComponent<?>) sw).getControlPointCount(); i++)
        for (int j = 0; j < ((IDIYComponent<?>) sw).getControlPointCount(); j++) {
          String test = p + "," + i + "," + j;
          boolean isConnected = sw.arePointsConnected(i, j, p);
          if (isConnected)
            count++;
          boolean isOk = (isConnected && Arrays.binarySearch(validCombinations, test) >= 0)
              || !isConnected && Arrays.binarySearch(validCombinations, test) < 0;
          if (!isOk)
            fail("Bad connection for p=" + p + ", i=" + i + ",j=" + j);
        }
    assertEquals(validCombinations.length, count);
  }
}
