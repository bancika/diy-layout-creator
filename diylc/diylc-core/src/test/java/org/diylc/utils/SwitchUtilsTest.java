package org.diylc.utils;

import org.diylc.core.ISwitch;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

public class SwitchUtilsTest {

    // Mock switch implementation for testing
    private static class MockSwitch implements ISwitch {
        private final List<int[]> connections;
        private final Integer selectedPosition;
        private final boolean renderConnected;

        public MockSwitch(List<int[]> connections, Integer selectedPosition, boolean renderConnected) {
            this.connections = connections;
            this.selectedPosition = selectedPosition;
            this.renderConnected = renderConnected;
        }

        @Override
        public boolean arePointsConnected(int index1, int index2, int position) {
            if (position != selectedPosition) return false;
            return connections.stream()
                .anyMatch(conn -> (conn[0] == index1 && conn[1] == index2) || 
                                (conn[0] == index2 && conn[1] == index1));
        }

        @Override
        public Integer getSelectedPosition() {
            return selectedPosition;
        }

        @Override
        public Boolean getHighlightConnectedTerminals() {
            return renderConnected;
        }

        // Other interface methods not needed for testing
        @Override public int getPositionCount() { return 0; }
        @Override public String getPositionName(int position) { return null; }
    }

    @Test
    public void testNoConnections() {
        MockSwitch mockSwitch = new MockSwitch(Collections.emptyList(), 0, true);
        List<Set<Integer>> groups = SwitchUtils.getConnectedTerminals(mockSwitch, 5);
        assertTrue("Should return empty list when no connections", groups.isEmpty());
    }

    @Test
    public void testSingleConnection() {
        List<int[]> connections = Arrays.asList(new int[]{0, 1});
        MockSwitch mockSwitch = new MockSwitch(connections, 0, true);
        List<Set<Integer>> groups = SwitchUtils.getConnectedTerminals(mockSwitch, 5);
        
        assertEquals("Should have one group", 1, groups.size());
        assertTrue("Group should contain points 0 and 1", 
            groups.get(0).containsAll(Arrays.asList(0, 1)));
    }

    @Test
    public void testTransitiveConnections() {
        // Test case where 1-2-3 are connected through 2
        List<int[]> connections = Arrays.asList(
            new int[]{1, 2},
            new int[]{2, 3}
        );
        MockSwitch mockSwitch = new MockSwitch(connections, 0, true);
        List<Set<Integer>> groups = SwitchUtils.getConnectedTerminals(mockSwitch, 5);
        
        assertEquals("Should have one group", 1, groups.size());
        assertTrue("Group should contain points 1, 2, and 3", 
            groups.get(0).containsAll(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void testMultipleGroups() {
        // Test case with two separate groups: {1,2,3} and {4,6,7}
        List<int[]> connections = Arrays.asList(
            new int[]{2, 3},
            new int[]{1, 3},
            new int[]{4, 7},
            new int[]{4, 6}
        );
        MockSwitch mockSwitch = new MockSwitch(connections, 0, true);
        List<Set<Integer>> groups = SwitchUtils.getConnectedTerminals(mockSwitch, 8);
        
        assertEquals("Should have two groups", 2, groups.size());
        assertTrue("Groups should contain {1,2,3} and {4,6,7}",
            groups.stream().anyMatch(g -> g.containsAll(Arrays.asList(1, 2, 3))) &&
            groups.stream().anyMatch(g -> g.containsAll(Arrays.asList(4, 6, 7))));
    }

    @Test
    public void testRenderConnectedFalse() {
        List<int[]> connections = Arrays.asList(new int[]{0, 1});
        MockSwitch mockSwitch = new MockSwitch(connections, 0, false);
        List<Set<Integer>> groups = SwitchUtils.getConnectedTerminals(mockSwitch, 5);
        assertTrue("Should return empty list when renderConnected is false", groups.isEmpty());
    }

    @Test
    public void testNoSelectedPosition() {
        List<int[]> connections = Arrays.asList(new int[]{0, 1});
        MockSwitch mockSwitch = new MockSwitch(connections, null, true);
        List<Set<Integer>> groups = SwitchUtils.getConnectedTerminals(mockSwitch, 5);
        assertTrue("Should return empty list when no position selected", groups.isEmpty());
    }

    @Test
    public void testComplexConnections() {
        // Test case with multiple interconnected points
        List<int[]> connections = Arrays.asList(
            new int[]{0, 1},
            new int[]{1, 2},
            new int[]{2, 3},
            new int[]{3, 0},  // Creates a circle
            new int[]{4, 5},
            new int[]{5, 6},
            new int[]{7, 8}   // Separate pair
        );
        MockSwitch mockSwitch = new MockSwitch(connections, 0, true);
        List<Set<Integer>> groups = SwitchUtils.getConnectedTerminals(mockSwitch, 9);
        
        assertEquals("Should have three groups", 3, groups.size());
        assertTrue("Should have group with points 0,1,2,3",
            groups.stream().anyMatch(g -> g.containsAll(Arrays.asList(0, 1, 2, 3))));
        assertTrue("Should have group with points 4,5,6",
            groups.stream().anyMatch(g -> g.containsAll(Arrays.asList(4, 5, 6))));
        assertTrue("Should have group with points 7,8",
            groups.stream().anyMatch(g -> g.containsAll(Arrays.asList(7, 8))));
    }
} 
