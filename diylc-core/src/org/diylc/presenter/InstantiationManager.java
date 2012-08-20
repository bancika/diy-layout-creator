package org.diylc.presenter;

import java.awt.Point;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.common.Orientation;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.measures.Size;

import com.rits.cloning.Cloner;

/**
 * Manages component instantiation.
 * 
 * @author Branislav Stojkovic
 */
public class InstantiationManager {

	private static final Logger LOG = Logger
			.getLogger(InstantiationManager.class);

	public static int MAX_RECENT_COMPONENTS = 16;

	private Cloner cloner;

	private ComponentType componentTypeSlot;
	private IDIYComponent<?> componentSlot;
	private Point firstControlPoint;
	private Point potentialControlPoint;

	public InstantiationManager() {
		cloner = new Cloner();
	}

	public ComponentType getComponentTypeSlot() {
		return componentTypeSlot;
	}

	public IDIYComponent<?> getComponentSlot() {
		return componentSlot;
	}

	public Point getFirstControlPoint() {
		return firstControlPoint;
	}

	public Point getPotentialControlPoint() {
		return potentialControlPoint;
	}

	public void setComponentTypeSlot(ComponentType componentTypeSlot,
			Project currentProject) throws Exception {
		this.componentTypeSlot = componentTypeSlot;
		if (componentTypeSlot == null) {
			this.componentSlot = null;
		} else {
			switch (componentTypeSlot.getCreationMethod()) {
			case POINT_BY_POINT:
				this.componentSlot = null;
				break;
			case SINGLE_CLICK:
				this.componentSlot = instantiateComponent(componentTypeSlot,
						new Point(0, 0), currentProject);
				break;
			}
		}
		this.firstControlPoint = null;
		this.potentialControlPoint = null;
	}

	public void instatiatePointByPoint(Point scaledPoint, Project currentProject)
			throws Exception {
		firstControlPoint = scaledPoint;
		componentSlot = instantiateComponent(componentTypeSlot,
				firstControlPoint, currentProject);

		// Set the other control point to the same location, we'll
		// move it later when mouse moves.
		componentSlot.setControlPoint(firstControlPoint, 0);
		componentSlot.setControlPoint(firstControlPoint, 1);
	}

	/**
	 * Updates component in the slot with the new second control point.
	 * 
	 * @param scaledPoint
	 * @return true, if any change is made
	 */
	public boolean updatePointByPoint(Point scaledPoint) {
		boolean changeMade = !scaledPoint.equals(potentialControlPoint);
		potentialControlPoint = scaledPoint;
		if (componentSlot != null) {
			componentSlot.setControlPoint(scaledPoint, 1);
		}
		return changeMade;
	}

	public boolean updateSingleClick(Point scaledPoint, boolean snapToGrid,
			Size gridSpacing) {
		if (potentialControlPoint == null) {
			potentialControlPoint = new Point(0, 0);
		}
		int dx = scaledPoint.x - potentialControlPoint.x;
		int dy = scaledPoint.y - potentialControlPoint.y;
		if (snapToGrid) {
			dx = CalcUtils.roundToGrid(dx, gridSpacing);
			dy = CalcUtils.roundToGrid(dy, gridSpacing);
		}
		// Only repaint if there's an actual change.
		if (dx == 0 && dy == 0) {
			return false;
		}
		potentialControlPoint.translate(dx, dy);
		if (componentSlot == null) {
			LOG.error("Component slot should not be null!");
		} else {
			Point p = new Point();
			for (int i = 0; i < componentSlot.getControlPointCount(); i++) {
				p.setLocation(componentSlot.getControlPoint(i));
				p.translate(dx, dy);
				componentSlot.setControlPoint(p, i);
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public IDIYComponent<?> instantiateComponent(ComponentType componentType,
			Point point, Project currentProject) throws Exception {
		LOG.info("Instatiating component of type: "
				+ componentType.getInstanceClass().getName());

		// Instantiate the component.
		IDIYComponent<?> component = componentType.getInstanceClass()
				.newInstance();

		component.setName(createUniqueName(componentType, currentProject));

		// Translate them to the desired location.
		if (point != null) {
			for (int j = 0; j < component.getControlPointCount(); j++) {
				Point controlPoint = new Point(component.getControlPoint(j));
				controlPoint.translate(point.x, point.y);
				// snapPointToGrid(controlPoint);
				component.setControlPoint(controlPoint, j);
			}
		}

		fillWithDefaultProperties(component);

		// Write to recent components
		List<String> recentComponentTypes = (List<String>) ConfigurationManager
				.getInstance().readObject(IPlugInPort.RECENT_COMPONENTS_KEY,
						new ArrayList<ComponentType>());
		String className = componentType.getInstanceClass().getName();
		if (recentComponentTypes.size() == 0
				|| !recentComponentTypes.get(0).equals(className)) {
			// Remove if it's already somewhere in the list.
			recentComponentTypes.remove(className);
			// Add to the end of the list.
			recentComponentTypes.add(0, className);
			// Trim the list if necessary.
			if (recentComponentTypes.size() > MAX_RECENT_COMPONENTS) {
				recentComponentTypes.remove(recentComponentTypes.size() - 1);
			}
			ConfigurationManager.getInstance().writeValue(
					IPlugInPort.RECENT_COMPONENTS_KEY, recentComponentTypes);
		}

		return component;
	}

	public String createUniqueName(ComponentType componentType,
			Project currentProject) {
		int i = 0;
		boolean exists = true;
		List<IDIYComponent<?>> components = currentProject.getComponents();
		String[] takenNames = new String[components.size()];
		for (int j = 0; j < currentProject.getComponents().size(); j++) {
			takenNames[j] = components.get(i).getName();
		}
		Arrays.sort(takenNames);
		while (exists) {
			i++;
			String name = componentType.getNamePrefix() + i;
			exists = false;
			if (Arrays.binarySearch(takenNames, name) >= 0) {
				exists = true;
				break;
			}
		}
		return componentType.getNamePrefix() + i;
	}

	/**
	 * Finds any properties that have default values and injects default values.
	 * Typically it should be used for {@link IDIYComponent} and {@link Project}
	 * objects.
	 * 
	 * @param object
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void fillWithDefaultProperties(Object object)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, SecurityException, NoSuchMethodException {
		// Extract properties.
		List<PropertyWrapper> properties = ComponentProcessor.getInstance()
				.extractProperties(object.getClass());
		// Override with default values if available.
		for (PropertyWrapper property : properties) {
			Object defaultValue = ConfigurationManager.getInstance()
					.readObject(
							Presenter.DEFAULTS_KEY_PREFIX
									+ object.getClass().getName() + ":"
									+ property.getName(), null);
			if (defaultValue != null) {
				property.setValue(cloner.deepClone(defaultValue));
				property.writeTo(object);
			}
		}
	}

	public void tryToRotateComponentSlot() {
		if (this.componentSlot == null) {
			LOG.debug("Component slot is empty, cannot rotate");
			return;
		}
		List<PropertyWrapper> properties = ComponentProcessor.getInstance()
				.extractProperties(this.componentTypeSlot.getInstanceClass());
		PropertyWrapper angleProperty = null;
		for (PropertyWrapper propertyWrapper : properties) {
			if (propertyWrapper.getType().getName().equals(
					Orientation.class.getName())) {
				angleProperty = propertyWrapper;
				break;
			}
		}
		if (angleProperty == null) {
			LOG
					.debug("Component in the slot does not have a property of type Orientation, cannot rotate");
			return;
		}
		try {
			angleProperty.readFrom(this.componentSlot);
			Orientation value = (Orientation) angleProperty.getValue();
			value.ordinal();
			angleProperty.setValue(Orientation.values()[(value.ordinal() + 1)
					% Orientation.values().length]);
			angleProperty.writeTo(this.componentSlot);
		} catch (Exception e) {
			LOG.warn("Error trying to rotate the component", e);
		}
	}
}
