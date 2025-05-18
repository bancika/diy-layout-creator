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
package org.diylc.netlist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.diylc.core.IDIYComponent;

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
      }
    });
    return ordered;
  }

  @Override
  public String toString() {
    if (leaf != null)
      return leaf.toString();

    // If we only have one child, no need for parentheses
    if (children.size() == 1)
      return children.get(0).toString();

    StringBuilder sb = new StringBuilder("(");
    boolean first = true;
    for (Tree child : getOrderedChildren()) {
      if (!first)
        sb.append(" ").append(connectionType).append(" ");
      first = false;
      // Only include child's parentheses if it has multiple children with a different connection type
      String childStr = child.toString();
      if (child.children != null && child.children.size() > 1 && child.connectionType != connectionType) {
        sb.append(childStr);
      } else {
        sb.append(childStr.replaceAll("^\\((.*)\\)$", "$1"));
      }
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
    for (Tree child : children) {
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

  public String toAsciiString() {
    return String.join("<br>", toAsciiLines());
  }

  private List<String> toAsciiLines() {
    if (leaf != null) {
      return Arrays.asList(TreeAsciiUtil.generateString(TreeAsciiUtil.BOXH,
          1) + " " + leaf.toHTML() + " " + TreeAsciiUtil.generateString(TreeAsciiUtil.BOXH, 1));
    }

    List<Tree> children = getOrderedChildren();

    List<List<String>> childrenAscii =
        children.stream().map(c -> c.toAsciiLines()).collect(Collectors.toList());

    if (TreeConnectionType.Series.equals(connectionType)) {
      return TreeAsciiUtil.concatenateMultiLineSerial(
          TreeAsciiUtil.generateString(TreeAsciiUtil.BOXH, 1), childrenAscii);
    } else {
      return TreeAsciiUtil.concatenateMultiLineParallel(childrenAscii);
    }
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

  public Set<IDIYComponent<?>> extractComponents(Set<String> types) {
    Set<IDIYComponent<?>> res = new HashSet<IDIYComponent<?>>();
    if (leaf != null && types.contains(leaf.getComponent().getClass().getCanonicalName())) {
      res.add(leaf.getComponent());
    } else if (children != null) {
      for (Tree t : children) {
        Set<IDIYComponent<?>> childRes = t.extractComponents(types);
        if (childRes != null)
          res.addAll(childRes);
      }
    }
    return res;
  }

  public Tree locate(TreeLeaf l, boolean forceDirection) {
    if (leaf != null && leaf.equals(l, forceDirection))
      return this;
    if (children != null) {
      for (Tree t : children) {
        Tree childL = t.locate(l, forceDirection);
        if (childL != null) {
          //          if (t.getLeaf() != null)
          //            return this;
          return childL;
        }
      }
    }
    return null;
  }

  public Tree findCommonParent(Tree t1, Tree t2) {
    if (t1 == null)
      return t2;
    if (t2 == null)
      return t1;

    if (children == null)
      return null;

    if (!this.contains(t1) || !this.contains(t2))
      return null;

    Tree p1 = this;
    Tree p2 = this;
    for (Tree c : children) {
      if (c.contains(t1))
        p1 = c;
      if (c.contains(t2))
        p2 = c;
    }

    if (p1 != p2 || p1 == this || p2 == this)
      return this;

    return p1.findCommonParent(t1, t2);
  }

  public Tree findCommonParent(List<Tree> t) {
    if (t.contains(null)) {
      while (t.contains(null))
        t.remove(null);
      return findCommonParent(t);
    }

    if (t.size() == 0)
      return null;
    if (t.size() == 1)
      return t.get(0);
    if (t.size() == 2)
      return findCommonParent(t.get(0), t.get(1));

    List<Tree> remainder = t.subList(1, t.size());
    return findCommonParent(t.get(0), findCommonParent(remainder));
  }

  public Tree findParent(Tree t) {
    if (children == null)
      return null;
    if (children.contains(t))
      return this;
    for (Tree c : children) {
      Tree p = c.findParent(t);
      if (p != null)
        return p;
    }
    return null;
  }

  public boolean contains(Tree t) {
    if (this == t)
      return true;

    if (children == null)
      return false;
    if (children.contains(t))
      return true;
    for (Tree c : children)
      if (c.contains(t))
        return true;
    return false;
  }

  public interface ITreeWalker {

    void visit(Tree t);

    void visit(TreeLeaf l);
  }
}
