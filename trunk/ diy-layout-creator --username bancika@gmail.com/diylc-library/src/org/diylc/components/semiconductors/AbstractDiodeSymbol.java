package org.diylc.components.semiconductors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;

import org.diylc.components.AbstractSchematicLeadedSymbol;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

public abstract class AbstractDiodeSymbol extends AbstractSchematicLeadedSymbol<String> {

	private static final long serialVersionUID = 1L;

	public static Size DEFAULT_SIZE = new Size(0.1, SizeUnit.in);	

	private String value = null;

	public AbstractDiodeSymbol() {
		super();
		this.bodyColor = COLOR;
		this.borderColor = null;
	}

	@EditableProperty(validatorClass = PositiveMeasureValidator.class)
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getValueForDisplay() {
		return getValue();
	}

	@Override
	protected Size getDefaultWidth() {
		return DEFAULT_SIZE;
	}

	@Override
	protected Size getDefaultLength() {
		return DEFAULT_SIZE;
	}

	@Override
	protected Shape getBodyShape() {
		double width = getWidth().convertToPixels();
		Polygon p = new Polygon(
				new int[] { 0, 0, (int) (width / Math.sqrt(2)) }, new int[] {
						0, (int) (width), (int) (width / 2) }, 3);
		// Area a = new Area(p);
		// int bandSize = (int) BAND_SIZE.convertToPixels();
		// a.add(new Area(new Rectangle2D.Double((int) (width / Math.sqrt(2)) +
		// 1,
		// 0, bandSize, (int) width)));
		return p;
	}

	@Override
	protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
	}

	@Deprecated
	@Override
	public Size getLength() {
		return super.getLength();
	}

	@Deprecated
	@Override
	public Color getBorderColor() {
		return super.getBorderColor();
	}

	@Override
	@EditableProperty(name = "Color")
	public Color getBodyColor() {
		return super.getBodyColor();
	}
}
