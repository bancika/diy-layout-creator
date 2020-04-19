/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.common;

import java.io.Serializable;

import javax.swing.Icon;

import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.KeywordPolicy;

/**
 * Entity class used to describe a component type.
 * 
 * @author Branislav Stojkovic
 * 
 * @see IDIYComponent
 */
public class ComponentType implements Serializable {


  private static final long serialVersionUID = 1L;
  
  private String name;
  private String description;
  private CreationMethod creationMethod;
  private String category;
  private String namePrefix;
  private String author;
  private Icon icon;
  private Class<? extends IDIYComponent<?>> instanceClass;
  private double zOrder;
  private boolean flexibleZOrder;
  private BomPolicy bomPolicy;
  private boolean autoEdit;
  private IComponentTransformer transformer;
  private KeywordPolicy keywordPolicy;
  private String keywordTag;
  private boolean enableCache;
  
  public ComponentType() {  
  }

  public ComponentType(String name, String description, CreationMethod creationMethod, String category,
      String namePrefix, String author, Icon icon, Class<? extends IDIYComponent<?>> instanceClass, double zOrder,
      boolean flexibleZOrder, BomPolicy bomPolicy, boolean autoEdit, IComponentTransformer transformer, 
      KeywordPolicy keywordPolicy, String keywordTag, boolean enableCache) {
    super();
    this.name = name;
    this.description = description;
    this.creationMethod = creationMethod;
    this.category = category;
    this.namePrefix = namePrefix;
    this.author = author;
    this.icon = icon;
    this.instanceClass = instanceClass;
    this.zOrder = zOrder;
    this.flexibleZOrder = flexibleZOrder;
    this.bomPolicy = bomPolicy;
    this.autoEdit = autoEdit;
    this.transformer = transformer;
    this.keywordPolicy = keywordPolicy;
    this.keywordTag = keywordTag;
    this.enableCache = enableCache;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public CreationMethod getCreationMethod() {
    return creationMethod;
  }

  public String getCategory() {
    return category;
  }

  public String getNamePrefix() {
    return namePrefix;
  }

  public String getAuthor() {
    return author;
  }

  public Icon getIcon() {
    return icon;
  }

  public Class<? extends IDIYComponent<?>> getInstanceClass() {
    return instanceClass;
  }

  public double getZOrder() {
    return zOrder;
  }

  public boolean isFlexibleZOrder() {
    return flexibleZOrder;
  }

  public BomPolicy getBomPolicy() {
    return bomPolicy;
  }

  public boolean isAutoEdit() {
    return autoEdit;
  }

  public IComponentTransformer getTransformer() {
    return transformer;
  }

  public KeywordPolicy getKeywordPolicy() {
    return keywordPolicy;
  }

  public String getKeywordTag() {
    return keywordTag;
  }
  
  public boolean getEnableCache() {
    return enableCache;
  }

  @Override
  public String toString() {
    return name;
  }
}
