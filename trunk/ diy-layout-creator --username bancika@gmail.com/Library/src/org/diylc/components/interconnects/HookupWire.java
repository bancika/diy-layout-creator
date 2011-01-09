package org.diylc.components.interconnects;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;

import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Hookup wire", author = "Branislav Stojkovic", category = "Interconnects", instanceNamePrefix = "W", desciption = "Jumper wire")
public class HookupWire extends AbstractTransparentComponent<AWG> {

	private static final long serialVersionUID = 1L;

	public static Color GUIDELINE_COLOR = Color.blue;
	public static Color COLOR = Color.green;

	private Point[] controlPoints = new Point[] {
			new Point(-5 * Constants.GRID, -5 * Constants.GRID), new Point(0, -3 * Constants.GRID),
			new Point(0, 3 * Constants.GRID), new Point(5 * Constants.GRID, 5 * Constants.GRID) };

	private AWG value = AWG._22;
	private Color color = COLOR;

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {

	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project) {
		if (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING) {
			g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10f,
					new float[] { 2f, 3f }, 5f));
			g2d.setColor(GUIDELINE_COLOR);
			g2d.drawLine(controlPoints[0].x, controlPoints[0].y, controlPoints[1].x,
					controlPoints[1].y);
			g2d.drawLine(controlPoints[1].x, controlPoints[1].y, controlPoints[2].x,
					controlPoints[2].y);
			g2d.drawLine(controlPoints[2].x, controlPoints[2].y, controlPoints[3].x,
					controlPoints[3].y);
		}
		int thickness = (int) (Math.pow(Math.E, -1.12436 - 0.11594 * value.getValue()) * Constants.PIXELS_PER_INCH);
		Path2D path = new Path2D.Double();
		path.moveTo(controlPoints[0].x, controlPoints[0].y);
		path.curveTo(controlPoints[1].x, controlPoints[1].y, controlPoints[2].x,
				controlPoints[2].y, controlPoints[3].x, controlPoints[3].y);

		Composite oldComposite = g2d.getComposite();
		if (alpha < MAX_ALPHA) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
					/ MAX_ALPHA));
		}
		g2d.setColor(color.darker());
		g2d.setStroke(new BasicStroke(thickness));
		g2d.draw(path);
		g2d.setColor(color);
		g2d.setStroke(new BasicStroke(thickness - 2));
		g2d.draw(path);
		g2d.setComposite(oldComposite);
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

	@EditableProperty(name = "AWG")
	@Override
	public AWG getValue() {
		return value;
	}

	@Override
	public void setValue(AWG value) {
		this.value = value;
	}

	@EditableProperty
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
