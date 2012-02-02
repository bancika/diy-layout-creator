package org.diylc.common;

/**
 * Abstract layer between the app and platform worker thread implementation.
 * 
 * @author Branislav Stojkovic
 * 
 * @param <T>
 */
public interface ITask<T> {

	/**
	 * Runs in background thread.
	 * 
	 * @return
	 * @throws Exception
	 */
	T doInBackground() throws Exception;

	/**
	 * Called if background thread fails.
	 * 
	 * @param e
	 */
	void failed(Exception e);

	/**
	 * Called if background thread is executed correctly, the result is passed
	 * as a parameter.
	 * 
	 * @param result
	 */
	void complete(T result);
}
