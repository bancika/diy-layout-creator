package org.diylc.awt;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImageUtils {

  public static BufferedImage ToBufferedImage(Image image) {
    BufferedImage bi =
        new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = bi.createGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    return bi;
  }
}
