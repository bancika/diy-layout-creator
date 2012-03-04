package org.diylc.components.semiconductors;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;

@ComponentDescriptor(name = "MOSFET Symbol", author = "Branislav Stojkovic", category = "Semiconductors", instanceNamePrefix = "Q", description = "BJT NPN and PNP transistor", stretchable = false, zOrder = IDIYComponent.COMPONENT)
public class MOSFETSymbol extends AbstractTransistorSymbol {

	private static final long serialVersionUID = 1L;

	public Shape[] getBody() {
		if (body == null) {
			body = new Shape[3];
			int x = controlPoints[0].x;
			int y = controlPoints[0].y;
			int pinSpacing = (int) PIN_SPACING.convertToPixels();

			GeneralPath polyline = new GeneralPath();

			polyline.moveTo(x + pinSpacing / 2, y - pinSpacing);
			polyline.lineTo(x + pinSpacing / 2, y + pinSpacing);
			polyline.moveTo(x + pinSpacing, y - pinSpacing);
			polyline.lineTo(x + pinSpacing, y + pinSpacing);
			polyline.moveTo(x + pinSpacing, y - pinSpacing);
			polyline.lineTo(x + pinSpacing * 2, y - pinSpacing);
			polyline.moveTo(x + pinSpacing, y + pinSpacing);
			polyline.lineTo(x + pinSpacing * 2, y + pinSpacing);
			body[0] = polyline;

			polyline = new GeneralPath();

			polyline.moveTo(x, y);
			polyline.lineTo(x + pinSpacing / 2, y);
			polyline.moveTo(x + pinSpacing * 2, y - pinSpacing * 2);
			polyline.lineTo(x + pinSpacing * 2, y - pinSpacing);
			polyline.moveTo(x + pinSpacing * 2, y + pinSpacing * 2);
			polyline.lineTo(x + pinSpacing * 2, y + pinSpacing);
			body[1] = polyline;

			Polygon arrow;
			if (polarity == TransistorPolarity.PNP) {
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
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
		g2d.drawLine(width / 5, height / 2, width / 2, height / 2);
		g2d.drawLine(width / 2, height / 4, width / 2, height * 3 / 4);
		g2d.drawLine(width * 3 / 5, height / 4, width * 3 / 5, height * 3 / 4);
		
		g2d.drawLine(width * 4 / 5, 1, width * 4 / 5, height / 4);
		g2d.drawLine(width * 4 / 5, height / 4, width * 3 / 5, height / 4);
		
		g2d.drawLine(width * 4 / 5, height - 1, width * 4 / 5, height * 3 / 4);
		g2d.drawLine(width * 4 / 5, height * 3 / 4, width * 3 / 5, height * 3 / 4);
	}
}
