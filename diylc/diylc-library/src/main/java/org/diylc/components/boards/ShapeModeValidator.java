package org.diylc.components.boards;

import org.diylc.components.boards.AbstractBoard.BoardSizingMode;
import org.diylc.components.shapes.AbstractShapeWithDimensions;
import org.diylc.components.shapes.AbstractShapeWithDimensions.ShapeSizingMode;
import org.diylc.core.IPropertyValidator;
import org.diylc.core.ValidationException;
import org.diylc.core.measures.Size;

public class ShapeModeValidator implements IPropertyValidator {
  
  public ShapeModeValidator() {}

  @Override
  public void validate(Object owner, Object value) throws ValidationException {
    AbstractShapeWithDimensions board = (AbstractShapeWithDimensions)owner;
    Size size = (Size)value;
    if (board.getMode() == ShapeSizingMode.Explicit && (value == null || size.getValue() == null || size.getUnit() == null))
      throw new ValidationException("Dimensions are required when mode is set to " + BoardSizingMode.Explicit);
  }    
}