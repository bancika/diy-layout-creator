package org.diylc.components;

import org.diylc.core.annotations.EditableProperty;

public abstract class AbstractTransparentComponent<T> extends AbstractComponent<T> {

	private static final long serialVersionUID = 1L;

	public static byte MAX_ALPHA = 127;

	protected byte alpha = MAX_ALPHA;

	@EditableProperty
	public Byte getAlpha() {
		return alpha;
	}

	public void setAlpha(Byte alpha) {
		this.alpha = alpha;
	}
}
