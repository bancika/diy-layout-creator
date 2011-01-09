package org.diylc.components.boards;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;

import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.Project;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.utils.Constants;

public abstract class AbstractBoard extends AbstractTransparentComponent<String> {

	private static final long serialVersionUID = 1L;

	protected String name;
	protected String value;
	protected Point[] controlPoints = new Point[] {
			new Point((int) (-5 * Constants.GRID), (int) (-5 * Constants.GRID)),
			new Point((int) (5 * Constants.GRID), (int) (5 * Constants.GRID)) };

	protected abstract Color getBoardColor();

	protected abstract Color getBorderColor();

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project) {
		g2d.setStroke(new BasicStroke());
		if (componentState != ComponentState.DRAGGING) {
			Composite oldComposite = g2d.getComposite();
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
						/ MAX_ALPHA));
			}
			g2d.setColor(getBoardColor());
			g2d.fillRect(controlPoints[0].x, controlPoints[0].y, controlPoints[1].x
					- controlPoints[0].x, controlPoints[1].y - controlPoints[0].y);
			g2d.setComposite(oldComposite);
		}
		g2d.setColor(getBorderColor());
		g2d.drawRect(controlPoints[0].x, controlPoints[0].y, controlPoints[1].x
				- controlPoints[0].x, controlPoints[1].y - controlPoints[0].y);
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

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
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
