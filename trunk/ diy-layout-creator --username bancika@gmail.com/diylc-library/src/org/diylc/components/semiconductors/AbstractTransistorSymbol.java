package org.diylc.components.semiconductors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

public abstract class AbstractTransistorSymbol extends AbstractComponent<String> {

	private static final long serialVersionUID = 1L;

	public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
	public static Color COLOR = Color.black;
	
	protected String value = "";
	protected Point[] controlPoints = new Point[] { new Point(0, 0), new Point(0, 0),
			new Point(0, 0) };
	protected Color color = COLOR;
	protected Display display = Display.NAME;
	protected Shape[] body;

	public AbstractTransistorSymbol() {
		super();
		updateControlPoints();
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
			Project project, IDrawingObserver drawingObserver) {
		if (checkPointsClipped(g2d.getClip())) {
			return;
		}
		int pinSpacing = (int) PIN_SPACING.convertToPixels();
		Color finalColor;
		if (componentState == ComponentState.SELECTED) {
			finalColor = SELECTION_COLOR;
		} else if (outlineMode) {
			Theme theme = (Theme) ConfigurationManager.getInstance().readObject(
					IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
			finalColor = theme.getOutlineColor();
		} else {
			finalColor = color;
		}
		g2d.setColor(finalColor);

		// Draw transistor

		int x = controlPoints[0].x;
		int y = controlPoints[0].y;

		Shape[] body = getBody();

		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
		g2d.draw(body[0]);

		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.draw(body[1]);

		g2d.fill(body[2]);

		// Draw label
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
					: LABEL_COLOR;
		}
		g2d.setColor(finalLabelColor);
		drawCenteredText(g2d, display == Display.VALUE ? getValue() : getName(),
				x + pinSpacing * 2, y, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
	}

	@Override
	public Point getControlPoint(int index) {
		return controlPoints[index];
	}

	@Override
	public int getControlPointCount() {
		return controlPoints.length;
	}

	private void updateControlPoints() {
		int pinSpacing = (int) PIN_SPACING.convertToPixels();
		// Update control points.
		int x = controlPoints[0].x;
		int y = controlPoints[0].y;

		controlPoints[1].x = x + pinSpacing * 2;
		controlPoints[1].y = y - pinSpacing * 2;

		controlPoints[2].x = x + pinSpacing * 2;
		controlPoints[2].y = y + pinSpacing * 2;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.WHEN_SELECTED;
	}

	@EditableProperty
	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean isControlPointSticky(int index) {
		return true;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		controlPoints[index].setLocation(point);
		// Invalidate body
		body = null;
	}

	@EditableProperty
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@EditableProperty
	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	/**
	 * Returns transistor shape consisting of 3 parts, in this order: main body,
	 * connectors, polarity arrow.
	 * 
	 * @return
	 */
	protected abstract Shape[] getBody();
}
