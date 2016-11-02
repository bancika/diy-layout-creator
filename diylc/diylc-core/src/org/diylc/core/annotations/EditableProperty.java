package org.diylc.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.diylc.core.AllowAllValidator;
import org.diylc.core.IPropertyValidator;

/**
 * Used to annotate editable component property. Editable property XYZ must have both getter and
 * setter named <code>getXYZ</code> and <code>setXYZ</code> respectively and this annotation must be
 * placed on the getter method. <br>
 * <br>
 * In the example below, {@code category} is the property we want to edit and
 * {@code getCategories()} provides a list of available values for category.
 * 
 * <pre>
 * <code>
 * &#64;EditableProperty(additionalOptions = EditableProperty.DYNAMIC_LIST
 *   + "getCategories")
 * public String getCategory() {
 *   return category;
 * }
 * 
 * public String[] getCategories() {
 *   return categories;
 * }
 * </code>
 * </pre>
 * 
 * @author Branislav Stojkovic
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EditableProperty {

  /**
   * Used to designate that a {@link String} property can have multi-lined text.
   */
  public static final String MULTI_LINE = "MULTI-LINE";
  /**
   * Used when property can have one of the values from a dynamic list. In such cases,
   * {@link EditableProperty#additionalOptions()} should begin with
   * {@link EditableProperty#DYNAMIC_LIST} and contain a method name from the parent object that
   * should be called to obtain the {@link String} array with possible options.
   */
  public static final String DYNAMIC_LIST = "DYNAMIC_LIST:";

  /**
   * Returns property name. If left blank property name will be extracted from the getter name, e.g.
   * getter <code>getXYZ</code> will induce property name <code>XYZ</code>. <br>
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
   * 
   * @return
   */
  Class<? extends IPropertyValidator> validatorClass() default AllowAllValidator.class;

  /**
   * Optional parameter in case we want to force an order of properties.
   * 
   * @return
   */
  int sortOrder() default Integer.MAX_VALUE;

  /**
   * Optional parameter for properties that require additional configuration. This will be passed
   * down to the editor factory.
   * 
   * @return
   */
  String additionalOptions() default "";
}
