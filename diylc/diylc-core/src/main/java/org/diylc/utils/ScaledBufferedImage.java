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
package org.diylc.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ScaledBufferedImage extends BufferedImage {
  
  private final double scaleFactor;

  public ScaledBufferedImage(int width, int height, int imageType, double scaleFactor) {
    super((int) Math.round(width * scaleFactor), (int) Math.round(height * scaleFactor), imageType);
    this.scaleFactor = scaleFactor;
  }

  @Override
  public Graphics2D createGraphics() {
    Graphics2D returnValue = super.createGraphics();
    returnValue.scale(scaleFactor, scaleFactor);
    return returnValue;
  }
}
