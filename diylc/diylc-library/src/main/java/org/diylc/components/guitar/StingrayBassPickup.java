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
package org.diylc.components.guitar;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.OrientationHV;
import org.diylc.components.transform.GuitarPickupTransformer;
import org.diylc.core.*;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

import java.awt.*;
import java.awt.geom.*;

@ComponentDescriptor(name = "Stingray Bass Pickup", category = "Guitar", author = "JD",
    description = "Humbucker Bass Pickup for Musicman Stingray and similar guitars",
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "PKP", autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "Guitar Wiring Diagram",
    enableCache = true, transformer = GuitarPickupTransformer.class)

/**
 * Component type for Stingray Bass Humbucker
 * Modified from the P Bass Pickup. Author: JD
 * @author JD
 */ public class StingrayBassPickup extends AbstractGuitarPickup {

  private static final long serialVersionUID = 1L;

  private static Color BODY_COLOR = Color.decode("#333333");

  private static Size WIDTH = new Size(1.910, SizeUnit.in);
  private static Size LENGTH = new Size(3.505, SizeUnit.in);
  private static Size EDGE_RADIUS = new Size(0.08d, SizeUnit.in);

  private static Size LIP_RADIUS = new Size(0.4d, SizeUnit.in);
  private static Size LIP_HOLE_SIZE = new Size(0.1d, SizeUnit.in);
  private static Size LIP_HOLE_SPACING = new Size(0.05d, SizeUnit.in);
  private static Size LIP_DISTANCE_VERTICAL = new Size(0.935d, SizeUnit.in);
      // Left side vertical between mount points
  // private static Size LIP_DISTANCE_HORIZONTAL = new Size(3.7000d, SizeUnit.in); // Distance across pickup

  private static Size POINT_MARGIN = new Size(1.5d, SizeUnit.mm);

  private static Size POLE_SIZE = new Size(.39d, SizeUnit.in);
  private static Size POLE_SPACING_HORIZONTAL = new Size(0.75366d, SizeUnit.in);
      // Distance Between poles
  private static Size POLE_SPACING_TO_EDGE = new Size(0.4875d, SizeUnit.in);
      // Outer edge to centre of nearest pole
  // (Perpendicular of course!)
  private Color color = BODY_COLOR;
  private Color poleColor = METAL_COLOR;

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {

    Shape[] body = getBody();
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    Composite oldComposite = g2d.getComposite();

    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }

    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : color);
    g2d.fill(body[0]);
    g2d.fill(body[1]);
    drawingObserver.stopTracking();
    g2d.setComposite(oldComposite);

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance()
          .readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ?
              SELECTION_COLOR :
              theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ?
              SELECTION_COLOR :
              color.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);
    g2d.draw(body[1]);

    markContactPoints(g2d, drawingObserver);

    if (!outlineMode) {
      g2d.setColor(getPoleColor());
      g2d.fill(body[3]);
      g2d.setColor(darkerOrLighter(getPoleColor()));
      g2d.draw(body[3]);
    }

    drawMainLabel(g2d, project, outlineMode, componentState);

    drawTerminalLabels(g2d, finalBorderColor, project);
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[4];

      Point2D[] points = getControlPoints();
      double x = points[0].getX();
      double y = points[0].getY();
      int width = (int) WIDTH.convertToPixels();
      int length = (int) LENGTH.convertToPixels();
      int edgeRadius = (int) EDGE_RADIUS.convertToPixels();
      int pointMargin = (int) POINT_MARGIN.convertToPixels();
      int lipRadius = (int) LIP_RADIUS.convertToPixels();
      int lipHoleSize = (int) LIP_HOLE_SIZE.convertToPixels();
      int lipHoleSpacing = getClosestOdd(LIP_HOLE_SPACING.convertToPixels());
      int lipDistanceV = (int) LIP_DISTANCE_VERTICAL.convertToPixels();

      // This is the main body
      body[0] = new Area(
          new RoundRectangle2D.Double(x - length, y - pointMargin, length, width, edgeRadius,
              edgeRadius));

      // Lips around pickup - change transforms
      Area lip =
          new Area(new Ellipse2D.Double(-lipRadius / 2, -lipRadius / 2, lipRadius, lipRadius));
      lip.subtract(new Area(
          new Ellipse2D.Double(-lipRadius / 2 + lipHoleSpacing, -lipRadius / 4 + lipHoleSize / 2,
              lipHoleSize, lipHoleSize)));
      lip.transform(AffineTransform.getTranslateInstance(x - length,
          y - pointMargin + (width - lipDistanceV) / 2));

      Area lip2 =
          new Area(new Ellipse2D.Double(-lipRadius / 2, -lipRadius / 2, lipRadius, lipRadius));
      lip2 = new Area(new Ellipse2D.Double(-lipRadius / 2, -lipRadius / 2, lipRadius, lipRadius));
      lip2.subtract(new Area(new Ellipse2D.Double(lipRadius / 2 - lipHoleSize - lipHoleSpacing,
          -lipRadius / 4 + lipHoleSize / 2, lipHoleSize, lipHoleSize)));
      lip2.transform(AffineTransform.getTranslateInstance(x, y - pointMargin + width / 2));
      lip.add(lip2);

      Area lip3 =
          new Area(new Ellipse2D.Double(-lipRadius / 2, -lipRadius / 2, lipRadius, lipRadius));
      lip3.subtract(new Area(
          new Ellipse2D.Double(-lipRadius / 2 + lipHoleSpacing, -lipRadius / 4 + lipHoleSize / 2,
              lipHoleSize, lipHoleSize)));
      lip3.transform(AffineTransform.getTranslateInstance(x - length,
          (y - pointMargin) + (width - ((width - lipDistanceV) / 2))));
      lip.add(lip3);

      body[1] = new Area(lip);

      // Merge
      ((Area) body[1]).subtract((Area) body[0]);

      // Poles
      int poleSize = (int) POLE_SIZE.convertToPixels();
      int poleSpacing = (int) POLE_SPACING_HORIZONTAL.convertToPixels();
      int poleSpacingEdge = (int) POLE_SPACING_TO_EDGE.convertToPixels();
      int poleMargin = (length - poleSpacing * 3) / 2; // Edge to centre of outer poles

      Area poleArea = new Area();
      for (int i = 0; i < 4; i++) {
        // Top Row
        Ellipse2D pole =
            new Ellipse2D.Double(x - length + poleMargin + i * poleSpacing - poleSize / 2,
                y + poleSpacingEdge - pointMargin - poleSize / 2, poleSize, poleSize);
        // Bottom Row
        Ellipse2D pole2 =
            new Ellipse2D.Double(x - length + poleMargin + i * poleSpacing - poleSize / 2,
                y - pointMargin - poleSize / 2 + (width - poleSpacingEdge), poleSize, poleSize);

        poleArea.add(new Area(pole));
        poleArea.add(new Area(pole2));
      }

      body[3] = poleArea;

      // Rotate if needed
      if (orientation != Orientation.DEFAULT) {
        // Set variables depending on required transform
        double theta = 0;
        switch (orientation) {
          case _90:
            theta = Math.PI / 2;
            break;
          case _180:
            theta = Math.PI;
            break;
          case _270:
            theta = Math.PI * 3 / 2;
            break;
        }
        // Do the rotation
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
        for (Shape shape : body) {
          Area area = (Area) shape;
          if (shape != null)
            area.transform(rotation);
        }
      } // end if for rotation
    } // end the if (body == null)
    return body;
  }

  @Override
  protected int getMainLabelYOffset() {
    return (int) (WIDTH.convertToPixels() / 2 - 20);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(Math.PI / 4, width / 2, height / 2);
    int bodyWidth = (int) (14f * width / 32);
    int bodyLength = (int) (26f * width / 32);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect((width - bodyWidth) / 2, (height - bodyLength) / 2, bodyWidth, bodyLength, 3,
        3);

    // Very neat solution, doesn't work for this :(
    // Could potentially make them shorter so they don't poke out both ends and then do three of them?

    int lipSize = (int) (4f * width / 32);
    // g2d.fillRoundRect((width - lipSize) / 2, (height - bodyLength) / 2 - lipSize + 1, lipSize, bodyLength + 2 * lipSize - 2, lipSize, lipSize);  
    g2d.fillRoundRect((width - lipSize) / 2, (height - bodyLength) / 2 - lipSize + 1, lipSize,
        lipSize, lipSize, lipSize); // Right, center
    g2d.fillRoundRect(9 * width / 32, bodyLength + ((height - bodyLength) / 2), lipSize, lipSize,
        lipSize, lipSize);
    g2d.fillRoundRect(18 * width / 32, bodyLength + ((height - bodyLength) / 2), lipSize, lipSize,
        lipSize, lipSize);
    //g2d.fillRoundRect((width - lipSize) / 2, 12 * width / 32, lipSize, lipSize,lipSize,lipSize);
    //g2d.fillRoundRect((width - lipSize) / 2, 20 * width / 32, lipSize, lipSize,lipSize,lipSize);

    g2d.setColor(BODY_COLOR.darker());
    g2d.drawRoundRect((width - bodyWidth) / 2, (height - bodyLength) / 2, bodyWidth, bodyLength, 3,
        3);

    // g2d.setColor(Color.gray);
    // g2d.drawLine(width / 2, 4 * width / 32, width / 2, 4 * width / 32);
    // g2d.drawLine(width / 2, height - 4 * width / 32, width / 2, height - 4 * width / 32);

    g2d.setColor(METAL_COLOR);
    int poleSize = 3;
    int poleSpacing = (int) (6d * width / 32);
    for (int i = 0; i < 4; i++) {
      // Top Row
      g2d.fillOval(11 * width / 32, 5 * width / 32 + i * poleSpacing, poleSize * width / 32,
          poleSize * width / 32);
      //g2d.fillOval((width - poleSize) / 2, (height - poleSpacing) / 2 + (i * poleSpacing / 3), poleSize, poleSize);
      // Bottom Row
      g2d.fillOval(17 * width / 32, 5 * width / 32 + i * poleSpacing, poleSize * width / 32,
          poleSize * width / 32);
    }
  }

  @Override
  protected OrientationHV getControlPointDirection() {
    return OrientationHV.VERTICAL;
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty(name = "Pole Color")
  public Color getPoleColor() {
    if (poleColor == null)
      poleColor = METAL_COLOR;
    return poleColor;
  }

  public void setPoleColor(Color poleColor) {
    this.poleColor = poleColor;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public boolean isHumbucker() {
    return true;
  }

  @Override
  public Polarity getPolarity() {
    // just to disable editor
    return super.getPolarity();
  }
}
