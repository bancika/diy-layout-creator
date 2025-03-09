package org.diylc.core;

import java.awt.geom.Rectangle2D;

public interface IBoard {
  
  Rectangle2D getBoardRectangle();

  BoardUndersideDisplay getBoardUndersideDisplay();
  
  boolean shouldExportToGerber();
}
