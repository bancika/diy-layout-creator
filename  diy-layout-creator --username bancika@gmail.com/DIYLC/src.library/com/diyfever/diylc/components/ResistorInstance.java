package com.diyfever.diylc.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import com.diyfever.diylc.model.ComponentState;
import com.diyfever.diylc.model.IComponentInstance;
import com.diyfever.diylc.model.VisibilityPolicy;
import com.diyfever.diylc.model.annotations.BomName;
import com.diyfever.diylc.model.annotations.BomValue;
import com.diyfever.diylc.model.annotations.ControlPoint;
import com.diyfever.diylc.model.annotations.EditableProperty;
import com.diyfever.diylc.model.measures.Capacitance;
import com.diyfever.diylc.model.measures.CapacitanceUnit;
import com.diyfever.diylc.model.measures.Resistance;
import com.diyfever.diylc.model.measures.ResistanceUnit;
import com.diyfever.diylc.model.measures.Size;
import com.diyfever.diylc.model.measures.SizeUnit;
import com.diyfever.diylc.utils.Constants;

public class ResistorInstance extends AbstractLeadedComponentInstance {

	private static final long serialVersionUID = 1L;

	private String testField = "Hello World";
	private Capacitance c = new Capacitance(100d, CapacitanceUnit.uF);
	private Resistance r = new Resistance(123d, ResistanceUnit.K);
	private Size s = new Size(1d, SizeUnit.cm);
	private Color color = Color.green;

	public ResistorInstance() {
		super();
	}

	@EditableProperty(name = "Test Field")
	public String getTestField() {
		return testField;
	}

	public void setTestField(String testField) {
		this.testField = testField;
	}

	@BomValue
	@EditableProperty(defaultable = true)
	public Capacitance getC() {
		return c;
	}

	public void setC(Capacitance c) {
		this.c = c;
	}

	@EditableProperty(defaultable = true)
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@EditableProperty
	public Resistance getR() {
		return r;
	}

	// Setter is commented out, ClassProcessor should ignore this property.
	// public void setR(Resistance r) {
	// this.r = r;
	// }

	// No annotation, so this property should be ignored.
	public Size getS() {
		return s;
	}

	public void setS(Size s) {
		this.s = s;
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
