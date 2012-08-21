package org.diylc.core;

import java.awt.Graphics2D;
import java.awt.Point;
import java.io.Serializable;

import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

/**
 * Interface for component instance. Implementation classes of this interface
 * will be instantiated by the application when component is added to the
 * canvas. <br>
 * <br>
 * <b>Implementing classes should meet the following: </b>
 * <ul>
 * <li>Must have an empty constructor.</li>
 * <li>Class should be annotated with {@link ComponentDescriptor}.</li>
 * <li>Getters for properties editable by users should be annotated with
 * {@link EditableProperty}.</li>
 * <li>Component configuration should be stored int <code>public static</code>
 * fields so they can be set through config file.</li>
 * </ul>
 * 
 * @author Branislav Stojkovic
 * 
 * @param <T>
 *            type of component values, e.g. Resistance for resistors or String
 *            for transistors.
 */
public interface IDIYComponent<T> extends Serializable {

	public static final int CHASSIS = 1;
	public static final int BOARD = 2;
	public static final int TRACE = 3;
	public static final int COMPONENT = 4;
	public static final int TEXT = 5;

	/**
	 * @return component instance name.
	 */
	String getName();

	/**
	 * Updates component instance name.
	 * 
	 * @param name
	 */
	void setName(String name);

	/**
	 * @return component value.
	 */
	T getValue();

	/**
	 * Updates component value.
	 * 
	 * @param value
	 */
	void setValue(T value);

	/**
	 * @return number of control points for this component instance. May vary
	 *         between two instances of the same type, e.g. DIL IC with 8 and 16
	 *         pins will have 8 or 16 pins although they are of the same type.
	 */
	int getControlPointCount();

	/**
	 * @param index
	 * @return control point at the specified index.
	 */
	Point getControlPoint(int index);

	/**
	 * Updates the control point at the specified index.
	 * 
	 * @param point
	 * @param index
	 */
	void setControlPoint(Point point, int index);

	/**
	 * @param index
	 * @return true, if the specified control point may stick to control points
	 *         of other components.
	 */
	boolean isControlPointSticky(int index);

	/**
	 * @param index
	 * @return true, if the specified control point may overlap with other
	 *         control points <b>of the same component</b>. The other control
	 *         point must be able to overlap too.
	 */
	boolean canControlPointOverlap(int index);

	VisibilityPolicy getControlPointVisibilityPolicy(int index);

	/**
	 * Draws the component onto the {@link Graphics2D}.
	 * 
	 * @param g2d
	 * @param componentState
	 * @param outlineMode
	 * @param project
	 * @param drawingObserver
	 */
	void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
			IDrawingObserver drawingObserver);

	/**
	 * Draws icon representation of the component. This should not depend on
	 * component state, i.e. it should be treated as a static method.
	 * 
	 * @param g2d
	 * @param width
	 * @param height
	 */
	void drawIcon(Graphics2D g2d, int width, int height);

	/**
	 * Clones the component.
	 * 
	 * @return
	 */
	IDIYComponent<T> clone() throws CloneNotSupportedException;

	/**
	 * Checks if two components are equal.
	 * 
	 * @param other
	 * @return
	 */
	boolean equalsTo(IDIYComponent<?> other);
}
