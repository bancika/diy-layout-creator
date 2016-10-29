package org.diylc.core.annotations;

import java.util.List;

import org.diylc.core.IDIYComponent;

public interface IAutoCreator {

  public List<IDIYComponent<?>> createIfNeeded(IDIYComponent<?> lastAdded);
}
