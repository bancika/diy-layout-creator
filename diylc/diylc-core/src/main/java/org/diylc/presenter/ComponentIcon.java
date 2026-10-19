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
package org.diylc.presenter;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import org.apache.log4j.Logger;

import org.diylc.core.IDIYComponent;

/**
 * Toolbox icon of a component type. Drawing it means instantiating the component, so it is
 * deferred until the icon is first painted rather than done for every type at startup.
 *
 * @author Branislav Stojkovic
 */
class ComponentIcon implements Icon {

  private static final Logger LOG = Logger.getLogger(ComponentIcon.class);

  private final Class<? extends IDIYComponent<?>> componentClass;
  private BufferedImage image;

  ComponentIcon(Class<? extends IDIYComponent<?>> componentClass) {
    this.componentClass = componentClass;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    if (image == null) {
      image = drawImage();
    }
    g.drawImage(image, x, y, null);
  }

  @Override
  public int getIconWidth() {
    return Presenter.ICON_SIZE;
  }

  @Override
  public int getIconHeight() {
    return Presenter.ICON_SIZE;
  }

  private BufferedImage drawImage() {
    BufferedImage image =
        new BufferedImage(Presenter.ICON_SIZE, Presenter.ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = image.createGraphics();
    try {
      IDIYComponent<?> componentInstance = componentClass.getDeclaredConstructor().newInstance();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
          RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      componentInstance.drawIcon(g2d, Presenter.ICON_SIZE, Presenter.ICON_SIZE);
    } catch (Exception e) {
      LOG.error("Error drawing component icon for " + componentClass.getName(), e);
    } finally {
      g2d.dispose();
    }
    return image;
  }
}
