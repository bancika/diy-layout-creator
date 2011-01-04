package org.diylc.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.diylc.core.ComponentLayer;
import org.diylc.core.IDIYComponent;
import org.diylc.core.VisibilityPolicy;

/**
 * Annotation for {@link IDIYComponent} implementation. Describes component
 * properties.
 * 
 * @author Branislav Stojkovic
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentDescriptor {
	
	String name();
	
	String desciption();
	
	String category();
	
	String author();
	
	String instanceNamePrefix();
	
	ComponentLayer componentLayer() default ComponentLayer.COMPONENT;

	boolean stretchable() default true;
}
