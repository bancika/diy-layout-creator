package org.diylc.filter;

import org.diylc.core.IBoard;
import org.diylc.core.IDIYComponent;
import org.diylc.core.gerber.GerberRenderMode;
import org.diylc.core.gerber.IGerberComponent;

import java.util.Set;

public class SilkScreenFilter implements IComponentFilter {

  @Override
  public boolean testComponent(IDIYComponent<?> component) {
    if (component instanceof IBoard board) {
      return board.shouldExportToGerber();
    }
    if (!(component instanceof IGerberComponent gerberComponent)) {
      return false;
    }
    Set<GerberRenderMode> gerberRenderModes = gerberComponent.getGerberRenderModes();
    return gerberRenderModes.contains(GerberRenderMode.Outline);
  }
}
