package org.diylc.swing.plugins.toolbox.openide;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.UIManager;

import org.diylc.images.IconLoader;

class IconWithArrow implements Icon {
  private Icon orig;
  private Icon arrow = IconLoader.Arrow.getIcon();
  private boolean paintRollOver;
  private static final int GAP = 6;

  public IconWithArrow(Icon orig, boolean paintRollOver) {
    Parameters.notNull("original icon", orig);
    this.orig = orig;
    this.paintRollOver = paintRollOver;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    int height = getIconHeight();
    this.orig.paintIcon(c, g, x, y + (height - this.orig.getIconHeight()) / 2);

    this.arrow.paintIcon(c, g, x + 6 + this.orig.getIconWidth(), y + (height - this.arrow.getIconHeight()) / 2);
    if (this.paintRollOver) {
      Color brighter = UIManager.getColor("controlHighlight");
      Color darker = UIManager.getColor("controlShadow");
      if ((null == brighter) || (null == darker)) {
        brighter = c.getBackground().brighter();
        darker = c.getBackground().darker();
      }
      if ((null != brighter) && (null != darker)) {
        g.setColor(brighter);
        g.drawLine(x + this.orig.getIconWidth() + 1, y, x + this.orig.getIconWidth() + 1, y + getIconHeight());

        g.setColor(darker);
        g.drawLine(x + this.orig.getIconWidth() + 2, y, x + this.orig.getIconWidth() + 2, y + getIconHeight());
      }
    }
  }

  public int getIconWidth() {
    return this.orig.getIconWidth() + 6 + this.arrow.getIconWidth();
  }

  public int getIconHeight() {
    return Math.max(this.orig.getIconHeight(), this.arrow.getIconHeight());
  }

  public static int getArrowAreaWidth() {
    return 8;
  }
}
