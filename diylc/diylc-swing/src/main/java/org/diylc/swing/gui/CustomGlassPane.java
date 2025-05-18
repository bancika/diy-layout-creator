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
package org.diylc.swing.gui;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

public class CustomGlassPane extends JPanel implements MouseListener, KeyListener {

  public static final CustomGlassPane GLASS_PANE = new CustomGlassPane();

  private static final long serialVersionUID = -5344758920442881290L;

  public CustomGlassPane() {
    addKeyListener(this);
    addMouseListener(this);
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    setOpaque(false);
  }

  @Override
  public void mouseClicked(final MouseEvent pArg0) {}

  @Override
  public void mouseEntered(final MouseEvent pArg0) {}

  @Override
  public void mouseExited(final MouseEvent pArg0) {}

  @Override
  public void mousePressed(final MouseEvent pArg0) {}

  @Override
  public void mouseReleased(final MouseEvent pArg0) {}

  @Override
  public void keyPressed(KeyEvent e) {}

  @Override
  public void keyReleased(KeyEvent e) {}

  @Override
  public void keyTyped(KeyEvent e) {}
}
