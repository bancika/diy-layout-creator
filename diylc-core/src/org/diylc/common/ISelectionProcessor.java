package org.diylc.common;

import java.awt.Point;
import java.util.List;

import org.diylc.core.IDIYComponent;

public interface ISelectionProcessor {

	/**
	 * Returns the current {@link ComponentTransferable}.
	 * 
	 * @return
	 */
	List<IDIYComponent<?>> getSelectedComponents();

	/**
	 * Updates the selection with the specified list of component. Also, updates
	 * control point map with all components that are stuck to the newly
	 * selected components.
	 * 
	 * @param newSelection
	 */
	void updateSelection(List<IDIYComponent<?>> newSelection);

	/**
	 * Finds all components at the specified location, sorted by z-index from
	 * top to bottom.
	 * 
	 * @param point
	 * @return
	 */
	List<IDIYComponent<?>> findComponentsAt(Point point);

	/**
	 * Selects all components in the project.
	 */
	void selectAll();
}
