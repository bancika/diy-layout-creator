package org.diylc.components.passive;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Potentiometer (panel mount)", author = "Branislav Stojkovic", category = "Passive", creationMethod = CreationMethod.SINGLE_CLICK, instanceNamePrefix = "VR", description = "Panel mount potentiometer with solder lugs", zOrder = IDIYComponent.COMPONENT, stretchable = false)
public class PotentiometerPanel extends AbstractPotentiometer {

	private static final long serialVersionUID = 1L;

	protected static Size BODY_DIAMETER = new Size(1d, SizeUnit.in);
	protected static Size SPACING = new Size(0.3d, SizeUnit.in);
	protected static Size LUG_DIAMETER = new Size(0.15d, SizeUnit.in);
	protected static Color BODY_COLOR = Color.gray.brighter();
	protected static Color BORDER_COLOR = Color.gray;

	protected Size bodyDiameter = BODY_DIAMETER;
	protected Size spacing = SPACING;
	protected Size lugDiameter = LUG_DIAMETER;
	protected Color bodyColor = BODY_COLOR;
	protected Color borderColor = BORDER_COLOR;
	// Array of 7 elements: 3 lug connectors, 1 pot body and 3 lugs
	transient protected Area[] body = null;

	public PotentiometerPanel() {
		controlPoints = new Point[] { new Point(0, 0), new Point(0, 0), new Point(0, 0) };
		updateControlPoints();
	}

	protected void updateControlPoints() {
		int spacing = (int) this.spacing.convertToPixels();
		switch (orientation) {
		case DEFAULT:
			controlPoints[1].setLocation(controlPoints[0].x + spacing, controlPoints[0].y);
			controlPoints[2].setLocation(controlPoints[0].x + 2 * spacing, controlPoints[0].y);
			break;
		case _90:
			controlPoints[1].setLocation(controlPoints[0].x, controlPoints[0].y + spacing);
			controlPoints[2].setLocation(controlPoints[0].x, controlPoints[0].y + 2 * spacing);
			break;
		case _180:
			controlPoints[1].setLocation(controlPoints[0].x - spacing, controlPoints[0].y);
			controlPoints[2].setLocation(controlPoints[0].x - 2 * spacing, controlPoints[0].y);
			break;
		case _270:
			controlPoints[1].setLocation(controlPoints[0].x, controlPoints[0].y - spacing);
			controlPoints[2].setLocation(controlPoints[0].x, controlPoints[0].y - 2 * spacing);
			break;
		default:
			break;
		}
	}

	public Area[] getBody() {
		int spacing = (int) this.spacing.convertToPixels();
		int diameter = getClosestOdd(bodyDiameter.convertToPixels());
		if (body == null) {
			body = new Area[7];

			// Add lugs.
			int lugDiameter = getClosestOdd(this.lugDiameter.convertToPixels());
			int holeDiameter = getClosestOdd(this.lugDiameter.convertToPixels() / 2);
			for (int i = 0; i < 3; i++) {
				Area area = new Area(new Ellipse2D.Double(controlPoints[i].x - lugDiameter / 2,
						controlPoints[i].y - lugDiameter / 2, lugDiameter, lugDiameter));
				body[4 + i] = area;
			}

			switch (orientation) {
			case DEFAULT:
				body[3] = new Area(new Ellipse2D.Double(
						controlPoints[0].x + spacing - diameter / 2, controlPoints[0].y - spacing
								/ 2 - diameter, diameter, diameter));
				for (int i = 0; i < 3; i++) {
					body[i] = new Area(new Rectangle2D.Double(
							controlPoints[i].x - holeDiameter / 2, controlPoints[i].y
									- (spacing + diameter) / 2, holeDiameter,
							(spacing + diameter) / 2));
				}
				break;
			case _90:
				body[3] = new Area(new Ellipse2D.Double(controlPoints[0].x + spacing / 2,
						controlPoints[0].y + spacing - diameter / 2, diameter, diameter));
				for (int i = 0; i < 3; i++) {
					body[i] = new Area(new Rectangle2D.Double(controlPoints[i].x,
							controlPoints[i].y - holeDiameter / 2, (spacing + diameter) / 2,
							holeDiameter));
				}
				break;
			case _180:
				body[3] = new Area(new Ellipse2D.Double(
						controlPoints[0].x - spacing - diameter / 2, controlPoints[0].y + spacing
								/ 2, diameter, diameter));
				for (int i = 0; i < 3; i++) {
					body[i] = new Area(new Rectangle2D.Double(
							controlPoints[i].x - holeDiameter / 2, controlPoints[i].y,
							holeDiameter, (spacing + diameter) / 2));
				}
				break;
			case _270:
				body[3] = new Area(new Ellipse2D.Double(
						controlPoints[0].x - spacing / 2 - diameter, controlPoints[0].y - spacing
								- diameter / 2, diameter, diameter));
				for (int i = 0; i < 3; i++) {
					body[i] = new Area(new Rectangle2D.Double(controlPoints[i].x
							- (spacing + diameter) / 2, controlPoints[i].y - holeDiameter / 2,
							(spacing + diameter) / 2, holeDiameter));
				}
				break;
			default:
				break;
			}
			for (int i = 0; i < 3; i++) {
				for (int j = 3; j < 7; j++) {
					body[i].subtract(body[j]);
				}
			}
			// Make holes in the lugs.
			for (int i = 0; i < 3; i++) {
				body[4 + i].subtract(new Area(new Ellipse2D.Double(controlPoints[i].x
						- holeDiameter / 2, controlPoints[i].y - holeDiameter / 2, holeDiameter,
						holeDiameter)));
			}
		}
		return body;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		super.setControlPoint(point, index);
		body = null;
	}

	@Override
	public void setOrientation(Orientation orientation) {
		super.setOrientation(orientation);
		updateControlPoints();
		body = null;
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
			Project project, IDrawingObserver drawingObserver) {
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		for (Area shape : getBody()) {
			if (shape != null) {
				g2d.setColor(bodyColor);
				Composite oldComposite = g2d.getComposite();
				if (alpha < MAX_ALPHA) {
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
							/ MAX_ALPHA));
				}
				g2d.fill(shape);
				g2d.setComposite(oldComposite);
				g2d.setColor(componentState == ComponentState.SELECTED
						|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR
						: borderColor);
				g2d.draw(shape);
			}
		}
		// Draw caption.
		g2d.setFont(LABEL_FONT);
		g2d
				.setColor(componentState == ComponentState.SELECTED ? LABEL_COLOR_SELECTED
						: LABEL_COLOR);
		FontMetrics fontMetrics = g2d.getFontMetrics();
		Rectangle2D bodyRect = getBody()[3].getBounds2D();
		Rectangle2D rect = fontMetrics.getStringBounds(getName(), g2d);

		int textHeight = (int) rect.getHeight();
		int textWidth = (int) rect.getWidth();
		int panelHeight = (int) bodyRect.getHeight();
		int panelWidth = (int) bodyRect.getWidth();

		int x = (panelWidth - textWidth) / 2;
		int y = panelHeight / 2 - textHeight + fontMetrics.getAscent();

		g2d.drawString(getName(), (int) (bodyRect.getX() + x), (int) (bodyRect.getY() + y));

		// Draw value.
		rect = fontMetrics.getStringBounds(getValueForDisplay(), g2d);

		textHeight = (int) rect.getHeight();
		textWidth = (int) rect.getWidth();

		x = (panelWidth - textWidth) / 2;
		y = panelHeight / 2 + fontMetrics.getAscent();

		g2d.drawString(getValueForDisplay(), (int) (bodyRect.getX() + x),
				(int) (bodyRect.getY() + y));
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int margin = 4 * width / 32;
		int spacing = width / 3 - 1;
		g2d.setColor(BORDER_COLOR);
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2 * width / 32));
		g2d.drawLine(width / 2 - spacing, height / 2, width / 2 - spacing, height - margin);
		g2d.drawLine(width / 2 + spacing, height / 2, width / 2 + spacing, height - margin);
		g2d.drawLine(width / 2, height / 2, width / 2, height - margin);
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(4 * width / 32));
		g2d.drawLine(width / 2 - spacing, height - margin, width / 2 - spacing, height - margin);
		g2d.drawLine(width / 2 + spacing, height - margin, width / 2 + spacing, height - margin);
		g2d.drawLine(width / 2, height - margin, width / 2, height - margin);
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.setColor(BODY_COLOR);
		g2d.fillOval(margin, margin / 2, width - 2 * margin, height - 2 * margin);
		g2d.setColor(BORDER_COLOR);
		g2d.drawOval(margin, margin / 2, width - 2 * margin, height - 2 * margin);
	}

	@EditableProperty
	public Size getSpacing() {
		return spacing;
	}

	public void setSpacing(Size spacing) {
		this.spacing = spacing;
		updateControlPoints();
		body = null;
	}

	@EditableProperty(name = "Diameter")
	public Size getBodyDiameter() {
		return bodyDiameter;
	}

	public void setBodyDiameter(Size bodyDiameter) {
		this.bodyDiameter = bodyDiameter;
	}

	@EditableProperty(name = "Lug size")
	public Size getLugDiameter() {
		return lugDiameter;
	}

	public void setLugDiameter(Size lugDiameter) {
		this.lugDiameter = lugDiameter;
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
