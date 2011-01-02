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

public class ResistorInstance implements IComponentInstance {

	private static final long serialVersionUID = 1L;

	private String testField = "Hello World";
	private Capacitance c = new Capacitance(100d, CapacitanceUnit.uF);
	private Resistance r = new Resistance(123d, ResistanceUnit.K);
	private Size s = new Size(1d, SizeUnit.cm);
	private Color color = Color.green;
	private Point leftTopCorner = new Point(0, 0);
	private Point otherCorner = new Point(100, 100);
	private String name = "something";

	public ResistorInstance(String testField, Capacitance c, Resistance r, Size s, Color color,
			Point leftTopCorner) {
		super();
		this.testField = testField;
		this.c = c;
		this.r = r;
		this.s = s;
		this.color = color;
		this.leftTopCorner = leftTopCorner;
	}

	public ResistorInstance() {
		super();
	}

	@BomName
	@EditableProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ControlPoint(visibilityPolicy = VisibilityPolicy.WHEN_SELECTED)
	public Point getLeftTopCorner() {
		return leftTopCorner;
	}

	public void setLeftTopCorner(Point leftTopCorner) {
		this.leftTopCorner = leftTopCorner;
	}

	@ControlPoint(visibilityPolicy = VisibilityPolicy.WHEN_SELECTED)
	public Point getOtherCorner() {
		return otherCorner;
	}

	public void setOtherCorner(Point otherCorner) {
		this.otherCorner = otherCorner;
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
		g2d.setStroke(new BasicStroke(2));
		if (componentState == ComponentState.DRAGGING) {
			g2d.setStroke(new BasicStroke(1));
		}
		g2d.drawLine(leftTopCorner.x, leftTopCorner.y, otherCorner.x, otherCorner.y);
		g2d.fillRect((leftTopCorner.x + otherCorner.x) / 2 - 10,
				(leftTopCorner.y + otherCorner.y) / 2 - 10, 20, 20);
	}

	@Override
	public ResistorInstance clone() throws CloneNotSupportedException {
		ResistorInstance newInstance = new ResistorInstance(testField, c.clone(), r.clone(), s
				.clone(), color, (Point) leftTopCorner.clone());
		return newInstance;
	}
}
