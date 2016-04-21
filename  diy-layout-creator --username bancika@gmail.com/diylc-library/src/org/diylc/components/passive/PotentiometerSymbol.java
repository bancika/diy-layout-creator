package org.diylc.components.passive;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.components.semiconductors.AbstractTransistorSymbol;

@ComponentDescriptor(name = "Trimmer Symbol", author = "MCbx", category = "Schematics", instanceNamePrefix = " VR", description = "Trimmer (potentiometer) symbol", stretchable = false, zOrder = IDIYComponent.COMPONENT, rotatable = true)
public class PotentiometerSymbol extends AbstractTransistorSymbol {

	private static final long serialVersionUID = 1L;

	public Shape[] getBody() {
		if (body == null) {
			body = new Shape[3];
			int x = controlPoints[0].x;
			int y = controlPoints[0].y;
			int pinSpacing = (int) PIN_SPACING.convertToPixels();

			GeneralPath polyline = new GeneralPath();
			polyline.moveTo(x + pinSpacing * 2, y - pinSpacing * 2);
			polyline.lineTo(x + pinSpacing * 2, y - pinSpacing * 2 + pinSpacing / 2);
			polyline.lineTo(x + pinSpacing * 2 - pinSpacing / 4,
					(y - pinSpacing * 2) + pinSpacing / 2 + pinSpacing / 4);
			polyline.lineTo(x + pinSpacing * 2 + pinSpacing / 4, (y - pinSpacing * 2) + 2 * pinSpacing / 2);
			polyline.lineTo(x + pinSpacing * 2 - pinSpacing / 4,
					(y - pinSpacing * 2) + 3 * pinSpacing / 2 - pinSpacing / 8);
			polyline.lineTo(x + pinSpacing * 2 + pinSpacing / 4,
					(y - pinSpacing * 2) + 4 * pinSpacing / 2 - pinSpacing / 4);
			polyline.lineTo(x + pinSpacing * 2 - pinSpacing / 4,
					(y - pinSpacing * 2) + 5 * pinSpacing / 2 - pinSpacing / 3 - pinSpacing / 16);
			polyline.lineTo(x + pinSpacing * 2 + pinSpacing / 4,
					(y - pinSpacing * 2) + 6 * pinSpacing / 2 - pinSpacing / 2 + pinSpacing / 16);
			polyline.lineTo(x + pinSpacing * 2 - pinSpacing / 4,
					(y - pinSpacing * 2) + 7 * pinSpacing / 2 - pinSpacing / 2 - pinSpacing / 16);
			polyline.lineTo(x + pinSpacing * 2 + pinSpacing / 4,
					(y - pinSpacing * 2) + 7 * pinSpacing / 2 - pinSpacing / 2 + pinSpacing / 4);
			polyline.lineTo(x + pinSpacing * 2, (y - pinSpacing * 2) + 7 * pinSpacing / 2);
			polyline.lineTo(x + pinSpacing * 2, y + pinSpacing * 2);
			polyline.moveTo(x, y);
			polyline.lineTo(x + pinSpacing * 2 - pinSpacing / 4, y);
			polyline.lineTo(x + pinSpacing * 2 - 2 * pinSpacing / 3, y - pinSpacing / 2);
			polyline.moveTo(x + pinSpacing * 2 - pinSpacing / 4, y);
			polyline.lineTo(x + pinSpacing * 2 - 2 * pinSpacing / 3, y + pinSpacing / 2);

			body[0] = polyline;
			polyline = new GeneralPath();

			body[1] = polyline;
			body[2] = polyline;
		}
		return body;
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.rotate(-Math.PI / 4, width / 2, height / 2);
		g2d.setColor(COLOR);
		g2d.drawLine(0, height / 2, 4, height / 2);
		g2d.drawLine(width - 4, height / 2, width, height / 2);
		g2d.drawPolyline(new int[] { 4, 6, 10, 14, 18, 22, 26, 28 }, new int[] { height / 2, height / 2 + 2,
				height / 2 - 2, height / 2 + 2, height / 2 - 2, height / 2 + 2, height / 2 - 2, height / 2 }, 8);
		g2d.drawPolyline(new int[] { width / 2, width / 2 - 2, width / 2 - 4, width / 2 + 2, width / 2 - 2 },
				new int[] { height, height / 2 + 2, height / 2 + 6, height / 2 + 6, height / 2 + 2 }, 5);
	}

}