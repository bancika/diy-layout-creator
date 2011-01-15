package org.diylc.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

/**
 * Entity class that defines a project. Contains project properties and a
 * collection of components.This class is serialized to file. Some filed getters
 * are tagged with {@link EditableProperty} to enable for user to edit them.
 * 
 * @author Branislav Stojkovic
 */
public class Project implements Serializable {

	private static final long serialVersionUID = 1L;

	public static String DEFAULT_TITLE = "New Project";
	public static Size DEFAULT_WIDTH = new Size(29d, SizeUnit.cm);
	public static Size DEFAULT_HEIGHT = new Size(21d, SizeUnit.cm);
	public static Size DEFAULT_GRID_SPACING = new Size(0.2d, SizeUnit.in);

	private String title;
	private String author;
	private String description;
	private Size width;
	private Size height;
	private Size gridSpacing;
	private List<IDIYComponent<?>> components;
	private Set<Set<IDIYComponent<?>>> groups;

	public Project() {
		components = new ArrayList<IDIYComponent<?>>();
		groups = new HashSet<Set<IDIYComponent<?>>>();
		title = DEFAULT_TITLE;
		author = System.getProperty("user.name");
		width = DEFAULT_WIDTH;
		height = DEFAULT_HEIGHT;
		gridSpacing = DEFAULT_GRID_SPACING;
	}

	public Project(String title, String author, String description, Size width, Size height,
			Size gridSpacing, List<IDIYComponent<?>> components, Set<Set<IDIYComponent<?>>> groups) {
		super();
		this.title = title;
		this.author = author;
		this.description = description;
		this.width = width;
		this.height = height;
		this.gridSpacing = gridSpacing;
		this.components = components;
		this.groups = groups;
	}

	@EditableProperty
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@EditableProperty
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	@EditableProperty
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@EditableProperty
	public Size getWidth() {
		return width;
	}

	public void setWidth(Size width) {
		this.width = width;
	}

	@EditableProperty
	public Size getHeight() {
		return height;
	}

	public void setHeight(Size height) {
		this.height = height;
	}

	@EditableProperty(name = "Grid spacing")
	public Size getGridSpacing() {
		return gridSpacing;
	}

	public void setGridSpacing(Size gridSpacing) {
		this.gridSpacing = gridSpacing;
	}

	/**
	 * List of components sorted by z-order ascending.
	 * 
	 * @return
	 */
	public List<IDIYComponent<?>> getComponents() {
		return components;
	}

	/**
	 * Set of grouped components.
	 * 
	 * @return
	 */
	public Set<Set<IDIYComponent<?>>> getGroups() {
		return groups;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((components == null) ? 0 : components.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((height == null) ? 0 : height.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((width == null) ? 0 : width.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Project other = (Project) obj;
		if (author == null) {
			if (other.author != null)
				return false;
		} else if (!author.equals(other.author))
			return false;
		if (components == null) {
			if (other.components != null)
				return false;
		} else if (!components.equals(other.components))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (height == null) {
			if (other.height != null)
				return false;
		} else if (!height.equals(other.height))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (width == null) {
			if (other.width != null)
				return false;
		} else if (!width.equals(other.width))
			return false;
		return true;
	}

	// @Override
	// public Project clone() throws CloneNotSupportedException {
	// List<IDIYComponent> clonedComponents = new
	// ArrayList<IDIYComponent>();
	// for (IDIYComponent component : components) {
	// clonedComponents.add(component.clone());
	// }
	// Project project = new Project(title, author, description,
	// width.clone(), height.clone(), clonedComponents);
	// return project;
	// }
}
