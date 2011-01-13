package org.diylc.common;

import javax.swing.Icon;

import org.diylc.core.IDIYComponent;

/**
 * Entity class used to describe a component type.
 * 
 * @author Branislav Stojkovic
 * 
 * @see IDIYComponent
 */
public class ComponentType {

	private String name;
	private String description;
	private String category;
	private String namePrefix;
	private String author;
	private Icon icon;
	@SuppressWarnings("unchecked")
	private Class<? extends IDIYComponent> instanceClass;
	private double zOrder;
	private boolean stretchable;
	private boolean sticky;

	@SuppressWarnings("unchecked")
	public ComponentType(String name, String description, String category, String namePrefix,
			String author, Icon icon, Class<? extends IDIYComponent> instanceClass,
			double zOrder, boolean stretchable, boolean sticky) {
		super();
		this.name = name;
		this.description = description;
		this.category = category;
		this.namePrefix = namePrefix;
		this.author = author;
		this.icon = icon;
		this.instanceClass = instanceClass;
		this.zOrder = zOrder;
		this.stretchable = stretchable;
		this.sticky = sticky;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getCategory() {
		return category;
	}

	public String getNamePrefix() {
		return namePrefix;
	}

	public String getAuthor() {
		return author;
	}

	public Icon getIcon() {
		return icon;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends IDIYComponent> getInstanceClass() {
		return instanceClass;
	}

	public double getZOrder() {
		return zOrder;
	}

	public boolean isStretchable() {
		return stretchable;
	}
	
	public boolean isSticky() {
		return sticky;
	}

	@Override
	public String toString() {
		return name;
	}
}
