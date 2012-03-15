package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
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

@ComponentDescriptor(name = "Cliff 1/4\" Jack", category = "Electromechanical", author = "Branislav Stojkovic", description = "Cliff-style enclosed panel mount 1/4\" phono jack", stretchable = false, zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "J")
public class CliffJack extends AbstractTransparentComponent<String> {

	private static final long serialVersionUID = 1L;

	private static Size SPACING = new Size(0.3d, SizeUnit.in);
	private static Size PIN_WIDTH = new Size(0.1d, SizeUnit.in);
	private static Size PIN_THICKNESS = new Size(0.02d, SizeUnit.in);
	private static Color BODY_COLOR = Color.decode("#666666");
	private static Color NUT_COLOR = Color.decode("#999999");
	private static Color BORDER_COLOR = Color.black;
	private static Color LABEL_COLOR = Color.lightGray;
	private static Size BODY_WIDTH = new Size(0.8d, SizeUnit.in);
	private static Size BODY_LENGTH = new Size(1d, SizeUnit.in);
	private static Size TAIL_LENGTH = new Size(0.1d, SizeUnit.in);

	private Point[] controlPoints = new Point[] { new Point(0, 0) };
	private JackType type = JackType.MONO;
	private Orientation orientation = Orientation.DEFAULT;
	private Shape[] body;
	private String value = "";

	public CliffJack() {
		super();
		updateControlPoints();
	}

	private void updateControlPoints() {
		// invalidate body shape
		body = null;
		int x = controlPoints[0].x;
		int y = controlPoints[0].y;
		int spacing = (int) SPACING.convertToPixels();
		controlPoints = new Point[type == JackType.STEREO ? 6 : 4];

		controlPoints[0] = new Point(x, y);
		controlPoints[1] = new Point(x, y + 2 * spacing);
		controlPoints[2] = new Point(x + 2 * spacing, y);
		controlPoints[3] = new Point(x + 2 * spacing, y + 2 * spacing);
		if (type == JackType.STEREO) {
			controlPoints[4] = new Point(x + spacing, y);
			controlPoints[5] = new Point(x + spacing, y + 2 * spacing);
		}

		// Apply rotation if necessary
		double angle = getAngle();
		if (angle != 0) {
			AffineTransform rotation = AffineTransform.getRotateInstance(angle, x, y);
			for (int i = 1; i < controlPoints.length; i++) {
				rotation.transform(controlPoints[i], controlPoints[i]);
			}
		}
	}

	private double getAngle() {
		// Apply rotation if necessary
		double angle;
		switch (orientation) {
		case _90:
			angle = Math.PI / 2;
			break;
		case _180:
			angle = Math.PI;
			break;
		case _270:
			angle = Math.PI * 3 / 2;
			break;
		default:
			angle = 0;
		}

		return angle;
	}

	public Shape[] getBody() {
		if (body == null) {
			body = new Shape[5];

			// Create body.
			int bodyLength = (int) BODY_LENGTH.convertToPixels();
			int bodyWidth = (int) BODY_WIDTH.convertToPixels();
			int centerX = (controlPoints[0].x + controlPoints[3].x) / 2;
			int centerY = (controlPoints[0].y + controlPoints[3].y) / 2;
			body[0] = new Rectangle(centerX - bodyLength / 2, centerY - bodyWidth / 2, bodyLength,
					bodyWidth);

			int tailLength = (int) TAIL_LENGTH.convertToPixels();
			body[1] = new RoundRectangle2D.Double(centerX - bodyLength / 2 - tailLength, centerY
					- bodyWidth / 4, tailLength * 2, bodyWidth / 2, tailLength, tailLength);
			Area tailArea = new Area(body[1]);
			tailArea.subtract(new Area(body[0]));
			body[1] = tailArea;

			body[2] = new Rectangle(centerX + bodyLength / 2, centerY - bodyWidth / 4, tailLength,
					bodyWidth / 2);

			body[3] = new Rectangle(centerX + bodyLength / 2 + tailLength, centerY - bodyWidth / 4,
					tailLength, bodyWidth / 2);
			tailArea = new Area(body[3]);
			int radius = bodyLength / 2 + tailLength * 2;
			tailArea.intersect(new Area(new Ellipse2D.Double(centerX - radius, centerY - radius,
					radius * 2, radius * 2)));
			body[3] = tailArea;

			// Apply rotation if necessary
			double angle = getAngle();
			if (angle != 0) {
				AffineTransform rotation = AffineTransform.getRotateInstance(angle, centerX,
						centerY);
				for (int i = 0; i < body.length; i++) {
					if (body[i] != null) {
						Area area = new Area(body[i]);
						area.transform(rotation);
						body[i] = area;
					}
				}
			}

			// Create pins.
			Area pins = new Area();

			int pinWidth = (int) PIN_WIDTH.convertToPixels();
			int pinThickness = (int) PIN_THICKNESS.convertToPixels();
			for (int i = 0; i < getControlPointCount(); i++) {
				Point point = getControlPoint(i);
				Rectangle pin;
				if (orientation == Orientation.DEFAULT || orientation == Orientation._180) {
					pin = new Rectangle(point.x - pinWidth / 2, point.y - pinThickness / 2,
							pinWidth, pinThickness);
				} else {
					pin = new Rectangle(point.x - pinThickness / 2, point.y - pinWidth / 2,
							pinThickness, pinWidth);
				}
				pins.add(new Area(pin));
			}

			body[4] = pins;
		}
		return body;
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
			Project project, IDrawingObserver drawingObserver) {
		Shape[] body = getBody();

		// Rectangle bounds = body.getBounds();

		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		if (componentState != ComponentState.DRAGGING) {
			Composite oldComposite = g2d.getComposite();
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
						/ MAX_ALPHA));
			}
			g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
			// GradientPaint gradient;
			// if (orientation == Orientation.DEFAULT || orientation ==
			// Orientation._180) {
			// gradient = new GradientPaint(bounds.x, bounds.y, BORDER_COLOR,
			// bounds.x, bounds.y
			// + bounds.height / 2, BODY_COLOR, true);
			// } else {
			// gradient = new GradientPaint(bounds.x, bounds.y, BORDER_COLOR,
			// bounds.x
			// + bounds.width / 2, bounds.y, BODY_COLOR, true);
			// }
			// Paint oldPaint = g2d.getPaint();
			// g2d.setPaint(gradient);
			for (int i = 0; i < body.length - 1; i++) {
				// Nut is brighter colored.
				if (i == body.length - 2)
					g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : NUT_COLOR);
				g2d.fill(body[i]);
			}
			// g2d.setPaint(oldPaint);
			g2d.setComposite(oldComposite);
		}
		g2d.setColor(componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : BORDER_COLOR);
		for (int i = 0; i < body.length - 1; i++) {
			g2d.draw(body[i]);
		}

		// Pins are the last piece.
		Shape pins = body[body.length - 1];
		g2d.setColor(METAL_COLOR);
		g2d.fill(pins);
		g2d.setColor(METAL_COLOR.darker());
		g2d.draw(pins);

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
		g2d.setFont(LABEL_FONT);
		int centerX = (controlPoints[0].x + controlPoints[3].x) / 2;
		int centerY = (controlPoints[0].y + controlPoints[3].y) / 2;
		drawCenteredText(g2d, name, centerX, centerY, HorizontalAlignment.CENTER,
				VerticalAlignment.CENTER);
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int bodyWidth = getClosestOdd(width * 3 / 5);
		int tailWidth = getClosestOdd(width * 3 / 9);

		g2d.setColor(BODY_COLOR);
		g2d.fillRoundRect((width - tailWidth) / 2, height / 2, tailWidth, height / 2 - 2 * 32
				/ height, 3 * 32 / width, 4 * 32 / width);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRoundRect((width - tailWidth) / 2, height / 2, tailWidth, height / 2 - 2 * 32
				/ height, 3 * 32 / width, 4 * 32 / width);

		g2d.setColor(NUT_COLOR);
		g2d.fillRoundRect((width - tailWidth) / 2, 2 * 32 / height, tailWidth, height / 2,
				3 * 32 / width, 4 * 32 / width);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRoundRect((width - tailWidth) / 2, 2 * 32 / height, tailWidth, height / 2,
				3 * 32 / width, 4 * 32 / width);

		g2d.setColor(BODY_COLOR);
		g2d.fillRect((width - bodyWidth) / 2, height / 7 + 1, bodyWidth, height * 5 / 7);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRect((width - bodyWidth) / 2, height / 7 + 1, bodyWidth, height * 5 / 7);

		g2d.setColor(METAL_COLOR);
		int pinX1 = getClosestOdd((width - bodyWidth * 3 / 4) / 2);
		int pinX2 = getClosestOdd((width + bodyWidth * 3 / 4) / 2) - 1;
		g2d.drawLine(pinX1, width * 2 / 8, pinX1, width * 3 / 8);
		g2d.drawLine(pinX1, width * 5 / 8, pinX1, width * 6 / 8);
		g2d.drawLine(pinX2, width * 2 / 8, pinX2, width * 3 / 8);
		g2d.drawLine(pinX2, width * 5 / 8, pinX2, width * 6 / 8);
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
		controlPoints[index] = point;
		updateControlPoints();
	}

	@Override
	public boolean isControlPointSticky(int index) {
		return true;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.NEVER;
	}

	@EditableProperty
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@EditableProperty
	public JackType getType() {
		return type;
	}

	public void setType(JackType type) {
		this.type = type;
		updateControlPoints();
	}

	@EditableProperty
	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
		updateControlPoints();
	}
}
