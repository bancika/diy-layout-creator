package org.diylc.components.boards;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Perf Board w/ Pads", category = "Boards", author = "Branislav Stojkovic", zOrder = IDIYComponent.BOARD, instanceNamePrefix = "Board", description = "Perforated board with solder pads")
public class PerfBoard extends AbstractBoard {

	private static final long serialVersionUID = 1L;

	public static Color COPPER_COLOR = Color.decode("#DA8A67");

	public static Size SPACING = new Size(0.1d, SizeUnit.in);
	public static Size PAD_SIZE = new Size(0.08d, SizeUnit.in);
	public static Size HOLE_SIZE = new Size(0.7d, SizeUnit.mm);

	// private Area copperArea;
	protected Size spacing = SPACING;
	protected Color padColor = COPPER_COLOR;

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project,
			IDrawingObserver drawingObserver) {
		super.draw(g2d, componentState, project, drawingObserver);
		if (componentState != ComponentState.DRAGGING) {
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
						/ MAX_ALPHA));
			}
			Point p = new Point(firstPoint);
			int diameter = getClosestOdd(PAD_SIZE.convertToPixels());
			int holeDiameter = getClosestOdd(HOLE_SIZE.convertToPixels());
			int spacing = this.spacing.convertToPixels();

			while (p.y < secondPoint.y - spacing) {
				p.x = firstPoint.x;
				p.y += spacing;
				while (p.x < secondPoint.x - spacing) {
					p.x += spacing;
					g2d.setColor(padColor);
					g2d.fillOval(p.x - diameter / 2, p.y - diameter / 2, diameter, diameter);
					g2d.setColor(padColor.darker());
					g2d.drawOval(p.x - diameter / 2, p.y - diameter / 2, diameter, diameter);
					g2d.setColor(Constants.CANVAS_COLOR);
					g2d.fillOval(p.x - holeDiameter / 2, p.y - holeDiameter / 2, holeDiameter,
							holeDiameter);
					g2d.setColor(padColor.darker());
					g2d.drawOval(p.x - holeDiameter / 2, p.y - holeDiameter / 2, holeDiameter,
							holeDiameter);
				}
			}
		}
	}

	@EditableProperty(name = "Pad color")
	public Color getPadColor() {
		return padColor;
	}

	public void setPadColor(Color padColor) {
		this.padColor = padColor;
	}

	@EditableProperty
	public Size getSpacing() {
		return spacing;
	}

	public void setSpacing(Size spacing) {
		this.spacing = spacing;
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setColor(BOARD_COLOR);
		g2d.fillRect(2, 2, width - 4, height - 4);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRect(2, 2, width - 4, height - 4);
		g2d.setColor(COPPER_COLOR);
		g2d.fillOval(width / 4, width / 4, width / 2, width / 2);
		g2d.setColor(COPPER_COLOR.darker());
		g2d.drawOval(width / 4, width / 4, width / 2, width / 2);
		g2d.setColor(Constants.CANVAS_COLOR);
		g2d.fillOval(width / 2 - 2, width / 2 - 2, 5, 5);
		g2d.setColor(COPPER_COLOR.darker());
		g2d.drawOval(width / 2 - 2, width / 2 - 2, 5, 5);
	}
}
