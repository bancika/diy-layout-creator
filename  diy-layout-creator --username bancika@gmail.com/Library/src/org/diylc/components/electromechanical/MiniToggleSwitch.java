package org.diylc.components.electromechanical;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.RoundRectangle2D;

import org.diylc.core.ComponentLayer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Mini Toggle Switch", category = "Electromechanical", author = "Branislav Stojkovic", desciption = "", stretchable = false, componentLayer = ComponentLayer.COMPONENT, instanceNamePrefix = "SW")
public class MiniToggleSwitch implements IDIYComponent<ToggleSwitchType> {

	private static final long serialVersionUID = 1L;

	private static Size SPACING = new Size(0.1d, SizeUnit.in);
	private static Size MARGIN = new Size(0.08d, SizeUnit.in);
	private static Size CIRCLE_SIZE = new Size(0.09d, SizeUnit.in);
	private static Size LUG_WIDTH = new Size(0.065d, SizeUnit.in);
	private static Size LUG_HEIGHT = new Size(0.03d, SizeUnit.in);

	private static Color BODY_COLOR = Color.decode("#3299CC");
	private static Color BORDER_COLOR = BODY_COLOR.darker();
	private static Color CIRCLE_COLOR = Color.red;
	private static Color LUG_COLOR = Color.blue;

	protected Point[] controlPoints = new Point[] { new Point(0, 0) };
	protected RoundRectangle2D body;
	protected String name;
	protected ToggleSwitchType switchType = ToggleSwitchType.SPST;

	public MiniToggleSwitch() {
		super();
		updateControlPoints();
	}

	private void updateControlPoints() {
		Point firstPoint = controlPoints[0];
		int margin = MARGIN.convertToPixels();
		int spacing = SPACING.convertToPixels();
		switch (switchType) {
		case SPST:
			controlPoints = new Point[] { firstPoint,
					new Point(firstPoint.x, firstPoint.y + spacing) };
			body = new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin,
					2 * margin, 2 * margin + spacing, margin, margin);
			break;
		case SPDT:
			controlPoints = new Point[] { firstPoint,
					new Point(firstPoint.x, firstPoint.y + spacing),
					new Point(firstPoint.x, firstPoint.y + 2 * spacing) };
			body = new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin,
					2 * margin, 2 * margin + 2 * spacing, margin, margin);
			break;
		case DPDT:
			controlPoints = new Point[] { firstPoint,
					new Point(firstPoint.x, firstPoint.y + spacing),
					new Point(firstPoint.x, firstPoint.y + 2 * spacing),
					new Point(firstPoint.x + spacing, firstPoint.y),
					new Point(firstPoint.x + spacing, firstPoint.y + spacing),
					new Point(firstPoint.x + spacing, firstPoint.y + 2 * spacing) };
			body = new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2
					* margin + spacing, 2 * margin + 2 * spacing, margin, margin);
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
			body = new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2
					* margin + 2 * spacing, 2 * margin + 2 * spacing, margin, margin);
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
			body = new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2
					* margin + 3 * spacing, 2 * margin + 2 * spacing, margin, margin);
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
			body = new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2
					* margin + 4 * spacing, 2 * margin + 2 * spacing, margin, margin);
			break;
		}
	}

	@Override
	public Point getControlPoint(int index) {
		return controlPoints[index];
	}

	@Override
	public int getControlPointCount() {
		return controlPoints.length;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		controlPoints[index].setLocation(point);
		updateControlPoints();
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
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project) {
		if (body != null) {
			if (componentState != ComponentState.DRAGGING) {
				g2d.setColor(BODY_COLOR);
				g2d.fill(body);
			}
			g2d.setStroke(new BasicStroke());
			g2d.setColor(BORDER_COLOR);
			g2d.draw(body);
		}
		if (componentState != ComponentState.DRAGGING) {
			int circleRadius = CIRCLE_SIZE.convertToPixels() / 2;
			int lugWidth = LUG_WIDTH.convertToPixels();
			int lugHeight = LUG_HEIGHT.convertToPixels();
			for (Point p : controlPoints) {
				g2d.setColor(CIRCLE_COLOR);
				g2d.fillOval(p.x - circleRadius, p.y - circleRadius, 2 * circleRadius,
						2 * circleRadius);
				g2d.setColor(LUG_COLOR);
				g2d.fillRect(p.x - lugWidth / 2, p.y - lugHeight / 2, lugWidth, lugHeight);
			}
		}
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		// TODO Auto-generated method stub

	}
}
