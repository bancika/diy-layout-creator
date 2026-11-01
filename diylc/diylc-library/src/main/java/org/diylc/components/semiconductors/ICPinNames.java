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
package org.diylc.components.semiconductors;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.components.AbstractMakerBoard;

/**
 * Pin names shared by the IC packages. A package keeps its names as one comma-separated string in
 * pin-number order, pin 1 first, so the names survive a change of pin count and read back as typed.
 */
public final class ICPinNames {

  // a size below the maker boards' pin font, as IC pin names are longer than a board's silkscreen
  // and get only one pin pitch each
  public static Font PIN_NAME_FONT = new Font(AbstractMakerBoard.SILK_FONT_FAMILY, Font.PLAIN, 6);
  public static int PIN_NAME_GAP = 2;

  private ICPinNames() {}

  public static String[] parse(String pinNames) {
    return pinNames == null ? new String[0] : pinNames.split(",", -1);
  }

  /**
   * @param names names as returned by {@link #parse(String)}
   * @param pinNumber 1-based pin number
   * @return the trimmed name of the pin, or null if it has none
   */
  public static String get(String[] names, int pinNumber) {
    if (pinNumber < 1 || pinNumber > names.length) {
      return null;
    }
    String name = names[pinNumber - 1].trim();
    return name.isEmpty() ? null : name;
  }

  /**
   * Draws a pin label outside the package, starting just beyond whichever reaches further in the
   * given direction, the body or the pin itself, and reading away from the package. The direction
   * is one of the four axis directions; text pointing left or down is right-aligned so that it never
   * reads upside down.
   *
   * @param pin the pin the label belongs to
   * @param dirX x component of the unit direction away from the package
   * @param dirY y component of the unit direction away from the package
   * @param bodyBounds bounds of the package body
   * @param pinRadius half the drawn pin size
   */
  public static void drawOutside(Graphics2D g2d, String text, Point2D pin, double dirX, double dirY,
      Rectangle2D bodyBounds, double pinRadius) {
    boolean horizontal = Math.abs(dirX) > Math.abs(dirY);
    double pinReach = (horizontal ? pin.getX() * dirX : pin.getY() * dirY) + pinRadius;
    double bodyReach = horizontal ? (dirX > 0 ? bodyBounds.getMaxX() : -bodyBounds.getMinX())
        : (dirY > 0 ? bodyBounds.getMaxY() : -bodyBounds.getMinY());
    double start = Math.max(pinReach, bodyReach) + PIN_NAME_GAP;
    double x = horizontal ? start * dirX : pin.getX();
    double y = horizontal ? pin.getY() : start * dirY;

    FontMetrics fontMetrics = g2d.getFontMetrics();
    int width = fontMetrics.stringWidth(text);
    float baseline = (fontMetrics.getAscent() - fontMetrics.getDescent()) / 2f;
    AffineTransform oldTransform = g2d.getTransform();
    g2d.translate(x, y);
    double forward;
    if (horizontal) {
      forward = dirX;
    } else {
      g2d.rotate(-Math.PI / 2);
      forward = -dirY;
    }
    g2d.drawString(text, forward > 0 ? 0 : -width, baseline);
    g2d.setTransform(oldTransform);
  }
}
