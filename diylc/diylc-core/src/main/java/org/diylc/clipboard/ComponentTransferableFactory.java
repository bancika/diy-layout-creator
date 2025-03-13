/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

import org.diylc.core.IDIYComponent;

public class ComponentTransferableFactory {
  
  private static final Logger LOG = Logger.getLogger(ComponentTransferableFactory.class);
 
  private static ComponentTransferableFactory instance;
  
  public static ComponentTransferableFactory getInstance() {
    if (instance == null)
      instance = new ComponentTransferableFactory();
    return instance;
  }
  
  public ComponentTransferable build(Collection<IDIYComponent<?>> selectedComponents, Set<Set<IDIYComponent<?>>> groups) {
    Set<IDIYComponent<?>> originalComponentSet = new HashSet<IDIYComponent<?>>(selectedComponents);
    List<IDIYComponent<?>> originalComponents = new ArrayList<IDIYComponent<?>>(selectedComponents);
    List<IDIYComponent<?>> clonedComponents = originalComponents.stream(
        ).map(x -> {
          try {
            return x.clone();
          } catch (CloneNotSupportedException e) {
            LOG.error(e);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    Set<Set<IDIYComponent<?>>> clonedGroups = new HashSet<Set<IDIYComponent<?>>>();
    
    for (Set<IDIYComponent<?>> group : groups) {
      if (group.isEmpty())
        continue;
      
      if (originalComponentSet.contains(group.iterator().next())) {
        Set<IDIYComponent<?>> clonedGroup = new HashSet<IDIYComponent<?>>();
        clonedGroups.add(clonedGroup);
        for (int i = 0; i < originalComponents.size(); i++) {
          if (group.contains(originalComponents.get(i))) {
            clonedGroup.add(clonedComponents.get(i));
          }
        }
      }
    }
    
    return new ComponentTransferable(clonedComponents, clonedGroups);
  }
}
