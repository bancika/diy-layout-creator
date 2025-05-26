/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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

  public static String[] getSwitchingMarkers(ISwitch switchComponent, int numPoints, boolean verbose) {
    String[] markers = new String[numPoints];
    List<List<Set<Integer>>> switchingMatrix = new ArrayList<>();
    for (int p = 0; p < switchComponent.getPositionCount(); p++) {
      List<Set<Integer>> connectedPoints = new ArrayList<>();
      switchingMatrix.add(connectedPoints);
      for (int i = 0; i < numPoints - 1; i++) {
        for (int j = i + 1; j < numPoints; j++) {
          if (switchComponent.arePointsConnected(i, j, p)) {
            connectedPoints.add(Set.of(i, j));
          }
        }
      }
    }
    List<Integer> commonPoints = new ArrayList<>();
    for (int i = 0; i < numPoints; i++) {
      final int finalI = i;
      if (!switchingMatrix.stream().allMatch(
          l -> l.isEmpty()) &&
          switchingMatrix.stream().allMatch(
          l -> l.isEmpty() ||
              l.stream().anyMatch(
              s -> s.contains(finalI)))) {
        markers[i] = String.valueOf((char)((int)'A' + commonPoints.size()));
        commonPoints.add(i);
      }
    }
    for (int p = 0; p < switchComponent.getPositionCount(); p++) {
      for (int c : commonPoints) {
        for (int i = 0; i < numPoints; i++) {
          if (commonPoints.contains(i)) {
            continue;
          }
          if (switchComponent.arePointsConnected(Math.min(c, i), Math.max(c, i), p)) {
            String marker = verbose ? markers[c] + (p + 1) : Integer.toString(p + 1);
            if (markers[i] == null) {
              markers[i] = marker;
            } else {
              markers[i] += "," + marker;
            }
          }
        }
      }
    }
    return markers;
  }
}
