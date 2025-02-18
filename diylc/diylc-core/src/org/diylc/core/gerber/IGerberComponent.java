package org.diylc.core.gerber;

import java.awt.Graphics2D;
import java.util.Set;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;

public interface IGerberComponent {

  Set<GerberRenderMode> getGerberRenderModes();
  
  void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver, IGerberDrawingObserver gerberDrawingObserver);
}
