package org.diylc.core.annotations;

import java.awt.Point;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.diylc.core.VisibilityPolicy;


/**
 * Annotation for control points. This annotation may be added fields of type
 * {@link Point} only when both getters and setters exist. Annotation is added
 * always on the getter.
 * 
 * @author Branislav Stojkovic
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ControlPoint {

	/**
	 * Determines when should the control point be rendered on the screen.
	 * 
	 * @return
	 */
	VisibilityPolicy visibilityPolicy() default VisibilityPolicy.ALWAYS;

	/**
	 * If true, application will allow the user to edit the control point.
	 * 
	 * @return
	 */
	boolean editable() default true;

	/**
	 * If true, the control point may be joined with other components' control
	 * points.
	 * 
	 * @return
	 */
	boolean sticky() default true;

	// /**
	// * If true, the control point is the main control point for the component.
	// * There cannot be more than one control point for a single component.
	// *
	// * @return
	// */
	// boolean main() default false;
}
