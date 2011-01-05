package org.diylc.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.diylc.core.IDIYComponent;

/**
 * Entity class for editable properties extracted from component objects.
 * Represents a single editable property together with it's current value.
 * 
 * @author Branislav Stojkovic
 */
public class PropertyWrapper implements Cloneable {

	private String name;
	private Class<?> type;
	private Object value;
	private Method setter;
	private Method getter;
	private boolean defaultable;

	public PropertyWrapper(String name, Class<?> type, Method getter, Method setter,
			boolean defaultable) {
		super();
		this.name = name;
		this.type = type;
		this.getter = getter;
		this.setter = setter;
		this.defaultable = defaultable;
	}

	public void readFrom(IDIYComponent<?> component) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		this.value = getter.invoke(component);
	}

	// public void readUniqueFrom(IDIYComponent component)
	// throws IllegalArgumentException, IllegalAccessException,
	// InvocationTargetException {
	// Object newValue = getter.invoke(component);
	// if (!newValue.equals(value)) {
	// this.value = null;
	// }
	// }

	public void writeTo(IDIYComponent<?> component) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		setter.invoke(component, value);
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public boolean isDefaultable() {
		return defaultable;
	}

	public void setDefaultable(boolean defaultable) {
		this.defaultable = defaultable;
	}

	// @Override
	// public Object clone() throws CloneNotSupportedException {
	// // Try to invoke clone method on value if possible.
	// try {
	// Method cloneMethod = value.getClass().getMethod("clone");
	// return new Property(name, type, cloneMethod.invoke(value));
	// } catch (Exception e) {
	// }
	// return new Property(name, type, value);
	// }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (defaultable ? 1231 : 1237);
		result = prime * result + ((getter == null) ? 0 : getter.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((setter == null) ? 0 : setter.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		PropertyWrapper other = (PropertyWrapper) obj;
		if (defaultable != other.defaultable)
			return false;
		if (getter == null) {
			if (other.getter != null)
				return false;
		} else if (!getter.equals(other.getter))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (setter == null) {
			if (other.setter != null)
				return false;
		} else if (!setter.equals(other.setter))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name + " = " + value;
	}
}
