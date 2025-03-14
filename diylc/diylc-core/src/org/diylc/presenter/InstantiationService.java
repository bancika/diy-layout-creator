package org.diylc.presenter;

import java.awt.geom.Point2D;
import java.util.List;
import org.diylc.clipboard.ComponentTransferable;
import org.diylc.common.ComponentType;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.Template;
import org.diylc.core.measures.Size;

public interface InstantiationService {
    // Component type slot management
    ComponentType getComponentTypeSlot();
    Template getTemplate();
    String[] getModel();
    List<IDIYComponent<?>> getComponentSlot();
    void setComponentTypeSlot(ComponentType componentTypeSlot, Template template, String[] model, 
            Project currentProject, boolean forceInstantiate) throws Exception;
            
    // Point-by-point creation
    Point2D getFirstControlPoint();
    Point2D getPotentialControlPoint();
    void setPotentialControlPoint(Point2D potentialControlPoint);
    void instatiatePointByPoint(Point2D scaledPoint, Project currentProject) throws Exception;
    boolean updatePointByPoint(Point2D scaledPoint);
    boolean updateSingleClick(Point2D point, boolean snapToGrid, Size gridSpacing);
    
    // Component operations
    void pasteComponents(ComponentTransferable componentTransferable, Point2D scaledPoint, 
            boolean snapToGrid, Size gridSpacing, boolean autoGroup, Project currentProject, 
            boolean assignNewNames);
    String createUniqueName(ComponentType componentType, List<IDIYComponent<?>> components);
    void fillWithDefaultProperties(Object object, Template template);
    void loadComponentShapeFromTemplate(IDIYComponent<?> component, Template template);
    void tryToRotateComponentSlot();
} 