package org.diylc.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.diylc.core.IDIYComponent;

/**
 * Annotation for {@link IDIYComponent} implementation. Tells the core which component should be auto-created as a solder pad.
 * 
 * @author Branislav Stojkovic
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoCreated {
  /**
   * @return auto-create type name.
   */
  String name();
}
