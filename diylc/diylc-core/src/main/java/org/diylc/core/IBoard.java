package org.diylc.core;

import org.diylc.core.measures.Size;

import java.awt.geom.Rectangle2D;

public interface IBoard {
  
  Rectangle2D getBoardRectangle();

  BoardUndersideDisplay getUndersideDisplay();

  Size getUndersideOffset();

  Boolean getUndersideTransparency();
  
  boolean shouldExportToGerber();
}
