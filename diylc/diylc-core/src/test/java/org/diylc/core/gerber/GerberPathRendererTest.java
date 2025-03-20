package org.diylc.core.gerber;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.Test;
import com.bancika.gerberwriter.DataLayer;
import com.bancika.gerberwriter.GenerationSoftware;

import org.diylc.core.gerber.GerberPathRenderer;

public class GerberPathRendererTest {
  
  @Test
  public void testCurveApproximation1() throws IOException {
    String text = "5pF";
    String font = "Square721 BT";
    Shape shape = textToShape(text, font);
    DataLayer dataLayer = new DataLayer("Copper.Top", false, new GenerationSoftware("v", "a", "ver"));
    GerberPathRenderer.outputPathArea(shape.getPathIterator(null), dataLayer, 0.1, false, "Conductor");
//    dataLayer.dumpGerberToFile(font + text + ".gbr");
  }

  private Shape textToShape(String text, String font) {
    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    Font f = new Font(font, Font.BOLD, 50);
    g2d.setFont(f);
    TextLayout layout = new TextLayout(text, f, g2d.getFontRenderContext());
    Shape shape = layout.getOutline(AffineTransform.getScaleInstance(1, -1));
    return shape;
  }
}
