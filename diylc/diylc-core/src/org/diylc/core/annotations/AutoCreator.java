package org.diylc.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AutoCreator {
  public Class<? extends IAutoCreator> creatorClass();
}
