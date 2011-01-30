package org.diylc.components.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Project Information", author = "Branislav Stojkovic", category = "Misc", description = "Draws a box with general project information", instanceNamePrefix = "BOM", zOrder = IDIYComponent.TEXT, stretchable = false)
public class ProjectInformation extends AbstractComponent<Void> {

	public static Size DEFAULT_SIZE = new Size(10d, SizeUnit.cm);
	public static Color COLOR = Color.black;
	public static String DEFAULT_TEXT = "No components to show in the Bill of Materials";

	private static final long serialVersionUID = 1L;

	private Point point = new Point(0, 0);

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project,
			IDrawingObserver drawingObserver) {
		g2d.setFont(LABEL_FONT);
		g2d.setColor(componentState == ComponentState.DRAGGING
				|| componentState == ComponentState.SELECTED ? SELECTION_COLOR : COLOR);
		// FontMetrics fontMetrics = g2d.getFontMetrics();
		g2d.drawString(project.getTitle(), point.x, point.y);
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setColor(Color.white);
		g2d.fillRect(width / 8, 0, 6 * width / 8, height - 1);
		g2d.setColor(Color.black);
		g2d.drawRect(width / 8, 0, 6 * width / 8, height - 1);
		g2d.setFont(LABEL_FONT.deriveFont(1f * 9 * width / 32).deriveFont(Font.PLAIN));

		FontMetrics fontMetrics = g2d.getFontMetrics();
		Rectangle2D rect = fontMetrics.getStringBounds("BOM", g2d);

		int textHeight = (int) (rect.getHeight());
		int textWidth = (int) (rect.getWidth());

		// Center text horizontally and vertically.
		int x = (width - textWidth) / 2 + 1;
		int y = textHeight + 2;

		g2d.drawString("BOM", x, y);

		g2d.setFont(g2d.getFont().deriveFont(1f * 5 * width / 32));

		fontMetrics = g2d.getFontMetrics();
		rect = fontMetrics.getStringBounds("resistors", g2d);
		x = (width - textWidth) / 2 + 1;
		y = height / 2 + 2;
		g2d.drawString("resistors", x, y);
		y += rect.getHeight() - 1;
		g2d.drawString("tubes", x, y);
		y += rect.getHeight() - 1;
		g2d.drawString("diodes", x, y);
	}

	@Override
	public int getControlPointCount() {
		return 1;
	}

	@Override
	public Point getControlPoint(int index) {
		return point;
	}

	@Override
	public boolean isControlPointSticky(int index) {
		return false;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.NEVER;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		this.point.setLocation(point);
	}

	@Deprecated
	@Override
	public Void getValue() {
		return null;
	}

	@Override
	public void setValue(Void value) {
	}
}
