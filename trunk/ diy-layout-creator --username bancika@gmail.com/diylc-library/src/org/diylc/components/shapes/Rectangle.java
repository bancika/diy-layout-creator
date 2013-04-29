package org.diylc.components.shapes;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;

import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Rectangle", author = "Branislav Stojkovic", category = "Shapes", instanceNamePrefix = "RECT", description = "Ractangular area, with or withouth rounded edges", zOrder = IDIYComponent.COMPONENT, flexibleZOrder = true, bomPolicy = BomPolicy.SHOW_ALL_NAMES, autoEdit = false)
public class Rectangle extends AbstractTransparentComponent<Void> {

	private static final long serialVersionUID = 1L;

	public static Color COLOR = Color.white;
	public static Color BORDER_COLOR = Color.black;
	public static Size DEFAULT_WIDTH = new Size(1.5d, SizeUnit.in);
	public static Size DEFAULT_HEIGHT = new Size(1.2d, SizeUnit.in);

	protected String value = "";
	protected Point[] controlPoints = new Point[] {
			new Point(0, 0),
			new Point((int) DEFAULT_WIDTH.convertToPixels(),
					(int) DEFAULT_HEIGHT.convertToPixels()) };
	protected Point firstPoint = new Point();
	protected Point secondPoint = new Point();

	protected Color color = COLOR;
	protected Color borderColor = BORDER_COLOR;
	protected Size edgeRadius = new Size(0d, SizeUnit.mm);
	protected Size borderThickness = new Size(0.2d, SizeUnit.mm);

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState,
			boolean outlineMode, Project project,
			IDrawingObserver drawingObserver) {
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke((int) borderThickness.convertToPixels()));
		int radius = (int) edgeRadius.convertToPixels();
		if (componentState != ComponentState.DRAGGING) {
			Composite oldComposite = g2d.getComposite();
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
			}
			g2d.setColor(color);
			g2d.fillRoundRect(firstPoint.x, firstPoint.y, secondPoint.x
					- firstPoint.x, secondPoint.y - firstPoint.y, radius,
					radius);
			g2d.setComposite(oldComposite);
		}
		// Do not track any changes that follow because the whole rect has been
		// tracked so far.
		drawingObserver.stopTracking();
		g2d.setColor(componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR
				: borderColor);
		g2d.drawRoundRect(firstPoint.x, firstPoint.y, secondPoint.x
				- firstPoint.x, secondPoint.y - firstPoint.y, radius, radius);
	}

	@EditableProperty(name = "Color")
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@EditableProperty(name = "Border")
	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}
	
	@EditableProperty(name = "Border thickness")
	public Size getBorderThickness() {
		return borderThickness;
	}
	
	public void setBorderThickness(Size borderThickness) {
		this.borderThickness = borderThickness;
	}

	@EditableProperty(name = "Radius")
	public Size getEdgeRadius() {
		return edgeRadius;
	}

	public void setEdgeRadius(Size edgeRadius) {
		this.edgeRadius = edgeRadius;
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
		firstPoint.setLocation(
				Math.min(controlPoints[0].x, controlPoints[1].x), Math.min(
						controlPoints[0].y, controlPoints[1].y));
		secondPoint.setLocation(Math
				.max(controlPoints[0].x, controlPoints[1].x), Math.max(
				controlPoints[0].y, controlPoints[1].y));
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int factor = 32 / width;
		g2d.setColor(COLOR);
		g2d.fillRect(2 / factor, 2 / factor, width - 4 / factor, height - 4
				/ factor);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRect(2 / factor, 2 / factor, width - 4 / factor, height - 4
				/ factor);
	}

	@Deprecated
	@Override
	public Void getValue() {
		return null;
	}

	@Deprecated
	@Override
	public void setValue(Void value) {
	}
}
