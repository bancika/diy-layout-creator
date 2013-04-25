package org.diylc.components.tube;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import org.diylc.common.ObjectCache;
import org.diylc.core.IDIYComponent;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

@ComponentDescriptor(name = "Triode Symbol", author = "Branislav Stojkovic", category = "Tubes", instanceNamePrefix = "V", description = "Triode tube symbol", stretchable = false, zOrder = IDIYComponent.COMPONENT, rotatable = false)
public class TriodeSymbol extends AbstractTubeSymbol {

	private static final long serialVersionUID = 1L;

	protected Point[] controlPoints = new Point[] { new Point(0, 0),
			new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0) };

	protected boolean directlyHeated = false;

	public TriodeSymbol() {
		super();
		updateControlPoints();
	}

	public Shape[] getBody() {
		if (body == null) {
			body = new Shape[3];
			int x = controlPoints[0].x;
			int y = controlPoints[0].y;
			int pinSpacing = (int) PIN_SPACING.convertToPixels();

			// electrodes
			GeneralPath polyline = new GeneralPath();

			// grid
			polyline.moveTo(x + pinSpacing * 5 / 4, y);
			polyline.lineTo(x + pinSpacing * 7 / 4, y);
			polyline.moveTo(x + pinSpacing * 9 / 4, y);
			polyline.lineTo(x + pinSpacing * 11 / 4, y);
			polyline.moveTo(x + pinSpacing * 13 / 4, y);
			polyline.lineTo(x + pinSpacing * 15 / 4, y);
			polyline.moveTo(x + pinSpacing * 17 / 4, y);
			polyline.lineTo(x + pinSpacing * 19 / 4, y);

			// plate
			polyline.moveTo(x + pinSpacing * 3 / 2, y - pinSpacing);
			polyline.lineTo(x + pinSpacing * 9 / 2, y - pinSpacing);

			// cathode
			if (directlyHeated) {
				polyline.moveTo(controlPoints[2].x, controlPoints[2].y
						- pinSpacing);
				polyline.lineTo(controlPoints[2].x + pinSpacing,
						controlPoints[2].y - pinSpacing * 2);
				polyline.lineTo(controlPoints[4].x, controlPoints[4].y
						- pinSpacing);
			} else {
				polyline.moveTo(x + pinSpacing * 2, y + pinSpacing);
				polyline.lineTo(x + pinSpacing * 4, y + pinSpacing);
			}

			body[0] = polyline;

			// connectors
			polyline = new GeneralPath();

			// grid
			polyline.moveTo(x, y);
			polyline.lineTo(x + pinSpacing, y);

			// plate
			polyline.moveTo(controlPoints[1].x, controlPoints[1].y);
			polyline.lineTo(x + pinSpacing * 3, y - pinSpacing);

			// cathode
			if (directlyHeated) {
				polyline.moveTo(controlPoints[2].x, controlPoints[2].y);
				polyline.lineTo(controlPoints[2].x, controlPoints[2].y
						- pinSpacing);

				polyline.moveTo(controlPoints[4].x, controlPoints[4].y);
				polyline.lineTo(controlPoints[4].x, controlPoints[4].y
						- pinSpacing);
			} else {
				polyline.moveTo(controlPoints[2].x, controlPoints[2].y);
				polyline.lineTo(x + pinSpacing * 2, y + pinSpacing);

				if (showHeaters) {
					polyline.moveTo(controlPoints[3].x, controlPoints[3].y);
					polyline.lineTo(controlPoints[3].x, controlPoints[3].y
							- pinSpacing);
					polyline.lineTo(controlPoints[3].x + pinSpacing / 2,
							controlPoints[3].y - 3 * pinSpacing / 2);

					polyline.moveTo(controlPoints[4].x, controlPoints[4].y);
					polyline.lineTo(controlPoints[4].x, controlPoints[4].y
							- pinSpacing);
					polyline.lineTo(controlPoints[4].x - pinSpacing / 2,
							controlPoints[4].y - 3 * pinSpacing / 2);
				}
			}

			body[1] = polyline;

			// bulb
			body[2] = new Ellipse2D.Double(x + pinSpacing / 2, y - pinSpacing
					* 5 / 2, pinSpacing * 5, pinSpacing * 5);
		}
		return body;
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setColor(COLOR);

		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

		g2d.drawLine(width / 4, height / 4, width * 3 / 4, height / 4);
		g2d.drawLine(width / 2, height / 4, width / 2, 0);

		g2d.drawLine(width / 4 + 2 * width / 32, height * 3 / 4, width * 3 / 4
				- 4 * width / 32, height * 3 / 4);
		g2d.drawLine(width / 4 + 2 * width / 32, height * 3 / 4, width / 4 + 2
				* width / 32, height - 1);

		g2d.drawOval(1, 1, width - 1 - 2 * width / 32, height - 1 - 2 * width
				/ 32);

		g2d.drawLine(0, height / 2, width / 8, height / 2);
		g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_BEVEL, 0, new float[] { 3f }, 6f));
		g2d.drawLine(width / 8, height / 2, width * 7 / 8, height / 2);
	}

	@Override
	public Point getControlPoint(int index) {
		return controlPoints[index];
	}

	@Override
	public int getControlPointCount() {
		return controlPoints.length;
	}

	protected void updateControlPoints() {
		int pinSpacing = (int) PIN_SPACING.convertToPixels();
		// Update control points.
		int x = controlPoints[0].x;
		int y = controlPoints[0].y;

		controlPoints[1].x = x + pinSpacing * 3;
		controlPoints[1].y = y - pinSpacing * 3;

		controlPoints[2].x = x + pinSpacing * 2;
		controlPoints[2].y = y + pinSpacing * 3;

		controlPoints[3].x = x + pinSpacing * 3;
		controlPoints[3].y = y + pinSpacing * 3;

		controlPoints[4].x = x + pinSpacing * 4;
		controlPoints[4].y = y + pinSpacing * 3;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		controlPoints[index].setLocation(point);
		// Invalidate body
		body = null;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		if (directlyHeated) {
			return index != 3 ? VisibilityPolicy.WHEN_SELECTED
					: VisibilityPolicy.NEVER;
		} else if (showHeaters) {
			return VisibilityPolicy.WHEN_SELECTED;
		} else {
			return index < 3 ? VisibilityPolicy.WHEN_SELECTED
					: VisibilityPolicy.NEVER;
		}
	}

	@Override
	public boolean isControlPointSticky(int index) {
		if (directlyHeated) {
			return index != 3;
		} else if (showHeaters) {
			return true;
		} else {
			return index < 3;
		}
	}

	@Override
	protected Point getTextLocation() {
		int pinSpacing = (int) PIN_SPACING.convertToPixels();
		return new Point(controlPoints[0].x + pinSpacing * 5,
				controlPoints[0].y + pinSpacing * 2);
	}

	@EditableProperty(name = "Directly heated")
	public boolean getDirectlyHeated() {
		return directlyHeated;
	}

	public void setDirectlyHeated(boolean directlyHeated) {
		this.directlyHeated = directlyHeated;
		// Invalidate body
		body = null;
	}
}
