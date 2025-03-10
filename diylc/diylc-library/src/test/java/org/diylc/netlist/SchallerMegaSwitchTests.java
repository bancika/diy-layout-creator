package org.diylc.netlist;

import org.diylc.components.guitar.SchallerMegaSwitch;
import org.diylc.components.guitar.SchallerMegaSwitch.MegaSwitchType;
import org.junit.Test;

public class SchallerMegaSwitchTests extends AbstractSwitchTests {

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
}
