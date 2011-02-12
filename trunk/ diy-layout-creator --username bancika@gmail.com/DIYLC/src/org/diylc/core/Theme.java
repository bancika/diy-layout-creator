package org.diylc.core;

import java.awt.Color;
import java.io.Serializable;

public class Theme implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private Color bgColor;
	private Color gridColor;
	private Color componentBodyColor;
	private Color componentBorderColor;
	private Color labelColor;

	public Theme(String name, Color bgColor, Color gridColor, Color componentBodyColor,
			Color componentBorderColor, Color labelColor) {
		super();
		this.name = name;
		this.bgColor = bgColor;
		this.gridColor = gridColor;
		this.componentBodyColor = componentBodyColor;
		this.componentBorderColor = componentBorderColor;
		this.labelColor = labelColor;
	}
	
	public String getName() {
		return name;
	}

	public Color getBgColor() {
		return bgColor;
	}

	public Color getGridColor() {
		return gridColor;
	}

	public Color getComponentBodyColor() {
		return componentBodyColor;
	}

	public Color getComponentBorderColor() {
		return componentBorderColor;
	}

	public Color getLabelColor() {
		return labelColor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bgColor == null) ? 0 : bgColor.hashCode());
		result = prime * result
				+ ((componentBodyColor == null) ? 0 : componentBodyColor.hashCode());
		result = prime * result
				+ ((componentBorderColor == null) ? 0 : componentBorderColor.hashCode());
		result = prime * result
				+ ((labelColor == null) ? 0 : labelColor.hashCode());
		result = prime * result + ((gridColor == null) ? 0 : gridColor.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Theme other = (Theme) obj;
		if (bgColor == null) {
			if (other.bgColor != null)
				return false;
		} else if (!bgColor.equals(other.bgColor))
			return false;
		if (componentBodyColor == null) {
			if (other.componentBodyColor != null)
				return false;
		} else if (!componentBodyColor.equals(other.componentBodyColor))
			return false;
		if (componentBorderColor == null) {
			if (other.componentBorderColor != null)
				return false;
		} else if (!componentBorderColor.equals(other.componentBorderColor))
			return false;
		if (labelColor == null) {
			if (other.labelColor != null)
				return false;
		} else if (!labelColor.equals(other.labelColor))
			return false;
		if (gridColor == null) {
			if (other.gridColor != null)
				return false;
		} else if (!gridColor.equals(other.gridColor))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
