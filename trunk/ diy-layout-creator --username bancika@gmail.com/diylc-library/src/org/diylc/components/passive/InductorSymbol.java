package org.diylc.components.passive;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import org.diylc.components.AbstractSchematicLeadedSymbol;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Current;
import org.diylc.core.measures.Inductance;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Inductor (schematic symbol)", author = "Branislav Stojkovic", category = "Schematics", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "L", description = "Inductor schematic symbol", zOrder = IDIYComponent.COMPONENT)
public class InductorSymbol extends AbstractSchematicLeadedSymbol<Inductance> {

	private static final long serialVersionUID = 1L;

	public static Size DEFAULT_LENGTH = new Size(0.3, SizeUnit.in);
	public static Size DEFAULT_WIDTH = new Size(0.08, SizeUnit.in);

	private Inductance value = null;
	private Current current = null;
	private Resistance resistance = null;
	private boolean core = false;

	@EditableProperty(validatorClass = PositiveMeasureValidator.class)
	public Inductance getValue() {
		return value;
	}

	public void setValue(Inductance value) {
		this.value = value;
	}

	@EditableProperty
	public Current getCurrent() {
		return current;
	}

	public void setCurrent(Current current) {
		this.current = current;
	}

	@EditableProperty
	public Resistance getResistance() {
		return resistance;
	}

	public void setResistance(Resistance resistance) {
		this.resistance = resistance;
	}

	@Override
	public String getValueForDisplay() {
		return getValue().toString() + (getCurrent() == null ? "" : " " + getCurrent().toString());
	}

	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.rotate(-Math.PI / 4, width / 2, height / 2);
		g2d.setColor(LEAD_COLOR);
		g2d.drawLine(0, height / 2, width / 8, height / 2);
		g2d.drawLine(width * 7 / 8, height / 2, width, height / 2);
		g2d.setColor(COLOR);

		GeneralPath polyline = new GeneralPath();
		polyline.moveTo(width / 8, height / 2);
		polyline.curveTo(width / 8, height / 4, width * 3 / 8, height / 4,
				width * 3 / 8, height / 2);
		polyline.curveTo(width * 3 / 8, height / 4, width * 5 / 8, height / 4,
				width * 5 / 8, height / 2);
		polyline.curveTo(width * 5 / 8, height / 4, width * 7 / 8, height / 4,
				width * 7 / 8, height / 2);

		polyline.moveTo(width / 8, height * 6 / 10);
		polyline.lineTo(width * 7 / 8, height * 6 / 10);
		polyline.moveTo(width / 8, height * 7 / 10);
		polyline.lineTo(width * 7 / 8, height * 7 / 10);
		g2d.draw(polyline);
	}

	@Override
	protected Size getDefaultWidth() {
		return DEFAULT_WIDTH;
	}

	@Override
	protected Size getDefaultLength() {
		return DEFAULT_LENGTH;
	}

	@Override
	protected Shape getBodyShape() {
		GeneralPath polyline = new GeneralPath();
		double length = getLength().convertToPixels();
		double width = getWidth().convertToPixels();
		double d = length / 10;
		polyline.moveTo(0, width / 2);
		polyline.curveTo(0, 0, 2 * d, 0, 2 * d, width / 2);
		polyline.curveTo(2 * d, 0, 4 * d, 0, 4 * d, width / 2);
		polyline.curveTo(4 * d, 0, 6 * d, 0, 6 * d, width / 2);
		polyline.curveTo(6 * d, 0, 8 * d, 0, 8 * d, width / 2);
		polyline.curveTo(8 * d, 0, 10 * d, 0, 10 * d, width / 2);
		if (core) {
			polyline.moveTo(0, width * 3 / 4);
			polyline.lineTo(length, width * 3 / 4);
			polyline.moveTo(0, width * 7 / 8);
			polyline.lineTo(length, width * 7 / 8);
		}
		return polyline;
	}

	@Override
	protected boolean useShapeRectAsPosition() {
		return false;
	}

	@EditableProperty
	public boolean getCore() {
		return core;
	}

	public void setCore(boolean core) {
		this.core = core;
	}
}
