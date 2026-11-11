package org.diylc.filter;

import org.diylc.common.PCBLayer;
import org.diylc.core.IBoard;
import org.diylc.core.IBoardSideComponent;
import org.diylc.core.IDIYComponent;
import org.diylc.core.gerber.GerberRenderMode;
import org.diylc.core.gerber.IGerberComponent;

import java.util.Set;

public class SilkScreenFilter implements IComponentFilter {

  private PCBLayer side;

  public SilkScreenFilter() {
    this(null);
  }

  /**
   * @param side the side of the board whose silkscreen to keep, or null for both; board outlines
   *        are kept on either side
   */
  public SilkScreenFilter(PCBLayer side) {
    this.side = side;
  }

  @Override
  public boolean testComponent(IDIYComponent<?> component) {
    if (component instanceof IBoard board) {
      return board.shouldExportToGerber();
    }
    if (!(component instanceof IGerberComponent gerberComponent)) {
      return false;
    }
    if (side != null && getSide(component) != side) {
      return false;
    }
    Set<GerberRenderMode> gerberRenderModes = gerberComponent.getGerberRenderModes();
    return gerberRenderModes.contains(GerberRenderMode.Outline);
  }

  // components that do not say which side they are on are on top
  private static PCBLayer getSide(IDIYComponent<?> component) {
    return component instanceof IBoardSideComponent sided && sided.getBoardSide() == PCBLayer._1
        ? PCBLayer._1 : PCBLayer._2;
  }
}
