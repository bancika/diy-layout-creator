/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.components.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractComponent;
import org.diylc.components.transform.TextTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.ICommonNode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

@ComponentDescriptor(name = "Common Node", author = "Branislav Stojkovic", category = "Misc",
    description = "Label that ties all nodes together", instanceNamePrefix = "CN",
    zOrder = IDIYComponent.TEXT, flexibleZOrder = true, bomPolicy = BomPolicy.NEVER_SHOW,
    transformer = TextTransformer.class)
public class CommonNode extends AbstractComponent<String> implements ICommonNode {

  public static String DEFAULT_TEXT = "X";

  private static int BUFFER_X = 6;
  private static int BUFFER_Y = 4;

  private static final int STROKE_WIDTH = 1;
  private static final Color BG_COLOR = Color.decode("#EEEEEE");

  private static final long serialVersionUID = 1L;

  private Point2D.Double point = new Point2D.Double(0, 0);
  private String text = DEFAULT_TEXT;
  private Font font = LABEL_FONT;
  private Color color = LABEL_COLOR;
  private Color bgColor = BG_COLOR;
  private CommonNodeShape shape = CommonNodeShape.RECTANGLE;

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    g2d.setFont(font);

    double x = point.getX();
    double y = point.getY();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(STROKE_WIDTH));

    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle stringBounds = fontMetrics.getStringBounds(text, g2d).getBounds();

    Shape rect;
    if (shape == CommonNodeShape.RECTANGLE) {
      rect = new RoundRectangle2D.Double(x - stringBounds.getWidth() / 2 - BUFFER_X,
          y - stringBounds.getHeight() / 2 - BUFFER_Y, stringBounds.getWidth() + 2 * BUFFER_X,
          stringBounds.getHeight() + 2 * BUFFER_Y, stringBounds.getHeight() / 2,
          stringBounds.getHeight() / 2);
    } else {
      double max = Math.max(stringBounds.getWidth(), stringBounds.getHeight());
      rect = new Ellipse2D.Double(x - max / 2 - BUFFER_X, y - max / 2 - BUFFER_X,
          max + 2 * BUFFER_X, max + 2 * BUFFER_X);
    }

    g2d.setColor(bgColor);
    drawingObserver.startTrackingContinuityArea(true);
    g2d.fill(rect);
    drawingObserver.stopTrackingContinuityArea();
    drawingObserver.stopTracking();
    g2d.setColor(componentState == ComponentState.SELECTED ? LABEL_COLOR_SELECTED : color);
    g2d.draw(rect);

    StringUtils.drawCenteredText(g2d, text, x, y, HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {

    g2d.setFont(LABEL_FONT.deriveFont(13f * width / 32).deriveFont(Font.BOLD));

    String text = "B+";

    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds(text, g2d);

    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());

    // Center text horizontally and vertically.
    int x = (width - textWidth) / 2 + 1;
    int y = (height - textHeight) / 2 + fontMetrics.getAscent();

    g2d.setColor(BG_COLOR);
    g2d.fillRoundRect(4, 6, width - 8, height - 12, 6, 6);
    g2d.setColor(LABEL_COLOR);
    g2d.drawRoundRect(4, 6, width - 8, height - 12, 6, 6);
    g2d.drawString(text, x, y);
  }

  @EditableProperty
  public Font getFont() {
    return font;
  }

  public void setFont(Font font) {
    this.font = font;
  }

  // Bold and italic fields are named to be alphabetically after Font. This is
  // important!

  @EditableProperty(name = "Font Bold")
  public boolean getBold() {
    return font.isBold();
  }

  public void setBold(boolean bold) {
    if (bold) {
      if (font.isItalic()) {
        font = font.deriveFont(Font.BOLD + Font.ITALIC);
      } else {
        font = font.deriveFont(Font.BOLD);
      }
    } else {
      if (font.isItalic()) {
        font = font.deriveFont(Font.ITALIC);
      } else {
        font = font.deriveFont(Font.PLAIN);
      }
    }
  }

  @EditableProperty(name = "Font Size")
  public int getFontSize() {
    return font.getSize();
  }

  public void setFontSize(int size) {
    font = font.deriveFont((float) size);
  }

  @Override
  public int getControlPointCount() {
    return 1;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return point;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    this.point.setLocation(point);
  }

  @EditableProperty(name = "Label")
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty(name = "Background")
  public Color getBgColor() {
    return bgColor;
  }

  public void setBgColor(Color bgColor) {
    this.bgColor = bgColor;
  }

  @EditableProperty
  public CommonNodeShape getShape() {
    return shape;
  }

  public void setShape(CommonNodeShape shape) {
    this.shape = shape;
  }

  @Deprecated
  @Override
  public String getName() {
    return super.getName();
  }

  @EditableProperty
  @Override
  public String getValue() {
    return text;
  }

  @Override
  public void setValue(String value) {
    this.text = value;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  @Override
  public String getCommonNodeLabel() {
    return text;
  }

  public static enum CommonNodeShape {
    RECTANGLE("Rectangle"), CIRCLE("Circle");

    private String label;

    private CommonNodeShape(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
