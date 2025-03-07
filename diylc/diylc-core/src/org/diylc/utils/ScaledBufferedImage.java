package org.diylc.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ScaledBufferedImage extends BufferedImage {
  
  private final double scaleFactor;

  public ScaledBufferedImage(int width, int height, int imageType, double scaleFactor) {
    super((int) Math.round(width * scaleFactor), (int) Math.round(height * scaleFactor), imageType);
    this.scaleFactor = scaleFactor;
  }

  @Override
  public Graphics2D createGraphics() {
    Graphics2D returnValue = super.createGraphics();
    returnValue.scale(scaleFactor, scaleFactor);
    return returnValue;
  }
}
