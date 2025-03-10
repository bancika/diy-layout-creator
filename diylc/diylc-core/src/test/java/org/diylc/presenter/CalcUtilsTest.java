package org.diylc.presenter;

import static org.junit.Assert.assertTrue;

import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Test;

public class CalcUtilsTest {

  @Test
  public void TestRoundToGrid() {
    Size spacing = new Size(1d, SizeUnit.mm);
    int value = 5;
    double s = spacing.convertToPixels();
    double targetPrev = (value - 1) * s;
    double target = value * s;
    double targetNext = (value + 1) * s;
    for (int i = -100; i <= 100; i++) {
      double rounded = CalcUtils.roundToGrid(target + i / 100.0 * s, spacing);
      double expected = i < -50 ? targetPrev : (i >= 50 ? targetNext : target);
      assertTrue("Failed at " + i + " rounded = " + rounded + "; expected = " + expected, Math.abs(rounded - expected) < 1e-10);
    }
  }
}
