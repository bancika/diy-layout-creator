package org.diylc.components.semiconductors;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
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

import com.diyfever.gui.miscutils.ConfigurationManager;

@ComponentDescriptor(name = "Transistor (TO-92 package)", author = "Branislav Stojkovic", category = "Semiconductors", instanceNamePrefix = "Q", description = "Transistor with small plastic or epoxy body", stretchable = false, zOrder = IDIYComponent.COMPONENT)
public class TransistorTO92 extends AbstractTransparentComponent<String> {

	private static final long serialVersionUID = 1L;

	public static Color BODY_COLOR = Color.gray;
	public static Color BORDER_COLOR = Color.gray.darker();
	public static Color PIN_COLOR = Color.decode("#00B2EE");
	public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
	public static Color LABEL_COLOR = Color.white;
	public static Size PIN_SIZE = new Size(0.03d, SizeUnit.in);
	public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
	public static Size BODY_DIAMETER = new Size(0.2d, SizeUnit.in);

	private String value = "";
	private Orientation orientation = Orientation.DEFAULT;
	private Point[] controlPoints = new Point[] { new Point(0, 0), new Point(0, 0), new Point(0, 0) };
	transient private Area body;
	private Color bodyColor = BODY_COLOR;
	private Color borderColor = BORDER_COLOR;

	public TransistorTO92() {
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

	public Area getBody() {
		if (body == null) {
			int x = controlPoints[0].x;
			int y = controlPoints[0].y;
			int pinSpacing = (int) PIN_SPACING.convertToPixels();
			int bodyDiameter = getClosestOdd(BODY_DIAMETER.convertToPixels());

			switch (orientation) {
			case DEFAULT:
				body = new Area(new Ellipse2D.Double(x - bodyDiameter / 2, y + pinSpacing
						- bodyDiameter / 2, bodyDiameter, bodyDiameter));
				body.subtract(new Area(new Rectangle2D.Double(x - bodyDiameter, y + pinSpacing
						- bodyDiameter / 2, 3 * bodyDiameter / 4, bodyDiameter)));
				break;
			case _90:
				body = new Area(new Ellipse2D.Double(x - pinSpacing - bodyDiameter / 2, y
						- bodyDiameter / 2, bodyDiameter, bodyDiameter));
				body.subtract(new Area(new Rectangle2D.Double(x - pinSpacing - bodyDiameter / 2, y
						- bodyDiameter, bodyDiameter, 3 * bodyDiameter / 4)));
				break;
			case _180:
				body = new Area(new Ellipse2D.Double(x - bodyDiameter / 2, y - pinSpacing
						- bodyDiameter / 2, bodyDiameter, bodyDiameter));
				body.subtract(new Area(new Rectangle2D.Double(x + bodyDiameter / 4, y - pinSpacing
						- bodyDiameter / 2, 3 * bodyDiameter / 4, bodyDiameter)));
				break;
			case _270:
				body = new Area(new Ellipse2D.Double(x + pinSpacing - bodyDiameter / 2, y
						- bodyDiameter / 2, bodyDiameter, bodyDiameter));
				body.subtract(new Area(new Rectangle2D.Double(x + pinSpacing - bodyDiameter / 2, y
						+ bodyDiameter / 4, bodyDiameter, 3 * bodyDiameter / 4)));
				break;
			default:
				throw new RuntimeException("Unexpected orientation: " + orientation);
			}
		}
		return body;
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
			Project project, IDrawingObserver drawingObserver) {
		if (checkPointsClipped(g2d.getClip())) {
			return;
		}
		int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
		Area mainArea = getBody();
		Composite oldComposite = g2d.getComposite();
		if (alpha < MAX_ALPHA) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
					/ MAX_ALPHA));
		}
		g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
		g2d.fill(mainArea);
		g2d.setComposite(oldComposite);
		Color finalBorderColor;
		if (outlineMode) {
			Theme theme = (Theme) ConfigurationManager.getInstance().readObject(
					IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
			finalBorderColor = componentState == ComponentState.SELECTED
					|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : theme
					.getOutlineColor();
		} else {
			finalBorderColor = componentState == ComponentState.SELECTED
					|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : borderColor;
		}
		g2d.setColor(finalBorderColor);
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.draw(mainArea);

		if (!outlineMode) {
			for (Point point : controlPoints) {
				g2d.setColor(PIN_COLOR);
				g2d.fillOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
				g2d.setColor(PIN_BORDER_COLOR);
				g2d.drawOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
			}
		}

		// Draw label.
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
		int margin = 3 * width / 32;
		Area area = new Area(new Ellipse2D.Double(margin / 2, margin, width - 2 * margin, width - 2
				* margin));
		// area.subtract(new Area(new Rectangle2D.Double(0, 0, 2 * margin,
		// height)));
		area.intersect(new Area(new Rectangle2D.Double(2 * margin, 0, width, height)));
		g2d.setColor(BODY_COLOR);
		g2d.fill(area);
		g2d.setColor(BORDER_COLOR);
		g2d.draw(area);
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
}
