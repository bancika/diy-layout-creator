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
package org.diylc.components.guitar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.OrientationHV;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.RoundedPath;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Single Coil Pickup", category = "Guitar", author = "Branislav Stojkovic",
    description = "Single coil guitar pickup, both Strat and Tele style", stretchable = false,
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "PKP", autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "Guitar Wiring Diagram")
public class SingleCoilPickup extends AbstractSingleOrHumbuckerPickup {

  private static final long serialVersionUID = 1L;  

  private static Color BODY_COLOR = Color.white;
  private static Color BASE_COLOR = Color.gray;
  public static Color LUG_COLOR = Color.decode("#E0C04C");
//  private static Color POINT_COLOR = Color.lightGray;
  private static Size WIDTH = new Size(15.5d, SizeUnit.mm);
  private static Size LENGTH = new Size(83d, SizeUnit.mm);
  private static Size BASE_RADIUS = new Size(0.15d, SizeUnit.in);  
  private static Size LUG_DIAMETER = new Size(0.06d, SizeUnit.in);

  // strat-specific
  private static Size STRAT_LIP_WIDTH = new Size(5d, SizeUnit.mm);
  private static Size STRAT_LIP_LENGTH = new Size(20d, SizeUnit.mm);
  private static Size STRAT_INNER_LENGTH = new Size(70d, SizeUnit.mm);

  // tele-specific
  private static Size TELE_BASE_WIDTH = new Size(1.5d, SizeUnit.in);
  private static Size TELE_LIP_LENGTH = new Size(1.735d, SizeUnit.in);
  private static Size TELE_LENGTH = new Size(2.87d, SizeUnit.in);
  private static Size TELE_HOLE_SPACING = new Size(1.135d, SizeUnit.in);
  
  private static Size HOLE_SIZE = new Size(2d, SizeUnit.mm);
  private static Size HOLE_MARGIN = new Size(4d, SizeUnit.mm);
  private static Size POLE_SIZE = new Size(4d, SizeUnit.mm);
  private static Size POLE_SPACING = new Size(11.68d, SizeUnit.mm);

  private static Size RAIL_WIDTH = new Size(1.5d, SizeUnit.mm);
  private static Size RAIL_LENGTH = new Size(60d, SizeUnit.mm);
  private static Size COIL_SPACING = new Size(7.5d, SizeUnit.mm);
  
  private Color color = BODY_COLOR;
  private Color poleColor = METAL_COLOR;
  private Color baseColor = BASE_COLOR;
  private SingleCoilType type = SingleCoilType.Stratocaster;
  private PolePieceType polePieceType = PolePieceType.Rods;
  private Color lugColor = LUG_COLOR;  
  
  @Override
  protected OrientationHV getControlPointDirection() {   
    return OrientationHV.HORIZONTAL;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    
    Theme theme =
        (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);

    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBaseColor());
    g2d.fill(body[4]);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getColor());
    if (body[0] == null)
      g2d.fill(body[3]);
    else
      g2d.fill(body[0]);
    
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getLugColor());
    g2d.fill(body[1]);
    g2d.setColor(outlineMode ? theme.getOutlineColor() : darkerOrLighter(LUG_COLOR));
    g2d.draw(body[1]);

    // g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : color);
    // g2d.fill(body[3]);
    g2d.setComposite(oldComposite);   

    Color finalBorderColor;
    if (outlineMode) {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : darkerOrLighter(getBaseColor());
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[4]);

    if (outlineMode) {     
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : darkerOrLighter(color);
    }

    g2d.setColor(finalBorderColor);
    if (body[0] != null)
      g2d.draw(body[0]);
    g2d.draw(body[3]);

    if (!outlineMode) {
      g2d.setColor(getPoleColor());
      g2d.fill(body[2]);
      g2d.setColor(darkerOrLighter(getPoleColor()));
      g2d.draw(body[2]);
    }

    Color finalLabelColor;
    if (outlineMode) {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : LABEL_COLOR;
    }
    g2d.setColor(finalLabelColor);
    g2d.setFont(project.getFont());
    Rectangle bounds = body[3].getBounds();
    drawCenteredText(g2d, value, bounds.x + bounds.width / 2, bounds.y + bounds.height / 2, HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
    
    drawTerminalLabels(g2d, finalLabelColor, project);
  }  

  @SuppressWarnings("incomplete-switch")
  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[5];

      Point[] points = getControlPoints();
      int x = (points[0].x + points[3].x) / 2;
      int y = (points[0].y + points[3].y) / 2;;
      int width = (int) WIDTH.convertToPixels();
      int length = (int) LENGTH.convertToPixels();
      int stratInnerLength = (int) STRAT_INNER_LENGTH.convertToPixels();
      int teleLength = (int) TELE_LENGTH.convertToPixels();
      int teleBaseWidth = (int) TELE_BASE_WIDTH.convertToPixels();
      int teleLipLength = getClosestOdd(TELE_LIP_LENGTH.convertToPixels());
      int teleHoleSpacing = (int) TELE_HOLE_SPACING.convertToPixels();
      int coilLength = getType() == SingleCoilType.Stratocaster ? stratInnerLength : teleLength;
      int lipWidth = (int) STRAT_LIP_WIDTH.convertToPixels();
      int lipLength = (int) STRAT_LIP_LENGTH.convertToPixels();
      int lugDiameter = getClosestOdd(LUG_DIAMETER.convertToPixels());
      int holeSize = getClosestOdd(HOLE_SIZE.convertToPixels());
      int holeMargin = getClosestOdd(HOLE_MARGIN.convertToPixels());
      int baseRadius = (int) BASE_RADIUS.convertToPixels();
      int coilOffset = 0;

      if (getType() == SingleCoilType.Stratocaster) {
        coilOffset = lipWidth / 2;

        Area mainArea =
            new Area(new RoundRectangle2D.Double(x - length / 2, y - lipWidth / 2 - width, length, width, width, width));
        // Cutout holes
        mainArea.subtract(new Area(new Ellipse2D.Double(x - length / 2 + holeMargin - holeSize / 2, y - lipWidth / 2
            - width / 2 - holeSize / 2, holeSize, holeSize)));
        mainArea.subtract(new Area(new Ellipse2D.Double(x + length / 2 - holeMargin - holeSize / 2, y - lipWidth / 2
            - width / 2 - holeSize / 2, holeSize, holeSize)));

        body[0] = mainArea;
        RoundedPath basePath = new RoundedPath(baseRadius);
        basePath.moveTo(x, y + lipWidth / 2);
        basePath.lineTo(x + lipLength / 2, y + lipWidth / 2);
        basePath.lineTo(x + length / 2, y - lipWidth);
        basePath.lineTo(x - length / 2, y - lipWidth);
        basePath.lineTo(x - lipLength / 2, y + lipWidth / 2);
        basePath.lineTo(x, y + lipWidth / 2);

        Area base = new Area(basePath.getPath());
        base.subtract(mainArea);

        body[4] = base;
      } else if (getType() == SingleCoilType.Telecaster) {
        coilOffset = (teleBaseWidth - width) / 4;

        RoundedPath basePath = new RoundedPath(baseRadius);
        basePath.moveTo(x, y + coilOffset);
        basePath.lineTo(x + teleLipLength / 2, y + coilOffset);
        basePath.lineTo(x + coilLength * 0.53, y - coilOffset - width / 2);
        basePath.lineTo(x + coilLength / 2 - width * 0.45, y - coilOffset - width);
        basePath.lineTo(x, y - 3 * coilOffset - width);
        basePath.lineTo(x - coilLength / 2 + width * 0.45, y - coilOffset - width);
        basePath.lineTo(x - coilLength * 0.53, y - coilOffset - width / 2);
        basePath.lineTo(x - teleLipLength / 2, y + coilOffset);
        basePath.lineTo(x, y + coilOffset);

        Area base = new Area(basePath.getPath());
        base.intersect(new Area(new Rectangle2D.Double(x - coilLength * 0.48, y - teleBaseWidth, coilLength * 0.96,
            teleBaseWidth * 2)));

        // Cutout holes
        base.subtract(new Area(new Ellipse2D.Double(x - teleLipLength / 2 - holeSize / 2, y, holeSize, holeSize)));
        base.subtract(new Area(new Ellipse2D.Double(x + teleLipLength / 2 - holeSize / 2, y, holeSize, holeSize)));
        base.subtract(new Area(new Ellipse2D.Double(x - holeSize / 2, y - teleHoleSpacing, holeSize, holeSize)));

        body[4] = base;
      }      

      Area poleArea = new Area();
      if (getPolePieceType() == PolePieceType.Rods) {
        int poleSize = (int) POLE_SIZE.convertToPixels();
        int poleSpacing = (int) POLE_SPACING.convertToPixels();
        int poleMargin = (length - poleSpacing * 5) / 2;
        for (int i = 0; i < 6; i++) {
          Ellipse2D pole =
              new Ellipse2D.Double(x - length / 2 + poleMargin + i * poleSpacing - poleSize / 2, y - coilOffset - width
                  / 2 - poleSize / 2, poleSize, poleSize);
          poleArea.add(new Area(pole));
        }
      } else if (getPolePieceType() == PolePieceType.RodHumbucker) {
        int poleSize = (int) POLE_SIZE.convertToPixels() / 2;
        int poleSpacing = (int) POLE_SPACING.convertToPixels();
        int poleMargin = (length - poleSpacing * 5) / 2;
        int coilSpacing = (int) COIL_SPACING.convertToPixels();
        for (int i = 0; i < 6; i++) {
          poleArea.add(new Area(new Ellipse2D.Double(x - length / 2 + poleMargin + i * poleSpacing - poleSize / 2, y
              - coilOffset - width / 2 - poleSize / 2 - coilSpacing / 2, poleSize, poleSize)));
          poleArea.add(new Area(new Ellipse2D.Double(x - length / 2 + poleMargin + i * poleSpacing - poleSize / 2, y
              - coilOffset - width / 2 - poleSize / 2 + coilSpacing / 2, poleSize, poleSize)));
        }
      } else if (getPolePieceType() == PolePieceType.RailHumbucker) {
        int railWidth = (int) RAIL_WIDTH.convertToPixels();
        int railSpacing = (int) COIL_SPACING.convertToPixels();
        int railLength = (int) RAIL_LENGTH.convertToPixels();
        poleArea.add(new Area(new Rectangle2D.Double(x - railLength / 2, y - coilOffset - width / 2 - railSpacing / 2
            - railWidth / 2, railLength, railWidth)));
        poleArea.add(new Area(new Rectangle2D.Double(x - railLength / 2, y - coilOffset - width / 2 + railSpacing / 2
            - railWidth / 2, railLength, railWidth)));
      } else if (getPolePieceType() == PolePieceType.Rail) {
        int railWidth = 2 * (int) RAIL_WIDTH.convertToPixels();
        int railLength = (int) RAIL_LENGTH.convertToPixels();
        poleArea.add(new Area(new RoundRectangle2D.Double(x - railLength / 2, y - coilOffset - width / 2 - railWidth
            / 2, railLength, railWidth, railWidth / 2, railWidth / 2)));
      }

      body[2] = poleArea;

      body[3] =
          new Area(new RoundRectangle2D.Double(x - coilLength / 2, y - coilOffset - width, coilLength, width, width,
              width));

      ((Area) body[4]).subtract((Area) body[3]);

      // Rotate if needed
      if (orientation != Orientation.DEFAULT) {
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
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
        for (Shape shape : body) {
          Area area = (Area) shape;
          if (area != null)
            area.transform(rotation);
        }
      }
      
      body[1] = new Area();
      double lugHole = getClosestOdd(lugDiameter * 0.4);
      for (int i = getPolarity() == Polarity.Humbucking ? 0 : 1; i < (getPolarity() == Polarity.Humbucking ? 4 : 3); i++) {
        Point p = points[i];
        ((Area)body[1]).add(new Area(new Ellipse2D.Double(p.x - lugDiameter / 2, p.y - lugDiameter / 2, lugDiameter, lugDiameter)));
        ((Area)body[1]).subtract(new Area(new Ellipse2D.Double(p.x - lugHole / 2, p.y - lugHole / 2, lugHole, lugHole)));
      } 
    }
    return body;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(Math.PI / 4, width / 2, height / 2);

    int bodyWidth = 8 * width / 32;
    int bodyLength = 30 * width / 32;

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.setColor(BASE_COLOR);
    g2d.fillPolygon(new int[] {width * 9 / 16, width * 9 / 16, width * 11 / 16, width * 11 / 16}, new int[] {
        (height - bodyLength) / 2, (height + bodyLength) / 2, height * 5 / 8, height * 3 / 8}, 4);
    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect((width - bodyWidth) / 2, (height - bodyLength) / 2, bodyWidth, bodyLength, bodyWidth, bodyWidth);

    g2d.setColor(Color.gray);
    g2d.drawRoundRect((width - bodyWidth) / 2, (height - bodyLength) / 2, bodyWidth, bodyLength, bodyWidth, bodyWidth);

    // g2d.setColor(Color.gray);
    // g2d.drawLine(width / 2, 4 * width / 32, width / 2, 4 * width / 32);
    // g2d.drawLine(width / 2, height - 4 * width / 32, width / 2, height - 4 * width / 32);

    g2d.setColor(METAL_COLOR);
    int poleSize = 2;
    int poleSpacing = 17 * width / 32;
    for (int i = 0; i < 6; i++) {
      g2d.fillOval((width - poleSize) / 2, (height - poleSpacing) / 2 + (i * poleSpacing / 5), poleSize, poleSize);
    }
  }

  @EditableProperty
  public SingleCoilType getType() {
    if (type == null)
      type = SingleCoilType.Stratocaster;
    return type;
  }

  public void setType(SingleCoilType type) {
    this.type = type;
    // Invalidate the body
    body = null;
  }

  @EditableProperty(name = "Pole Pieces")
  public PolePieceType getPolePieceType() {
    if (polePieceType == null)
      polePieceType = PolePieceType.Rods;
    return polePieceType;
  }

  public void setPolePieceType(PolePieceType polePieceType) {
    this.polePieceType = polePieceType;
    // Invalidate the body
    body = null;
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty(name = "Base")
  public Color getBaseColor() {
    if (baseColor == null)
      baseColor = BASE_COLOR;
    return baseColor;
  }

  public void setBaseColor(Color baseColor) {
    this.baseColor = baseColor;
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
  
  @EditableProperty(name = "Lugs")
  public Color getLugColor() {
    if (lugColor == null) {
      lugColor = LUG_COLOR;
    }
    return lugColor;
  }
  
  public void setLugColor(Color lugColor) {
    this.lugColor = lugColor;
  }
   

  public enum SingleCoilType {
    Stratocaster, Telecaster;
  }

  public enum PolePieceType {
    Rods("Single Rods"), Rail("Single Rail"), RailHumbucker("Dual Rail Humbucker"), RodHumbucker("Dual Rods"), None("None");

    private String label;

    private PolePieceType(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
