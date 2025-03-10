package org.diylc.netlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Arrays;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;

public abstract class AbstractSwitchTests {

  protected void testSwitch(ISwitch sw, String[] validCombinations) {
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
