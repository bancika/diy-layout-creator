package org.diylc.components.semiconductors;

public enum TransistorPinout {

  BJT_EBC, BJT_CBE, JFET_DSG, JFET_GSD, JFET_DGS, JFET_SGD, MOSFET_DSG, MOSFET_GSD, MOSFET_DGS, MOSFET_SGD;
  
  @Override
  public String toString() {
    return name().replace("_", " ");
  };
}
