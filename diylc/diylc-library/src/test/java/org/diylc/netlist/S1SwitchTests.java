package org.diylc.netlist;

import static org.junit.Assert.assertTrue;
import java.awt.geom.Point2D;

import org.junit.Test;

import org.diylc.common.Orientation;
import org.diylc.components.guitar.S1Switch;

public class S1SwitchTests extends AbstractSwitchTests {
  
  @Test
  public void testS1Switch() {
    S1Switch s1Switch = new S1Switch();

    String[] validCombinations = new String[] {"0,1,2", "0,4,5", "0,7,8", "0,10,11", "1,2,3", "1,5,6", "1,8,9", "1,11,12"};
    
    testSwitch(s1Switch, validCombinations);
  }

  // Fender S-1 matrix as seen from the back with the pot lugs pointing right: in the up position
  // the top and bottom commons connect to the lug on their right and the side commons to the lug
  // above them, pressed down connects the opposite lugs
  @Test
  public void testS1SwitchMatchesFenderMatrixWithLugsPointingRight() {
    S1Switch s1Switch = new S1Switch();
    s1Switch.setOrientation(Orientation._270);

    int[] commons = new int[] {2, 5, 8, 11};
    double centerX = 0;
    double centerY = 0;
    for (int common : commons) {
      centerX += s1Switch.getControlPoint(common).getX() / commons.length;
      centerY += s1Switch.getControlPoint(common).getY() / commons.length;
    }

    for (int position = 0; position < s1Switch.getPositionCount(); position++) {
      int sign = "Up".equals(s1Switch.getPositionName(position)) ? 1 : -1;
      for (int common : commons) {
        Point2D c = s1Switch.getControlPoint(common);
        boolean horizontalStage = Math.abs(c.getY() - centerY) > Math.abs(c.getX() - centerX);
        int connected = 0;
        for (int i = 1; i < s1Switch.getControlPointCount(); i++) {
          if (i == common || !(s1Switch.arePointsConnected(Math.min(i, common), Math.max(i, common), position))) {
            continue;
          }
          connected++;
          Point2D p = s1Switch.getControlPoint(i);
          if (horizontalStage) {
            assertTrue("pin " + i + " for common " + common, sign * (p.getX() - c.getX()) > 0);
          } else {
            assertTrue("pin " + i + " for common " + common, sign * (c.getY() - p.getY()) > 0);
          }
        }
        assertTrue(connected == 1);
      }
    }
  }
}
