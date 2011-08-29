package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Mini Toggle Switch", category = "Electromechanical", author = "Branislav Stojkovic", description = "Panel mounted mini toggle switch", stretchable = false, zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "SW")
public class MiniToggleSwitch extends AbstractTransparentComponent<ToggleSwitchType> {

	private static final long serialVersionUID = 1L;

	private static Size SPACING = new Size(0.2d, SizeUnit.in);
	private static Size MARGIN = new Size(0.08d, SizeUnit.in);
	private static Size CIRCLE_SIZE = new Size(0.09d, SizeUnit.in);
	private static Size LUG_WIDTH = new Size(0.060d, SizeUnit.in);
	private static Size LUG_HEIGHT = new Size(0.03d, SizeUnit.in);

	private static Color BODY_COLOR = Color.decode("#3299CC");
	private static Color BORDER_COLOR = BODY_COLOR.darker();
	private static Color CIRCLE_COLOR = Color.red;
	private static Color LUG_COLOR = Color.decode("#00B2EE");

	protected Point[] controlPoints = new Point[] { new Point(0, 0) };
	transient protected RoundRectangle2D body;
	protected String name;
	protected ToggleSwitchType switchType = ToggleSwitchType.DPDT;

	public MiniToggleSwitch() {
		super();
		updateControlPoints();
	}

	private void updateControlPoints() {
		Point firstPoint = controlPoints[0];
		int spacing = (int) SPACING.convertToPixels();
		switch (switchType) {
		case SPST:
			controlPoints = new Point[] { firstPoint,
					new Point(firstPoint.x, firstPoint.y + spacing) };
			break;
		case SPDT:
			controlPoints = new Point[] { firstPoint,
					new Point(firstPoint.x, firstPoint.y + spacing),
					new Point(firstPoint.x, firstPoint.y + 2 * spacing) };
			break;
		case DPDT:
			controlPoints = new Point[] { firstPoint,
					new Point(firstPoint.x, firstPoint.y + spacing),
					new Point(firstPoint.x, firstPoint.y + 2 * spacing),
					new Point(firstPoint.x + spacing, firstPoint.y),
					new Point(firstPoint.x + spacing, firstPoint.y + spacing),
					new Point(firstPoint.x + spacing, firstPoint.y + 2 * spacing) };
			break;
		case _3PDT:
			controlPoints = new Point[] { firstPoint,
					new Point(firstPoint.x, firstPoint.y + spacing),
					new Point(firstPoint.x, firstPoint.y + 2 * spacing),
					new Point(firstPoint.x + spacing, firstPoint.y),
					new Point(firstPoint.x + spacing, firstPoint.y + spacing),
					new Point(firstPoint.x + spacing, firstPoint.y + 2 * spacing),
					new Point(firstPoint.x + 2 * spacing, firstPoint.y),
					new Point(firstPoint.x + 2 * spacing, firstPoint.y + spacing),
					new Point(firstPoint.x + 2 * spacing, firstPoint.y + 2 * spacing) };
			break;
		case _4PDT:
			controlPoints = new Point[] { firstPoint,
					new Point(firstPoint.x, firstPoint.y + spacing),
					new Point(firstPoint.x, firstPoint.y + 2 * spacing),
					new Point(firstPoint.x + spacing, firstPoint.y),
					new Point(firstPoint.x + spacing, firstPoint.y + spacing),
					new Point(firstPoint.x + spacing, firstPoint.y + 2 * spacing),
					new Point(firstPoint.x + 2 * spacing, firstPoint.y),
					new Point(firstPoint.x + 2 * spacing, firstPoint.y + spacing),
					new Point(firstPoint.x + 2 * spacing, firstPoint.y + 2 * spacing),
					new Point(firstPoint.x + 3 * spacing, firstPoint.y),
					new Point(firstPoint.x + 3 * spacing, firstPoint.y + spacing),
					new Point(firstPoint.x + 3 * spacing, firstPoint.y + 2 * spacing) };
			break;
		case _5PDT:
			controlPoints = new Point[] { firstPoint,
					new Point(firstPoint.x, firstPoint.y + spacing),
					new Point(firstPoint.x, firstPoint.y + 2 * spacing),
					new Point(firstPoint.x + spacing, firstPoint.y),
					new Point(firstPoint.x + spacing, firstPoint.y + spacing),
					new Point(firstPoint.x + spacing, firstPoint.y + 2 * spacing),
					new Point(firstPoint.x + 2 * spacing, firstPoint.y),
					new Point(firstPoint.x + 2 * spacing, firstPoint.y + spacing),
					new Point(firstPoint.x + 2 * spacing, firstPoint.y + 2 * spacing),
					new Point(firstPoint.x + 3 * spacing, firstPoint.y),
					new Point(firstPoint.x + 3 * spacing, firstPoint.y + spacing),
					new Point(firstPoint.x + 3 * spacing, firstPoint.y + 2 * spacing),
					new Point(firstPoint.x + 4 * spacing, firstPoint.y),
					new Point(firstPoint.x + 4 * spacing, firstPoint.y + spacing),
					new Point(firstPoint.x + 4 * spacing, firstPoint.y + 2 * spacing) };
			break;
		}
	}

	@Override
	public Point getControlPoint(int index) {
		return controlPoints[index];
	}

	@Override
	public boolean isControlPointSticky(int index) {
		return true;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.NEVER;
	}

	@Override
	public int getControlPointCount() {
		return controlPoints.length;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		controlPoints[index].setLocation(point);
		// Reset body shape.
		body = null;
	}

	@EditableProperty
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@EditableProperty(name = "Type")
	@Override
	public ToggleSwitchType getValue() {
		return switchType;
	}

	@Override
	public void setValue(ToggleSwitchType value) {
		this.switchType = value;
		updateControlPoints();
		// Reset body shape.
		body = null;
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
			Project project, IDrawingObserver drawingObserver) {
		if (checkPointsClipped(g2d.getClip())) {
			return;
		}
		Shape body = getBody();
		// Draw body if available.
		if (body != null) {
			Composite oldComposite = g2d.getComposite();
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
						/ MAX_ALPHA));
			}
			g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
			g2d.fill(body);
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
						: BORDER_COLOR;
			}
			g2d.setColor(finalBorderColor);
			g2d.draw(body);
		}
		// Do not track these changes because the whole switch has been tracked
		// so far.
		drawingObserver.stopTracking();
		// Draw lugs.
		if (!outlineMode) {
			int circleDiameter = getClosestOdd((int) CIRCLE_SIZE.convertToPixels());
			int lugWidth = getClosestOdd((int) LUG_WIDTH.convertToPixels());
			int lugHeight = getClosestOdd((int) LUG_HEIGHT.convertToPixels());
			for (Point p : controlPoints) {
				g2d.setColor(CIRCLE_COLOR);
				g2d.fillOval(p.x - circleDiameter / 2, p.y - circleDiameter / 2, circleDiameter,
						circleDiameter);
				g2d.setColor(LUG_COLOR);
				g2d.fillRect(p.x - lugWidth / 2, p.y - lugHeight / 2, lugWidth, lugHeight);
			}
		}
	}

	public RoundRectangle2D getBody() {
		if (body == null) {
			Point firstPoint = controlPoints[0];
			int margin = (int) MARGIN.convertToPixels();
			int spacing = (int) SPACING.convertToPixels();
			switch (switchType) {
			case SPST:
				body = new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin,
						2 * margin, 2 * margin + spacing, margin, margin);
				break;
			case SPDT:
				body = new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin,
						2 * margin, 2 * margin + 2 * spacing, margin, margin);
				break;
			case DPDT:
				body = new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2
						* margin + spacing, 2 * margin + 2 * spacing, margin, margin);
				break;
			case _3PDT:
				body = new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2
						* margin + 2 * spacing, 2 * margin + 2 * spacing, margin, margin);
				break;
			case _4PDT:
				body = new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2
						* margin + 3 * spacing, 2 * margin + 2 * spacing, margin, margin);
				break;
			case _5PDT:
				body = new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2
						* margin + 4 * spacing, 2 * margin + 2 * spacing, margin, margin);
				break;
			}
		}
		return body;
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int circleSize = 5 * width / 32;
		g2d.setColor(BODY_COLOR);
		g2d.fillRoundRect(width / 4, 1, width / 2, height - 2, circleSize, circleSize);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRoundRect(width / 4, 1, width / 2, height - 2, circleSize, circleSize);
		g2d.setColor(CIRCLE_COLOR);
		for (int i = 1; i <= 3; i++) {
			g2d.fillOval(width / 2 - circleSize / 2, i * height / 4 - 3, circleSize, circleSize);
		}
	}
}
