package org.diylc.components.passive;

public enum InductorCore {

  Ferrite("Ferrite"),
  Air("Air");

  private String name;

  private InductorCore(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

}
