package org.diylc.components.boards;

import org.diylc.components.boards.AbstractBoard.BoardSizingMode;
import org.diylc.core.IPropertyValidator;
import org.diylc.core.ValidationException;
import org.diylc.core.measures.Size;

public class BoardModeValidator implements IPropertyValidator {
  
  public BoardModeValidator() {}

  @Override
  public void validate(Object owner, Object value) throws ValidationException {
    AbstractBoard board = (AbstractBoard)owner;
    Size size = (Size)value;
    if (board.getMode() == BoardSizingMode.Explicit && (value == null || size.getValue() == null || size.getUnit() == null))
      throw new ValidationException("Dimensions are required when mode is set to " + BoardSizingMode.Explicit);
  }    
}