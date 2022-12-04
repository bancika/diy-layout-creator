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
package org.diylc.components.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractComponent;
import org.diylc.components.transform.TextTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.MultiLineText;

@ComponentDescriptor(name = "Label", author = "Branislav Stojkovic", category = "Misc",
    description = "User defined label", instanceNamePrefix = "LAB", zOrder = IDIYComponent.TEXT, flexibleZOrder = true,
    bomPolicy = BomPolicy.NEVER_SHOW, transformer = TextTransformer.class)
public class Label extends AbstractComponent<String> {

  public static String DEFAULT_TEXT = "Double click to edit text";

  private static final long serialVersionUID = 1L;

  private Point2D.Double point = new Point2D.Double(0, 0);
  private String text = DEFAULT_TEXT;
  private Font font = LABEL_FONT;
  private Color color = LABEL_COLOR;
  @SuppressWarnings("unused")
  @Deprecated
  private transient boolean center;
  private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
  private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
  private Orientation orientation = Orientation.DEFAULT;

  @SuppressWarnings("incomplete-switch")
  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    g2d.setColor(componentState == ComponentState.SELECTED ? LABEL_COLOR_SELECTED : color);
    g2d.setFont(font);
   
    double x = point.getX();
    double y = point.getY();

    switch (getOrientation()) {
      case _90:
        g2d.rotate(Math.PI / 2, point.getX(), point.getY());
        break;
      case _180:
        g2d.rotate(Math.PI, point.getX(), point.getY());
        break;
      case _270:
        g2d.rotate(Math.PI * 3 / 2, point.getX(), point.getY());
        break;
    }
    
    StringUtils.drawCenteredText(g2d, text, x, y, getHorizontalAlignment(), getVerticalAlignment());    
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(LABEL_COLOR);
    g2d.setFont(LABEL_FONT.deriveFont(13f * width / 32).deriveFont(Font.PLAIN));

    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds("Abc", g2d);

    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());

    // Center text horizontally and vertically.
    int x = (width - textWidth) / 2 + 1;
    int y = (height - textHeight) / 2 + fontMetrics.getAscent();

    g2d.drawString("Abc", x, y);
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

  @EditableProperty(name = "Font Italic")
  public boolean getItalic() {
    return font.isItalic();
  }

  public void setItalic(boolean italic) {
    if (italic) {
      if (font.isBold()) {
        font = font.deriveFont(Font.BOLD + Font.ITALIC);
      } else {
        font = font.deriveFont(Font.ITALIC);
      }
    } else {
      if (font.isBold()) {
        font = font.deriveFont(Font.BOLD);
      } else {
        font = font.deriveFont(Font.PLAIN);
      }
    }
  }

  @EditableProperty
  public Orientation getOrientation() {
    if (orientation == null) {
      orientation = Orientation.DEFAULT;
    }
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
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

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty(name = "Vertical Alignment")
  public VerticalAlignment getVerticalAlignment() {
    if (verticalAlignment == null) {
      verticalAlignment = VerticalAlignment.CENTER;
    }
    return verticalAlignment;
  }

  public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
    this.verticalAlignment = verticalAlignment;
  }

  @EditableProperty(name = "Horizontal Alignment")
  public HorizontalAlignment getHorizontalAlignment() {
    if (horizontalAlignment == null) {
      horizontalAlignment = HorizontalAlignment.CENTER;
    }
    return horizontalAlignment;
  }

  public void setHorizontalAlignment(HorizontalAlignment alignment) {
    this.horizontalAlignment = alignment;
  }

  @Override
  public String getName() {
    return super.getName();
  }

  @EditableProperty(name="Text", defaultable = false)
  @MultiLineText
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
}
