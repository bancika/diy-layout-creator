package org.diylc.core.gerber;

import com.bancika.gerberwriter.DataLayer;
import com.bancika.gerberwriter.GenerationSoftware;

public enum GerberLayer {
  Outline("Profile,NP", "gko", false),
  Drill("NonPlated,1,2,NPTH", "drl", false),
  CopperTop("Copper,L1,Top,Signal", "gtl", false),
  SilkscreenTop("", "", false),
  SolderMaskTop("Soldermask,Top", "gts", true);
  
  private String function;
  private String extension;
  private boolean negative;

  private GerberLayer(String function, String extension, boolean negative) {
    this.function = function;
    this.extension = extension;
    this.negative = negative;
    
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
}
