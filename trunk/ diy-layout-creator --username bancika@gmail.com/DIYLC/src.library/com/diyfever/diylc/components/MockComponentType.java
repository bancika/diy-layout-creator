package com.diyfever.diylc.components;

import javax.swing.Icon;

import com.diyfever.diylc.images.IconLoader;
import com.diyfever.diylc.model.ComponentLayer;
import com.diyfever.diylc.model.IComponentInstance;
import com.diyfever.diylc.model.IComponentType;

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
