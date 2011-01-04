package org.diylc.components;

import javax.swing.Icon;

import org.diylc.core.ComponentLayer;
import org.diylc.core.IComponentInstance;
import org.diylc.core.IComponentType;
import org.diylc.images.IconLoader;


public class MockComponentType implements IComponentType {

	@Override
	public Class<? extends IComponentInstance> getComponentInstanceClass() {
		return MockComponentInstance.class;
	}
	
	@Override
	public String getInstanceNamePrefix() {
		return "ABC";
	}

	@Override
	public ComponentLayer getComponentLayer() {
		return ComponentLayer.BOARD;
	}

	@Override
	public String getDescription() {
		return "Some description";
	}

	@Override
	public Icon getIcon() {
		return IconLoader.Gears.getIcon();
	}

	@Override
	public String getName() {
		return "Mock Component";
	}

	@Override
	public String getCategory() {
		return "Samples";
	}

}
