package com.diyfever.diylc.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import org.diylc.model.ComponentState;
import org.diylc.model.annotations.EditableProperty;
import org.diylc.model.measures.Resistance;
import org.diylc.model.measures.ResistanceUnit;
import org.diylc.model.measures.Size;
import org.diylc.model.measures.SizeUnit;


public class ResistorInstance extends AbstractLeadedComponentInstance {

	private static final long serialVersionUID = 1L;

	private Resistance r = new Resistance(123d, ResistanceUnit.K);
	private Color color = Color.green;

	public ResistorInstance() {
		super();
	}

	@EditableProperty
	public Resistance getR() {
		return r;
	}

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState) {
		g2d.setColor(componentState.equals(ComponentState.SELECTED) ? color : color.darker());
		g2d.setStroke(new BasicStroke(1));
		if (componentState == ComponentState.DRAGGING) {
			g2d.setStroke(new BasicStroke(1));
		}
		g2d.drawLine(point1.x, point1.y, point2.x, point2.y);
		g2d.rotate(Math.PI / 4, (point1.x + point2.x) / 2, (point1.y + point2.y) / 2);
		g2d.fillRect((point1.x + point2.x) / 2 - 10, (point1.y + point2.y) / 2 - 10, 20, 20);
	}

	@Override
	protected Size getDefaultHeight() {
		return new Size(3d, SizeUnit.mm);
	}

	@Override
	protected Size getDefaultWidth() {
		return new Size(10d, SizeUnit.mm);
	}
}
