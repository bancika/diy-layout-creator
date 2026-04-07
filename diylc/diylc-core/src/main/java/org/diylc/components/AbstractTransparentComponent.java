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
package org.diylc.components;

import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.ComponentState;
import org.diylc.common.Percentage;

import java.awt.*;

public abstract class AbstractTransparentComponent<T> extends AbstractComponent<T> {

  private static final long serialVersionUID = 1L;
  private static final boolean OUTLINE_MODE_WHILE_DRAGGING = false;

  public static byte MAX_ALPHA_LEGACY = Byte.MAX_VALUE;
  public static int MAX_ALPHA = 100;

  @Deprecated
  protected Byte alpha;

  protected Percentage alphaPercent = new Percentage(100);

  @EditableProperty(name = "Alpha")
  public Percentage getAlpha() {
    if (alphaPercent == null) {
      if (alpha != null) {
        alphaPercent = new Percentage(Math.round(100f * alpha / MAX_ALPHA_LEGACY));
        alpha = null;
      } else {
        alphaPercent = new Percentage(100);
      }
    }
    return alphaPercent;
  }

  public void setAlpha(Percentage alphaPercent) {
    this.alphaPercent = alphaPercent;
  }

  public Composite applyAlpha(Graphics2D g2d, ComponentState componentState) {
    Composite oldComposite = g2d.getComposite();
    int alpha = (OUTLINE_MODE_WHILE_DRAGGING && componentState == ComponentState.DRAGGING) ? 0 :
        getAlpha().getValue();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 100f));
    }
    return oldComposite;
  }

  public Composite applyAlpha(Graphics2D g2d) {
    Composite oldComposite = g2d.getComposite();
    int alpha = getAlpha().getValue();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 100f));
    }
    return oldComposite;
  }
}
