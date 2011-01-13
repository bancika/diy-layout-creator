package org.diylc.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.diylc.core.IDIYComponent;

/**
 * Annotation for {@link IDIYComponent} implementation. Describes component
 * properties.
 * 
 * @author Branislav Stojkovic
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentDescriptor {

	/**
	 * @return component type name.
	 */
	String name();

	/**
	 * @return component type description.
	 */
	String description();

	/**
	 * @return component category, e.g. "Passive", "Semiconductors", etc.
	 */
	String category();

	/**
	 * @return component author name.
	 */
	String author();

	/**
	 * @return prefix that will be used to generate component instance names,
	 *         e.g. "R" for resistors or "Q" for transistors.
	 */
	String instanceNamePrefix();

	/**
	 * @return Z-order of the component.
	 */
	double zOrder();

	/**
	 * @return when false, moving one control point will cause all the others to
	 *         move together with it.
	 */
	boolean stretchable() default true;

	/**
	 * @return true, if control points may stick to other components' control
	 *         points.
	 */
	boolean sticky() default false;
}
