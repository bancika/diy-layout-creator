package org.diylc.common;

import java.awt.Point;

public interface IMouseProcessor {

	/**
	 * Notifies the presenter that mouse is clicked.
	 * 
	 * Note: point coordinates are display based, i.e. scaled for zoom factor.
	 * 
	 * @param point
	 * @param button
	 * @param ctrlDown
	 * @param shiftDown
	 * @param altDown
	 * @param clickCount
	 */
	void mouseClicked(Point point, int button, boolean ctrlDown, boolean shiftDown, boolean altDown,
			int clickCount);

	/**
	 * Notifies the presenter that mouse is moved.
	 * 
	 * Note: point coordinates are display based, i.e. scaled for zoom factor.
	 * 
	 * @param point
	 * @param ctrlDown
	 * @param shiftDown
	 * @param altDown
	 */
	void mouseMoved(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown);

	/**
	 * Notification that drag has been started from the specified point.
	 * 
	 * Note: point coordinates are scaled for zoom factor.
	 * 
	 * @param point
	 * @param dragAction
	 */
	void dragStarted(Point point, int dragAction);

	/**
	 * Checks if it's possible to drop over the specified point.
	 * 
	 * Note: point coordinates are scaled for zoom factor.
	 * 
	 * @param point
	 * @return
	 */
	boolean dragOver(Point point);

	/**
	 * Changes the current drag action during the dragging.
	 * 
	 * @param dragAction
	 */
	void dragActionChanged(int dragAction);

	/**
	 * Notification that drag has been ended in the specified point.
	 * 
	 * Note: point coordinates are scaled for zoom factor.
	 * 
	 * @param point
	 */
	void dragEnded(Point point);
}
