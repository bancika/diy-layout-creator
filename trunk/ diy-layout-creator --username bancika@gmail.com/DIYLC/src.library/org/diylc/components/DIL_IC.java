package org.diylc.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "DIL IC", author = "bancika", category = "Semiconductors", instanceNamePrefix = "IC", desciption = "test")
public class DIL_IC implements IDIYComponent<String> {

	private static final long serialVersionUID = 1L;

	public static Color BODY_COLOR = Color.gray;
	public static Color BORDER_COLOR = Color.black;
	public static Color LABEL_COLOR = Color.white;
	public static int EDGE_RADIUS = 6;

	private Point point1 = new Point(0, 0);
	private Point point2 = new Point((int) (Constants.GRID) * 4, (int) (Constants.GRID) * 5);
	private String name = "New Component";
	private String value = "";

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

	@Override
	public int getControlPointCount() {
		return 2;
	}
	
	@Override
	public Point getControlPoint(int index) {
		return index == 0 ? point1 : point2;
	}
	
	@Override
	public void setControlPoint(Point point, int index) {
		if (index == 0) {
			point1.setLocation(point);
		} else {
			point2.setLocation(point);
		}
	}

	public void setPoint2(Point point2) {
		this.point2 = point2;
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project) {
		int width = ((int) (Math.abs(point1.x - point2.x) / Constants.GRID));
		if (width % 2 == 1) {
			width--;
		}
		width = (int) (width * Constants.GRID);
		int height = ((int) (Math.abs(point1.y - point2.y) / Constants.GRID));
		if (height % 2 == 1) {
			height--;
		}
		height = (int) (height * Constants.GRID);
		int x = (point1.x + point2.x - width) / 2;
		int y = (point1.y + point2.y - height) / 2;
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
}
