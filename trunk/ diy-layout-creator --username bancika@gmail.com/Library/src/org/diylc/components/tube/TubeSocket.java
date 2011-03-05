package org.diylc.components.tube;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

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

@ComponentDescriptor(name = "Tube Socket", author = "Branislav Stojkovic", category = "Tubes", instanceNamePrefix = "V", description = "Various types of tube/valve sockets", stretchable = false, zOrder = IDIYComponent.COMPONENT)
public class TubeSocket extends AbstractTransparentComponent<String> {

	private static final long serialVersionUID = 1L;

	private static Color BODY_COLOR = Color.decode("#FFFFE0");
	private static Color BORDER_COLOR = Color.decode("#8E8E38");
	public static Color PIN_COLOR = Color.decode("#00B2EE");
	public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
	public static Size PIN_SIZE = new Size(1d, SizeUnit.mm);
	public static Size HOLE_SIZE = new Size(5d, SizeUnit.mm);
	public static Size TICK_SIZE = new Size(2d, SizeUnit.mm);

	private Base base = Base.B9A;
	private String type = "";
	private Orientation orientation = Orientation.DEFAULT;
	// private Mount mount = Mount.CHASSIS;

	private Point[] controlPoints = new Point[] { new Point(0, 0) };

	transient private Shape body;

	public TubeSocket() {
		super();
		updateControlPoints();
	}

	@EditableProperty
	public Base getBase() {
		return base;
	}

	public void setBase(Base base) {
		this.base = base;
		updateControlPoints();
		// Reset body shape
		body = null;
	}

	@EditableProperty
	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
		updateControlPoints();
		// Reset body shape
		body = null;
	}

	// @EditableProperty
	// public Mount getMount() {
	// return mount;
	// }
	//
	// public void setMount(Mount mount) {
	// this.mount = mount;
	// }

	private void updateControlPoints() {
		Point firstPoint = controlPoints[0];
		int pinCount;
		int pinCircleDiameter;
		boolean hasEmptySpace;
		switch (base) {
		case B7G:
			pinCount = 7;
			pinCircleDiameter = getClosestOdd(new Size(12d, SizeUnit.mm).convertToPixels());
			hasEmptySpace = true;
			break;
		case OCTAL:
			pinCount = 8;
			pinCircleDiameter = getClosestOdd(new Size(17.5d, SizeUnit.mm).convertToPixels());
			hasEmptySpace = false;
			break;
		case B9A:
			pinCount = 9;
			pinCircleDiameter = getClosestOdd(new Size(14d, SizeUnit.mm).convertToPixels());
			hasEmptySpace = true;
			break;
		default:
			throw new RuntimeException("Unexpected base: " + base);
		}
		double angleIncrement = Math.PI * 2 / (hasEmptySpace ? (pinCount + 1) : pinCount);
		double initialAngleOffset = hasEmptySpace ? angleIncrement : (angleIncrement / 2);
		double initialAngle;
		switch (orientation) {
		case DEFAULT:
			initialAngle = Math.PI / 2 + initialAngleOffset;
			break;
		case _90:
			initialAngle = Math.PI + initialAngleOffset;
			break;
		case _180:
			initialAngle = 3 * Math.PI / 2 + initialAngleOffset;
			break;
		case _270:
			initialAngle = initialAngleOffset;
			break;
		default:
			throw new RuntimeException("Unexpected orientation: " + orientation);
		}
		controlPoints = new Point[pinCount + 1];
		double angle = initialAngle;
		controlPoints[0] = firstPoint;
		for (int i = 0; i < pinCount; i++) {
			controlPoints[i + 1] = new Point((int) (firstPoint.getX() + Math.cos(angle)
					* pinCircleDiameter / 2), (int) (firstPoint.getY() + Math.sin(angle)
					* pinCircleDiameter / 2));
			angle += angleIncrement;
		}
	}

	public Shape getBody() {
		if (body == null) {
			int bodyDiameter;
			switch (base) {
			case B7G:
				bodyDiameter = getClosestOdd(new Size(17d, SizeUnit.mm).convertToPixels());
				break;
			case B9A:
				bodyDiameter = getClosestOdd(new Size(19d, SizeUnit.mm).convertToPixels());
				break;
			case OCTAL:
				bodyDiameter = getClosestOdd(new Size(24.5d, SizeUnit.mm).convertToPixels());
				break;
			default:
				throw new RuntimeException("Unexpected base: " + base);
			}
			body = new Ellipse2D.Double(controlPoints[0].x - bodyDiameter / 2, controlPoints[0].y
					- bodyDiameter / 2, bodyDiameter, bodyDiameter);
			Area bodyArea = new Area(body);
			int holeSize = getClosestOdd(HOLE_SIZE.convertToPixels());
			bodyArea.subtract(new Area(new Ellipse2D.Double(controlPoints[0].x - holeSize / 2,
					controlPoints[0].y - holeSize / 2, holeSize, holeSize)));
			if (base == Base.OCTAL) {
				int tickSize = getClosestOdd(TICK_SIZE.convertToPixels());
				double angle = 0;
				switch (orientation) {
				case DEFAULT:
					angle = Math.PI / 2;
					break;
				case _90:
					angle = Math.PI;
					break;
				case _180:
					angle = 3 * Math.PI / 2;
					break;
				case _270:
					angle = 0;
					break;
				}
				int centerX = (int) (controlPoints[0].x + Math.cos(angle) * holeSize / 2);
				int centerY = (int) (controlPoints[0].y + Math.sin(angle) * holeSize / 2);
				bodyArea.subtract(new Area(new Ellipse2D.Double(centerX - tickSize / 2, centerY
						- tickSize / 2, tickSize, tickSize)));
			}
			body = bodyArea;
		}
		return body;
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
			Project project, IDrawingObserver drawingObserver) {
		// g2d.setColor(Color.black);
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		// for (int i = 0; i < controlPoints.length; i++) {
		// g2d.drawString(Integer.toString(i), controlPoints[i].x,
		// controlPoints[i].y);
		// }
		// Draw body
		Shape body = getBody();
		Composite oldComposite = g2d.getComposite();
		if (alpha < MAX_ALPHA) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
					/ MAX_ALPHA));
		}
		if (componentState != ComponentState.DRAGGING) {
			g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
			g2d.fill(body);
		}
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
					|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : BORDER_COLOR;
		}
		g2d.setColor(finalBorderColor);
		g2d.draw(body);
		// Draw pins
		if (!outlineMode) {
			int pinSize = getClosestOdd(PIN_SIZE.convertToPixels());
			for (int i = 1; i < controlPoints.length; i++) {
				g2d.setColor(PIN_COLOR);
				g2d.fillOval(controlPoints[i].x - pinSize / 2, controlPoints[i].y - pinSize / 2,
						pinSize, pinSize);
				g2d.setColor(PIN_BORDER_COLOR);
				g2d.drawOval(controlPoints[i].x - pinSize / 2, controlPoints[i].y - pinSize / 2,
						pinSize, pinSize);
			}
		}
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		Area area = new Area(new Ellipse2D.Double(1, 1, width - 2, width - 2));
		int center = width / 2;
		area.subtract(new Area(new Ellipse2D.Double(center - 2, center - 2, 5, 5)));
		g2d.setColor(BODY_COLOR);
		g2d.fill(area);
		g2d.setColor(BORDER_COLOR);
		g2d.draw(area);

		int radius = width / 2 - 6;
		for (int i = 0; i < 8; i++) {
			int x = (int) (center + Math.cos(i * Math.PI / 4) * radius);
			int y = (int) (center + Math.sin(i * Math.PI / 4) * radius);
			g2d.setColor(PIN_COLOR);
			g2d.fillOval(x - 1, y - 1, 3, 3);
			g2d.setColor(PIN_BORDER_COLOR);
			g2d.drawOval(x - 1, y - 1, 3, 3);
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
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.NEVER;
	}

	@Override
	@EditableProperty(name = "Type")
	public String getValue() {
		return type;
	}

	@Override
	public void setValue(String value) {
		this.type = value;
	}

	@Override
	public boolean isControlPointSticky(int index) {
		return index > 0;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		controlPoints[index].setLocation(point);
		body = null;
	}

	static enum Base {
		B9A("Noval B9A"), OCTAL("Octal"), B7G("Small-button B7G");

		String name;

		private Base(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	static enum Mount {
		CHASSIS("Chassis"), PCB("PCB");

		String name;

		private Mount(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
