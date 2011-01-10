package org.diylc.components.boards;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;

import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Vero Board", category = "Boards", author = "Branislav Stojkovic", zOrder = IDIYComponent.BOARD, instanceNamePrefix = "B", description = "Perforated FR4 board with copper strips connecting all holes in a row")
public class VeroBoard extends AbstractBoard {

	private static final long serialVersionUID = 1L;

	public static Color BOARD_COLOR = Color.decode("#BCED91");
	public static Color COPPER_COLOR = Color.decode("#B87333");
	public static Color BORDER_COLOR = BOARD_COLOR.darker();

	private static Size SPACING = new Size(0.1d, SizeUnit.in);
	private static Size STRIP_SIZE = new Size(0.085d, SizeUnit.in);
	private static Size HOLE_SIZE = new Size(0.8d, SizeUnit.mm);

	// private Area copperArea;
	private Size spacing = SPACING;

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

		if (componentState != ComponentState.DRAGGING) {
			Composite oldComposite = g2d.getComposite();
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
						/ MAX_ALPHA));
			}
			Point p = new Point(controlPoints[0]);
			int stripSize = STRIP_SIZE.convertToPixels() / 2 * 2;
			int holeRadius = HOLE_SIZE.convertToPixels() / 2;

			while (p.y < controlPoints[1].y - spacing.convertToPixels()) {
				p.x = controlPoints[0].x;
				p.y += spacing.convertToPixels();
				g2d.setColor(COPPER_COLOR);
				g2d.fillRect(p.x + SPACING.convertToPixels() / 2, p.y - stripSize / 2,
						controlPoints[1].x - spacing.convertToPixels() - p.x, stripSize);
				while (p.x < controlPoints[1].x - spacing.convertToPixels()) {
					p.x += spacing.convertToPixels();
					g2d.setColor(Constants.CANVAS_COLOR);
					g2d
							.fillOval(p.x - holeRadius, p.y - holeRadius, holeRadius * 2,
									holeRadius * 2);
				}
			}
			g2d.setComposite(oldComposite);
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
	
	@EditableProperty
	public Size getSpacing() {
		return spacing;
	}
	
	public void setSpacing(Size spacing) {
		this.spacing = spacing;
	}

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
		g2d.setColor(BOARD_COLOR);
		g2d.fillRect(2, 2, width - 4, height - 4);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRect(2, 2, width - 4, height - 4);
		g2d.setColor(COPPER_COLOR);
		g2d.fillRect(4, width / 4, width - 8, width / 2);
		g2d.setColor(Constants.CANVAS_COLOR);
		g2d.fillOval(width / 3 - 1, width / 2 - 1, 2, 2);
		g2d.fillOval(2 * width / 3 - 1, width / 2 - 1, 2, 2);
	}
}
