package org.diylc.components.transform;

import java.awt.geom.Point2D;

import org.diylc.common.IComponentTransformer;
import org.diylc.components.boards.AbstractBoard;
import org.diylc.components.boards.AbstractBoard.CoordinateOrigin;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.IDIYComponent;

public class AbstractBoardTransformer extends SimpleComponentTransformer {
  
  @Override
  public void rotate(IDIYComponent<?> component, Point2D center, int direction) {
    super.rotate(component, center, direction);
    
    AbstractBoard board = (AbstractBoard)component;
    CoordinateOrigin origin = board.getCoordinateOrigin();
    
    if (direction == IComponentTransformer.CLOCKWISE) {
      switch (origin) {
        case Bottom_Left:
          origin = CoordinateOrigin.Top_Left;
          break;
        case Bottom_Right:
          origin = CoordinateOrigin.Bottom_Left;
          break;
        case Top_Left:
          origin = CoordinateOrigin.Top_Right;
          break;
        case Top_Right:
          origin = CoordinateOrigin.Bottom_Right;
          break;
      }
    } else {
      switch (origin) {
        case Bottom_Left:
          origin = CoordinateOrigin.Bottom_Right;
          break;
        case Bottom_Right:
          origin = CoordinateOrigin.Top_Right;
          break;
        case Top_Left:
          origin = CoordinateOrigin.Bottom_Left;
          break;
        case Top_Right:
          origin = CoordinateOrigin.Top_Left;
          break;
      }
    }
    
    board.setCoordinateOrigin(origin);
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point2D center, int direction) {
    super.mirror(component, center, direction);
    
    AbstractBoard board = (AbstractBoard)component;
    CoordinateOrigin origin = board.getCoordinateOrigin();
    if (direction == IComponentTransformer.HORIZONTAL) {
      switch (origin) {
        case Bottom_Left:
          origin = CoordinateOrigin.Bottom_Right;
          break;
        case Bottom_Right:
          origin = CoordinateOrigin.Bottom_Left;
          break;
        case Top_Left:
          origin = CoordinateOrigin.Top_Right;
          break;
        case Top_Right:
          origin = CoordinateOrigin.Top_Left;
          break;
      }
    } else {
      switch (origin) {
        case Bottom_Left:
          origin = CoordinateOrigin.Top_Left;
          break;
        case Bottom_Right:
          origin = CoordinateOrigin.Top_Right;
          break;
        case Top_Left:
          origin = CoordinateOrigin.Bottom_Left;
          break;
        case Top_Right:
          origin = CoordinateOrigin.Top_Right;
          break;
      }
    }
    
    board.setCoordinateOrigin(origin);
  }
}
