package org.diylc.components.boards;

import java.awt.Color;

import org.diylc.common.OrientationHV;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

public abstract class AbstractVeroBoard extends AbstractBoard {
  
  private static final long serialVersionUID = 1L;  

  public static Size SPACING = new Size(0.1d, SizeUnit.in);
  public static Size STRIP_SIZE = new Size(0.07d, SizeUnit.in);
  public static Size HOLE_SIZE = new Size(0.7d, SizeUnit.mm);

  // strip
  protected Size spacing = SPACING;
  protected Color stripColor = COPPER_COLOR;
  protected OrientationHV orientation = OrientationHV.HORIZONTAL;
  
  @EditableProperty(name = "Strip Color")
  public Color getStripColor() {
    return stripColor;
  }

  public void setStripColor(Color padColor) {
    this.stripColor = padColor;
  }

  @EditableProperty(validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getSpacing() {
    return spacing;
  }

  public void setSpacing(Size spacing) {
    this.spacing = spacing;
  }

  @EditableProperty
  public OrientationHV getOrientation() {
    return orientation;
  }

  public void setOrientation(OrientationHV orientation) {
    this.orientation = orientation;
  } 
}
