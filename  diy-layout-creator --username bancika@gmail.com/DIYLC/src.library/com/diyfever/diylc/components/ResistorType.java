package com.diyfever.diylc.components;

import javax.swing.Icon;

import com.diyfever.diylc.images.IconLoader;
import com.diyfever.diylc.model.ComponentLayer;
import com.diyfever.diylc.model.IComponentInstance;
import com.diyfever.diylc.model.IComponentType;

public class ResistorType implements IComponentType {

	@Override
	public Class<? extends IComponentInstance> getComponentInstanceClass() {
		return ResistorInstance.class;
	}
	
	@Override
	public String getInstanceNamePrefix() {
		return "R";
	}

	@Override
	public ComponentLayer getComponentLayer() {
		return ComponentLayer.COMPONENT;
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
		return "Resistor";
	}

	@Override
	public String getCategory() {
		return "Samples";
	}

}
