package org.diylc.components.guitar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.diylc.components.guitar.FreewayBlade.FreewayBladeType;
import org.junit.Test;

/**
 * Focused regression tests verifying that FreewayBlade.arePointsConnected() is symmetric,
 * fixing the 5B5-02 analysis issue where reversed connection pairs were not found.
 */
public class FreewayBladeConnectivityTest {

  /**
   * Assertion 1: B5_02, position 0, terminals 0 and 6 must be connected in both argument orders.
   * The connection table stores this as {6, 0}; a query for (0, 6) was the failing case.
   */
  @Test
  public void testB5_02Position0Terminals0And6ConnectedBothOrders() {
    FreewayBlade sw = new FreewayBlade();
    sw.setType(FreewayBladeType.B5_02);

    assertTrue("arePointsConnected(6, 0, 0) should be true",
        sw.arePointsConnected(6, 0, 0));
    assertTrue("arePointsConnected(0, 6, 0) should be true (was the failing query direction)",
        sw.arePointsConnected(0, 6, 0));
  }

  /**
   * Assertion 2: For every FreewayBladeType, every position, and every terminal pair (a, b):
   * arePointsConnected(a, b, position) == arePointsConnected(b, a, position).
   */
  @Test
  public void testArePointsConnectedIsSymmetricForAllTypes() {
    for (FreewayBladeType type : FreewayBladeType.values()) {
      FreewayBlade sw = new FreewayBlade();
      sw.setType(type);
      int terminalCount = sw.getControlPointCount();
      for (int pos = 0; pos < sw.getPositionCount(); pos++) {
        for (int a = 0; a < terminalCount; a++) {
          for (int b = 0; b < terminalCount; b++) {
            assertEquals(
                "Symmetry violated: type=" + type + " pos=" + pos + " a=" + a + " b=" + b,
                sw.arePointsConnected(a, b, pos),
                sw.arePointsConnected(b, a, pos));
          }
        }
      }
    }
  }

  /**
   * Assertion 3: A known disconnected pair is not connected.
   * In B5_02 position 0 only {6,0} is active, so terminals 1 and 5 are disconnected.
   */
  @Test
  public void testKnownDisconnectedPairIsNotConnected() {
    FreewayBlade sw = new FreewayBlade();
    sw.setType(FreewayBladeType.B5_02);

    assertFalse("Terminals 1 and 5 should not be connected in B5_02 position 0",
        sw.arePointsConnected(1, 5, 0));
    assertFalse("Terminals 5 and 1 should not be connected in B5_02 position 0",
        sw.arePointsConnected(5, 1, 0));
  }
}
