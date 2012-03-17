package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
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

@ComponentDescriptor(name = "Plastic DC Jack", category = "Electromechanical", author = "Branislav Stojkovic", description = "Panel mount plastic DC jack", stretchable = false, zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "J")
public class PlasticDCJack extends AbstractTransparentComponent<String> {

	private static final long serialVersionUID = 1L;

	private static Size LUG_WIDTH = new Size(0.08d, SizeUnit.in);
	private static Size LUG_THICKNESS = new Size(0.02d, SizeUnit.in);
	private static Size SPACING = new Size(0.1d, SizeUnit.in);
	private static Size DIAMETER = new Size(0.5d, SizeUnit.in);
	private static Color BODY_COLOR = Color.decode("#666666");
	private static Color PHENOLIC_COLOR = Color.decode("#CD8500");
	private static Color BORDER_COLOR = Color.black;
	private static Color MARKING_COLOR = Color.lightGray;

	private Point[] controlPoints = new Point[] { new Point(0, 0), new Point(0, 0), new Point(0, 0) };
	private String value = "";
	transient private Shape[] body;

	public PlasticDCJack() {
		updateControlPoints();
	}

	private void updateControlPoints() {
		// invalidate body shape
		body = null;

		int x = controlPoints[0].x;
		int y = controlPoints[0].y;

		int spacing = (int) SPACING.convertToPixels();
		controlPoints[1] = new Point(x + spacing, y + spacing);
		controlPoints[2] = new Point(x - spacing, y + spacing * 2);
	}

	public Shape[] getBody() {
		if (body == null) {
			body = new Shape[4];

			int x = controlPoints[0].x;
			int y = controlPoints[0].y;
			int spacing = (int) SPACING.convertToPixels();
			int diameter = getClosestOdd(DIAMETER.convertToPixels());
			body[0] = new Ellipse2D.Double(x - diameter / 2, y + spacing - diameter / 2, diameter,
					diameter);

			int rectWidth = (int) (diameter / Math.sqrt(2)) - 2;
			body[1] = new Rectangle(x - rectWidth / 2, y + spacing - rectWidth / 2, rectWidth,
					rectWidth);

			int lugWidth = getClosestOdd(LUG_WIDTH.convertToPixels());
			int lugThickness = getClosestOdd(LUG_THICKNESS.convertToPixels());

			Point groundPoint = controlPoints[controlPoints.length - 1];
			Area groundLug = new Area(new Ellipse2D.Double(groundPoint.x + spacing - lugWidth / 2,
					groundPoint.y - lugWidth / 2, lugWidth, lugWidth));
			groundLug.add(new Area(new Rectangle(groundPoint.x, groundPoint.y - lugWidth / 2,
					spacing, lugWidth)));
			groundLug.subtract(new Area(new Ellipse2D.Double(
					groundPoint.x + spacing - lugWidth / 6, groundPoint.y - lugWidth / 6,
					lugWidth / 3, lugWidth / 3)));
			body[2] = groundLug;

			Area lugArea = new Area();
			for (int i = 0; i < controlPoints.length; i++) {
				Point point = controlPoints[i];
				if (i == getControlPointCount() - 1) {
					lugArea.add(new Area(new Rectangle(point.x - lugThickness / 2, point.y
							- lugWidth / 2, lugThickness, lugWidth)));
				} else {
					lugArea.add(new Area(new Rectangle(point.x - lugWidth / 2, point.y
							- lugThickness / 2, lugWidth, lugThickness)));
				}
			}
			body[3] = lugArea;
		}
		return body;
	}

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
			g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
			g2d.fill(body[0]);
			if (!outlineMode) {
				g2d.setColor(PHENOLIC_COLOR);
				g2d.fill(body[1]);
			}
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
					|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR : BORDER_COLOR;
		}

		g2d.setColor(finalBorderColor);
		g2d.draw(body[0]);
		if (!outlineMode) {
			g2d.setColor(PHENOLIC_COLOR.darker());
			g2d.draw(body[1]);

			g2d.setColor(METAL_COLOR);
			g2d.fill(body[2]);
			g2d.setColor(METAL_COLOR.darker());
			g2d.draw(body[2]);
		}

		g2d.setColor(METAL_COLOR);
		g2d.fill(body[3]);
		g2d.setColor(METAL_COLOR.darker());
		g2d.draw(body[3]);

		if (!outlineMode) {
			int spacing = (int) SPACING.convertToPixels();
			g2d.setColor(MARKING_COLOR);
			g2d.setFont(LABEL_FONT.deriveFont(12f));
			drawCenteredText(g2d, "+", controlPoints[0].x, controlPoints[0].y - spacing * 7 / 16,
					HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
			drawCenteredText(g2d, "_", controlPoints[2].x, controlPoints[2].y - spacing * 3 / 4,
					HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
		}
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int margin = 2 * 32 / width;
		int diameter = getClosestOdd(width - margin);
		g2d.setColor(BODY_COLOR);
		g2d.fillOval((width - diameter) / 2, (height - diameter) / 2, diameter, diameter);
		g2d.setColor(BORDER_COLOR);
		g2d.drawOval((width - diameter) / 2, (height - diameter) / 2, diameter, diameter);
		int rectWidth = getClosestOdd(((width - 2 * margin) / Math.sqrt(2)) - margin / 2);
		g2d.setColor(PHENOLIC_COLOR);
		g2d.fillRect((width - rectWidth) / 2, (height - rectWidth) / 2, rectWidth, rectWidth);
		g2d.setColor(PHENOLIC_COLOR.darker());
		g2d.drawRect((width - rectWidth) / 2, (height - rectWidth) / 2, rectWidth, rectWidth);
		int lugWidth = 4 * 32 / width;
		g2d.setColor(METAL_COLOR);
		g2d.drawLine((width - lugWidth) / 2, height / 3, (width + lugWidth) / 2, height / 3);
		g2d
				.drawLine(width * 2 / 3, (height - lugWidth) / 2, width * 2 / 3,
						(height + lugWidth) / 2);
		g2d.fillOval((width - lugWidth) / 2, height * 2 / 3 - lugWidth / 2, lugWidth, lugWidth);
		g2d.fillRect(width / 2 - lugWidth * 3 / 2, height * 2 / 3 - lugWidth / 2, lugWidth * 3 / 2,
				lugWidth);
		g2d.setColor(PHENOLIC_COLOR);
		g2d.fillOval((width - margin) / 2, height * 2 / 3 - margin / 2, margin, margin);
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
		controlPoints[index].setLocation(point);
		this.body = null;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.NEVER;
	}

	@Override
	public boolean isControlPointSticky(int index) {
		return true;
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
}
