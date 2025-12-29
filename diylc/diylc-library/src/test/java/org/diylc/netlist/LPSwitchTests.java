package org.diylc.netlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import org.diylc.components.guitar.LPSwitch;
import org.diylc.components.guitar.LPSwitch.MiddleTerminalConfiguration;
import org.diylc.common.Orientation;
import org.diylc.core.IDIYComponent;

public class LPSwitchTests extends AbstractSwitchTests {
  
  @Test
  public void testLPSwitchConnected() {
    LPSwitch lpSwitch = new LPSwitch();
    // Default configuration is Connected
    assertEquals(MiddleTerminalConfiguration.Connected, lpSwitch.getMiddleTerminalConfiguration());
    // Connected has 4 control points (0, 1, 2, 3)
    assertEquals(4, ((IDIYComponent<?>) lpSwitch).getControlPointCount());
    // Position 0 (Treble): 1-2 connected
    // Position 1 (Middle): 1-2, 1-3, 2-3 connected (all terminals > 0 connected)
    // Position 2 (Rhythm): 2-3 connected
    String[] validCombinations = new String[] {"0,1,2", "1,1,2", "1,2,3", "1,1,3", "2,2,3"};
    testSwitch(lpSwitch, validCombinations);
  }
  
  @Test
  public void testLPSwitchDisconnected() {
    LPSwitch lpSwitch = new LPSwitch();
    lpSwitch.setMiddleTerminalConfiguration(MiddleTerminalConfiguration.Disconnected);
    assertEquals(MiddleTerminalConfiguration.Disconnected, lpSwitch.getMiddleTerminalConfiguration());
    // Disconnected has 5 control points (0, 1, 2, 3, 4)
    assertEquals(5, ((IDIYComponent<?>) lpSwitch).getControlPointCount());
    // Position 0 (Treble): 1-2 connected
    // Position 1 (Middle): 1-2 and 3-4 connected (both pairs)
    // Position 2 (Rhythm): 3-4 connected
    String[] validCombinations = new String[] {"0,1,2", "1,1,2", "1,3,4", "2,3,4"};
    testSwitch(lpSwitch, validCombinations);
  }
  
  @Test
  public void testControlPointCountChange() {
    LPSwitch lpSwitch = new LPSwitch();
    // Start with Connected (4 points)
    assertEquals(4, ((IDIYComponent<?>) lpSwitch).getControlPointCount());
    
    // Switch to Disconnected (5 points)
    lpSwitch.setMiddleTerminalConfiguration(MiddleTerminalConfiguration.Disconnected);
    assertEquals(5, ((IDIYComponent<?>) lpSwitch).getControlPointCount());
    
    // Switch back to Connected (4 points)
    lpSwitch.setMiddleTerminalConfiguration(MiddleTerminalConfiguration.Connected);
    assertEquals(4, ((IDIYComponent<?>) lpSwitch).getControlPointCount());
  }
  
  @Test
  public void testPositionNames() {
    LPSwitch lpSwitch = new LPSwitch();
    assertEquals(3, lpSwitch.getPositionCount());
    assertEquals("Treble", lpSwitch.getPositionName(0));
    assertEquals("Middle", lpSwitch.getPositionName(1));
    assertEquals("Rhythm", lpSwitch.getPositionName(2));
    assertNull(lpSwitch.getPositionName(3)); // Invalid position
    assertNull(lpSwitch.getPositionName(-1)); // Invalid position
  }
  
  @Test
  public void testOrientationDoesNotAffectConnectivity() {
    LPSwitch lpSwitch = new LPSwitch();
    // Test with default Connected configuration
    String[] validCombinations = new String[] {"0,1,2", "1,1,2", "1,2,3", "1,1,3", "2,2,3"};
    
    // Test all orientations - connectivity should remain the same
    for (Orientation orientation : Orientation.values()) {
      lpSwitch.setOrientation(orientation);
      assertEquals(orientation, lpSwitch.getOrientation());
      // Connectivity should be unchanged regardless of orientation
      testSwitch(lpSwitch, validCombinations);
    }
  }
  
  @Test
  public void testOrientationWithDisconnectedConfiguration() {
    LPSwitch lpSwitch = new LPSwitch();
    lpSwitch.setMiddleTerminalConfiguration(MiddleTerminalConfiguration.Disconnected);
    String[] validCombinations = new String[] {"0,1,2", "1,1,2", "1,3,4", "2,3,4"};
    
    // Test all orientations with Disconnected configuration
    for (Orientation orientation : Orientation.values()) {
      lpSwitch.setOrientation(orientation);
      assertEquals(orientation, lpSwitch.getOrientation());
      // Connectivity should be unchanged regardless of orientation
      testSwitch(lpSwitch, validCombinations);
    }
  }
  
  @Test
  public void testConfigurationSwitchPreservesConnectivity() {
    LPSwitch lpSwitch = new LPSwitch();
    
    // Test Connected configuration
    lpSwitch.setMiddleTerminalConfiguration(MiddleTerminalConfiguration.Connected);
    String[] connectedCombinations = new String[] {"0,1,2", "1,1,2", "1,2,3", "1,1,3", "2,2,3"};
    testSwitch(lpSwitch, connectedCombinations);
    
    // Switch to Disconnected
    lpSwitch.setMiddleTerminalConfiguration(MiddleTerminalConfiguration.Disconnected);
    String[] disconnectedCombinations = new String[] {"0,1,2", "1,1,2", "1,3,4", "2,3,4"};
    testSwitch(lpSwitch, disconnectedCombinations);
    
    // Switch back to Connected
    lpSwitch.setMiddleTerminalConfiguration(MiddleTerminalConfiguration.Connected);
    testSwitch(lpSwitch, connectedCombinations);
  }
}
