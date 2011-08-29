package org.diylc.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
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

	public static Color LEAD_COLOR = Color.decode("#236B8E");
	public static Size LEAD_THICKNESS = new Size(0.6d, SizeUnit.mm);
	public static Size DEFAULT_SIZE = new Size(1d, SizeUnit.in);

	protected Size length;
	protected Size width;
	protected Point[] points = new Point[] {
			new Point((int) (-DEFAULT_SIZE.convertToPixels() / 2), 0),
			new Point((int) (DEFAULT_SIZE.convertToPixels() / 2), 0) };
	protected Color bodyColor = Color.white;
	protected Color borderColor = Color.black;
	protected Color labelColor = LABEL_COLOR;
	protected Display display = Display.VALUE;

	protected AbstractLeadedComponent() {
		super();
		try {
			this.length = getDefaultLength().clone();
			this.width = getDefaultWidth().clone();
		} catch (CloneNotSupportedException e) {
			// This should never happen because Size supports cloning.
		} catch (NullPointerException e) {
			// This will happen if components do not have any shape.
		}
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
			Project project, IDrawingObserver drawingObserver) {
		double distance = points[0].distance(points[1]);
		Shape shape = getBodyShape();
		// If there's no body, just draw the line connecting the ending points.
		if (shape == null) {
			drawLead(g2d, componentState);
		} else if (supportsStandingMode()
				&& length.convertToPixels() > points[0].distance(points[1])) {
			// When ending points are too close draw the component in standing
			// mode.
			int width = getClosestOdd(this.width.convertToPixels());
			Shape body = new Ellipse2D.Double(points[0].x - width / 2, points[0].y - width / 2,
					width, width);
			Composite oldComposite = g2d.getComposite();
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
						/ MAX_ALPHA));
			}
			g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
			g2d.fill(body);
			g2d.setComposite(oldComposite);
			Color finalBorderColor;
			if (outlineMode) {
				Theme theme = (Theme) ConfigurationManager.getInstance().readObject(
						IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
				finalBorderColor = componentState == ComponentState.SELECTED
						|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : theme
						.getOutlineColor();
			} else {
				finalBorderColor = componentState == ComponentState.SELECTED
						|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR
						: borderColor;
			}

			g2d.setColor(finalBorderColor);
			g2d.draw(body);
			if (!outlineMode) {
				drawLead(g2d, componentState);
			}
		} else {
			// Normal mode with component body in the center and two lead parts.
			Rectangle shapeRect = shape.getBounds();
			Double theta = Math.atan2(points[1].y - points[0].y, points[1].x - points[0].x);
			// Transform graphics to draw the body in the right place and at the
			// right angle.
			AffineTransform oldTransform = g2d.getTransform();
			g2d.translate((points[0].x + points[1].x - shapeRect.width) / 2, (points[0].y
					+ points[1].y - shapeRect.height) / 2);
			g2d.rotate(theta, shapeRect.width / 2, shapeRect.height / 2);
			// Draw body.
			Composite oldComposite = g2d.getComposite();
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
						/ MAX_ALPHA));
			}
			if (bodyColor != null) {
				if (bodyColor != null) {
					g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
					g2d.fill(shape);
				}
			}
			decorateComponentBody(g2d, outlineMode);
			g2d.setComposite(oldComposite);
			g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
			Color finalBorderColor;
			if (outlineMode) {
				Theme theme = (Theme) ConfigurationManager.getInstance().readObject(
						IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
				finalBorderColor = componentState == ComponentState.SELECTED
						|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : theme
						.getOutlineColor();
			} else {
				finalBorderColor = componentState == ComponentState.SELECTED
						|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR
						: borderColor;
			}
			g2d.setColor(finalBorderColor);
			g2d.draw(shape);
			// Draw label.
			g2d.setFont(LABEL_FONT);
			Color finalLabelColor;
			if (outlineMode) {
				Theme theme = (Theme) ConfigurationManager.getInstance().readObject(
						IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
				finalLabelColor = componentState == ComponentState.SELECTED
						|| componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED : theme
						.getOutlineColor();
			} else {
				finalLabelColor = componentState == ComponentState.SELECTED
						|| componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
						: labelColor;
			}
			g2d.setColor(finalLabelColor);
			FontMetrics fontMetrics = g2d.getFontMetrics();
			String label = display == Display.NAME ? getName() : getValue().toString();
			// Adjust label angle if needed to make sure that it's readable.
			if ((theta >= Math.PI / 2 && theta <= Math.PI)
					|| (theta < -Math.PI / 2 && theta > -Math.PI)) {
				g2d.rotate(Math.PI, shapeRect.width / 2, shapeRect.height / 2);
			}
			Rectangle2D textRect = fontMetrics.getStringBounds(label, g2d);
			g2d.drawString(label, (int) (shapeRect.width - textRect.getWidth()) / 2,
					calculateLabelYCoordinate(shapeRect, textRect, fontMetrics));

			// Go back to the original transformation to draw leads.
			if (!outlineMode) {
				g2d.setTransform(oldTransform);
				int leadThickness = getClosestOdd(getLeadThickness());
				double leadLength = (distance - shapeRect.width) / 2 - leadThickness / 2;
				g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
				Color leadColor = shouldShadeLeads() ? getLeadColor(componentState).darker()
						: getLeadColor(componentState);
				g2d.setColor(leadColor);
				int endX = (int) (points[0].x + Math.cos(theta) * leadLength);
				int endY = (int) Math.round(points[0].y + Math.sin(theta) * leadLength);
				g2d.drawLine(points[0].x, points[0].y, endX, endY);
				endX = (int) (points[1].x + Math.cos(theta - Math.PI) * leadLength);
				endY = (int) Math.round(points[1].y + Math.sin(theta - Math.PI) * leadLength);
				g2d.drawLine(points[1].x, points[1].y, endX, endY);
				if (shouldShadeLeads()) {
					g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
					leadColor = getLeadColor(componentState);
					g2d.setColor(leadColor);
					g2d.drawLine(points[0].x, points[0].y, (int) (points[0].x + Math.cos(theta)
							* leadLength), (int) (points[0].y + Math.sin(theta) * leadLength));
					g2d.drawLine(points[1].x, points[1].y, (int) (points[1].x + Math.cos(theta
							- Math.PI)
							* leadLength), (int) (points[1].y + Math.sin(theta - Math.PI)
							* leadLength));
				}
			}
		}
	}

	private void drawLead(Graphics2D g2d, ComponentState componentState) {
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(getLeadThickness()));
		Color leadColor = shouldShadeLeads() ? getLeadColor(componentState).darker()
				: getLeadColor(componentState);
		g2d.setColor(leadColor);
		g2d.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
		if (shouldShadeLeads()) {
			g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(getLeadThickness() - 2));
			leadColor = getLeadColor(componentState);
			g2d.setColor(leadColor);
			g2d.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
		}
	}

	protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
		// Do nothing.
	}

	protected int calculateLabelYCoordinate(Rectangle2D shapeRect, Rectangle2D textRect,
			FontMetrics fontMetrics) {
		return (int) (shapeRect.getHeight() - textRect.getHeight()) / 2 + fontMetrics.getAscent();
	}

	protected boolean shouldShadeLeads() {
		return true;
	}

	protected boolean supportsStandingMode() {
		return false;
	}

	/**
	 * @return default component length.
	 */
	protected abstract Size getDefaultLength();

	/**
	 * Returns default component width.
	 * 
	 * @return
	 */
	protected abstract Size getDefaultWidth();

	/**
	 * @return shape that represents component body. Shape should not be
	 *         transformed and should be referenced to (0, 0).
	 */
	protected abstract Shape getBodyShape();

	/**
	 * @return default lead thickness. Override this method to change it.
	 */
	protected int getLeadThickness() {
		return (int) LEAD_THICKNESS.convertToPixels();
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
	public boolean isControlPointSticky(int index) {
		return true;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.ALWAYS;
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

	@EditableProperty(name = "Length", defaultable = true)
	public Size getLength() {
		return length;
	}

	public void setLength(Size length) {
		this.length = length;
	}

	@EditableProperty(name = "Width", defaultable = true)
	public Size getWidth() {
		return width;
	}

	public void setWidth(Size width) {
		this.width = width;
	}

	@EditableProperty
	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}
}
