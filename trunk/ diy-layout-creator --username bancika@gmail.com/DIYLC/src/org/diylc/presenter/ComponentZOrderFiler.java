package org.diylc.presenter;

import org.diylc.common.ComponentType;
import org.diylc.common.IComponentFiler;
import org.diylc.core.IDIYComponent;

public class ComponentZOrderFiler implements IComponentFiler {

	private int zOrder;

	public ComponentZOrderFiler(int zOrder) {
		super();
		this.zOrder = zOrder;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean testComponent(IDIYComponent<?> component) {
		ComponentType componentType = ComponentProcessor.getInstance().createComponentTypeFrom(
				(Class<? extends IDIYComponent<?>>) component.getClass());
		return Math.round(componentType.getZOrder()) == zOrder;
	}
}
