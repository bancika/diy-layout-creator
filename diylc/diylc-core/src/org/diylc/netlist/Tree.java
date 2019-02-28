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
package org.diylc.netlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Tree {

  private List<Tree> children;
  private TreeConnectionType connectionType;  
  private TreeLeaf leaf;
  
  public Tree(TreeConnectionType connectionType) {
    this.children = new ArrayList<Tree>();
    this.connectionType = connectionType;
  }
  
  public Tree(TreeLeaf leaf) {
    this.leaf = leaf;
  }

  public Tree(List<Tree> children, TreeConnectionType connectionType) {
    this.children = children;
    this.connectionType = connectionType;
  }

  public List<Tree> getChildren() {
    return children;
  }
  
  public void trimChildrenLeft(int count) {
    children = children.subList(count, children.size());
  }
  
  public void trimChildrenRight(int count) {
    children = children.subList(0, children.size() - count);
  }

  public TreeConnectionType getConnectionType() { 
    return connectionType;
  }   
  
  public TreeLeaf getLeaf() {
    return leaf;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((children == null) ? 0 : children.hashCode());
    result = prime * result + ((connectionType == null) ? 0 : connectionType.hashCode());
    result = prime * result + ((leaf == null) ? 0 : leaf.hashCode());
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
    Tree other = (Tree) obj;
    if (children == null) {
      if (other.children != null)
        return false;
    } else if (!children.equals(other.children))
      return false;
    if (connectionType != other.connectionType)
      return false;
    if (leaf == null) {
      if (other.leaf != null)
        return false;
    } else if (!leaf.equals(other.leaf))
      return false;
    return true;
  }
  
  private List<Tree> getOrderedChildren() {
    if (getConnectionType() == TreeConnectionType.Series)
      return children;
    List<Tree> ordered = new ArrayList<Tree>(children);
    Collections.sort(ordered, new Comparator<Tree>() {

      @Override
      public int compare(Tree o1, Tree o2) {
        return o1.toString().compareToIgnoreCase(o2.toString());
      }});
    return ordered;
  }

  @Override
  public String toString() {
    if (leaf != null)
      return leaf.toString();
    
    StringBuilder sb = new StringBuilder("(");
    boolean first = true;
    for(Tree child : getOrderedChildren()) {
      if (!first)
        sb.append(" " ).append(connectionType).append(" ");
      first = false;
      sb.append(child);
    }
    sb.append(")");
    return sb.toString();
  }
  
  public String toHTML(int depth) {
    if (leaf != null)
      return leaf.toHTML();
    
    List<Tree> children = getOrderedChildren();
    
    StringBuilder sb = new StringBuilder();
    if (depth > 0 && children.size() > 1)
      sb.append("(");
    boolean first = true;
    for(Tree child : children) {
      if (!first) {
        sb.append("&nbsp;").append(connectionType.toHTML()).append("&nbsp;");
        if (depth == 0)
          sb.append("<br>");
      }
      first = false;
      sb.append(child.toHTML(depth + children.size() > 1 ? 1 : 0));
    }
    if (depth > 0 && children.size() > 1)
      sb.append(")");
    return sb.toString();
  }
  
  @Override
  protected Object clone() throws CloneNotSupportedException {
    if (leaf != null)
      return new Tree(leaf);
    List<Tree> newChildren = new ArrayList<Tree>();
    for (Tree t : children)
      newChildren.add((Tree) t.clone());
    return new Tree(newChildren, connectionType);
  }
  
  public Tree filter(Set<String> types) {
    if (leaf != null) {
      if (types.contains(leaf.getComponent().getClass().getCanonicalName()))        
        return new Tree(leaf);
      return null;
    }
    List<Tree> newChildren = new ArrayList<Tree>();
    for (Tree t : children) {
      Tree child = t.filter(types);
      if (child != null)
        newChildren.add(child);
    }
    if (newChildren.isEmpty())
      return null;
    return new Tree(newChildren, connectionType);
  }
  
  public void walk(ITreeWalker walker) {
    if (leaf != null) {
      walker.visit(leaf);
    } else {
      for (Tree t : children) {
        walker.visit(t);
        t.walk(walker);
      }
    }
  }
  
  public interface ITreeWalker {
    
    void visit(Tree t);
    
    void visit(TreeLeaf l);
  }
}
