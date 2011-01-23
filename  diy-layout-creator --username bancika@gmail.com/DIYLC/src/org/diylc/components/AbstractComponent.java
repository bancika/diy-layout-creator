package org.diylc.components;

import java.awt.Color;
import java.awt.Font;

import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.EditableProperty;

/**
 * Abstract implementation of {@link IDIYComponent} that contains component name
 * and toString.
 * 
 * @author Branislav Stojkovic
 * 
 * @param <T>
 */
public abstract class AbstractComponent<T> implements IDIYComponent<T> {

	private static final long serialVersionUID = 1L;

	protected String name = "";
	
	public static Color SELECTION_COLOR = Color.red;
	public static Color LABEL_COLOR = Color.black;
	public static Color LABEL_COLOR_SELECTED = Color.red;
	public static Font LABEL_FONT = new Font("Tahoma", Font.PLAIN, 12);

	@EditableProperty(defaultable = false)
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Returns the closest odd number, i.e. x when x is odd, or x + 1 when x is
	 * even.
	 * 
	 * @param x
	 * @return
	 */
	protected int getClosestOdd(int x) {
		return (x / 2) * 2 + 1;
	}
}
