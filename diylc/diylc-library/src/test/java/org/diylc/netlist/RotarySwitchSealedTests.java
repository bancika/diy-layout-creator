package org.diylc.netlist;

import org.junit.Test;

import org.diylc.components.electromechanical.RotarySwitchSealed;
import org.diylc.components.electromechanical.RotarySwitchSealedType;

public abstract class RotarySwitchSealedTests extends AbstractSwitchTests {
  
  @Test
  public void testRotarySwitchSP12T() {
    RotarySwitchSealed sw = new RotarySwitchSealed();

    String[] validCombinations = new String[] {"0,1,13", "1,2,13", "2,3,13", "3,4,13", "4,5,13", "5,6,13", "6,7,13", "7,8,13",
        "8,9,13", "9,10,13", "10,11,13", "11,12,13"};
    
    sw.setValue(RotarySwitchSealedType._1P12T);
    testSwitch(sw, validCombinations);
  }
  
  @Test
  public void testRotarySwitchDP6T() {
    RotarySwitchSealed sw = new RotarySwitchSealed();

    String[] validCombinations = new String[] {
        "0,1,13", "1,2,13", "2,3,13", "3,4,13", "4,5,13", "5,6,13", 
        "0,7,14", "1,8,14", "2,9,14", "3,10,14", "4,11,14", "5,12,14"};
    
    sw.setValue(RotarySwitchSealedType._2P6T);
    testSwitch(sw, validCombinations);
  }
  
  @Test
  public void testRotarySwitch3P4T() {
    RotarySwitchSealed sw = new RotarySwitchSealed();

    String[] validCombinations = new String[] {
        "0,1,13", "1,2,13", "2,3,13", "3,4,13",
        "0,5,14", "1,6,14", "2,7,14", "3,8,14",
        "0,9,15", "1,10,15", "2,11,15", "3,12,15"};
    
    sw.setValue(RotarySwitchSealedType._3P4T);
    testSwitch(sw, validCombinations);
  }
  
  @Test
  public void testRotarySwitch4P3T() {
    RotarySwitchSealed sw = new RotarySwitchSealed();

    String[] validCombinations = new String[] {
        "0,1,13", "1,2,13", "2,3,13",
        "0,4,14", "1,5,14", "2,6,14",
        "0,7,15", "1,8,15", "2,9,15",
        "0,10,16", "1,11,16", "2,12,16",};
    
    sw.setValue(RotarySwitchSealedType._4P3T);
    testSwitch(sw, validCombinations);
  }
}
