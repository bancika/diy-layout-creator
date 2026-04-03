package org.diylc.components.tube;

public enum TubeEnvelope {
  FULL("Full"),
  SECTION_A("Section A"),
  SECTION_B("Section B");

  private String name;

  private TubeEnvelope(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
