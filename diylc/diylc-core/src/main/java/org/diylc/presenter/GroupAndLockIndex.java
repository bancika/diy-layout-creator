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
package org.diylc.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.diylc.common.ComponentType;
import org.diylc.core.ComponentGroup;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;

/**
 * Answers which components are grouped together and which are locked. Both are read on every
 * repaint and every mouse move, so they are worked out once and kept until {@link #invalidate()}
 * is called, which must happen wherever the project's components, groups or locks may have
 * changed. Asking about a different project than last time rebuilds them as well.
 */
public class GroupAndLockIndex {

  private Project project;
  private Map<UUID, Set<IDIYComponent<?>>> groups;
  private Set<IDIYComponent<?>> lockedComponents;

  public void invalidate() {
    groups = null;
    lockedComponents = null;
  }

  /**
   * @param project
   * @param component
   * @return the project components that share a group with the specified component, matched by
   *         id, or just the component itself when it is not grouped. The set is read-only.
   */
  public Set<IDIYComponent<?>> findGroup(Project project, IDIYComponent<?> component) {
    checkProject(project);
    if (groups == null) {
      groups = buildGroups(project);
    }
    Set<IDIYComponent<?>> group = groups.get(component.getId());
    return group == null ? Collections.<IDIYComponent<?>>singleton(component) : group;
  }

  /**
   * @param project
   * @return the project components that are locked, either one by one or through their layer. The
   *         set is read-only.
   */
  public Set<IDIYComponent<?>> getLockedComponents(Project project) {
    checkProject(project);
    if (lockedComponents == null) {
      Set<IDIYComponent<?>> locked = new HashSet<IDIYComponent<?>>();
      for (IDIYComponent<?> component : project.getComponents()) {
        if (isLocked(project, component)) {
          locked.add(component);
        }
      }
      lockedComponents = Collections.unmodifiableSet(locked);
    }
    return lockedComponents;
  }

  @SuppressWarnings("unchecked")
  public static boolean isLocked(Project project, IDIYComponent<?> component) {
    ComponentType componentType = ComponentProcessor.getInstance()
        .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass());
    // for internal-use components
    if (componentType == null) {
      return false;
    }
    return project.getLockedLayers().contains((int) Math.round(componentType.getZOrder()))
        || project.getLockedComponents().contains(component);
  }

  private void checkProject(Project project) {
    if (project != this.project) {
      this.project = project;
      invalidate();
    }
  }

  // a component listed in more than one group keeps the first, as a search through the groups in
  // order would find it
  private static Map<UUID, Set<IDIYComponent<?>>> buildGroups(Project project) {
    Map<UUID, List<IDIYComponent<?>>> componentsById = new HashMap<UUID, List<IDIYComponent<?>>>();
    for (IDIYComponent<?> component : project.getComponents()) {
      componentsById.computeIfAbsent(component.getId(), id -> new ArrayList<IDIYComponent<?>>())
          .add(component);
    }
    Map<UUID, Set<IDIYComponent<?>>> groups = new HashMap<UUID, Set<IDIYComponent<?>>>();
    for (ComponentGroup group : project.getGroupsEx()) {
      if (group.getComponentIds() == null) {
        continue;
      }
      Set<IDIYComponent<?>> members = new HashSet<IDIYComponent<?>>();
      for (UUID id : group.getComponentIds()) {
        members.addAll(componentsById.getOrDefault(id, Collections.emptyList()));
      }
      Set<IDIYComponent<?>> readOnlyMembers = Collections.unmodifiableSet(members);
      for (UUID id : group.getComponentIds()) {
        groups.putIfAbsent(id, readOnlyMembers);
      }
    }
    return groups;
  }
}
