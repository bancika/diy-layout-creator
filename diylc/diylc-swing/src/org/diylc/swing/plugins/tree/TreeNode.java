/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.swing.plugins.tree;

import java.awt.event.MouseListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.diylc.common.ComponentType;

public class TreeNode {
  private ComponentType componentType;
  private String category;
  private MouseListener clickListener;
  private boolean isLeaf;
  
  private final Pattern contributedPattern = Pattern.compile("^(.*)\\[(.*)\\]");  

  public TreeNode(ComponentType componentType, MouseListener clickListener) {
    super();
    this.componentType = componentType;
    this.clickListener = clickListener;
    this.isLeaf = true;
  }

  public TreeNode(String category, MouseListener clickListener, boolean isLeaf) {
    super();
    this.category = category;
    this.clickListener = clickListener;
    this.isLeaf = isLeaf;
  }

  public ComponentType getComponentType() {
    return componentType;
  }

  public MouseListener getClickListener() {
    return clickListener;
  }
  
  public boolean isLeaf() {
    return isLeaf;
  }
  
  public String forDisplay() {
    if (componentType == null) {
      String display = category;
      Matcher match = contributedPattern.matcher(display);
      if (match.find()) {
        String name = match.group(1);
        String owner = match.group(2);
        display = name + "<font color='gray'>[" + owner + "]</font>"; 
      }
      return display;
    }
    return componentType.getName();
  }

  @Override
  public String toString() {
    return componentType == null ? category : componentType.getName();
  }
}