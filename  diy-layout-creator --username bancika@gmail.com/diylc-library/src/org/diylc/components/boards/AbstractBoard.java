package org.diylc.components.boards;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;

import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

public abstract class AbstractBoard extends AbstractTransparentComponent<String> {

	private static final long serialVersionUID = 1L;

	public static Color BOARD_COLOR = Color.decode("#F8EBB3");
	public static Color BORDER_COLOR = BOARD_COLOR.darker();
	public static Color COORDINATE_COLOR = Color.gray.brighter();
	public static float COORDINATE_FONT_SIZE = 9f;
	public static Size DEFAULT_WIDTH = new Size(1.5d, SizeUnit.in);
	public static Size DEFAULT_HEIGHT = new Size(1.2d, SizeUnit.in);

	protected String value = "";
	protected Point[] controlPoints = new Point[] {
			new Point(0, 0),
			new Point((int) DEFAULT_WIDTH.convertToPixels(), (int) DEFAULT_HEIGHT.convertToPixels()) };
	protected Point firstPoint = new Point();
	protected Point secondPoint = new Point();

	protected Color boardColor = BOARD_COLOR;
	protected Color borderColor = BORDER_COLOR;
	protected Color coordinateColor = COORDINATE_COLOR;
	protected Boolean drawCoordinates = true;

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
			Project project, IDrawingObserver drawingObserver) {
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		if (componentState != ComponentState.DRAGGING) {
			Composite oldComposite = g2d.getComposite();
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
						/ MAX_ALPHA));
			}
			g2d.setColor(boardColor);
			g2d.fillRect(firstPoint.x, firstPoint.y, secondPoint.x - firstPoint.x, secondPoint.y
					- firstPoint.y);
			g2d.setComposite(oldComposite);
		}
		// Do not track any changes that follow because the whole board has been
		// tracked so far.
		drawingObserver.stopTracking();
		g2d.setColor(componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : borderColor);
		g2d.drawRect(firstPoint.x, firstPoint.y, secondPoint.x - firstPoint.x, secondPoint.y
				- firstPoint.y);
	}

	protected void drawCoordinates(Graphics2D g2d, int spacing) {
		if (!getDrawCoordinates())
			return;
		Point p = new Point(firstPoint);
		g2d.setColor(coordinateColor);
		g2d.setFont(LABEL_FONT.deriveFont(COORDINATE_FONT_SIZE));

		int t = 1;
		while (p.y < secondPoint.y - spacing) {
			p.y += spacing;
			super.drawCenteredText(g2d, getCoordinateLabel(t++), p.x + 2, p.y,
					HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
		}

		p = new Point(firstPoint);
		t = 1;
		while (p.x < secondPoint.x - spacing) {
			p.x += spacing;
			super.drawCenteredText(g2d, getCoordinateLabel(t++), p.x, p.y - 2,
					HorizontalAlignment.CENTER, VerticalAlignment.TOP);
		}
	}

	private String getCoordinateLabel(int coordinate) {
		String result = "";
		while (coordinate > 0) {
			int digit = coordinate % 26;
			coordinate /= 26;
			if (digit == 0) {
				result = 'Z' + result;
				coordinate--;
			} else {
				result = (char) ((int) 'A' + digit - 1) + result;
			}
		}
		return result;
	}

	@EditableProperty(name = "Color")
	public Color getBoardColor() {
		return boardColor;
	}

	public void setBoardColor(Color boardColor) {
		this.boardColor = boardColor;
	}

	@EditableProperty(name = "Coordinate Color")
	public Color getCoordinateColor() {
		// Null protection for older files
		return coordinateColor == null ? COORDINATE_COLOR : coordinateColor;
	}

	public void setCoordinateColor(Color coordinateColor) {
		this.coordinateColor = coordinateColor;
	}

	@EditableProperty(name = "Border")
	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	@EditableProperty(name = "Draw Coordinates")
	public boolean getDrawCoordinates() {
		// Null protection for older files
		return drawCoordinates == null || drawCoordinates;
	}

	public void setDrawCoordinates(boolean drawCoordinates) {
		this.drawCoordinates = drawCoordinates;
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
	public boolean isControlPointSticky(int index) {
		return false;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.WHEN_SELECTED;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		controlPoints[index].setLocation(point);
		firstPoint.setLocation(Math.min(controlPoints[0].x, controlPoints[1].x), Math.min(
				controlPoints[0].y, controlPoints[1].y));
		secondPoint.setLocation(Math.max(controlPoints[0].x, controlPoints[1].x), Math.max(
				controlPoints[0].y, controlPoints[1].y));
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
