package org.diylc.components.boards;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;

import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Eyelet Board", category = "Boards", author = "Branislav Stojkovic", zOrder = IDIYComponent.BOARD, instanceNamePrefix = "Board", description = "Perforated board with eyelets", bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME)
public class EyeletBoard extends AbstractBoard {

	private static final long serialVersionUID = 1L;

	public static Color BOARD_COLOR = Color.decode("#CCFFCC");
	public static Color BORDER_COLOR = BOARD_COLOR.darker();
	public static Color EYELET_COLOR = Color.decode("#C3E4ED");

	public static Size SPACING = new Size(0.5d, SizeUnit.in);
	public static Size EYELET_SIZE = new Size(0.2d, SizeUnit.in);
	public static Size HOLE_SIZE = new Size(0.1d, SizeUnit.in);

	// private Area copperArea;
	protected Size spacing = SPACING;
	protected Color eyeletColor = EYELET_COLOR;

	public EyeletBoard() {
		super();
		this.boardColor = BOARD_COLOR;
		this.borderColor = BORDER_COLOR;
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
			Project project, IDrawingObserver drawingObserver) {
		Shape clip = g2d.getClip();
		if (checkPointsClipped(clip) && !clip.contains(firstPoint.x, secondPoint.y)
				&& !clip.contains(secondPoint.x, firstPoint.y)) {
			return;
		}
		super.draw(g2d, componentState, outlineMode, project, drawingObserver);
		if (componentState != ComponentState.DRAGGING) {
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha
						/ MAX_ALPHA));
			}
			Point p = new Point(firstPoint);
			int diameter = getClosestOdd((int) EYELET_SIZE.convertToPixels());
			int holeDiameter = getClosestOdd((int) HOLE_SIZE.convertToPixels());
			int spacing = (int) this.spacing.convertToPixels();

			while (p.y < secondPoint.y - spacing) {
				p.x = firstPoint.x;
				p.y += spacing;
				while (p.x < secondPoint.x - spacing - diameter) {
					p.x += spacing;
					g2d.setColor(eyeletColor);
					g2d.fillOval(p.x - diameter / 2, p.y - diameter / 2, diameter, diameter);
					g2d.setColor(eyeletColor.darker());
					g2d.drawOval(p.x - diameter / 2, p.y - diameter / 2, diameter, diameter);
					g2d.setColor(Constants.CANVAS_COLOR);
					g2d.fillOval(p.x - holeDiameter / 2, p.y - holeDiameter / 2, holeDiameter,
							holeDiameter);
					g2d.setColor(eyeletColor.darker());
					g2d.drawOval(p.x - holeDiameter / 2, p.y - holeDiameter / 2, holeDiameter,
							holeDiameter);
				}
			}
			super.drawCoordinates(g2d, spacing);
		}
	}

	@EditableProperty(name = "Eyelet color")
	public Color getEyeletColor() {
		return eyeletColor;
	}

	public void setEyeletColor(Color padColor) {
		this.eyeletColor = padColor;
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
		g2d.setColor(EYELET_COLOR);
		g2d.fillOval(width / 4, width / 4, width / 2, width / 2);
		g2d.setColor(EYELET_COLOR.darker());
		g2d.drawOval(width / 4, width / 4, width / 2, width / 2);
		g2d.setColor(Constants.CANVAS_COLOR);
		g2d.fillOval(width / 2 - 2, width / 2 - 2, 5, 5);
		g2d.setColor(EYELET_COLOR.darker());
		g2d.drawOval(width / 2 - 2, width / 2 - 2, 5, 5);
	}
}
