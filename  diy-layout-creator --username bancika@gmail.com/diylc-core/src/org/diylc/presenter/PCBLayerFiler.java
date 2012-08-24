package org.diylc.presenter;

import java.lang.reflect.Method;

import org.diylc.common.IComponentFiler;
import org.diylc.common.PCBLayer;
import org.diylc.core.IDIYComponent;

public class PCBLayerFiler implements IComponentFiler {

	private PCBLayer layer;

	public PCBLayerFiler(PCBLayer layer) {
		super();
		this.layer = layer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean testComponent(IDIYComponent<?> component) {
		Class<?> clazz = component.getClass();
		try {
			Method m = clazz.getMethod("getLayer", null);
			PCBLayer l = (PCBLayer) m.invoke(component);
			return layer == l;
		} catch (Exception e) {
			return false;
		}		
	}
}
