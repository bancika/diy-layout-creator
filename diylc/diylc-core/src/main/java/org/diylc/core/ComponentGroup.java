package org.diylc.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ComponentGroup implements Serializable {

  private Set<UUID> componentIds;
  private String name;

  public ComponentGroup(Set<UUID> componentIds) {
    this.componentIds = componentIds;
  }

  public ComponentGroup(Set<UUID> componentIds, String name) {
    this.componentIds = componentIds;
    this.name = name;
  }

  public static ComponentGroup from(Collection<IDIYComponent<?>> components, String name) {
    Set<UUID> componentIds = components.stream()
        .map(IDIYComponent::getId)
        .filter(id -> id != null)
        .collect(Collectors.toSet());
    return new ComponentGroup(componentIds, name);
  }

  public Set<UUID> getComponentIds() {
    return componentIds;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
