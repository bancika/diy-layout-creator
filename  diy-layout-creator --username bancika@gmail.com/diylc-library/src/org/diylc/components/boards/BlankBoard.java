package org.diylc.components.boards;

import java.awt.Graphics2D;

import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;

@ComponentDescriptor(name = "Blank Board", category = "Boards", author = "Branislav Stojkovic", zOrder = IDIYComponent.BOARD, instanceNamePrefix = "Board", description = "Blank circuit board", bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, autoEdit = false)
public class BlankBoard extends AbstractBoard {

	private static final long serialVersionUID = 1L;

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int factor = 32 / width;
		g2d.setColor(BOARD_COLOR);
		g2d.fillRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);
	}
	
	@Override
	public boolean getDrawCoordinates() {
		// Override to prevent editing.
		return super.getDrawCoordinates();
	}
}
