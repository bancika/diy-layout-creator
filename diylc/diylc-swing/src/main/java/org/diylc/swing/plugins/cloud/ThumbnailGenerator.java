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
package org.diylc.swing.plugins.cloud;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.EnumSet;

import org.diylc.common.DrawOption;
import org.diylc.common.IPlugInPort;

/**
 * Generates thumbnail image with hard-coded dimensions.
 * 
 * @author bancika
 *
 */
public class ThumbnailGenerator {

  private IPlugInPort plugInPort;

  public ThumbnailGenerator(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
  }

  public BufferedImage getThumbnail() {
    Dimension d = getThumbnailSize();
    BufferedImage thumbnailImage = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
    Graphics2D cg = thumbnailImage.createGraphics();

    paintThumbnail(cg, new Rectangle(thumbnailImage.getWidth(), thumbnailImage.getHeight()));
    return thumbnailImage;
  }

  public Dimension getThumbnailSize() {
    Dimension d = this.plugInPort.getCanvasDimensions(false, false);
    if (d.height > d.width)
      return new Dimension(192 * d.width / d.height, 192);
    else
      return new Dimension(192, 192 * d.height / d.width);
  }

  public void paintThumbnail(Graphics g, Rectangle rect) {
    Graphics2D g2d = (Graphics2D) g;
    Dimension d = this.plugInPort.getCanvasDimensions(false, false);

    g2d.setColor(Color.white);
    g2d.fill(rect);

    double projectRatio = d.getWidth() / d.getHeight();
    double actualRatio = rect.getWidth() / rect.getHeight();
    double zoomRatio;
    if (projectRatio > actualRatio) {
      zoomRatio = rect.getWidth() / d.getWidth();
    } else {
      zoomRatio = rect.getHeight() / d.getHeight();
    }

    // g2d.scale(zoomRatio, zoomRatio);
    this.plugInPort.draw(g2d, EnumSet.of(DrawOption.ANTIALIASING), null, zoomRatio, null, null);
  }
}
