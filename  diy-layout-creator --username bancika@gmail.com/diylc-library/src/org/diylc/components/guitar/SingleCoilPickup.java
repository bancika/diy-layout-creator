package org.diylc.components.guitar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
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

@ComponentDescriptor(name = "Strat Single Coil Pickup", category = "Guitar", author = "Branislav Stojkovic", description = "Strat-style single coil guitar pickup", stretchable = false, zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "PKP", autoEdit = false)
public class SingleCoilPickup extends AbstractTransparentComponent<String> {

	private static final long serialVersionUID = 1L;

	private static Color BODY_COLOR = Color.darkGray;
	private static Color POINT_COLOR = Color.lightGray;
	private static Size WIDTH = new Size(15.5d, SizeUnit.mm);
	private static Size LENGTH = new Size(83.82d, SizeUnit.mm);
	private static Size LIP_WIDTH = new Size(5d, SizeUnit.mm);
	private static Size LIP_LENGTH = new Size(20d, SizeUnit.mm);
	private static Size POINT_SIZE = new Size(3d, SizeUnit.mm);
	private static Size HOLE_SIZE = new Size(2d, SizeUnit.mm);
	private static Size HOLE_MARGIN = new Size(4d, SizeUnit.mm);
	private static Size POLE_SIZE = new Size(3d, SizeUnit.mm);
	private static Size POLE_SPACING = new Size(11.68d, SizeUnit.mm);

	private String value = "";
	private Point controlPoint = new Point(0, 0);
	transient Shape[] body;
	private Orientation orientation = Orientation.DEFAULT;
	private Color color = BODY_COLOR;

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
			Project project, IDrawingObserver drawingObserver) {
		Shape[] body = getBody();

		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		if (componentState != ComponentState.DRAGGING) {
			Composite oldComposite = g2d.getComposite();
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
						/ MAX_ALPHA));
			}
			g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : color);
			g2d.fill(body[0]);
			g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : POINT_COLOR);
			g2d.fill(body[1]);
			g2d.setComposite(oldComposite);
		}

		Color finalBorderColor;
		if (outlineMode) {
			Theme theme = (Theme) ConfigurationManager.getInstance().readObject(
					IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
			finalBorderColor = componentState == ComponentState.SELECTED
					|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : theme
					.getOutlineColor();
		} else {
			finalBorderColor = componentState == ComponentState.SELECTED
					|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : color
					.brighter();
		}

		g2d.setColor(finalBorderColor);
		g2d.draw(body[0]);
		if (!outlineMode) {
			g2d.draw(body[2]);
		}

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
		Rectangle bounds = body[0].getBounds();
		drawCenteredText(g2d, value, bounds.x + bounds.width / 2, bounds.y + bounds.height / 2,
				HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
	}

	public Shape[] getBody() {
		if (body == null) {
			body = new Shape[3];

			int x = controlPoint.x;
			int y = controlPoint.y;
			int width = (int) WIDTH.convertToPixels();
			int length = (int) LENGTH.convertToPixels();
			int lipWidth = (int) LIP_WIDTH.convertToPixels();
			int lipLength = (int) LIP_LENGTH.convertToPixels();
			int pointSize = getClosestOdd(POINT_SIZE.convertToPixels());
			int holeSize = getClosestOdd(HOLE_SIZE.convertToPixels());
			int holeMargin = getClosestOdd(HOLE_MARGIN.convertToPixels());

			Area mainArea = new Area(new RoundRectangle2D.Double(x - length / 2, y - lipWidth / 2
					- width, length, width, width, width));
			mainArea.add(new Area(new Polygon(new int[] { x - length / 2 + width / 2,
					x + length / 2 - width / 2, x + lipLength / 2, x - lipLength / 2 }, new int[] {
					y - lipWidth / 2, y - lipWidth / 2, y + lipWidth / 2, y + lipWidth / 2 }, 4)));
			// Cutout holes
			mainArea.subtract(new Area(new Ellipse2D.Double(x - length / 2 + holeMargin - holeSize
					/ 2, y - lipWidth / 2 - width / 2 - holeSize / 2, holeSize, holeSize)));
			mainArea.subtract(new Area(new Ellipse2D.Double(x + length / 2 - holeMargin - holeSize
					/ 2, y - lipWidth / 2 - width / 2 - holeSize / 2, holeSize, holeSize)));

			body[0] = mainArea;

			body[1] = new Area(new Ellipse2D.Double(x - pointSize / 2, y - pointSize / 2,
					pointSize, pointSize));

			int poleSize = (int) POLE_SIZE.convertToPixels();
			int poleSpacing = (int) POLE_SPACING.convertToPixels();
			int poleMargin = (length - poleSpacing * 5) / 2;
			Area poleArea = new Area();
			for (int i = 0; i < 6; i++) {
				Ellipse2D pole = new Ellipse2D.Double(
						x - length / 2 + poleMargin + i * poleSpacing, y - lipWidth / 2 - width / 2
								- poleSize / 2, poleSize, poleSize);
				poleArea.add(new Area(pole));
			}
			body[2] = poleArea;

			// Rotate if needed
			if (orientation != Orientation.DEFAULT) {
				double theta = 0;
				switch (orientation) {
				case _90:
					theta = Math.PI / 2;
					break;
				case _180:
					theta = Math.PI;
					break;
				case _270:
					theta = Math.PI * 3 / 2;
					break;
				}
				AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
				for (Shape shape : body) {
					Area area = (Area) shape;
					area.transform(rotation);
				}
			}
		}
		return body;
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int bodyWidth = 8 * width / 32;
		int bodyLength = 30 * width / 32;
		g2d.setColor(BODY_COLOR);
		g2d.fillRoundRect((width - bodyWidth) / 2, (height - bodyLength) / 2, bodyWidth,
				bodyLength, bodyWidth, bodyWidth);
		g2d.fillPolygon(new int[] { width * 9 / 16, width * 9 / 16, width * 11 / 16, width * 11 / 16 },
				new int[] { (height - bodyLength) / 2, (height + bodyLength) / 2, height * 5 / 8,
						height * 3 / 8 }, 4);
		
		g2d.setColor(Color.lightGray);
		g2d.drawLine(width / 2, 4 * width / 32, width / 2, 4 * width / 32);
		g2d.drawLine(width / 2, height - 4 * width / 32, width / 2, height - 4 * width / 32);
	}

	@Override
	public int getControlPointCount() {
		return 1;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.WHEN_SELECTED;
	}

	@Override
	public boolean isControlPointSticky(int index) {
		return true;
	}

	@Override
	public Point getControlPoint(int index) {
		return controlPoint;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		this.controlPoint.setLocation(point);
		// Invalidate the body
		body = null;
	}

	@EditableProperty(name = "Model")
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@EditableProperty
	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
		// Invalidate the body
		body = null;
	}

	@EditableProperty
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
