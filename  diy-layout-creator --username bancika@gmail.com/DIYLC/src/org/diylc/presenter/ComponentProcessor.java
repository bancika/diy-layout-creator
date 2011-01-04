package org.diylc.presenter;

import java.awt.Point;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.diylc.common.ControlPointWrapper;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentName;
import org.diylc.core.annotations.ComponentValue;
import org.diylc.core.annotations.ControlPoint;
import org.diylc.core.annotations.EditableProperty;

import com.rits.cloning.Cloner;

/**
 * Utility class with component processing methods.
 * 
 * @author Branislav Stojkovic
 */
public class ComponentProcessor {

	private static final Logger LOG = Logger.getLogger(ComponentProcessor.class);

	private static ComponentProcessor instance;

	private Map<Class<?>, List<PropertyWrapper>> propertyCache;
	private Map<Class<?>, List<ControlPointWrapper>> controlPointCache;

	private Cloner cloner;

	public static ComponentProcessor getInstance() {
		if (instance == null) {
			instance = new ComponentProcessor();
		}
		return instance;
	}

	private ComponentProcessor() {
		super();
		this.cloner = new Cloner();
		this.propertyCache = new HashMap<Class<?>, List<PropertyWrapper>>();
		this.controlPointCache = new HashMap<Class<?>, List<ControlPointWrapper>>();
	}

	/**
	 * Extracts all editable properties from the component class.
	 * 
	 * @param clazz
	 * @return
	 */
	public List<PropertyWrapper> extractProperties(Class<? extends IDIYComponent> clazz) {
		if (propertyCache.containsKey(clazz)) {
			return cloner.deepClone(propertyCache.get(clazz));
		}
		List<PropertyWrapper> properties = new ArrayList<PropertyWrapper>();
		for (Method method : clazz.getMethods()) {
			if (method.getName().startsWith("get")) {
				try {
					Method setter = clazz.getMethod("set" + method.getName().substring(3), method
							.getReturnType());
					if (method.isAnnotationPresent(EditableProperty.class)) {
						EditableProperty annotation = method.getAnnotation(EditableProperty.class);
						String name;
						if (annotation.name().equals("")) {
							name = method.getName().substring(3);
						} else {
							name = annotation.name();
						}
						PropertyWrapper property = new PropertyWrapper(name,
								method.getReturnType(), method, setter, annotation.defaultable());
						properties.add(property);
					}
				} catch (NoSuchMethodException e) {
					LOG.debug("No matching setter found for \"" + method.getName()
							+ "\". Skipping...");
				}
			}
		}

		propertyCache.put(clazz, properties);
		return cloner.deepClone(properties);
	}

	/**
	 * Reads all control points from the specified component class. Note than
	 * control points are cached, so it may happen that control points returned
	 * already have their values populated. Always use
	 * {@link ControlPointWrapper#readFrom(IDIYComponent)} to update their state
	 * from an actual component.
	 * 
	 * @param clazz
	 * @return
	 */
	public List<ControlPointWrapper> extractControlPoints(Class<? extends IDIYComponent> clazz) {
		if (controlPointCache.containsKey(clazz)) {
			return cloner.deepClone(controlPointCache.get(clazz));
		}
		List<ControlPointWrapper> controlPoints = new ArrayList<ControlPointWrapper>();
		for (Method method : clazz.getMethods()) {
			if (method.getName().startsWith("get")) {
				try {
					Method setter = clazz.getMethod("set" + method.getName().substring(3), method
							.getReturnType());
					if (method.isAnnotationPresent(ControlPoint.class)) {
						if (method.getReturnType().equals(Point.class)) {
							ControlPoint annotation = method.getAnnotation(ControlPoint.class);
							ControlPointWrapper controlPoint = new ControlPointWrapper(method
									.getName().substring(3), method, setter, annotation.editable(),
									annotation.sticky(), annotation.visibilityPolicy());
							controlPoints.add(controlPoint);
						} else {
							LOG.debug("Control point return type must be java.awt.Point.");
						}
					}
				} catch (NoSuchMethodException e) {
					LOG.debug("No matching setter found for \"" + method.getName()
							+ "\". Skipping...");
				}
			}
		}

		controlPointCache.put(clazz, controlPoints);
		return cloner.deepClone(controlPoints);
	}

	/**
	 * Returns properties mutual for all the selected components.
	 * 
	 * @param selectedComponents
	 * @return
	 */
	public List<PropertyWrapper> getMutualSelectionProperties(List<IDIYComponent> selectedComponents) {
		if (selectedComponents.isEmpty()) {
			return null;
		}
		List<PropertyWrapper> properties = new ArrayList<PropertyWrapper>();
		IDIYComponent firstComponent = selectedComponents.get(0);
		properties.addAll(extractProperties(firstComponent.getClass()));
		// Initialize values
		for (PropertyWrapper property : properties) {
			try {
				property.readFrom(firstComponent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i = 1; i < selectedComponents.size(); i++) {
			IDIYComponent component = selectedComponents.get(i);
			List<PropertyWrapper> newProperties = extractProperties(component.getClass());
			for (PropertyWrapper property : newProperties) {
				try {
					property.readFrom(component);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			properties.retainAll(newProperties);
			// for (PropertyWrapper property : properties) {
			// try {
			// property.readUniqueFrom(component);
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
		}
		return properties;
	}

	/**
	 * Finds the method annotated with {@link ComponentName} annotations and
	 * calls it to retrieve component name.
	 * 
	 * @param component
	 * @return
	 */
	public String extractComponentName(IDIYComponent component) {
		String name = null;
		for (Method method : component.getClass().getMethods()) {
			if (method.isAnnotationPresent(ComponentName.class)) {
				try {
					name = method.invoke(component).toString();
				} catch (Exception e) {
					name = null;
				}
			}
		}
		return name;
	}

	/**
	 * Finds the method annotated with {@link ComponentName} and it's matching
	 * setter and calls the setter to update the component name.
	 * 
	 * @param component
	 * @param newName
	 */
	public void writeComponentName(IDIYComponent component, String newName) {
		for (Method method : component.getClass().getMethods()) {
			if (method.isAnnotationPresent(ComponentName.class)) {
				try {
					Method setter = component.getClass().getMethod(
							method.getName().replaceFirst("get", "set"), String.class);
					setter.invoke(component, newName);
				} catch (Exception e) {
					LOG.warn("Could not update component name on a component of type "
							+ component.getClass().getName());
				}
			}
		}
	}

	/**
	 * Finds the method annotated with {@link ComponentValue} annotations and
	 * calls it to retrieve component value.
	 * 
	 * @param component
	 * @return
	 */
	public String extractComponentValue(IDIYComponent component) {
		String value = null;
		for (Method method : component.getClass().getMethods()) {
			if (method.isAnnotationPresent(ComponentValue.class)) {
				try {
					value = method.invoke(component).toString();
				} catch (Exception e) {
					value = null;
				}
			}
		}
		return value;
	}
}
