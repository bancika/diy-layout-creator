package org.diylc.core.gerber;

import com.bancika.gerberwriter.DataLayer;
import com.bancika.gerberwriter.GenerationSoftware;

public enum GerberLayer {
  
  Outline("Profile,NP", "gko", false, false),
  Drill("NonPlated,1,2,NPTH", "drl", false, false),
  CopperBot("Copper,L1,Bot", "gbl", false, true),
  CopperTop("Copper,L1,Top", "gtl", false, false),
  SilkscreenBot("Legend,Bot", "gbo", false, true),
  SilkscreenTop("Legend,Top", "gto", false, false),
  SolderMaskBot("Soldermask,Bot", "gbs", true, true),
  SolderMaskTop("Soldermask,Top", "gts", true, false);
  
  private String function;
  private String extension;
  private boolean negative;
  private boolean mirrored;

  private GerberLayer(String function, String extension, boolean negative, boolean mirrored) {
    this.function = function;
    this.extension = extension;
    this.negative = negative;
    this.mirrored = mirrored;
  }
  
  public DataLayer buildLayer(String diylcVersion) {
    GenerationSoftware genSoftware =
        new GenerationSoftware("bancika", "DIY Layout Creator", diylcVersion);
    return new DataLayer(function, negative, genSoftware);
  }
  
  public String getExtension() {
    return extension;
  }
  
  public String getFunction() {
    return function;
  }
  
  public boolean isNegative() {
    return negative;
  }
  
  public boolean isMirrored() {
    return mirrored;
  }
}
