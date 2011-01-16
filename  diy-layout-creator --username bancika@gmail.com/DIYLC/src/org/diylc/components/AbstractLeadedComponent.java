package org.diylc.components;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.diylc.common.Display;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

/**
 * Base class for all leaded components such as resistors or capacitors. Has two
 * control points and draws leads between them. Also, it positions and draws the
 * shape of the component as specified by a child class.
 * 
 * @author Branislav Stojkovic
 */
public abstract class AbstractLeadedComponent<T> extends AbstractTransparentComponent<T> {

	private static final long serialVersionUID = 1L;

	public static Color LEAD_COLOR = Color.decode("#65909A");
	public static Color LABEL_COLOR = Color.black;
	public static Color LABEL_COLOR_SELECTED = Color.red;
	public static Size LEAD_THICKNESS = new Size(0.8d, SizeUnit.mm);
	public static Size DEFAULT_SIZE = new Size(1d, SizeUnit.in);

	protected Size width;
	protected Size height;
	protected Point[] points = new Point[] {
			new Point((int) (-DEFAULT_SIZE.convertToPixels() / 2), 0),
			new Point(DEFAULT_SIZE.convertToPixels() / 2, 0) };
	protected Color bodyColor = getDefaultBodyColor();
	protected Color borderColor = getDefaultBorderColor();
	protected Display display = Display.NAME;

	protected AbstractLeadedComponent() {
		super();
		try {
			this.width = getDefaultWidth().clone();
			this.height = getDefaultHeight().clone();
		} catch (CloneNotSupportedException e) {
			// This should never happen because Size supports cloning.
		} catch (NullPointerException e) {
			// This will happen if components do not have any shape.
		}
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project,
			IDrawingObserver drawingObserver) {
		double distance = points[0].distance(points[1]);
		Shape shape = getBodyShape();
		if (shape == null) {
			g2d.setStroke(new BasicStroke(getLeadThickness().convertToPixels()));
			g2d.setColor(getLeadColor(componentState));
			g2d.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
			return;
		}
		Rectangle shapeRect = shape.getBounds();
		double leadLenght = (distance - shapeRect.width) / 2;
		Double theta = Math.atan2(points[1].y - points[0].y, points[1].x - points[0].x);
		// Transform graphics to draw the body in the right place and at the
		// right angle.
		g2d.translate((points[0].x + points[1].x - shapeRect.width) / 2,
				(points[0].y + points[1].y - shapeRect.height) / 2);
		g2d.rotate(theta, shapeRect.width / 2, shapeRect.height / 2);
		// Draw body.
		Composite oldComposite = g2d.getComposite();
		if (alpha < MAX_ALPHA) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
					/ MAX_ALPHA));
		}
		g2d.setColor(bodyColor);
		g2d.fill(shape);
		g2d.setComposite(oldComposite);
		g2d.setStroke(Constants.BASIC_STROKE);
		g2d.setColor(componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : borderColor);
		g2d.draw(shape);
		// Draw leads.
		int leadThickness = getLeadThickness().convertToPixels();
		g2d.setStroke(new BasicStroke(leadThickness));
		g2d.setColor(getLeadColor(componentState));
		g2d.drawLine((int) (shapeRect.width - distance) / 2, (int) shapeRect.height / 2,
				(int) ((shapeRect.width - distance) / 2 + leadLenght) - leadThickness / 2,
				(int) shapeRect.height / 2);
		g2d.drawLine((int) (shapeRect.width + distance) / 2, (int) shapeRect.height / 2,
				(int) ((shapeRect.width + distance) / 2 - leadLenght) + leadThickness / 2,
				(int) shapeRect.height / 2);
		// Do not track the label because that area was already covered.
		drawingObserver.stopTracking();
		// Draw label.
		g2d.setFont(Constants.LABEL_FONT);
		g2d
				.setColor(componentState == ComponentState.SELECTED ? LABEL_COLOR_SELECTED
						: LABEL_COLOR);
		FontMetrics fontMetrics = g2d.getFontMetrics();
		String label = display == Display.NAME ? getName() : getValue().toString();
		Rectangle2D textRect = fontMetrics.getStringBounds(label, g2d);
		g2d.drawString(label, (int) (shapeRect.width - textRect.getWidth()) / 2,
				(int) (shapeRect.height - textRect.getHeight()) / 2 + fontMetrics.getAscent());
	}

	/**
	 * @return default component width.
	 */
	protected abstract Size getDefaultWidth();

	/**
	 * Returns default component height.
	 * 
	 * @return
	 */
	protected abstract Size getDefaultHeight();

	/**
	 * @return shape that represents component body. Shape should not be
	 *         transformed and should be referenced to (0, 0).
	 */
	protected abstract Shape getBodyShape();

	/**
	 * @return default component body color.
	 */
	protected abstract Color getDefaultBodyColor();

	/**
	 * @return default component border color.
	 */
	protected abstract Color getDefaultBorderColor();

	/**
	 * @return default lead thickness. Override this method to change it.
	 */
	protected Size getLeadThickness() {
		return LEAD_THICKNESS;
	}

	/**
	 * @return default lead color. Override this method to change it.
	 */
	protected Color getLeadColor(ComponentState componentState) {
		return componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : LEAD_COLOR;
	}

	@Override
	public int getControlPointCount() {
		return points.length;
	}

	@Override
	public Point getControlPoint(int index) {
		return (Point) points[index];
	}

	@Override
	public void setControlPoint(Point point, int index) {
		points[index].setLocation(point);
	}

	@EditableProperty(name = "Color")
	public Color getBodyColor() {
		return bodyColor;
	}

	public void setBodyColor(Color bodyColor) {
		this.bodyColor = bodyColor;
	}

	@EditableProperty(name = "Border")
	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	@EditableProperty(defaultable = true)
	public Size getWidth() {
		return width;
	}

	public void setWidth(Size width) {
		this.width = width;
	}

	@EditableProperty(defaultable = true)
	public Size getHeight() {
		return height;
	}

	public void setHeight(Size height) {
		this.height = height;
	}

	@EditableProperty
	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}
}
