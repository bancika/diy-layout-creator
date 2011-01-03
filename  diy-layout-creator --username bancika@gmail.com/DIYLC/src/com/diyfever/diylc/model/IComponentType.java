package com.diyfever.diylc.model;

import javax.swing.Icon;

/**
 * Interface used to describe a component type.
 * 
 * @author Branislav Stojkovic
 * 
 * @see IComponentInstance
 */
public interface IComponentType {

	/**
	 * Returns component type name.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Returns component instance name prefix, e.g. "R" for resistors, "C" for
	 * capacitors, etc.
	 * 
	 * @return
	 */
	String getInstanceNamePrefix();

	/**
	 * Returns component category.
	 * 
	 * @return
	 */
	String getCategory();

	/**
	 * Returns component type description that may contain HTML tags.
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * Returns an {@link Icon} used to represent component type.
	 * 
	 * @return
	 */
	Icon getIcon();

	/**
	 * Returns a {@link ComponentLayer} where this component belongs.
	 * 
	 * @return
	 */
	ComponentLayer getComponentLayer();

	/**
	 * Returns class that represents component instances of this component type.
	 * Application will instantiate objects of this class when components are
	 * created.
	 * 
	 * @return
	 */
	Class<? extends IComponentInstance> getComponentInstanceClass();
}
