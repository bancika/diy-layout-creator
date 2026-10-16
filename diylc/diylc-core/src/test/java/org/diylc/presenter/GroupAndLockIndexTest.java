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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.diylc.core.ComponentGroup;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.testcomponents.MockDIYComponent;
import org.junit.Test;

public class GroupAndLockIndexTest {

  @Test
  public void testGroupMembersFoundById() {
    Project project = new Project();
    MockDIYComponent a = add(project);
    MockDIYComponent b = add(project);
    MockDIYComponent single = add(project);
    group(project, a, b);
    GroupAndLockIndex index = new GroupAndLockIndex();

    assertEquals(Set.of(a, b), index.findGroup(project, a));
    assertEquals(Set.of(a, b), index.findGroup(project, b));
    assertEquals(Set.of(single), index.findGroup(project, single));

    // a component outside the project, such as one being placed, is matched by its id
    MockDIYComponent copy = new MockDIYComponent();
    copy.setId(a.getId());
    assertEquals(Set.of(a, b), index.findGroup(project, copy));
  }

  @Test
  public void testGroupChangesSeenAfterInvalidation() {
    Project project = new Project();
    MockDIYComponent a = add(project);
    MockDIYComponent b = add(project);
    group(project, a, b);
    GroupAndLockIndex index = new GroupAndLockIndex();
    assertEquals(Set.of(a, b), index.findGroup(project, a));

    project.getGroupsEx().clear();
    index.invalidate();

    assertEquals(Set.of(a), index.findGroup(project, a));
  }

  @Test
  public void testAnotherProjectRebuildsWithoutInvalidation() {
    Project first = new Project();
    MockDIYComponent a = add(first);
    MockDIYComponent b = add(first);
    group(first, a, b);
    GroupAndLockIndex index = new GroupAndLockIndex();
    assertEquals(Set.of(a, b), index.findGroup(first, a));

    Project second = new Project();
    second.getComponents().add(a);

    assertEquals(Set.of(a), index.findGroup(second, a));
  }

  @Test
  public void testLockedComponentsIncludeLockedLayers() {
    Project project = new Project();
    MockDIYComponent a = add(project);
    MockDIYComponent b = add(project);
    GroupAndLockIndex index = new GroupAndLockIndex();
    assertTrue(index.getLockedComponents(project).isEmpty());

    project.getLockedComponents().add(a);
    index.invalidate();
    assertEquals(Set.of(a), index.getLockedComponents(project));

    project.getLockedLayers().add(IDIYComponent.COMPONENT);
    index.invalidate();
    assertEquals(Set.of(a, b), index.getLockedComponents(project));
  }

  private MockDIYComponent add(Project project) {
    MockDIYComponent component = new MockDIYComponent();
    project.getComponents().add(component);
    return component;
  }

  private void group(Project project, IDIYComponent<?>... components) {
    Set<UUID> ids = new HashSet<UUID>();
    for (IDIYComponent<?> component : components) {
      ids.add(component.getId());
    }
    project.getGroupsEx().add(new ComponentGroup(ids));
  }
}
