package org.diylc.components.semiconductors;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import org.diylc.common.ObjectCache;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

@ComponentDescriptor(name = "JFET Symbol", author = "Branislav Stojkovic", category = "Semiconductors", instanceNamePrefix = "Q", description = "JFET transistor schematic symbol", stretchable = false, zOrder = IDIYComponent.COMPONENT)
public class JFETSymbol extends AbstractTransistorSymbol {

	private static final long serialVersionUID = 1L;

	protected FETPolarity polarity = FETPolarity.NEGATIVE;

	public Shape[] getBody() {
		if (body == null) {
			body = new Shape[3];
			int x = controlPoints[0].x;
			int y = controlPoints[0].y;
			int pinSpacing = (int) PIN_SPACING.convertToPixels();

			GeneralPath polyline = new GeneralPath();

			polyline.moveTo(x + pinSpacing, y - pinSpacing);
			polyline.lineTo(x + pinSpacing, y + pinSpacing);

			body[0] = polyline;

			polyline = new GeneralPath();

			polyline.moveTo(x, y);
			polyline.lineTo(x + pinSpacing, y);
			polyline.moveTo(x + pinSpacing, y - pinSpacing * 7 / 8);
			polyline.lineTo(x + pinSpacing * 2, y - pinSpacing * 7 / 8);
			polyline.lineTo(x + pinSpacing * 2, y - pinSpacing * 2);
			polyline.moveTo(x + pinSpacing, y + pinSpacing * 7 / 8);
			polyline.lineTo(x + pinSpacing * 2, y + pinSpacing * 7 / 8);
			polyline.lineTo(x + pinSpacing * 2, y + pinSpacing * 2);
			body[1] = polyline;

			Polygon arrow;
			if (polarity == FETPolarity.NEGATIVE) {
				arrow = new Polygon(new int[] { x + pinSpacing * 2 / 6, x + pinSpacing * 2 / 6,
						x + pinSpacing * 6 / 6 }, new int[] { y - pinSpacing / 5,
						y + pinSpacing / 5, y }, 3);
			} else {
				arrow = new Polygon(new int[] { x + pinSpacing / 6, x + pinSpacing * 5 / 6,
						x + pinSpacing * 5 / 6 }, new int[] { y, y + pinSpacing / 5,
						y - pinSpacing / 5 }, 3);
			}
			body[2] = arrow;
		}
		return body;
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setColor(COLOR);
		
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
		g2d.drawLine(width / 2, height / 5, width / 2, height * 4 / 5);		
		
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.drawLine(width / 5, height / 2, width / 2, height / 2);

		g2d.drawLine(width * 3 / 4, 1, width * 3 / 4, height / 4);
		g2d.drawLine(width / 2, height / 4, width * 3 / 4, height / 4);

		g2d.drawLine(width * 3 / 4, height - 1, width * 3 / 4, height * 3 / 4);
		g2d.drawLine(width / 2, height * 3 / 4, width * 3 / 4, height * 3 / 4);
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
