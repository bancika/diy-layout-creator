package org.diylc.test;

import java.awt.geom.Area;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.presenter.ComponentArea;

public class Snapshot implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private Project project;
  private Set<IDIYComponent<?>> selection;
  private Map<IDIYComponent<?>, ComponentArea> areas;
  private Set<Area> continuityAreas;
  
  public Snapshot() {   
  }
      
  public Snapshot(Project project, Set<IDIYComponent<?>> selection,
      Map<IDIYComponent<?>, ComponentArea> areas, Set<Area> continuityAreas) {
    super();
    this.project = project;
    this.selection = selection;
    this.areas = areas;
    this.continuityAreas = continuityAreas;
  }
  
  public Project getProject() {
    return project;
  }
  
  public Set<IDIYComponent<?>> getSelection() {
    return selection;
  }
  
  public Map<IDIYComponent<?>, ComponentArea> getAreas() {
    return areas;
  }
  
  public Set<Area> getContinuityAreas() {
    return continuityAreas;
  }
  
  public boolean equalsTo(Snapshot other) {
    if (!project.equals(other.getProject()))
      return false;
    if (selection == null) {
      if (other.selection != null)
        return false;
    } else if (selection.size() != other.selection.size()) {
      return false;
    } else {
//      IDIYComponent<?> c1= selection.iterator().next();
//      IDIYComponent<?> c2=other.selection.iterator().next();
//      if (!c1.equalsTo(c2))
//        return false;
//      for(IDIYComponent<?> c : selection)
//        if (!other.selection.contains(c))
//          return false;
//      for(IDIYComponent<?> c : other.selection)
//        if (!selection.contains(c))
//          return false;
    }
//    if (!areas.equals(other.getAreas()))
//      return false;
//    if (!continuityAreas.equals(other.getContinuityAreas()))
//      return false;
    return true;
  }
}
