package org.diylc.components.boards;

import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
import org.diylc.core.BoardUndersideDisplay;
import org.diylc.core.IBoard;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

public abstract class AbstractProtoBoard extends AbstractComponent<Void> implements IBoard {

  private static final long serialVersionUID = 1L;

  public static final Size DEFAULT_UNDERSIDE_OFFSET = new Size(0.1, SizeUnit.in);
  
  protected Orientation orientation;

  protected BoardUndersideDisplay boardUndersideDisplay = BoardUndersideDisplay.NONE;
  protected Size undersideOffset = DEFAULT_UNDERSIDE_OFFSET;
  private Boolean undersideTransparency = true;

  @EditableProperty
  public Orientation getOrientation() {
    if (orientation == null)
      orientation = Orientation.DEFAULT;
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
  }

  @EditableProperty(name = "Underside Display")
  @Override
  public BoardUndersideDisplay getUndersideDisplay() {
    if (this.boardUndersideDisplay == null) {
      this.boardUndersideDisplay = BoardUndersideDisplay.NONE;
    }
    return this.boardUndersideDisplay;
  }

  public void setUndersideDisplay(BoardUndersideDisplay boardUndersideDisplay) {
    this.boardUndersideDisplay = boardUndersideDisplay;
  }

  @EditableProperty(name = "Underside Offset")
  @Override
  public Size getUndersideOffset() {
    if (undersideOffset == null) {
      undersideOffset = DEFAULT_UNDERSIDE_OFFSET;
    }
    return undersideOffset;
  }

  public void setUndersideOffset(Size undersideOffset) {
    this.undersideOffset = undersideOffset;
  }

  @EditableProperty(name = "Underside Transparency")
  @Override
  public Boolean getUndersideTransparency() {
    if (undersideTransparency == null) {
      undersideTransparency = true;
    }
    return undersideTransparency;
  }

  public void setUndersideTransparency(Boolean undersideTransparency) {
    this.undersideTransparency = undersideTransparency;
  }

  @Override
  public boolean shouldExportToGerber() {
    return false;
  }
}
