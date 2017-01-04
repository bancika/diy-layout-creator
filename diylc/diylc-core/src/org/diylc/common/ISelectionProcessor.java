package org.diylc.common;

import java.awt.Point;
import java.util.Collection;
import java.util.List;

import org.diylc.core.ExpansionMode;
import org.diylc.core.IDIYComponent;

public interface ISelectionProcessor {
  
  public static final int CLOCKWISE = 1;
  public static final int COUNTER_CLOCKWISE = 1;
  
  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;

  /**
   * Returns the current {@link ComponentTransferable}.
   * 
   * @return
   */
  Collection<IDIYComponent<?>> getSelectedComponents();

  /**
   * Updates the selection with the specified list of component. Also, updates control point map
   * with all components that are stuck to the newly selected components.
   * 
   * @param newSelection
   */
  void updateSelection(Collection<IDIYComponent<?>> newSelection);

  /**
   * Finds all components at the specified location, sorted by z-index from top to bottom.
   * 
   * @param point
   * @return
   */
  List<IDIYComponent<?>> findComponentsAt(Point point);

  /**
   * Expands the current selection to include surrounding components. Options are controlled with
   * <code>expansionMode</code> flag.
   * 
   * @param expansionMode
   */
  void expandSelection(ExpansionMode expansionMode);

  /**
   * Selects all components in the project.
   * 
   * @param int layer if > 0, designates which layer to select. If <= 0 we should select all
   *        regardless of layer
   */
  void selectAll(int layer);

  /**
   * Rotates selection for 90 degrees.
   * 
   * @param direction 1 for clockwise, -1 for counter-clockwise
   */
  void rotateSelection(int direction);
  
  /**
   * Groups all selected components.
   */
  void groupSelectedComponents();

  /**
   * Ungroups all selected components.
   */
  void ungroupSelectedComponents();
  
  void mirrorSelection(int direction);
}
