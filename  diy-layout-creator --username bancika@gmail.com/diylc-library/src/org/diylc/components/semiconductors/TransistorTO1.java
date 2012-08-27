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

@ComponentDescriptor(name = "Transistor (TO-1 package)", author = "Branislav Stojkovic", category = "Semiconductors", instanceNamePrefix = "Q", description = "Transistor with small metal body", stretchable = false, zOrder = IDIYComponent.COMPONENT)
public class TransistorTO1 extends AbstractTransparentComponent<String> {

	private static final long serialVersionUID = 1L;

	public static Color BODY_COLOR = Color.decode("#D0E0EF");
	public static Color BORDER_COLOR = BODY_COLOR.darker();
	public static Color PIN_COLOR = Color.decode("#00B2EE");
	public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
	public static Color LABEL_COLOR = Color.black;
	public static Size PIN_SIZE = new Size(0.03d, SizeUnit.in);
	public static Size PIN_SPACING = new Size(0.05d, SizeUnit.in);
	public static Size BODY_DIAMETER = new Size(0.24d, SizeUnit.in);
	public static Size BODY_LENGTH = new Size(0.4d, SizeUnit.in);
	public static Size EDGE_RADIUS = new Size(2d, SizeUnit.mm);

	private String value = "";
	private Orientation orientation = Orientation.DEFAULT;
	private Point[] controlPoints = new Point[] { new Point(0, 0),
			new Point(0, 0), new Point(0, 0) };
	transient private Area body;
	private Color bodyColor = BODY_COLOR;
	private Color borderColor = BORDER_COLOR;
	private Color labelColor = LABEL_COLOR;
	protected Display display = Display.NAME;
	private boolean folded = false;
	private Size pinSpacing = PIN_SPACING;

	public TransistorTO1() {
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
		int pinSpacing = (int) getPinSpacing().convertToPixels();
		// Update control points.
		int x = controlPoints[0].x;
		int y = controlPoints[0].y;
		switch (orientation) {
		case DEFAULT:
			controlPoints[1].setLocation(x - (folded ? 0 : pinSpacing), y
					+ pinSpacing);
			controlPoints[2].setLocation(x, y + 2 * pinSpacing);
			break;
		case _90:
			controlPoints[1].setLocation(x - pinSpacing, y
					- (folded ? 0 : pinSpacing));
			controlPoints[2].setLocation(x - 2 * pinSpacing, y);
			break;
		case _180:
			controlPoints[1].setLocation(x + (folded ? 0 : pinSpacing), y
					- pinSpacing);
			controlPoints[2].setLocation(x, y - 2 * pinSpacing);
			break;
		case _270:
			controlPoints[1].setLocation(x + pinSpacing, y
					+ (folded ? 0 : pinSpacing));
			controlPoints[2].setLocation(x + 2 * pinSpacing, y);
			break;
		default:
			throw new RuntimeException("Unexpected orientation: " + orientation);
		}
	}

	public Area getBody() {
		if (body == null) {
			int x = (controlPoints[0].x + controlPoints[1].x + controlPoints[2].x) / 3;
			int y = (controlPoints[0].y + controlPoints[1].y + controlPoints[2].y) / 3;
			int bodyDiameter = getClosestOdd(BODY_DIAMETER.convertToPixels());
			int bodyLength = getClosestOdd(BODY_LENGTH.convertToPixels());
			int edgeRadius = (int) EDGE_RADIUS.convertToPixels();

			if (folded) {
				switch (orientation) {
				case DEFAULT:
					body = new Area(new RoundRectangle2D.Double(x - bodyLength, y - bodyDiameter / 2, bodyLength, bodyDiameter, edgeRadius, edgeRadius));
					body.add(new Area(new Rectangle2D.Double(x - bodyLength / 2, y - bodyDiameter / 2, bodyLength / 2, bodyDiameter)));
					break;
				case _90:
					body = new Area(new RoundRectangle2D.Double(x - bodyDiameter / 2, y - bodyLength, bodyDiameter, bodyLength, edgeRadius, edgeRadius));
					body.add(new Area(new Rectangle2D.Double(x - bodyDiameter / 2, y - bodyLength / 2, bodyDiameter, bodyLength / 2)));
					break;
				case _180:
					body = new Area(new RoundRectangle2D.Double(x, y - bodyDiameter / 2, bodyLength, bodyDiameter, edgeRadius, edgeRadius));
					body.add(new Area(new Rectangle2D.Double(x, y - bodyDiameter / 2, bodyLength / 2, bodyDiameter)));
					break;
				case _270:
					body = new Area(new RoundRectangle2D.Double(x - bodyDiameter / 2, y, bodyDiameter, bodyLength, edgeRadius, edgeRadius));
					body.add(new Area(new Rectangle2D.Double(x - bodyDiameter / 2, y, bodyDiameter, bodyLength / 2)));
					break;
				default:
					throw new RuntimeException("Unexpected orientation: " + orientation);
				}
			} else {
				body = new Area(new Ellipse2D.Double(x - bodyDiameter / 2, y
						- bodyDiameter / 2, bodyDiameter, bodyDiameter));
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
		int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
		Area mainArea = getBody();
		Composite oldComposite = g2d.getComposite();
		if (alpha < MAX_ALPHA) {
			g2d.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
		}
		g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
		g2d.fill(mainArea);
		g2d.setComposite(oldComposite);
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
					: borderColor;
		}
		g2d.setColor(finalBorderColor);
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.draw(mainArea);

		if (!outlineMode) {
			for (Point point : controlPoints) {
				g2d.setColor(PIN_COLOR);
				g2d.fillOval(point.x - pinSize / 2, point.y - pinSize / 2,
						pinSize, pinSize);
				g2d.setColor(PIN_BORDER_COLOR);
				g2d.drawOval(point.x - pinSize / 2, point.y - pinSize / 2,
						pinSize, pinSize);
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
		String label = (getDisplay() == Display.NAME) ? getName() : getValue();
		FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
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
		int margin = 2 * width / 32;
		Area area = new Area(new Ellipse2D.Double(margin / 2, margin, width - 2
				* margin, width - 2 * margin));
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

	@EditableProperty
	public boolean getFolded() {
		return folded;
	}

	public void setFolded(boolean folded) {
		this.folded = folded;
		updateControlPoints();
		// Reset body shape;
		body = null;
	}
	
	@EditableProperty(name="Pin spacing")
	public Size getPinSpacing() {
		return pinSpacing;
	}
	
	public void setPinSpacing(Size pinSpacing) {
		this.pinSpacing = pinSpacing;
		updateControlPoints();
		// Reset body shape;
		body = null;
	}

	@EditableProperty(name = "Border")
	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}
	
	@EditableProperty(name = "Label")
	public Color getLabelColor() {
		return labelColor;
	}
	
	public void setLabelColor(Color labelColor) {
		this.labelColor = labelColor;
	}

	@EditableProperty
	public Display getDisplay() {
		if (display == null) {
			display = Display.NAME;
		}
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}
}
