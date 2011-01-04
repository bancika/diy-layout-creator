package org.diylc.core;

import java.awt.Graphics2D;
import java.io.Serializable;

import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.ComponentName;
import org.diylc.core.annotations.ComponentValue;
import org.diylc.core.annotations.ControlPoint;
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
 * <li>Exactly one getter should be annotated with {@link ComponentName}.</li>
 * <li>Exactly one getter should be annotated with {@link ComponentValue}.</li>
 * <li>Control point getters need to be annotated with {@link ControlPoint}.</li>
 * <li>Getters for properties editable by users should be annotated with
 * {@link EditableProperty}.</li>
 * <li>Component configuration should be declared using
 * <code>public static</code> fields so they can be set through config file.</li>
 * </ul>
 * 
 * @author Branislav Stojkovic
 */
public interface IDIYComponent extends Serializable {

	/**
	 * Draws the component onto the {@link Graphics2D}.
	 * 
	 * @param g2d
	 * @param componentState
	 */
	void draw(Graphics2D g2d, ComponentState componentState);

	/**
	 * Draws icon representation of the component.
	 * 
	 * @param g2d
	 * @param width
	 * @param height
	 */
	void drawIcon(Graphics2D g2d, int width, int height);
}
