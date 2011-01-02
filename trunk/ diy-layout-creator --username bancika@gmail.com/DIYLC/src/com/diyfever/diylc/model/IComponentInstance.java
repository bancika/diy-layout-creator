package com.diyfever.diylc.model;

import java.awt.Graphics2D;
import java.io.Serializable;

/**
 * Interface for component instance. Implementation classes of this interface
 * will be instantiated by the application when component is added to the
 * canvas. <br>
 * <br>
 * <b>Note: </b>Implementing classes must have an empty constructor.
 * 
 * @author Branislav Stojkovic
 * 
 * @see IComponentType
 */
public interface IComponentInstance extends Serializable {

	/**
	 * Draws the component onto the {@link Graphics2D}.
	 * 
	 * @param g2d
	 * @param componentState
	 */
	void draw(Graphics2D g2d, ComponentState componentState);

	// IComponentInstance clone() throws CloneNotSupportedException;
}
