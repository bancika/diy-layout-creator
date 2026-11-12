package org.diylc.core;

import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

import java.awt.geom.Rectangle2D;

public interface IBoard {

  Size DEFAULT_UNDERSIDE_OFFSET = new Size(0.1, SizeUnit.in);
  
  Rectangle2D getBoardRectangle();

  BoardUndersideDisplay getUndersideDisplay();

  Size getUndersideOffset();

  Boolean getUndersideTransparency();
  
  boolean shouldExportToGerber();
}
