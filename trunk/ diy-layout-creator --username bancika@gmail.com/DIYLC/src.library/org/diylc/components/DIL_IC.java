package org.diylc.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import org.diylc.common.Orientation;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "DIL IC", author = "bancika", category = "Semiconductors", instanceNamePrefix = "IC", desciption = "test", stretchable = false)
public class DIL_IC implements IDIYComponent<String> {

	private static final long serialVersionUID = 1L;

	public static Color BODY_COLOR = Color.gray;
	public static Color BORDER_COLOR = Color.black;
	public static Color LABEL_COLOR = Color.white;
	public static int EDGE_RADIUS = 6;

	private String name = "New Component";
	private String value = "";
	private Orientation orientation = Orientation.DEFAULT;
	private PinCount pinCount = PinCount._8;
	private Size rowSpacing = new Size(0.3d, SizeUnit.in);
	private Point[] controlPoints = new Point[] { new Point(0, 0),
			new Point(0, (int) Constants.GRID), new Point(0, (int) (2 * Constants.GRID)),
			new Point(0, (int) (3 * Constants.GRID)), new Point((int) (3 * Constants.GRID), 0),
			new Point((int) (3 * Constants.GRID), (int) Constants.GRID),
			new Point((int) (3 * Constants.GRID), (int) (2 * Constants.GRID)),
			new Point((int) (3 * Constants.GRID), (int) (3 * Constants.GRID)) };

	@EditableProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@EditableProperty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@EditableProperty
	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

	@EditableProperty(name = "Pins")
	public PinCount getPinCount() {
		return pinCount;
	}

	public void setPinCount(PinCount pinCount) {
		this.pinCount = pinCount;
		updateControlPoints();
	}

	@EditableProperty(name = "Row spacing")
	public Size getRowSpacing() {
		return rowSpacing;
	}

	public void setRowSpacing(Size rowSpacing) {
		this.rowSpacing = rowSpacing;
		updateControlPoints();
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

	private void updateControlPoints() {
		Point firstPoint = controlPoints[0];
		controlPoints = new Point[pinCount.getValue()];
		controlPoints[0] = firstPoint;
		int dx1;
		int dy1;
		int dx2;
		int dy2;
		for (int i = 0; i < pinCount.getValue() / 2; i++) {
			switch (orientation) {
			case DEFAULT:
				dx1 = 0;
				dy1 = (int) (i * Constants.GRID);
				dx2 = rowSpacing.convertToPixels();
				dy2 = (int) (i * Constants.GRID);
				break;
			case _90:
				dx1 = (int) (-i * Constants.GRID);
				dy1 = 0;
				dx2 = (int) (-i * Constants.GRID);
				dy2 = rowSpacing.convertToPixels();
				break;
			case _180:
				dx1 = 0;
				dy1 = (int) (-i * Constants.GRID);
				dx2 = -rowSpacing.convertToPixels();
				dy2 = (int) (-i * Constants.GRID);
				break;
			case _270:
				dx1 = (int) (i * Constants.GRID);
				dy1 = 0;
				dx2 = (int) (i * Constants.GRID);
				dy2 = rowSpacing.convertToPixels();
				break;
			default:
				throw new RuntimeException("Unexpected orientation: " + orientation);
			}
			controlPoints[i] = new Point(firstPoint.x + dx1, firstPoint.y + dy1);
			controlPoints[i + pinCount.getValue() / 2] = new Point(firstPoint.x + dx2, firstPoint.y
					+ dy2);
		}
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project) {
		int x = controlPoints[0].x;
		int y = controlPoints[0].y;
		int width;
		int height;
		if (orientation == Orientation.DEFAULT || orientation == Orientation._180) {
			width = rowSpacing.convertToPixels();
			height = (int) ((pinCount.getValue() / 2) * Constants.GRID);
			y -= (int) (Constants.GRID / 2);
		} else {
			width = (int) ((pinCount.getValue() / 2) * Constants.GRID);
			height = rowSpacing.convertToPixels();
			x -= (int) (Constants.GRID / 2);
		}
		if (orientation == Orientation._90) {
			x -= width - Constants.GRID;
		} else if (orientation == Orientation._180) {
			x -= rowSpacing.convertToPixels();
			y -= height - Constants.GRID;
		}
		if (componentState != ComponentState.DRAGGING) {
			g2d.setColor(BODY_COLOR);
			g2d.fillRoundRect(x, y, width, height, EDGE_RADIUS, EDGE_RADIUS);
		}
		g2d.setColor(BORDER_COLOR);
		g2d.drawRoundRect(x, y, width, height, EDGE_RADIUS, EDGE_RADIUS);
		// Draw label.
		g2d.setFont(Constants.LABEL_FONT);
		g2d.setColor(componentState == ComponentState.DRAGGING ? BORDER_COLOR : LABEL_COLOR);
		FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
		Rectangle2D rect = fontMetrics.getStringBounds(getName(), g2d);
		int textHeight = (int) (rect.getHeight());
		int textWidth = (int) (rect.getWidth());
		// Center text horizontally and vertically
		x += (width - textWidth) / 2;
		y += (height - textHeight) / 2 + fontMetrics.getAscent();
		g2d.drawString(getName(), x, y);
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.drawString("IC", 10, 10);
	}

	static enum PinCount {

		_4, _8, _10, _12, _14, _16, _24, _32;

		@Override
		public String toString() {
			return name().replace("_", "");
		}

		public int getValue() {
			return Integer.parseInt(toString());
		}
	}
}
