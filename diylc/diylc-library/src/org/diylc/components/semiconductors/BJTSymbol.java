/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.components.semiconductors;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

import org.diylc.common.ObjectCache;
import org.diylc.components.Abstract3LegSymbol;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;

@ComponentDescriptor(name = "BJT", author = "Branislav Stojkovic", category = "Schematic Symbols",
    instanceNamePrefix = "Q", description = "Bipolar junction transistor schematic symbol",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_TAG_AND_VALUE, keywordTag = "Schematic")
public class BJTSymbol extends Abstract3LegSymbol {

  private static final long serialVersionUID = 1L;

  protected BJTPolarity polarity = BJTPolarity.NPN;

  public Shape[] getBody() {
    Shape[] body = new Shape[3];
    Point[] controlPoints = getControlPoints();
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;
    int pinSpacing = (int) PIN_SPACING.convertToPixels();

    GeneralPath polyline = new GeneralPath();

    polyline.moveTo(x + pinSpacing / 2, y - pinSpacing);
    polyline.lineTo(x + pinSpacing / 2, y + pinSpacing);

    body[0] = polyline;

    polyline = new GeneralPath();

    polyline.moveTo(x, y);
    polyline.lineTo(x + pinSpacing / 2, y);
    polyline.moveTo(x + pinSpacing / 2, y - pinSpacing / 2);
    polyline.lineTo(x + pinSpacing * 2, y - pinSpacing);
    polyline.lineTo(x + pinSpacing * 2, y - pinSpacing * 2);
    polyline.moveTo(x + pinSpacing / 2, y + pinSpacing / 2);
    polyline.lineTo(x + pinSpacing * 2, y + pinSpacing);
    polyline.lineTo(x + pinSpacing * 2, y + pinSpacing * 2);
    body[1] = polyline;

    Area arrow;
    double theta;
    if (polarity == BJTPolarity.NPN) {
      theta = Math.atan(1.0 / 3);
      arrow =
          new Area(new Polygon(new int[] {x + pinSpacing, x + pinSpacing, x + pinSpacing * 10 / 6}, new int[] {
              y - pinSpacing / 5 + pinSpacing / 2, y + pinSpacing / 5 + pinSpacing / 2, y + pinSpacing / 2}, 3));
      arrow.transform(AffineTransform.getRotateInstance(theta, x + pinSpacing / 2, y + pinSpacing / 2));
    } else {
      theta = -Math.atan(1.0 / 3);
      arrow =
          new Area(
              new Polygon(new int[] {x + pinSpacing, x + pinSpacing * 10 / 6, x + pinSpacing * 10 / 6}, new int[] {
                  y - pinSpacing / 2, y - pinSpacing / 5 - pinSpacing / 2, y + pinSpacing / 5 - pinSpacing / 2}, 3));
      arrow.transform(AffineTransform.getRotateInstance(theta, x + pinSpacing / 2, y - pinSpacing / 2));
    }
    body[2] = arrow;
      
    return body;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(COLOR);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    g2d.drawLine(width / 3, height / 5, width / 3, height * 4 / 5);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.drawLine(width / 8, height / 2, width / 3, height / 2);

    g2d.drawLine(width * 3 / 4, 1, width * 3 / 4, height / 4);
    g2d.drawLine(width / 3, height / 3 + 1, width * 3 / 4, height / 4);

    g2d.drawLine(width * 3 / 4, height - 1, width * 3 / 4, height * 3 / 4);
    g2d.drawLine(width / 3, height * 2 / 3 - 1, width * 3 / 4, height * 3 / 4);
  }

  @EditableProperty(name = "Polarity")
  public BJTPolarity getPolarity() {
    return polarity;
  }

  public void setPolarity(BJTPolarity polarity) {
    this.polarity = polarity;

    body = null;
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    return getName() + "." + index;
  }
}
