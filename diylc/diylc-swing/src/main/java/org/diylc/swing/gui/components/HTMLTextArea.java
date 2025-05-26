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
package org.diylc.swing.gui.components;

import javax.swing.JTextArea;

/**
 * {@link JTextArea} that replaces new line characters with html &lt;br&gt; tags.
 * 
 * @author Branislav Stojkovic
 */
public class HTMLTextArea extends JTextArea {

  private static final long serialVersionUID = 1L;

  public HTMLTextArea(String text) {
    super(text.replace("<br>", "\n"));
  }

  public HTMLTextArea() {
    super();
  }

  @Override
  public String getText() {
    return super.getText().replace("\n", "<br>");
  }

  @Override
  public void setText(String t) {
    super.setText(t.replace("<br>", "\n"));
  }
}
