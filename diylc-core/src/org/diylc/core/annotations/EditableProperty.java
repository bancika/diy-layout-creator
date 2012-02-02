package org.diylc.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.diylc.core.AllowAllValidator;
import org.diylc.core.IPropertyValidator;

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
	 * name <code>XYZ</code>. <br>
	 * Default value is blank.
	 * 
	 * @return
	 */
	String name() default "";

	/**
	 * If true, application may set default value for this property. <br>
	 * Default value is true.
	 * 
	 * @return
	 */
	boolean defaultable() default true;
	
	/**
	 * Class of validator to use to 
	 * @return
	 */
	Class<? extends IPropertyValidator> validatorClass() default AllowAllValidator.class;
}
