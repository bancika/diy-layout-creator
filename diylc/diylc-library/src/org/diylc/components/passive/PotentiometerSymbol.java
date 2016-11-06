package org.diylc.components.passive;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.components.Abstract3LegSymbol;
import org.diylc.components.semiconductors.SymbolFlipping;

@ComponentDescriptor(name = "Potentiometer Symbol", author = "MCbx", category = "Schematics",
    instanceNamePrefix = "VR", description = "Potentiometer symbol", stretchable = false,
    zOrder = IDIYComponent.COMPONENT, rotatable = true, keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Schematic")
public class PotentiometerSymbol extends Abstract3LegSymbol {

  private static final long serialVersionUID = 1L;

  public PotentiometerSymbol() {
    this.color = Color.blue;
  }

  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[3];
      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int pinSpacing = (int) PIN_SPACING.convertToPixels();

      GeneralPath polyline = new GeneralPath();
      polyline.moveTo(x + pinSpacing * 2, y - pinSpacing * 2);
      polyline.lineTo(x + pinSpacing * 2, y - pinSpacing * 2 + pinSpacing / 2);
      polyline.lineTo(x + pinSpacing * 2 - pinSpacing / 4, (y - pinSpacing * 2) + pinSpacing / 2 + pinSpacing / 4);
      polyline.lineTo(x + pinSpacing * 2 + pinSpacing / 4, (y - pinSpacing * 2) + 2 * pinSpacing / 2);
      polyline.lineTo(x + pinSpacing * 2 - pinSpacing / 4, (y - pinSpacing * 2) + 3 * pinSpacing / 2 - pinSpacing / 8);
      polyline.lineTo(x + pinSpacing * 2 + pinSpacing / 4, (y - pinSpacing * 2) + 4 * pinSpacing / 2 - pinSpacing / 4);
      polyline.lineTo(x + pinSpacing * 2 - pinSpacing / 4, (y - pinSpacing * 2) + 5 * pinSpacing / 2 - pinSpacing / 3
          - pinSpacing / 16);
      polyline.lineTo(x + pinSpacing * 2 + pinSpacing / 4, (y - pinSpacing * 2) + 6 * pinSpacing / 2 - pinSpacing / 2
          + pinSpacing / 16);
      polyline.lineTo(x + pinSpacing * 2 - pinSpacing / 4, (y - pinSpacing * 2) + 7 * pinSpacing / 2 - pinSpacing / 2
          - pinSpacing / 16);
      polyline.lineTo(x + pinSpacing * 2 + pinSpacing / 4, (y - pinSpacing * 2) + 7 * pinSpacing / 2 - pinSpacing / 2
          + pinSpacing / 4);
      polyline.lineTo(x + pinSpacing * 2, (y - pinSpacing * 2) + 7 * pinSpacing / 2);
      polyline.lineTo(x + pinSpacing * 2, y + pinSpacing * 2);
      polyline.moveTo(x, y);
      polyline.lineTo(x + pinSpacing * 2 - pinSpacing / 2, y);

      body[1] = polyline;
      polyline = new GeneralPath();

      body[0] = polyline;

      polyline = new GeneralPath();
      polyline.moveTo(x + pinSpacing * 2 - pinSpacing / 3, y);
      polyline.lineTo(x + pinSpacing * 2 - pinSpacing * 2 / 3, y - pinSpacing / 3);
      polyline.lineTo(x + pinSpacing * 2 - pinSpacing * 2 / 3, y + pinSpacing / 3);
      polyline.lineTo(x + pinSpacing * 2 - pinSpacing / 3, y);
      body[2] = polyline;
    }
    return body;
  }

  @Override
  protected int getLabelX(Rectangle2D shapeRect, Rectangle2D textRect, FontMetrics fontMetrics, boolean outlineMode) {
    int x = super.getLabelX(shapeRect, textRect, fontMetrics, outlineMode);
    if (getFlip() == SymbolFlipping.X)
      return x - (int) (PIN_SPACING.convertToPixels() / 2);
    return x + (int) PIN_SPACING.convertToPixels();
  }

  // @Override
  // protected int getLabelX(Rectangle2D shapeRect, Rectangle2D textRect, FontMetrics fontMetrics,
  // boolean outlineMode) {
  // int pinSpacing = (int) PIN_SPACING.convertToPixels();
  // if (controlPoints[0].x < controlPoints[1].x && controlPoints[0].x < controlPoints[2].x)
  // return controlPoints[1].x + pinSpacing / 3;
  // if (controlPoints[0].y < controlPoints[1].y && controlPoints[0].y < controlPoints[2].y)
  // return controlPoints[0].x - (int) (textRect.getWidth() / 2);
  // if (controlPoints[0].x > controlPoints[1].x && controlPoints[0].x > controlPoints[2].x)
  // return controlPoints[1].x - pinSpacing / 2 - (int) textRect.getWidth();
  // if (controlPoints[0].y > controlPoints[1].y && controlPoints[0].y > controlPoints[2].y)
  // return controlPoints[0].x - (int) (textRect.getWidth() / 2);
  // return 2 * ((int) (shapeRect.getWidth() - textRect.getWidth()) / 2 + (int) shapeRect.getX());
  // }
  //
  // @Override
  // protected int getLabelY(Rectangle2D shapeRect, Rectangle2D textRect, FontMetrics fontMetrics,
  // boolean outlineMode) {
  // int pinSpacing = (int) PIN_SPACING.convertToPixels();
  // if (controlPoints[0].x < controlPoints[1].x && controlPoints[0].x < controlPoints[2].x)
  // return controlPoints[0].y;
  // if (controlPoints[0].y < controlPoints[1].y && controlPoints[0].y < controlPoints[2].y)
  // return controlPoints[1].y + pinSpacing * 2 / 3;
  // if (controlPoints[0].x > controlPoints[1].x && controlPoints[0].x > controlPoints[2].x)
  // return controlPoints[0].y;
  // if (controlPoints[0].y > controlPoints[1].y && controlPoints[0].y > controlPoints[2].y)
  // return controlPoints[1].y - pinSpacing * 2 / 3;
  // return 2 * ((int) (shapeRect.getHeight() - textRect.getHeight()) / 2 + fontMetrics.getAscent()
  // + (int) shapeRect
  // .getY());
  // }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(Color.blue);
    g2d.drawLine(0, height / 2, 4, height / 2);
    g2d.drawLine(width - 4, height / 2, width, height / 2);
    g2d.drawPolyline(new int[] {4, 6, 10, 14, 18, 22, 26, 28}, new int[] {height / 2, height / 2 + 2, height / 2 - 2,
        height / 2 + 2, height / 2 - 2, height / 2 + 2, height / 2 - 2, height / 2}, 8);

    g2d.drawPolyline(new int[] {width / 2, width / 2 - 2}, new int[] {height, height / 2 + 2}, 2);

    g2d.fill(new Polygon(new int[] {width / 2 - 4, width / 2 + 2, width / 2 - 2}, new int[] {height / 2 + 6,
        height / 2 + 6, height / 2 + 2}, 3));
  }

}
