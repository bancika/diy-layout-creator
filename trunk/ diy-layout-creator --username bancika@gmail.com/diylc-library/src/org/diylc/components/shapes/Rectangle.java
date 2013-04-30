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
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Rectangle", author = "Branislav Stojkovic", category = "Shapes", instanceNamePrefix = "RECT", description = "Ractangular area, with or withouth rounded edges", zOrder = IDIYComponent.COMPONENT, flexibleZOrder = true, bomPolicy = BomPolicy.SHOW_ALL_NAMES, autoEdit = false)
public class Rectangle extends AbstractShape {

	private static final long serialVersionUID = 1L;

	protected Size edgeRadius = new Size(0d, SizeUnit.mm);

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState,
			boolean outlineMode, Project project,
			IDrawingObserver drawingObserver) {
		g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke((int) borderThickness.convertToPixels()));
		int radius = (int) edgeRadius.convertToPixels();
		if (componentState != ComponentState.DRAGGING) {
			Composite oldComposite = g2d.getComposite();
			if (alpha < MAX_ALPHA) {
				g2d.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
			}
			g2d.setColor(color);
			g2d.fillRoundRect(firstPoint.x, firstPoint.y, secondPoint.x
					- firstPoint.x, secondPoint.y - firstPoint.y, radius,
					radius);
			g2d.setComposite(oldComposite);
		}
		// Do not track any changes that follow because the whole rect has been
		// tracked so far.
		drawingObserver.stopTracking();
		g2d.setColor(componentState == ComponentState.SELECTED
				|| componentState == ComponentState.DRAGGING ? SELECTION_COLOR
				: borderColor);
		g2d.drawRoundRect(firstPoint.x, firstPoint.y, secondPoint.x
				- firstPoint.x, secondPoint.y - firstPoint.y, radius, radius);
	}

	@EditableProperty(name = "Radius")
	public Size getEdgeRadius() {
		return edgeRadius;
	}

	public void setEdgeRadius(Size edgeRadius) {
		this.edgeRadius = edgeRadius;
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		int factor = 32 / width;
		g2d.setColor(COLOR);
		g2d.fillRect(2 / factor, 2 / factor, width - 4 / factor, height - 4
				/ factor);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRect(2 / factor, 2 / factor, width - 4 / factor, height - 4
				/ factor);
	}
}
