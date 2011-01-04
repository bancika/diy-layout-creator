package org.diylc.core;

import java.awt.Graphics2D;
import java.io.Serializable;

import org.diylc.presenter.ComponentType;

/**
 * Interface for component instance. Implementation classes of this interface
 * will be instantiated by the application when component is added to the
 * canvas. <br>
 * <br>
 * <b>Note: </b>Implementing classes must have an empty constructor.
 * 
 * @author Branislav Stojkovic
 * 
 * @see ComponentType
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
