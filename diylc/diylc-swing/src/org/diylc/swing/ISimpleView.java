package org.diylc.swing;

import javax.swing.JFrame;

import org.diylc.common.ITask;

/**
 * Interface for secondary windows that support basic functionalities.
 * 
 * @author Branislav Stojkovic
 */
public interface ISimpleView {

  /**
   * Runs a task in background while showing busy cursor and a glass pane.
   * 
   * @param task
   */
  <T extends Object> void executeBackgroundTask(ITask<T> task);

  void showMessage(String message, String title, int messageType);

  int showConfirmDialog(String message, String title, int optionType, int messageType);

  /**
   * @return {@link JFrame} that can be used to reference secondary dialogs and frames
   */
  JFrame getOwnerFrame();
}
