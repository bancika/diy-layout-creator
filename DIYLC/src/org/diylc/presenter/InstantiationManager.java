package org.diylc.presenter;

import java.awt.Point;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.diylc.common.ComponentType;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.measures.Size;

import com.diyfever.gui.miscutils.ConfigurationManager;
import com.rits.cloning.Cloner;

/**
 * Manages component instantiation.
 * 
 * @author Branislav Stojkovic
 */
public class InstantiationManager {

	private static final Logger LOG = Logger.getLogger(InstantiationManager.class);

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

	public void setComponentTypeSlot(ComponentType componentTypeSlot, Project currentProject)
			throws Exception {
		this.componentTypeSlot = componentTypeSlot;
		if (componentTypeSlot == null) {
			this.componentSlot = null;
		} else {
			switch (componentTypeSlot.getCreationMethod()) {
			case POINT_BY_POINT:
				this.componentSlot = null;
				break;
			case SINGLE_CLICK:
				this.componentSlot = instantiateComponent(componentTypeSlot, new Point(0, 0),
						currentProject);
				break;
			}
		}
		this.firstControlPoint = null;
		this.potentialControlPoint = null;
	}

	public void instatiatePointByPoint(Point scaledPoint, Project currentProject) throws Exception {
		firstControlPoint = scaledPoint;
		componentSlot = instantiateComponent(componentTypeSlot, firstControlPoint, currentProject);

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

	public boolean updateSingleClick(Point scaledPoint, boolean snapToGrid, Size gridSpacing) {
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

	public IDIYComponent<?> instantiateComponent(ComponentType componentType, Point point,
			Project currentProject) throws Exception {
		LOG.info("Instatiating component of type: " + componentType.getInstanceClass().getName());

		// Instantiate the component.
		IDIYComponent<?> component = componentType.getInstanceClass().newInstance();

		// Find the next available componentName for the component.
		int i = 0;
		boolean exists = true;
		while (exists) {
			i++;
			String name = componentType.getNamePrefix() + i;
			exists = false;
			for (IDIYComponent<?> c : currentProject.getComponents()) {
				if (c.getName().equals(name)) {
					exists = true;
					break;
				}
			}
		}
		component.setName(componentType.getNamePrefix() + i);

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

		return component;
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
	 */
	public void fillWithDefaultProperties(Object object) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		// Extract properties.
		List<PropertyWrapper> properties = ComponentProcessor.getInstance().extractProperties(
				object.getClass());
		// Override with default values if available.
		for (PropertyWrapper property : properties) {
			Object defaultValue = ConfigurationManager.getInstance().readObject(
					Presenter.DEFAULTS_KEY_PREFIX + object.getClass().getName() + ":"
							+ property.getName(), null);
			if (defaultValue != null) {
				property.setValue(cloner.deepClone(defaultValue));
				property.writeTo(object);
			}
		}
	}

}
