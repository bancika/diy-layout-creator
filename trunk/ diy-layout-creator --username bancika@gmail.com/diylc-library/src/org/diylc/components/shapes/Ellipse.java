package org.diylc.components.shapes;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

import org.diylc.common.ObjectCache;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;

@ComponentDescriptor(name = "Ellipse", author = "Branislav Stojkovic", category = "Shapes", instanceNamePrefix = "ELL", description = "Elliptical area", zOrder = IDIYComponent.COMPONENT, flexibleZOrder = true, bomPolicy = BomPolicy.SHOW_ALL_NAMES, autoEdit = false)
public class Ellipse extends AbstractShape {

	private static final long serialVersionUID = 1L;

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState,
			boolean outlineMode, Project project,
			IDrawingObserver drawingObserver) {
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke((int) borderThickness.convertToPixels()));

		if (componentState != ComponentState.DRAGGING) {
			Composite oldComposite = g2d.getComposite();
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
			}
			g2d.setColor(color);
			g2d.fillOval(firstPoint.x, firstPoint.y, secondPoint.x
					- firstPoint.x, secondPoint.y - firstPoint.y);
			g2d.setComposite(oldComposite);
		}
		// Do not track any changes that follow because the whole oval has been
		// tracked so far.
		drawingObserver.stopTracking();
		g2d.setColor(componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR
				: borderColor);
		g2d.drawOval(firstPoint.x, firstPoint.y, secondPoint.x - firstPoint.x,
				secondPoint.y - firstPoint.y);
	}	

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int factor = 32 / width;
		g2d.setColor(COLOR);
		g2d.fillOval(2 / factor, 2 / factor, width - 4 / factor, height - 4
				/ factor);
		g2d.setColor(BORDER_COLOR);
		g2d.drawOval(2 / factor, 2 / factor, width - 4 / factor, height - 4
				/ factor);
	}
}
