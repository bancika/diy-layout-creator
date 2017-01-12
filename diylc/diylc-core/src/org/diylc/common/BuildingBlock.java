package org.diylc.common;

import java.util.Collection;

import org.diylc.core.IDIYComponent;

public class BuildingBlock {

  private String name;
  private Collection<IDIYComponent<?>> components;

  public BuildingBlock(String name, Collection<IDIYComponent<?>> components) {
    super();
    this.name = name;
    this.components = components;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Collection<IDIYComponent<?>> getComponents() {
    return components;
  }

  public void setComponents(Collection<IDIYComponent<?>> components) {
    this.components = components;
  }

  @Override
  public String toString() {
    return name;
  }
}
