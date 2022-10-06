package org.diylc.netlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Arrays;
import org.diylc.components.electromechanical.MiniToggleSwitch;
import org.diylc.components.electromechanical.SlideSwitch;
import org.diylc.components.electromechanical.SlideSwitchType;
import org.diylc.components.electromechanical.ToggleSwitchType;
import org.diylc.components.guitar.LPSwitch;
import org.diylc.components.guitar.LeverSwitch;
import org.diylc.components.guitar.LeverSwitch.LeverSwitchType;
import org.diylc.components.guitar.S1Switch;
import org.diylc.components.guitar.SchallerMegaSwitch;
import org.diylc.components.guitar.SchallerMegaSwitch.MegaSwitchType;
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
  public void testLeverSwitchImport() {
    LeverSwitch leverSwitch = new LeverSwitch();

    String[] validCombinations = new String[] {"0,0,3", "0,4,5", "1,0,3", "1,4,5", "1,1,3",
        "1,4,6", "2,1,3", "2,4,6", "3,1,3", "3,2,3", "3,4,6", "3,4,7", "4,2,3", "4,4,7"};
    
    leverSwitch.setType(LeverSwitchType.DP3T_5pos_Import);
    testSwitch(leverSwitch, validCombinations);
  }
  
  @Test
  public void testLeverSwitch6Way() {
    LeverSwitch leverSwitch = new LeverSwitch();

    String[] validCombinations = new String[] {"0,0,8", "0,2,8", "0,1,3", "1,2,8", "1,1,3", "1,1,5", "2,2,8", "2,4,8", "2,1,5",
        "3,4,8", "3,1,5", "3,1,7", "4,4,8", "4,6,8", "4,1,7", "5,6,8", "5,1,7", "5,1,9"};
    
    leverSwitch.setType(LeverSwitchType._6_WAY_OG);
    testSwitch(leverSwitch, validCombinations);
  }
  
  @Test
  public void testS1Switch() {
    S1Switch s1Switch = new S1Switch();

    String[] validCombinations = new String[] {"0,1,2", "0,4,5", "0,7,8", "0,10,11", "1,2,3", "1,5,6", "1,8,9", "1,11,12"};
    
    testSwitch(s1Switch, validCombinations);
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
  
  @Test
  public void testMegaSwitchE() {
    SchallerMegaSwitch sw = new SchallerMegaSwitch();

    sw.setType(MegaSwitchType.E);
    String[] validCombinations = new String[] {"0,2,6", "1,3,5", "1,2,6", "1,0,2", "2,3,4",
        "2,2,5", "2,1,2", "3,3,4", "3,0,2", "3,1,2", "4,1,2"};

    testSwitch(sw, validCombinations);
  }
  
  @Test
  public void testMegaSwitchEPlus() {
    SchallerMegaSwitch sw = new SchallerMegaSwitch();

    sw.setType(MegaSwitchType.E_PLUS);
    String[] validCombinations = new String[] {"0,2,8", "1,3,6", "1,2,8", "1,0,2", "2,3,5",
        "2,2,7", "2,1,2", "3,3,4", "3,0,2", "3,1,2", "4,1,2"};

    testSwitch(sw, validCombinations);
  }
  
  @Test
  public void testMegaSwitchP() {
    SchallerMegaSwitch sw = new SchallerMegaSwitch();

    sw.setType(MegaSwitchType.P);
    String[] validCombinations = new String[] {"0,3,4", "0,1,6", "1,3,4", "1,1,2", "1,0,4", "1,1,5", 
        "2,0,2", "2,1,6", "3,0,1", "3,3,4", "3,2,4", "3,1,6", "4,1,5"};

    testSwitch(sw, validCombinations);
  }
  
  @Test
  public void testMegaSwitchS() {
    SchallerMegaSwitch sw = new SchallerMegaSwitch();

    sw.setType(MegaSwitchType.S);
    String[] validCombinations = new String[] {"0,6,7", "0,2,3", "1,4,7", "1,6,7", "1,0,3", "1,2,3", 
        "2,4,7", "2,0,3", "3,4,7", "3,5,7", "3,0,3", "3,1,3", "4,5,7", "4,1,3"};

    testSwitch(sw, validCombinations);
  }
  
  @Test
  public void testMegaSwitchM() {
    SchallerMegaSwitch sw = new SchallerMegaSwitch();

    sw.setType(MegaSwitchType.M);
    String[] validCombinations = new String[] {"0,4,5", "0,10,11", "0,16,17", "0,22,23", 
        "1,3,5", "1,9,11", "1,15,17", "1,21,23",
        "2,2,5", "2,8,11", "2,14,17", "2,20,23",
        "3,1,5", "3,7,11", "3,13,17", "3,19,23",
        "4,0,5", "4,6,11", "4,12,17", "4,18,23"};

    testSwitch(sw, validCombinations);
  }
  
  @Test
  public void testMegaSwitchT() {
    SchallerMegaSwitch sw = new SchallerMegaSwitch();

    sw.setType(MegaSwitchType.T);
    String[] validCombinations = new String[] {"0,6,7", "0,2,3", "1,4,7", "1,0,3",  
        "2,5,7", "2,1,3"};

    testSwitch(sw, validCombinations);
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
