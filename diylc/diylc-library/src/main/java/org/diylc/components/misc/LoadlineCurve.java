package org.diylc.components.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.connectivity.CurvedTrace;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.Voltage;

public class LoadlineCurve extends CurvedTrace {

  private static final long serialVersionUID = 1L;
  
  private static final Size LABEL_SPACING = new Size(1d, SizeUnit.mm);

  private Voltage voltage;
  
  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {   
    super.draw(g2d, componentState, outlineMode, project, drawingObserver);
    if (getVoltage() == null)
      return;
    g2d.setColor(Color.black);
    Font oldFont = g2d.getFont();
    g2d.setFont(project.getFont().deriveFont(12f).deriveFont(Font.BOLD));
    Point2D lastPoint = getControlPoint(getControlPointCount() - 1);
    int spacing = (int) LABEL_SPACING.convertToPixels();
    double x = lastPoint.getX() + spacing;
    double y = lastPoint.getY() - spacing;
    StringUtils.drawCenteredText(g2d, getVoltage().toString(), x, y, HorizontalAlignment.LEFT, VerticalAlignment.TOP);
    g2d.setFont(oldFont);
  }
  
  @EditableProperty
  public Voltage getVoltage() {
    return voltage;
  }
  
  public void setVoltage(Voltage voltage) {
    this.voltage = voltage;
  }
}
