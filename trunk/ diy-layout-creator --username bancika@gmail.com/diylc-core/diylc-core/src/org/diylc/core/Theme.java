package org.diylc.core;

import java.awt.Color;
import java.io.Serializable;

public class Theme implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private Color bgColor;
	private Color gridColor;
	private Color outlineColor;

	public Theme(String name, Color bgColor, Color gridColor, Color outlineColor) {
		super();
		this.name = name;
		this.bgColor = bgColor;
		this.gridColor = gridColor;
		this.outlineColor = outlineColor;
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

	public Color getOutlineColor() {
		return outlineColor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bgColor == null) ? 0 : bgColor.hashCode());
		result = prime * result + ((gridColor == null) ? 0 : gridColor.hashCode());
		result = prime * result + ((outlineColor == null) ? 0 : outlineColor.hashCode());
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
		if (gridColor == null) {
			if (other.gridColor != null)
				return false;
		} else if (!gridColor.equals(other.gridColor))
			return false;
		if (outlineColor == null) {
			if (other.outlineColor != null)
				return false;
		} else if (!outlineColor.equals(other.outlineColor))
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
