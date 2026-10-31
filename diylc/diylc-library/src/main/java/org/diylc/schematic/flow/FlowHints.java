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
package org.diylc.schematic.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * What an {@link ISignalFlowRule} worked out about one component: the {@link FlowRole} of each of its
 * control points. Points the rule said nothing about read back as {@link FlowRole#NEUTRAL}.
 *
 * @author Branislav Stojkovic
 */
public class FlowHints {

  private final Map<Integer, FlowRole> roles;

  private FlowHints(Map<Integer, FlowRole> roles) {
    this.roles = Collections.unmodifiableMap(roles);
  }

  public static Builder builder() {
    return new Builder();
  }

  public FlowRole getRole(int pointIndex) {
    FlowRole role = roles.get(pointIndex);
    return role == null ? FlowRole.NEUTRAL : role;
  }

  public Map<Integer, FlowRole> getRoles() {
    return roles;
  }

  /**
   * @return true if at least one point is an input or an output. A set of hints that is entirely
   *         neutral says nothing about direction, so the analyzer treats it as no answer at all.
   */
  public boolean hasDirection() {
    for (FlowRole role : roles.values()) {
      if (role == FlowRole.INPUT || role == FlowRole.OUTPUT) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return roles.toString();
  }

  public static class Builder {

    private final Map<Integer, FlowRole> roles = new HashMap<Integer, FlowRole>();

    public Builder role(int pointIndex, FlowRole role) {
      roles.put(pointIndex, role);
      return this;
    }

    public Builder input(int pointIndex) {
      return role(pointIndex, FlowRole.INPUT);
    }

    public Builder output(int pointIndex) {
      return role(pointIndex, FlowRole.OUTPUT);
    }

    public Builder neutral(int pointIndex) {
      return role(pointIndex, FlowRole.NEUTRAL);
    }

    public FlowHints build() {
      return new FlowHints(roles);
    }
  }
}
