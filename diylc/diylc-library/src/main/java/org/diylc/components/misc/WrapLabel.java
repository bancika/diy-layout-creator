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
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Auto-Wrap Label", author = "Branislav Stojkovic", category = "Misc",
description = "User defined label with auto-wrapped text", instanceNamePrefix = "L", zOrder = IDIYComponent.TEXT, flexibleZOrder = true,
bomPolicy = BomPolicy.NEVER_SHOW, transformer = TextTransformer.class)
public class WrapLabel extends AbstractComponent<String> {

  private static final long serialVersionUID = 1L;
  
  public static Size DEFAULT_WIDTH = new Size(1.5d, SizeUnit.in);
  public static Size DEFAULT_HEIGHT = new Size(0.5d, SizeUnit.in);
  
  protected Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0),
      new Point2D.Double((int) DEFAULT_WIDTH.convertToPixels(), (int) DEFAULT_HEIGHT.convertToPixels())};
  protected Point2D.Double firstPoint = new Point2D.Double();
  protected Point2D.Double secondPoint = new Point2D.Double();
  
  private Font font = LABEL_FONT;
  private Color color = LABEL_COLOR;
  private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
  
  private String value = "The quick brown fox jumped over a lazy dog";

  public WrapLabel() {
  }

  @MultiLineText
  @EditableProperty
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public int getControlPointCount() {
    return 2;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    firstPoint.setLocation(Math.min(controlPoints[0].getX(), controlPoints[1].getX()),
        Math.min(controlPoints[0].getY(), controlPoints[1].getY()));
    secondPoint.setLocation(Math.max(controlPoints[0].getX(), controlPoints[1].getX()),
        Math.max(controlPoints[0].getY(), controlPoints[1].getY()));
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }
  
  @EditableProperty
  public Font getFont() {
    return font;
  }

  public void setFont(Font font) {
    this.font = font;
  }
  
//Bold and italic fields are named to be alphabetically after Font. This is
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
  
  @EditableProperty(name = "Font Size")
  public int getFontSize() {
    return font.getSize();
  }

  public void setFontSize(int size) {
    font = font.deriveFont((float) size);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    g2d.setColor(componentState == ComponentState.SELECTED ? LABEL_COLOR_SELECTED : color);
    g2d.setFont(font);

    double x = firstPoint.getX();
    double y = firstPoint.getY();
    double maxWidth = secondPoint.getX() - firstPoint.getX();
    
    StringUtils.drawWrappedText(value, g2d, (int)x, (int)y, (int)maxWidth, horizontalAlignment);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(LABEL_COLOR);
    g2d.setFont(LABEL_FONT.deriveFont(10f * width / 32).deriveFont(Font.PLAIN));

    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds("Wrap", g2d);

//    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());

    int x = (width - textWidth) / 2 + 1;
    int y = (int) (4f * width / 32) + fontMetrics.getAscent();

    g2d.drawString("Wrap", x, y);
    y += g2d.getFont().getSize();
    g2d.drawString("Text", x, y);
  }
  
  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
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
}
