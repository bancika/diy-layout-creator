package org.diylc.utils;

import org.diylc.core.ISwitch;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

public class SwitchUtilsTest {

    // Mock switch implementation for testing
    private static class MockSwitch implements ISwitch {
        private final List<List<int[]>> connectionsByPosition;
        private final Integer selectedPosition;
        private final boolean renderConnected;
        private final int positionCount;

        public MockSwitch(List<List<int[]>> connectionsByPosition, Integer selectedPosition, boolean renderConnected) {
            this.connectionsByPosition = connectionsByPosition;
            this.selectedPosition = selectedPosition;
            this.renderConnected = renderConnected;
            this.positionCount = connectionsByPosition.size();
        }

        @Override
        public boolean arePointsConnected(int index1, int index2, int position) {
            if (position >= connectionsByPosition.size()) return false;
            return connectionsByPosition.get(position).stream()
                .anyMatch(conn -> 
                    (conn[0] == index1 && conn[1] == index2) || 
                    (conn[0] == index2 && conn[1] == index1));
        }

        @Override
        public Integer getSelectedPosition() {
            return selectedPosition;
        }

        public Boolean getShowMarkers() {
            return renderConnected;
        }

        @Override 
        public int getPositionCount() { 
            return positionCount; 
        }

        @Override 
        public String getPositionName(int position) { 
            return String.valueOf(position + 1); 
        }
    }

    @Test
    public void testSimpleSPSTSwitch() {
        // Create a simple SPST switch with 2 points
        List<List<int[]>> connectionsByPosition = Arrays.asList(
            Arrays.asList(new int[]{0, 1})  // Position 0
        );
        MockSwitch mockSwitch = new MockSwitch(connectionsByPosition, 0, true);
        
        String[] markers = SwitchUtils.getSwitchingMarkers(mockSwitch, 2, true);
        
        // Both points are common in the only position
        assertEquals("A", markers[0]);
        assertEquals("B", markers[1]);
    }

    @Test
    public void testSPDTSwitch() {
        // Create an SPDT switch with 3 points
        List<List<int[]>> connectionsByPosition = Arrays.asList(
            Arrays.asList(new int[]{0, 1}),  // Position 0
            Arrays.asList(new int[]{0, 2})   // Position 1
        );
        MockSwitch mockSwitch = new MockSwitch(connectionsByPosition, 0, true);
        
        String[] markers = SwitchUtils.getSwitchingMarkers(mockSwitch, 3, true);
        
        // Point 0 is common in all positions
        assertEquals("A", markers[0]);
        // Point 1 is connected to point 0 in position 0
        assertEquals("A1", markers[1]);
        // Point 2 is connected to point 0 in position 1
        assertEquals("A2", markers[2]);
    }

    @Test
    public void testDPDTSwitch() {
        // Create a DPDT switch with 6 points
        List<List<int[]>> connectionsByPosition = Arrays.asList(
            Arrays.asList(new int[]{0, 1}, new int[]{3, 4}),  // Position 0
            Arrays.asList(new int[]{0, 2}, new int[]{3, 5})   // Position 1
        );
        MockSwitch mockSwitch = new MockSwitch(connectionsByPosition, 0, true);
        
        String[] markers = SwitchUtils.getSwitchingMarkers(mockSwitch, 6, true);
        
        // Points 0 and 3 are common in all positions
        assertEquals("A", markers[0]);
        assertEquals("A1", markers[1]);
        assertEquals("A2", markers[2]);
        assertEquals("B", markers[3]);
        assertEquals("B1", markers[4]);
        assertEquals("B2", markers[5]);
    }

    @Test
    public void testNonVerboseMarkers() {
        // Create a DPDT switch with 6 points
        List<List<int[]>> connectionsByPosition = Arrays.asList(
            Arrays.asList(new int[]{0, 1}, new int[]{3, 4}),  // Position 0
            Arrays.asList(new int[]{0, 2}, new int[]{3, 5})   // Position 1
        );
        MockSwitch mockSwitch = new MockSwitch(connectionsByPosition, 0, true);

        String[] markers = SwitchUtils.getSwitchingMarkers(mockSwitch, 6, false);

        // Points 0 and 3 are common in all positions
        assertEquals("A", markers[0]);
        assertEquals("1", markers[1]);
        assertEquals("2", markers[2]);
        assertEquals("B", markers[3]);
        assertEquals("1", markers[4]);
        assertEquals("2", markers[5]);
    }

    @Test
    public void testMultiplePositions() {
        // Test switch with multiple positions
        List<List<int[]>> connectionsByPosition = Arrays.asList(
            Arrays.asList(new int[]{0, 1}),  // Position 0
            Arrays.asList(new int[]{0, 2}),  // Position 1
            Arrays.asList(new int[]{0, 3})   // Position 2
        );
        MockSwitch mockSwitch = new MockSwitch(connectionsByPosition, 0, true);
        
        String[] markers = SwitchUtils.getSwitchingMarkers(mockSwitch, 4, true);
        
        // Point 0 is common in all positions
        assertEquals("A", markers[0]);
        // Points 1, 2, 3 are connected to point 0 in positions 0, 1, 2 respectively
        assertEquals("A1", markers[1]);
        assertEquals("A2", markers[2]);
        assertEquals("A3", markers[3]);
    }

    @Test
    public void testNoConnections() {
        // Test switch with no connections
        List<List<int[]>> connectionsByPosition = Arrays.asList(
            Collections.emptyList()  // Position 0 with no connections
        );
        MockSwitch mockSwitch = new MockSwitch(connectionsByPosition, 0, true);
        
        String[] markers = SwitchUtils.getSwitchingMarkers(mockSwitch, 3, true);
        
        // No common points when there are no connections
        assertNull(markers[0]);
        assertNull(markers[1]);
        assertNull(markers[2]);
    }

    @Test
    public void testComplexSwitch() {
        // Test a more complex switch with multiple poles and positions
        List<List<int[]>> connectionsByPosition = Arrays.asList(
            Arrays.asList(new int[]{0, 1}, new int[]{3, 4}, new int[]{6, 7}),  // Position 0
            Arrays.asList(new int[]{0, 2}, new int[]{3, 5}, new int[]{6, 8})   // Position 1
        );
        MockSwitch mockSwitch = new MockSwitch(connectionsByPosition, 0, true);
        
        String[] markers = SwitchUtils.getSwitchingMarkers(mockSwitch, 9, true);
        
        // Points 0, 3, and 6 are common in all positions
        assertEquals("A", markers[0]);
        assertEquals("A1", markers[1]);
        assertEquals("A2", markers[2]);
        assertEquals("B", markers[3]);
        assertEquals("B1", markers[4]);
        assertEquals("B2", markers[5]);
        assertEquals("C", markers[6]);
        assertEquals("C1", markers[7]);
        assertEquals("C2", markers[8]);
    }
} 
