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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import org.diylc.common.ObjectCache;
import org.diylc.components.transform.TubeSymbolTransformer;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IPropertyValidator;
import org.diylc.core.ValidationException;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;

@ComponentDescriptor(name = "Diode (symbol)", author = "Branislav Stojkovic", category = "Schematic Symbols",
    instanceNamePrefix = "V", description = "Diode tube symbol", stretchable = false, zOrder = IDIYComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_VALUE, transformer = TubeSymbolTransformer.class)
public class DiodeSymbol extends AbstractTubeSymbol {

  private static final long serialVersionUID = 1L;

  protected boolean directlyHeated = false;

  public DiodeSymbol() {
    super();
    this.controlPoints =
        new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0)};
    updateControlPoints();
  }

  public Shape[] initializeBody() {
    if (body == null) {
      Point[] controlPoints = initializeControlPoints(this.controlPoints[0]);

      body = new Shape[3];
      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int pinSpacing = (int) PIN_SPACING.convertToPixels();

      // electrodes
      GeneralPath polyline = new GeneralPath();

      // plate
      polyline.moveTo(x + pinSpacing * 3 / 2, y - pinSpacing);
      polyline.lineTo(x + pinSpacing * 9 / 2, y - pinSpacing);

      // cathode
      if (directlyHeated) {
        polyline.moveTo(controlPoints[2].x, controlPoints[2].y - pinSpacing);
        polyline.lineTo(controlPoints[2].x + pinSpacing, controlPoints[2].y - pinSpacing * 2);
        polyline.lineTo(controlPoints[4].x, controlPoints[4].y - pinSpacing);
      } else {
        polyline.moveTo(x + pinSpacing * 2, y + pinSpacing);
        polyline.lineTo(x + pinSpacing * 4, y + pinSpacing);
      }

      body[0] = polyline;

      // connectors
      polyline = new GeneralPath();

      // plate
      polyline.moveTo(controlPoints[1].x, controlPoints[1].y);
      polyline.lineTo(x + pinSpacing * 3, y - pinSpacing);

      // cathode
      if (directlyHeated) {
        polyline.moveTo(controlPoints[2].x, controlPoints[2].y);
        polyline.lineTo(controlPoints[2].x, controlPoints[2].y - pinSpacing);

        polyline.moveTo(controlPoints[4].x, controlPoints[4].y);
        polyline.lineTo(controlPoints[4].x, controlPoints[4].y - pinSpacing);
      } else {
        polyline.moveTo(controlPoints[2].x, controlPoints[2].y);
        polyline.lineTo(x + pinSpacing * 2, y + pinSpacing);

        if (showHeaters) {
          polyline.moveTo(controlPoints[3].x, controlPoints[3].y);
          polyline.lineTo(controlPoints[3].x, controlPoints[3].y - pinSpacing);
          polyline.lineTo(controlPoints[3].x + pinSpacing / 2, controlPoints[3].y - 3 * pinSpacing / 2);

          polyline.moveTo(controlPoints[4].x, controlPoints[4].y);
          polyline.lineTo(controlPoints[4].x, controlPoints[4].y - pinSpacing);
          polyline.lineTo(controlPoints[4].x - pinSpacing / 2, controlPoints[4].y - 3 * pinSpacing / 2);
        }
      }

      body[1] = polyline;

      // bulb
      body[2] = new Ellipse2D.Double(x + pinSpacing / 2, y - pinSpacing * 5 / 2, pinSpacing * 5, pinSpacing * 5);
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
  }

  @Override
  protected Point[] initializeControlPoints(Point first) {
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    // Update control points.
    int x = first.x;
    int y = first.y;

    Point[] newPoints = new Point[] {first, new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0)};

    newPoints[1].x = x + pinSpacing * 3;
    newPoints[1].y = y - pinSpacing * 3;

    newPoints[2].x = x + pinSpacing * 2;
    newPoints[2].y = y + pinSpacing * 3;

    newPoints[3].x = x + pinSpacing * 3;
    newPoints[3].y = y + pinSpacing * 3;

    newPoints[4].x = x + pinSpacing * 4;
    newPoints[4].y = y + pinSpacing * 3;

    return newPoints;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    if (directlyHeated) {
      return index > 0 && index != 3 ? VisibilityPolicy.WHEN_SELECTED : VisibilityPolicy.NEVER;
    } else if (showHeaters) {
      return index > 0 ? VisibilityPolicy.WHEN_SELECTED : VisibilityPolicy.NEVER;
    } else {
      return index < 3 && index > 0 ? VisibilityPolicy.WHEN_SELECTED : VisibilityPolicy.NEVER;
    }
  }

  @Override
  public boolean isControlPointSticky(int index) {
    if (directlyHeated)
      return index > 0 && index != 3;
    else if (showHeaters) {
      return index > 0;
    } else {
      return index > 0 && index < 3;
    }
  }

  @EditableProperty(name = "Directly heated")
  public boolean getDirectlyHeated() {
    return directlyHeated;
  }

  public void setDirectlyHeated(boolean directlyHeated) {
    this.directlyHeated = directlyHeated;

    this.body = null;
  }

  public class HeaterValidator implements IPropertyValidator {

    @Override
    public void validate(Object value) throws ValidationException {
      if (value != null && value instanceof Boolean) {
        boolean b = (Boolean) value;
        if (!b && getDirectlyHeated()) {
          throw new ValidationException("Must show heaters for directly heated tubes.");
        }
      }
    }
  }
}
