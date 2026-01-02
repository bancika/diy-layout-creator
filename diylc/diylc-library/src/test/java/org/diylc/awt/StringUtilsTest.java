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

import static org.junit.Assert.*;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class StringUtilsTest {

  private FontMetrics fontMetrics;
  private Graphics2D g2d;

  @Before
  public void setUp() {
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    g2d = image.createGraphics();
    g2d.setFont(new Font("Arial", Font.PLAIN, 12));
    fontMetrics = g2d.getFontMetrics();
  }

  @Test
  public void testWrapWithNewline() {
    String text = "Line 1\nLine 2\nLine 3";
    int maxWidth = 1000; // Large enough to not wrap
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertEquals("Should split into 3 lines", 3, lines.size());
    assertEquals("Line 1", lines.get(0));
    assertEquals("Line 2", lines.get(1));
    assertEquals("Line 3", lines.get(2));
  }

  @Test
  public void testWrapWithCarriageReturn() {
    String text = "Line 1\rLine 2\rLine 3";
    int maxWidth = 1000;
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertEquals("Should split into 3 lines", 3, lines.size());
    assertEquals("Line 1", lines.get(0));
    assertEquals("Line 2", lines.get(1));
    assertEquals("Line 3", lines.get(2));
  }

  @Test
  public void testWrapWithCarriageReturnLineFeed() {
    String text = "Line 1\r\nLine 2\r\nLine 3";
    int maxWidth = 1000;
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertEquals("Should split into 3 lines", 3, lines.size());
    assertEquals("Line 1", lines.get(0));
    assertEquals("Line 2", lines.get(1));
    assertEquals("Line 3", lines.get(2));
  }

  @Test
  public void testWrapWithMixedNewlines() {
    String text = "Line 1\nLine 2\rLine 3\r\nLine 4";
    int maxWidth = 1000;
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertEquals("Should split into 4 lines", 4, lines.size());
    assertEquals("Line 1", lines.get(0));
    assertEquals("Line 2", lines.get(1));
    assertEquals("Line 3", lines.get(2));
    assertEquals("Line 4", lines.get(3));
  }

  @Test
  public void testWrapWithNewlineAndBrTag() {
    String text = "Line 1\nLine 2<br>Line 3";
    int maxWidth = 1000;
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertEquals("Should split into 3 lines", 3, lines.size());
    assertEquals("Line 1", lines.get(0));
    assertEquals("Line 2", lines.get(1));
    assertEquals("Line 3", lines.get(2));
  }

  @Test
  public void testWrapWithNewlineAndWrapping() {
    String text = "First line\nSecond line with very long text that should wrap";
    int maxWidth = fontMetrics.stringWidth("Second line with very long text that should"); // Force wrapping
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertTrue("Should have at least 2 lines (newline + wrapped)", lines.size() >= 2);
    assertEquals("First line", lines.get(0));
    // The second line should be wrapped, so we should have more lines
    assertTrue("Second line should be wrapped", lines.size() >= 3);
  }

  @Test
  public void testWrapWithEmptyLines() {
    String text = "Line 1\n\nLine 3";
    int maxWidth = 1000;
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertEquals("Should split into 3 lines including empty line", 3, lines.size());
    assertEquals("Line 1", lines.get(0));
    assertEquals("", lines.get(1));
    assertEquals("Line 3", lines.get(2));
  }

  @Test
  public void testWrapWithNewlineAtEnd() {
    String text = "Line 1\nLine 2\n";
    int maxWidth = 1000;
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertEquals("Should split into 2 lines (no trailing empty line)", 2, lines.size());
    assertEquals("Line 1", lines.get(0));
    assertEquals("Line 2", lines.get(1));
  }

  @Test
  public void testWrapWithNoNewlines() {
    String text = "This is a single line of text";
    int maxWidth = 1000;
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertEquals("Should have 1 line", 1, lines.size());
    assertEquals("This is a single line of text", lines.get(0));
  }

  @Test
  public void testWrapWithBrTagOnly() {
    String text = "Line 1<br>Line 2<br>Line 3";
    int maxWidth = 1000;
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertEquals("Should split into 3 lines", 3, lines.size());
    assertEquals("Line 1", lines.get(0));
    assertEquals("Line 2", lines.get(1));
    assertEquals("Line 3", lines.get(2));
  }

  @Test
  public void testWrapWithNewlineAndBrTagMixed() {
    String text = "Line 1\nLine 2<br>Line 3\nLine 4<br>Line 5";
    int maxWidth = 1000;
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertEquals("Should split into 5 lines", 5, lines.size());
    assertEquals("Line 1", lines.get(0));
    assertEquals("Line 2", lines.get(1));
    assertEquals("Line 3", lines.get(2));
    assertEquals("Line 4", lines.get(3));
    assertEquals("Line 5", lines.get(4));
  }

  @Test
  public void testWrapWithMultipleConsecutiveNewlines() {
    String text = "Line 1\n\n\nLine 4";
    int maxWidth = 1000;
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertEquals("Should split into 4 lines with 2 empty lines", 4, lines.size());
    assertEquals("Line 1", lines.get(0));
    assertEquals("", lines.get(1));
    assertEquals("", lines.get(2));
    assertEquals("Line 4", lines.get(3));
  }

  @Test
  public void testWrapWithMultipleConsecutiveNewlinesAtStart() {
    String text = "\n\nLine 3";
    int maxWidth = 1000;
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertEquals("Should split into 3 lines with 2 empty lines at start", 3, lines.size());
    assertEquals("", lines.get(0));
    assertEquals("", lines.get(1));
    assertEquals("Line 3", lines.get(2));
  }

  @Test
  public void testWrapWithMultipleConsecutiveNewlinesAtEnd() {
    String text = "Line 1\n\n\n";
    int maxWidth = 1000;
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    // Trailing newlines: the last newline at end doesn't create an empty line
    // So \n\n\n at end creates 2 empty lines (from first 2 newlines)
    assertEquals("Should split into 3 lines (2 empty from trailing newlines)", 3, lines.size());
    assertEquals("Line 1", lines.get(0));
    assertEquals("", lines.get(1));
    assertEquals("", lines.get(2));
  }

  @Test
  public void testWrapWithMultipleConsecutiveNewlinesMixed() {
    String text = "Line 1\n\nLine 3\n\n\nLine 6";
    int maxWidth = 1000;
    
    List<String> lines = StringUtils.wrap(text, fontMetrics, maxWidth);
    
    assertEquals("Should split into 6 lines", 6, lines.size());
    assertEquals("Line 1", lines.get(0));
    assertEquals("", lines.get(1));
    assertEquals("Line 3", lines.get(2));
    assertEquals("", lines.get(3));
    assertEquals("", lines.get(4));
    assertEquals("Line 6", lines.get(5));
  }
}

