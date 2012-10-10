package org.diylc.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;

import org.diylc.common.LabelPosition;
import org.diylc.core.ComponentState;
import org.diylc.core.annotations.EditableProperty;

public abstract class AbstractSchematicLeadedSymbol<T> extends AbstractLeadedComponent<T> {

	private static final long serialVersionUID = 1L;

	public static Color COLOR = Color.blue;
	public static Color LEAD_COLOR = Color.black;

	protected LabelPosition labelPosition = LabelPosition.ABOVE;

	public AbstractSchematicLeadedSymbol() {
		super();
		// We don't want to fill the body, so use null.
		this.bodyColor = null;
		this.leadColor = LEAD_COLOR;
		this.borderColor = COLOR;
	}

	@Override
	public Color getBodyColor() {
		return super.getBodyColor();
	}

	@Override
	protected boolean shouldShadeLeads() {
		return false;
	}

	@Override
	protected int getLeadThickness() {
		return 1;
	}

	@Override
	protected int calculateLabelYCoordinate(Rectangle2D shapeRect, Rectangle2D textRect,
			FontMetrics fontMetrics) {
		if (labelPosition == LabelPosition.ABOVE) {
			return -1;
		} else {
			return (int) (shapeRect.getHeight() + textRect.getHeight() - 1);
		}
	}

	@EditableProperty(name = "Label position")
	public LabelPosition getLabelPosition() {
		return labelPosition;
	}

	@Override
	@EditableProperty(name = "Color")
	public Color getBorderColor() {
		return super.getBorderColor();
	}

	public void setLabelPosition(LabelPosition labelPosition) {
		this.labelPosition = labelPosition;
	}

	@Override
	protected Color getLeadColorForPainting(ComponentState componentState) {
		return componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : getLeadColor();
	}
}
