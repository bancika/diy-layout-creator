/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.netlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.diylc.core.IDIYComponent;

public class Netlist implements Comparable<Netlist> {

  private List<IDIYComponent<?>> components;
  private Set<Group> groups = new HashSet<Group>();
  private List<SwitchSetup> switchSetup = new ArrayList<SwitchSetup>();

  public Netlist(List<IDIYComponent<?>> components) {
    this.components = components;
  }

  public Set<Group> getGroups() {
    return groups;
  }

  public Netlist add(Group g) {
    groups.add(g);
    return this;
  }

  public List<IDIYComponent<?>> getComponents() {
    return components;
  }

  public List<Group> getSortedGroups() {
    List<Group> list = new ArrayList<Group>(groups);
    Collections.sort(list);
    return list;
  }

  public List<SwitchSetup> getSwitchSetup() {
    return switchSetup;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((groups == null) ? 0 : groups.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Netlist other = (Netlist) obj;
    if (groups == null) {
      if (other.groups != null)
        return false;
    } else if (!groups.equals(other.groups))
      return false;
    return true;
  }

  public String getSwitchSetupString() {
    return getSwitchSetup().isEmpty() ? "[ ALL ]" : "[ " +
        getSwitchSetup().stream().map(x -> x.toString()).collect(Collectors.joining(",")) + " ]";
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Switch setup: ").append(getSwitchSetupString()).append("\n\n");
    List<Group> list = getSortedGroups();
    for (Group g : list) {
      sb.append("\t").append(g).append("\n");
    }
    return sb.toString();
  }

  @Override
  public int compareTo(Netlist o) {
    return switchSetup.toString().compareToIgnoreCase(o.switchSetup.toString());
  }
}
