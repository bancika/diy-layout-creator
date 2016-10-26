package org.diylc.swing.plugins.cloud.view.browser;

/***
 * Generic interface for dynamic data provider.
 * 
 * @author Branislav Stojkovic
 *
 * @param <T>
 */
public interface ILazyProvider<T> {

  /**
   * @return true if there's potentially more data at the source
   */
  boolean hasMoreData();

  /**
   * call to request more data. New data batch should be sent asynchronously to this function call.
   */
  void requestMoreData();
}
