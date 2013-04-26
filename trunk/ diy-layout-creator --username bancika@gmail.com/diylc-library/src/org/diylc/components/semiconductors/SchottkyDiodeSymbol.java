package org.diylc.components.semiconductors;

import java.awt.Graphics2D;
import java.awt.Polygon;

import org.diylc.common.ObjectCache;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Schottky diode (schematic symbol)", author = "Branislav Stojkovic", category = "Schematics", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "D", description = "Schottky diode schematic symbol", zOrder = IDIYComponent.COMPONENT)
public class SchottkyDiodeSymbol extends AbstractDiodeSymbol {

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
		int finSize = 2 * width / 32;
		g2d.drawLine((int) ((width - size) / 2 + size / Math.sqrt(2)),
				(height - size) / 2, (int) ((width - size) / 2 + size
						/ Math.sqrt(2) + finSize), (height - size) / 2);
		g2d.drawLine(
				(int) ((width - size) / 2 + size / Math.sqrt(2) + finSize),
				(height - size) / 2, (int) ((width - size) / 2 + size
						/ Math.sqrt(2) + finSize), (height - size) / 2
						+ finSize);

		g2d.drawLine((int) ((width - size) / 2 + size / Math.sqrt(2)),
				(height + size) / 2, (int) ((width - size) / 2 + size
						/ Math.sqrt(2) - finSize), (height + size) / 2);
		g2d.drawLine(
				(int) ((width - size) / 2 + size / Math.sqrt(2) - finSize),
				(height + size) / 2, (int) ((width - size) / 2 + size
						/ Math.sqrt(2) - finSize), (height + size) / 2
						- finSize);
	}

	@Override
	protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
		double width = getWidth().convertToPixels();
		int finSize = (int) (width / 6);
		int bandSize = (int) BAND_SIZE.convertToPixels();
		g2d.setColor(getBodyColor());
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(bandSize));
		g2d.drawPolyline(new int[] {
				(int) (width / Math.sqrt(2) + bandSize) + finSize + 1,
				(int) (width / Math.sqrt(2) + bandSize) + finSize + 1,
				(int) (width / Math.sqrt(2) + bandSize),
				(int) (width / Math.sqrt(2) + bandSize),
				(int) (width / Math.sqrt(2) + bandSize) - finSize - 1,
				(int) (width / Math.sqrt(2) + bandSize) - finSize - 1 },
				new int[] { (int) finSize, 0, 0, (int) width, (int) width,
						(int) (width - finSize) }, 6);
	}
}
