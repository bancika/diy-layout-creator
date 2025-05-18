/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.awt;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

public class ShadedPaint implements Paint {
  
  private ShadedPaintContext context;
  private Point2D p1;
  private Point2D p2;
  private Color c1;
  private Color c2;  
  
  public ShadedPaint(Point2D p1, Point2D p2, Color c, double shadeFactor) {
    this.p1 = p1;
    this.p2 = p2;
    int r = c.getRed();
    int g = c.getGreen();
    int b = c.getBlue();
    
    this.c1 = new Color(
        Math.max((int) (r * shadeFactor), 0), 
        Math.max((int) (g * shadeFactor), 0), 
        Math.max((int) (b * shadeFactor), 0));

    
    int i = (int)(1.0/(1.0-shadeFactor));
    if ( r == 0 && g == 0 && b == 0) {
       this.c2 = new Color(i, i, i);
    } else {
      if ( r > 0 && r < i ) r = i;
      if ( g > 0 && g < i ) g = i;
      if ( b > 0 && b < i ) b = i;
  
      this.c2 = new Color(Math.min((int)(r / shadeFactor), 255),
                       Math.min((int)(g / shadeFactor), 255),
                       Math.min((int)(b / shadeFactor), 255)); 
    }
  }

  public ShadedPaint(Point2D p1, Point2D p2, Color c2) {
    this(p1, p2, c2, 0.9f);
  }

  @Override
  public int getTransparency() {
    return OPAQUE;
  }

  @Override
  public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
      AffineTransform xform, RenderingHints hints) {
    if (context == null || !context.getColorModel().equals(cm))
          context = new ShadedPaintContext(cm, p1, p2, xform, c1, c2);
    return context;
  }

}
