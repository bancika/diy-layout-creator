/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.utils;

import org.diylc.core.ISwitch;

import java.util.*;

public class SwitchUtils {

   public static List<Set<Integer>> getConnectedTerminals(ISwitch switchComponent, int numPoints) {
        List<Set<Integer>> connectedGroups = new ArrayList<>();
        
        // Only process if we have a selected position and should render connections
        if (switchComponent.getSelectedPosition() == null || !Boolean.TRUE.equals(switchComponent.getRenderConnectedTerminals())) {
            return connectedGroups;
        }

        // First, collect all direct connections
        List<int[]> connections = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            for (int j = i + 1; j < numPoints; j++) {
                if (switchComponent.arePointsConnected(i, j, switchComponent.getSelectedPosition())) {
                    connections.add(new int[]{i, j});
                }
            }
        }

        // Create a map to track which points are in which group
        Map<Integer, Set<Integer>> pointToGroup = new HashMap<>();
        
        // Process each connection
        for (int[] connection : connections) {
            int p1 = connection[0];
            int p2 = connection[1];
            
            Set<Integer> group1 = pointToGroup.get(p1);
            Set<Integer> group2 = pointToGroup.get(p2);
            
            if (group1 == null && group2 == null) {
                // Create new group
                Set<Integer> newGroup = new HashSet<>();
                newGroup.add(p1);
                newGroup.add(p2);
                pointToGroup.put(p1, newGroup);
                pointToGroup.put(p2, newGroup);
            } else if (group1 == null) {
                // Add p1 to p2's group
                group2.add(p1);
                pointToGroup.put(p1, group2);
            } else if (group2 == null) {
                // Add p2 to p1's group
                group1.add(p2);
                pointToGroup.put(p2, group1);
            } else if (group1 != group2) {
                // Merge groups
                group1.addAll(group2);
                for (Integer p : group2) {
                    pointToGroup.put(p, group1);
                }
            }
        }
        
        // Collect unique groups
        Set<Set<Integer>> uniqueGroups = new HashSet<>(pointToGroup.values());
        connectedGroups.addAll(uniqueGroups);
        
        return connectedGroups;
    }
}
