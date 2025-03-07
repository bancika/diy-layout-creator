package org.diylc.core.gerber;

import com.bancika.gerberwriter.DataLayer;
import com.bancika.gerberwriter.GenerationSoftware;

public enum GerberLayer {
  
  Outline("Profile,NP", "gko", "Edge-Cuts", false, false),
  DrillNonPlated("NonPlated,1,2,NPTH", "drl", "NPTH", false, false),
  DrillPlated("Plated,1,2,PTH", "drl", "PTH", false, false),
  CopperBot("Copper,L1,Bot", "gbl", "B_Cu", false, false),
  CopperTop("Copper,L1,Top", "gtl", "T_Cu", false, false),
  SilkscreenBot("Legend,Bot", "gbo", "B_Silkscreen", false, false),
  SilkscreenTop("Legend,Top", "gto", "T_Silkscreen", false, false),
  SolderMaskBot("Soldermask,Bot", "gbs", "B_Mask", true, false),
  SolderMaskTop("Soldermask,Top", "gts", "T_Mask", true, false);
  
  private String function;
  private String extension;
  private boolean negative;
  private boolean mirrored;
  private String suffix;

  private GerberLayer(String function, String extension, String suffix, boolean negative, boolean mirrored) {
    this.function = function;
    this.extension = extension;
    this.suffix = suffix;
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
  
  public String getSuffix() {
    return suffix;
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
  
  public String formatFileName(String fileNameBase, String boardName) {
    return fileNameBase + (fileNameBase.endsWith(".") ? "" : ".")
        + boardName + "-" + getSuffix() + "." + getExtension();
  }
}
