package com.diyfever.diylc.model.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to annotate editable component property. Editable property XYZ must have
 * both getter and setter named <code>getXYZ</code> and <code>setXYZ</code>
 * respectively and this annotation must be placed on the getter method.
 * 
 * @author Branislav Stojkovic
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EditableProperty {

	/**
	 * Returns property name. If left blank property name will be extracted from
	 * the getter name, e.g. getter <code>getXYZ</code> will induce property
	 * name <code>XYZ</code>.
	 * 
	 * @return
	 */
	String name() default "";

	/**
	 * If true, application may set default value for this property.
	 * 
	 * @return
	 */
	boolean defaultable() default false;
}
