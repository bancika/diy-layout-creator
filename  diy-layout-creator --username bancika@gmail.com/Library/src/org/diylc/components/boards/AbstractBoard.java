package org.diylc.components.boards;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;

import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

public abstract class AbstractBoard extends AbstractTransparentComponent<String> {

	private static final long serialVersionUID = 1L;

	public static Color BOARD_COLOR = Color.decode("#BCED91");
	public static Color BORDER_COLOR = BOARD_COLOR.darker();
	public static Size DEFAULT_WIDTH = new Size(1.5d, SizeUnit.in);
	public static Size DEFAULT_HEIGHT = new Size(1d, SizeUnit.in);

	protected String value;
	protected Point[] controlPoints = new Point[] {
			new Point(-DEFAULT_WIDTH.convertToPixels() / 2, -DEFAULT_HEIGHT.convertToPixels() / 2),
			new Point(DEFAULT_WIDTH.convertToPixels() / 2, DEFAULT_HEIGHT.convertToPixels() / 2) };

	protected Color boardColor = BOARD_COLOR;
	protected Color borderColor = BORDER_COLOR;

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project,
			IDrawingObserver drawingObserver) {
		g2d.setStroke(new BasicStroke());
		if (componentState != ComponentState.DRAGGING) {
			Composite oldComposite = g2d.getComposite();
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
						/ MAX_ALPHA));
			}
			g2d.setColor(boardColor);
			g2d.fillRect(controlPoints[0].x, controlPoints[0].y, controlPoints[1].x
					- controlPoints[0].x, controlPoints[1].y - controlPoints[0].y);
			g2d.setComposite(oldComposite);
		}
		g2d.setColor(componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : borderColor);
		g2d.drawRect(controlPoints[0].x, controlPoints[0].y, controlPoints[1].x
				- controlPoints[0].x, controlPoints[1].y - controlPoints[0].y);
		// Do not track any changes that follow because the whole board has been
		// tracked so far.
		drawingObserver.stopTracking();
	}

	@EditableProperty(name = "Color")
	public Color getBoardColor() {
		return boardColor;
	}

	public void setBoardColor(Color boardColor) {
		this.boardColor = boardColor;
	}

	@EditableProperty(name = "Border")
	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	@Override
	public int getControlPointCount() {
		return controlPoints.length;
	}

	@Override
	public Point getControlPoint(int index) {
		return controlPoints[index];
	}

	@Override
	public void setControlPoint(Point point, int index) {
		controlPoints[index].setLocation(point);
	}

	@EditableProperty
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
