/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.core;

import java.awt.Font;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import org.diylc.appframework.update.VersionNumber;

import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.MultiLineText;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

/**
 * Entity class that defines a project. Contains project properties and a collection of
 * components.This class is serialized to file. Some filed getters are tagged with
 * {@link EditableProperty} to enable for user to edit them.
 * 
 * @author Branislav Stojkovic
 */
public class Project implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  public static String DEFAULT_TITLE = "New Project";
  public static Size DEFAULT_WIDTH = new Size(29d, SizeUnit.cm);
  public static Size DEFAULT_HEIGHT = new Size(21d, SizeUnit.cm);
  public static Size DEFAULT_GRID_SPACING = new Size(0.1d, SizeUnit.in);
  public static Font DEFAULT_FONT = new Font("Square721 BT", Font.PLAIN, 14);

  private VersionNumber fileVersion;

  private String title;
  private String author;
  private String description;
  private Size width;
  private Size height;
  private Size gridSpacing;
  private Integer dotSpacing;
  private List<IDIYComponent<?>> components;
  private Set<Set<IDIYComponent<?>>> groups;
  private Set<ComponentGroup> groupsEx;
  private Set<Integer> lockedLayers;
  private Set<IDIYComponent<?>> lockedComponents;
  private Set<Integer> hiddenLayers;
  private Font font = DEFAULT_FONT;

  public Project() {
    components = new ArrayList<IDIYComponent<?>>();
    groups = new HashSet<Set<IDIYComponent<?>>>();
    groupsEx = new HashSet<>();
    lockedLayers = new HashSet<Integer>();
    hiddenLayers = new HashSet<Integer>();
    lockedComponents = new HashSet<IDIYComponent<?>>();
    title = DEFAULT_TITLE;
    author = System.getProperty("user.name");
    width = DEFAULT_WIDTH;
    height = DEFAULT_HEIGHT;
    gridSpacing = DEFAULT_GRID_SPACING;
  }

  @EditableProperty(defaultable = false, sortOrder = 1)
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @EditableProperty(sortOrder = 3)
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @MultiLineText
  @EditableProperty(defaultable = false, sortOrder = 2)
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @EditableProperty(sortOrder = 4)
  public Size getWidth() {
    return width;
  }

  public void setWidth(Size width) {
    this.width = width;
  }

  @EditableProperty(sortOrder = 5)
  public Size getHeight() {
    return height;
  }

  public void setHeight(Size height) {
    this.height = height;
  }

  @EditableProperty(name = "Grid Spacing", validatorClass = SpacingValidator.class, sortOrder = 6)
  public Size getGridSpacing() {
    return gridSpacing;
  }

  public void setGridSpacing(Size gridSpacing) {
    this.gridSpacing = gridSpacing;
  }
  
  @EditableProperty(name = "Dot Spacing", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Integer getDotSpacing() {
    if (dotSpacing == null)
      dotSpacing = 1;
    return dotSpacing;
  }
  
  public void setDotSpacing(Integer dotSpacing) {
    this.dotSpacing = dotSpacing;
  }

  /**
   * List of components sorted by z-order ascending.
   * s
   * @return
   */
  public List<IDIYComponent<?>> getComponents() {
    return components;
  }

  public Set<ComponentGroup> getGroupsEx() {
    if (groupsEx == null) {
      groupsEx = new HashSet<>();

      if (groups != null && !groups.isEmpty()) {
        for (Set<IDIYComponent<?>> group : groups) {
          ComponentGroup newGroup = ComponentGroup.from(group, null);
          groupsEx.add(newGroup);
        }
        groups.clear();
      }
    }
    return groupsEx;
  }

  public Set<IDIYComponent<?>> getLockedComponents() {
    if (lockedComponents == null)
      lockedComponents = new HashSet<IDIYComponent<?>>();
    return lockedComponents;
  }

  public Set<Integer> getLockedLayers() {
    return lockedLayers;
  }
  
  public Set<Integer> getHiddenLayers() {
    if (hiddenLayers == null)
      hiddenLayers = new HashSet<Integer>();
    return hiddenLayers;
  }

  public VersionNumber getFileVersion() {
    return fileVersion;
  }

  public void setFileVersion(VersionNumber fileVersion) {
    this.fileVersion = fileVersion;
  }
  
  @EditableProperty(name = "Default Font")
  public Font getFont() {
    if (font == null)
      font = DEFAULT_FONT;
    return font;
  }
  
  public void setFont(Font font) {
    this.font = font;
  }
  
  @EditableProperty(name = "Default Font Size")
  public int getFontSize() {
    if (font == null)
      font = DEFAULT_FONT;
    return font.getSize();
  }

  public void setFontSize(int size) {
    font = font.deriveFont((float) size);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((author == null) ? 0 : author.hashCode());
    result = prime * result + ((components == null) ? 0 : components.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((height == null) ? 0 : height.hashCode());
    result = prime * result + ((title == null) ? 0 : title.hashCode());
    result = prime * result + ((width == null) ? 0 : width.hashCode());
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
    Project other = (Project) obj;
    if (author == null) {
      if (other.author != null)
        return false;
    } else if (!author.equals(other.author))
      return false;
    if (components == null) {
      if (other.components != null)
        return false;
    } else if (components.size() != other.components.size()) {
      return false;
    } else {
      Iterator<IDIYComponent<?>> i1 = components.iterator();
      Iterator<IDIYComponent<?>> i2 = other.components.iterator();
      while (i1.hasNext()) {
        IDIYComponent<?> c1 = i1.next();
        IDIYComponent<?> c2 = i2.next();
        if (!c1.equalsTo(c2))
          return false;
      }
    }
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (height == null) {
      if (other.height != null)
        return false;
    } else if (!height.equals(other.height))
      return false;
    if (title == null) {
      if (other.title != null)
        return false;
    } else if (!title.equals(other.title))
      return false;
    if (width == null) {
      if (other.width != null)
        return false;
    } else if (!width.equals(other.width))
      return false;
    if (gridSpacing == null) {
      if (other.gridSpacing != null)
        return false;
    } else if (!gridSpacing.equals(other.gridSpacing))
      return false;
    if (font == null) {
      if (other.font != null)
        return false;
    } else if (!font.getFamily().equals(other.font.getFamily()) || font.getSize() != other.font.getSize() || font.getStyle() != other.font.getStyle())
      return false;
    if (groups == null) {
      if (other.groups != null)
        return false;
    } else if (!groups.equals(other.groups))
      return false;
    if (groupsEx == null) {
      if (other.groupsEx != null)
        return false;
    } else if (!groupsEx.equals(other.groupsEx))
      return false;
    if (lockedLayers == null) {
      if (other.lockedLayers != null)
        return false;
    } else if (!lockedLayers.equals(other.lockedLayers))
      return false;
    if (getLockedComponents() == null) {
      if (other.lockedComponents != null)
        return false;
    } else if (!getLockedComponents().equals(other.lockedComponents))
      return false;
    if (hiddenLayers == null) {
      if (other.hiddenLayers != null)
        return false;
    } else if (!hiddenLayers.equals(other.hiddenLayers))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return title;
  }

  public static class SpacingValidator extends PositiveMeasureValidator {

    @Override
    public void validate(Object owner, Object value) throws ValidationException {
      super.validate(owner, value);
      Size size = (Size) value;
      if (size.compareTo(new Size(0.1d, SizeUnit.mm)) < 0) {
        throw new ValidationException("must be at least 0.1mm");
      }
      if (size.compareTo(new Size(1d, SizeUnit.in)) > 0) {
        throw new ValidationException("must be less than 1in");
      }
    }
  }

  @Override
  public Project clone() {
    Project project = new Project();
    project.setTitle(this.getTitle());
    project.setAuthor(this.getAuthor());
    project.setDescription(this.getDescription());
    project.setFileVersion(this.getFileVersion());
    project.setGridSpacing(this.getGridSpacing());    
    project.setHeight(this.getHeight());
    project.setWidth(this.getWidth());
    project.getLockedLayers().addAll(this.getLockedLayers());
    project.getHiddenLayers().addAll(this.getHiddenLayers());    
    project.setFont(this.getFont());    

    Map<IDIYComponent<?>, IDIYComponent<?>> cloneMap = new HashMap<IDIYComponent<?>, IDIYComponent<?>>();

    for (IDIYComponent<?> component : this.components) {
      try {
        IDIYComponent<?> clone = component.clone();        
        project.getComponents().add(clone);
        cloneMap.put(component, clone);
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }

    for (ComponentGroup group : this.getGroupsEx()) {
      project.getGroupsEx().add(new ComponentGroup(group.getComponentIds(), group.getName()));
    }
    
    for (IDIYComponent<?> component : this.getLockedComponents()) {
      project.lockedComponents.add(cloneMap.get(component));
    }
    return project;
  }
}
