package org.diylc.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.diylc.core.ComponentLayer;
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
	 * Component type name.
	 * 
	 * @return
	 */
	String name();

	/**
	 * Component type description.
	 * 
	 * @return
	 */
	String desciption();

	/**
	 * Component category, e.g. "Passive" or "Semiconductors".
	 * 
	 * @return
	 */
	String category();

	/**
	 * Component author name.
	 * 
	 * @return
	 */
	String author();

	/**
	 * Prefix that will be used to generate component instance names, e.g. "R"
	 * for resistors or "Q" for transistors.
	 * 
	 * @return
	 */
	String instanceNamePrefix();

	/**
	 * Z-order of the component.
	 * 
	 * @return
	 */
	ComponentLayer componentLayer() default ComponentLayer.COMPONENT;

	/**
	 * When false, moving one control point will cause all the others to move
	 * together with it.
	 * 
	 * @return
	 */
	boolean stretchable() default true;
}
