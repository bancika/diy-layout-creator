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
package org.diylc.awt;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.diylc.common.HorizontalAlignment;
import org.diylc.common.VerticalAlignment;

/**
 * Globally available utility classes, mostly for string manipulation.
 * 
 * @author Jim Menard, <a href="mailto:jimm@io.com">jimm@io.com</a>
 */
public class StringUtils {

  public static void drawWrappedText(String text, Graphics2D g2d, int x, int y, int maxWidth, HorizontalAlignment horizontalAlignment) {
    FontMetrics textMetrics = g2d.getFontMetrics();
    List<String> lines = wrap(text, textMetrics, maxWidth);
    int lineHeight = textMetrics.getHeight();
    
    if (horizontalAlignment == HorizontalAlignment.CENTER)
      x += maxWidth / 2;
    else if (horizontalAlignment == HorizontalAlignment.RIGHT)
      x += maxWidth;
    
    for (String line : lines) {
      drawCenteredText(g2d, line, x, y, horizontalAlignment, VerticalAlignment.BOTTOM);
      y += lineHeight;
    }
  }

  /**
   * Returns an array of strings, one for each line in the string after it has been wrapped to fit
   * lines of <var>maxWidth</var>. Lines end with any of cr, lf, or cr lf. A line ending at the end
   * of the string will not output a further, empty string.
   * <p>
   * This code assumes <var>str</var> is not <code>null</code>.
   * 
   * @param str the string to split
   * @param fm needed for string width calculations
   * @param maxWidth the max line width, in points
   * @return a non-empty list of strings
   */
  public static List<String> wrap(String str, FontMetrics fm, int maxWidth) {
    // First split by actual newlines to honor them, then split by <br> tags
    List<String> lines = splitIntoLines(str);
    List<String> allLines = new ArrayList<String>();
    for (String line : lines) {
      String[] split = line.split("\\<br\\>");
      allLines.addAll(Arrays.asList(split));
    }
    if (allLines.size() == 0)
      return allLines;

    ArrayList<String> strings = new ArrayList<String>();
    for (Iterator<String> iter = allLines.iterator(); iter.hasNext();)
      wrapLineInto((String) iter.next(), strings, fm, maxWidth);
    return strings;
  }

  /**
   * Given a line of text and font metrics information, wrap the line and add the new line(s) to
   * <var>list</var>.
   * 
   * @param line a line of text
   * @param list an output list of strings
   * @param fm font metrics
   * @param maxWidth maximum width of the line(s)
   */
  public static void wrapLineInto(String line, List<String> list, FontMetrics fm, int maxWidth) {
    int len = line.length();
    int width;
    while (len > 0 && (width = fm.stringWidth(line)) > maxWidth) {
      // Guess where to split the line. Look for the next space before
      // or after the guess.
      int guess = len * maxWidth / width;
      String before = line.substring(0, guess).trim();

      width = fm.stringWidth(before);
      int pos;
      if (width > maxWidth) // Too long
        pos = findBreakBefore(line, guess);
      else { // Too short or possibly just right
        pos = findBreakAfter(line, guess);
        if (pos != -1) { // Make sure this doesn't make us too long
          before = line.substring(0, pos).trim();
          if (fm.stringWidth(before) > maxWidth)
            pos = findBreakBefore(line, guess);
        }
      }
      if (pos == -1)
        pos = guess; // Split in the middle of the word
      
      // if we couldn't find a match, expand maxWidth
      if (pos <= 0) {
        maxWidth++;
        continue;
      }

      list.add(line.substring(0, pos).trim());
      line = line.substring(pos).trim();
      len = line.length();
    }
    // Always add the line, even if empty, to preserve empty lines from newlines
    list.add(line);
  }

  /**
   * Returns the index of the first whitespace character or '-' in <var>line</var> that is at or
   * before <var>start</var>. Returns -1 if no such character is found.
   * 
   * @param line a string
   * @param start where to star looking
   */
  public static int findBreakBefore(String line, int start) {
    for (int i = start; i >= 0; --i) {
      char c = line.charAt(i);
      if (Character.isWhitespace(c) || c == '-')
        return i;
    }
    return -1;
  }

  /**
   * Returns the index of the first whitespace character or '-' in <var>line</var> that is at or
   * after <var>start</var>. Returns -1 if no such character is found.
   * 
   * @param line a string
   * @param start where to star looking
   */
  public static int findBreakAfter(String line, int start) {
    int len = line.length();
    for (int i = start; i < len; ++i) {
      char c = line.charAt(i);
      if (Character.isWhitespace(c) || c == '-')
        return i;
    }
    return -1;
  }

  /**
   * Returns an array of strings, one for each line in the string. Lines end with any of cr, lf, or
   * cr lf. A line ending at the end of the string will not output a further, empty string.
   * <p>
   * This code assumes <var>str</var> is not <code>null</code>.
   * 
   * @param str the string to split
   * @return a non-empty list of strings
   */
  public static List<String> splitIntoLines(String str) {
    List<String> strings = new ArrayList<String>();

    int len = str.length();
    if (len == 0) {
      strings.add("");
      return strings;
    }

    int lineStart = 0;

    for (int i = 0; i < len; ++i) {
      char c = str.charAt(i);
      if (c == '\r') {
        int newlineLength = 1;
        if ((i + 1) < len && str.charAt(i + 1) == '\n')
          newlineLength = 2;
        strings.add(str.substring(lineStart, i));
        lineStart = i + newlineLength;
        if (newlineLength == 2) // skip \n next time through loop
          ++i;
      } else if (c == '\n') {
        strings.add(str.substring(lineStart, i));
        lineStart = i + 1;
      }
    }
    if (lineStart < len)
      strings.add(str.substring(lineStart));

    return strings;
  }

  public static void drawCenteredText(Graphics2D g2d, String text, double x, double y, HorizontalAlignment horizontalAlignment,
      VerticalAlignment verticalAlignment) {
    if (text == null)
      return;
    
    String[] parts = text.split("\\<br\\>");
    if (parts.length > 1) {
      FontMetrics fontMetrics = g2d.getFontMetrics();
      Rectangle stringBounds = fontMetrics.getStringBounds(parts[0], g2d).getBounds();
      for (int i = 0; i < parts.length; i++)
        drawCenteredText(g2d, parts[i], x, (int)(y - stringBounds.height * (parts.length - 1) / 2d + i * stringBounds.height), horizontalAlignment, verticalAlignment);
      return;
    }
    
    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle stringBounds = fontMetrics.getStringBounds(text, g2d).getBounds();

    Font font = g2d.getFont();
    FontRenderContext renderContext = g2d.getFontRenderContext();
    GlyphVector glyphVector = font.createGlyphVector(renderContext, text);
    Rectangle visualBounds = glyphVector.getVisualBounds().getBounds();

    double textX = 0;
    switch (horizontalAlignment) {
      case CENTER:
        textX = x - stringBounds.width / 2;
        break;
      case LEFT:
        textX = x;
        break;
      case RIGHT:
        textX = x - stringBounds.width;
        break;
    }

    double textY = 0;
    switch (verticalAlignment) {
      case TOP:
        textY = y;
        break;
      case CENTER:
        textY = y - visualBounds.height / 2 - visualBounds.y;
        break;
      case BOTTOM:
        textY = y - visualBounds.y;
        break;
    }   

    g2d.drawString(text, (int)Math.round(textX), (int)Math.round(textY));
  }
}
