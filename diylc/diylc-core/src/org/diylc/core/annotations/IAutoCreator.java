package org.diylc.core.annotations;

import java.util.List;

import org.diylc.core.IDIYComponent;

/**
 * Interface for creating components automatically each time a component is created by the user.
 * 
 * @author Branislav Stojkovic
 */
public interface IAutoCreator {

  /**
   * @param lastAdded the component created by the user
   * @return {@link List} of {@link IDIYComponent}s that will be automatically created as a result.
   *         Empty {@link List} or {@code null} are valid responses for cases when no components
   *         should be auto-created.
   */
  public List<IDIYComponent<?>> createIfNeeded(IDIYComponent<?> lastAdded);
}
