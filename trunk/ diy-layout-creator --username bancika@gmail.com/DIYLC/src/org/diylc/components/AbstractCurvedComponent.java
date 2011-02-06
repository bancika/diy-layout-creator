package org.diylc.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.CubicCurve2D;

import org.diylc.common.ObjectCache;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

public abstract class AbstractCurvedComponent<T> extends AbstractTransparentComponent<T> {

	private static final long serialVersionUID = 1L;

	public static Color GUIDELINE_COLOR = Color.blue;
	public static Size DEFAULT_SIZE = new Size(1d, SizeUnit.in);

	protected Point[] controlPoints = new Point[] {
			new Point(0, 0),
			new Point((int) (DEFAULT_SIZE.convertToPixels() / 2), 0),
			new Point((int) (DEFAULT_SIZE.convertToPixels() / 2), (int) (DEFAULT_SIZE
					.convertToPixels())),
			new Point((int) DEFAULT_SIZE.convertToPixels(), (int) DEFAULT_SIZE.convertToPixels()) };

	protected Color color = getDefaultColor();

	/**
	 * Draws the specified curve onto graphics.
	 * 
	 * @param curve
	 * @param g2d
	 * @param componentState
	 */
	protected abstract void drawCurve(CubicCurve2D curve, Graphics2D g2d,
			ComponentState componentState);

	/**
	 * @return default color.
	 */
	protected abstract Color getDefaultColor();

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setColor(getDefaultColor().darker());
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3));
		CubicCurve2D curve = new CubicCurve2D.Double(1, height - 1, width / 4, height / 3,
				3 * width / 4, 2 * height / 3, width - 1, 1);
		g2d.draw(curve);
		g2d.setColor(getDefaultColor());
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.draw(curve);
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project,
			IDrawingObserver drawingObserver) {
		if (checkPointsClipped(g2d.getClip())) {
			return;
		}
		if (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING) {
			// Do not track guidelines.
			drawingObserver.stopTracking();
			g2d.setStroke(Constants.DASHED_STROKE);
			g2d.setColor(GUIDELINE_COLOR);
			g2d.drawLine(controlPoints[0].x, controlPoints[0].y, controlPoints[1].x,
					controlPoints[1].y);
			g2d.drawLine(controlPoints[1].x, controlPoints[1].y, controlPoints[2].x,
					controlPoints[2].y);
			g2d.drawLine(controlPoints[2].x, controlPoints[2].y, controlPoints[3].x,
					controlPoints[3].y);
			drawingObserver.startTracking();
		}
		CubicCurve2D curve = new CubicCurve2D.Double(controlPoints[0].x, controlPoints[0].y,
				controlPoints[1].x, controlPoints[1].y, controlPoints[2].x, controlPoints[2].y,
				controlPoints[3].x, controlPoints[3].y);

		Composite oldComposite = g2d.getComposite();
		if (alpha < MAX_ALPHA) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
					/ MAX_ALPHA));
		}
		// g2d.setColor(color.darker());
		// g2d.setStroke(new BasicStroke(thickness));
		// g2d.draw(path);
		// g2d.setColor(color);
		// g2d.setStroke(new BasicStroke(thickness - 2));
		// g2d.draw(path);
		drawCurve(curve, g2d, componentState);
		g2d.setComposite(oldComposite);
	}

	@Override
	public int getControlPointCount() {
		return controlPoints.length;
	}

	@Override
	public boolean isControlPointSticky(int index) {
		return index == 0 || index == 3;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.WHEN_SELECTED;
	}

	@Override
	public boolean canControlPointOverlap(int index) {
		// Only shape control points may overlap.
		return index == 1 || index == 2;
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
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

}
