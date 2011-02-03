package org.diylc.components.semiconductors;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Transistor (TO-220 package)", author = "Branislav Stojkovic", category = "Semiconductors", instanceNamePrefix = "Q", description = "Transistors with metal tab for heat sink mounting", stretchable = false, zOrder = IDIYComponent.COMPONENT)
public class TransistorTO220 extends AbstractTransparentComponent<String> {

	private static final long serialVersionUID = 1L;

	public static Color BODY_COLOR = Color.gray;
	public static Color BORDER_COLOR = Color.gray.darker();
	public static Color PIN_COLOR = Color.decode("#00B2EE");
	public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
	public static Color TAB_COLOR = Color.decode("#C3E4ED");
	public static Color TAB_BORDER_COLOR = TAB_COLOR.darker();
	public static Color LABEL_COLOR = Color.white;
	public static Size PIN_SIZE = new Size(0.03d, SizeUnit.in);
	public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
	public static Size BODY_WIDTH = new Size(0.4d, SizeUnit.in);
	public static Size BODY_HEIGHT = new Size(4.5d, SizeUnit.mm);
	public static Size TAB_THICKNESS = new Size(0.05d, SizeUnit.in);

	private String value = "";
	private Orientation orientation = Orientation.DEFAULT;
	private Point[] controlPoints = new Point[] { new Point(0, 0), new Point(0, 0), new Point(0, 0) };
	transient private Shape[] body;
	private Color bodyColor = BODY_COLOR;
	private Color borderColor = BORDER_COLOR;
	private Color tabColor = TAB_COLOR;
	private Color tabBorderColor = TAB_BORDER_COLOR;

	public TransistorTO220() {
		super();
		updateControlPoints();
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
		updateControlPoints();
		// Reset body shape;
		body = null;
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
	public boolean isControlPointSticky(int index) {
		return true;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.NEVER;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		controlPoints[index].setLocation(point);
		body = null;
	}

	private void updateControlPoints() {
		int pinSpacing = (int) PIN_SPACING.convertToPixels();
		// Update control points.
		int x = controlPoints[0].x;
		int y = controlPoints[0].y;
		switch (orientation) {
		case DEFAULT:
			controlPoints[1].setLocation(x, y + pinSpacing);
			controlPoints[2].setLocation(x, y + 2 * pinSpacing);
			break;
		case _90:
			controlPoints[1].setLocation(x - pinSpacing, y);
			controlPoints[2].setLocation(x - 2 * pinSpacing, y);
			break;
		case _180:
			controlPoints[1].setLocation(x, y - pinSpacing);
			controlPoints[2].setLocation(x, y - 2 * pinSpacing);
			break;
		case _270:
			controlPoints[1].setLocation(x + pinSpacing, y);
			controlPoints[2].setLocation(x + 2 * pinSpacing, y);
			break;
		default:
			throw new RuntimeException("Unexpected orientation: " + orientation);
		}
	}

	public Shape[] getBody() {
		if (body == null) {
			body = new Shape[2];
			int x = controlPoints[0].x;
			int y = controlPoints[0].y;
			int pinSpacing = (int) PIN_SPACING.convertToPixels();
			int bodyWidth = getClosestOdd(BODY_WIDTH.convertToPixels());
			int bodyHeight = getClosestOdd(BODY_HEIGHT.convertToPixels());
			int tabThickness = (int) TAB_THICKNESS.convertToPixels();

			switch (orientation) {
			case DEFAULT:
				body[0] = new Rectangle2D.Double(x - bodyHeight / 2,
						y + pinSpacing - bodyWidth / 2, bodyHeight, bodyWidth);
				body[1] = new Rectangle2D.Double(x + bodyHeight / 2 - tabThickness, y + pinSpacing
						- bodyWidth / 2, tabThickness, bodyWidth);
				break;
			case _90:
				body[0] = new Rectangle2D.Double(x - pinSpacing - bodyWidth / 2,
						y - bodyHeight / 2, bodyWidth, bodyHeight);
				body[1] = new Rectangle2D.Double(x - pinSpacing - bodyWidth / 2, y + bodyHeight / 2
						- tabThickness, bodyWidth, tabThickness);
				break;
			case _180:
				body[0] = new Rectangle2D.Double(x - bodyHeight / 2,
						y - pinSpacing - bodyWidth / 2, bodyHeight, bodyWidth);
				body[1] = new Rectangle2D.Double(x - bodyHeight / 2,
						y - pinSpacing - bodyWidth / 2, tabThickness, bodyWidth);
				break;
			case _270:
				body[0] = new Rectangle2D.Double(x + pinSpacing - bodyWidth / 2,
						y - bodyHeight / 2, bodyWidth, bodyHeight);
				body[1] = new Rectangle2D.Double(x + pinSpacing - bodyWidth / 2,
						y - bodyHeight / 2, bodyWidth, tabThickness);
				break;
			default:
				throw new RuntimeException("Unexpected orientation: " + orientation);
			}
		}
		return body;
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project,
			IDrawingObserver drawingObserver) {
		int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
		Shape mainArea = getBody()[0];
		Shape tabArea = getBody()[1];
		Composite oldComposite = g2d.getComposite();
		if (alpha < MAX_ALPHA) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
					/ MAX_ALPHA));
		}
		g2d.setColor(bodyColor);
		g2d.fill(mainArea);
		g2d.setColor(tabColor);
		g2d.fill(tabArea);
		g2d.setComposite(oldComposite);
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.setColor(tabBorderColor);
		g2d.draw(tabArea);
		g2d.setColor(componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : borderColor);
		g2d.draw(mainArea);

		// Draw pins.
		for (Point point : controlPoints) {
			g2d.setColor(PIN_COLOR);
			g2d.fillOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
			g2d.setColor(PIN_BORDER_COLOR);
			g2d.drawOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
		}

		// Draw label.
		g2d.setFont(LABEL_FONT);
		g2d.setColor(LABEL_COLOR);
		FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
		Rectangle2D rect = fontMetrics.getStringBounds(getName(), g2d);
		int textHeight = (int) (rect.getHeight());
		int textWidth = (int) (rect.getWidth());
		// Center text horizontally and vertically
		Rectangle bounds = mainArea.getBounds();
		int x = bounds.x + (bounds.width - textWidth) / 2;
		int y = bounds.y + (bounds.height - textHeight) / 2 + fontMetrics.getAscent();
		g2d.drawString(getName(), x, y);
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setColor(BODY_COLOR);
		int margin = 2 * width / 32;
		int thickness = 7 * width / 32;
		g2d.fillRect(margin, (height - thickness) / 2, width - margin * 2, thickness);
		g2d.setColor(TAB_COLOR);
		g2d.fillRect(margin, (height + thickness) / 2 - margin - 1, width - margin * 2, margin);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRect(margin, (height - thickness) / 2, width - margin * 2, thickness);
	}

	@EditableProperty(name = "Body")
	public Color getBodyColor() {
		return bodyColor;
	}

	public void setBodyColor(Color bodyColor) {
		this.bodyColor = bodyColor;
	}

	@EditableProperty(name = "Border")
	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	@EditableProperty(name = "Tab")
	public Color getTabColor() {
		return tabColor;
	}

	public void setTabColor(Color tabColor) {
		this.tabColor = tabColor;
	}

	@EditableProperty(name = "Tab border")
	public Color getTabBorderColor() {
		return tabBorderColor;
	}

	public void setTabBorderColor(Color tabBorderColor) {
		this.tabBorderColor = tabBorderColor;
	}
}
