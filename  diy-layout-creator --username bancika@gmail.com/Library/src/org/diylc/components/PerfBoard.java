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

@ComponentDescriptor(name = "Perf Board", category = "Boards", author = "Branislav Stojkovic", componentLayer = ComponentLayer.BOARD, instanceNamePrefix = "B", desciption = "Perforated FR4 board with solder pads spaced 0.1 inch apart")
public class PerfBoard extends AbstractBoard {

	private static final long serialVersionUID = 1L;

	public static Color BOARD_COLOR = Color.decode("#BCED91");
	public static Color COPPER_COLOR = Color.decode("#B87333");
	public static Color BORDER_COLOR = BOARD_COLOR.darker();

	private static Size SPACING = new Size(0.1d, SizeUnit.in);
	private static Size PAD_SIZE = new Size(0.06d, SizeUnit.in);

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project) {
		super.draw(g2d, componentState, project);
		Point p = new Point(controlPoints[0]);
		int radius = PAD_SIZE.convertToPixels() / 2;
		g2d.setStroke(new BasicStroke(radius));
		g2d.setColor(COPPER_COLOR);
		while (p.y < controlPoints[1].y - SPACING.convertToPixels()) {
			p.x = controlPoints[0].x;
			p.y += SPACING.convertToPixels();
			while (p.x < controlPoints[1].x - SPACING.convertToPixels()) {
				p.x += SPACING.convertToPixels();
				g2d.drawOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
			}
		}
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

	}
}
