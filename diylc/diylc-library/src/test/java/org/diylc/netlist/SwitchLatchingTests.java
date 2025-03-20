package org.diylc.netlist;

import org.junit.Test;

import org.diylc.components.electromechanical.SwitchLatchingSymbol;
import org.diylc.components.electromechanical.SwitchLatchingSymbol.PoleCount;

public class SwitchLatchingTests extends AbstractSwitchTests {
  
  @Test
  public void testSPSTSwitch() {
    SwitchLatchingSymbol sw = new SwitchLatchingSymbol();

    String[] validCombinations =
        new String[] {"0,0,1"};

    sw.setValue(SwitchLatchingSymbol.SwitchConfiguration._1x2);
    sw.setPoleCount(PoleCount.ONE);
    testSwitch(sw, validCombinations);
  }

  @Test
  public void test3PDTSwitch() {
    SwitchLatchingSymbol sw = new SwitchLatchingSymbol();

    String[] validCombinations =
        new String[] {"0,0,1", "0,4,5", "0,8,9", "1,0,2", "1,4,6", "1,8,10", "2,0,3", "2,4,7", "2,8,11"};

    sw.setValue(SwitchLatchingSymbol.SwitchConfiguration._3x3);
    sw.setPoleCount(PoleCount.THREE);
    testSwitch(sw, validCombinations);
  }

  @Test
  public void testStratSwitch() {
    SwitchLatchingSymbol sw = new SwitchLatchingSymbol();

    String[] validCombinations = new String[] {"0,0,1", "0,4,5", "1,0,1", "1,4,5", "1,0,2", "1,4,6",
        "2,0,2", "2,4,6", "3,0,2", "3,0,3", "3,4,6", "3,4,7", "4,0,3", "4,4,7"};

    sw.setValue(SwitchLatchingSymbol.SwitchConfiguration._3x5xSHORT);
    sw.setPoleCount(PoleCount.TWO);
    testSwitch(sw, validCombinations);
  }
  
  @Test
  public void testON_OFF_ON() {
    SwitchLatchingSymbol sw = new SwitchLatchingSymbol();

    String[] validCombinations = new String[] {"0,0,1", "0,3,4", "2,0,2", "2,3,5"};

    sw.setValue(SwitchLatchingSymbol.SwitchConfiguration._2x3xOFF);
    sw.setPoleCount(PoleCount.TWO);
    testSwitch(sw, validCombinations);
  }
}
