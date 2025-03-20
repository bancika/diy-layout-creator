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
package org.diylc.components.tube;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.diylc.common.ObjectCache;
import org.diylc.components.transform.TubeSymbolTransformer;
import org.diylc.core.IDIYComponent;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;

@ComponentDescriptor(name = "Pentode", author = "Branislav Stojkovic", category = "Schematic Symbols",
    instanceNamePrefix = "V", description = "Pentode tube symbol", 
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE,
    transformer = TubeSymbolTransformer.class)
public class PentodeSymbol extends AbstractTubeSymbol {

  private static final long serialVersionUID = 1L;

  protected boolean exposeSuppressorGrid = true;

  public PentodeSymbol() {
    super();
    this.controlPoints =
        new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0),
            new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
    updateControlPoints();
  }

  public Shape[] initializeBody() {
    if (body == null) {
      Point2D[] controlPoints = initializeControlPoints(this.controlPoints[0]);

      body = new Shape[3];
      double x = controlPoints[0].getX();
      double y = controlPoints[0].getY();
      double pinSpacing = PIN_SPACING.convertToPixels();

      // electrodes
      GeneralPath polyline = new GeneralPath();

      // grid 1
      polyline.moveTo(x + pinSpacing * 5 / 4, y - pinSpacing * 3 / 8);
      polyline.lineTo(x + pinSpacing * 7 / 4, y - pinSpacing * 3 / 8);
      polyline.moveTo(x + pinSpacing * 9 / 4, y - pinSpacing * 3 / 8);
      polyline.lineTo(x + pinSpacing * 11 / 4, y - pinSpacing * 3 / 8);
      polyline.moveTo(x + pinSpacing * 13 / 4, y - pinSpacing * 3 / 8);
      polyline.lineTo(x + pinSpacing * 15 / 4, y - pinSpacing * 3 / 8);
      polyline.moveTo(x + pinSpacing * 17 / 4, y - pinSpacing * 3 / 8);
      polyline.lineTo(x + pinSpacing * 19 / 4, y - pinSpacing * 3 / 8);

      // grid 2
      polyline.moveTo(x + pinSpacing * 5 / 4, y - pinSpacing);
      polyline.lineTo(x + pinSpacing * 7 / 4, y - pinSpacing);
      polyline.moveTo(x + pinSpacing * 9 / 4, y - pinSpacing);
      polyline.lineTo(x + pinSpacing * 11 / 4, y - pinSpacing);
      polyline.moveTo(x + pinSpacing * 13 / 4, y - pinSpacing);
      polyline.lineTo(x + pinSpacing * 15 / 4, y - pinSpacing);
      polyline.moveTo(x + pinSpacing * 17 / 4, y - pinSpacing);
      polyline.lineTo(x + pinSpacing * 19 / 4, y - pinSpacing);

      // grid 3
      polyline.moveTo(x + pinSpacing * 5 / 4, y - pinSpacing - pinSpacing * 5 / 8);
      polyline.lineTo(x + pinSpacing * 7 / 4, y - pinSpacing - pinSpacing * 5 / 8);
      polyline.moveTo(x + pinSpacing * 9 / 4, y - pinSpacing - pinSpacing * 5 / 8);
      polyline.lineTo(x + pinSpacing * 11 / 4, y - pinSpacing - pinSpacing * 5 / 8);
      polyline.moveTo(x + pinSpacing * 13 / 4, y - pinSpacing - pinSpacing * 5 / 8);
      polyline.lineTo(x + pinSpacing * 15 / 4, y - pinSpacing - pinSpacing * 5 / 8);
      polyline.moveTo(x + pinSpacing * 17 / 4, y - pinSpacing - pinSpacing * 5 / 8);
      polyline.lineTo(x + pinSpacing * 19 / 4, y - pinSpacing - pinSpacing * 5 / 8);

      // plate
      polyline.moveTo(x + pinSpacing * 3 / 2, y - pinSpacing * 9 / 4);
      polyline.lineTo(x + pinSpacing * 9 / 2, y - pinSpacing * 9 / 4);

      // cathode
      polyline.moveTo(x + pinSpacing * 2, y + pinSpacing * 3 / 8);
      polyline.lineTo(x + pinSpacing * 4, y + pinSpacing * 3 / 8);

      body[0] = polyline;

      // connectors
      polyline = new GeneralPath();

      // grid1
      polyline.moveTo(x, y);
      polyline.lineTo(x + pinSpacing, y);
      polyline.lineTo(x + pinSpacing * 5 / 4, y - pinSpacing * 3 / 8);

      // grid2
      polyline.moveTo(controlPoints[3].getX(), controlPoints[3].getY());
      polyline.lineTo(x + pinSpacing * 19 / 4, y - pinSpacing);

      // grid3
      if (exposeSuppressorGrid) {
        polyline.moveTo(controlPoints[4].getX(), controlPoints[4].getY());
        polyline.lineTo(x + pinSpacing, controlPoints[4].getY());
        polyline.lineTo(x + pinSpacing * 5 / 4, y - pinSpacing - pinSpacing * 5 / 8);
      } else {
        polyline.moveTo(x + pinSpacing * 19 / 4, y - pinSpacing - pinSpacing * 5 / 8);
        polyline.lineTo(x + pinSpacing * 5, y - pinSpacing - pinSpacing * 5 / 8);
        polyline.lineTo(x + pinSpacing * 5, y - pinSpacing * 5 / 4);
        polyline.curveTo(x + pinSpacing * 21 / 4, y - pinSpacing * 5 / 4, x + pinSpacing * 21 / 4, y - pinSpacing * 3
            / 4, x + pinSpacing * 5, y - pinSpacing * 3 / 4);
        polyline.moveTo(x + pinSpacing * 5, y - pinSpacing * 3 / 4);
        polyline.curveTo(x + pinSpacing * 5, y + pinSpacing * 3 / 8, x + pinSpacing * 5, y + pinSpacing * 3 / 8, x
            + pinSpacing * 4, y + pinSpacing * 3 / 8);
      }

      // plate
      polyline.moveTo(controlPoints[1].getX(), controlPoints[1].getY());
      polyline.lineTo(x + pinSpacing * 3, y - pinSpacing * 9 / 4);

      // cathode
      polyline.moveTo(controlPoints[2].getX(), controlPoints[2].getY());
      polyline.lineTo(x + pinSpacing * 2, y + pinSpacing * 3 / 8);

      if (showHeaters) {
        polyline.moveTo(controlPoints[5].getX(), controlPoints[5].getY());
        polyline.lineTo(controlPoints[5].getX(), controlPoints[5].getY() - pinSpacing * 6 / 8);
        polyline.lineTo(controlPoints[5].getX() + pinSpacing / 2, controlPoints[5].getY() - pinSpacing * 10 / 8);

        polyline.moveTo(controlPoints[6].getX(), controlPoints[6].getY());
        polyline.lineTo(controlPoints[6].getX(), controlPoints[6].getY() - pinSpacing * 6 / 8);
        polyline.lineTo(controlPoints[6].getX() - pinSpacing / 2, controlPoints[6].getY() - pinSpacing * 10 / 8);
      }

      body[1] = polyline;

      // bulb
      body[2] = new Ellipse2D.Double(x + pinSpacing / 2, y - pinSpacing * 7 / 2, pinSpacing * 5, pinSpacing * 5);
    }
    return body;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(COLOR);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    g2d.drawLine(width / 4, height / 4, width * 3 / 4, height / 4);
    g2d.drawLine(width / 2, height / 4, width / 2, 0);

    g2d.drawLine(width / 4 + 2 * width / 32, height * 3 / 4, width * 3 / 4 - 4 * width / 32, height * 3 / 4);
    g2d.drawLine(width / 4 + 2 * width / 32, height * 3 / 4, width / 4 + 2 * width / 32, height - 1);

    g2d.drawOval(1, 1, width - 1 - 2 * width / 32, height - 1 - 2 * width / 32);

    g2d.drawLine(0, height / 2, width / 8, height / 2);
    g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[] {3f}, 6f));
    g2d.drawLine(width / 8, height / 2, width * 7 / 8, height / 2);
    g2d.drawLine(width / 8, height * 3 / 8, width * 7 / 8, height * 3 / 8);
    g2d.drawLine(width / 8, height * 5 / 8, width * 7 / 8, height * 5 / 8);
  }

  protected Point2D[] initializeControlPoints(Point2D first) {
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    // Update control points.
    double x = first.getX();
    double y = first.getY();

    Point2D[] newPoints =
        new Point2D[] {first, 
            new Point2D.Double(x + pinSpacing * 3, y - pinSpacing * 4), 
            new Point2D.Double(x + pinSpacing * 2, y + pinSpacing * 2), 
            new Point2D.Double(x + pinSpacing * 6, y - pinSpacing), 
            new Point2D.Double(x, y - pinSpacing * 2), 
            new Point2D.Double(x + pinSpacing * 3, y + pinSpacing * 2),
            new Point2D.Double(x + pinSpacing * 4, y + pinSpacing * 2)};

    return newPoints;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    if (showHeaters) {
      return !exposeSuppressorGrid && index == 4 ? VisibilityPolicy.NEVER : VisibilityPolicy.WHEN_SELECTED;
    } else {
      return index >= 5 || (!exposeSuppressorGrid && index == 4) ? VisibilityPolicy.NEVER
          : VisibilityPolicy.WHEN_SELECTED;
    }
  }

  @Override
  public boolean isControlPointSticky(int index) {
    if (showHeaters) {
      return exposeSuppressorGrid ? true : index != 4;
    } else {
      return exposeSuppressorGrid ? index < 5 : index < 4;
    }
  }

  @EditableProperty(name = "Suppressor grid")
  public boolean getExposeSuppressorGrid() {
    return exposeSuppressorGrid;
  }

  public void setExposeSuppressorGrid(boolean exposeSuppressorGrid) {
    this.exposeSuppressorGrid = exposeSuppressorGrid;

    this.body = null;
  }
}
