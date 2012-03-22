package org.diylc.components.boards;

import java.awt.Graphics2D;

import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;

@ComponentDescriptor(name = "Blank Board", category = "Boards", author = "Branislav Stojkovic", zOrder = IDIYComponent.BOARD, instanceNamePrefix = "Board", description = "Blank circuit board")
public class BlankBoard extends AbstractBoard {

	private static final long serialVersionUID = 1L;

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setColor(BOARD_COLOR);
		g2d.fillRect(2, 2, width - 4, height - 4);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRect(2, 2, width - 4, height - 4);
	}
	
	@Override
	public boolean getDrawCoordinates() {
		// Override to prevent editing.
		return super.getDrawCoordinates();
	}
}
