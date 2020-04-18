package org.diylc.editor;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import org.diylc.common.IProjectEditor;
import org.diylc.components.AbstractCurvedComponent.PointCount;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.components.connectivity.AWG;
import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.semiconductors.AbstractTransistorPackage;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.measures.Size;
import org.diylc.presenter.InstantiationManager;

public class FlexibleLeadsEditor implements IProjectEditor {
  
  private InstantiationManager instantiationManager;

  @Override
  public Set<IDIYComponent<?>> edit(Project project, Set<IDIYComponent<?>> selection) {
    Set<IDIYComponent<?>> newSelection = new HashSet<IDIYComponent<?>>(selection);
    for (IDIYComponent<?> c : selection) {
      if (c instanceof AbstractLeadedComponent<?>)
        addLeads((AbstractLeadedComponent<?>) c, project, newSelection);
    }
    
    return newSelection;
  }
  
  private void addLeads(AbstractLeadedComponent<?> c, Project project, Set<IDIYComponent<?>> newSelection) {
      AbstractLeadedComponent<?> leaded = (AbstractLeadedComponent<?>)c;
      Size l = leaded.getLength();
      // do not apply to jumpers, etc
      if (l == null)
        return;
      
      Point p1 = new Point(leaded.getControlPoint(0));
      Point p2 = new Point(leaded.getControlPoint(1));
      double d = p1.distance(p2);
      double len = l.convertToPixels() + 2;
              
      double diff = (d - len)  / 2;
      
      if (diff < 20)
        return;
      
      // find the center and angle
      double centerX = (p1.x + p2.x) / 2.0;
      double centerY = (p1.y + p2.y) / 2.0;
      double theta = Math.atan2(p2.y - p1.y, p2.x - p1.x);
      // shorten the component to match the size of the body
      Point newP1 = new Point((int)Math.round(centerX - Math.cos(theta) * len / 2), (int)Math.round(centerY - Math.sin(theta) * len / 2));
      Point newP2 = new Point((int)Math.round(centerX + Math.cos(theta) * len / 2), (int)Math.round(centerY + Math.sin(theta) * len / 2));
      leaded.setControlPoint(newP1, 0);
      leaded.setControlPoint(newP2, 1);
             
      // create leads
      HookupWire w1 = new HookupWire();
      w1.setGauge(AWG._26);
      w1.setLeadColor(leaded.getLeadColor());
      w1.setPointCount(PointCount.THREE);
      w1.setControlPoint(p1, 0);
      w1.setControlPoint(new Point((int)Math.round(centerX - Math.cos(theta) * (len / 2 + diff / 2)), (int)Math.round(centerY - Math.sin(theta) * (len / 2 + diff / 2))), 1);   
      w1.setControlPoint(newP1, 2);
      newSelection.add(w1);
      
      HookupWire w2 = new HookupWire();
      w2.setGauge(AWG._26);
      w2.setLeadColor(leaded.getLeadColor());
      w2.setPointCount(PointCount.THREE);
      w2.setControlPoint(p2, 0);
      w2.setControlPoint(new Point((int)Math.round(centerX + Math.cos(theta) * (len / 2 + diff / 2)), (int)Math.round(centerY + Math.sin(theta) * (len / 2 + diff / 2))), 1);       
      w2.setControlPoint(newP2, 2);
      newSelection.add(w2);
      
      // inject leads right before the component
      int index = project.getComponents().indexOf(c);
      project.getComponents().add(index, w1);
      project.getComponents().add(index, w2);    
  }
  
  private void addLeads(AbstractTransistorPackage c, Project project, Set<IDIYComponent<?>> newSelection) {
    if (c.getFolded()) {
      
    } else {
      
    }
  }

  @Override
  public String getEditAction() {
    return "Add Flexible Leads";
  }

  public InstantiationManager getInstantiationManager() {
    if (instantiationManager == null)
      instantiationManager = new InstantiationManager();
    return instantiationManager;
  }
}
