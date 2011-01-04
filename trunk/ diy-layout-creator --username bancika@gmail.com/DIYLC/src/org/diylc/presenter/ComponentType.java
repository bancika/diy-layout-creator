package org.diylc.presenter;

import javax.swing.Icon;

import org.diylc.core.ComponentLayer;
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
	private Class<? extends IDIYComponent> instanceClass;
	private ComponentLayer layer;

	public ComponentType(String name, String description, String category, String namePrefix,
			String author, Icon icon, Class<? extends IDIYComponent> instanceClass,
			ComponentLayer layer) {
		super();
		this.name = name;
		this.description = description;
		this.category = category;
		this.namePrefix = namePrefix;
		this.author = author;
		this.icon = icon;
		this.instanceClass = instanceClass;
		this.layer = layer;
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

	public Class<? extends IDIYComponent> getInstanceClass() {
		return instanceClass;
	}

	public ComponentLayer getLayer() {
		return layer;
	}

	@Override
	public String toString() {
		return name;
	}
}
