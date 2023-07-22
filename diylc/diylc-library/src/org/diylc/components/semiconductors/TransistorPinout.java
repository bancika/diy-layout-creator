package org.diylc.components.semiconductors;

public enum TransistorPinout {

  BJT_EBC, BJT_CBE, BJT_BCE, JFET_DSG, JFET_GSD, JFET_DGS, JFET_SGD, JFET_GDS, MOSFET_DSG, MOSFET_GSD, MOSFET_DGS, MOSFET_SGD, MOSFET_GDS, REGULATOR_IGO, REGULATOR_OGI, REGULATOR_GOI, REGULATOR_AOI, REGULATOR_GIO;
  
  @Override
  public String toString() {
    String[] parts = name().split("_");
    StringBuilder sb = new StringBuilder();    
    for (int i = 0; i < parts[1].length(); i++) {
      if (i > 0)
        sb.append("-");
      sb.append(parts[1].charAt(i));
    }
    sb.append("     ").append(parts[0]);
    return sb.toString();
  };
  
  public String toPinout() {
    String[] parts = name().split("_");
    return parts[1];
  }
}
