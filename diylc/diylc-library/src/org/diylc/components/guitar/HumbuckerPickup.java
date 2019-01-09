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
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Humbucker Pickup", category = "Guitar", author = "Branislav Stojkovic",
    description = "PAF-style humbucker guitar pickup", stretchable = false, zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "PKP", autoEdit = false, keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Guitar Wiring Diagram")
public class HumbuckerPickup extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = Color.lightGray;
  private static Color BOBIN_COLOR1 = Color.decode("#EAE3C6");
  private static Color BOBIN_COLOR2 = Color.black;
  private static Color POINT_COLOR = Color.darkGray;
  private static Size WIDTH = new Size(36.5d, SizeUnit.mm);
  private static Size LENGTH = new Size(68.58d, SizeUnit.mm);
  private static Size WIDTH_MINI = new Size(29.3d, SizeUnit.mm);
  private static Size LENGTH_MINI = new Size(67.4d, SizeUnit.mm);
  private static Size LIP_WIDTH = new Size(12.7d, SizeUnit.mm);
  private static Size LIP_LENGTH = new Size(7.9d, SizeUnit.mm);
  private static Size EDGE_RADIUS = new Size(4d, SizeUnit.mm);
  private static Size POINT_MARGIN = new Size(1.5d, SizeUnit.mm);
  private static Size POINT_SIZE = new Size(2d, SizeUnit.mm);
  private static Size LIP_HOLE_SIZE = new Size(1.5d, SizeUnit.mm);
  private static Size POLE_SIZE = new Size(3d, SizeUnit.mm);
  private static Size POLE_SPACING = new Size(10.1d, SizeUnit.mm);

  private String value = "";
  private Point controlPoint = new Point(0, 0);
  transient Shape[] body;
  private Orientation orientation = Orientation.DEFAULT;
  private Color color = BASE_COLOR;
  private Color poleColor = METAL_COLOR;
  private HumbuckerType type;
  private boolean cover;
  private Color bobinColor1 = BOBIN_COLOR1;
  private Color bobinColor2 = BOBIN_COLOR2;
  private PolePieceType coilType1;
  private PolePieceType coilType2;

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (componentState != ComponentState.DRAGGING) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : color);
      g2d.fill(body[0]);
      g2d.fill(body[1]);

      if (body[4] != null) {
        g2d.setColor(getBobinColor1());
        g2d.fill(body[4]);
      }
      if (body[5] != null) {
        g2d.setColor(getBobinColor2());
        g2d.fill(body[5]);
      }

      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : POINT_COLOR);
      g2d.fill(body[2]);

      g2d.setComposite(oldComposite);
    }

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : color.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);
    g2d.draw(body[1]);
    if (!outlineMode && componentState != ComponentState.DRAGGING) {
      g2d.setColor(getPoleColor());
      g2d.fill(body[3]);
      g2d.setColor(darkerOrLighter(getPoleColor()));
      g2d.draw(body[3]);

      if (body[4] != null) {
        g2d.setColor(getBobinColor1().darker());
        g2d.draw(body[4]);
      }
      if (body[5] != null) {
        g2d.setColor(getBobinColor2().darker());
        g2d.draw(body[5]);
      }
    }

    Color finalLabelColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
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
    Rectangle bounds = body[0].getBounds();
    drawCenteredText(g2d, value, bounds.x + bounds.width / 2, bounds.y + bounds.height / 2, HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
  }

  @SuppressWarnings("incomplete-switch")
  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[6];

      int x = controlPoint.x;
      int y = controlPoint.y;
      int width = (int) getType().getWidth().convertToPixels();
      int length = (int) getType().getLength().convertToPixels();
      int lipWidth = (int) LIP_WIDTH.convertToPixels();
      int lipLength = (int) LIP_LENGTH.convertToPixels();
      int edgeRadius = (int) EDGE_RADIUS.convertToPixels();
      int pointMargin = (int) POINT_MARGIN.convertToPixels();
      int pointSize = getClosestOdd(POINT_SIZE.convertToPixels());
      int lipHoleSize = getClosestOdd(LIP_HOLE_SIZE.convertToPixels());

      body[0] =
          new Area(new RoundRectangle2D.Double(x + pointMargin - length, y - pointMargin, length, width, edgeRadius,
              edgeRadius));

      if (!getCover()) {
        int bobinWidth = width / 2;
        body[4] =
            new Area(new RoundRectangle2D.Double(x + pointMargin - length, y - pointMargin, length, bobinWidth,
                bobinWidth, bobinWidth));
        body[5] =
            new Area(new RoundRectangle2D.Double(x + pointMargin - length, y - pointMargin + bobinWidth, length,
                bobinWidth, bobinWidth, bobinWidth));
      }

      body[1] =
          new Area(new RoundRectangle2D.Double(x + pointMargin - length - lipLength, y - pointMargin + width / 2
              - lipWidth / 2, length + 2 * lipLength, lipWidth, edgeRadius / 2, edgeRadius / 2));
      Area lipArea = new Area(body[1]);
      lipArea.subtract((Area) (body[0]));
      lipArea.subtract(new Area(new Ellipse2D.Double(x + pointMargin - length - lipLength / 2, y - pointMargin + width
          / 2 - lipHoleSize / 2, lipHoleSize, lipHoleSize)));
      lipArea.subtract(new Area(new Ellipse2D.Double(x + pointMargin + lipLength / 2, y - pointMargin + width / 2
          - lipHoleSize / 2, lipHoleSize, lipHoleSize)));
      body[1] = lipArea;
      body[2] = new Area(new Ellipse2D.Double(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize));

      int poleSize = (int) POLE_SIZE.convertToPixels();
      int poleSpacing = (int) POLE_SPACING.convertToPixels();
      int coilSpacing = width / 2;
      int coilMargin = (width - coilSpacing) / 2;
      int poleMargin = (length - poleSpacing * 5) / 2;
      Area poleArea = new Area();

      if (getCoilType1() == PolePieceType.Rail) {
        poleArea.add(new Area(
            new RoundRectangle2D.Double(x + pointMargin - length + poleMargin - poleSize / 2, y - pointMargin
                + coilMargin - poleSize / 2, poleSpacing * 5 + poleSize, poleSize, poleSize / 2, poleSize / 2)));
      } else if (getCoilType1() == PolePieceType.Rods) {
        for (int i = 0; i < 6; i++) {
          if (getType() == HumbuckerType.PAF || !getCover()) {
            Ellipse2D pole =
                new Ellipse2D.Double(x + pointMargin - length + poleMargin + i * poleSpacing - poleSize / 2, y
                    - pointMargin + coilMargin - poleSize / 2, poleSize, poleSize);
            poleArea.add(new Area(pole));
          }
        }
      }

      if (getCoilType2() == PolePieceType.Rail) {
        poleArea.add(new Area(new RoundRectangle2D.Double(x + pointMargin - length + poleMargin - poleSize / 2, y
            - pointMargin + width - coilMargin - poleSize / 2, poleSpacing * 5 + poleSize, poleSize, poleSize / 2,
            poleSize / 2)));
      } else if (getCoilType1() == PolePieceType.Rods) {
        for (int i = 0; i < 6; i++) {
          Ellipse2D pole =
              new Ellipse2D.Double(x + pointMargin - length + poleMargin + i * poleSpacing - poleSize / 2, y
                  - pointMargin + width - coilMargin - poleSize / 2, poleSize, poleSize);
          poleArea.add(new Area(pole));
        }
      }

      body[3] = poleArea;

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
    }
    return body;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(Math.PI / 4, width / 2, height / 2);

    int baseWidth = 16 * width / 32;
    int baseLength = 27 * width / 32;

    g2d.setColor(BASE_COLOR);
    g2d.fillRoundRect((width - baseWidth / 4) / 2, 0, baseWidth / 4, height - 1, 2 * width / 32, 2 * width / 32);
    g2d.setColor(BASE_COLOR.darker());
    g2d.drawRoundRect((width - baseWidth / 4) / 2, 0, baseWidth / 4, height - 1, 2 * width / 32, 2 * width / 32);

    g2d.setColor(BASE_COLOR);
    g2d.fillRoundRect((width - baseWidth) / 2, (height - baseLength) / 2, baseWidth, baseLength, 4 * width / 32,
        4 * width / 32);
    g2d.setColor(BASE_COLOR.darker());
    g2d.drawRoundRect((width - baseWidth) / 2, (height - baseLength) / 2, baseWidth, baseLength, 4 * width / 32,
        4 * width / 32);

    g2d.setColor(BOBIN_COLOR1);
    g2d.fillRoundRect((width - baseWidth) / 2, (height - baseLength) / 2, baseWidth / 2, baseLength, baseWidth / 2,
        baseWidth / 2);
    g2d.setColor(BOBIN_COLOR2);
    g2d.fillRoundRect(width / 2, (height - baseLength) / 2, baseWidth / 2, baseLength, baseWidth / 2, baseWidth / 2);

    g2d.setColor(METAL_COLOR);
    int poleSize = 2;
    int poleSpacing = 17 * width / 32;
    for (int i = 0; i < 6; i++) {
      g2d.fillOval((width - poleSize - baseWidth / 2) / 2, (height - poleSpacing) / 2 + (i * poleSpacing / 5),
          poleSize, poleSize);
      g2d.fillOval((width - poleSize + baseWidth / 2) / 2, (height - poleSpacing) / 2 + (i * poleSpacing / 5),
          poleSize, poleSize);
    }
  }

  @EditableProperty
  public HumbuckerType getType() {
    if (type == null)
      type = HumbuckerType.PAF;
    return type;
  }

  public void setType(HumbuckerType type) {
    this.type = type;
    // Invalidate the body
    body = null;
  }

  @EditableProperty(name = "Bobin 1")
  public Color getBobinColor1() {
    if (bobinColor1 == null)
      bobinColor1 = BOBIN_COLOR1;
    return bobinColor1;
  }

  public void setBobinColor1(Color bobinColor1) {
    this.bobinColor1 = bobinColor1;
  }

  @EditableProperty(name = "Bobin 2")
  public Color getBobinColor2() {
    if (bobinColor2 == null)
      bobinColor2 = BOBIN_COLOR2;
    return bobinColor2;
  }

  public void setBobinColor2(Color bobinColor2) {
    this.bobinColor2 = bobinColor2;
  }

  @EditableProperty
  public boolean getCover() {
    return cover;
  }

  public void setCover(boolean cover) {
    this.cover = cover;
    // Invalidate the body
    body = null;
  }

  @Override
  public int getControlPointCount() {
    return 1;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoint;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.controlPoint.setLocation(point);
    // Invalidate the body
    body = null;
  }

  @EditableProperty(name = "Model")
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
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

  @EditableProperty(name = "Pole Color")
  public Color getPoleColor() {
    if (poleColor == null)
      poleColor = METAL_COLOR;
    return poleColor;
  }

  public void setPoleColor(Color poleColor) {
    this.poleColor = poleColor;
  }

  @EditableProperty(name = "Pole Pieces 1")
  public PolePieceType getCoilType1() {
    if (coilType1 == null) {
      return PolePieceType.Rods;
    }
    return coilType1;
  }

  public void setCoilType1(PolePieceType coilType1) {
    this.coilType1 = coilType1;
    // Invalidate the body
    body = null;
  }

  @EditableProperty(name = "Pole Pieces 2")
  public PolePieceType getCoilType2() {
    if (coilType2 == null) {
      return PolePieceType.Rods;
    }
    return coilType2;
  }

  public void setCoilType2(PolePieceType coilType2) {
    this.coilType2 = coilType2;
    // Invalidate the body
    body = null;
  }

  public static enum HumbuckerType {

    PAF(WIDTH, LENGTH), Mini(WIDTH_MINI, LENGTH_MINI);

    private Size width;
    private Size length;

    private HumbuckerType(Size width, Size length) {
      this.width = width;
      this.length = length;
    }

    public Size getWidth() {
      return width;
    }

    public Size getLength() {
      return length;
    }

    @Override
    public String toString() {
      return name();
    }
  }

  public enum PolePieceType {
    Rods, Rail, None;
  }
}
