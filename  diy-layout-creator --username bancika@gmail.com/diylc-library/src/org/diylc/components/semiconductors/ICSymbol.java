package org.diylc.components.semiconductors;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
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

@ComponentDescriptor(name = "IC Symbol", author = "Branislav Stojkovic", category = "Semiconductors", instanceNamePrefix = "IC", description = "IC symbol with 3 or 5 contacts", stretchable = false, zOrder = IDIYComponent.COMPONENT)
public class ICSymbol extends AbstractTransparentComponent<String> {

	private static final long serialVersionUID = 1L;

	public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
	public static Color BODY_COLOR = Color.white;
	public static Color BORDER_COLOR = Color.black;

	protected ICPointCount icPointCount = ICPointCount._5;
	protected String value = "";
	protected Point[] controlPoints = new Point[] { new Point(0, 0), new Point(0, 0),
			new Point(0, 0), new Point(0, 0), new Point(0, 0) };
	protected Color bodyColor = BODY_COLOR;
	protected Color borderColor = BORDER_COLOR;
	protected Display display = Display.NAME;
	transient private Shape body;

	public ICSymbol() {
		super();
		updateControlPoints();
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
			Project project, IDrawingObserver drawingObserver) {
		if (checkPointsClipped(g2d.getClip())) {
			return;
		}
		int pinSpacing = (int) PIN_SPACING.convertToPixels();
		Composite oldComposite = g2d.getComposite();
		if (alpha < MAX_ALPHA) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
					/ MAX_ALPHA));
		}
		g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
		g2d.fill(getBody());
		g2d.setComposite(oldComposite);
		Color finalBorderColor;
		if (outlineMode) {
			Theme theme = (Theme) ConfigurationManager.getInstance().readObject(
					IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
			finalBorderColor = theme.getOutlineColor();
		} else {
			finalBorderColor = borderColor;
		}
		g2d.setColor(finalBorderColor);
		// Draw contacts
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.drawLine(controlPoints[0].x, controlPoints[0].y, controlPoints[0].x + pinSpacing / 2,
				controlPoints[0].y);
		g2d.drawLine(controlPoints[1].x, controlPoints[1].y, controlPoints[1].x + pinSpacing / 2,
				controlPoints[1].y);
		g2d.drawLine(controlPoints[2].x, controlPoints[2].y, controlPoints[2].x - pinSpacing / 2,
				controlPoints[2].y);
		if (icPointCount == ICPointCount._5) {
			g2d.drawLine(controlPoints[3].x, controlPoints[3].y, controlPoints[3].x,
					controlPoints[3].y + pinSpacing * 3 / 4);
			g2d.drawLine(controlPoints[4].x, controlPoints[4].y, controlPoints[4].x,
					controlPoints[4].y - pinSpacing * 3 / 4);
		}
		// Draw triangle
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
		g2d.draw(getBody());
		// Draw label
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
		int x = (controlPoints[0].x + controlPoints[2].x) / 2;
		drawCenteredText(g2d, display == Display.VALUE ? getValue() : getName(), x,
				controlPoints[0].y + pinSpacing, HorizontalAlignment.CENTER,
				VerticalAlignment.CENTER);
		// Draw +/- markers
		drawCenteredText(g2d, "-", controlPoints[0].x + pinSpacing, controlPoints[0].y,
				HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
		drawCenteredText(g2d, "+", controlPoints[1].x + pinSpacing, controlPoints[1].y,
				HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int margin = 3 * width / 32;
		Area area = new Area(new Polygon(new int[] { margin, margin, width - margin }, new int[] {
				margin, height - margin, height / 2 }, 3));
		// area.subtract(new Area(new Rectangle2D.Double(0, 0, 2 * margin,
		// height)));
		area.intersect(new Area(new Rectangle2D.Double(2 * margin, 0, width, height)));
		g2d.setColor(BODY_COLOR);
		g2d.fill(area);
		g2d.setColor(BORDER_COLOR);
		g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2d.draw(area);
	}

	@Override
	public Point getControlPoint(int index) {
		return controlPoints[index];
	}

	@Override
	public int getControlPointCount() {
		return icPointCount.getValue();
	}

	private void updateControlPoints() {
		int pinSpacing = (int) PIN_SPACING.convertToPixels();
		// Update control points.
		int x = controlPoints[0].x;
		int y = controlPoints[0].y;

		controlPoints[1].x = x;
		controlPoints[1].y = y + pinSpacing * 2;

		controlPoints[2].x = x + pinSpacing * 6;
		controlPoints[2].y = y + pinSpacing;

		controlPoints[3].x = x + pinSpacing * 3;
		controlPoints[3].y = y - pinSpacing;

		controlPoints[4].x = x + pinSpacing * 3;
		controlPoints[4].y = y + pinSpacing * 3;
	}

	public Shape getBody() {
		if (body == null) {
			int pinSpacing = (int) PIN_SPACING.convertToPixels();
			int x = controlPoints[0].x;
			int y = controlPoints[0].y;
			body = new Polygon(new int[] { x + pinSpacing / 2, x + pinSpacing * 11 / 2,
					x + pinSpacing / 2 }, new int[] { y - pinSpacing * 3 / 2, y + pinSpacing,
					y + pinSpacing * 7 / 2 }, 3);
		}
		return body;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.WHEN_SELECTED;
	}

	@EditableProperty
	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean isControlPointSticky(int index) {
		return true;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		controlPoints[index].setLocation(point);
		body = null;
	}

	@EditableProperty(name = "Contacts")
	public ICPointCount getIcPointCount() {
		return icPointCount;
	}

	public void setIcPointCount(ICPointCount icPointCount) {
		this.icPointCount = icPointCount;
		updateControlPoints();
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

	@EditableProperty
	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}
}
