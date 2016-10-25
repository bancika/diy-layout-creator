package org.diylc.swing.plugins.cloud.view.browser;


public interface ILazyProvider<T> {

  boolean hasMore();

  void requestMore();
}
