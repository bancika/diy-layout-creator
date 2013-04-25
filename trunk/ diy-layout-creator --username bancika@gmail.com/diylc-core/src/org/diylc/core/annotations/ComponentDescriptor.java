package org.diylc.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.diylc.core.CreationMethod;
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
	 * @return method that should be used to create a component. If
	 *         <code>CreationMethod.POINT_BY_POINT</code> is used, user will
	 *         have to select ending points before the component is created.
	 */
	CreationMethod creationMethod() default CreationMethod.SINGLE_CLICK;

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
	 * @return true if the component may go beyond it's predefined layer. In
	 *         that case, <code>zOrder</code> is used as initial z order of the
	 *         component.
	 */
	boolean flexibleZOrder() default false;

	/**
	 * @return when false, moving one control point will cause all the others to
	 *         move together with it.
	 */
	boolean stretchable() default true;

	/**
	 * @return controls what should be shown the BOM
	 */
	BomPolicy bomPolicy() default BomPolicy.SHOW_ALL_NAMES;

	/**
	 * @return when true, component editor dialog should be shown in Auto-Edit
	 *         mode.
	 */
	boolean autoEdit() default true;
	
	/**
	 * @return true if component may be rotated, false otherwise
	 * @return
	 */
	boolean rotatable() default true;

}
