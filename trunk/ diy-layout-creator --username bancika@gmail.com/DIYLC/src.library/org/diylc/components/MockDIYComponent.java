package org.diylc.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentName;
import org.diylc.core.annotations.ComponentValue;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.ControlPoint;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.ResistanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Mock", author = "bancika", category = "Sample", instanceNamePrefix = "M", desciption = "test")
public class MockDIYComponent implements IDIYComponent {

	private static final long serialVersionUID = 1L;

	private String testField = "Hello World";
	private Capacitance c = new Capacitance(100d, CapacitanceUnit.uF);
	private Resistance r = new Resistance(123d, ResistanceUnit.K);
	private Size s = new Size(1d, SizeUnit.cm);
	private Color color = Color.green;
	private Point leftTopCorner = new Point(0, 0);
	private String name = "something";

	public MockDIYComponent(String testField, Capacitance c, Resistance r, Size s,
			Color color, Point leftTopCorner) {
		super();
		this.testField = testField;
		this.c = c;
		this.r = r;
		this.s = s;
		this.color = color;
		this.leftTopCorner = leftTopCorner;
	}

	public MockDIYComponent() {
		super();
	}

	@ComponentName
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

	@EditableProperty(name = "Test Field")
	public String getTestField() {
		return testField;
	}

	public void setTestField(String testField) {
		this.testField = testField;
	}

	@ComponentValue
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
		g2d.fillRect(leftTopCorner.x, leftTopCorner.y, 200, 50);
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.drawString("X", 10, 10);
	}
}
