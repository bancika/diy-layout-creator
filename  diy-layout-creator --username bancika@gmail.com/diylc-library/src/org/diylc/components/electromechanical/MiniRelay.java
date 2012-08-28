package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
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
import org.diylc.core.measures.Voltage;
import org.diylc.core.measures.VoltageUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Mini Relay", author = "Branislav Stojkovic", category = "Electromechanical", instanceNamePrefix = "RY", description = "Miniature PCB mount relay, like Omron G5V-1 or G5V-2", stretchable = false, zOrder = IDIYComponent.COMPONENT)
public class MiniRelay extends AbstractTransparentComponent<String> {

	private static final long serialVersionUID = 1L;

	public static Color BODY_COLOR = Color.gray;
	public static Color BORDER_COLOR = Color.gray.darker();
	public static Color PIN_COLOR = Color.decode("#00B2EE");
	public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
	public static Color INDENT_COLOR = Color.gray.darker();
	public static Color LABEL_COLOR = Color.white;
	public static int EDGE_RADIUS = 6;
	public static Size PIN_SIZE = new Size(0.03d, SizeUnit.in);
	public static Size INDENT_SIZE = new Size(0.07d, SizeUnit.in);
	public static Size BODY_MARGIN = new Size(0.05d, SizeUnit.in);

	public static Size MINI_PIN_SPACING = new Size(0.2d, SizeUnit.in);
	public static Size MINI_ROW_SPACING = new Size(0.3d, SizeUnit.in);
	public static Size MINI_WIDTH = new Size(20.1d, SizeUnit.mm);
	public static Size MINI_HEIGHT = new Size(9.9d, SizeUnit.mm);
	public static Size MINI_GAP = new Size(0.1d, SizeUnit.in);

	public static Size ULTRA_PIN_SPACING = new Size(0.1d, SizeUnit.in);
	public static Size ULTRA_ROW_SPACING = new Size(0.2d, SizeUnit.in);
	public static Size ULTRA_WIDTH = new Size(12.2d, SizeUnit.mm);
	public static Size ULTRA_HEIGHT = new Size(7.2d, SizeUnit.mm);
	public static Size ULTRA_GAP = new Size(0.1d, SizeUnit.in);

	private String value = "";
	private Orientation orientation = Orientation.DEFAULT;

	private Point[] controlPoints = new Point[] { new Point(0, 0) };
	protected Display display = Display.NAME;
	private RelayType type = RelayType.DPDT;
	private RelaySize size = RelaySize.Miniature;
	private Voltage voltage = new Voltage(12d, VoltageUnit.V);
	transient private Area[] body;

	public MiniRelay() {
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
		int pinCount = 0;
		switch (type) {
		case DPDT:
			pinCount = 8;
			break;
		case SPDT:
			pinCount = 6;
			break;
		default:
			throw new RuntimeException("Unexpected type: " + type);
		}
		controlPoints = new Point[pinCount];
		controlPoints[0] = firstPoint;
		int pinSpacing = size == RelaySize.Miniature ? (int) MINI_PIN_SPACING
				.convertToPixels() : (int) ULTRA_PIN_SPACING.convertToPixels();
		int rowSpacing = size == RelaySize.Miniature ? (int) MINI_ROW_SPACING
				.convertToPixels() : (int) ULTRA_ROW_SPACING.convertToPixels();
		// Update control points.
		int dx1;
		int dy1;
		int dx2;
		int dy2;
		int delta = 0;
		for (int i = 0; i < pinCount / 2; i++) {
			if (i == 1) {
				delta = size == RelaySize.Miniature ? (int) MINI_GAP
						.convertToPixels() : (int) ULTRA_GAP.convertToPixels();
				if (type == RelayType.SPDT) {
					delta += pinSpacing;
				}
			}
			switch (orientation) {
			case DEFAULT:
				dx1 = 0;
				dy1 = i * pinSpacing + delta;
				dx2 = rowSpacing;
				dy2 = i * pinSpacing + delta;
				break;
			case _90:
				dx1 = -(i * pinSpacing + delta);
				dy1 = 0;
				dx2 = -(i * pinSpacing + delta);
				dy2 = rowSpacing;
				break;
			case _180:
				dx1 = 0;
				dy1 = -(i * pinSpacing + delta);
				dx2 = -rowSpacing;
				dy2 = -(i * pinSpacing + delta);
				break;
			case _270:
				dx1 = i * pinSpacing + delta;
				dy1 = 0;
				dx2 = i * pinSpacing + delta;
				dy2 = -rowSpacing;
				break;
			default:
				throw new RuntimeException("Unexpected orientation: "
						+ orientation);
			}
			controlPoints[i] = new Point(firstPoint.x + dx1, firstPoint.y + dy1);
			controlPoints[i + pinCount / 2] = new Point(firstPoint.x + dx2,
					firstPoint.y + dy2);
		}
	}

	public Area[] getBody() {
		if (body == null) {
			body = new Area[2];
			int x = controlPoints[0].x;
			int y = controlPoints[0].y;
			int centerX = (controlPoints[0].x + controlPoints[controlPoints.length - 1].x) / 2;
			int centerY = (controlPoints[0].y + controlPoints[controlPoints.length - 1].y) / 2;
			int bodyMargin = getClosestOdd(BODY_MARGIN.convertToPixels());
			int width = 0;
			int height = 0;
			int rowSpacing = size == RelaySize.Miniature ? (int) MINI_ROW_SPACING
					.convertToPixels()
					: (int) ULTRA_ROW_SPACING.convertToPixels();
			Area indentation = null;
			int indentationSize = getClosestOdd(INDENT_SIZE.convertToPixels());
			switch (orientation) {
			case DEFAULT:
				width = (int) (size == RelaySize.Miniature ? MINI_HEIGHT
						.convertToPixels() : ULTRA_HEIGHT.convertToPixels());
				height = (int) (size == RelaySize.Miniature ? MINI_WIDTH
						.convertToPixels() : ULTRA_WIDTH.convertToPixels());
				x -= bodyMargin;
				y -= bodyMargin;
				indentation = new Area(new Rectangle2D.Double(centerX
						- indentationSize / 2, y - indentationSize / 2,
						indentationSize, indentationSize));
				break;
			case _90:
				width = (int) (size == RelaySize.Miniature ? MINI_WIDTH
						.convertToPixels() : ULTRA_WIDTH.convertToPixels());
				height = (int) (size == RelaySize.Miniature ? MINI_HEIGHT
						.convertToPixels() : ULTRA_HEIGHT.convertToPixels());
				x -= -bodyMargin + width;
				y -= bodyMargin;
				indentation = new Area(new Rectangle2D.Double(x + width
						- indentationSize / 2, centerY - indentationSize / 2,
						indentationSize, indentationSize));
				break;
			case _180:
				width = (int) (size == RelaySize.Miniature ? MINI_HEIGHT
						.convertToPixels() : ULTRA_HEIGHT.convertToPixels());
				height = (int) (size == RelaySize.Miniature ? MINI_WIDTH
						.convertToPixels() : ULTRA_WIDTH.convertToPixels());
				x -= rowSpacing + bodyMargin;
				y -= -bodyMargin + height;
				indentation = new Area(new Rectangle2D.Double(centerX
						- indentationSize / 2,
						y + height - indentationSize / 2, indentationSize,
						indentationSize));
				break;
			case _270:
				width = (int) (size == RelaySize.Miniature ? MINI_WIDTH
						.convertToPixels() : ULTRA_WIDTH.convertToPixels());
				height = (int) (size == RelaySize.Miniature ? MINI_HEIGHT
						.convertToPixels() : ULTRA_HEIGHT.convertToPixels());
				x -= bodyMargin;
				y -= bodyMargin + rowSpacing;
				indentation = new Area(new Rectangle2D.Double(x
						- indentationSize / 2, centerY - indentationSize / 2,
						indentationSize, indentationSize));
				break;
			default:
				throw new RuntimeException("Unexpected orientation: "
						+ orientation);
			}
			body[0] = new Area(new RoundRectangle2D.Double(centerX - width / 2,
					centerY - height / 2, width, height, EDGE_RADIUS,
					EDGE_RADIUS));
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
		g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
		g2d.fill(mainArea);
		g2d.setComposite(oldComposite);

		if (!outlineMode) {
			int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
			for (Point point : controlPoints) {
				g2d.setColor(PIN_COLOR);
				g2d.fillOval(point.x - pinSize / 2, point.y - pinSize / 2,
						pinSize, pinSize);
				g2d.setColor(PIN_BORDER_COLOR);
				g2d.fillOval(point.x - pinSize / 2, point.y - pinSize / 2,
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
					: BORDER_COLOR;
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
				g2d.setColor(INDENT_COLOR);
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
					: LABEL_COLOR;
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
		g2d.setColor(BODY_COLOR);
		g2d.fillRoundRect(width / 6, 1, 4 * width / 6, height - 4, radius,
				radius);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRoundRect(width / 6, 1, 4 * width / 6, height - 4, radius,
				radius);
		int pinSize = 2 * width / 32;
		g2d.setColor(PIN_COLOR);
		for (int i = 0; i < 4; i++) {
			if (i == 1)
				continue;
			g2d.fillOval(width / 4, (height / 5) * (i + 1) - 1, pinSize,
					pinSize);
			g2d.fillOval(3 * width / 4 - pinSize, (height / 5) * (i + 1) - 1,
					pinSize, pinSize);
		}
	}

	@Override
	public String getValueForDisplay() {
		return getValue() + " " + getType() + " " + getVoltage();
	}

	@EditableProperty
	public RelayType getType() {
		return type;
	}

	public void setType(RelayType type) {
		this.type = type;
		updateControlPoints();
		// Invalidate body
		this.body = null;
	}

	@EditableProperty
	public Voltage getVoltage() {
		return voltage;
	}

	public void setVoltage(Voltage voltage) {
		this.voltage = voltage;
	}

	@EditableProperty
	public RelaySize getSize() {
		return size;
	}

	public void setSize(RelaySize size) {
		this.size = size;
		updateControlPoints();
		// Invalidate body
		this.body = null;
	}

	public static enum RelayType {

		SPDT, DPDT;

	}

	public static enum RelaySize {

		Miniature, Ultra_miniature;

		public String toString() {
			return name().replace('_', '-');
		};
	}
}
