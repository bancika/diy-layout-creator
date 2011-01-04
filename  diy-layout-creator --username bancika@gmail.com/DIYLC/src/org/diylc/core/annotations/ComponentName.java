package org.diylc.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotates getter for the component name that will be shown in the BOM. Only
 * one getter per class may be tagged with this annotation and, must return
 * string type and there has to be a matching setter.
 * 
 * @author Branislav Stojkovic
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentName {

}
