package org.diylc.core.gerber;

import java.util.EnumSet;
import java.util.Set;

public interface IGerberComponent {

  default Set<GerberRenderMode> getGerberRenderModes() {
    return EnumSet.of(GerberRenderMode.Outline);
  }
}
