package org.diylc.components.boards;

import java.awt.Color;
import java.awt.Graphics2D;

import org.diylc.core.ComponentLayer;
import org.diylc.core.annotations.ComponentDescriptor;

@ComponentDescriptor(name = "Blank Board", category = "Boards", author = "Branislav Stojkovic", componentLayer = ComponentLayer.BOARD, instanceNamePrefix = "B", desciption = "Blank circuit board")
public class BlankBoard extends AbstractBoard {

	private static final long serialVersionUID = 1L;

	public static Color BOARD_COLOR = Color.decode("#BCED91");
	public static Color BORDER_COLOR = BOARD_COLOR.darker();

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
	}
}
