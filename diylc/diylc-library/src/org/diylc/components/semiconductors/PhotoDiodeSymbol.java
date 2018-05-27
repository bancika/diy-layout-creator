package org.diylc.components.semiconductors;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;

import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.KeywordPolicy;

@ComponentDescriptor(
	name = "PhotoDiode (symbol)", 
	author = "N9XYP", 
	category = "Schematics",
    creationMethod = CreationMethod.POINT_BY_POINT, 
	instanceNamePrefix = "D", 
	description = "PhotoDiode schematic symbol",
    zOrder = IDIYComponent.COMPONENT, 
	keywordPolicy = KeywordPolicy.SHOW_TAG, 
	keywordTag = "Schematic",
    transformer = SimpleComponentTransformer.class
)

public class PhotoDiodeSymbol extends DiodeSymbol {

  private static final long serialVersionUID = 1L;

@Override
 protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    super.decorateComponentBody(g2d, outlineMode);

// Draw arrows
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    double width = getWidth().convertToPixels();
    double arrowLength = width / 3;
    double arrowSize = width / 6;
    int d = (int) (width / 3);

// lower arrow
    int x2 = (int) (d / 2 + Math.cos(Math.PI / 4) * arrowLength);
    int y2 = (int) (width + Math.sin(Math.PI / 4) * arrowLength);
    g2d.drawLine(d / 2, (int) width, x2 , y2);
    g2d.fillPolygon(new Polygon(new int[] {d / 2, (int) ((d / 2) + arrowSize), d / 2}, new int[] {(int) width, (int) width, (int) (width + arrowSize)},3));

//upper arrow
    x2 = (int) (3 * d / 2 + Math.cos(Math.PI / 4) * arrowLength);
    y2 = (int) (width + Math.sin(Math.PI / 4) * arrowLength);
    g2d.drawLine(3 * d / 2, (int) width, x2 - 2, y2 - 2);
    g2d.fillPolygon(new Polygon(new int[] {3 * d / 2, (int) ((3 * d / 2) + arrowSize), 3 * d / 2}, new int[] {(int) width, (int) width, (int) (width + arrowSize)},3));
  }
  
  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    AffineTransform tx = g2d.getTransform();
    super.drawIcon(g2d, width, height);
    int arrowSize = 3 * width / 32;
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    // Go back to original transform
    g2d.setTransform(tx);
// draw two "arrows"
    g2d.drawLine(width * 9 / 16, height * 10 / 16, width * 9 / 16 + arrowSize, height * 10 / 16);
    g2d.drawLine(width * 9 / 16, height * 11 / 16, width * 9 / 16 + arrowSize, height * 11 / 16);
  }

}
