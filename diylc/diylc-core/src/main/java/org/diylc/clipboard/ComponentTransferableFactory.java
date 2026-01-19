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
 */
package org.diylc.clipboard;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

import org.diylc.core.ComponentGroup;
import org.diylc.core.IDIYComponent;

public class ComponentTransferableFactory {
  
  private static final Logger LOG = Logger.getLogger(ComponentTransferableFactory.class);
 
  private static ComponentTransferableFactory instance;
  
  public static ComponentTransferableFactory getInstance() {
    if (instance == null)
      instance = new ComponentTransferableFactory();
    return instance;
  }
  
  public ComponentTransferable build(Collection<IDIYComponent<?>> selectedComponents, Set<ComponentGroup> groups) {

    List<IDIYComponent<?>> originalComponents = new ArrayList<IDIYComponent<?>>(selectedComponents);

    Map<UUID, UUID> idMap = new HashMap<>();

    List<IDIYComponent<?>> clonedComponents = originalComponents.stream(
        ).map(x -> {
          try {
            IDIYComponent<?> clone = x.clone();
            clone.setId(UUID.randomUUID());
            idMap.put(x.getId(), clone.getId());
            return clone;
          } catch (CloneNotSupportedException e) {
            LOG.error(e);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    Set<ComponentGroup> clonedGroups = groups.stream()
        .map(group -> {
          Set<UUID> newIds = group.getComponentIds().stream()
              .map(idMap::get)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
          // Only create group if it has at least one valid ID
          return newIds.isEmpty() ? null : new ComponentGroup(newIds, group.getName());
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    
    return new ComponentTransferable(clonedComponents, clonedGroups);
  }
}
