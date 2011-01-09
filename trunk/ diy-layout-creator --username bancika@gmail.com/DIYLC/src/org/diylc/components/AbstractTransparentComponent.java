package org.diylc.components;

import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.EditableProperty;

public abstract class AbstractTransparentComponent<T> implements IDIYComponent<T> {

	private static final long serialVersionUID = 1L;

	public static byte MAX_ALPHA = 127;

	protected String name = "New Component";
	protected byte alpha = MAX_ALPHA;

	@EditableProperty(defaultable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@EditableProperty
	public Byte getAlpha() {
		return alpha;
	}

	public void setAlpha(Byte alpha) {
		this.alpha = alpha;
	}
}
