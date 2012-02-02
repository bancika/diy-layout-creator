package org.diylc.core.measures;

import java.awt.Color;

import org.diylc.common.ResistorColorCode;

public class Resistance extends AbstractMeasure<ResistanceUnit> {

	private static final long serialVersionUID = 1L;

	private static final Color[] COLOR_DIGITS = new Color[] { Color.black, Color.decode("#8B4513"),
			Color.red, Color.orange, Color.yellow, Color.decode("#76EE00"), Color.blue,
			Color.decode("#91219E"), Color.lightGray, Color.white };
	private static final Color[] COLOR_MULTIPLIER = new Color[] { Color.lightGray.brighter(),
			Color.decode("#FFB90F"), Color.black, Color.decode("#8B4513"), Color.red, Color.orange,
			Color.yellow, Color.decode("#76EE00"), Color.blue };

	public Resistance() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Resistance(Double value, ResistanceUnit multiplier) {
		super(value, multiplier);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Resistance clone() throws CloneNotSupportedException {
		return new Resistance(value, unit);
	}

	public Color[] getColorCode(ResistorColorCode resistorColorCode) {
		Color[] bands;
		double base = getValue() * getUnit().getFactor();
		int multiplier = 0;
		while (base > (resistorColorCode == ResistorColorCode._4_BAND ? 99 : 999)) {
			multiplier += 1;
			base /= 10;
		}
		while (base < (resistorColorCode == ResistorColorCode._4_BAND ? 10 : 100)) {
			multiplier -= 1;
			base *= 10;
		}
		if (multiplier > 6 || multiplier < -2) {
			// Out of range.
			return new Color[] {};
		}
		switch (resistorColorCode) {
		case _4_BAND:
			bands = new Color[3];
			bands[0] = COLOR_DIGITS[(int) (base / 10)];
			bands[1] = COLOR_DIGITS[(int) (base % 10)];
			bands[2] = COLOR_MULTIPLIER[multiplier + 2];
			break;
		case _5_BAND:
			bands = new Color[4];
			bands[0] = COLOR_DIGITS[(int) (base / 100)];
			bands[1] = COLOR_DIGITS[(int) (base / 10 % 10)];
			bands[2] = COLOR_DIGITS[(int) (base % 10)];
			bands[3] = COLOR_MULTIPLIER[multiplier + 2];
			break;
		default:
			bands = new Color[] {};
		}
		return bands;
	}

	public static Resistance parseResistance(String value) {
		for (ResistanceUnit unit : ResistanceUnit.values()) {
			if (value.toLowerCase().endsWith(unit.toString().toLowerCase())) {
				value = value.substring(0, value.length() - unit.toString().length() - 1).trim();
				return new Resistance(Double.parseDouble(value), unit);
			}
		}
		throw new IllegalArgumentException("Could not parse resistance: " + value);
	}
}
