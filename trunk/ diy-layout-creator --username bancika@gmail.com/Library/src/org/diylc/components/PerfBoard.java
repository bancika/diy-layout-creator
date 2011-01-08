package org.diylc.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import org.diylc.core.ComponentLayer;
import org.diylc.core.ComponentState;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Perf Board w/ Pads", category = "Boards", author = "Branislav Stojkovic", componentLayer = ComponentLayer.BOARD, instanceNamePrefix = "B", desciption = "Perforated FR4 board with solder pads spaced 0.1 inch apart")
public class PerfBoard extends AbstractBoard {

	private static final long serialVersionUID = 1L;

	public static Color BOARD_COLOR = Color.decode("#BCED91");
	public static Color COPPER_COLOR = Color.decode("#B87333");
	public static Color BORDER_COLOR = BOARD_COLOR.darker();

	private static Size SPACING = new Size(0.1d, SizeUnit.in);
	private static Size PAD_SIZE = new Size(0.08d, SizeUnit.in);
	private static Size HOLE_SIZE = new Size(0.8d, SizeUnit.mm);

//	private Area copperArea;

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project) {
		// long t = System.nanoTime();
		// Area copperArea = getCopperArea();
		// System.err.println("Calc: " + (System.nanoTime() - t));
		// t = System.nanoTime();
		// super.draw(g2d, componentState, project);
		// g2d.setColor(COPPER_COLOR);
		// g2d.fill(copperArea);
		// System.err.println("   Draw: " + (System.nanoTime() - t));

		// long t = System.nanoTime();
		super.draw(g2d, componentState, project);
		Point p = new Point(controlPoints[0]);
		int radius = PAD_SIZE.convertToPixels() / 2;
		int holeRadius = HOLE_SIZE.convertToPixels() / 2;
		g2d.setStroke(new BasicStroke(radius));

		while (p.y < controlPoints[1].y - SPACING.convertToPixels()) {
			p.x = controlPoints[0].x;
			p.y += SPACING.convertToPixels();
			while (p.x < controlPoints[1].x - SPACING.convertToPixels()) {
				p.x += SPACING.convertToPixels();
				g2d.setColor(COPPER_COLOR);
				g2d.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
				g2d.setColor(Constants.CANVAS_COLOR);
				g2d.fillOval(p.x - holeRadius, p.y - holeRadius, holeRadius * 2, holeRadius * 2);
			}
		}
		// System.err.println("OldDraw: " + (System.nanoTime() - t));
	}

	// @Override
	// public void setControlPoint(Point point, int index) {
	// // Reset copper area if a control point is changed.
	// if (!point.equals(getControlPoint(index))) {
	// copperArea = null;
	// }
	// super.setControlPoint(point, index);
	// }
	//
	// public Area getCopperArea() {
	// if (copperArea == null) {
	// copperArea = new Area();
	//
	// int holeRadius = HOLE_SIZE.convertToPixels() / 2;
	// int radius = PAD_SIZE.convertToPixels() / 2;
	// int spacing = SPACING.convertToPixels();
	//
	// // Create a pad.
	// Area pad = new Area(new Ellipse2D.Double(-radius, -radius, radius * 2,
	// radius * 2));
	// pad.subtract(new Area(new Ellipse2D.Double(-holeRadius, -holeRadius,
	// holeRadius * 2,
	// holeRadius * 2)));
	//
	// // Create a row duplicating the pad.
	// Area row = new Area();
	// int columnCount = (controlPoints[1].x - controlPoints[0].x) / spacing -
	// 1;
	// int rowCount = (controlPoints[1].y - controlPoints[0].y) / spacing - 1;
	//
	// AffineTransform xTransform =
	// AffineTransform.getTranslateInstance(spacing, 0);
	// for (int i = 0; i < columnCount; i++) {
	// pad.transform(xTransform);
	// row.add(pad);
	// }
	//
	// // Create the whole matrix duplicating the row.
	// AffineTransform yTransform = AffineTransform.getTranslateInstance(0,
	// spacing);
	// for (int i = 0; i < rowCount; i++) {
	// row.transform(yTransform);
	// copperArea.add(row);
	// }
	//
	// copperArea.transform(AffineTransform.getTranslateInstance(controlPoints[0].x,
	// controlPoints[0].y));
	// }
	// return copperArea;
	// }

	@Override
	protected Color getBoardColor() {
		return BOARD_COLOR;
	}

	@Override
	protected Color getBorderColor() {
		return BORDER_COLOR;
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {

	}
}
