package org.diylc.common;

import javax.swing.Icon;

import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.BomPolicy;

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
	private CreationMethod creationMethod;
	private String category;
	private String namePrefix;
	private String author;
	private Icon icon;
	private Class<? extends IDIYComponent<?>> instanceClass;
	private double zOrder;
	private boolean stretchable;
	private BomPolicy bomPolicy;

	public ComponentType(String name, String description, CreationMethod creationMethod,
			String category, String namePrefix, String author, Icon icon,
			Class<? extends IDIYComponent<?>> instanceClass, double zOrder, 
					boolean stretchable, BomPolicy bomPolicy) {
		super();
		this.name = name;
		this.description = description;
		this.creationMethod = creationMethod;
		this.category = category;
		this.namePrefix = namePrefix;
		this.author = author;
		this.icon = icon;
		this.instanceClass = instanceClass;
		this.zOrder = zOrder;
		this.stretchable = stretchable;
		this.bomPolicy = bomPolicy;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public CreationMethod getCreationMethod() {
		return creationMethod;
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

	public Class<? extends IDIYComponent<?>> getInstanceClass() {
		return instanceClass;
	}

	public double getZOrder() {
		return zOrder;
	}

	public boolean isStretchable() {
		return stretchable;
	}
	
	public BomPolicy getBomPolicy() {
		return bomPolicy;
	}

	@Override
	public String toString() {
		return name;
	}
}
