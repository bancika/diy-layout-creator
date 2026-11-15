package org.diylc.core.gerber;

import com.bancika.gerberwriter.DataLayer;
import com.bancika.gerberwriter.DrillLayer;
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
    return new DataLayer(function, negative, createGenerationSoftware(diylcVersion));
  }

  public DrillLayer buildDrillLayer(String diylcVersion) {
    return new DrillLayer(this == DrillPlated, createGenerationSoftware(diylcVersion));
  }

  /**
   * @return true for the drill layers, which hold holes rather than drawn copper or graphics
   */
  public boolean isDrill() {
    return this == DrillPlated || this == DrillNonPlated;
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
    return formatFileName(fileNameBase, boardName, getExtension());
  }

  public String formatFileName(String fileNameBase, String boardName, String extension) {
    return fileNameBase + (fileNameBase.endsWith(".") ? "" : ".")
        + boardName + "-" + getSuffix() + "." + extension;
  }

  private static GenerationSoftware createGenerationSoftware(String diylcVersion) {
    return new GenerationSoftware("bancika", "DIY Layout Creator", diylcVersion);
  }
}
