package org.diylc.core;

/**
 * Interface for tracking component drawing process. Components may use it to
 * stop or restart tracking. Anything drawn while tracking is stopped will not
 * be added to the mouse hot-spot area.
 * 
 * @author Branislav Stojkovic
 */
public interface IDrawingObserver {

	void stopTracking();

	void startTracking();
}
