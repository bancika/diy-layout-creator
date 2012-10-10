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
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
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

@ComponentDescriptor(name = "SIP IC", author = "Branislav Stojkovic", category = "Semiconductors", instanceNamePrefix = "IC", description = "Single-in-line package IC", stretchable = false, zOrder = IDIYComponent.COMPONENT)
public class SIL_IC extends AbstractTransparentComponent<String> {

	private static final long serialVersionUID = 1L;

	public static Color BODY_COLOR = Color.gray;
	public static Color BORDER_COLOR = Color.gray.darker();
	public static Color PIN_COLOR = Color.decode("#00B2EE");
	public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
	public static Color INDENT_COLOR = Color.gray.darker();
	public static Color LABEL_COLOR = Color.white;
	public static int EDGE_RADIUS = 6;
	public static Size PIN_SIZE = new Size(0.8d, SizeUnit.mm);
	public static Size INDENT_SIZE = new Size(0.07d, SizeUnit.in);
	public static Size THICKNESS = new Size(0.13d, SizeUnit.in);

	private String value = "";
	private Orientation orientation = Orientation.DEFAULT;
	private PinCount pinCount = PinCount._8;
	private Size pinSpacing = new Size(0.1d, SizeUnit.in);
	private Point[] controlPoints = new Point[] { new Point(0, 0) };
	protected Display display = Display.NAME;
	private Color bodyColor = BODY_COLOR;
	private Color borderColor = BORDER_COLOR;
	private Color labelColor = LABEL_COLOR;
	private Color indentColor = INDENT_COLOR;
	// new Point(0, pinSpacing.convertToPixels()),
	// new Point(0, 2 * pinSpacing.convertToPixels()),
	// new Point(0, 3 * pinSpacing.convertToPixels()),
	// new Point(3 * pinSpacing.convertToPixels(), 0),
	// new Point(3 * pinSpacing.convertToPixels(),
	// pinSpacing.convertToPixels()),
	// new Point(3 * pinSpacing.convertToPixels(), 2 *
	// pinSpacing.convertToPixels()),
	// new Point(3 * pinSpacing.convertToPixels(), 3 *
	// pinSpacing.convertToPixels()) };
	transient private Area[] body;

	public SIL_IC() {
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
		// Reset body shape.
		body = null;
	}

	@EditableProperty(name = "Pins")
	public PinCount getPinCount() {
		return pinCount;
	}

	public void setPinCount(PinCount pinCount) {
		this.pinCount = pinCount;
		updateControlPoints();
		// Reset body shape;
		body = null;
	}

	@EditableProperty(name = "Pin spacing")
	public Size getPinSpacing() {
		return pinSpacing;
	}

	public void setPinSpacing(Size pinSpacing) {
		this.pinSpacing = pinSpacing;
	}

	@EditableProperty
	public Display getDisplay() {
		if (display == null) {
			display = Display.VALUE;
		}
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
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
		Point firstPoint = controlPoints[0];
		controlPoints = new Point[pinCount.getValue()];
		controlPoints[0] = firstPoint;
		int pinSpacing = (int) this.pinSpacing.convertToPixels();
		// Update control points.
		int dx1;
		int dy1;
		for (int i = 0; i < pinCount.getValue(); i++) {
			switch (orientation) {
			case DEFAULT:
				dx1 = 0;
				dy1 = i * pinSpacing;
				break;
			case _90:
				dx1 = -i * pinSpacing;
				dy1 = 0;
				break;
			case _180:
				dx1 = 0;
				dy1 = -i * pinSpacing;
				break;
			case _270:
				dx1 = i * pinSpacing;
				dy1 = 0;
				break;
			default:
				throw new RuntimeException("Unexpected orientation: "
						+ orientation);
			}
			controlPoints[i] = new Point(firstPoint.x + dx1, firstPoint.y + dy1);
		}
	}

	public Area[] getBody() {
		if (body == null) {
			body = new Area[2];
			int x = controlPoints[0].x;
			int y = controlPoints[0].y;
			int thickness = getClosestOdd(THICKNESS.convertToPixels());
			int width;
			int height;
			int pinSpacing = (int) this.pinSpacing.convertToPixels();
			Area indentation = null;
			int indentationSize = getClosestOdd(INDENT_SIZE.convertToPixels());
			switch (orientation) {
			case DEFAULT:
				width = thickness;
				height = pinCount.getValue() * pinSpacing;
				x -= thickness / 2;
				y -= pinSpacing / 2;
				indentation = new Area(new Ellipse2D.Double(x + width / 2
						- indentationSize / 2, y - indentationSize / 2,
						indentationSize, indentationSize));
				break;
			case _90:
				width = pinCount.getValue() * pinSpacing;
				height = thickness;
				x -= (pinSpacing / 2) + width - pinSpacing;
				y -= thickness / 2;
				indentation = new Area(new Ellipse2D.Double(x + width
						- indentationSize / 2, y + height / 2 - indentationSize
						/ 2, indentationSize, indentationSize));
				break;
			case _180:
				width = thickness;
				height = pinCount.getValue() * pinSpacing;
				x -= thickness / 2;
				y -= (pinSpacing / 2) + height - pinSpacing;
				indentation = new Area(new Ellipse2D.Double(x + width / 2
						- indentationSize / 2,
						y + height - indentationSize / 2, indentationSize,
						indentationSize));
				break;
			case _270:
				width = pinCount.getValue() * pinSpacing;
				height = thickness;
				x -= pinSpacing / 2;
				y -= thickness / 2;
				indentation = new Area(new Ellipse2D.Double(x - indentationSize
						/ 2, y + height / 2 - indentationSize / 2,
						indentationSize, indentationSize));
				break;
			default:
				throw new RuntimeException("Unexpected orientation: "
						+ orientation);
			}
			body[0] = new Area(new RoundRectangle2D.Double(x, y, width, height,
					EDGE_RADIUS, EDGE_RADIUS));
			body[1] = indentation;
			if (indentation != null) {
				indentation.intersect(body[0]);
			}
		}
		return body;
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState,
			boolean outlineMode, Project project,
			IDrawingObserver drawingObserver) {
		if (checkPointsClipped(g2d.getClip())) {
			return;
		}
		Area mainArea = getBody()[0];
		Composite oldComposite = g2d.getComposite();
		if (alpha < MAX_ALPHA) {
			g2d.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
		}
		g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
		g2d.fill(mainArea);
		g2d.setComposite(oldComposite);

		if (!outlineMode) {
			int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
			for (Point point : controlPoints) {
				g2d.setColor(PIN_COLOR);
				g2d.fillOval(point.x - pinSize / 2, point.y - pinSize / 2,
						pinSize, pinSize);
				g2d.setColor(PIN_BORDER_COLOR);
				g2d.drawOval(point.x - pinSize / 2, point.y - pinSize / 2,
						pinSize, pinSize);
			}
		}

		Color finalBorderColor;
		if (outlineMode) {
			Theme theme = (Theme) ConfigurationManager.getInstance()
					.readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
			finalBorderColor = componentState == ComponentState.SELECTED
					|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR
					: theme.getOutlineColor();
		} else {
			finalBorderColor = componentState == ComponentState.SELECTED
					|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR
					: getBorderColor();
		}
		g2d.setColor(finalBorderColor);
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		if (outlineMode) {
			Area area = new Area(mainArea);
			area.subtract(getBody()[1]);
			g2d.draw(area);
		} else {
			g2d.draw(mainArea);
			if (getBody()[1] != null) {
				g2d.setColor(getIndentColor());
				g2d.fill(getBody()[1]);
			}
		}
		// Draw label.
		g2d.setFont(LABEL_FONT);
		Color finalLabelColor;
		if (outlineMode) {
			Theme theme = (Theme) ConfigurationManager.getInstance()
					.readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
			finalLabelColor = componentState == ComponentState.SELECTED
					|| componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
					: theme.getOutlineColor();
		} else {
			finalLabelColor = componentState == ComponentState.SELECTED
					|| componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
					: getLabelColor();
		}
		g2d.setColor(finalLabelColor);
		FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
		String label = display == Display.NAME ? getName() : getValue();
		Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);
		int textHeight = (int) (rect.getHeight());
		int textWidth = (int) (rect.getWidth());
		// Center text horizontally and vertically
		Rectangle bounds = mainArea.getBounds();
		int x = bounds.x + (bounds.width - textWidth) / 2;
		int y = bounds.y + (bounds.height - textHeight) / 2
				+ fontMetrics.getAscent();
		g2d.drawString(label, x, y);
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int radius = 6 * width / 32;
		int thickness = getClosestOdd(width / 3);
		g2d.rotate(Math.PI / 4, width / 2, height / 2);
		g2d.setColor(BODY_COLOR);
		g2d.fillRoundRect((width - thickness) / 2, 0, thickness, height, radius, radius);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRoundRect((width - thickness) / 2, 0, thickness, height, radius, radius);
		int pinSize = 2 * width / 32;
		g2d.setColor(PIN_COLOR);
		for (int i = 0; i < 4; i++) {
			g2d.fillOval(width / 2 - pinSize / 2, (height / 5) * (i + 1),
					pinSize, pinSize);
		}
	}
	
	@EditableProperty(name = "Body")
	public Color getBodyColor() {
		if (bodyColor == null) {
			bodyColor = BODY_COLOR;
		}
		return bodyColor;
	}

	public void setBodyColor(Color bodyColor) {
		this.bodyColor = bodyColor;
	}

	@EditableProperty(name = "Border")
	public Color getBorderColor() {
		if (borderColor == null) {
			borderColor = BORDER_COLOR;
		}
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	@EditableProperty(name = "Label")
	public Color getLabelColor() {
		if (labelColor == null) {
			labelColor = LABEL_COLOR;
		}
		return labelColor;
	}

	public void setLabelColor(Color labelColor) {
		this.labelColor = labelColor;
	}
	
	@EditableProperty(name = "Indent")
	public Color getIndentColor() {
		if (indentColor == null) {
			indentColor = INDENT_COLOR;
		}
		return indentColor;
	}
	
	public void setIndentColor(Color indentColor) {
		this.indentColor = indentColor;
	}

	public static enum PinCount {

		_2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20;

		@Override
		public String toString() {
			return name().replace("_", "");
		}

		public int getValue() {
			return Integer.parseInt(toString());
		}
	}
}
