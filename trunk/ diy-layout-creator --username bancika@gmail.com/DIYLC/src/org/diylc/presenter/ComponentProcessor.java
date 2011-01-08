package org.diylc.presenter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
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
	}

	/**
	 * Extracts all editable properties from the component class.
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<PropertyWrapper> extractProperties(Class<? extends IDIYComponent> clazz) {
		if (propertyCache.containsKey(clazz)) {
			return cloner.deepClone(propertyCache.get(clazz));
		}
		List<PropertyWrapper> properties = new ArrayList<PropertyWrapper>();
		for (Method method : clazz.getMethods()) {
			if (method.getName().startsWith("get")) {
				try {
					if (method.isAnnotationPresent(EditableProperty.class)
							&& !method.isAnnotationPresent(Deprecated.class)) {
						EditableProperty annotation = method.getAnnotation(EditableProperty.class);
						String name;
						if (annotation.name().equals("")) {
							name = method.getName().substring(3);
						} else {
							name = annotation.name();
						}
						Method setter = clazz.getMethod("set" + method.getName().substring(3),
								method.getReturnType());
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
	 * Returns properties mutual for all the selected components.
	 * 
	 * @param selectedComponents
	 * @return
	 */
	public List<PropertyWrapper> getMutualSelectionProperties(
			List<IDIYComponent<?>> selectedComponents) {
		if (selectedComponents.isEmpty()) {
			return null;
		}
		List<PropertyWrapper> properties = new ArrayList<PropertyWrapper>();
		IDIYComponent<?> firstComponent = selectedComponents.get(0);
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
			IDIYComponent<?> component = selectedComponents.get(i);
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
}
