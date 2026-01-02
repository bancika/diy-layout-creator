package org.diylc.netlist;

import org.junit.Test;

import org.diylc.components.electromechanical.TactileMicroSwitch;
import org.diylc.components.electromechanical.TactileMicroSwitch.PinPairingMode;

public class TactileMicroSwitchTests extends AbstractSwitchTests {

  @Test
  public void testTactileMicroSwitchOpposite() {
    TactileMicroSwitch tactileSwitch = new TactileMicroSwitch();
    tactileSwitch.setPinPairingMode(PinPairingMode.Opposite);
    // Position 0 (Released): no connections
    // Position 1 (Pressed): pins on opposite sides (same row): 0-2 and 1-3
    String[] validCombinations = new String[] {
      "1,0,2", "1,2,0",  // Position 1: 0-2 (both directions)
      "1,1,3", "1,3,1"   // Position 1: 1-3 (both directions)
    };
    testSwitch(tactileSwitch, validCombinations);
  }

  @Test
  public void testTactileMicroSwitchDiagonal() {
    TactileMicroSwitch tactileSwitch = new TactileMicroSwitch();
    tactileSwitch.setPinPairingMode(PinPairingMode.Diagonal);
    // Position 0 (Released): no connections
    // Position 1 (Pressed): diagonally opposite pins: 0-3 and 1-2
    String[] validCombinations = new String[] {
      "1,0,3", "1,3,0",  // Position 1: 0-3 (both directions)
      "1,1,2", "1,2,1"   // Position 1: 1-2 (both directions)
    };
    testSwitch(tactileSwitch, validCombinations);
  }

  @Test
  public void testTactileMicroSwitchAdjacent() {
    TactileMicroSwitch tactileSwitch = new TactileMicroSwitch();
    tactileSwitch.setPinPairingMode(PinPairingMode.Adjacent);
    // Position 0 (Released): no connections
    // Position 1 (Pressed): pins next to each other (same column): 0-1 and 2-3
    String[] validCombinations = new String[] {
      "1,0,1", "1,1,0",  // Position 1: 0-1 (both directions)
      "1,2,3", "1,3,2"   // Position 1: 2-3 (both directions)
    };
    testSwitch(tactileSwitch, validCombinations);
  }
}

