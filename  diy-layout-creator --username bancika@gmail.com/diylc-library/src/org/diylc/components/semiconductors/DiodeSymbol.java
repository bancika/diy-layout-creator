package org.diylc.components.semiconductors;

import java.awt.Graphics2D;
import java.awt.Polygon;

import org.diylc.common.ObjectCache;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Diode (schematic symbol)", author = "Branislav Stojkovic", category = "Semiconductors", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "D", description = "Diode schematic symbol", zOrder = IDIYComponent.COMPONENT)
public class DiodeSymbol extends AbstractDiodeSymbol {

	private static final long serialVersionUID = 1L;

	public static Size BAND_SIZE = new Size(0.01, SizeUnit.in);

	public void drawIcon(Graphics2D g2d, int width, int height) {
		int size = width * 3 / 8;
		int bandSize = 1;
		g2d.rotate(-Math.PI / 4, width / 2, height / 2);
		g2d.setColor(LEAD_COLOR);
		g2d.drawLine(0, height / 2, (width - size) / 2, height / 2);
		g2d.drawLine((int) (width + size / Math.sqrt(2) + bandSize) / 2,
				height / 2, width, height / 2);
		g2d.setColor(COLOR);
		g2d.fill(new Polygon(new int[] { (width - size) / 2,
				(width - size) / 2,
				(int) ((width - size) / 2 + size / Math.sqrt(2)) }, new int[] {
				(height - size) / 2, (height + size) / 2, height / 2 }, 3));
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(bandSize));
		g2d.drawLine((int) ((width - size) / 2 + size / Math.sqrt(2)),
				(height - size) / 2, (int) ((width - size) / 2 + size
						/ Math.sqrt(2)), (height + size) / 2);
	}

	@Override
	protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
		double width = getWidth().convertToPixels();
		int bandSize = (int) BAND_SIZE.convertToPixels();
		g2d.setColor(getBodyColor());
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(bandSize));
		g2d.drawLine((int) (width / Math.sqrt(2)) + bandSize, 0, (int) (width
				/ Math.sqrt(2) + bandSize), (int) width);
	}
}
