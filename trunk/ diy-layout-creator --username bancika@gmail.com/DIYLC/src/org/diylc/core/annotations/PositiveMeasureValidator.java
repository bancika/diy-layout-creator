package org.diylc.core.annotations;

import org.diylc.core.IPropertyValidator;
import org.diylc.core.ValidationException;
import org.diylc.core.measures.AbstractMeasure;

public class PositiveMeasureValidator implements IPropertyValidator {

	@Override
	public void validate(Object value) throws ValidationException {
		if (value instanceof AbstractMeasure) {
			AbstractMeasure measure = (AbstractMeasure) value;
			if (measure.getValue() < 0) {
				throw new ValidationException("must be greater than zero.");
			}
		} else {
			throw new ValidationException("wrong data type, measure expected.");
		}
	}
}
