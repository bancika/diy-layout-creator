package org.diylc.components;

import javax.swing.Icon;

import org.diylc.core.ComponentLayer;
import org.diylc.core.IComponentInstance;
import org.diylc.core.IComponentType;
import org.diylc.images.IconLoader;


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
