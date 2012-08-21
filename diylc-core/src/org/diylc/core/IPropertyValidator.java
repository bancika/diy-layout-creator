package org.diylc.core;

/**
 * Interface for validating property value changes.
 * 
 * @author Branislav Stojkovic
 */
public interface IPropertyValidator {

	/**
	 * @param value
	 * @throws ValidationException
	 *             if validation fails. Message will contain the reason.
	 */
	void validate(Object value) throws ValidationException;
}
