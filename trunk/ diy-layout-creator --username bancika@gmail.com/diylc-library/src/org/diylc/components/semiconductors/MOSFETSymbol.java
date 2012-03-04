package org.diylc.components.semiconductors;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import org.diylc.common.ObjectCache;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

@ComponentDescriptor(name = "MOSFET Symbol", author = "Branislav Stojkovic", category = "Semiconductors", instanceNamePrefix = "Q", description = "MOSFET transistor schematic symbol", stretchable = false, zOrder = IDIYComponent.COMPONENT)
public class MOSFETSymbol extends AbstractTransistorSymbol {

	private static final long serialVersionUID = 1L;

	protected FETPolarity polarity = FETPolarity.NEGATIVE;

	public Shape[] getBody() {
		if (body == null) {
			body = new Shape[3];
			int x = controlPoints[0].x;
			int y = controlPoints[0].y;
			int pinSpacing = (int) PIN_SPACING.convertToPixels();

			GeneralPath polyline = new GeneralPath();

			polyline.moveTo(x + pinSpacing / 2, y - pinSpacing + 1);
			polyline.lineTo(x + pinSpacing / 2, y + pinSpacing - 1);
			polyline.moveTo(x + pinSpacing, y - pinSpacing + 1);
			polyline.lineTo(x + pinSpacing, y + pinSpacing - 1);
			body[0] = polyline;

			polyline = new GeneralPath();

			polyline.moveTo(x + pinSpacing, y - pinSpacing);
			polyline.lineTo(x + pinSpacing * 2, y - pinSpacing);
			polyline.moveTo(x + pinSpacing, y + pinSpacing);
			polyline.lineTo(x + pinSpacing * 2, y + pinSpacing);
			polyline.moveTo(x, y);
			polyline.lineTo(x + pinSpacing / 2, y);
			polyline.moveTo(x + pinSpacing * 2, y - pinSpacing * 2);
			polyline.lineTo(x + pinSpacing * 2, y - pinSpacing);
			polyline.moveTo(x + pinSpacing * 2, y + pinSpacing * 2);
			polyline.lineTo(x + pinSpacing * 2, y + pinSpacing);
			body[1] = polyline;

			Polygon arrow;
			if (polarity == FETPolarity.NEGATIVE) {
				arrow = new Polygon(new int[] { x + pinSpacing * 8 / 6, x + pinSpacing * 8 / 6,
						x + pinSpacing * 12 / 6 }, new int[] { y + pinSpacing * 6 / 5,
						y + pinSpacing * 4 / 5, y + pinSpacing }, 3);
			} else {
				arrow = new Polygon(new int[] { x + pinSpacing * 7 / 6, x + pinSpacing * 11 / 6,
						x + pinSpacing * 11 / 6 }, new int[] { y - pinSpacing,
						y - pinSpacing * 6 / 5, y - pinSpacing * 4 / 5 }, 3);
			}
			body[2] = arrow;
		}
		return body;
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setColor(COLOR);
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
		g2d.drawLine(width * 2 / 5, height / 4 + 1, width * 2 / 5, height * 3 / 4 - 1);
		g2d.drawLine(width * 3 / 5, height / 4 + 1, width * 3 / 5, height * 3 / 4 - 1);

		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.drawLine(width / 5, height / 2, width * 2 / 5, height / 2);

		g2d.drawLine(width * 4 / 5, 1, width * 4 / 5, height / 4);
		g2d.drawLine(width * 4 / 5, height / 4, width * 3 / 5, height / 4);

		g2d.drawLine(width * 4 / 5, height - 1, width * 4 / 5, height * 3 / 4);
		g2d.drawLine(width * 4 / 5, height * 3 / 4, width * 3 / 5, height * 3 / 4);
	}

	@EditableProperty(name = "Channel")
	public FETPolarity getPolarity() {
		return polarity;
	}

	public void setPolarity(FETPolarity polarity) {
		this.polarity = polarity;
		// Invalidate body
		body = null;
	}
}
