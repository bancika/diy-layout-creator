package org.diylc.model;

/**
 * Enumerates control point visibility policies.
 * 
 * @author Branislav Stojkovic
 */
public enum VisibilityPolicy {

	/**
	 * Control point should be rendered all the time.
	 */
	ALWAYS,
	/**
	 * Control point should be rendered only when the component is selected.
	 */
	WHEN_SELECTED,
	/**
	 * Control point should not be rendered regardless of component selection.
	 */
	NEVER;
}
