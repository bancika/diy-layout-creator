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
import java.awt.Polygon;

import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Diode (symbol)", author = "Branislav Stojkovic", category = "Schematic Symbols",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "D", description = "Diode schematic symbol",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "Schematic",
    transformer = SimpleComponentTransformer.class)
public class DiodeSymbol extends AbstractDiodeSymbol {

  private static final long serialVersionUID = 1L;

  public static Size BAND_SIZE = new Size(0.01, SizeUnit.in);

  public void drawIcon(Graphics2D g2d, int width, int height) {
    int size = width * 3 / 8;
    int bandSize = 1;
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR);
    g2d.drawLine(0, height / 2, (width - size) / 2, height / 2);
    g2d.drawLine((int) (width + size / Math.sqrt(2) + bandSize) / 2, height / 2, width, height / 2);
    g2d.setColor(COLOR);
    g2d.fill(new Polygon(new int[] {(width - size) / 2, (width - size) / 2,
        (int) ((width - size) / 2 + size / Math.sqrt(2))}, new int[] {(height - size) / 2, (height + size) / 2,
        height / 2}, 3));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(bandSize));
    g2d.drawLine((int) ((width - size) / 2 + size / Math.sqrt(2)), (height - size) / 2,
        (int) ((width - size) / 2 + size / Math.sqrt(2)), (height + size) / 2);
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    double width = getWidth().convertToPixels();
    int bandSize = (int) BAND_SIZE.convertToPixels();
    g2d.setColor(getBodyColor());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(bandSize));
    g2d.drawLine((int) (width / Math.sqrt(2)) + bandSize, 0, (int) (width / Math.sqrt(2) + bandSize), (int) width);
  }
}
